//package akka.actors;
//
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import org.apache.commons.lang3.StringUtils;
//
//import com.github.ddth.djs.bo.log.ITaskLogDao;
//import com.github.ddth.djs.bo.log.TaskLogBo;
//import com.github.ddth.djs.message.BaseMessage;
//import com.github.ddth.djs.message.bus.TickMessage;
//import com.github.ddth.djs.message.queue.TaskFinishMessage;
//import com.github.ddth.djs.message.queue.TaskFireoffMessage;
//import com.github.ddth.djs.message.queue.TaskPickupMessage;
//import com.github.ddth.djs.utils.DjsConstants;
//import com.github.ddth.queue.IQueue;
//import com.github.ddth.queue.IQueueMessage;
//
//import akka.actor.ActorRef;
//import akka.actor.Address;
//import akka.actor.Cancellable;
//import akka.cluster.ClusterEvent;
//import akka.cluster.ClusterEvent.MemberEvent;
//import akka.cluster.ClusterEvent.MemberRemoved;
//import akka.cluster.ClusterEvent.MemberUp;
//import akka.cluster.ClusterEvent.UnreachableMember;
//import akka.cluster.Member;
//import akka.utils.AkkaConstants;
//import modules.registry.IRegistry;
//import play.Logger;
//import queue.IQueueService;
//import utils.PngConstants;
//import utils.JobUtils;
//
///**
// * Actor on [master] node(s) that:
// * 
// * <ul>
// * <li>Keeps track of nodes within the cluster.</li>
// * <li>Publishes "tick" message every tick (only if the current node is leader).
// * </li>
// * </ul>
// * 
// * @author Thanh Nguyen <btnguyen2k@gmail.com>
// * @since 0.1.0
// */
//public class MasterFacadeActor extends BaseActor {
//
//    public final static String NAME = MasterFacadeActor.class.getSimpleName();
//
//    private Cancellable tick = getContext().system().scheduler().schedule(
//            PngConstants.DELAY_INITIAL, PngConstants.DELAY_TICK, new Runnable() {
//                @Override
//                public void run() {
//                    getSelf().tell(new TickMessage(), ActorRef.noSender());
//                }
//            }, getContext().dispatcher());
//
//    private void _eventTaskReturned(TaskFireoffMessage taskMsg) {
//        IRegistry registry = getRegistry();
//
//        ITaskLogDao taskLogDao = registry.getTaskLogDao();
//        TaskLogBo taskLog = taskLogDao.getTaskLog(taskMsg.id);
//        if (taskLog == null) {
//            Logger.warn("Task [" + taskMsg.id + "] is returned but no log record found!");
//            return;
//        }
//        int taskStatus = taskLog.getStatus();
//        if (taskStatus == DjsConstants.TASK_STATUS_FINISHED_CANCEL
//                || taskStatus == DjsConstants.TASK_STATUS_FINISHED_ERROR
//                || taskStatus == DjsConstants.TASK_STATUS_FINISHED_OK
//                || taskStatus == DjsConstants.TASK_STATUS_SKIPPED) {
//            Logger.warn("Task [" + taskMsg.id + "] is returned but its status ("
//                    + JobUtils.taskStatusToString(taskStatus) + ") is invalid for return.");
//            return;
//        }
//
//        IQueueService queueService = getRegistry().getQueueService();
//        IQueue queue = queueService.getQueue(PngConstants.GROUP_ID_MASTER);
//        JobUtils.queueEvent(queue, taskMsg);
//        JobUtils.logTaskReturn(getRegistry().getTaskLogDao(), taskMsg);
//    }
//
//    private void _eventTaskPicked(TaskPickupMessage taskMsg) {
//        IRegistry registry = getRegistry();
//
//        ITaskLogDao taskLogDao = registry.getTaskLogDao();
//        TaskLogBo taskLog = taskLogDao.getTaskLog(taskMsg.id);
//        if (taskLog == null) {
//            Logger.warn("Task [" + taskMsg.id + "] is picked-up but no log record found!");
//            return;
//        }
//        int taskStatus = taskLog.getStatus();
//        if (taskStatus == DjsConstants.TASK_STATUS_FINISHED_CANCEL
//                || taskStatus == DjsConstants.TASK_STATUS_FINISHED_ERROR
//                || taskStatus == DjsConstants.TASK_STATUS_FINISHED_OK
//                || taskStatus == DjsConstants.TASK_STATUS_SKIPPED) {
//            Logger.warn("Task [" + taskMsg.id + "] is picked-up but its status ("
//                    + JobUtils.taskStatusToString(taskStatus) + ") is invalid for pickup.");
//            return;
//        }
//
//        JobUtils.logTask(getRegistry().getTaskLogDao(), taskMsg);
//    }
//
//    private void _eventTaskFinished(TaskFinishMessage taskMsg) {
//        IRegistry registry = getRegistry();
//
//        ITaskLogDao taskLogDao = registry.getTaskLogDao();
//        TaskLogBo taskLog = taskLogDao.getTaskLog(taskMsg.id);
//        if (taskLog == null) {
//            Logger.warn("Task [" + taskMsg.id + "] is finished but no log record found!");
//            return;
//        }
//        int taskStatus = taskLog.getStatus();
//        if (taskStatus == DjsConstants.TASK_STATUS_FINISHED_CANCEL
//                || taskStatus == DjsConstants.TASK_STATUS_FINISHED_ERROR
//                || taskStatus == DjsConstants.TASK_STATUS_FINISHED_OK
//                || taskStatus == DjsConstants.TASK_STATUS_SKIPPED) {
//            Logger.warn("Task [" + taskMsg.id + "] is finished but its status ("
//                    + JobUtils.taskStatusToString(taskStatus) + ") is invalid for finish.");
//            return;
//        }
//
//        JobUtils.logTask(getRegistry().getTaskLogDao(), taskMsg);
//    }
//
//    private Thread taskFeedbackThread = new Thread("task-feedback") {
//        public void run() {
//            IQueue queue = getRegistry().getQueueTaskFeedback();
//            try {
//                while (!isInterrupted()) {
//                    if (!isMasterLeader()) {
//                        // I'm not leader, let leader do the work
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                        }
//                        continue;
//                    }
//
//                    IQueueMessage _queueMsg = queue.take();
//                    if (_queueMsg != null) {
//                        queue.finish(_queueMsg);
//                    }
//                    BaseMessage _baseMsg = JobUtils.fromQueueMessage(_queueMsg);
//                    if (_queueMsg != null && Logger.isDebugEnabled()) {
//                        Logger.debug("Took a message from feedback queue [" + _queueMsg.qId()
//                                + "/" + (_baseMsg != null
//                                        ? (_baseMsg.getClass() + ":" + _baseMsg.id) : "[null]")
//                                + "], queue size: " + queue.queueSize());
//                    }
//
//                    if (_baseMsg instanceof TaskFireoffMessage) {
//                        _eventTaskReturned((TaskFireoffMessage) _baseMsg);
//                    } else if (_baseMsg instanceof TaskPickupMessage) {
//                        _eventTaskPicked((TaskPickupMessage) _baseMsg);
//                    } else if (_baseMsg instanceof TaskFinishMessage) {
//                        _eventTaskFinished((TaskFinishMessage) _baseMsg);
//                    } else {
//                        if (_queueMsg != null) {
//                            Logger.warn("Invalid message: " + _baseMsg);
//                        }
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                Logger.warn(e.getMessage(), e);
//            }
//        }
//    };
//
//    public MasterFacadeActor(IRegistry registry) {
//        super(registry);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void preStart() throws Exception {
//        // subscribe to cluster changes
//        getCluster().subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class,
//                UnreachableMember.class);
//
//        super.preStart();
//
//        taskFeedbackThread.start();
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void postStop() throws Exception {
//        try {
//            taskFeedbackThread.interrupt();
//        } catch (Exception e) {
//            Logger.warn(e.getMessage(), e);
//        }
//
//        try {
//            tick.cancel();
//        } catch (Exception e) {
//            Logger.warn(e.getMessage(), e);
//        }
//
//        try {
//            getCluster().unsubscribe(getSelf());
//        } catch (Exception e) {
//            Logger.warn(e.getMessage(), e);
//        }
//
//        super.postStop();
//    }
//
//    private boolean isMasterLeader() {
//        IRegistry registry = getRegistry();
//        Member leader = registry != null ? registry.getLeader(AkkaConstants.ROLE_MASTER) : null;
//        Address leaderAddr = leader != null ? leader.address() : null;
//        Address thisNodeAddr = context().system().provider().getDefaultAddress();
//        return leaderAddr != null && thisNodeAddr != null && thisNodeAddr.equals(leaderAddr);
//    }
//
//    protected void _eventMemberUp(MemberUp msg) {
//        Member m = msg.member();
//        getRegistry().addNode(m);
//        Logger.info(
//                "Node [" + m.address().toString() + "] with roles [" + m.getRoles() + "] is UP.");
//    }
//
//    protected void _eventMemberRemoved(MemberRemoved msg) {
//        Member m = msg.member();
//        getRegistry().removeNode(m);
//        Logger.info("Node [" + m.address().toString() + "] with roles [" + m.getRoles()
//                + "] is REMOVED.");
//    }
//
//    private AtomicBoolean LOCK = new AtomicBoolean(false);
//
//    protected void _eventTick(TickMessage tick) {
//        Member leader = getRegistry().getLeader(AkkaConstants.ROLE_MASTER);
//        if (leader == null) {
//            final String msg = "Received TICK message, but cluster [" + AkkaConstants.ROLE_MASTER
//                    + "] is empty! " + tick;
//            Logger.warn(msg);
//        } else {
//            if (LOCK.compareAndSet(false, true)) {
//                try {
//                    String leaderAddr = leader.address().toString();
//                    String thisNodeAddr = context().system().provider().getDefaultAddress()
//                            .toString();
//                    if (StringUtils.equalsIgnoreCase(leaderAddr, thisNodeAddr)) {
//                        publishToTopic(new TickMessage(), AkkaConstants.TOPIC_TICK, true);
//                    } else {
//                        // I am not leader!
//                    }
//                } finally {
//                    // unlock
//                    LOCK.set(false);
//                }
//            } else {
//                // Busy processing a previous message
//                final String msg = "Received TICK message for cluster [" + AkkaConstants.ROLE_MASTER
//                        + "], but I am busy! " + tick;
//                Logger.warn(msg);
//            }
//        }
//    }
//
//    @Override
//    public void onReceive(Object message) throws Exception {
//        if (message instanceof MemberUp) {
//            _eventMemberUp((MemberUp) message);
//        } else if (message instanceof MemberRemoved) {
//            _eventMemberRemoved((MemberRemoved) message);
//        } else if (message instanceof TickMessage) {
//            _eventTick((TickMessage) message);
//        } else {
//            super.onReceive(message);
//        }
//    }
//}
