package bo.pushtoken.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import bo.pushtoken.PushTokenBo;

public class PushTokenBoMapper implements RowMapper<PushTokenBo> {

    public final static PushTokenBoMapper instance = new PushTokenBoMapper();

    public final static String COL_APP_ID = "app_id";
    public final static String COL_TOKEN = "push_token";
    public final static String COL_OS = "push_os";
    public final static String COL_TIMESTAMP_UPDATE = "timestamp_update";
    public final static String COL_TAGS = "tags";
    public final static String COL_TAGS_CHECKSUM = "tags_checksum";

    public final static String[] _COLS_ALL = { COL_APP_ID, COL_TOKEN, COL_OS, COL_TIMESTAMP_UPDATE,
            COL_TAGS, COL_TAGS_CHECKSUM };
    public final static String[] _COLS_CREATE = _COLS_ALL;
    public final static String[] _COLS_KEY = { COL_APP_ID, COL_TOKEN, COL_OS };
    public final static String _COLS_KEY_WHERE_CLAUSE = COL_APP_ID + "=? AND " + COL_TOKEN
            + "=? AND " + COL_OS + "=?";
    public final static String _COLS_UPDATE_CLAUSE = StringUtils.join(
            new String[] { COL_TIMESTAMP_UPDATE + "=?", COL_TAGS + "=?", COL_TAGS_CHECKSUM + "=?" },
            ',');

    public static Object[] valuesForCreate(PushTokenBo bo) {
        return new Object[] { bo.getAppId(), bo.getToken(), bo.getOs(), bo.getTimestampUpdate(),
                bo.getTags(), bo.getTagsChecksum() };
    }

    public static Object[] valuesForDelete(PushTokenBo bo) {
        return new Object[] { bo.getAppId(), bo.getToken(), bo.getOs() };
    }

    public static Object[] valuesForSelect(String appId, String token, String os) {
        return new Object[] { appId, token, os };
    }

    public static Object[] valuesForUpdate(PushTokenBo bo) {
        return new Object[] { bo.getTimestampUpdate(), bo.getTags(), bo.getTagsChecksum(),
                bo.getAppId(), bo.getToken(), bo.getOs() };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PushTokenBo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return newObjFromResultSet(rs);
    }

    private static PushTokenBo newObjFromResultSet(ResultSet rs) throws SQLException {
        PushTokenBo bo = new PushTokenBo();
        bo.setAppId(rs.getString(COL_APP_ID));
        bo.setToken(rs.getString(COL_TOKEN));
        bo.setOs(rs.getString(COL_OS));
        bo.setTimestampUpdate(rs.getTimestamp(COL_TIMESTAMP_UPDATE));
        bo.setTags(rs.getString(COL_TAGS));
        bo.setTagsChecksum(rs.getString(COL_TAGS_CHECKSUM));
        bo.markClean();
        return bo;
    }
}
