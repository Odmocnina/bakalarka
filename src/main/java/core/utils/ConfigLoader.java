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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigLoader {

    private static File configFile;

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

    public static Road[] loadRoads() {
        Road[] roads;

        try {
            int numberOfRoads = 0;
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
            } else if (id.equals("idmn")) {
                modelFromConfig = new IDMN();
            } else if (id.equals("rule184")) {
                modelFromConfig = new Rule184();
            } else if (id.equals("ovm")) {
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
                //AppContext.cellSize = cellSize;
            }

            return modelFromConfig;
        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error loading config file: " + e.getMessage()
                    , Constants.FATAL_FOR_LOGGING);
        }

        return null;
    }

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

    public static CarGenerator loadCarGenerator() {
        CarGenerator loadedGenerator = null;
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
            Element outputFile = (Element) runDetailsElement.getElementsByTagName("outputFile").item(0);
            Element drawCells = (Element) runDetailsElement.getElementsByTagName("drawCells").item(0);
            Element logElements = (Element) runDetailsElement.getElementsByTagName("logging").item(0);
            Element timeBetweenSteps = (Element) runDetailsElement.getElementsByTagName("timeBetweenSteps")
                    .item(0);
            //Element writeResults = (Element) runDetailsElement.getElementsByTagName("writeResults").item(0);

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

            if (outputFile != null) {
                MyLogger.logBeforeLoading("Output file from config: " + outputFile.getTextContent()
                        , Constants.INFO_FOR_LOGGING);
                detailsFromConfig.outputFile = outputFile.getTextContent();
            } else {
                MyLogger.logBeforeLoading("Missing outputFile in run details, results will not be saved"
                        , Constants.WARN_FOR_LOGGING);
                detailsFromConfig.outputFile = null;
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

            final String generalLogging = "log";
            final String infoLogging = "info";
            final String warnLogging = "warn";
            final String errorLogging = "error";
            final String fatalLogging = "fatal";
            final String debugLogging = "debug";

            final int generalIndex = 0;
            final int infoIndex = 1;
            final int warnIndex = 2;
            final int errorIndex = 3;
            final int fatalIndex = 4;
            final int debugIndex = 5;

            if (logElements != null) {
                NodeList logChildren = logElements.getChildNodes();
                for (int i = 0; i < logChildren.getLength(); i++) {
                    Node logNode = logChildren.item(i);
                    if (logNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element logElement = (Element) logNode;
                        String logType = logElement.getNodeName();
                        boolean logValue = Boolean.parseBoolean(logElement.getTextContent());

                        switch (logType) {
                            case generalLogging:
                                detailsFromConfig.log[generalIndex] = logValue;
                                break;
                            case infoLogging:
                                detailsFromConfig.log[infoIndex] = logValue;
                                break;
                            case warnLogging:
                                detailsFromConfig.log[warnIndex] = logValue;
                                break;
                            case errorLogging:
                                detailsFromConfig.log[errorIndex] = logValue;
                                break;
                            case fatalLogging:
                                detailsFromConfig.log[fatalIndex] = logValue;
                                break;
                            case debugLogging:
                                detailsFromConfig.log[debugIndex] = logValue;
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

            return detailsFromConfig;
        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error loading config file: " + e.getMessage()
                    , Constants.FATAL_FOR_LOGGING);
        }

        return null;
    }

}
