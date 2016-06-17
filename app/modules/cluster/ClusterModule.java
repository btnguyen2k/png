package modules.cluster;

import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

public class ClusterModule extends Module {

    /**
     * {@inheritDoc}
     */
    @Override
    public Seq<Binding<?>> bindings(Environment env, Configuration conf) {
        Seq<Binding<?>> bindings = seq(bind(ICluster.class).to(ClusterImpl.class).eagerly());
        return bindings;
    }

}
