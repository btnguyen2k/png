package modules.registry;

import java.io.File;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.github.ddth.djs.bo.job.IJobDao;
import com.github.ddth.djs.bo.log.ITaskLogDao;
import com.github.ddth.queue.IQueue;

import akka.actor.ActorSystem;
import akka.cluster.Member;
import bo.user.IUserDao;
import play.Application;
import play.Logger;
import play.inject.ApplicationLifecycle;
import queue.IQueueService;
import utils.DjsMasterGlobals;
import utils.NetworkUtils;

@Singleton
public class RegistryImpl implements IRegistry {

    private ApplicationContext appContext;
    private Application playApp;
    private ActorSystem actorSystem;
    private String nodeId;

    /**
     * {@inheritDoc}
     */
    @Inject
    public RegistryImpl(ApplicationLifecycle lifecycle, Application playApp,
            ActorSystem actorSystem) {
        DjsMasterGlobals.registry = this;
        DjsMasterGlobals.appConfig = playApp.configuration();

        this.playApp = playApp;
        this.actorSystem = actorSystem;

        try {
            init();
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }

        lifecycle.addStopHook(() -> {
            destroy();
            return null;
        });
    }

    private void initApplicationContext() {
        String configFile = playApp.configuration().getString("spring.conf");
        File springConfigFile = configFile.startsWith("/") ? new File(configFile)
                : new File(playApp.path(), configFile);
        AbstractApplicationContext applicationContext = new FileSystemXmlApplicationContext(
                "file:" + springConfigFile.getAbsolutePath());
        applicationContext.start();
        appContext = applicationContext;
    }

    private void initActors() {
    }

    private void init() throws Exception {
        initApplicationContext();
        initActors();

        // init node-id
        nodeId = appContext.getBean("NODE_ID", String.class);
        if (StringUtils.isBlank(nodeId)) {
            nodeId = NetworkUtils.getLocalIpAddress();
        }
    }

    private void destroyApplicationContext() {
        if (appContext != null) {
            try {
                ((AbstractApplicationContext) appContext).destroy();
            } catch (Exception e) {
                Logger.warn(e.getMessage(), e);
            } finally {
                appContext = null;
            }
        }
    }

    private void destroyActors() {
    }

    private void destroy() {
        destroyActors();
        destroyApplicationContext();
    }

    /*----------------------------------------------------------------------*/
    private Map<String, SortedSet<Member>> nodeManager = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeId() {
        return nodeId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNode(Member node) {
        synchronized (nodeManager) {
            Set<String> memberRoles = node.getRoles();
            for (String role : memberRoles) {
                SortedSet<Member> members = nodeManager.get(role);
                if (members == null) {
                    members = new TreeSet<Member>(new Comparator<Member>() {
                        public int compare(Member a, Member b) {
                            if (a.isOlderThan(b)) {
                                return -1;
                            } else if (b.isOlderThan(a)) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    });
                    nodeManager.put(role, members);
                }
                members.add(node);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNode(Member node) {
        synchronized (nodeManager) {
            Set<String> memberRoles = node.getRoles();
            for (String role : memberRoles) {
                SortedSet<Member> members = nodeManager.get(role);
                if (members != null) {
                    members.remove(node);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLeader(String role, Member node) {
        SortedSet<Member> members = nodeManager.get(role);
        Member leader = null;
        try {
            leader = members != null ? members.first() : null;
        } catch (NoSuchElementException e) {
            leader = null;
        }
        return leader != null && node.address().equals(leader.address());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Member getLeader(String role) {
        SortedSet<Member> members = nodeManager.get(role);
        Member leader = null;
        try {
            leader = members != null ? members.first() : null;
        } catch (NoSuchElementException e) {
            leader = null;
        }
        return leader;
    }

    /*----------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    @Override
    public Application getPlayApplication() {
        return playApp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActorSystem getLocalActorSystem() {
        return actorSystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IJobDao getJobDao() {
        return appContext.getBean(IJobDao.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITaskLogDao getTaskLogDao() {
        return appContext.getBean(ITaskLogDao.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IUserDao getUserDao() {
        return appContext.getBean(IUserDao.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IQueue getQueueTaskFeedback() {
        return appContext.getBean("QUEUE_TASK_FEEDBACK", IQueue.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IQueueService getQueueService() {
        return appContext.getBean(IQueueService.class);
    }
}
