package bo.user;

/**
 * API to access user storage.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public interface IUserDao {
    /**
     * Creates a new user account.
     * 
     * @param user
     * @return
     */
    public boolean create(UserBo user);

    /**
     * Deletes an existing user account.
     * 
     * @param user
     * @return
     */
    public boolean delete(UserBo user);

    /**
     * Updates an existing user account.
     * 
     * @param user
     * @return
     */
    public boolean update(UserBo user);

    /**
     * Fetches an existing user account.
     * 
     * @param id
     * @return
     */
    public UserBo getUser(String id);

    /**
     * Fetches an existing user account.
     * 
     * @param username
     * @return
     */
    public UserBo getUserByUsername(String username);
}
