package bo.pushtoken.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import bo.pushtoken.PushTokenBo;

public class PushTokenBoMapper implements RowMapper<PushTokenBo> {

    public final static PushTokenBoMapper instance = new PushTokenBoMapper();

    public final static String COL_TOKEN = "push_token";
    public final static String COL_OS = "push_os";
    public final static String COL_TIMESTAMP_UPDATE = "timestamp_update";
    public final static String COL_TAGS = "tags";
    public final static String COL_TAGS_CHECKSUM = "tags_checksum";

    /**
     * {@inheritDoc}
     */
    @Override
    public PushTokenBo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return newObjFromResultSet(rs);
    }

    private static PushTokenBo newObjFromResultSet(ResultSet rs) throws SQLException {
        PushTokenBo bo = new PushTokenBo();
        bo.setToken(rs.getString(COL_TOKEN));
        bo.setOs(rs.getString(COL_OS));
        bo.setTimestampUpdate(rs.getTimestamp(COL_TIMESTAMP_UPDATE));
        bo.setTags(rs.getString(COL_TAGS));
        bo.setTagsChecksum(rs.getString(COL_TAGS_CHECKSUM));
        bo.markClean();
        return bo;
    }
}
