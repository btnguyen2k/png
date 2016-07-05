package modules.cluster.workers;

import com.github.ddth.queue.IQueueMessage;

import bo.pushtoken.IPushTokenDao;
import bo.pushtoken.PushTokenBo;
import modules.cluster.BaseQueueThread;
import modules.registry.IRegistry;
import play.Logger;
import queue.message.BaseMessage;
import queue.message.DeletePushNotificationMessage;
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
        if (result) {

        }
        return result;
    }

    private boolean deletePushNotification(DeletePushNotificationMessage msg) {
        IPushTokenDao pushTokenDao = getRegistry().getPushTokenDao();
        String appId = msg.getAppId();
        String token = msg.getToken();
        String os = msg.getOs();
        PushTokenBo pushToken = pushTokenDao.getPushToken(appId, token, os);
        return pushToken != null ? pushTokenDao.delete(pushToken) : true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean processQueueMessage(IQueueMessage queueMsg) {
        Object data = queueMsg.qData();
        if (data instanceof byte[]) {
            BaseMessage baseMsg = PngUtils.fromBytes((byte[]) data, BaseMessage.class);
            Logger.debug("\tMessage from queue [" + baseMsg.getClass().getSimpleName() + "]: "
                    + baseMsg);
            if (baseMsg instanceof UpdatePushNotificationMessage) {
                return updatePushNotification((UpdatePushNotificationMessage) baseMsg);
            } else if (baseMsg instanceof DeletePushNotificationMessage) {
                return deletePushNotification((DeletePushNotificationMessage) baseMsg);
            }
        } else {
            Logger.debug("\tMessage from queue: " + data);
        }
        return false;
    }

}
