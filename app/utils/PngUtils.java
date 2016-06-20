package utils;

public class PngUtils {

    /**
     * Counts number of available applications.
     * 
     * @return
     */
    public static int countApplications() {
        return PngGlobals.registry.getAppDao().getAllAppIds().length;
    }
}
