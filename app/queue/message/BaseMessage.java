package queue.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.dao.BaseBo;

/**
 * Base class for other message classes.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class BaseMessage extends BaseBo {

    protected final static String ATTR_ID = "id";
    protected final static String ATTR_TIMESTAMP_MS = "t";
    protected final static String ATTR_APP_ID = "app_id";
    protected final static String ATTR_API_KEY = "api_key";

    @JsonIgnore
    public String getId() {
        return getAttribute(ATTR_ID, String.class);
    }

    public BaseMessage setId(String id) {
        setAttribute(ATTR_APP_ID, id);
        return this;
    }

    @JsonIgnore
    public long getTimestampMs() {
        Long value = getAttribute(ATTR_TIMESTAMP_MS, Long.class);
        return value != null ? value.longValue() : 0;
    }

    public BaseMessage setTimestampMs(long timestampMs) {
        setAttribute(ATTR_TIMESTAMP_MS, timestampMs);
        return this;
    }

    @JsonIgnore
    public String getAppId() {
        return getAttribute(ATTR_APP_ID, String.class);
    }

    public BaseMessage setAppId(String appId) {
        setAttribute(ATTR_APP_ID, appId);
        return this;
    }

    @JsonIgnore
    public String getApiKey() {
        return getAttribute(ATTR_API_KEY, String.class);
    }

    public BaseMessage setApiKey(String apiKey) {
        setAttribute(ATTR_API_KEY, apiKey);
        return this;
    }
}
