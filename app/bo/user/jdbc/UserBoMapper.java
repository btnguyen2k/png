package bo.user.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import bo.user.UserBo;

public class UserBoMapper implements RowMapper<UserBo> {

    public final static UserBoMapper instance = new UserBoMapper();

    public final static String COL_ID = "uid";
    public final static String COL_GROUP_ID = "ugroup_id";
    public final static String COL_USERNAME = "uname";
    public final static String COL_PASSWORD = "upassword";
    public final static String COL_EMAIL = "uemail";

    /**
     * {@inheritDoc}
     */
    @Override
    public UserBo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return newObjFromResultSet(rs);
    }

    private static UserBo newObjFromResultSet(ResultSet rs) throws SQLException {
        UserBo bo = new UserBo();
        bo.setId(rs.getString(COL_ID));
        bo.setGroupId(rs.getInt(COL_GROUP_ID));
        bo.setUsername(rs.getString(COL_USERNAME));
        bo.setPassword(rs.getString(COL_PASSWORD));
        bo.setEmail(rs.getString(COL_EMAIL));
        bo.markClean();
        return bo;
    }
}
