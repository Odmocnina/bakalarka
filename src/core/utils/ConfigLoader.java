package core.utils;

import app.AppContext;
import core.model.*;
import core.model.cellular.CellularRoad;
import core.model.continous.ContinuosRoad;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import models.carFollowingModels.IDM;
import models.carFollowingModels.NagelSchreckenberg;

import models.carFollowingModels.Rule184;
import models.laneChangingModels.Rickert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ConfigLoader {

    private static File configFile;

    public static boolean giveConfigFile(String filePath) {
        try {
            configFile = new File(filePath);
        } catch (NullPointerException e) {
            System.out.println("Config file not found, loading default config");
            configFile = new File(Constants.CONFIG_FILE);
            if (!configFile.exists()) {
                System.out.println("Default config file not found, exiting");
                return false;
            }
        }

        return true;
    }

    public static Road[] loadRoads(String filePath) {
        Road[] roads;

        try {
            int numberOfRoads = 0;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(configFile);
            Element numberOfRoadsElement = (Element) doc.getElementsByTagName("numberOfRoads").item(0);
            numberOfRoads = Integer.parseInt(numberOfRoadsElement.getTextContent());

            if (numberOfRoads <= 0) {
                System.out.println("Number of roads must be greater than 0, exiting");
                return null;
            }

            String roadFile = doc.getElementsByTagName("roadFile").item(0).getTextContent();

            if (roadFile == null || roadFile.isEmpty()) {
                System.out.println("Road file path is empty, exiting");
                return null;
            }

            if (!new File(roadFile).exists()) {
                System.out.println("Road file does not exist: " + roadFile + ", exiting");
                return null;
            }

            roads = new Road[numberOfRoads];
            for (int i = 0; i < numberOfRoads; i++) {
                Road road = loadRoad(roadFile);
                if (road == null) {
                    System.out.println("Failed to load road from file: " + roadFile + ", exiting");
                    return null;
                }
                roads[i] = road;
            }

            return roads;
        } catch (Exception e) {
            System.out.println("Error loading config file: " + e.getMessage());
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
            System.out.println("Config file not found, loading default config");
            xmlFile = new File(Constants.CONFIG_FILE);
            if (!xmlFile.exists()) {
                System.out.println("Default config file not found, exiting");
                return null;
            }
        }
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            Element road = (Element) doc.getElementsByTagName("road").item(0);
            System.out.println(road.getTextContent());
            int numberOfLanes = Integer.parseInt(road.getElementsByTagName("lanes").item(0).getTextContent());
            if (numberOfLanes <= 0) {
                System.out.println("Number of lanes must be greater than 0, exiting");
                return null;
            }
            int getMaxSpeed = Integer.parseInt(road.getElementsByTagName("maxSpeed").item(0).getTextContent());
            if (getMaxSpeed <= 0) {
                System.out.println("Max speed must be greater than 0, exiting");
                return null;
            }
            int roadLength = Integer.parseInt(road.getElementsByTagName("roadLength").item(0).getTextContent());
            if (roadLength <= 0) {
                System.out.println("Road length must be greater than 0, exiting");
                return null;
            }
            String type = AppContext.CAR_FOLLOWING_MODEL.getType();
            System.out.println("Loading road from config: lanes=" + numberOfLanes + ", maxSpeed=" + getMaxSpeed +
                    ", roadLength=" + roadLength + ", type=" + type);

            if (type.equals(Constants.CELLULAR)) {
                AppContext.drawCells = Boolean.
                        parseBoolean(road.getElementsByTagName("drawCells").item(0).getTextContent());
            }

            if (type.equals(Constants.CELLULAR)) {
                roadFormConfig = new CellularRoad(roadLength, numberOfLanes, getMaxSpeed);
            } else if (type.equals(Constants.CONTINOUS)) {
                roadFormConfig = new ContinuosRoad(roadLength, numberOfLanes, getMaxSpeed);
            } else {
                System.out.println("Unknown road type in config file: " + type + ", exiting");
                return null;
            }

            return roadFormConfig;
        } catch (Exception e) {
            System.out.println("Error loading config file: " + e.getMessage());
        }

        return null;
    }

    public static ICarFollowingModel loadCarFollowingModel(String filePath) {
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
                System.out.println("Unknown car-following model id in config file: " + id + ", exiting");
                return null;
            }

            String typeOfModel = modelFromConfig.getType();
            if (modelFromConfig.getType().equals(Constants.CELLULAR)) {
                double cellSize = modelFromConfig.getCellSize();
                if (cellSize <= 0) {
                    System.out.println("Cell size must be greater than 0, exiting");
                    return null;
                }
                AppContext.cellSize = cellSize;
            }

            return modelFromConfig;
        } catch (Exception e) {
            System.out.println("Error loading config file: " + e.getMessage());
        }

        return null;
    }

    public static ILaneChangingModel loadLaneChangingModel(String filePath) {
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
                System.out.println("Unknown lane changing model id in config file: " + id + ", exiting");
                return null;
            }

            return modelFromConfig;
        } catch (Exception e) {
            System.out.println("Error loading config file: " + e.getMessage());
        }

        return null;
    }

    public static CarGenerator loadCarGenerator(String filePath) {
        File xmlFile;
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


            if (flowRate == null) {
                System.out.println("Missing generator parameters in config file, exiting");
                return null;
            }

            double flow = Double.parseDouble(flowRate.getTextContent());
            if (flow < 0) {
                System.out.println("Invalid flow rate in config file, exiting");
                return null;
            }

            loadedGenerator = new CarGenerator(flow);

            if (minLength != null && maxLenght != null) {
                double minL = Double.parseDouble(minLength.getTextContent());
                double maxL = Double.parseDouble(maxLenght.getTextContent());
                if (minL <= 0 || maxL <= 0 || minL > maxL) {
                    System.out.println("Invalid length parameters in config file, exiting");
                    return null;
                }
                loadedGenerator.setMinLength(minL);
                loadedGenerator.setMaxLength(maxL);
            }

            if (minSpeed != null && maxSpeed != null) {
                double minS = Double.parseDouble(minSpeed.getTextContent());
                double maxS = Double.parseDouble(maxSpeed.getTextContent());
                if (minS <= 0 || maxS <= 0 || minS > maxS) {
                    System.out.println("Invalid speed parameters in config file, exiting");
                    return null;
                }
                loadedGenerator.setMinMaxSpeed(minS);
                loadedGenerator.setMaxMaxSpeed(maxS);
            }

            return loadedGenerator;
        } catch (Exception e) {
            System.out.println("Error loading config file: " + e.getMessage());
        }

        return null;
    }

    public static HashMap<String, String> loadRunDetails(String filePath) {
        File xmlFile;
        HashMap<String, String> runDetails = new HashMap<>();
        try {
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
            Element writeResults = (Element) runDetailsElement.getElementsByTagName("writeResults").item(0);

            if (duration != null) {
                runDetails.put("duration", duration.getTextContent());
            } else {
                System.out.println("Missing duration in run details, exiting");
                return null;
            }

            if (timeStep != null) {
                runDetails.put("timeStep", timeStep.getTextContent());
            } else {
                System.out.println("Missing timeStep in run details, exiting");
                return null;
            }

            if (showGui != null) {
                runDetails.put("showGui", showGui.getTextContent());
            } else {
                runDetails.put("showGui", "false");
            }

            if (writeResults != null) {
                runDetails.put("writeResults", writeResults.getTextContent());
                if (writeResults.getTextContent().equals("true")) {
                    if (outputFile != null) {
                        runDetails.put("outputFile", outputFile.getTextContent());
                    } else {
                        runDetails.put("outputFile", "simulation_output.txt");
                    }
                }
            } else {
                runDetails.put("writeResults", "false");
            }

            if (drawCells != null) {
                runDetails.put("drawCells", drawCells.getTextContent());
            } else {
                runDetails.put("drawCells", "false");
            }

            return runDetails;
        } catch (Exception e) {
            System.out.println("Error loading config file: " + e.getMessage());
        }

        return null;
    }

}
