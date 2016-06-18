package bo.pushtoken.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import bo.pushtoken.TagLookupBo;

public class TagLookupBoMapper implements RowMapper<TagLookupBo> {

    public final static TagLookupBoMapper instance = new TagLookupBoMapper();

    public final static String COL_TAG = "tag_value";
    public final static String COL_TOKEN = "push_token";
    public final static String COL_OS = "push_os";

    /**
     * {@inheritDoc}
     */
    @Override
    public TagLookupBo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return newObjFromResultSet(rs);
    }

    private static TagLookupBo newObjFromResultSet(ResultSet rs) throws SQLException {
        TagLookupBo bo = new TagLookupBo();
        bo.setTag(rs.getString(COL_TAG));
        bo.setToken(rs.getString(COL_TOKEN));
        bo.setOs(rs.getString(COL_OS));
        bo.markClean();
        return bo;
    }
}
