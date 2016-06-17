package modules.registry;

import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

public class RegistryModule extends Module {

    /**
     * {@inheritDoc}
     */
    @Override
    public Seq<Binding<?>> bindings(Environment env, Configuration conf) {
        Seq<Binding<?>> bindings = seq(bind(IRegistry.class).to(RegistryImpl.class).eagerly(),
                bind(IFormValidator.class).to(FormValidatorImpl.class).eagerly());
        return bindings;
    }

}
