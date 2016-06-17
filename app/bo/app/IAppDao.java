package bo.app;

/**
 * API to access application storage.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public interface IAppDao {
    /**
     * Creates a new application.
     * 
     * @param app
     * @return
     */
    public boolean create(AppBo app);

    /**
     * Deletes an existing application.
     * 
     * @param app
     * @return
     */
    public boolean delete(AppBo app);

    /**
     * Updates an existing application.
     * 
     * @param app
     * @return
     */
    public boolean update(AppBo app);

    /**
     * Fetches an existing application.
     * 
     * @param id
     * @return
     */
    public AppBo getApp(String id);

    /**
     * Gets ids of all all available applications.
     * 
     * @return
     */
    public String[] getAllAppIds();
}
