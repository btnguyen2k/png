package modules.cluster;

import com.github.ddth.queue.IQueueMessage;

import modules.registry.IRegistry;

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

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean processQueueMessage(IQueueMessage queueMessage) {
        return true;
    }

}
