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

public class ConfigLoader {

    private static final Logger logger = LogManager.getLogger(ConfigLoader.class);

    private static File configFile;

    public static boolean giveConfigFile(String filePath) {
        try {
            configFile = new File(filePath);
        } catch (NullPointerException e) {
            logger.error("Config file not found, loading default config");
            configFile = new File(Constants.CONFIG_FILE);
            if (!configFile.exists()) {
                logger.fatal("Default config file not found, exiting");
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
                logger.fatal("Invalid number of roads in config file: " + numberOfRoads + ", exiting");
                return null;
            }

            String roadFile = doc.getElementsByTagName("roadFile").item(0).getTextContent();

            if (roadFile == null || roadFile.isEmpty()) {
                logger.fatal("Road file path is empty, exiting");
                return null;
            }

            if (!new File(roadFile).exists()) {
                logger.fatal("Road file does not exist: " + roadFile + ", exiting");
                return null;
            }

            roads = new Road[numberOfRoads];
            for (int i = 0; i < numberOfRoads; i++) {
                Road road = loadRoad(roadFile);
                if (road == null) {
                    logger.fatal("Failed to load road from file: " + roadFile + ", exiting");
                    return null;
                }
                roads[i] = road;
            }

            return roads;
        } catch (Exception e) {
            logger.fatal("Error loading config file: " + e.getMessage());
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
            logger.error("Road file not found, loading default config");
            xmlFile = new File(Constants.CONFIG_FILE);
            if (!xmlFile.exists()) {
                logger.fatal("Default road file not found, exiting");
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
                logger.fatal("Number of lanes must be greater than 0, exiting");
                return null;
            }
            int getMaxSpeed = Integer.parseInt(road.getElementsByTagName("maxSpeed").item(0).getTextContent());
            if (getMaxSpeed <= 0) {
                logger.fatal("Max speed must be greater than 0, exiting");
                return null;
            }
            int roadLength = Integer.parseInt(road.getElementsByTagName("roadLength").item(0).getTextContent());
            if (roadLength <= 0) {
                logger.fatal("Road length must be greater than 0, exiting");
                return null;
            }
            String type = AppContext.CAR_FOLLOWING_MODEL.getType();
            logger.info("Loading road from config: lanes=" + numberOfLanes + ", maxSpeed=" + getMaxSpeed +
                    ", roadLength=" + roadLength + ", type=" + type);


            if (type.equals(Constants.CELLULAR)) {
                roadFormConfig = new CellularRoad(roadLength, numberOfLanes, getMaxSpeed);
            } else if (type.equals(Constants.CONTINOUS)) {
                roadFormConfig = new ContinuosRoad(roadLength, numberOfLanes, getMaxSpeed);
            } else {
                logger.fatal("Unknown road type in config file: " + type + ", exiting");
                return null;
            }

            return roadFormConfig;
        } catch (Exception e) {
            logger.fatal("Error loading config file: " + e.getMessage());
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

            //TODO add refexion for dynamic loading of models
            if (id.equals("nagelschreckenberg")) {
                modelFromConfig = new NagelSchreckenberg();
            } else if (id.equals("idm")) {
                modelFromConfig = new IDM();
            } else if (id.equals("rule184")) {
                modelFromConfig = new Rule184();
            } else {
                logger.fatal("Unknown car-following model id in config file: " + id + ", exiting");
                return null;
            }

            String typeOfModel = modelFromConfig.getType();
            if (modelFromConfig.getType().equals(Constants.CELLULAR)) {
                double cellSize = modelFromConfig.getCellSize();
                if (cellSize <= 0) {
                    logger.fatal("Cell size must be greater than 0, exiting");
                    return null;
                }
                AppContext.cellSize = cellSize;
            }

            return modelFromConfig;
        } catch (Exception e) {
            logger.fatal("Error loading config file: " + e.getMessage());
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

            //TODO add refexion for dynamic loading of models
            if (id.equals("rickert")) {
                modelFromConfig = new Rickert();
            } else {
                logger.fatal("Unknown lane changing model id in config file: " + id + ", exiting");
                return null;
            }

            return modelFromConfig;
        } catch (Exception e) {
            logger.fatal("Error loading config file: " + e.getMessage());
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
            Element minLength = (Element) generator.getElementsByTagName("minLength").item(0);
            Element maxLenght = (Element) generator.getElementsByTagName("maxLength").item(0);
            Element maxSpeed = (Element) generator.getElementsByTagName("maxMaxSpeed").item(0);
            Element minSpeed = (Element) generator.getElementsByTagName("minMaxSpeed").item(0);
            Element minAcceleration = (Element) generator.getElementsByTagName("minAcceleration").item(0);
            Element maxAcceleration = (Element) generator.getElementsByTagName("maxAcceleration").item(0);
            Element minDeceleration = (Element) generator.getElementsByTagName("minDeceleration").item(0);
            Element maxDeceleration = (Element) generator.getElementsByTagName("maxDeceleration").item(0);
            Element minDesiredTimeHeadway = (Element) generator.getElementsByTagName("minDesiredTimeHeadway").item(0);
            Element maxDesiredTimeHeadway = (Element) generator.getElementsByTagName("maxDesiredTimeHeadway").item(0);
            Element minMinGapToNextCar = (Element) generator.getElementsByTagName("minMinGapToNextCar").item(0);
            Element maxMinGapToNextCar = (Element) generator.getElementsByTagName("maxMinGapToNextCar").item(0);


            if (flowRate == null) {
                logger.fatal("Missing generator parameters in config file, exiting");
                return null;
            }

            double flow = Double.parseDouble(flowRate.getTextContent());
            if (flow < 0) {
                logger.fatal("Invalid flow rate in config file, exiting");
                return null;
            }

            loadedGenerator = new CarGenerator(flow);

            if (minLength != null && maxLenght != null) {
                double minL = Double.parseDouble(minLength.getTextContent());
                double maxL = Double.parseDouble(maxLenght.getTextContent());
                if (minL <= 0 || maxL <= 0 || minL > maxL) {
                    logger.fatal("Invalid length parameters in config file, exiting");
                    return null;
                }
                loadedGenerator.setMinLength(minL);
                loadedGenerator.setMaxLength(maxL);
            } else {
                logger.warn("Missing length parameters in config file, using default values");
            }

            if (minSpeed != null && maxSpeed != null) {
                double minS = Double.parseDouble(minSpeed.getTextContent());
                double maxS = Double.parseDouble(maxSpeed.getTextContent());
                if (minS <= 0 || maxS <= 0 || minS > maxS) {
                    logger.fatal("Invalid speed parameters in config file, exiting");
                    return null;
                }
                loadedGenerator.setMinMaxSpeed(minS);
                loadedGenerator.setMaxMaxSpeed(maxS);
            } else {
                logger.warn("Missing speed parameters in config file, using default values");
            }

            if (minAcceleration != null && maxAcceleration != null) {
                double minA = Double.parseDouble(minAcceleration.getTextContent());
                double maxA = Double.parseDouble(maxAcceleration.getTextContent());
                if (minA <= 0 || maxA <= 0 || minA > maxA) {
                    logger.fatal("Invalid acceleration parameters in config file, exiting");
                    return null;
                }
                loadedGenerator.setMinAcceleration(minA);
                loadedGenerator.setMaxAcceleration(maxA);
            } else {
                logger.warn("Missing acceleration parameters in config file, using default values");
            }

            if (minDeceleration != null && maxDeceleration != null) {
                double minD = Double.parseDouble(minDeceleration.getTextContent());
                double maxD = Double.parseDouble(maxDeceleration.getTextContent());
                if (minD <= 0 || maxD <= 0 || minD > maxD) {
                    logger.fatal("Invalid deceleration parameters in config file, exiting");
                    return null;
                }
                loadedGenerator.setMinDeceleration(minD);
                loadedGenerator.setMaxDeceleration(maxD);
            } else {
                logger.warn("Missing deceleration parameters in config file, using default values");
            }

            if (minDesiredTimeHeadway != null && maxDesiredTimeHeadway != null) {
                double minT = Double.parseDouble(minDesiredTimeHeadway.getTextContent());
                double maxT = Double.parseDouble(maxDesiredTimeHeadway.getTextContent());
                if (minT <= 0 || maxT <= 0 || minT > maxT) {
                    logger.fatal("Invalid desired time headway parameters in config file, exiting");
                    return null;
                }
                loadedGenerator.setMinDesiredTimeHeadway(minT);
                loadedGenerator.setMaxDesiredTimeHeadway(maxT);
            } else {
                logger.warn("Missing desired time headway parameters in config file, using default values");
            }

            if (minMinGapToNextCar != null && maxMinGapToNextCar != null) {
                double minG = Double.parseDouble(minMinGapToNextCar.getTextContent());
                double maxG = Double.parseDouble(maxMinGapToNextCar.getTextContent());
                if (minG <= 0 || maxG <= 0 || minG > maxG) {
                    logger.fatal("Invalid minimum gap to next car parameters in config file, exiting");
                    return null;
                }
                loadedGenerator.setMinMinGapToNextCar(minG);
                loadedGenerator.setMaxMinGapToNextCar(maxG);
            } else {
                logger.warn("Missing minimum gap to next car parameters in config file, using default values");
            }

            return loadedGenerator;
        } catch (Exception e) {
            logger.fatal("Error loading config file: " + e.getMessage());
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
            //Element writeResults = (Element) runDetailsElement.getElementsByTagName("writeResults").item(0);

            if (duration != null) {
                detailsFromConfig.duration = Integer.parseInt(duration.getTextContent());
                if (detailsFromConfig.duration <= 0) {
                    logger.fatal("Duration must be greater than 0, exiting");
                    return null;
                }
            } else {
                logger.warn("Missing duration in run details, setting to undefined");
                detailsFromConfig.duration = (int) Constants.PARAMETER_UNDEFINED;
            }

            if (timeStep != null) {
                logger.info("Time step from config: " + timeStep.getTextContent());
                detailsFromConfig.timeStep = Double.parseDouble(timeStep.getTextContent());
            } else {
                logger.fatal("Missing timeStep in run details, exiting");
                return null;
            }

            if (showGui != null) {
                logger.info("Show GUI from config: " + showGui.getTextContent());
                detailsFromConfig.showGui = Boolean.parseBoolean(showGui.getTextContent());
            } else {
                logger.info("Missing showGui in run details, setting to false");
                detailsFromConfig.showGui = false;
            }

            if (outputFile != null) {
                logger.info("Output file from config: " + outputFile.getTextContent());
                detailsFromConfig.outputFile = outputFile.getTextContent();
            } else {
                logger.warn("Missing outputFile in run details, results will not be saved");
                detailsFromConfig.outputFile = null;
            }

            if (drawCells != null) {
                detailsFromConfig.drawCells = Boolean.parseBoolean(drawCells.getTextContent());
            } else {
                detailsFromConfig.drawCells = false;
            }

            if (detailsFromConfig.duration == Constants.PARAMETER_UNDEFINED && !detailsFromConfig.showGui) {
                logger.fatal("Duration is undefined and GUI is disabled, simulation cannot run, exiting");
                return null;
            }

            return detailsFromConfig;
        } catch (Exception e) {
            logger.fatal("Error loading config file: " + e.getMessage());
        }

        return null;
    }

}
