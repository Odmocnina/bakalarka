package core.utils;

import app.AppContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyLogger {

    private static final Logger logger = LogManager.getLogger(MyLogger.class);

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
