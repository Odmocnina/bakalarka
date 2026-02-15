package app;

import core.utils.MyLogger;
import core.utils.constants.ConfigConstants;
import core.utils.constants.Constants;
import core.utils.loading.ConfigLoader;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import models.ModelId;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static core.utils.loading.ConfigLoader.getClasses;

/*****************************
 * Class for handling input parameters from command line, such as duration of simulation, car following and lane
 * changing models to use if different then default ones in config, etc.
 *
 * @author Michael Hladky
 * @version 1.0
 ****************************/
public class InputParametersHandeler {

    /**
     * Handles input parameters from command line, checks if duration parameter is provided and if yes, sets duration in
     * run details and returns true to indicate that application should be started without gui, otherwise returns false
     * to indicate that application should be started with gui
     *
     * @param args input parameters in command line
     * @return int value of duration if provided, -2 (constant INVALID_INPUT_PARAMETERS) if any of the parameters was invalid
     *          (not number duration, non-existent models or something like that), otherwise -1
     *          (constant NO_DURATION_PROVIDED) to indicate that duration is not provided and app will run in gui mode
     **/
    /*public static int handleParameters(String[] args, String configPath, String oufFile, ICarFollowingModel
            carFollowingModel, ILaneChangingModel laneChangingModel) {
        int duration = Constants.NO_DURATION_PROVIDED; // default value indicating duration is not provided
        String configPathInParameters = null;
        String outFileInParameters = null;
        ICarFollowingModel carFollowingModelInParameters = null;
        ILaneChangingModel laneChangingModelInParameters = null;
        for (String arg : args) {
            if (arg.startsWith("--d=")) {
                String durationStr = arg.substring("--d=".length());
                try {
                    duration = Integer.parseInt(durationStr);
                    AppContext.RUN_DETAILS.duration = duration;
                    MyLogger.logBeforeLoading("Duration provided: " + duration + " seconds (steps in simulation," +
                            " starting app without GUI.", Constants.INFO_FOR_LOGGING);
                } catch (NumberFormatException e) {
                    MyLogger.logBeforeLoading("Invalid duration value provided in input parameters: " +
                            durationStr + ", exiting.", Constants.FATAL_FOR_LOGGING);
                    return Constants.INVALID_INPUT_PARAMETERS;
                }
            } else if (arg.startsWith("--c=")) {
                String path = arg.substring("--c=".length());
                if (path.isEmpty()) {
                    MyLogger.logBeforeLoading("Invalid config file path provided in input parameters: " +
                            path + ", exiting.", Constants.FATAL_FOR_LOGGING);
                    return Constants.INVALID_INPUT_PARAMETERS;
                }
                configPath = path;
                MyLogger.logBeforeLoading("Config file path provided: " + configPath, Constants.INFO_FOR_LOGGING);
            } else if (arg.startsWith("--f=")) {
                String modelName = arg.substring("--f=".length());

                if (carFollowingModel == null) {
                    MyLogger.logBeforeLoading("Invalid car following model provided in input parameters: " +
                            modelName + ", exiting.", Constants.FATAL_FOR_LOGGING);
                    return Constants.INVALID_INPUT_PARAMETERS;
                }

                MyLogger.logBeforeLoading("Car following model provided: " + modelName, Constants.INFO_FOR_LOGGING);
            } else if (arg.startsWith("--l=")) {
                String modelName = arg.substring("--l=".length());

                if (laneChangingModel == null) {
                    MyLogger.logBeforeLoading("Invalid lane changing model provided in input parameters: " +
                            modelName + ", exiting.", Constants.FATAL_FOR_LOGGING);
                    return Constants.INVALID_INPUT_PARAMETERS;
                }

                MyLogger.logBeforeLoading("Lane changing model provided: " + modelName, Constants.INFO_FOR_LOGGING);
            } else {
                MyLogger.logBeforeLoading("Unknown input parameter provided: " + arg + ", exiting.", Constants.FATAL_FOR_LOGGING);
                return Constants.INVALID_INPUT_PARAMETERS;
            }
        }

        if (duration == Constants.NO_DURATION_PROVIDED) {
            MyLogger.logBeforeLoading("No duration provided, app will be started with GUI", Constants.WARN_FOR_LOGGING);
        }

        if (configPathInParameters != null) {
            configPath = configPathInParameters;
        } else {
            configPath = Constants.DEFAULT_CONFIG_FILE;
            MyLogger.logBeforeLoading("No config file path provided in input parameters, using default config file path: " +
                    configPath, Constants.WARN_FOR_LOGGING);
        }

        if (outFileInParameters != null) {
            oufFile = outFileInParameters;
        } else {
            oufFile = Constants.DEFAULT_OUTPUT_FILE;
            MyLogger.logBeforeLoading("No output file path provided in input parameters, using default output file path: " +
                    oufFile, Constants.WARN_FOR_LOGGING);
        }

        if (carFollowingModelInParameters != null) {
            carFollowingModel = carFollowingModelInParameters;
        } else {
            MyLogger.logBeforeLoading("No car following model provided in input parameters, using default car" +
                    " following model from config file.", Constants.WARN_FOR_LOGGING);
        }

        if (laneChangingModelInParameters != null) {
            laneChangingModel = laneChangingModelInParameters;
        } else {
            MyLogger.logBeforeLoading("No lane changing model provided in input parameters, using default " +
                    "lane changing model from config file.", Constants.WARN_FOR_LOGGING);
        }

        return duration;
    }*/

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
            MyLogger.logBeforeLoading("Duration not given, application will run in GUI mode.",
                    Constants.INFO_FOR_LOGGING);
            return Constants.NO_DURATION_PROVIDED;
        }

        try {
            int durationValue = Integer.parseInt(duration);
            if (durationValue < 0) {
                MyLogger.logBeforeLoading("Duration value cannot be negative: " + durationValue + ", exiting.",
                        Constants.FATAL_FOR_LOGGING);
                return Constants.INVALID_INPUT_PARAMETERS;
            }
            MyLogger.logBeforeLoading("Duration provided: " + durationValue + " seconds (steps in simulation)," +
                    " starting app without GUI.", Constants.INFO_FOR_LOGGING);
            return durationValue;
        } catch (NumberFormatException e) {
            MyLogger.logBeforeLoading("Invalid duration value provided in input parameters (needs to be " +
                    "Integer: " + duration + ", exiting.", Constants.FATAL_FOR_LOGGING);
            return Constants.INVALID_INPUT_PARAMETERS;
        }
    }

    public static String getConfigPathFromParameter(String configPath) {
        if (configPath == null || configPath.isEmpty()) {
            MyLogger.logBeforeLoading("No config file path provided in input parameters, using default config" +
                    " file path: " + Constants.DEFAULT_CONFIG_FILE, Constants.WARN_FOR_LOGGING);
            return Constants.DEFAULT_CONFIG_FILE;
        }
        MyLogger.logBeforeLoading("Config file path provided: " + configPath, Constants.INFO_FOR_LOGGING);
        return configPath;
    }

    public static String getOutputFilePathFromParameter(String outputFilePath) {
        if (outputFilePath == null || outputFilePath.isEmpty()) {
            MyLogger.logBeforeLoading("No output file path provided in input parameters, using default output " +
                    "file path: " + Constants.DEFAULT_OUTPUT_FILE, Constants.WARN_FOR_LOGGING);
            return Constants.DEFAULT_OUTPUT_FILE;
        }
        MyLogger.logBeforeLoading("Output file path provided: " + outputFilePath, Constants.INFO_FOR_LOGGING);
        return outputFilePath;
    }

    public static ICarFollowingModel getCarFollowingModelFromParameter(String carFollowingModelId) {
        if (carFollowingModelId == null || carFollowingModelId.isEmpty()) {
            MyLogger.logBeforeLoading("Invalid car following model provided in input parameters: " +
                    carFollowingModelId + ", exiting.", Constants.FATAL_FOR_LOGGING);
            return null;
        }
        ICarFollowingModel model = getCarFollowingModelById(carFollowingModelId);
        if (model == null) {
            MyLogger.logBeforeLoading("Invalid car following model provided in input parameters: " +
                    carFollowingModelId + ", exiting.", Constants.FATAL_FOR_LOGGING);
            return null;
        }
        MyLogger.logBeforeLoading("Car following model provided: " + carFollowingModelId, Constants.INFO_FOR_LOGGING);
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
                        MyLogger.logBeforeLoading("Found model via reflection (by ID): " + clazz.getSimpleName(), Constants.INFO_FOR_LOGGING);
                        return (ICarFollowingModel) clazz.getDeclaredConstructor().newInstance();
                    }
                }
            }
        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error searching for model by ID: " + e.getMessage(), Constants.FATAL_FOR_LOGGING);
            e.printStackTrace();
        }

        MyLogger.logBeforeLoading("Model with ID '" + targetId + "' not found.", Constants.WARN_FOR_LOGGING);
        return null;
    }

    public static ILaneChangingModel getLaneChangingModelFromParameter(String laneChangingModelId) {
        if (laneChangingModelId == null || laneChangingModelId.isEmpty()) {
            MyLogger.logBeforeLoading("Invalid lane changing model provided in input parameters: " +
                    laneChangingModelId + ", exiting.", Constants.FATAL_FOR_LOGGING);
            return null;
        }
        ILaneChangingModel model = getLaneChangingModelById(laneChangingModelId);
        if (model == null) {
            MyLogger.logBeforeLoading("Invalid lane changing model provided in input parameters: " +
                    laneChangingModelId + ", exiting.", Constants.FATAL_FOR_LOGGING);
            return null;
        }
        MyLogger.logBeforeLoading("Lane changing model provided: " + laneChangingModelId, Constants.INFO_FOR_LOGGING);
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
                        MyLogger.logBeforeLoading("Found model via reflection (by ID): " + clazz.getSimpleName(), Constants.INFO_FOR_LOGGING);
                        return (ILaneChangingModel) clazz.getDeclaredConstructor().newInstance();
                    }
                }
            }
        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error searching for model by ID: " + e.getMessage(), Constants.FATAL_FOR_LOGGING);
            e.printStackTrace();
        }

        MyLogger.logBeforeLoading("Model with ID '" + targetId + "' not found.", Constants.WARN_FOR_LOGGING);
        return null;
    }


}
