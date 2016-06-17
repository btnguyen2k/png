package utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.ddth.djs.bo.job.IJobDao;
import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.bo.job.JobTemplateBo;
import com.github.ddth.djs.bo.log.ITaskLogDao;
import com.github.ddth.djs.bo.log.TaskLogBo;
import com.github.ddth.djs.message.BaseJobMessage;
import com.github.ddth.djs.message.BaseMessage;
import com.github.ddth.djs.message.queue.TaskFinishMessage;
import com.github.ddth.djs.message.queue.TaskFireoffMessage;
import com.github.ddth.djs.message.queue.TaskPickupMessage;
import com.github.ddth.djs.utils.DjsConstants;
import com.github.ddth.queue.IQueue;
import com.github.ddth.queue.IQueueMessage;
import com.github.ddth.queue.impl.BaseUniversalQueueMessage;
import com.github.ddth.queue.impl.universal.UniversalQueueMessage;

import play.Logger;

public class JobUtils {
    public static String taskStatusToString(int status) {
        switch (status) {
        case DjsConstants.TASK_STATUS_FINISHED_CANCEL:
            return "FINISHED-CANCELLED";
        case DjsConstants.TASK_STATUS_FINISHED_ERROR:
            return "FINISHED-ERROR";
        case DjsConstants.TASK_STATUS_FINISHED_OK:
            return "FINISHED";
        case DjsConstants.TASK_STATUS_NEW:
            return "NEW";
        case DjsConstants.TASK_STATUS_PICKED:
            return "PICKUP";
        case DjsConstants.TASK_STATUS_RETURNED:
            return "RETURNED";
        case DjsConstants.TASK_STATUS_SKIPPED:
            return "SKIPPED";
        default:
            return String.valueOf(status);
        }
    }

    /*----------------------------------------------------------------------*.
    
    /**
     * Counts number of available job templates.
     * 
     * @return
     */
    public static int countJobTemplates() {
        return DjsMasterGlobals.registry.getJobDao().getAllJobTemplateIds().length;
    }

    /**
     * Counts number of available jobs.
     * 
     * @return
     */
    public static int countJobs() {
        return DjsMasterGlobals.registry.getJobDao().getAllJobInfoIds().length;
    }

    /**
     * Gets all available job templates as a list.
     * 
     * @return
     */
    public static JobTemplateBo[] getAllJobTemplates() {
        IJobDao jobDao = DjsMasterGlobals.registry.getJobDao();
        String[] idList = jobDao.getAllJobTemplateIds();
        List<JobTemplateBo> result = new ArrayList<>();
        for (String id : idList) {
            JobTemplateBo bo = jobDao.getJobTemplate(id);
            if (bo != null) {
                result.add(bo);
            }
        }
        return result.toArray(JobTemplateBo.EMPTY_ARRAY);
    }

    /**
     * Gets all available jobs as a list.
     * 
     * @return
     */
    public static JobInfoBo[] getAllJobs() {
        IJobDao jobDao = DjsMasterGlobals.registry.getJobDao();
        String[] idList = jobDao.getAllJobInfoIds();
        List<JobInfoBo> result = new ArrayList<>();
        for (String id : idList) {
            JobInfoBo bo = jobDao.getJobInfo(id);
            if (bo != null) {
                result.add(bo);
            }
        }
        return result.toArray(JobInfoBo.EMPTY_ARRAY);
    }

    // public static byte[] serializeTask(BaseMessage msg) {
    // return msg != null ? msg.toBytes() : null;
    // }
    //
    // public static <T extends BaseMessage> T deserializeTask(byte[] data,
    // Class<T> clazz) {
    // try {
    // return BaseMessage.deserialize(data, clazz);
    // } catch (Exception e) {
    // Logger.warn("Cannot deserialize data to [" + clazz + "]: " +
    // e.getMessage(), e);
    // return null;
    // }
    // }

    /**
     * Logs a task-fireoff event.
     * 
     * @param taskLogDao
     * @param nodeId
     * @param taskMsg
     * @return
     */
    public static boolean logTask(ITaskLogDao taskLogDao, String nodeId,
            TaskFireoffMessage taskMsg) {
        TaskLogBo taskLog = TaskLogBo.newInstance(taskMsg);
        taskLog.setStatus(DjsConstants.TASK_STATUS_NEW).setNodeCreate(nodeId)
                .setTimestampCreate(new Date());
        return taskLogDao.create(taskLog);
    }

    /**
     * Logs a task-return event.
     * 
     * @param taskLogDao
     * @param taskMsg
     * @return
     */
    public static boolean logTaskReturn(ITaskLogDao taskLogDao, TaskFireoffMessage taskMsg) {
        TaskLogBo taskLog = taskLogDao.getTaskLog(taskMsg.id);
        if (taskLog != null) {
            taskLog.setStatus(DjsConstants.TASK_STATUS_RETURNED);
        }
        return taskLog != null ? taskLogDao.update(taskLog) : false;
    }

    /**
     * Logs a task-pickup event.
     * 
     * @param taskLogDao
     * @param taskMsg
     * @return
     */
    public static boolean logTask(ITaskLogDao taskLogDao, TaskPickupMessage taskMsg) {
        TaskLogBo taskLog = taskLogDao.getTaskLog(taskMsg.id);
        if (taskLog != null) {
            taskLog.setStatus(DjsConstants.TASK_STATUS_PICKED).setNodePickup(taskMsg.pickupNode)
                    .setTimestampPickup(new Date(taskMsg.pickupTimestamp));
        }
        return taskLog != null ? taskLogDao.update(taskLog) : false;
    }

    /**
     * Logs a task-finish event.
     * 
     * @param taskLogDao
     * @param taskMsg
     * @return
     */
    public static boolean logTask(ITaskLogDao taskLogDao, TaskFinishMessage taskMsg) {
        TaskLogBo taskLog = taskLogDao.getTaskLog(taskMsg.id);
        if (taskLog != null) {
            taskLog.setStatus((int) taskMsg.status)
                    .setTimestampFinish(new Date(taskMsg.finishTimestamp)).setError(taskMsg.error)
                    .setMessage(taskMsg.message).setOutput(taskMsg.output);
        }
        return taskLog != null ? taskLogDao.update(taskLog) : false;
    }

    /**
     * Puts a job/task event to queue.
     * 
     * @param queue
     * @param message
     * @return
     */
    public static boolean queueEvent(IQueue queue, BaseJobMessage message) {
        IQueueMessage queueMsg = toQueueMessage(message);
        boolean result = queueMsg != null ? queue.queue(queueMsg) : false;
        if (Logger.isDebugEnabled() && queueMsg != null) {
            Logger.debug("Put a task message to queue [" + queueMsg.qId() + "/"
                    + message.getClass().getSimpleName() + "/" + message.id + "], queue size: "
                    + queue.queueSize());
        }
        return result;
    }

    /**
     * Converts a {@link BaseMessage} to {@link IQueueMessage}
     * 
     * @param msg
     * @return
     */
    public static IQueueMessage toQueueMessage(BaseMessage msg) {
        IQueueMessage queueMsg = UniversalQueueMessage.newInstance().content(msg.toBytes());
        return queueMsg;
    }

    /**
     * Converts a {@link IQueueMessage} to {@link BaseMessage}.
     * 
     * @param queueMsg
     * @return
     */
    public static BaseMessage fromQueueMessage(IQueueMessage queueMsg) {
        if (queueMsg instanceof BaseUniversalQueueMessage) {
            return BaseMessage.deserialize(((BaseUniversalQueueMessage) queueMsg).content());
        }
        return null;
    }

    /**
     * Converts a {@link IQueueMessage} to {@link BaseMessage}.
     * 
     * @param queueMsg
     * @param clazz
     * @return
     */
    public static <T extends BaseMessage> T fromQueueMessage(IQueueMessage queueMsg,
            Class<T> clazz) {
        if (queueMsg instanceof BaseUniversalQueueMessage) {
            return BaseMessage.deserialize(((BaseUniversalQueueMessage) queueMsg).content(), clazz);
        }
        return null;
    }
}
