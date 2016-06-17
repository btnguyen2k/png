package bo.app.jdbc;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.dao.jdbc.BaseJdbcDao;

import bo.app.AppBo;
import bo.app.IAppDao;

/**
 * Jdbc-implement of {@link IAppDao}.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class JdbcAppDao extends BaseJdbcDao implements IAppDao {

    private String tableNameApp = "png_app";
    private String cacheNameApp = "PNG_APP";

    protected String getTableNameApp() {
        return tableNameApp;
    }

    public JdbcAppDao setTableNameApp(String tableNameApp) {
        this.tableNameApp = tableNameApp;
        return this;
    }

    protected String getCacheNameApp() {
        return cacheNameApp;
    }

    public JdbcAppDao setCacheNameApp(String cacheNameApp) {
        this.cacheNameApp = cacheNameApp;
        return this;
    }

    /*----------------------------------------------------------------------*/
    private static String cacheKeyAppId(String id) {
        return id;
    }

    private static String cacheKeyAllApps() {
        return "_APP_APPS_";
    }

    private static String cacheKey(AppBo app) {
        return cacheKeyAppId(app.getId());
    }

    private void invalidate(AppBo app, boolean update) {
        if (update) {
            putToCache(cacheNameApp, cacheKey(app), app);
        } else {
            removeFromCache(cacheNameApp, cacheKeyAppId(app.getId()));
            removeFromCache(cacheNameApp, cacheKeyAllApps());
        }
    }
    /*----------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    @Override
    public JdbcAppDao init() {
        super.init();

        SQL_CREATE_APP = MessageFormat.format(SQL_CREATE_APP, tableNameApp);
        SQL_DELETE_APP = MessageFormat.format(SQL_DELETE_APP, tableNameApp);
        SQL_GET_APP = MessageFormat.format(SQL_GET_APP, tableNameApp);
        SQL_GET_ALL_APP_IDS = MessageFormat.format(SQL_GET_ALL_APP_IDS, tableNameApp);
        SQL_UPDATE_APP = MessageFormat.format(SQL_UPDATE_APP, tableNameApp);

        return this;
    }

    /*----------------------------------------------------------------------*/

    private final static String[] COLS_APP_ALL = { AppBoMapper.COL_ID, AppBoMapper.COL_DISABLED,
            AppBoMapper.COL_API_KEY, AppBoMapper.COL_IOS_P12_CONTENT,
            AppBoMapper.COL_IOS_P12_PASSWORD };
    private final static String[] COLS_APP_CREATE = COLS_APP_ALL;
    private String SQL_CREATE_APP = "INSERT INTO {0} (" + StringUtils.join(COLS_APP_CREATE, ',')
            + ") VALUES (" + StringUtils.repeat("?", ",", COLS_APP_CREATE.length) + ")";
    private String SQL_DELETE_APP = "DELETE FROM {0} WHERE " + AppBoMapper.COL_ID + "=?";
    private String SQL_GET_APP = "SELECT " + StringUtils.join(COLS_APP_ALL, ',')
            + " FROM {0} WHERE " + AppBoMapper.COL_ID + "=?";
    private String SQL_GET_ALL_APP_IDS = "SELECT " + AppBoMapper.COL_ID + " FROM {0} ORDER BY "
            + AppBoMapper.COL_ID;
    private String SQL_UPDATE_APP = "UPDATE {0} SET "
            + StringUtils.join(
                    new String[] { AppBoMapper.COL_DISABLED + "=?", AppBoMapper.COL_DISABLED + "=?",
                            AppBoMapper.COL_API_KEY + "=?", AppBoMapper.COL_IOS_P12_CONTENT + "=?",
                            AppBoMapper.COL_IOS_P12_PASSWORD + "=?" },
                    ',')
            + " WHERE " + AppBoMapper.COL_ID + "=?";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean create(AppBo app) {
        final Object[] VALUES = new Object[] { app.getId(), app.isDisabled() ? 1 : 0,
                app.getApiKey(), app.getIOSP12Content(), app.getIOSP12Password() };
        try {
            int numRows = execute(SQL_CREATE_APP, VALUES);
            invalidate(app, true);
            return numRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(AppBo app) {
        final Object[] VALUES = new Object[] { app.getId() };
        try {
            int numRows = execute(SQL_DELETE_APP, VALUES);
            invalidate(app, false);
            return numRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update(AppBo app) {
        final Object[] PARAM_VALUES = new Object[] { app.isDisabled() ? 1 : 0, app.getApiKey(),
                app.getIOSP12Content(), app.getIOSP12Password(), app.getId() };
        try {
            int nunRows = execute(SQL_UPDATE_APP, PARAM_VALUES);
            invalidate(app, true);
            return nunRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppBo getApp(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        final String cacheKey = cacheKeyAppId(id);
        AppBo result = getFromCache(cacheNameApp, cacheKey, AppBo.class);
        if (result == null) {
            final Object[] WHERE_VALUES = new Object[] { id };
            try {
                List<AppBo> dbRows = executeSelect(AppBoMapper.instance, SQL_GET_APP, WHERE_VALUES);
                result = dbRows != null && dbRows.size() > 0 ? dbRows.get(0) : null;
                putToCache(cacheNameApp, cacheKey, result);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public String[] getAllAppIds() {
        final String cacheKey = cacheKeyAllApps();
        List<String> appIds = getFromCache(cacheNameApp, cacheKey, List.class);
        if (appIds == null) {
            appIds = new ArrayList<>();
            try {
                List<Map<String, Object>> dbRows = executeSelect(SQL_GET_ALL_APP_IDS,
                        ArrayUtils.EMPTY_OBJECT_ARRAY);
                for (Map<String, Object> dbRow : dbRows) {
                    String id = DPathUtils.getValue(dbRow, AppBoMapper.COL_ID, String.class);
                    if (!StringUtils.isBlank(id)) {
                        appIds.add(id);
                    }
                }
                putToCache(cacheNameApp, cacheKey, appIds);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return appIds.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

}
