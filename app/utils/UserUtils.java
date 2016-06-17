package utils;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.commons.utils.HashUtils;

import bo.user.UserBo;
import modules.registry.IRegistry;
import play.mvc.Http.Session;

/**
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class UserUtils {
    /**
     * Encrypts a password.
     * 
     * @param salt
     * @param rawPassword
     * @return
     */
    public static String encryptPassword(String salt, String rawPassword) {
        return HashUtils.sha256(salt + "." + rawPassword);
    }

    /**
     * Encrypts a user's password.
     * 
     * @param user
     * @param rawPassword
     * @return
     */
    public static String encryptPassword(UserBo user, String rawPassword) {
        return encryptPassword(user.getId(), rawPassword);
    }

    /**
     * Authenticates user against a password.
     * 
     * @param user
     * @param inputPassword
     * @return
     */
    public static boolean authenticate(UserBo user, String inputPassword) {
        if (user == null || StringUtils.isEmpty(inputPassword)) {
            return false;
        }
        String encryptedPassword = encryptPassword(user, inputPassword);
        return StringUtils.equalsIgnoreCase(encryptedPassword, user.getPassword());
    }

    public final static String SESSION_ADMIN_ID = "admin_id";

    /**
     * Gets the current logged in administrator.
     * 
     * @param registry
     * @param session
     * @return
     */
    public static UserBo currentAdmin(IRegistry registry, Session session) {
        String id = session.get(SESSION_ADMIN_ID);
        return registry.getUserDao().getUser(id);
    }

    public static void loginAdmin(UserBo admin, Session session) {
        session.put(SESSION_ADMIN_ID, admin.getId());
    }
}
