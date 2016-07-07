package queue.message;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.commons.utils.SerializationUtils;

import utils.PngUtils;

public class UpdatePushNotificationMessage extends BaseMessage {

    public static UpdatePushNotificationMessage newInstance() {
        UpdatePushNotificationMessage bo = new UpdatePushNotificationMessage();
        bo.setId(PngUtils.IDGEN.generateId128Hex()).setTimestampMs(System.currentTimeMillis());
        return bo;
    }

    public static UpdatePushNotificationMessage newInstance(String appId, String token, String os) {
        UpdatePushNotificationMessage bo = newInstance();
        bo.setToken(token).setOs(os).setAppId(appId);
        return bo;
    }

    public static UpdatePushNotificationMessage newInstance(String appId, String token, String os,
            Collection<String> tags) {
        UpdatePushNotificationMessage bo = newInstance(appId, token, os);
        bo.addTags(tags.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        return bo;
    }

    protected final static String ATTR_TOKEN = "token";
    protected final static String ATTR_OS = "os";
    protected final static String ATTR_TAGS = "tags";

    @JsonIgnore
    public String getToken() {
        return getAttribute(ATTR_TOKEN, String.class);
    }

    public UpdatePushNotificationMessage setToken(String token) {
        setAttribute(ATTR_TOKEN, token);
        return this;
    }

    @JsonIgnore
    public String getOs() {
        return getAttribute(ATTR_OS, String.class);
    }

    public UpdatePushNotificationMessage setOs(String os) {
        setAttribute(ATTR_OS, os);
        return this;
    }

    @JsonIgnore
    public String getTags() {
        return getAttribute(ATTR_TAGS, String.class);
    }

    public UpdatePushNotificationMessage setTags(String tags) {
        setAttribute(ATTR_TAGS, tags);
        tagsSet = null;
        return this;
    }

    @SuppressWarnings("unchecked")
    public UpdatePushNotificationMessage addTags(String... tags) {
        if (tags.length > 0) {
            Set<String> tagsSet = null;
            try {
                tagsSet = SerializationUtils.fromJsonString(getTags(), Set.class);
            } catch (Exception e) {
            }
            if (tagsSet == null) {
                tagsSet = new HashSet<>();
            }
            for (String tag : tags) {
                tagsSet.add(tag);
            }
            setTags(SerializationUtils.toJsonString(tagsSet));
        }
        return this;
    }

    private Set<String> tagsSet = null;

    @SuppressWarnings("unchecked")
    public String[] getTagsAsList() {
        if (tagsSet == null) {
            try {
                tagsSet = SerializationUtils.fromJsonString(getTags(), Set.class);
            } catch (Exception e) {
                tagsSet = new HashSet<>();
            }
        }
        return tagsSet.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }
}
