package queue.message;

import com.fasterxml.jackson.annotation.JsonIgnore;

import utils.PngUtils;

public class DeletePushNotificationMessage extends BaseMessage {

    public static DeletePushNotificationMessage newInstance() {
        DeletePushNotificationMessage bo = new DeletePushNotificationMessage();
        bo.setId(PngUtils.IDGEN.generateId128Hex()).setTimestampMs(System.currentTimeMillis());
        return bo;
    }

    public static DeletePushNotificationMessage newInstance(String appId, String token, String os) {
        DeletePushNotificationMessage bo = newInstance();
        bo.setToken(token).setOs(os).setAppId(appId);
        return bo;
    }

    protected final static String ATTR_TOKEN = "token";
    protected final static String ATTR_OS = "os";

    @JsonIgnore
    public String getToken() {
        return getAttribute(ATTR_TOKEN, String.class);
    }

    public DeletePushNotificationMessage setToken(String token) {
        setAttribute(ATTR_TOKEN, token);
        return this;
    }

    @JsonIgnore
    public String getOs() {
        return getAttribute(ATTR_OS, String.class);
    }

    public DeletePushNotificationMessage setOs(String os) {
        setAttribute(ATTR_OS, os);
        return this;
    }

}
