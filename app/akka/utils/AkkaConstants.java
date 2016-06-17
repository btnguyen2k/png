package akka.utils;

import com.github.ddth.djs.message.bus.TickMessage;
import com.github.ddth.djs.message.queue.TaskFireoffMessage;

import akka.actors.TickManagerActor;
import utils.DjsMasterConstants;

public class AkkaConstants {
    /**
     * Cluster node: master.
     * 
     * <ul>
     * <li>keep track of cluster nodes ("tick" and "master" nodes)</li>
     * <li>- leader "master" node fires {@link TickMessage} per tick (see
     * {@link DjsMasterConstants#DELAY_TICK})</li>
     * </ul>
     */
    public final static String ROLE_MASTER = "master";

    /**
     * Cluster node: tick.
     * 
     * <ul>
     * <li>host "tick" actors that fire {@link TaskFireoffMessage} whenever a
     * job is due to execute</li>
     * </ul>
     */
    public final static String ROLE_TICK = "tick";

    /**
     * A "tick" message is published to this topic for every "TICK".
     */
    public final static String TOPIC_TICK = "TICK";

    /**
     * {@link TickManagerActor} listens to this topic for job's events
     * (add/remove/update).
     */
    public final static String TOPIC_JOBEVENT = "JOB_EVENT";
}
