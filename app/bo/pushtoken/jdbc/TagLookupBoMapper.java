package bo.pushtoken.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import bo.pushtoken.TagLookupBo;

public class TagLookupBoMapper implements RowMapper<TagLookupBo> {

    public final static TagLookupBoMapper instance = new TagLookupBoMapper();

    public final static String COL_APP_ID = "app_id";
    public final static String COL_TAG = "tag_value";
    public final static String COL_TOKEN = "push_token";
    public final static String COL_OS = "push_os";

    public final static String[] _COLS_ALL = { COL_APP_ID, COL_TAG, COL_TOKEN, COL_OS };
    public final static String[] _COLS_CREATE = _COLS_ALL;
    public final static String[] _COLS_KEY = { COL_APP_ID, COL_TAG, COL_TOKEN, COL_OS };
    public final static String _COLS_KEY_WHERE_CLAUSE = COL_APP_ID + "=? AND " + COL_TAG + "=? AND "
            + COL_TOKEN + "=? AND " + COL_OS + "=?";

    public static Object[] valuesForCreate(TagLookupBo bo) {
        return new Object[] { bo.getAppId(), bo.getTag(), bo.getToken(), bo.getOs() };
    }

    public static Object[] valuesForDelete(TagLookupBo bo) {
        return new Object[] { bo.getAppId(), bo.getTag(), bo.getToken(), bo.getOs() };
    }

    public static Object[] valuesForDelete(String appId, String tag, String token, String os) {
        return new Object[] { appId, tag, token, os };
    }

    public static Object[] valuesForSelect(String appId, String tag, String token, String os) {
        return new Object[] { appId, tag, token, os };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagLookupBo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return newObjFromResultSet(rs);
    }

    private static TagLookupBo newObjFromResultSet(ResultSet rs) throws SQLException {
        TagLookupBo bo = new TagLookupBo();
        bo.setAppId(rs.getString(COL_APP_ID));
        bo.setTag(rs.getString(COL_TAG));
        bo.setToken(rs.getString(COL_TOKEN));
        bo.setOs(rs.getString(COL_OS));
        bo.markClean();
        return bo;
    }
}
