package bo.pushtoken;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.commons.utils.HashUtils;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.dao.BaseBo;

public class PushTokenBo extends BaseBo {

    public final static PushTokenBo[] EMPTY_ARRAY = new PushTokenBo[0];

    public final static PushTokenBo newInstance() {
        PushTokenBo bo = new PushTokenBo();
        bo.setTimestampUpdate(new Date());
        return bo;
    }

    public final static PushTokenBo newInstance(String appId, String token, String os) {
        PushTokenBo bo = newInstance();
        bo.setAppId(appId).setToken(token).setOs(os);
        return bo;
    }

    public final static PushTokenBo newInstance(PushTokenBo another) {
        PushTokenBo bo = new PushTokenBo();
        bo.fromMap(another.toMap());
        return bo;
    }

    /*----------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    @Override
    public PushTokenBo clone() {
        PushTokenBo clone = (PushTokenBo) super.clone();
        clone.tagsAsSet = null;
        return clone;
    }

    public boolean equals(Object obj) {
        if (obj instanceof PushTokenBo) {
            PushTokenBo another = (PushTokenBo) obj;
            EqualsBuilder eq = new EqualsBuilder();
            eq.append(getToken(), another.getToken()).append(getOs(), another.getOs());
            return eq.isEquals();
        }
        return false;
    }

    private final static String ATTR_APP_ID = "app_id";
    private final static String ATTR_TOKEN = "token";
    private final static String ATTR_OS = "os";
    private final static String ATTR_TIMESTAMP_UPDATE = "timestamp";
    private final static String ATTR_TAGS = "tags";
    private final static String ATTR_TAGS_CHECKSUM = "tags_checksum";

    @JsonIgnore
    public String getAppId() {
        return getAttribute(ATTR_APP_ID, String.class);
    }

    public PushTokenBo setAppId(String appId) {
        setAttribute(ATTR_APP_ID, appId != null ? appId.trim().toLowerCase() : null);
        return this;
    }

    @JsonIgnore
    public String getToken() {
        return getAttribute(ATTR_TOKEN, String.class);
    }

    public PushTokenBo setToken(String token) {
        setAttribute(ATTR_TOKEN, token != null ? token.trim() : null);
        return this;
    }

    @JsonIgnore
    public String getOs() {
        return getAttribute(ATTR_OS, String.class);
    }

    public PushTokenBo setOs(String os) {
        setAttribute(ATTR_OS, os != null ? os.trim().toUpperCase() : "");
        return this;
    }

    @JsonIgnore
    public Date getTimestampUpdate() {
        return getAttribute(ATTR_TIMESTAMP_UPDATE, Date.class);
    }

    public PushTokenBo setTimestampUpdate(Date timestamp) {
        setAttribute(ATTR_TIMESTAMP_UPDATE, timestamp != null ? timestamp : new Date());
        return this;
    }

    @JsonIgnore
    private Set<String> tagsAsSet;

    @SuppressWarnings("unchecked")
    synchronized private Set<String> getTagsSet() {
        if (tagsAsSet == null) {
            try {
                tagsAsSet = SerializationUtils.fromJsonString(getTags(), Set.class);
            } catch (Exception e) {
                tagsAsSet = new TreeSet<>();
            }
        }
        return tagsAsSet;
    }

    public String[] getTagsAsList() {
        return getTagsSet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public PushTokenBo addTag(String tag) {
        if (!StringUtils.isBlank(tag)) {
            Set<String> tags = getTagsSet();
            if (tags.add(tag.trim().toLowerCase())) {
                setTags(SerializationUtils.toJsonString(tags), false);
            }
        }
        return this;
    }

    @JsonIgnore
    public String getTags() {
        return getAttribute(ATTR_TAGS, String.class);
    }

    public PushTokenBo setTags(String tags) {
        return setTags(tags, true);
    }

    @SuppressWarnings("unchecked")
    synchronized public PushTokenBo setTags(String tags, boolean updateTagsSet) {
        setAttribute(ATTR_TAGS, tags != null ? tags.trim().toLowerCase() : "[]");
        setTagsChecksum(HashUtils.crc32(tags));
        if (updateTagsSet) {
            try {
                tagsAsSet = SerializationUtils.fromJsonString(tags, Set.class);
            } catch (Exception e) {
                tagsAsSet = new TreeSet<>();
            }
        }
        return this;
    }

    @JsonIgnore
    public String getTagsChecksum() {
        return getAttribute(ATTR_TAGS_CHECKSUM, String.class);
    }

    public PushTokenBo setTagsChecksum(String checksum) {
        setAttribute(ATTR_TAGS_CHECKSUM, checksum != null ? checksum.trim() : "");
        return this;
    }
}
