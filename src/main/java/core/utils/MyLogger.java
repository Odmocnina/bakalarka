package core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyLogger {

    private static final Logger logger = LogManager.getLogger(MyLogger.class);

    public static void log(String message, String level) {
        if (true) {
            if (level.equalsIgnoreCase("info")) {
                logger.info(message);
            } else if (level.equalsIgnoreCase("debug")) {
                logger.debug(message);
            } else if (level.equalsIgnoreCase("error")) {
                logger.error(message);
            } else if (level.equalsIgnoreCase("warn")) {
                logger.warn(message);
            } else if (level.equalsIgnoreCase("fatal")) {
                logger.fatal(message);
            } else {
                logger.info(message); // default to info
            }
        }
    }

}
