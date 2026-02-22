package app;

import core.utils.MyLogger;
import core.utils.constants.Constants;
import core.utils.loading.ConfigLoader;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import models.ModelId;

import java.util.List;

/*****************************
 * Class for handling input parameters from command line, such as duration of simulation, car following and lane
 * changing models to use if different then default ones in config, etc.
 *
 * @author Michael Hladky
 * @version 1.0
 ****************************/
public class InputParametersHandeler {

    public static String getSpecificParameter(String[] args, String prefix) {
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }

        return null;
    }

    public static String getPartOfParameter(String arg, String prefix) {
        if (arg.startsWith(prefix)) {
            return arg.substring(prefix.length());
        }
        return null;
    }

    public static int getDurationFromParameter(String duration) {
        if (duration == null) {
            MyLogger.logLoadingOrSimulationStartEnd("Duration not given, application will run in GUI mode.",
                    Constants.INFO_FOR_LOGGING);
            return Constants.NO_DURATION_PROVIDED;
        }

        try {
            int durationValue = Integer.parseInt(duration);
            if (durationValue < 0) {
                MyLogger.logLoadingOrSimulationStartEnd("Duration value cannot be negative: " + durationValue + ", exiting.",
                        Constants.FATAL_FOR_LOGGING);
                return Constants.INVALID_INPUT_PARAMETERS;
            }
            MyLogger.logLoadingOrSimulationStartEnd("Duration provided: " + durationValue + " seconds (steps in simulation)," +
                    " starting app without GUI.", Constants.INFO_FOR_LOGGING);
            return durationValue;
        } catch (NumberFormatException e) {
            MyLogger.logLoadingOrSimulationStartEnd("Invalid duration value provided in input parameters (needs to be " +
                    "Integer: " + duration + ", exiting.", Constants.FATAL_FOR_LOGGING);
            return Constants.INVALID_INPUT_PARAMETERS;
        }
    }

    public static String getConfigPathFromParameter(String configPath) {
        if (configPath == null || configPath.isEmpty()) {
            MyLogger.logLoadingOrSimulationStartEnd("No config file path provided in input parameters, using default config" +
                    " file path: " + Constants.DEFAULT_CONFIG_FILE, Constants.WARN_FOR_LOGGING);
            return Constants.DEFAULT_CONFIG_FILE;
        }
        MyLogger.logLoadingOrSimulationStartEnd("Config file path provided: " + configPath, Constants.INFO_FOR_LOGGING);
        return configPath;
    }

    public static String getOutputFilePathFromParameter(String outputFilePath) {
        if (outputFilePath == null || outputFilePath.isEmpty()) {
            MyLogger.logLoadingOrSimulationStartEnd("No output file path provided in input parameters, using default output " +
                    "file path: " + Constants.DEFAULT_OUTPUT_FILE, Constants.WARN_FOR_LOGGING);
            return Constants.DEFAULT_OUTPUT_FILE;
        }
        MyLogger.logLoadingOrSimulationStartEnd("Output file path provided: " + outputFilePath, Constants.INFO_FOR_LOGGING);
        return outputFilePath;
    }

    public static int getLoggingFromParameter(String logging) {
        if (logging == null || logging.isEmpty()) {
            MyLogger.logLoadingOrSimulationStartEnd("No logging settings provided in input parameters, using" +
                            " default logging settings from config file.", Constants.WARN_FOR_LOGGING);
            return Constants.LOGGING_NOT_PROVIDED_IN_INPUT_PARAMETERS;
        }

        String loggingLower = logging.toLowerCase();
        try {
            boolean loggingValue = Boolean.parseBoolean(loggingLower);
            MyLogger.logLoadingOrSimulationStartEnd("Logging settings provided: " + loggingValue, Constants.INFO_FOR_LOGGING);
            if (loggingValue) {
                return Constants.LOGGING_ON_FROM_INPUT_PARAMETERS;
            } else {
                return Constants.LOGGING_OFF_FROM_INPUT_PARAMETERS;
            }
        } catch (Exception e) {
            MyLogger.logLoadingOrSimulationStartEnd("Invalid value (needs to be true/false) for logging " +
                    "settings provided in input parameters: " + logging + ", using specification from config.",
                    Constants.FATAL_FOR_LOGGING);
            return Constants.LOGGING_NOT_PROVIDED_IN_INPUT_PARAMETERS;
        }
    }

    public static ICarFollowingModel getCarFollowingModelFromParameter(String carFollowingModelId) {
        if (carFollowingModelId == null || carFollowingModelId.isEmpty()) {
            MyLogger.logLoadingOrSimulationStartEnd("Invalid car following model provided in input parameters: " +
                    carFollowingModelId + ", exiting.", Constants.FATAL_FOR_LOGGING);
            return null;
        }
        ICarFollowingModel model = getCarFollowingModelById(carFollowingModelId);
        if (model == null) {
            MyLogger.logLoadingOrSimulationStartEnd("Invalid car following model provided in input parameters: " +
                    carFollowingModelId + ", exiting.", Constants.FATAL_FOR_LOGGING);
            return null;
        }
        MyLogger.logLoadingOrSimulationStartEnd("Car following model provided: " + carFollowingModelId, Constants.INFO_FOR_LOGGING);
        return model;
    }

    public static ICarFollowingModel getCarFollowingModelById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        final String targetId = id.toLowerCase().trim();
        String packageName = "models.carFollowingModels"; // package with car following models

        try {
            // use existing method in ConfigLoader to get all classes in the package, this method uses reflection to
            // find all classes in the specified package
            List<Class<?>> classes = ConfigLoader.getClasses(packageName);

            for (Class<?> clazz : classes) {
                // look if class implements ICarFollowingModel and has ModelId annotation, if yes, get value of ModelId
                // annotation and compare it with targetId
                if (ICarFollowingModel.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(ModelId.class)) {
                    ModelId annotation = clazz.getAnnotation(ModelId.class);

                    // if the same => found the model, create new instance and return it
                    if (annotation.value().equals(targetId)) {
                        MyLogger.logLoadingOrSimulationStartEnd("Found model via reflection (by ID): " + clazz.getSimpleName(), Constants.INFO_FOR_LOGGING);
                        return (ICarFollowingModel) clazz.getDeclaredConstructor().newInstance();
                    }
                }
            }
        } catch (Exception e) {
            MyLogger.logLoadingOrSimulationStartEnd("Error searching for model by ID: " + e.getMessage(), Constants.FATAL_FOR_LOGGING);
            e.printStackTrace();
        }

        MyLogger.logLoadingOrSimulationStartEnd("Model with ID '" + targetId + "' not found.", Constants.WARN_FOR_LOGGING);
        return null;
    }

    public static ILaneChangingModel getLaneChangingModelFromParameter(String laneChangingModelId) {
        if (laneChangingModelId == null || laneChangingModelId.isEmpty()) {
            MyLogger.logLoadingOrSimulationStartEnd("Invalid lane changing model provided in input parameters: " +
                    laneChangingModelId + ", exiting.", Constants.FATAL_FOR_LOGGING);
            return null;
        }
        ILaneChangingModel model = getLaneChangingModelById(laneChangingModelId);
        if (model == null) {
            MyLogger.logLoadingOrSimulationStartEnd("Invalid lane changing model provided in input parameters: " +
                    laneChangingModelId + ", exiting.", Constants.FATAL_FOR_LOGGING);
            return null;
        }
        MyLogger.logLoadingOrSimulationStartEnd("Lane changing model provided: " + laneChangingModelId, Constants.INFO_FOR_LOGGING);
        return model;
    }

    public static ILaneChangingModel getLaneChangingModelById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        final String targetId = id.toLowerCase().trim();
        String packageName = "models.laneChangingModels"; // package with lane changing models

        try {
            // use existing method in ConfigLoader to get all classes in the package, this method uses reflection to
            // find all classes in the specified package
            List<Class<?>> classes = ConfigLoader.getClasses(packageName);

            for (Class<?> clazz : classes) {
                // look if class implements ICarFollowingModel and has ModelId annotation, if yes, get value of ModelId
                // annotation and compare it with targetId
                if (ILaneChangingModel.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(ModelId.class)) {
                    ModelId annotation = clazz.getAnnotation(ModelId.class);

                    // if the same => found the model, create new instance and return it
                    if (annotation.value().equals(targetId)) {
                        MyLogger.logLoadingOrSimulationStartEnd("Found model via reflection (by ID): " + clazz.getSimpleName(), Constants.INFO_FOR_LOGGING);
                        return (ILaneChangingModel) clazz.getDeclaredConstructor().newInstance();
                    }
                }
            }
        } catch (Exception e) {
            MyLogger.logLoadingOrSimulationStartEnd("Error searching for model by ID: " + e.getMessage(), Constants.FATAL_FOR_LOGGING);
            e.printStackTrace();
        }

        MyLogger.logLoadingOrSimulationStartEnd("Model with ID '" + targetId + "' not found.", Constants.WARN_FOR_LOGGING);
        return null;
    }

    public static String handleMapFileParameter(String mapFile) {
        if (mapFile == null || mapFile.isEmpty()) {
            MyLogger.logLoadingOrSimulationStartEnd("No map file provided in input parameters, using default map file from config.", Constants.WARN_FOR_LOGGING);
            return null;
        }
        MyLogger.logLoadingOrSimulationStartEnd("Map file provided: " + mapFile, Constants.INFO_FOR_LOGGING);
        return mapFile;
    }


}
