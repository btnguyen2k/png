package modules.cluster.workers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.queue.IQueue;
import com.github.ddth.queue.IQueueMessage;

import bo.pushtoken.IPushTokenDao;
import bo.pushtoken.PushTokenBo;
import modules.cluster.BaseQueueThread;
import modules.registry.IRegistry;
import play.Logger;
import queue.message.BaseMessage;
import queue.message.DeletePushNotificationMessage;
import queue.message.SendPushNotificationsMessage;
import queue.message.UpdatePushNotificationMessage;
import utils.PngUtils;

/**
 * Thread to process messages in app-event queue.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class AppEventThread extends BaseQueueThread {

    public AppEventThread(IRegistry registry) {
        super(AppEventThread.class.getSimpleName(), registry, registry.getQueueAppEvents());
    }

    private boolean deletePushNotification(DeletePushNotificationMessage msg) {
        IPushTokenDao pushTokenDao = getRegistry().getPushTokenDao();
        String appId = msg.getAppId();
        String token = msg.getToken();
        String os = msg.getOs();
        PushTokenBo pushToken = pushTokenDao.getPushToken(appId, token, os);
        return pushToken != null ? pushTokenDao.delete(pushToken) : true;
    }

    private boolean updatePushNotification(UpdatePushNotificationMessage msg) {
        IPushTokenDao pushTokenDao = getRegistry().getPushTokenDao();
        String appId = msg.getAppId();
        String token = msg.getToken();
        String os = msg.getOs();
        PushTokenBo pushToken = pushTokenDao.getPushToken(appId, token, os);
        boolean result = false;
        if (pushToken == null) {
            // new token
            pushToken = PushTokenBo.newInstance(appId, token, os);
            pushToken.setTags(msg.getTags());
            result = pushTokenDao.create(pushToken);
        } else {
            pushToken.setTags(msg.getTags());
            result = pushTokenDao.update(pushToken);
        }
        return result;
    }

    private boolean sendPushNotifications(SendPushNotificationsMessage msg) {
        Set<PushTokenBo> pushTokensToSend = new HashSet<>();
        IPushTokenDao pushTokenDao = getRegistry().getPushTokenDao();
        String appId = msg.getAppId();
        String title = msg.getTitle();
        String content = msg.getContent();

        SendPushNotificationsMessage.Target[] targets = msg.getTargets();
        for (SendPushNotificationsMessage.Target target : targets) {
            String token = target.getToken();
            String os = target.getOs();
            String[] tags = target.getTags();
            if (!StringUtils.isBlank(token) && !StringUtils.isBlank(os)) {
                PushTokenBo pushToken = pushTokenDao.getPushToken(appId, token, os);
                if (pushToken != null) {
                    pushTokensToSend.add(pushToken);
                }
            } else {
                PushTokenBo[] pushTokens = pushTokenDao.lookupPushTokens(tags);
                if (pushTokens != null) {
                    for (PushTokenBo pushToken : pushTokens) {
                        pushTokensToSend.add(pushToken);
                    }
                }
            }
        }

        if (Logger.isDebugEnabled()) {
            Logger.debug("Sending notification [" + title + "/" + content + "] from [" + appId
                    + "] to " + pushTokensToSend);
        }

        if (pushTokensToSend.size() == 0) {
            return false;
        }

        IQueue queue = getRegistry().getQueuePushNotifications();
        Collection<PushTokenBo> pushTokens = new ArrayList<>();
        for (PushTokenBo pushToken : pushTokensToSend) {
            pushTokens.add(pushToken);
            if (pushTokens.size() == 10) {
                PngUtils.queuePushNotificationDelivery(queue, appId, title, content,
                        pushTokens.toArray(PushTokenBo.EMPTY_ARRAY));
                pushTokens = new ArrayList<>();
            }
        }
        if (pushTokens.size() > 0) {
            PngUtils.queuePushNotificationDelivery(queue, appId, title, content,
                    pushTokens.toArray(PushTokenBo.EMPTY_ARRAY));
            pushTokens = new ArrayList<>();
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean processQueueMessage(IQueueMessage queueMsg) {
        Object data = queueMsg.qData();
        if (data instanceof byte[]) {
            BaseMessage baseMsg = PngUtils.fromBytes((byte[]) data, BaseMessage.class);

            if (Logger.isDebugEnabled()) {
                Logger.debug("\tMessage from queue [" + baseMsg.getClass().getSimpleName() + "]: "
                        + baseMsg);
            }

            if (baseMsg instanceof UpdatePushNotificationMessage) {
                return updatePushNotification((UpdatePushNotificationMessage) baseMsg);
            } else if (baseMsg instanceof DeletePushNotificationMessage) {
                return deletePushNotification((DeletePushNotificationMessage) baseMsg);
            } else if (baseMsg instanceof SendPushNotificationsMessage) {
                return sendPushNotifications((SendPushNotificationsMessage) baseMsg);
            }
        } else {
            if (Logger.isDebugEnabled()) {
                Logger.debug("\tMessage from queue: " + data);
            }
        }
        return false;
    }

}
