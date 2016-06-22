package bo.app.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import bo.app.AppBo;

public class AppBoMapper implements RowMapper<AppBo> {

    public final static AppBoMapper instance = new AppBoMapper();

    public final static String COL_ID = "aid";
    public final static String COL_DISABLED = "adisabled";
    public final static String COL_API_KEY = "api_key";
    public final static String COL_IOS_P12_CONTENT = "ios_p12_content";
    public final static String COL_IOS_P12_PASSWORD = "ios_p12_password";

    public final static String[] _COLS_ALL = { COL_ID, COL_DISABLED, COL_API_KEY,
            COL_IOS_P12_CONTENT, COL_IOS_P12_PASSWORD };
    public final static String[] _COLS_CREATE = _COLS_ALL;
    public final static String[] _COLS_KEY = { COL_ID };
    public final static String _COLS_KEY_WHERE_CLAUSE = COL_ID + "=?";
    public final static String _COLS_UPDATE_CLAUSE = StringUtils
            .join(new String[] { COL_DISABLED + "=?", COL_API_KEY + "=?",
                    COL_IOS_P12_CONTENT + "=?", COL_IOS_P12_PASSWORD + "=?" }, ',');

    public static Object[] valuesForCreate(AppBo bo) {
        return new Object[] { bo.getId(), bo.isDisabled() ? 1 : 0, bo.getApiKey(),
                bo.getIOSP12Content(), bo.getIOSP12Password() };
    }

    public static Object[] valuesForDelete(AppBo bo) {
        return new Object[] { bo.getId() };
    }

    public static Object[] valuesForSelect(String id) {
        return new Object[] { id };
    }

    public static Object[] valuesForUpdate(AppBo bo) {
        return new Object[] { bo.isDisabled() ? 1 : 0, bo.getApiKey(), bo.getIOSP12Content(),
                bo.getIOSP12Password(), bo.getId() };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppBo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return newObjFromResultSet(rs);
    }

    private static AppBo newObjFromResultSet(ResultSet rs) throws SQLException {
        AppBo bo = new AppBo();
        bo.setId(rs.getString(COL_ID));
        bo.setDisabled(rs.getInt(COL_DISABLED));
        bo.setApiKey(rs.getString(COL_API_KEY));
        bo.setIOSP12Content(rs.getString(COL_IOS_P12_CONTENT));
        bo.setIOSP12Password(rs.getString(COL_IOS_P12_PASSWORD));
        bo.markClean();
        return bo;
    }
}
