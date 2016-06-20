package bo.user.jdbc;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.dao.jdbc.BaseJdbcDao;

import bo.user.IUserDao;
import bo.user.UserBo;

/**
 * Jdbc-implement of {@link IUserDao}.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class JdbcUserDao extends BaseJdbcDao implements IUserDao {

    private String tableNameUser = "png_user";
    private String cacheNameUser = "PNG_USER";

    protected String getTableNameUser() {
        return tableNameUser;
    }

    public JdbcUserDao setTableNameUser(String tableNameUser) {
        this.tableNameUser = tableNameUser;
        return this;
    }

    protected String getCacheNameUser() {
        return cacheNameUser;
    }

    public JdbcUserDao setCacheNameUser(String cacheNameUser) {
        this.cacheNameUser = cacheNameUser;
        return this;
    }

    /*----------------------------------------------------------------------*/
    private static String cacheKeyUserId(String id) {
        return "ID_" + id;
    }

    private static String cacheKeyUserName(String username) {
        return "UN_" + username;
    }

    private static String cacheKey(UserBo user) {
        return cacheKeyUserId(user.getId());
    }

    private void invalidate(UserBo user, boolean update) {
        if (update) {
            putToCache(cacheNameUser, cacheKey(user), user);
        } else {
            removeFromCache(cacheNameUser, cacheKeyUserId(user.getId()));
            removeFromCache(cacheNameUser, cacheKeyUserName(user.getUsername()));
        }
    }
    /*----------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    @Override
    public JdbcUserDao init() {
        super.init();

        SQL_CREATE_USER = MessageFormat.format(SQL_CREATE_USER, tableNameUser);
        SQL_DELETE_USER = MessageFormat.format(SQL_DELETE_USER, tableNameUser);
        SQL_GET_USER = MessageFormat.format(SQL_GET_USER, tableNameUser);
        SQL_GET_USERID_BY_USERNAME = MessageFormat.format(SQL_GET_USERID_BY_USERNAME,
                tableNameUser);
        SQL_UPDATE_USER = MessageFormat.format(SQL_UPDATE_USER, tableNameUser);

        return this;
    }

    /*----------------------------------------------------------------------*/

    private final static String[] COLS_USER_ALL = { UserBoMapper.COL_ID, UserBoMapper.COL_GROUP_ID,
            UserBoMapper.COL_USERNAME, UserBoMapper.COL_PASSWORD, UserBoMapper.COL_EMAIL };
    private final static String[] COLS_USER_CREATE = COLS_USER_ALL;
    private String SQL_CREATE_USER = "INSERT INTO {0} (" + StringUtils.join(COLS_USER_CREATE, ',')
            + ") VALUES (" + StringUtils.repeat("?", ",", COLS_USER_CREATE.length) + ")";
    private String SQL_DELETE_USER = "DELETE FROM {0} WHERE " + UserBoMapper.COL_ID + "=?";
    private String SQL_GET_USER = "SELECT " + StringUtils.join(COLS_USER_ALL, ',')
            + " FROM {0} WHERE " + UserBoMapper.COL_ID + "=?";
    private String SQL_GET_USERID_BY_USERNAME = "SELECT " + UserBoMapper.COL_ID + " FROM {0} WHERE "
            + UserBoMapper.COL_USERNAME + "=?";
    private String SQL_UPDATE_USER = "UPDATE {0} SET "
            + StringUtils.join(new String[] { UserBoMapper.COL_GROUP_ID + "=?",
                    UserBoMapper.COL_PASSWORD + "=?", UserBoMapper.COL_EMAIL + "=?" }, ',')
            + " WHERE " + UserBoMapper.COL_ID + "=?";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean create(UserBo user) {
        final Object[] VALUES = new Object[] { user.getId(), user.getGroupId(), user.getUsername(),
                user.getPassword(), user.getEmail() };
        try {
            int numRows = execute(SQL_CREATE_USER, VALUES);
            invalidate(user, true);
            return numRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(UserBo user) {
        final Object[] VALUES = new Object[] { user.getId() };
        try {
            int numRows = execute(SQL_DELETE_USER, VALUES);
            invalidate(user, false);
            return numRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update(UserBo user) {
        final Object[] PARAM_VALUES = new Object[] { user.getGroupId(), user.getPassword(),
                user.getEmail(), user.getId() };
        try {
            int nunRows = execute(SQL_UPDATE_USER, PARAM_VALUES);
            invalidate(user, true);
            return nunRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserBo getUser(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        final String cacheKey = cacheKeyUserId(id);
        UserBo result = getFromCache(cacheNameUser, cacheKey, UserBo.class);
        if (result == null) {
            final Object[] WHERE_VALUES = new Object[] { id };
            try {
                List<UserBo> dbRows = executeSelect(UserBoMapper.instance, SQL_GET_USER,
                        WHERE_VALUES);
                result = dbRows != null && dbRows.size() > 0 ? dbRows.get(0) : null;
                putToCache(cacheNameUser, cacheKey, result);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserBo getUserByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        final String cacheKey = cacheKeyUserName(username);
        String id = getFromCache(cacheNameUser, cacheKey, String.class);
        if (id == null) {
            final Object[] WHERE_VALUES = new Object[] { username };
            try {
                List<Map<String, Object>> dbRows = executeSelect(SQL_GET_USERID_BY_USERNAME,
                        WHERE_VALUES);
                Map<String, Object> dbRow = dbRows != null && dbRows.size() > 0 ? dbRows.get(0)
                        : null;
                id = DPathUtils.getValue(dbRow, UserBoMapper.COL_ID, String.class);
                putToCache(cacheNameUser, cacheKey, id);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return getUser(id);
    }

}
