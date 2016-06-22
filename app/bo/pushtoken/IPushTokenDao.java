package bo.pushtoken;

/**
 * API to access push token storage.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public interface IPushTokenDao {
    /**
     * Creates a push token.
     * 
     * @param pushToken
     * @return
     */
    public boolean create(PushTokenBo pushToken);

    /**
     * Deletes an existing push token.
     * 
     * @param pushToken
     * @return
     */
    public boolean delete(PushTokenBo pushToken);

    /**
     * Updates an existing push token.
     * 
     * @param pushToken
     * @return
     */
    public boolean update(PushTokenBo pushToken);

    /**
     * Fetches an existing push token.
     * 
     * @param appId
     * @param token
     * @param os
     * @return
     */
    public PushTokenBo getPushToken(String appId, String token, String os);

    /**
     * Looks up push tokens by tags.
     * 
     * @param tags
     * @return
     */
    public PushTokenBo[] lookupPushTokens(String[] tags);
}
