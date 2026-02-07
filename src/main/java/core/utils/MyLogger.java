package core.utils;

import app.AppContext;

import core.utils.constants.Constants;
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
        final int GENERAL_INDEX = Constants.GENERAL_LOGGING_INDEX;
        final int INFO_INDEX = Constants.INFO_LOGGING_INDEX;
        final int WARN_INDEX = Constants.WARN_LOGGING_INDEX;
        final int ERROR_INDEX = Constants.ERROR_LOGGING_INDEX;
        final int FATAL_INDEX = Constants.FATAL_LOGGING_INDEX;
        final int DEBUG_INDEX = Constants.DEBUG_LOGGING_INDEX;
        boolean[] logSettings = AppContext.RUN_DETAILS.log;

        if ((logSettings[GENERAL_INDEX])) {
            if (level.equalsIgnoreCase(Constants.INFO_FOR_LOGGING) && logSettings[INFO_INDEX]) {
                logger.info(message);
            } else if (level.equalsIgnoreCase(Constants.DEBUG_FOR_LOGGING) && logSettings[DEBUG_INDEX]) {
                logger.debug(message);
            } else if (level.equalsIgnoreCase(Constants.ERROR_FOR_LOGGING) && logSettings[ERROR_INDEX]) {
                logger.error(message);
            } else if (level.equalsIgnoreCase(Constants.WARN_FOR_LOGGING) && logSettings[WARN_INDEX]) {
                logger.warn(message);
            } else if (level.equalsIgnoreCase(Constants.FATAL_FOR_LOGGING) && logSettings[FATAL_INDEX]) {
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
