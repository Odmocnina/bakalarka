package core.utils;

import app.AppContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/********************************
 * Custom logger class for the application, used so that logging during simulation can be enabled/disabled based on user
 * settings and simulation inst slows down due to excessive logging or deciding log4j levels are supposed to be logged
 * or not
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************/
public class MyLogger {

    /** Logger instance from log4j2 **/
    private static final Logger logger = LogManager.getLogger(MyLogger.class);

    /**
     * Logs a message at the specified level if logging is enabled in the application context.
     *
     * @param message The message to log.
     * @param level The log4j level at which to log the message (INFO, DEBUG, ERROR, WARN, FATAL).
     **/
    public static void log(String message, String level) {
        final int generalIndex = 0;
        final int infoIndex = 1;
        final int warnIndex = 2;
        final int errorIndex = 3;
        final int fatalIndex = 4;
        final int debugIndex = 5;
        boolean[] logSettings = AppContext.RUN_DETAILS.log;

        if ((logSettings[generalIndex])) {
            if (level.equalsIgnoreCase(Constants.INFO_FOR_LOGGING) && logSettings[infoIndex]) {
                logger.info(message);
            } else if (level.equalsIgnoreCase(Constants.DEBUG_FOR_LOGGING) && logSettings[debugIndex]) {
                logger.debug(message);
            } else if (level.equalsIgnoreCase(Constants.ERROR_FOR_LOGGING) && logSettings[errorIndex]) {
                logger.error(message);
            } else if (level.equalsIgnoreCase(Constants.WARN_FOR_LOGGING) && logSettings[warnIndex]) {
                logger.warn(message);
            } else if (level.equalsIgnoreCase(Constants.FATAL_FOR_LOGGING) && logSettings[fatalIndex]) {
                logger.fatal(message);
            }
        }
    }

    /**
     * Logs a message at the specified level before loading the application context.
     * This method is used for logging messages that occur before the application context is fully initialized, so it
     * does not slow the simulation.
     * Mainly it was done because logging settings are not yet available before loading the application context, and
     * logging during loading should always be enabled.
     *
     * @param message The message to log.
     * @param level The log4j level at which to log the message (INFO, DEBUG, ERROR, WARN, FATAL).
     **/
    public static void logBeforeLoading(String message, String level) {
        if (level.equalsIgnoreCase(Constants.INFO_FOR_LOGGING)) {
            logger.info(message);
        } else if (level.equalsIgnoreCase(Constants.DEBUG_FOR_LOGGING)) {
            logger.debug(message);
        } else if (level.equalsIgnoreCase(Constants.ERROR_FOR_LOGGING)) {
            logger.error(message);
        } else if (level.equalsIgnoreCase(Constants.WARN_FOR_LOGGING)) {
            logger.warn(message);
        } else if (level.equalsIgnoreCase(Constants.FATAL_FOR_LOGGING)) {
            logger.fatal(message);
        }
    }
}
