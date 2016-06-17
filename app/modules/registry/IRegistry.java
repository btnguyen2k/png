package modules.registry;

import com.github.ddth.djs.bo.job.IJobDao;
import com.github.ddth.djs.bo.log.ITaskLogDao;
import com.github.ddth.queue.IQueue;

import akka.actor.ActorSystem;
import akka.cluster.Member;
import bo.user.IUserDao;
import play.Application;
import queue.IQueueService;

public interface IRegistry {

    /*----------------------------------------------------------------------*/

    /**
     * Gets the current running Play application.
     * 
     * @return
     */
    public Application getPlayApplication();

    /**
     * Gets the local {@link ActorSystem} instance.
     * 
     * @return
     */
    public ActorSystem getLocalActorSystem();

    /*----------------------------------------------------------------------*/

    /**
     * Gets this node's id (name or IP).
     * 
     * @return
     */
    public String getNodeId();

    /**
     * Adds an Akka's member node to management list.
     * 
     * @param node
     */
    public void addNode(Member node);

    /**
     * Removes an Akka's member node from management list.
     * 
     * @param node
     */
    public void removeNode(Member node);

    /**
     * Checks if an Akka's member node is leader of a role.
     * 
     * @param role
     * @param node
     * @return
     */
    public boolean isLeader(String role, Member node);

    /**
     * Gets leader node for a role.
     * 
     * @param role
     * @return
     */
    public Member getLeader(String role);

    /*----------------------------------------------------------------------*/

    /**
     * Gets {@link IJobDao} instance.
     * 
     * @return
     */
    public IJobDao getJobDao();

    /**
     * Gets {@link ITaskLogDao} instance.
     * 
     * @return
     */
    public ITaskLogDao getTaskLogDao();

    /**
     * Gets {@link IUserDao} instance.
     * 
     * @return
     */
    public IUserDao getUserDao();

    /**
     * Gets {@link IQueueService} to push/retrieve task fireoff notifications.
     * 
     * @return
     */
    public IQueueService getQueueService();

    /**
     * Gets {@link IQueue} to buffer task results/feedbacks from workers.
     * 
     * @return
     */
    public IQueue getQueueTaskFeedback();

}
