package bo.pushtoken;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.dao.BaseBo;

public class TagLookupBo extends BaseBo {

    public final static TagLookupBo[] EMPTY_ARRAY = new TagLookupBo[0];

    public final static TagLookupBo newInstance() {
        TagLookupBo bo = new TagLookupBo();
        return bo;
    }

    public final static TagLookupBo newInstance(String appId, String tag, String token, String os) {
        TagLookupBo bo = newInstance();
        bo.setAppId(appId).setTag(tag).setToken(token).setOs(os);
        return bo;
    }

    public final static TagLookupBo newInstance(PushTokenBo pushToken, String tag) {
        return newInstance(pushToken.getAppId(), tag, pushToken.getToken(), pushToken.getOs());
    }

    public final static TagLookupBo newInstance(TagLookupBo another) {
        TagLookupBo bo = new TagLookupBo();
        bo.fromMap(another.toMap());
        return bo;
    }

    /*----------------------------------------------------------------------*/

    private final static String ATTR_APP_ID = "app_id";
    private final static String ATTR_TAG = "tag";
    private final static String ATTR_TOKEN = "token";
    private final static String ATTR_OS = "os";

    @JsonIgnore
    public String getAppId() {
        return getAttribute(ATTR_APP_ID, String.class);
    }

    public TagLookupBo setAppId(String appId) {
        setAttribute(ATTR_APP_ID, appId != null ? appId.trim().toLowerCase() : "");
        return this;
    }

    @JsonIgnore
    public String getTag() {
        return getAttribute(ATTR_TAG, String.class);
    }

    public TagLookupBo setTag(String tag) {
        setAttribute(ATTR_TAG, tag != null ? tag.trim().toLowerCase() : "");
        return this;
    }

    @JsonIgnore
    public String getToken() {
        return getAttribute(ATTR_TOKEN, String.class);
    }

    public TagLookupBo setToken(String token) {
        setAttribute(ATTR_TOKEN, token != null ? token.trim() : "");
        return this;
    }

    @JsonIgnore
    public String getOs() {
        return getAttribute(ATTR_OS, String.class);
    }

    public TagLookupBo setOs(String os) {
        setAttribute(ATTR_OS, os != null ? os.trim().toUpperCase() : "");
        return this;
    }
}
