package queue.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.dao.BaseBo;

import utils.PngUtils;

public class SendPushNotificationsMessage extends BaseMessage {

    public static SendPushNotificationsMessage newInstance() {
        SendPushNotificationsMessage bo = new SendPushNotificationsMessage();
        bo.setId(PngUtils.IDGEN.generateId128Hex()).setTimestampMs(System.currentTimeMillis());
        return bo;
    }

    public static SendPushNotificationsMessage newInstance(String appId, String title,
            String content, Collection<Target> targets) {
        SendPushNotificationsMessage bo = newInstance();
        bo.setAppId(appId);
        bo.setTitle(title).setContent(content);
        bo.setTargets(targets.toArray(Target.EMPTY_ARRAY));
        return bo;
    }

    /*----------------------------------------------------------------------*/
    public static class Target extends BaseBo {

        public final static Target[] EMPTY_ARRAY = new Target[0];

        public static Target newInstance(Map<String, Object> data) {
            Target bo = new Target();
            bo.fromMap(data);
            return bo;
        }

        public static Target newInstance(String token, String os) {
            if (StringUtils.isBlank(token) || StringUtils.isBlank(os)) {
                return null;
            }
            Target bo = new Target();
            bo.setToken(token).setOs(os);
            return bo;
        }

        public static Target newInstance(Collection<String> tags) {
            if (tags == null || tags.size() == 0) {
                return null;
            }
            Target bo = new Target();
            bo.setTags(tags.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
            return bo;
        }

        private final static String ATTR_TAGS = "tags";
        private final static String ATTR_TOKEN = "token";
        private final static String ATTR_OS = "os";

        public String getToken() {
            return getAttribute(ATTR_TOKEN, String.class);
        }

        public Target setToken(String token) {
            setAttribute(ATTR_TOKEN, token);
            return this;
        }

        public String getOs() {
            return getAttribute(ATTR_OS, String.class);
        }

        public Target setOs(String os) {
            setAttribute(ATTR_OS, os);
            return this;
        }

        private String getTagsJson() {
            return getAttribute(ATTR_TAGS, String.class);
        }

        private Target setTagsJson(String tagsJson) {
            setAttribute(ATTR_TAGS, tagsJson);
            return this;
        }

        public String[] getTags() {
            Collection<String> result = new HashSet<>();

            String tagsJson = getTagsJson();
            Object tags = null;
            try {
                tags = SerializationUtils.fromJsonString(tagsJson);
            } catch (Exception e) {
                tags = null;
            }
            if (tags == null) {
                return ArrayUtils.EMPTY_STRING_ARRAY;
            }
            if (tags instanceof Collection) {
                for (Object tag : ((Collection<?>) tags)) {
                    if (tag != null) {
                        result.add(tag.toString());
                    }
                }
            } else if (tags instanceof Object[]) {
                for (Object tag : ((Object[]) tags)) {
                    if (tag != null) {
                        result.add(tag.toString());
                    }
                }
            }

            return result.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
        }

        public Target setTags(String[] tags) {
            if (tags != null) {
                setTagsJson(SerializationUtils.toJsonString(tags));
            }
            return this;
        }
    }

    protected final static String ATTR_TITLE = "title";
    protected final static String ATTR_CONTENT = "content";
    protected final static String ATTR_TARGETS = "targets";

    @JsonIgnore
    public String getTitle() {
        return getAttribute(ATTR_TITLE, String.class);
    }

    public SendPushNotificationsMessage setTitle(String title) {
        setAttribute(ATTR_TITLE, title);
        return this;
    }

    @JsonIgnore
    public String getContent() {
        return getAttribute(ATTR_CONTENT, String.class);
    }

    public SendPushNotificationsMessage setContent(String content) {
        setAttribute(ATTR_CONTENT, content);
        return this;
    }

    private String getTargetsJson() {
        return getAttribute(ATTR_TARGETS, String.class);
    }

    private SendPushNotificationsMessage setTargetsJson(String targetsJson) {
        setAttribute(ATTR_TARGETS, targetsJson);
        return this;
    }

    @SuppressWarnings("unchecked")
    public Target[] getTargets() {
        Collection<Target> result = new HashSet<>();

        List<Map<String, Object>> tagsData = null;
        try {
            tagsData = SerializationUtils.fromJsonString(getTargetsJson(), List.class);
        } catch (Exception e) {
            tagsData = null;
        }
        if (tagsData == null) {
            return Target.EMPTY_ARRAY;
        }
        for (Map<String, Object> tagData : tagsData) {
            Target target = Target.newInstance(tagData);
            if (target != null) {
                result.add(target);
            }
        }

        return result.toArray(Target.EMPTY_ARRAY);
    }

    public SendPushNotificationsMessage setTargets(Target[] targets) {
        if (targets != null) {
            List<Map<String, Object>> targetsData = new ArrayList<>();
            for (Target target : targets) {
                Map<String, Object> targetData = target.toMap();
                targetsData.add(targetData);
            }
            setTargetsJson(SerializationUtils.toJsonString(targetsData));
        }
        return this;
    }

}
