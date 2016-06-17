package queue;

import com.github.ddth.queue.impl.JdbcQueue;
import com.github.ddth.queue.impl.universal.UniversalJdbcQueue;

public class JdbcQueueService extends AbstractSingleClientIdQueueService<JdbcQueue> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected UniversalJdbcQueue createMyOwnQueueInstance() {
        UniversalJdbcQueue queue = new UniversalJdbcQueue();
        queue.init();
        return queue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void destroyMyOwnQueueInstance(JdbcQueue queue) {
        queue.destroy();
    }

}
