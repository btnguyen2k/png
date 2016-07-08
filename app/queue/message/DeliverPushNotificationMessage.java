package queue.message;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.commons.utils.SerializationUtils;

import bo.pushtoken.PushTokenBo;
import utils.PngUtils;

public class DeliverPushNotificationMessage extends BaseMessage {

    public static DeliverPushNotificationMessage newInstance() {
        DeliverPushNotificationMessage bo = new DeliverPushNotificationMessage();
        bo.setId(PngUtils.IDGEN.generateId128Hex()).setTimestampMs(System.currentTimeMillis());
        return bo;
    }

    public static DeliverPushNotificationMessage newInstance(String appId, String title,
            String content, PushTokenBo[] pushTokens) {
        DeliverPushNotificationMessage bo = newInstance();
        bo.setAppId(appId);
        bo.setTitle(title).setContent(content).setPushTokens(pushTokens);
        return bo;
    }

    /*----------------------------------------------------------------------*/
    protected final static String ATTR_TITLE = "title";
    protected final static String ATTR_CONTENT = "content";
    protected final static String ATTR_PUSHTOKENS = "push_tokens";

    @JsonIgnore
    public String getTitle() {
        return getAttribute(ATTR_TITLE, String.class);
    }

    public DeliverPushNotificationMessage setTitle(String title) {
        setAttribute(ATTR_TITLE, title);
        return this;
    }

    @JsonIgnore
    public String getContent() {
        return getAttribute(ATTR_CONTENT, String.class);
    }

    public DeliverPushNotificationMessage setContent(String content) {
        setAttribute(ATTR_CONTENT, content);
        return this;
    }

    private String getPushTokensJson() {
        return getAttribute(ATTR_PUSHTOKENS, String.class);
    }

    private DeliverPushNotificationMessage setPushTokensJson(String pushTokensJson) {
        setAttribute(ATTR_PUSHTOKENS, pushTokensJson);
        return this;
    }

    @SuppressWarnings("unchecked")
    public PushTokenBo[] getPushTokens() {
        List<String> jsonList = null;
        try {
            jsonList = SerializationUtils.fromJsonString(getPushTokensJson(), List.class);
        } catch (Exception e) {
            jsonList = null;
        }

        List<PushTokenBo> result = new ArrayList<>();
        if (jsonList != null) {
            for (String json : jsonList) {
                PushTokenBo pushToken = new PushTokenBo();
                pushToken.fromJson(json);
                result.add(pushToken);
            }
        }
        return result.toArray(PushTokenBo.EMPTY_ARRAY);
    }

    public DeliverPushNotificationMessage setPushTokens(PushTokenBo[] pushTokens) {
        List<String> jsonList = new ArrayList<>();
        if (pushTokens != null) {
            for (PushTokenBo pushToken : pushTokens) {
                jsonList.add(pushToken.toJson());
            }
        }
        setPushTokensJson(SerializationUtils.toJsonString(jsonList));
        return this;
    }

}
