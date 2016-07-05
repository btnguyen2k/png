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
import org.springframework.dao.DuplicateKeyException;

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

    private void invalidate(TagLookupBo tagLookup) {
        invalidateLookupTag(tagLookup.getTag());
    }

    private void invalidateLookupTag(String tag) {
        removeFromCache(cacheNameTagLookup, cacheKeyTagLookup(tag));
    }

    private static String cacheKeyTagLookup(String tag) {
        return tag;
    }
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

        SQL_CREATE_TAG_LOOKUP = MessageFormat.format(SQL_CREATE_TAG_LOOKUP, tableNameTagLookup);
        SQL_DELETE_TAG_LOOKUP = MessageFormat.format(SQL_DELETE_TAG_LOOKUP, tableNameTagLookup);
        SQL_GET_TAGLOOKUPS_BY_TAG = MessageFormat.format(SQL_GET_TAGLOOKUPS_BY_TAG,
                tableNameTagLookup);
        SQL_GET_TAGS_FOR_PUSH_TOKEN = MessageFormat.format(SQL_GET_TAGS_FOR_PUSH_TOKEN,
                tableNameTagLookup);

        return this;
    }

    /*----------------------------------------------------------------------*/

    private String SQL_CREATE_PUSHTOKEN = "INSERT INTO {0} ("
            + StringUtils.join(PushTokenBoMapper._COLS_CREATE, ',') + ") VALUES ("
            + StringUtils.repeat("?", ",", PushTokenBoMapper._COLS_CREATE.length) + ")";
    private String SQL_DELETE_PUSHTOKEN = "DELETE FROM {0} WHERE "
            + PushTokenBoMapper._COLS_KEY_WHERE_CLAUSE;
    private String SQL_GET_PUSHTOKEN = "SELECT "
            + StringUtils.join(PushTokenBoMapper._COLS_ALL, ',') + " FROM {0} WHERE "
            + PushTokenBoMapper._COLS_KEY_WHERE_CLAUSE;
    private String SQL_UPDATE_PUSHTOKEN = "UPDATE {0} SET " + PushTokenBoMapper._COLS_UPDATE_CLAUSE
            + " WHERE " + PushTokenBoMapper._COLS_KEY_WHERE_CLAUSE;

    private String SQL_CREATE_TAG_LOOKUP = "INSERT INTO {0} ("
            + StringUtils.join(TagLookupBoMapper._COLS_CREATE, ',') + ") VALUES ("
            + StringUtils.repeat("?", ",", TagLookupBoMapper._COLS_CREATE.length) + ")";
    private String SQL_DELETE_TAG_LOOKUP = "DELETE FROM {0} WHERE "
            + TagLookupBoMapper._COLS_KEY_WHERE_CLAUSE;
    private String SQL_GET_TAGS_FOR_PUSH_TOKEN = "SELECT "
            + StringUtils.join(TagLookupBoMapper._COLS_ALL, ',') + " FROM {0} WHERE "
            + TagLookupBoMapper.COL_APP_ID + "=? AND " + TagLookupBoMapper.COL_TOKEN + "=? AND "
            + TagLookupBoMapper.COL_OS + "=?";

    private String SQL_GET_TAGLOOKUPS_BY_TAG = "SELECT "
            + StringUtils.join(TagLookupBoMapper._COLS_ALL, ',') + " FROM {0} WHERE "
            + TagLookupBoMapper.COL_TAG + "=?";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean create(PushTokenBo _pushToken) {
        PushTokenBo pushToken = PushTokenBo.newInstance(_pushToken);
        Date TIMESTAMP = pushToken.getTimestampUpdate() != null ? pushToken.getTimestampUpdate()
                : new Date();
        pushToken.setTimestampUpdate(TIMESTAMP);
        try {
            int numRows = execute(SQL_CREATE_PUSHTOKEN,
                    PushTokenBoMapper.valuesForCreate(pushToken));
            invalidate(pushToken, true);
            if (numRows > 0) {
                createTagLookups(pushToken);
            }
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
        try {
            int numRows = execute(SQL_DELETE_PUSHTOKEN,
                    PushTokenBoMapper.valuesForDelete(pushToken));
            invalidate(pushToken, false);
            deleteTagLookups(pushToken);
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
        PushTokenBo existing = getPushToken(_pushToken.getAppId(), _pushToken.getToken(),
                _pushToken.getOs());
        if (existing == null) {
            return false;
        }
        String oldTagChecksum = existing.getTagsChecksum();

        PushTokenBo pushToken = PushTokenBo.newInstance(_pushToken);
        Date TIMESTAMP = pushToken.getTimestampUpdate() != null ? pushToken.getTimestampUpdate()
                : new Date();
        pushToken.setTimestampUpdate(TIMESTAMP);
        try {
            int nunRows = execute(SQL_UPDATE_PUSHTOKEN,
                    PushTokenBoMapper.valuesForUpdate(pushToken));
            invalidate(pushToken, true);
            if (!StringUtils.equals(oldTagChecksum, pushToken.getTagsChecksum())) {
                // tag list has changed, update it
                List<TagLookupBo> tags = getTagsForPushToken(existing);
                if (tags != null) {
                    for (TagLookupBo tag : tags) {
                        delete(tag);
                    }
                }
                createTagLookups(pushToken);
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
    public PushTokenBo getPushToken(String appId, String token, String os) {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(token) || StringUtils.isBlank(os)) {
            return null;
        }

        final String cacheKey = cacheKeyPushToken(token, os);
        PushTokenBo result = getFromCache(cacheNamePushToken, cacheKey, PushTokenBo.class);
        if (result == null) {
            try {
                List<PushTokenBo> dbRows = executeSelect(PushTokenBoMapper.instance,
                        SQL_GET_PUSHTOKEN, PushTokenBoMapper.valuesForSelect(appId, token, os));
                result = dbRows != null && dbRows.size() > 0 ? dbRows.get(0) : null;
                putToCache(cacheNamePushToken, cacheKey, result);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /*----------------------------------------------------------------------*/

    private boolean delete(TagLookupBo tagLookup) {
        try {
            int numRows = execute(SQL_DELETE_TAG_LOOKUP,
                    TagLookupBoMapper.valuesForDelete(tagLookup));
            invalidate(tagLookup);
            return numRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteTagLookups(PushTokenBo pushToken) {
        List<TagLookupBo> tags = getTagsForPushToken(pushToken);
        for (TagLookupBo tag : tags) {
            delete(tag);
        }
    }

    private boolean create(TagLookupBo tagLookup) {
        try {
            int numRows = execute(SQL_CREATE_TAG_LOOKUP,
                    TagLookupBoMapper.valuesForCreate(tagLookup));
            invalidate(tagLookup);
            return numRows > 0;
        } catch (DuplicateKeyException dke) {
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTagLookups(PushTokenBo pushToken) {
        String[] tagList = pushToken.getTagsAsList();
        for (String tag : tagList) {
            TagLookupBo tagLookup = TagLookupBo.newInstance(pushToken.getAppId(), tag,
                    pushToken.getToken(), pushToken.getOs());
            create(tagLookup);
        }
    }

    private List<TagLookupBo> getTagsForPushToken(PushTokenBo pushToken) {
        try {
            return executeSelect(TagLookupBoMapper.instance, SQL_GET_TAGS_FOR_PUSH_TOKEN,
                    new Object[] { pushToken.getAppId(), pushToken.getToken(), pushToken.getOs() });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
                PushTokenBo pushToken = getPushToken(bo.getAppId(), bo.getToken(), bo.getOs());
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
