package queue;

import com.github.ddth.queue.IQueue;

public interface IQueueService {
    public IQueue getQueue(String clientId);
}
