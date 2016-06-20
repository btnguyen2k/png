package modules.cluster;

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
import modules.registry.IRegistry;
import play.Logger;
import play.inject.ApplicationLifecycle;

@Singleton
public class ClusterImpl implements ICluster {

    private String clusterName;
    private ActorSystem actorSystem;

    @SuppressWarnings("unused")
    private Provider<IRegistry> registry;

    private void init() {
        Config conf = ConfigFactory.load().getConfig("akka");
        if (conf == null) {
            throw new IllegalStateException("No configuration [akka] found!");
        }
        // int port = conf.getInt("remote.netty.tcp.port");
        // if (port != 0 && !NetworkUtils.isTcpPortAvailable(port)) {
        // throw new IllegalStateException("Invalid port [" + port + "], or port
        // not available!");
        // }
        clusterName = conf.getString("cluster.name");
    }

    private void destroy() {
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
