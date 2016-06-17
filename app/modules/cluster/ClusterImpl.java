package modules.cluster;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.ddth.djs.bo.job.IJobDao;
import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.message.bus.JobInfoAddedMessage;
import com.google.inject.Provider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actors.MasterFacadeActor;
import akka.actors.TickManagerActor;
import akka.cluster.Cluster;
import akka.utils.AkkaConstants;
import modules.registry.IRegistry;
import play.inject.ApplicationLifecycle;
import utils.NetworkUtils;

@Singleton
public class ClusterImpl implements ICluster {

    private ActorSystem actorSystem;
    private String clusterName;
    private Provider<IRegistry> registry;

    private void initMasterActors() {
        // master facade actor
        Props props = Props.create(MasterFacadeActor.class, registry.get());
        actorSystem.actorOf(props, MasterFacadeActor.NAME);
    }

    private void initTickActors() {
        // worker job manager actor
        Props props = Props.create(TickManagerActor.class, registry.get());
        ActorRef jobManagerActor = actorSystem.actorOf(props, TickManagerActor.NAME);

        // start jobs
        IJobDao jobDao = registry.get().getJobDao();
        String[] allJobIds = jobDao.getAllJobInfoIds();
        for (String id : allJobIds) {
            JobInfoBo job = jobDao.getJobInfo(id);
            if (job != null) {
                Object message = new JobInfoAddedMessage(job);
                jobManagerActor.tell(message, ActorRef.noSender());
            }
        }

        // // create some dummy workers for testing
        // {
        // JobInfoBo jobInfo = JobInfoBo.newInstance();
        // jobInfo.setId("every-5secs").setCron("*/5 * * * * *");
        // Props props = Props.create(WorkerDummyActor.class, registry.get(),
        // jobInfo);
        // actorSystem.actorOf(props, "every-5secs");
        // }
        // {
        // JobInfoBo jobInfo = JobInfoBo.newInstance();
        // jobInfo.setId("every-3secs").setCron("*/3 * * * * *");
        // Props props = Props.create(WorkerDummyActor.class, registry.get(),
        // jobInfo);
        // actorSystem.actorOf(props, "every-3secs");
        // }
        // {
        // JobInfoBo jobInfo = JobInfoBo.newInstance();
        // jobInfo.setId("everyminute").setCron("0 * * * * *");
        // Props props = Props.create(WorkerDummyActor.class, registry.get(),
        // jobInfo);
        // actorSystem.actorOf(props, "everyminute");
        // }
    }

    private void init() {
        Config conf = ConfigFactory.load().getConfig("master");
        if (conf == null) {
            throw new IllegalStateException("No config [master] found!");
        }
        int port = conf.getInt("akka.remote.netty.tcp.port");
        if (port != 0 && !NetworkUtils.isTcpPortAvailable(port)) {
            throw new IllegalStateException("Invalid port [" + port + "], or port not available!");
        }

        clusterName = conf.getString("akka.cluster.name");
        actorSystem = ActorSystem.create(clusterName, conf);

        Cluster cluster = Cluster.get(actorSystem);
        Set<String> selfRoles = cluster.getSelfRoles();
        if (selfRoles != null && selfRoles.contains(AkkaConstants.ROLE_MASTER)) {
            initMasterActors();
        }
        if (selfRoles != null && selfRoles.contains(AkkaConstants.ROLE_TICK)) {
            initTickActors();
        }
    }

    private void destroy() {
        if (actorSystem != null) {
            try {
                // leave the cluster
                Cluster cluster = Cluster.get(actorSystem);
                // cluster.leave(cluster.selfAddress());
                cluster.down(cluster.selfAddress());
            } catch (Exception e) {
            }

            try {
                Thread.sleep(1234);
            } catch (Exception e) {
            }

            try {
                // and terminate the Actor System
                actorSystem.terminate();
            } catch (Exception e) {
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @param lifecycle
     */
    @Inject
    public ClusterImpl(ApplicationLifecycle lifecycle, Provider<IRegistry> registry) {
        this.registry = registry;

        // previous contents of Plugin.onStart
        init();

        lifecycle.addStopHook(() -> {
            // previous contents of Plugin.onStop
            destroy();
            return null;
            // return F.Promise.pure(null);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActorSystem getClusterActorSystem() {
        return actorSystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClusterName() {
        return clusterName;
    }
}
