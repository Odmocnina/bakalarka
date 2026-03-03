package app;

import core.utils.MyLogger;
import core.utils.constants.Constants;
import core.utils.loading.ConfigLoader;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import models.ModelId;

import java.util.ArrayList;
import java.util.List;

/*****************************
 * Class for handling input parameters from command line, such as duration of simulation, car following and lane
 * changing models to use if different then default ones in config, etc.
 *
 * @author Michael Hladky
 * @version 1.0
 ****************************/
public class InputParametersHandeler {

    /**
     * Method to handle help parameter, shows help message with usage instructions and available models, then exits
     **/
    public static void handleHelp() {
        StringBuilder helpMessage = new StringBuilder("""
                Usage: java -jar traffic-simulation.jar [options]
                Options:
                  --help                Show this help message and exit
                  --dur=<seconds>       Set duration of simulation in seconds (steps in simulation), if not provided app will run in GUI mode until user closes it, if provided app will run without GUI for specified duration and then exit
                  --config=<file>       Path to config file (XML)
                  --output=<file>       Path to output file for simulation results (TXT or CSV)
                  --log=<true/false>    Enable or disable logging (overrides config file settings)
                  --cfm=<model_id>      Car following model to use (overrides config file settings), e.g. 'idm'
                  --lcm=<model_id>      Lane changing model to use (overrides config file settings), e.g. 'mobil'
                  --map=<file>          Path to map file (XML) to load, if not provided default map from config will be used, if also not provided in config app will start without map (when gui is enabled) or exit (when gui is disabled)
                """);

        String laneChangingModelsPackage = "models.laneChangingModels";
        String carFollowingModelsPackage = "models.carFollowingModels";
        ArrayList<ModelNameAndId> laneChangingModelsCellular = getAllModels(laneChangingModelsPackage, Constants.CELLULAR);
        ArrayList<ModelNameAndId> carFollowingModelsCellular = getAllModels(carFollowingModelsPackage, Constants.CELLULAR);
        ArrayList<ModelNameAndId> laneChangingModelsContinuous = getAllModels(laneChangingModelsPackage, Constants.CONTINUOUS);
        ArrayList<ModelNameAndId> carFollowingModelsContinuous = getAllModels(carFollowingModelsPackage, Constants.CONTINUOUS);

        helpMessage.append("""

                Available models can be used in input parameters to specify which model to use instead of default one from config file, e.g. --cfm=idm to use Intelligent Driver Model (idm is its id) as forward model and --lcm for lane changing models. If these parameters arent specified stock form config will be used. Types of forward and lane change model MUST match. Below you can see now available models (name - id).\s
                """);

        helpMessage.append("\nAvailable cellular forward models:\n");
        for (ModelNameAndId model : carFollowingModelsCellular) {
            helpMessage.append("  ").append(model.name).append(" - ").append(model.id).append("\n");
        }

        helpMessage.append("\nAvailable continuous forward models:\n");
        for (ModelNameAndId model : carFollowingModelsContinuous) {
            helpMessage.append("  ").append(model.name).append(" - ").append(model.id).append("\n");
        }

        helpMessage.append("\nAvailable cellular lane changing models:\n");
        for (ModelNameAndId model : laneChangingModelsCellular) {
            helpMessage.append("  ").append(model.name).append(" - ").append(model.id).append("\n");
        }

        helpMessage.append("\nAvailable continuous lane changing models:\n");
        for (ModelNameAndId model : laneChangingModelsContinuous) {
            helpMessage.append("  ").append(model.name).append(" - ").append(model.id).append("\n");
        }

        System.out.println(helpMessage);
    }

    /**
     * Method to get all available car following / lane changing models, e.g. for showing them in help message or for
     * validating input parameters, etc. Gets them of one type (cellular/continuous) Uses reflexion.
     *
     * @param packageName package to search for models, e.g. "models.carFollowingModels" or "models.laneChangingModels"
     * @param type type of models to search for, e.g. "cellular" or "continuous", this is determined by getType() method
     * @return list of all available car following cellular model ids and names (values of ModelId annotation) in the
     *          application
     **/
    public static ArrayList<ModelNameAndId> getAllModels(String packageName, String type) {
        ArrayList<ModelNameAndId> modelIds = new ArrayList<>();

        try {
            // get all classes in the package, this method uses reflection to find all classes in the specified package
            List<Class<?>> classes = ConfigLoader.getClasses(packageName);

            for (Class<?> clazz : classes) {
                // check if class implements ICarFollowingModel and has ModelId annotation, if yes, create instance of
                // the model and check if its type is cellular (using getType() method from ICarFollowingModel interface)
                if (ICarFollowingModel.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(ModelId.class)) {

                    // create new instance of the model to call getType() method, this is needed because we need to
                    // check if the model is cellular or not
                    ICarFollowingModel model = (ICarFollowingModel) clazz.getDeclaredConstructor().newInstance();

                    // if the model is cellular, get its id and name and add it to the list of model ids to return
                    if (type.equals(model.getType())) {
                        ModelNameAndId modelInfo = new ModelNameAndId();

                        // get value of ModelId annotation for the model, this is the id that can be used in input
                        // parameters to specify the model, e.g. "idm" for "Intelligent Driver Model"
                        modelInfo.id = model.getID();
                        modelInfo.name = model.getName();

                        modelIds.add(modelInfo);
                    }
                } else if (ILaneChangingModel.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(ModelId.class)) {

                    // create new instance of the model to call getType() method, this is needed because we need to
                    // check if the model is cellular or not
                    ILaneChangingModel model = (ILaneChangingModel) clazz.getDeclaredConstructor().newInstance();

                    // if the model is cellular, get its id and name and add it to the list of model ids to return
                    if (type.equals(model.getType())) {
                        ModelNameAndId modelInfo = new ModelNameAndId();

                        // get value of ModelId annotation for the model, this is the id that can be used in input
                        // parameters to specify the model, e.g. "mobil" for "Minimizing Overall Braking Induced by Lane changes" model
                        modelInfo.id = model.getID();
                        modelInfo.name = model.getName();

                        modelIds.add(modelInfo);
                    }
                }
            }
        } catch (Exception e) {
            MyLogger.logLoadingOrSimulationStartEnd("Error searching for cellular models: " + e.getMessage(), Constants.FATAL_FOR_LOGGING);
        }

        return modelIds;
    }

    /**
     * Method to get specific parameter from input parameters, e.g. duration, config file path, output file path, etc.
     *
     * @param args input parameters from command line
     * @param prefix prefix of parameter to look for, e.g. "--dur=", "--config=", "--output=", "--log=", "--cfm=",
     *               "--lcm="
     * @return value of parameter if found, null if not found
     **/
    public static String getSpecificParameter(String[] args, String prefix) {
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }

        return null;
    }

    /**
     * Method to get part of parameter value from input parameter, e.g. duration value from "--dur=60", config file path
     * from "--config=path/to/config.xml", etc.
     *
     * @param arg input parameter from command line, e.g. "--dur=60", "--config=path/to/config.json", etc.
     * @param prefix prefix of parameter to look for, e.g. "--dur=", "--config=", "--output=", "--log=", "--cfm=",
     *               "--lcm="
     * @return part of parameter value if arg starts with prefix, null otherwise
     **/
    public static String getPartOfParameter(String arg, String prefix) {
        if (arg.startsWith(prefix)) {
            return arg.substring(prefix.length());
        }
        return null;
    }

    /**
     * Method to get duration value from input parameter, if provided, otherwise return constant indicating no duration provided
     *
     * @param duration duration value as string from input parameter, e.g. "60" from "--dur=60"
     * @return duration value as int if valid, constant indicating no duration provided or invalid input parameters otherwise
     **/
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

    /**
     * Method to get config file path from input parameter, if provided, otherwise return default config file path
     *
     * @param configPath config file path from input parameter, e.g. "path/to/config.json" from
     *                   "--config=path/to/config.xml"
     * @return config file path from input parameter if valid, default config file path otherwise
     **/
    public static String getConfigPathFromParameter(String configPath) {
        if (configPath == null || configPath.isEmpty()) {
            MyLogger.logLoadingOrSimulationStartEnd("No config file path provided in input parameters, using default config" +
                    " file path: " + Constants.DEFAULT_CONFIG_FILE, Constants.WARN_FOR_LOGGING);
            return Constants.DEFAULT_CONFIG_FILE;
        }
        MyLogger.logLoadingOrSimulationStartEnd("Config file path provided: " + configPath, Constants.INFO_FOR_LOGGING);
        return configPath;
    }

    /**
     * Method to get output file path from input parameter, if provided, otherwise return default output file path
     *
     * @param outputFilePath output file path from input parameter, e.g. "path/to/output.csv" from
     *                       "--output=path/to/output.csv"
     * @return output file path from input parameter if valid, default output file path otherwise
     **/
    public static String getOutputFilePathFromParameter(String outputFilePath) {
        if (outputFilePath == null || outputFilePath.isEmpty()) {
            MyLogger.logLoadingOrSimulationStartEnd("No output file path provided in input parameters, using default output " +
                    "file path: " + Constants.DEFAULT_OUTPUT_FILE, Constants.WARN_FOR_LOGGING);
            return Constants.DEFAULT_OUTPUT_FILE;
        }
        MyLogger.logLoadingOrSimulationStartEnd("Output file path provided: " + outputFilePath, Constants.INFO_FOR_LOGGING);
        return outputFilePath;
    }

    /**
     * Method to get logging settings from input parameter, if provided, otherwise return constant indicating no logging settings provided
     *
     * @param logging logging settings from input parameter, e.g. "true" from "--log=true"
     * @return constant indicating logging on/off from input parameters if valid, constant indicating no logging settings provided or invalid input parameters otherwise
     **/
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

    /**
     * Method to get car following model from input parameter, if provided, otherwise return default car following model from config
     *
     * @param carFollowingModelId car following model id from input parameter, e.g. "idm" from "--cfm=idm"
     * @return car following model instance if valid, null otherwise
     **/
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

    /**
     * Method to get car following model instance by id, using reflection to find class with matching ModelId annotation value
     *
     * @param id car following model id, e.g. "idm"
     * @return car following model instance if found, null otherwise
     **/
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
        }

        MyLogger.logLoadingOrSimulationStartEnd("Model with ID '" + targetId + "' not found.", Constants.WARN_FOR_LOGGING);
        return null;
    }

    /**
     * Method to get lane changing model from input parameter, if provided, otherwise return default lane changing model from config
     *
     * @param laneChangingModelId lane changing model id from input parameter, e.g. "mobil" from "--lcm=mobil"
     * @return lane changing model instance if valid, null otherwise
     **/
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

    /**
     * Method to get lane changing model instance by id, using reflection to find class with matching ModelId annotation value
     *
     * @param id lane changing model id, e.g. "mobil"
     * @return lane changing model instance if found, null otherwise
     **/
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
        }

        MyLogger.logLoadingOrSimulationStartEnd("Model with ID '" + targetId + "' not found.", Constants.WARN_FOR_LOGGING);
        return null;
    }

    /**
     * Method to get map file path from input parameter, if provided, otherwise return null to indicate that default map file from config should be used
     *
     * @param mapFile map file path from input parameter, e.g. "path/to/map.xml" from "--map=path/to/map.xml"
     * @return map file path from input parameter if valid, null otherwise (indicating default map file from config should be used)
     **/
    public static String handleMapFileParameter(String mapFile) {
        if (mapFile == null || mapFile.isEmpty()) {
            MyLogger.logLoadingOrSimulationStartEnd("No map file provided in input parameters, using default map file from config.", Constants.WARN_FOR_LOGGING);
            return null;
        }
        MyLogger.logLoadingOrSimulationStartEnd("Map file provided: " + mapFile, Constants.INFO_FOR_LOGGING);
        return mapFile;
    }

    /**************************************
     * Helper class to store model name and id for showing available models in help message, etc.
     *
     * @author Michael Hladky
     * @version 1.0
     **************************************/
    public static class ModelNameAndId {

        /** Model name for showing in help message, e.g. "Intelligent Driver Model" for "idm" car following model **/
        public String name;

        /** Model id for showing in help message, e.g. "idm" for "Intelligent Driver Model" car following model **/
        public String id;
    }


}
