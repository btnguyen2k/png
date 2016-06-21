package bo.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.dao.BaseBo;

import utils.PngUtils;

public class AppBo extends BaseBo {

    public final static AppBo[] EMPTY_ARRAY = new AppBo[0];

    public final static AppBo newInstance() {
        AppBo user = new AppBo();
        return user;
    }

    public final static AppBo newInstance(String id) {
        AppBo user = newInstance();
        user.setId(id);
        return user;
    }

    /*----------------------------------------------------------------------*/

    private final static String ATTR_ID = "id";
    private final static String ATTR_IS_DISABLED = "disabled";
    private final static String ATTR_API_KEY = "api_key";
    private final static String ATTR_IOS_P12_CONTENT = "ios_p12";
    private final static String ATTR_IOS_P12_PASSWORD = "ios_p12_pwd";

    @JsonIgnore
    public String getId() {
        return getAttribute(ATTR_ID, String.class);
    }

    public AppBo setId(String id) {
        setAttribute(ATTR_ID, id != null ? id.trim().toLowerCase() : null);
        return this;
    }

    @JsonIgnore
    public boolean isDisabled() {
        Integer value = getAttribute(ATTR_IS_DISABLED, Integer.class);
        return value != null ? value.intValue() > 0 : false;
    }

    public AppBo setDisabled(int value) {
        setAttribute(ATTR_IS_DISABLED, value != 0 ? 1 : 0);
        return this;
    }

    public AppBo setDisabled(boolean value) {
        setAttribute(ATTR_IS_DISABLED, value ? 1 : 0);
        return this;
    }

    @JsonIgnore
    public String getApiKey() {
        return getAttribute(ATTR_API_KEY, String.class);
    }

    public AppBo setApiKey(String apiKey) {
        setAttribute(ATTR_API_KEY, apiKey != null ? apiKey.trim().toLowerCase() : "");
        return this;
    }

    @JsonIgnore
    private byte[] iOSP12ContentRaw = null;

    @JsonIgnore
    public byte[] getIOSP12ContentRaw() {
        if (iOSP12ContentRaw == null) {
            iOSP12ContentRaw = PngUtils.base64Decode(getIOSP12Content());
        }
        return iOSP12ContentRaw;
    }

    @JsonIgnore
    public String getIOSP12Content() {
        return getAttribute(ATTR_IOS_P12_CONTENT, String.class);
    }

    public AppBo setIOSP12Content(String iOSP12Content) {
        String content = iOSP12Content != null ? iOSP12Content.trim() : "";
        setAttribute(ATTR_IOS_P12_CONTENT, content);
        iOSP12ContentRaw = PngUtils.base64Decode(content);
        return this;
    }

    public AppBo setIOSP12Content(byte[] iOSP12ContentRaw) {
        String content = PngUtils.base64Encode(iOSP12ContentRaw);
        this.iOSP12ContentRaw = iOSP12ContentRaw;
        setAttribute(ATTR_IOS_P12_CONTENT, content);
        return this;
    }

    @JsonIgnore
    public String getIOSP12Password() {
        return getAttribute(ATTR_IOS_P12_PASSWORD, String.class);
    }

    public AppBo setIOSP12Password(String iOSP12Password) {
        setAttribute(ATTR_IOS_P12_PASSWORD, iOSP12Password != null ? iOSP12Password.trim() : "");
        return this;
    }
}
