package utils;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

/**
 * Commonly used constants.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class PngConstants {

    public final static Charset UTF8 = Charset.forName("UTF-8");

    public final static FiniteDuration DELAY_INITIAL = Duration.create(2, TimeUnit.SECONDS);
    public final static FiniteDuration DELAY_TICK = Duration.create(1, TimeUnit.SECONDS);

    public final static String FLASH_MSG_PREFIX_ERROR = "_E_:";

    public final static int GROUP_ADMIN = 1;

    public final static String DF_FULL = "yyyy-MM-dd HH:mm:ss";
    public final static String DF_HHMMSS = "HH:mm:ss";

    public final static String GROUP_ID_MASTER = "djs_master";

    public final static long MAX_IOS_P12_FILE_SIZE = 32000;

    public final static String OS_IOS = "IOS";

    /* Android OS */
    public final static String OS_AOS = "AOS";
    public final static String OS_ANDROID = "ANDROID";

    public final static int RESPONSE_OK = 200;
    public final static int RESPONSE_NOT_FOUND = 404;
    public final static int RESPONSE_ACCESS_DENIED = 403;
    public final static int RESPONSE_CLIENT_ERROR = 400;
    public final static int RESPONSE_SERVER_ERROR = 500;
}
