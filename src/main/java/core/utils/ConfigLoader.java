package core.utils;

import app.AppContext;
import core.model.*;
import core.model.cellular.CellularRoad;
import core.model.continous.ContinuosRoad;
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
            Element numberOfRoadsElement = (Element) doc.getElementsByTagName("numberOfRoads").item(0);
            numberOfRoads = Integer.parseInt(numberOfRoadsElement.getTextContent());

            if (numberOfRoads <= 0) {
                MyLogger.logBeforeLoading("Invalid number of roads in config file: " + numberOfRoads
                                + ", exiting", Constants.FATAL_FOR_LOGGING);
                return null;
            }

            String roadFile = doc.getElementsByTagName("roadFile").item(0).getTextContent();

            if (roadFile == null || roadFile.isEmpty()) {
                MyLogger.logBeforeLoading("Road file path is empty, exiting", Constants.FATAL_FOR_LOGGING);
                return null;
            }

            if (!new File(roadFile).exists()) {
                MyLogger.logBeforeLoading("Road file does not exist: " + roadFile + ", exiting"
                        , Constants.FATAL_FOR_LOGGING);
                return null;
            }

            roads = new Road[numberOfRoads];
            for (int i = 0; i < numberOfRoads; i++) {
                Road road = loadRoad(roadFile);
                if (road == null) {
                    MyLogger.logBeforeLoading("Failed to load road from file: " + roadFile + ", exiting"
                            , Constants.FATAL_FOR_LOGGING);
                    return null;
                }
                roads[i] = road;
            }

            return roads;
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
            Element models = (Element) doc.getElementsByTagName("models").item(0);
            Element model = (Element) models.getElementsByTagName("carFollowingModel").item(0);
            String id = model.getElementsByTagName("id").item(0).getTextContent();
            id = id.toLowerCase().trim();

            //TODO add reflexion for dynamic loading of models
            if (id.equals("nagelschreckenberg")) {
                modelFromConfig = new NagelSchreckenberg();
            } else if (id.equals("idm")) {
                modelFromConfig = new IDM();
            } else if (id.equals("rule184")) {
                modelFromConfig = new Rule184();
            } else if (id.equals("ovm")) {
                modelFromConfig = new OVM();
            } else if (id.equals("fvdm")) {
                modelFromConfig = new OVM();
            } else if (id.equals("helly")) {
                modelFromConfig = new Helly();
            } else if (id.equals("headleading")) {
                modelFromConfig = new HeadLeading();
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
            Element models = (Element) doc.getElementsByTagName("models").item(0);
            Element model = (Element) models.getElementsByTagName("laneChangingModel").item(0);
            String id = model.getElementsByTagName("id").item(0).getTextContent();
            id = id.toLowerCase().trim();

            //TODO add reflexion for dynamic loading of models
            if (id.equals("rickert")) {
                modelFromConfig = new Rickert();
            } else if (id.equals("mobil")) {
                modelFromConfig = new Mobil();
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

            Element generator = (Element) doc.getElementsByTagName("generator").item(0);
            Element flowRate = (Element) generator.getElementsByTagName("flowRate").item(0);

            Element carParams = (Element) generator.getElementsByTagName("carParams").item(0);
            Element queue = (Element) generator.getElementsByTagName("queue").item(0);


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
                Element useQueue = (Element) queue.getElementsByTagName("use").item(0);

                if (useQueue != null && Boolean.parseBoolean(useQueue.getTextContent())) {
                    try {
                        int min = Integer.parseInt(queue.getElementsByTagName("minValue").item(0).getTextContent());
                        int max = Integer.parseInt(queue.getElementsByTagName("maxValue").item(0).getTextContent());

                        if (min < 0 || max < 0 || min > max) {
                            MyLogger.logBeforeLoading("Invalid queue parameters in config file, it will be" +
                                            " ignored.", Constants.ERROR_FOR_LOGGING);
                        } else {
                            loadedGenerator.addParameter(Constants.GENERATOR_QUEUE, (double) min, (double) max);
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
                        double minValue = Double.parseDouble(paramElement.getElementsByTagName("minValue").
                                item(0).getTextContent());
                        double maxValue = Double.parseDouble(paramElement.getElementsByTagName("maxValue").
                                item(0).getTextContent());
                        loadedGenerator.addParameter(paramName, minValue, maxValue);
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

            Element runDetailsElement = (Element) doc.getElementsByTagName("runDetails").item(0);
            Element duration = (Element) runDetailsElement.getElementsByTagName("duration").item(0);
            Element timeStep = (Element) runDetailsElement.getElementsByTagName("timeStep").item(0);
            Element showGui = (Element) runDetailsElement.getElementsByTagName("showGui").item(0);
            Element outputElements = (Element) runDetailsElement.getElementsByTagName("output").item(0);
            Element drawCells = (Element) runDetailsElement.getElementsByTagName("drawCells").item(0);
            Element logElements = (Element) runDetailsElement.getElementsByTagName("logging").item(0);
            Element timeBetweenSteps = (Element) runDetailsElement.getElementsByTagName("timeBetweenSteps")
                    .item(0);

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
                MyLogger.logBeforeLoading("Output will be written to file: " + detailsFromConfig.outputFile
                        , Constants.INFO_FOR_LOGGING);
            } else {
                MyLogger.logBeforeLoading("No output will be written", Constants.WARN_FOR_LOGGING);
            }

            if (drawCells != null) {
                detailsFromConfig.drawCells = Boolean.parseBoolean(drawCells.getTextContent());
            } else {
                detailsFromConfig.drawCells = false;
            }

            if (detailsFromConfig.duration == Constants.PARAMETER_UNDEFINED && !detailsFromConfig.showGui) {
                MyLogger.logBeforeLoading("Duration is undefined and GUI is disabled, simulation cannot run" +
                                ", exiting", Constants.FATAL_FOR_LOGGING);
                return null;
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
        final String GENERAL_LOGGING = "log";
        final String INFO_LOGGING = "info";
        final String WARN_LOGGING = "warn";
        final String ERROR_LOGGING = "error";
        final String FATAL_LOGGING = "fatal";
        final String DEBUG_LOGGING = "debug";

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
            detailsFromConfig.outputFile = null;
            return false;
        }

        NodeList outputChildren = outputElements.getChildNodes();

        boolean writeOutput = true;
        String outputFile = null;
        String outputType = "txt";

        for (int i = 0; i < outputChildren.getLength(); i++) {
            Node outputNode = outputChildren.item(i);
            if (outputNode.getNodeType() == Node.ELEMENT_NODE) {
                Element outputElement = (Element) outputNode;
                String nodeName = outputElement.getNodeName();

                switch (nodeName) {
                    case "file" -> outputFile = outputElement.getTextContent();
                    case "writeOutput" -> writeOutput = Boolean.parseBoolean(outputElement.getTextContent());
                    case "type" -> outputType = outputElement.getTextContent().toLowerCase().trim();
                }
            }
        }

        if (!writeOutput) {
            detailsFromConfig.outputFile = null;
            return false;
        } else {
            if (outputFile == null || outputFile.isEmpty()) {
                MyLogger.logBeforeLoading("Output file is absent or empty in run details, results will be" +
                                " saved to default output file: " + Constants.DEFAULT_OUTPUT_FILE
                                , Constants.WARN_FOR_LOGGING);
                outputFile = Constants.DEFAULT_OUTPUT_FILE;
            }
            detailsFromConfig.outputFile = outputFile;
            ResultsRecorder.getResultsRecorder().setOutputType(outputType);
            return true;
        }

    }

}
