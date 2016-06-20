package utils;

import modules.registry.IFormValidator;
import modules.registry.IRegistry;
import play.Configuration;

/**
 * Global instances for cases where DI is not visible.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class PngGlobals {
    public static IRegistry registry;

    public static IFormValidator formValidator;

    public static Configuration appConfig;
}
