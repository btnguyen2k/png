package modules.initdb;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Provider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import bo.user.IUserDao;
import bo.user.UserBo;
import modules.registry.IRegistry;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import utils.PngConstants;
import utils.UserUtils;

public class InitDbModule extends Module {

    public final static class InitDbBootstrap {
        @Inject
        public InitDbBootstrap(Provider<IRegistry> registry) {
            IUserDao userDao = registry.get().getUserDao();
            UserBo admin = userDao.getUser("1");
            if (admin == null) {
                Config conf = ConfigFactory.load().getConfig("init");
                String id = "1";
                String username = conf != null ? conf.getString("user.name") : null;
                if (StringUtils.isBlank(username)) {
                    username = "admin";
                }
                String password = conf != null ? conf.getString("user.password") : null;
                if (StringUtils.isBlank(password)) {
                    password = "password";
                }
                String email = conf != null ? conf.getString("user.email") : null;
                if (StringUtils.isBlank(email)) {
                    password = "admin@localhost";
                }
                admin = UserBo.newInstance(username, UserUtils.encryptPassword(id, password), email)
                        .setId(id).setGroupId(PngConstants.GROUP_ADMIN);
                userDao.create(admin);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Seq<Binding<?>> bindings(Environment env, Configuration conf) {
        Seq<Binding<?>> bindings = seq(bind(InitDbBootstrap.class).toSelf().eagerly());
        return bindings;
    }

}
