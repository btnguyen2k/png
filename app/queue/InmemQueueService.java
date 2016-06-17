package queue;

import com.github.ddth.queue.impl.InmemQueue;
import com.github.ddth.queue.impl.universal.UniversalInmemQueue;

public class InmemQueueService extends AbstractSingleClientIdQueueService<InmemQueue> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected UniversalInmemQueue createMyOwnQueueInstance() {
        UniversalInmemQueue queue = new UniversalInmemQueue();
        queue.init();
        return queue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void destroyMyOwnQueueInstance(InmemQueue queue) {
        queue.destroy();
    }

}
