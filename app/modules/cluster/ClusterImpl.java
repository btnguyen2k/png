package modules.cluster;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.utils.AkkaConstants;
import modules.cluster.workers.AppEventThread;
import modules.cluster.workers.PushNotificationThread;
import modules.registry.IRegistry;
import play.Logger;
import play.inject.ApplicationLifecycle;

@Singleton
public class ClusterImpl implements ICluster {

    private String clusterName;
    private ActorSystem actorSystem;

    private Provider<IRegistry> registry;

    private AppEventThread appEventThread;
    private PushNotificationThread pushNotificationThread;

    private void initFrontendThreads() {
        appEventThread = new AppEventThread(registry.get());
        appEventThread.start();
    }

    private void initBackendThreads() {
        pushNotificationThread = new PushNotificationThread(registry.get());
        pushNotificationThread.start();
    }

    private void init() {
        Config conf = ConfigFactory.load().getConfig("akka");
        if (conf == null) {
            throw new IllegalStateException("No [akka] configurations found!");
        }
        clusterName = conf.getString("cluster.name");

        Cluster cluster = Cluster.get(actorSystem);
        Set<String> selfRoles = cluster.getSelfRoles();
        if (selfRoles != null && selfRoles.contains(AkkaConstants.ROLE_FRONTEND)) {
            initFrontendThreads();
        }
        if (selfRoles != null && selfRoles.contains(AkkaConstants.ROLE_BACKEND)) {
            initBackendThreads();
        }
    }

    private void destroy() {
        try {
            if (appEventThread != null) {
                appEventThread.stopThread();
                appEventThread = null;
            }
        } catch (Exception e) {
            Logger.warn(e.getMessage(), e);
        }

        try {
            if (pushNotificationThread != null) {
                pushNotificationThread.stopThread();
                pushNotificationThread = null;
            }
        } catch (Exception e) {
            Logger.warn(e.getMessage(), e);
        }

        try {
            // leave the cluster
            Cluster cluster = actorSystem != null ? Cluster.get(actorSystem) : null;
            if (cluster != null) {
                cluster.leave(cluster.selfAddress());
                cluster.down(cluster.selfAddress());
            }
        } catch (Exception e) {
            Logger.warn(e.getMessage(), e);
        }

        try {
            if (actorSystem != null) {
                actorSystem.terminate();
                actorSystem = null;
            }
        } catch (Exception e) {
            Logger.warn(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @param lifecycle
     */
    @Inject
    public ClusterImpl(ApplicationLifecycle lifecycle, Provider<IRegistry> registry,
            ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        this.registry = registry;

        // for Java pre-8
        lifecycle.addStopHook(new Callable<CompletionStage<?>>() {
            @Override
            public CompletionStage<?> call() throws Exception {
                destroy();
                return CompletableFuture.completedFuture(null);
            }
        });

        // //for Java 8+
        // lifecycle.addStopHook(() -> {
        // destroy();
        // return CompletableFuture.completedFuture(null);
        // });

        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClusterName() {
        return clusterName;
    }
}
