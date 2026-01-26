package core.utils.loading;

import app.AppContext;
import core.model.*;
import core.model.cellular.CellularRoad;
import core.model.continous.ContinuosRoad;
import core.sim.Simulation;
import core.utils.*;
import core.utils.constants.ConfigConstants;
import core.utils.constants.Constants;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import models.carFollowingModels.*;
import models.laneChangingModels.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ui.render.CellularRoadRenderer;
import ui.render.ContinuousRoadRenderer;
import ui.render.IRoadRenderer;

/***********************************
 * Class for loading configuration from XML file and road files
 *
 * @author Michael Hladky
 * @version 1.0
 ***********************************/
public class ConfigLoader {

    /** configuration file **/
    private static File configFile;

    /**
     * Method to set the configuration file path, if null or invalid, defaults to Constants.CONFIG_FILE
     *
     * @param filePath path to the configuration file
     * @return true if the file was set successfully, false otherwise
     **/
    public static boolean giveConfigFile(String filePath) {
        try {
            configFile = new File(filePath);
        } catch (NullPointerException e) {
            MyLogger.logBeforeLoading("Config file not found, loading default config"
                    , Constants.ERROR_FOR_LOGGING);
            configFile = new File(Constants.CONFIG_FILE);
            if (!configFile.exists()) {
                MyLogger.logBeforeLoading("Default config file not found, exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return false;
            }
        }

        try {
            AppContext.CONFIG_XML = new ConfigXml(configFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * Method to load what file is road file and how many roads to load from the configuration file
     *
     * @return array of loaded roads, or null if loading failed
     **/
    public static Road[] loadRoads() {
        Road[] roads;

        try {
            int numberOfRoads;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(configFile);
            Element numberOfRoadsElement = (Element) doc.getElementsByTagName(ConfigConstants.NUMBER_OF_ROADS_TAG).item(0);
            numberOfRoads = Integer.parseInt(numberOfRoadsElement.getTextContent());

            if (numberOfRoads <= 0) {
                MyLogger.logBeforeLoading("Invalid number of roads in config file: " + numberOfRoads
                                + ", exiting", Constants.FATAL_FOR_LOGGING);
                return null;
            }

            String roadFile = doc.getElementsByTagName(ConfigConstants.ROAD_FILE_TAG).item(0).getTextContent();

            if (roadFile == null || roadFile.isEmpty()) {
                // to - do open without road file
                MyLogger.logBeforeLoading("Road file path is empty, exiting", Constants.FATAL_FOR_LOGGING);
                return null;
            }

            if (!new File(roadFile).exists()) {
                // to - do open without road file
                MyLogger.logBeforeLoading("Road file does not exist: " + roadFile + ", exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return null;
            }

            /*roads = new Road[numberOfRoads];
            for (int i = 0; i < numberOfRoads; i++) {
                Road road = loadRoad(roadFile);
                if (road == null) {
                    MyLogger.logBeforeLoading("Failed to load road from file: " + roadFile + ", exiting"
                            , Constants.FATAL_FOR_LOGGING);
                    return null;
                }
                roads[i] = road;
            }*/

           // RoadLoader.loadMap(roadFile);
           // roads = AppContext.SIMULATION.getRoads();
            return RoadLoader.loadMapStart(roadFile);
        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error loading config file: " + e.getMessage()
                    , Constants.FATAL_FOR_LOGGING);
        }

        return null;
    }

    private static String getMapFileName() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(configFile);

            String roadFile = doc.getElementsByTagName(ConfigConstants.ROAD_FILE_TAG).item(0).getTextContent();

            if (roadFile == null || roadFile.isEmpty()) {
                // to - do open without road file
                MyLogger.logBeforeLoading("Road file path is empty, exiting", Constants.FATAL_FOR_LOGGING);
                return null;
            }

            if (!new File(roadFile).exists()) {
                // to - do open without road file
                MyLogger.logBeforeLoading("Road file does not exist: " + roadFile + ", exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return null;
            }

            return roadFile;
        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error loading config file: " + e.getMessage()
                    , Constants.FATAL_FOR_LOGGING);
        }

        return null;
    }

    /**
     * Method to load a single road from the specified file path
     *
     * @param filePath path to the road configuration file
     * @return loaded Road object, or null if loading failed
     **/
    public static Road loadRoad(String filePath) {
        Road roadFormConfig;
        // Logic to read the configuration file for road parameters
        File xmlFile;
        try {
            xmlFile = new File(filePath);
        } catch (NullPointerException e) {
            MyLogger.logBeforeLoading("Road file not found, loading default config"
                    , Constants.ERROR_FOR_LOGGING);
            xmlFile = new File(Constants.CONFIG_FILE);
            if (!xmlFile.exists()) {
                MyLogger.logBeforeLoading("Default road file not found, exiting", Constants.FATAL_FOR_LOGGING);
                return null;
            }
        }
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            Element road = (Element) doc.getElementsByTagName("road").item(0);
            int numberOfLanes = Integer.parseInt(road.getElementsByTagName("lanes").item(0).getTextContent());
            if (numberOfLanes <= 0) {
                MyLogger.logBeforeLoading("Number of lanes must be greater than 0, exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return null;
            }



            int getMaxSpeed = Integer.parseInt(road.getElementsByTagName("maxSpeed").item(0).getTextContent());
            if (getMaxSpeed <= 0) {
                MyLogger.logBeforeLoading("Max speed must be greater than 0, exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return null;
            }
            int roadLength = Integer.parseInt(road.getElementsByTagName("roadLength").item(0).getTextContent());
            if (roadLength <= 0) {
                MyLogger.logBeforeLoading("Road length must be greater than 0, exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return null;
            }
            String type = AppContext.CAR_FOLLOWING_MODEL.getType();
            MyLogger.logBeforeLoading("Loading road from config: lanes=" + numberOfLanes + ", maxSpeed="
                    + getMaxSpeed + ", roadLength=" + roadLength + ", type=" + type, Constants.INFO_FOR_LOGGING);


            if (type.equals(Constants.CELLULAR)) {
                double cellSize = AppContext.CAR_FOLLOWING_MODEL.getCellSize();
                roadFormConfig = new CellularRoad(roadLength, numberOfLanes, getMaxSpeed, cellSize);
            } else if (type.equals(Constants.CONTINOUS)) {
                roadFormConfig = new ContinuosRoad(roadLength, numberOfLanes, getMaxSpeed);
            } else {
                MyLogger.logBeforeLoading("Unknown road type in config file: " + type + ", exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return null;
            }

            return roadFormConfig;
        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error loading road file: " + e.getMessage()
                    , Constants.FATAL_FOR_LOGGING);
        }

        return null;
    }

    /**
     * Method to load the car-following model from the configuration file
     *
     * @return loaded ICarFollowingModel object, or null if loading failed
     **/
    public static ICarFollowingModel loadCarFollowingModel() {
        ICarFollowingModel modelFromConfig;
        // Logic to load and return the appropriate car-following model based on modelId
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(configFile);
            doc.getDocumentElement().normalize();
            Element models = (Element) doc.getElementsByTagName(ConfigConstants.MODELS_TAG).item(0);
            Element model = (Element) models.getElementsByTagName(ConfigConstants.CAR_FOLLOWING_MODEL_TAG).item(0);
            String id = model.getElementsByTagName(ConfigConstants.ID_TAG).item(0).getTextContent();
            id = id.toLowerCase().trim();

            //TODO add reflexion for dynamic loading of models
            if (id.equals("nagelschreckenberg")) {
                modelFromConfig = new NagelSchreckenberg();
            } else if (id.equals("idm")) {
                modelFromConfig = new IDM();
            } else if (id.equals("rule184")) {
                modelFromConfig = new Rule184();
            } else if (id.equals("ovm-original")) {
                modelFromConfig = new OVM_Original();
            } else if (id.equals("ovm-different")) {
                modelFromConfig = new OVM_Different();
            } else if (id.equals("fvdm")) {
                modelFromConfig = new FVDM();
            } else if (id.equals("helly")) {
                modelFromConfig = new Helly();
            } else if (id.equals("head-leading")) {
                modelFromConfig = new HeadLeading();
            } else if (id.equals("gipps")) {
                modelFromConfig = new Gipps();
            } else if (id.equals("kkw-linear")) {
                modelFromConfig = new KKW_Linear();
            } else if (id.equals("kkw-quadratic")) {
                modelFromConfig = new KKW_Quadratic();
            } else {
                MyLogger.logBeforeLoading("Unknown car-following model id in config file: " + id + ", exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return null;
            }


            if (modelFromConfig.getType().equals(Constants.CELLULAR)) {
                double cellSize = modelFromConfig.getCellSize();
                if (cellSize <= 0) {
                    MyLogger.logBeforeLoading("Cell size must be greater than 0, exiting"
                            , Constants.FATAL_FOR_LOGGING);
                    return null;
                }
            }

            return modelFromConfig;
        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error loading config file: " + e.getMessage()
                    , Constants.FATAL_FOR_LOGGING);
        }

        return null;
    }

    /**
     * Method to load the lane-changing model from the configuration file
     *
     * @return loaded ILaneChangingModel object, or null if loading failed
     **/
    public static ILaneChangingModel loadLaneChangingModel() {
        ILaneChangingModel modelFromConfig;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(configFile);
            doc.getDocumentElement().normalize();
            Element models = (Element) doc.getElementsByTagName(ConfigConstants.MODELS_TAG).item(0);
            Element model = (Element) models.getElementsByTagName(ConfigConstants.LANE_CHANGING_MODEL_TAG).item(0);
            String id = model.getElementsByTagName(ConfigConstants.ID_TAG).item(0).getTextContent();
            id = id.toLowerCase().trim();

            //TODO add reflexion for dynamic loading of models
            if (id.equals("rickert-transsims")) {
                modelFromConfig = new RickertTranssims();
            } else if (id.equals("rickert")) {
                modelFromConfig = new Rickert();
            } else if (id.equals("f-stca")) {
                modelFromConfig = new F_STCA();
            } else if (id.equals("stca")) {
                modelFromConfig = new STCA();
            } else if (id.equals("mobil")) {
                modelFromConfig = new Mobil();
            } else if (id.equals("mobil-simple")) {
                modelFromConfig = new MobilSimple();
            } else {
                MyLogger.logBeforeLoading("Unknown lane changing model id in config file: " + id + ", exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return null;
            }

            return modelFromConfig;
        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error loading config file: " + e.getMessage()
                    , Constants.FATAL_FOR_LOGGING);
        }

        return null;
    }

    /**
     * Method to load the car generator from the configuration file
     *
     * @return loaded CarGenerator object, or null if loading failed
     **/
    public static CarGenerator loadCarGenerator() {
        CarGenerator loadedGenerator;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(configFile);
            doc.getDocumentElement().normalize();

            Element generator = (Element) doc.getElementsByTagName(ConfigConstants.GENERATOR_TAG).item(0);
            Element flowRate = (Element) generator.getElementsByTagName(ConfigConstants.FLOW_RATE_TAG).item(0);

            Element carParams = (Element) generator.getElementsByTagName(ConfigConstants.CAR_PARAMS_TAG).item(0);
            Element queue = (Element) generator.getElementsByTagName(ConfigConstants.QUEUE_TAG).item(0);


            if (flowRate == null) {
                MyLogger.logBeforeLoading("Missing generator parameters in config file, exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return null;
            }

            double flow = Double.parseDouble(flowRate.getTextContent());
            if (flow < 0) {
                MyLogger.logBeforeLoading("Invalid flow rate in config file, exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return null;
            }

            loadedGenerator = new CarGenerator(flow);

            if (queue != null) {
                Element useQueue = (Element) queue.getElementsByTagName(ConfigConstants.USE_TAG).item(0);

                if (useQueue != null && Boolean.parseBoolean(useQueue.getTextContent())) {
                    try {
                        int min = Integer.parseInt(queue.getElementsByTagName(ConfigConstants.MIN_VALUE_TAG).item(0).getTextContent());
                        int max = Integer.parseInt(queue.getElementsByTagName(ConfigConstants.MAX_VALUE_TAG).item(0).getTextContent());

                        if (min < 0 || max < 0 || min > max) {
                            MyLogger.logBeforeLoading("Invalid queue parameters in config file, it will be" +
                                            " ignored.", Constants.ERROR_FOR_LOGGING);
                        } else {
                            loadedGenerator.addParameter(Constants.GENERATOR_QUEUE, "Test", (double) min, (double) max);
                        }
                    } catch (Exception e) {
                        MyLogger.logBeforeLoading("Error parsing queue parameters in config file, it will be" +
                                        " ignored.", Constants.ERROR_FOR_LOGGING);
                    }

                }

            }

            if (carParams != null) {
                NodeList children = carParams.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node param = children.item(i);
                    String paramName = param.getNodeName();

                    if (param.getNodeType() == Node.ELEMENT_NODE) {
                        Element paramElement = (Element) param;
                        double minValue = Double.parseDouble(paramElement.getElementsByTagName(ConfigConstants.MIN_VALUE_TAG).
                                item(0).getTextContent());
                        double maxValue = Double.parseDouble(paramElement.getElementsByTagName(ConfigConstants.MAX_VALUE_TAG).
                                item(0).getTextContent());
                        loadedGenerator.addParameter(paramName, "Test", minValue, maxValue);
                    }
                }
            }

            return loadedGenerator;
        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error loading config file: " + e.getMessage()
                    , Constants.FATAL_FOR_LOGGING);
        }

        return null;
    }

    /**
     * Method to load the run details from the configuration file
     *
     * @return loaded RunDetails object, or null if loading failed
     **/
    public static RunDetails loadRunDetails() {
        try {
            RunDetails detailsFromConfig = new RunDetails();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(configFile);
            doc.getDocumentElement().normalize();

            Element runDetailsElement = (Element) doc.getElementsByTagName(ConfigConstants.RUN_DETAILS_TAG).
                    item(0);
            Element duration = (Element) runDetailsElement.getElementsByTagName(ConfigConstants.DURATION_TAG).item(0);
            Element timeStep = (Element) runDetailsElement.getElementsByTagName(ConfigConstants.TIME_STEP_TAG).item(0);
            Element showGui = (Element) runDetailsElement.getElementsByTagName(ConfigConstants.SHOW_GUI_TAG).item(0);
            Element outputElements = (Element) runDetailsElement.getElementsByTagName(ConfigConstants.OUTPUT_TAG).item(0);
            Element drawCells = (Element) runDetailsElement.getElementsByTagName(ConfigConstants.DRAW_CELLS_TAG).item(0);
            Element logElements = (Element) runDetailsElement.getElementsByTagName(ConfigConstants.LOGGING_TAG).item(0);
            Element timeBetweenSteps = (Element) runDetailsElement.getElementsByTagName(ConfigConstants.TIME_BETWEEN_STEPS_TAG)
                    .item(0);
            Element laneChange = (Element) runDetailsElement.getElementsByTagName(ConfigConstants.LANE_CHANGE_TAG).item(0);
            Element debug = (Element) runDetailsElement.getElementsByTagName(ConfigConstants.DEBUG_TAG).item(0);
            Element preventCollisions = (Element) runDetailsElement.getElementsByTagName(ConfigConstants.PREVENT_COLLISION_TAG).item(0);

            if (duration != null) {
                detailsFromConfig.duration = Integer.parseInt(duration.getTextContent());
                if (detailsFromConfig.duration <= 0) {
                    MyLogger.logBeforeLoading("Duration must be greater than 0, exiting"
                            , Constants.FATAL_FOR_LOGGING);
                    return null;
                }
            } else {
                MyLogger.logBeforeLoading("Missing duration in run details, setting to undefined"
                        , Constants.WARN_FOR_LOGGING);
                detailsFromConfig.duration = (int) Constants.PARAMETER_UNDEFINED;
            }

            if (timeStep != null) {
                MyLogger.logBeforeLoading("Time step from config: " + timeStep.getTextContent()
                        , Constants.INFO_FOR_LOGGING);
                detailsFromConfig.timeStep = Double.parseDouble(timeStep.getTextContent());
            } else {
                MyLogger.logBeforeLoading("Missing timeStep in run details, exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return null;
            }

            if (showGui != null) {
                MyLogger.logBeforeLoading("Show GUI from config: " + showGui.getTextContent()
                        , Constants.INFO_FOR_LOGGING);
                detailsFromConfig.showGui = Boolean.parseBoolean(showGui.getTextContent());
            } else {
                MyLogger.logBeforeLoading("Missing showGui in run details, setting to false"
                        , Constants.INFO_FOR_LOGGING);
                detailsFromConfig.showGui = false;
            }

            boolean hasOutput = loadOutput(detailsFromConfig, outputElements);
            if (hasOutput) {
                MyLogger.logBeforeLoading("Output will be written to file: " + detailsFromConfig.outputDetails
                                .outputFile, Constants.INFO_FOR_LOGGING);
            } else {
                MyLogger.logBeforeLoading("No output will be written", Constants.WARN_FOR_LOGGING);
            }

            if (debug != null) {
                detailsFromConfig.debug = Boolean.parseBoolean(debug.getTextContent());
            } else {
                detailsFromConfig.debug = false;
            }

            if (drawCells != null) {
                detailsFromConfig.drawCells = Boolean.parseBoolean(drawCells.getTextContent());
                MyLogger.logBeforeLoading("Draw cells from config: " + drawCells.getTextContent()
                        , Constants.INFO_FOR_LOGGING);
            } else {
                detailsFromConfig.drawCells = false;
                MyLogger.logBeforeLoading("Draw cells from config: false", Constants.INFO_FOR_LOGGING);
            }

            if (preventCollisions != null) {
                detailsFromConfig.preventCollisions = Boolean.parseBoolean(preventCollisions.getTextContent());
                MyLogger.logBeforeLoading("Prevent collisions from config: " + preventCollisions.getTextContent()
                        , Constants.INFO_FOR_LOGGING);
            } else {
                detailsFromConfig.preventCollisions = true;
                MyLogger.logBeforeLoading("Prevent collisions from config: true", Constants.INFO_FOR_LOGGING);
            }

            if (detailsFromConfig.duration == Constants.PARAMETER_UNDEFINED && !detailsFromConfig.showGui) {
                MyLogger.logBeforeLoading("Duration is undefined and GUI is disabled, simulation cannot run" +
                                ", exiting", Constants.FATAL_FOR_LOGGING);
                return null;
            }

            if (laneChange != null) {
                detailsFromConfig.laneChange = Boolean.parseBoolean(laneChange.getTextContent());
            } else {
                detailsFromConfig.laneChange = true; // default to lane changing enabled
            }

            if (detailsFromConfig.showGui) {
                if (timeBetweenSteps != null) {
                    detailsFromConfig.timeBetweenSteps = Integer.parseInt(timeBetweenSteps.getTextContent());
                    if (detailsFromConfig.timeBetweenSteps < 0) {
                        MyLogger.logBeforeLoading("Time between steps must be non-negative, exiting"
                                , Constants.FATAL_FOR_LOGGING);
                        return null;
                    }
                } else {
                    MyLogger.logBeforeLoading("Missing timeBetweenSteps in run details, setting to 1 second"
                            , Constants.WARN_FOR_LOGGING);
                    detailsFromConfig.timeBetweenSteps = 1000;
                }
            }

            loadLoggingFromConfig(detailsFromConfig, logElements);

            return detailsFromConfig;
        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error loading config file: " + e.getMessage()
                    , Constants.FATAL_FOR_LOGGING);
        }

        return null;
    }

    /**
     * Helper method to load logging settings from the configuration file into RunDetails so that method loadRunDetails
     * inst long as hell
     *
     * @param detailsFromConfig RunDetails object to populate
     * @param logElements XML Element containing logging settings
     **/
    private static void loadLoggingFromConfig(RunDetails detailsFromConfig, Element logElements) {
        final String GENERAL_LOGGING = ConfigConstants.LOG_GENERAL_TAG;
        final String INFO_LOGGING = ConfigConstants.LOG_INFO_TAG;
        final String WARN_LOGGING = ConfigConstants.LOG_WARN_TAG;
        final String ERROR_LOGGING = ConfigConstants.LOG_ERROR_TAG;
        final String FATAL_LOGGING = ConfigConstants.LOG_FATAL_TAG;
        final String DEBUG_LOGGING = ConfigConstants.LOG_DEBUG_TAG;

        final int GENERAL_INDEX = 0;
        final int INFO_INDEX = 1;
        final int WARN_INDEX = 2;
        final int ERROR_INDEX = 3;
        final int FATAL_INDEX = 4;
        final int DEBUG_INDEX = 5;

        if (logElements != null) {
            NodeList logChildren = logElements.getChildNodes();
            for (int i = 0; i < logChildren.getLength(); i++) {
                Node logNode = logChildren.item(i);
                if (logNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element logElement = (Element) logNode;
                    String logType = logElement.getNodeName();
                    boolean logValue = Boolean.parseBoolean(logElement.getTextContent());

                    switch (logType) {
                        case GENERAL_LOGGING:
                            detailsFromConfig.log[GENERAL_INDEX] = logValue;
                            break;
                        case INFO_LOGGING:
                            detailsFromConfig.log[INFO_INDEX] = logValue;
                            break;
                        case WARN_LOGGING:
                            detailsFromConfig.log[WARN_INDEX] = logValue;
                            break;
                        case ERROR_LOGGING:
                            detailsFromConfig.log[ERROR_INDEX] = logValue;
                            break;
                        case FATAL_LOGGING:
                            detailsFromConfig.log[FATAL_INDEX] = logValue;
                            break;
                        case DEBUG_LOGGING:
                            detailsFromConfig.log[DEBUG_INDEX] = logValue;
                            break;
                        default:
                            MyLogger.logBeforeLoading("Unknown log type in run details: " + logType
                                    , Constants.WARN_FOR_LOGGING);
                            break;
                    }
                }
            }
        } else {
            MyLogger.logBeforeLoading("Missing log details in run details, all logging will be performed"
                    , Constants.WARN_FOR_LOGGING);
        }
    }

    private static boolean loadOutput(RunDetails detailsFromConfig, Element outputElements) {
        if (outputElements == null) {
            MyLogger.logBeforeLoading("Missing output details in run details, defaulting to no output"
                    , Constants.WARN_FOR_LOGGING);
            detailsFromConfig.outputDetails = null;
            return false;
        }

        NodeList outputChildren = outputElements.getChildNodes();

        boolean writeOutput = true;
        String outputFile = null;
        String outputType = "txt";
        String csvSeparator = Constants.DEFAULT_CSV_SEPARATOR;
        OutputDetails outputDetails = new OutputDetails();

        for (int i = 0; i < outputChildren.getLength(); i++) {
            Node outputNode = outputChildren.item(i);
            if (outputNode.getNodeType() == Node.ELEMENT_NODE) {
                Element outputElement = (Element) outputNode;
                String nodeName = outputElement.getNodeName();

                switch (nodeName) {
                    case ConfigConstants.FILE_TAG -> outputFile = outputElement.getTextContent();
                    case ConfigConstants.WRITE_OUTPUT_TAG -> writeOutput = Boolean.parseBoolean(outputElement.getTextContent());
                    case ConfigConstants.TYPE_TAG -> outputType = outputElement.getTextContent().toLowerCase().trim();
                    case ConfigConstants.CSV_SEPARATOR_TAG -> csvSeparator = outputElement.getTextContent();
                    case ConfigConstants.WHAT_TO_WRITE_TAG -> outputDetails.changeWhatToOutput(outputElement);
                }
            }
        }

        if (!writeOutput) {
            MyLogger.logBeforeLoading("Output writing disabled in run details", Constants.INFO_FOR_LOGGING);
            detailsFromConfig.outputDetails = null;
            return false;
        } else {
            if (outputFile == null || outputFile.isEmpty()) {
                MyLogger.logBeforeLoading("Output file is absent or empty in run details, results will be" +
                                " saved to default output file: " + Constants.DEFAULT_OUTPUT_FILE
                                , Constants.WARN_FOR_LOGGING);
                outputFile = Constants.DEFAULT_OUTPUT_FILE;
            }
            outputDetails.outputFile = outputFile;
            outputDetails.csvSeparator = csvSeparator;
            detailsFromConfig.outputDetails = outputDetails;
            ResultsRecorder.getResultsRecorder().setOutputType(outputType);
            return true;
        }

    }

    public static boolean loadAllConfig(String[] args) {
        String configFile;
        if (args.length == 0) { // use default config file if none provided
            MyLogger.logBeforeLoading("No config file provided, using default: " + Constants.CONFIG_FILE,
                    Constants.WARN_FOR_LOGGING);
            configFile = Constants.CONFIG_FILE;
        } else {
            MyLogger.logBeforeLoading("Config file provided: " + args[0], Constants.INFO_FOR_LOGGING);
            configFile = args[0];
        }

        if (ConfigLoader.giveConfigFile(configFile)) { // wierd thing that I thought of, config file was opened multiple
            // times when loading config info so now its opened only once and class
            // remembers it
            MyLogger.logBeforeLoading("Config file opened successfully: " + configFile, Constants.INFO_FOR_LOGGING);
        } else {
            MyLogger.logBeforeLoading("Failed to open config file: " + configFile + ", exiting.",
                    Constants.FATAL_FOR_LOGGING);
            return false;
        }

        // load car following model
        ICarFollowingModel carFollowingModel = ConfigLoader.loadCarFollowingModel();
        if (carFollowingModel == null) {
            MyLogger.logBeforeLoading("Failed to load car following model, exiting."
                    , Constants.FATAL_FOR_LOGGING);
            return false;
        } else {
            MyLogger.logBeforeLoading("Loaded car following model: " + carFollowingModel.getID(),
                    Constants.INFO_FOR_LOGGING);
        }
        AppContext.CAR_FOLLOWING_MODEL = carFollowingModel; // store model in app context for later use

        // load lane changing model
        ILaneChangingModel laneChangingModel = ConfigLoader.loadLaneChangingModel();
        if (laneChangingModel == null) {
            MyLogger.logBeforeLoading("Failed to load lane changing model, exiting.", Constants.FATAL_FOR_LOGGING);
            return false;
        } else {
            MyLogger.logBeforeLoading("Loaded lane changing model: " + laneChangingModel.getID(),
                    Constants.INFO_FOR_LOGGING);
        }
        AppContext.LANE_CHANGING_MODEL = laneChangingModel; // store model in app context for later use

        // load car generator, thing that decides when cars are generated and what params do they have
        CarGenerator carGenerator = ConfigLoader.loadCarGenerator();
        if (carGenerator == null) {
            MyLogger.logBeforeLoading("Failed to load car generator, exiting.", Constants.FATAL_FOR_LOGGING);
            return false;
        } else {
            MyLogger.logBeforeLoading("Loaded car generator: " + carGenerator, Constants.INFO_FOR_LOGGING);
        }

        String requestedParams = StringEditor.mergeRequestParameters(carFollowingModel.getParametersForGeneration(),
                laneChangingModel.getParametersForGeneration());
        carGenerator.setCarGenerationParameters(requestedParams);

        // check if car generator has all parameters needed for the selected car following model, for example person
        // loads car following model which need max speed, the model needs those parameters to work
        // so generator gives needs to generate cars with max speed parameter, otherwise model won't work properly, lol
        if (carGenerator.checkIfAllParametersAreLoaded()) {
            MyLogger.logBeforeLoading("Car generator parameters are valid for the selected car following model.",
                    Constants.INFO_FOR_LOGGING);
        } else { // missing some parameters, exit
            MyLogger.logBeforeLoading("Car generator parameters are NOT valid for the selected " +
                    "car-following model/lane-changing model, exiting.", Constants.FATAL_FOR_LOGGING);
            return false;
        }

        // load roads from config
        Road[] roads = ConfigLoader.loadRoads();
        if (roads == null) {
            MyLogger.logBeforeLoading("Failed to load road configuration, exiting."
                    , Constants.FATAL_FOR_LOGGING);
            return false;
        } else {
            MyLogger.logBeforeLoading("Loaded road: " + roads[0].toString() + ", number of roads: "
                    + roads.length, Constants.INFO_FOR_LOGGING);
        }
        String mapFileName = ConfigLoader.getMapFileName();

        // check if type of road matches type of car following model
        if (!roads[0].getType().equals(carFollowingModel.getType())) {
            MyLogger.logBeforeLoading("Types of car following model and road do not match: model type=" +
                    carFollowingModel.getType() + ", road type=" + roads[0].getType(), Constants.FATAL_FOR_LOGGING);
            return false;
        } else {
            MyLogger.logBeforeLoading("Types of car following model and road match: " + roads[0].getType(),
                    Constants.INFO_FOR_LOGGING);
        }

        // set type of road for generator and store roads in road instance
        carGenerator.setType(roads[0]);
        for (Road road : roads) {
            road.setCarGenerators(carGenerator.clone());

            if (carGenerator.generatingToQueue()) {
                MyLogger.logBeforeLoading("Car generator is generating cars to queue."
                        , Constants.INFO_FOR_LOGGING);
                road.initializeCarQueues();
            } else {
                MyLogger.logBeforeLoading("Car generator is generating cars directly on road."
                        , Constants.INFO_FOR_LOGGING);
            }
        }

        IRoadRenderer renderer; // renderer for drawing roads in gui, depends on road type, because different road types
        // have different content
        if (roads[0].getType().equals(Constants.CELLULAR)) {
            renderer = new CellularRoadRenderer();
        } else if (roads[0].getType().equals(Constants.CONTINOUS)) {
            renderer = new ContinuousRoadRenderer();
        } else {
            MyLogger.logBeforeLoading("Unknown road type: " + roads[0].getType(), Constants.FATAL_FOR_LOGGING);
            return false;
        }
        AppContext.RENDERER = renderer;

        RunDetails runDetails = ConfigLoader.loadRunDetails(); // loading run details (show gui, duration...)
        if (runDetails == null) {
            MyLogger.logBeforeLoading("Failed to load run details, exiting.", Constants.FATAL_FOR_LOGGING);
            return false;
        } else {
            MyLogger.logBeforeLoading("Loaded run details: duration=" + runDetails.duration + ", timeStep=" +
                    runDetails.timeStep + ", showGui=" + runDetails.showGui + ", outputFile=" + runDetails.outputDetails
                    .outputFile + ", drawCells=" + runDetails.drawCells, Constants.INFO_FOR_LOGGING);
        }
        runDetails.setNewMapFile(mapFileName);
        AppContext.RUN_DETAILS = runDetails;

        ResultsRecorder.getResultsRecorder().initialize(roads.length, runDetails.outputDetails.outputFile);

        // create simulation and store it in app context, simulation is the thing that updates all roads and cars
        AppContext.SIMULATION = new Simulation(roads);
        return true;
    }

    public static File getConfigFile() {
        return configFile;
    }

    public void writeIntoConfig(String path, String content) {

    }

}
