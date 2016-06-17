package queue;

import com.github.ddth.queue.IQueue;

/**
 * Abstract implementation of single client-id {@link IQueueService}. All
 * clients will share a single view of queue data.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class AbstractSingleClientIdQueueService<T extends IQueue>
        implements IQueueService {

    private boolean myOwnQueueInstance = false;
    private T queue;

    public IQueue getQueue() {
        return queue;
    }

    public AbstractSingleClientIdQueueService<T> setQueue(T queue) {
        this.queue = queue;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IQueue getQueue(String clientId) {
        return queue;
    }

    protected abstract T createMyOwnQueueInstance();

    protected abstract void destroyMyOwnQueueInstance(T queue);

    public AbstractSingleClientIdQueueService<T> init() {
        if (queue == null) {
            queue = createMyOwnQueueInstance();
        }
        return this;
    }

    public void destroy() {
        if (myOwnQueueInstance && queue != null) {
            destroyMyOwnQueueInstance(queue);
        }
    }
}
