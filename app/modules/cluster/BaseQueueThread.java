package modules.cluster;

import com.github.ddth.queue.IQueue;
import com.github.ddth.queue.IQueueMessage;

import modules.registry.IRegistry;
import play.Logger;

public abstract class BaseQueueThread extends Thread {
    private IQueue queue;
    private IRegistry registry;
    private boolean stop = false;

    public BaseQueueThread(String name, IRegistry registry, IQueue queue) {
        super(name);
        this.registry = registry;
        this.queue = queue;
    }

    public void stopThread() {
        stop = true;
    }

    protected IQueue getQueue() {
        return queue;
    }

    protected IRegistry getRegistry() {
        return registry;
    }

    protected IQueueMessage takeFromQueue() {
        IQueueMessage queueMessage = null;
        try {
            queueMessage = queue.take();
        } catch (Exception e) {
            Logger.error("Error while polling from queue: " + e.getMessage(), e);
        }
        return queueMessage;
    }

    protected void finishQueueMessage(IQueueMessage queueMessage) {
        try {
            queue.finish(queueMessage);
        } catch (Exception e) {
            Logger.error("Error while finishing queue message: " + e.getMessage(), e);
        }
    }

    protected void sleepForAwhile() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    protected abstract boolean processQueueMessage(IQueueMessage queueMessage);

    public void run() {
        while (!stop && !isInterrupted()) {
            IQueueMessage queueMessage = null;
            try {
                queueMessage = takeFromQueue();
                if (queueMessage == null) {
                    sleepForAwhile();
                    continue;
                }
                Logger.debug("Got message from queue [" + queueMessage.getClass().getSimpleName()
                        + "]: " + queueMessage);
                if (!processQueueMessage(queueMessage)) {
                    Logger.warn("Queue message was not processed ["
                            + queueMessage.getClass().getSimpleName() + "]: " + queueMessage);
                }
            } catch (Exception e) {
                Logger.error(e.getMessage(), e);
            } finally {
                if (queueMessage != null) {
                    finishQueueMessage(queueMessage);
                }
            }
        }
    }
}
