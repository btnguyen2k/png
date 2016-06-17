package modules.initdb;

import javax.inject.Inject;

import com.google.inject.Provider;

import bo.user.IUserDao;
import bo.user.UserBo;
import modules.registry.IRegistry;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import utils.DjsMasterConstants;
import utils.UserUtils;

public class InitDbModule extends Module {

    public final static class InitDbBootstrap {
        @Inject
        public InitDbBootstrap(Provider<IRegistry> registry) {
            IUserDao userDao = registry.get().getUserDao();
            UserBo admin = userDao.getUser("1");
            if (admin == null) {
                String username = "admin";
                String id = "1";
                String encPwd = UserUtils.encryptPassword(id, "password");
                String email = "admin@localhost";
                admin = UserBo.newInstance(username, encPwd, email).setId(id)
                        .setGroupId(DjsMasterConstants.GROUP_ADMIN);
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
