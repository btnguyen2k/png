package akka.actors;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.bo.log.ITaskLogDao;
import com.github.ddth.djs.message.bus.JobInfoStartedMessage;
import com.github.ddth.djs.message.bus.JobInfoStoppedMessage;
import com.github.ddth.djs.message.bus.JobInfoUpdatedMessage;
import com.github.ddth.djs.message.bus.TickMessage;
import com.github.ddth.djs.message.queue.TaskFireoffMessage;
import com.github.ddth.djs.utils.CronFormat;
import com.github.ddth.queue.IQueue;

import akka.actor.Props;
import akka.utils.AkkaConstants;
import modules.registry.IRegistry;
import play.Logger;
import queue.IQueueService;
import utils.JobUtils;

/**
 * Actor on [tick] node(s) that fires {@link TaskFireoffMessage} whenever a job
 * is due to execute.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class TickJobActor extends BaseDjsActor {

    public static Props newProps(IRegistry registry, JobInfoBo jobInfo) {
        return Props.create(TickJobActor.class, registry, jobInfo);
    }

    public final static String NAME = TickJobActor.class.getSimpleName();

    /**
     * Calculates an "instance" actor name according to the associated
     * {@link JobInfoBo} instance.
     * 
     * @param jobInfo
     * @return
     */
    public static String calcInstanceName(JobInfoBo jobInfo) {
        return NAME + "-" + jobInfo.getId();
    }

    private JobInfoBo jobInfo;
    private CronFormat cronFormat;

    // subscribe to "TICK" topic, with group=NAME+jobInfo
    private String instanceName, subscribeTopic = AkkaConstants.TOPIC_TICK;

    public TickJobActor(IRegistry registry, JobInfoBo jobInfo) {
        super(registry);
        this.jobInfo = jobInfo;
        instanceName = calcInstanceName(jobInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getActorName() {
        return instanceName;
    }

    protected JobInfoBo getJobInfo() {
        return jobInfo;
    }

    protected CronFormat getCronFormat() {
        if (cronFormat == null) {
            synchronized (this) {
                cronFormat = jobInfo.getCronFormat();
            }
        }
        return cronFormat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() throws Exception {
        subscribeToTopic(subscribeTopic, instanceName);
        super.preStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() throws Exception {
        unsubscribeFromTopic(subscribeTopic, instanceName);
        super.postStop();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof JobInfoUpdatedMessage) {
            _eventJobInfoUpdated((JobInfoUpdatedMessage) message);
        } else if (message instanceof JobInfoStartedMessage) {
            _eventJobInfoUpdated((JobInfoStartedMessage) message);
        } else if (message instanceof JobInfoStoppedMessage) {
            _eventJobInfoUpdated((JobInfoStoppedMessage) message);
        } else if (message instanceof TickMessage) {
            _eventTick((TickMessage) message);
        } else {
            super.onReceive(message);
        }
    }

    protected void _eventJobInfoUpdated(JobInfoUpdatedMessage msg) {
        JobInfoBo jobInfo = msg.jobInfo;
        if (StringUtils.equalsIgnoreCase(jobInfo.getId(), this.jobInfo.getId())) {
            this.jobInfo.fromMap(jobInfo.toMap());
            synchronized (this) {
                this.cronFormat = null;
            }
        }
    }

    protected void _eventJobInfoUpdated(JobInfoStartedMessage msg) {
        jobInfo.setIsRunning(true);
    }

    protected void _eventJobInfoUpdated(JobInfoStoppedMessage msg) {
        jobInfo.setIsRunning(false);
    }

    /*----------------------------------------------------------------------*/

    private TickMessage lastTick;

    /**
     * Checks if "tick" matches job's cron format.
     * 
     * @param tick
     * @return
     */
    protected boolean isTickMatched(TickMessage tick) {
        if (tick.timestampMillis + 30000L > System.currentTimeMillis()) {
            if (lastTick == null || lastTick.timestampMillis < tick.timestampMillis) {
                return getCronFormat().matches(tick.timestampMillis);
            }
        }
        return false;
    }

    /**
     * Main "tick" processing method.
     * 
     * @param tick
     */
    protected void doTick(TickMessage tick) {
        TaskFireoffMessage taskFireoffMsg = new TaskFireoffMessage(getJobInfo());
        IQueueService queueService = getRegistry().getQueueService();
        IQueue queue = queueService.getQueue("djs-master");
        if (!JobUtils.queueEvent(queue, taskFireoffMsg)) {
            Logger.warn("Cannot put [" + taskFireoffMsg.getClass().getSimpleName() + "/"
                    + taskFireoffMsg.id + "] to queue for tick " + tick.id + "/"
                    + tick.timestampMillis + ", queue size: " + queue.queueSize());
        } else {
            ITaskLogDao taskLogDao = getRegistry().getTaskLogDao();
            JobUtils.logTask(taskLogDao, getRegistry().getNodeId(), taskFireoffMsg);
        }
    }

    private AtomicBoolean LOCK = new AtomicBoolean(false);

    protected void _eventTick(TickMessage msg) {
        if (jobInfo.isRunning() && isTickMatched(msg)) {
            if (LOCK.compareAndSet(false, true)) {
                try {
                    lastTick = msg;
                    doTick(msg);
                } finally {
                    LOCK.set(false);
                }
            } else {
                // Busy processing a previous message
                final String logMsg = "{" + instanceName
                        + "} Received TICK message, but I am busy! " + msg;
                Logger.warn(logMsg);
            }
        }
    }
}
