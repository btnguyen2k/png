package bo.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.dao.BaseBo;

import utils.IdUtils;

public class UserBo extends BaseBo {

    public final static UserBo newInstance() {
        UserBo user = new UserBo();
        user.setId(IdUtils.ID_GEN.generateId128Hex());
        return user;
    }

    public final static UserBo newInstance(String username, String encryptedPassword,
            String email) {
        UserBo user = new UserBo();
        user.setUsername(username).setPassword(encryptedPassword).setEmail(email);
        return user;
    }

    /*----------------------------------------------------------------------*/

    private final static String ATTR_ID = "id";
    private final static String ATTR_GROUP_ID = "gid";
    private final static String ATTR_USERNAME = "u";
    private final static String ATTR_PASSWORD = "p";
    private final static String ATTR_EMAIL = "e";

    @JsonIgnore
    public String getId() {
        return getAttribute(ATTR_ID, String.class);
    }

    public UserBo setId(String id) {
        setAttribute(ATTR_ID, id != null ? id.trim().toLowerCase() : null);
        return this;
    }

    @JsonIgnore
    public int getGroupId() {
        Integer groupId = getAttribute(ATTR_GROUP_ID, Integer.class);
        return groupId != null ? groupId.intValue() : 0;
    }

    public UserBo setGroupId(int groupId) {
        setAttribute(ATTR_GROUP_ID, groupId);
        return this;
    }

    @JsonIgnore
    public String getUsername() {
        return getAttribute(ATTR_USERNAME, String.class);
    }

    public UserBo setUsername(String username) {
        setAttribute(ATTR_USERNAME, username != null ? username.trim().toLowerCase() : null);
        return this;
    }

    @JsonIgnore
    public String getPassword() {
        return getAttribute(ATTR_PASSWORD, String.class);
    }

    public UserBo setPassword(String password) {
        setAttribute(ATTR_PASSWORD, password != null ? password.trim() : null);
        return this;
    }

    @JsonIgnore
    public String getEmail() {
        return getAttribute(ATTR_EMAIL, String.class);
    }

    public UserBo setEmail(String email) {
        setAttribute(ATTR_EMAIL, email != null ? email.trim() : null);
        return this;
    }
}
