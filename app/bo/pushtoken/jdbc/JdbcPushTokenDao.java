package bo.pushtoken.jdbc;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.dao.jdbc.BaseJdbcDao;

import bo.pushtoken.IPushTokenDao;
import bo.pushtoken.PushTokenBo;
import bo.pushtoken.TagLookupBo;

/**
 * Jdbc-implement of {@link IPushTokenDao}.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class JdbcPushTokenDao extends BaseJdbcDao implements IPushTokenDao {

    private String tableNamePushToken = "png_push_token";
    private String cacheNamePushToken = "PNG_PUSH_TOKEN";

    private String tableNameTagLookup = "png_tag_lookup";
    private String cacheNameTagLookup = "PNG_TAG_LOOKUP";

    protected String getTableNamePushToken() {
        return tableNamePushToken;
    }

    public JdbcPushTokenDao setTableNamePushToken(String tableNamePushToken) {
        this.tableNamePushToken = tableNamePushToken;
        return this;
    }

    protected String getCacheNamePushToken() {
        return cacheNamePushToken;
    }

    public JdbcPushTokenDao setCacheNamePushToken(String cacheNamePushToken) {
        this.cacheNamePushToken = cacheNamePushToken;
        return this;
    }

    protected String getTableNameTagLookup() {
        return tableNameTagLookup;
    }

    public JdbcPushTokenDao setTableNameTagLookup(String tableNameTagLookup) {
        this.tableNameTagLookup = tableNameTagLookup;
        return this;
    }

    protected String getCacheNameTagLookup() {
        return cacheNameTagLookup;
    }

    public JdbcPushTokenDao setCacheNameTagLookup(String cacheNameTagLookup) {
        this.cacheNameTagLookup = cacheNameTagLookup;
        return this;
    }

    /*----------------------------------------------------------------------*/
    private static String cacheKeyPushToken(String token, String os) {
        return token + "-" + os;
    }

    private static String cacheKey(PushTokenBo pushToken) {
        return cacheKeyPushToken(pushToken.getTags(), pushToken.getOs());
    }

    private void invalidate(PushTokenBo pushToken, boolean update) {
        if (update) {
            putToCache(cacheNamePushToken, cacheKey(pushToken), pushToken);
        } else {
            removeFromCache(cacheNamePushToken, cacheKey(pushToken));
        }
    }

    private void invalidateLookupTags(String[] tagList) {
        if (tagList != null) {
            for (String tag : tagList) {
                removeFromCache(cacheNameTagLookup, cacheKeyTagLookup(tag));
            }
        }
    }

    private static String cacheKeyTagLookup(String tag) {
        return tag;
    }

    // public static String cacheKey(TagLookupBo tagLookup) {
    // return cacheKeyTagLookup(tagLookup.getTag());
    // }
    //
    // public void invalidate(TagLookupBo tagLookup) {
    //
    // }
    /*----------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    @Override
    public JdbcPushTokenDao init() {
        super.init();

        SQL_CREATE_PUSHTOKEN = MessageFormat.format(SQL_CREATE_PUSHTOKEN, tableNamePushToken);
        SQL_DELETE_PUSHTOKEN = MessageFormat.format(SQL_DELETE_PUSHTOKEN, tableNamePushToken);
        SQL_GET_PUSHTOKEN = MessageFormat.format(SQL_GET_PUSHTOKEN, tableNamePushToken);
        SQL_UPDATE_PUSHTOKEN = MessageFormat.format(SQL_UPDATE_PUSHTOKEN, tableNamePushToken);

        SQL_GET_TAGLOOKUPS_BY_TAG = MessageFormat.format(SQL_GET_TAGLOOKUPS_BY_TAG,
                tableNameTagLookup);

        return this;
    }

    /*----------------------------------------------------------------------*/

    private final static String[] COLS_PUSHTOKEN_ALL = { PushTokenBoMapper.COL_TOKEN,
            PushTokenBoMapper.COL_OS, PushTokenBoMapper.COL_TIMESTAMP_UPDATE,
            PushTokenBoMapper.COL_TAGS, PushTokenBoMapper.COL_TAGS_CHECKSUM };
    private final static String[] COLS_PUSHTOKEN_CREATE = COLS_PUSHTOKEN_ALL;
    private String SQL_CREATE_PUSHTOKEN = "INSERT INTO {0} ("
            + StringUtils.join(COLS_PUSHTOKEN_CREATE, ',') + ") VALUES ("
            + StringUtils.repeat("?", ",", COLS_PUSHTOKEN_CREATE.length) + ")";
    private String SQL_DELETE_PUSHTOKEN = "DELETE FROM {0} WHERE " + PushTokenBoMapper.COL_TOKEN
            + "=? AND " + PushTokenBoMapper.COL_OS + "=?";
    private String SQL_GET_PUSHTOKEN = "SELECT " + StringUtils.join(COLS_PUSHTOKEN_ALL, ',')
            + " FROM {0} WHERE " + PushTokenBoMapper.COL_TOKEN + "=? AND "
            + PushTokenBoMapper.COL_OS + "=?";
    private String SQL_UPDATE_PUSHTOKEN = "UPDATE {0} SET "
            + StringUtils.join(new String[] { PushTokenBoMapper.COL_TIMESTAMP_UPDATE + "=?",
                    PushTokenBoMapper.COL_TAGS + "=?", PushTokenBoMapper.COL_TAGS_CHECKSUM + "=?" },
                    ',')
            + " WHERE " + PushTokenBoMapper.COL_TOKEN + "=? AND " + PushTokenBoMapper.COL_OS + "=?";

    private final static String[] COLS_TAGLOOKUP_ALL = { TagLookupBoMapper.COL_TAG,
            TagLookupBoMapper.COL_TOKEN, TagLookupBoMapper.COL_OS };
    private String SQL_GET_TAGLOOKUPS_BY_TAG = "SELECT " + StringUtils.join(COLS_TAGLOOKUP_ALL, ',')
            + " FROM {0} WHERE " + TagLookupBoMapper.COL_TAG + "=?";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean create(PushTokenBo _pushToken) {
        PushTokenBo pushToken = PushTokenBo.newInstance(_pushToken);
        Date TIMESTAMP = pushToken.getTimestampUpdate() != null ? pushToken.getTimestampUpdate()
                : new Date();
        final Object[] VALUES = new Object[] { pushToken.getToken(), pushToken.getOs(), TIMESTAMP,
                pushToken.getTags(), pushToken.getTagsChecksum() };
        try {
            int numRows = execute(SQL_CREATE_PUSHTOKEN, VALUES);
            pushToken.setTimestampUpdate(TIMESTAMP);
            invalidate(pushToken, true);
            invalidateLookupTags(pushToken.getTagsAsList());
            return numRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(PushTokenBo pushToken) {
        final Object[] VALUES = new Object[] { pushToken.getToken(), pushToken.getOs() };
        try {
            int numRows = execute(SQL_DELETE_PUSHTOKEN, VALUES);
            invalidate(pushToken, false);
            invalidateLookupTags(pushToken.getTagsAsList());
            return numRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update(PushTokenBo _pushToken) {
        PushTokenBo existing = getPushToken(_pushToken.getToken(), _pushToken.getOs());
        if (existing == null) {
            return false;
        }

        PushTokenBo pushToken = PushTokenBo.newInstance(_pushToken);
        Date TIMESTAMP = pushToken.getTimestampUpdate() != null ? pushToken.getTimestampUpdate()
                : new Date();
        final Object[] PARAMS = new Object[] { TIMESTAMP, pushToken.getTags(),
                pushToken.getTagsChecksum(), pushToken.getToken(), pushToken.getOs() };
        try {
            int nunRows = execute(SQL_UPDATE_PUSHTOKEN, PARAMS);
            pushToken.setTimestampUpdate(TIMESTAMP);
            invalidate(pushToken, true);
            if (!StringUtils.equals(existing.getTagsChecksum(), pushToken.getTagsChecksum())) {
                invalidateLookupTags(existing.getTagsAsList());
                invalidateLookupTags(pushToken.getTagsAsList());
            }
            return nunRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PushTokenBo getPushToken(String token, String os) {
        if (StringUtils.isBlank(token) || StringUtils.isBlank(os)) {
            return null;
        }
        final String cacheKey = cacheKeyPushToken(token, os);
        PushTokenBo result = getFromCache(cacheNamePushToken, cacheKey, PushTokenBo.class);
        if (result == null) {
            final Object[] WHERE_VALUES = new Object[] { token, os };
            try {
                List<PushTokenBo> dbRows = executeSelect(PushTokenBoMapper.instance,
                        SQL_GET_PUSHTOKEN, WHERE_VALUES);
                result = dbRows != null && dbRows.size() > 0 ? dbRows.get(0) : null;
                putToCache(cacheNamePushToken, cacheKey, result);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Collection<PushTokenBo> getPushTokensByTag(String tag) {
        if (StringUtils.isBlank(tag)) {
            return null;
        }

        final String cacheKey = cacheKeyTagLookup(tag);
        List<TagLookupBo> lookupResult = getFromCache(cacheNameTagLookup, cacheKey, List.class);
        if (lookupResult == null) {
            final Object[] WHERE_VALUES = new Object[] { tag };
            try {
                lookupResult = executeSelect(TagLookupBoMapper.instance, SQL_GET_TAGLOOKUPS_BY_TAG,
                        WHERE_VALUES);
                putToCache(cacheNameTagLookup, cacheKey, lookupResult);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        List<PushTokenBo> result = new ArrayList<>();
        if (lookupResult != null) {
            for (TagLookupBo bo : lookupResult) {
                PushTokenBo pushToken = getPushToken(bo.getToken(), bo.getOs());
                if (pushToken != null) {
                    result.add(pushToken);
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public PushTokenBo[] lookupPushTokens(String[] tags) {
        if (tags == null || tags.length == 0) {
            return PushTokenBo.EMPTY_ARRAY;
        }

        Set<PushTokenBo> result = new HashSet<>();
        for (String tag : tags) {
            result.addAll(getPushTokensByTag(tag));
        }
        return result.toArray(PushTokenBo.EMPTY_ARRAY);
    }

}
