package utils;

import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

/**
 * Commonly used constants.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class DjsMasterConstants {
    public final static FiniteDuration DELAY_INITIAL = Duration.create(2, TimeUnit.SECONDS);
    public final static FiniteDuration DELAY_TICK = Duration.create(1, TimeUnit.SECONDS);

    public final static String FLASH_MSG_PREFIX_ERROR = "_E_:";

    public final static int GROUP_ADMIN = 1;

    public final static String DF_FULL = "yyyy-MM-dd HH:mm:ss";
    public final static String DF_HHMMSS = "HH:mm:ss";

    public final static String GROUP_ID_MASTER = "djs_master";
}
