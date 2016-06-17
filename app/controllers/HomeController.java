package controllers;

import java.util.Date;

import javax.inject.Inject;

import com.github.ddth.commons.utils.DateFormatUtils;
import com.google.inject.Provider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import compositions.AdminAuthRequired;
import modules.cluster.ICluster;
import modules.registry.IRegistry;
import play.mvc.Controller;
import play.mvc.Result;

@AdminAuthRequired
public class HomeController extends Controller {

    @Inject
    private Provider<ICluster> cluster;

    @Inject
    private Provider<IRegistry> registry;

    public Result index() {
        Date now = new Date();
        StringBuilder msg = new StringBuilder("Your application is ready: "
                + DateFormatUtils.toString(now, "yyyy-MM-dd HH:mm:ss"));
        msg.append("<br>\nCluster: ").append(cluster != null ? cluster.get() : "[null]")
                .append("<br>\n");
        msg.append("<br>\nRegistry: ").append(registry != null ? registry.get() : "[null]")
                .append("<br>\n");

        Config conf = ConfigFactory.load().getConfig("master");
        int port = conf.getInt("akka.remote.netty.tcp.port");
        msg.append("Port: " + port);

        // ActorSystem actorSystem = akka.getActorSystem();
        // // ActorRef myActor =
        // // actorSystem.actorOf(Props.create(MyUntypedActor.class),
        // "myactor");
        //
        // ClusterSingletonManagerSettings settings =
        // ClusterSingletonManagerSettings.create(actorSystem);
        // try {
        // actorSystem.actorOf(ClusterSingletonManager.props(MyUntypedActor.PROPS,
        // PoisonPill.getInstance(), settings),
        // "myactor");
        // } catch (InvalidActorNameException e) {
        // e.printStackTrace();
        // }
        // ClusterSingletonProxySettings proxySettings =
        // ClusterSingletonProxySettings.create(actorSystem);
        // ActorRef mySingletonActorRef =
        // actorSystem.actorOf(ClusterSingletonProxy.props("/user/myactor",
        // proxySettings),
        // "myactorProxy");
        // actorSystem.scheduler().schedule(Duration.Zero(), Duration.create(5,
        // TimeUnit.SECONDS), mySingletonActorRef,
        // "Tick", actorSystem.dispatcher(), null);

        response().setHeader(CONTENT_TYPE, "text/html; charset=utf-8");
        return ok(msg.toString());
    }
}
