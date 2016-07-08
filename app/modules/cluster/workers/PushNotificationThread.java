package modules.cluster.workers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.queue.IQueueMessage;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.PayloadBuilder;

import bo.app.AppBo;
import bo.app.IAppDao;
import bo.pushtoken.PushTokenBo;
import modules.cluster.BaseQueueThread;
import modules.registry.IRegistry;
import play.Logger;
import queue.message.BaseMessage;
import queue.message.DeliverPushNotificationMessage;
import utils.PngConstants;
import utils.PngUtils;

/**
 * Thread to process messages in push-notification queue.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class PushNotificationThread extends BaseQueueThread {

    public PushNotificationThread(IRegistry registry) {
        super(PushNotificationThread.class.getSimpleName(), registry,
                registry.getQueuePushNotifications());
    }

    private boolean deliverPushNotification(DeliverPushNotificationMessage msg) {
        IAppDao appDao = getRegistry().getAppDao();
        AppBo app = appDao.getApp(msg.getAppId());
        if (app == null) {
            Logger.error("App [" + msg.getAppId() + "] not found!");
            return false;
        }
        PushTokenBo[] pushTokens = msg.getPushTokens();
        if (pushTokens == null || pushTokens.length == 0) {
            Logger.error("Invalid message: push token list is empty!");
            return false;
        }

        String payload;
        {
            PayloadBuilder payloadBuilder = APNS.newPayload().alertBody(msg.getContent());
            String title = msg.getTitle();
            if (!StringUtils.isBlank(title)) {
                payloadBuilder.alertTitle(title);
            }
            payload = payloadBuilder.build();
        }
        ApnsService apnsService = null;
        try {
            // build ApnsService
            try (ByteArrayInputStream bais = new ByteArrayInputStream(app.getIOSP12ContentRaw())) {
                apnsService = APNS.newService().withCert(bais, app.getIOSP12Password())
                        .withAppleDestination(true).build();
            } catch (IOException e) {
                Logger.error(e.getMessage(), e);
            }

            Set<String> tokens = new HashSet<>();
            for (PushTokenBo pushToken : pushTokens) {
                if (StringUtils.equalsIgnoreCase(pushToken.getOs(), PngConstants.OS_IOS)) {
                    tokens.add(pushToken.getToken());
                } else {
                    Logger.warn(
                            "Push notification does not support this OS/Platform: " + pushToken);
                }
                if (tokens.size() == 10) {
                    apnsService.push(tokens, payload);
                    tokens = new HashSet<>();
                }
            }
            if (tokens.size() > 0) {
                apnsService.push(tokens, payload);
            }
        } finally {
            if (apnsService != null) {
                apnsService.stop();
            }
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

            if (baseMsg instanceof DeliverPushNotificationMessage) {
                return deliverPushNotification((DeliverPushNotificationMessage) baseMsg);
            }
        } else {
            if (Logger.isDebugEnabled()) {
                Logger.debug("\tMessage from queue: " + data);
            }
        }
        return false;
    }

}
