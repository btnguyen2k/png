package bo.pushtoken;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.dao.BaseBo;

public class TagLookupBo extends BaseBo {

    public final static TagLookupBo newInstance() {
        TagLookupBo bo = new TagLookupBo();
        return bo;
    }

    public final static TagLookupBo newInstance(String tag, String token, String os) {
        TagLookupBo bo = newInstance();
        bo.setTag(tag).setToken(token).setOs(os);
        return bo;
    }

    /*----------------------------------------------------------------------*/

    private final static String ATTR_TAG = "tag";
    private final static String ATTR_TOKEN = "token";
    private final static String ATTR_OS = "os";

    @JsonIgnore
    public String getTag() {
        return getAttribute(ATTR_TAG, String.class);
    }

    public TagLookupBo setTag(String tag) {
        setAttribute(ATTR_TOKEN, tag != null ? tag.trim().toLowerCase() : "");
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
