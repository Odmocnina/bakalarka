package core.utils.loading;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.model.Road;
import core.model.cellular.CellularRoad;
import core.model.continous.ContinuosRoad;
import core.utils.MyLogger;
import core.utils.StringEditor;
import core.utils.constants.Constants;
import core.utils.constants.RoadLoadingConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/******************************************************
 * Class for loading roads from XML files, it reads the map file and creates Road objects based on the parameters
 * specified in the file
 *
 * @author Michael Hladky
 * @version 1.0
 ******************************************************/
public class RoadLoader {

    /**
     * method to load the map from the specified file path, it calls loadMapStart to read the file and create Road
     * objects, then sets the loaded roads in the simulation context
     *
     * @param filePath path to the map configuration XML file
     * @return true if the map was loaded successfully, false otherwise
     **/
    public static boolean loadMap(String filePath) {
        Road[] map = loadMapStart(filePath);

        if (map == null) {
            MyLogger.logBeforeLoading("No roads loaded from map file", Constants.ERROR_FOR_LOGGING);
            return false;
        }

        AppContext.SIMULATION.setRoads(map);
        return true;
    }

    /**
     * method to load the map from the specified file path, it reads the XML file and creates Road objects based on the
     * parameters specified in the file
     *
     * @param filePath path to the map configuration XML file
     * @return array of loaded Road objects, or null if loading failed
     **/
    public static Road[] loadMapStart(String filePath) {
        // Logic to read the configuration file for multiple roads
        File xmlFile;
        try {
            xmlFile = new File(filePath);
        } catch (NullPointerException e) {
            MyLogger.logBeforeLoading("Map file not found",
                    Constants.ERROR_FOR_LOGGING);
            return null;
        }
        Road[] map;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            int roadCount = doc.getElementsByTagName(RoadLoadingConstants.ROAD_TAG).getLength();
            MyLogger.logBeforeLoading("Loading map from config: number of roads=" + roadCount
                    , Constants.INFO_FOR_LOGGING);

            map = new Road[roadCount];

            for (int i = 0; i < roadCount; i++) {
                Element roadElement = (Element) doc.getElementsByTagName(RoadLoadingConstants.ROAD_TAG).item(i);
                Road road = loadRoad(roadElement);
                if (road != null) {
                    map[i] = road;
                } else {
                    MyLogger.logBeforeLoading("Failed to load road: " + (i + 1) + " from file",
                            Constants.ERROR_FOR_LOGGING);
                    return null;
                }
            }

        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error loading map file: " + e.getMessage(), Constants.ERROR_FOR_LOGGING);
            return null;
        }

        return map;
    }

    /**
     * method to load a single road from the specified file path
     *
     * @param roadElement XML Element containing road configuration
     * @return loaded Road object, or null if loading failed
     **/
    public static Road loadRoad(Element roadElement) {
        Element length = (Element) roadElement.getElementsByTagName(RoadLoadingConstants.ROAD_LENGTH_TAG).item(0);
        Element maxSpeed = (Element) roadElement.getElementsByTagName(RoadLoadingConstants.ROAD_MAX_SPEED_TAG).item(0);
        Element numberOfLanes = (Element) roadElement.getElementsByTagName(RoadLoadingConstants.NUMBER_OF_LANES_TAG).item(0);
        // get lane elements in array
        NodeList laneNodes = roadElement.getElementsByTagName(RoadLoadingConstants.ROAD_LANE_TAG);

        double lengthValue = Double.parseDouble(length.getTextContent());
        double maxSpeedValue = Double.parseDouble(maxSpeed.getTextContent());
        int numberOfLanesValue = Integer.parseInt(numberOfLanes.getTextContent());

        Road road;
        if (AppContext.CAR_FOLLOWING_MODEL.getType().equals(Constants.CELLULAR)) {
            road = new CellularRoad(lengthValue, numberOfLanesValue, maxSpeedValue, AppContext.CAR_FOLLOWING_MODEL.getCellSize());
        } else if (AppContext.CAR_FOLLOWING_MODEL.getType().equals(Constants.CONTINUOUS)) {
            road = new ContinuosRoad(lengthValue, numberOfLanesValue, maxSpeedValue);
        } else {
            MyLogger.logBeforeLoading("Unknown car following model type: " + AppContext.CAR_FOLLOWING_MODEL.getType()
                    , Constants.FATAL_FOR_LOGGING);
            return null;
        }

        // Load light plans for each lane
        for (int i = 0; i < numberOfLanesValue; i++) {
            Element laneElement = (Element) laneNodes.item(i);
            int cycleDuration = Integer.parseInt(laneElement.getElementsByTagName(RoadLoadingConstants.CYCLE_DURATION_TAG).item(0).getTextContent());
            int timeOfSwitch = Integer.parseInt(laneElement.getElementsByTagName(RoadLoadingConstants.TIME_OF_SWITCH_TAG).item(0).getTextContent());
            boolean startWithGreen = Boolean.parseBoolean(laneElement.getElementsByTagName(RoadLoadingConstants.START_WITH_GREEN_TAG).item(0).getTextContent());
            LightPlan lp = new LightPlan(cycleDuration, timeOfSwitch, startWithGreen);
            road.setLightPlan(i, lp);
        }

        // Load car generators for each lane
        for (int i = 0; i < numberOfLanesValue; i++) {
            Element laneElement = (Element) laneNodes.item(i);
            Element generatorElement = (Element) laneElement.getElementsByTagName(RoadLoadingConstants.GENERATOR_TAG).item(0);
            CarGenerator generator = createGenerator(generatorElement);
            String type = AppContext.CAR_FOLLOWING_MODEL.getType();
            //generator.setType(type, cellSize);
            generator.setType(type);
            String carGenerationParameters = StringEditor.mergeRequestParameters(AppContext.CAR_FOLLOWING_MODEL.getParametersForGeneration(),
                     AppContext.LANE_CHANGING_MODEL.getParametersForGeneration());
            generator.setCarGenerationParameters(carGenerationParameters);
            road.setRoadGenerator(i, generator);
        }

        if (road.getLength() == 0) {
            MyLogger.logBeforeLoading("No roads loaded from map file", Constants.FATAL_FOR_LOGGING);
            return null;
        }

        boolean allParametersInGenerators = road.doAllGeneratorsHaveNeededParameters();
        if (!allParametersInGenerators) {
            MyLogger.logBeforeLoading("Not all car generators have necessary parameters for models, exiting"
                    , Constants.FATAL_FOR_LOGGING);
            return null;
        }

        road.setUpQueuesIfNeeded();
        return road;
    }

    /**
     * method to create a CarGenerator object from the specified XML element
     *
     * @param generatorElement XML Element containing generator configuration
     * @return created CarGenerator object
     **/
    private static CarGenerator createGenerator(Element generatorElement) {
        double flowRate = Double.parseDouble(generatorElement.getElementsByTagName(RoadLoadingConstants.FLOW_RATE_TAG).
                item(0).getTextContent());
        CarGenerator generator = new CarGenerator(flowRate);

        //queue
        Element queueElement = (Element) generatorElement.getElementsByTagName(RoadLoadingConstants.QUEUE_TAG).item(0);

        if (queueElement != null) {
            boolean useQueue = Boolean.parseBoolean(queueElement.getElementsByTagName(RoadLoadingConstants.USE_TAG).item(0).getTextContent());

            if (useQueue) {
                int minValue = Integer.parseInt(queueElement.getElementsByTagName(RoadLoadingConstants.MIN_VALUE_TAG).item(0).getTextContent());
                int maxValue = Integer.parseInt(queueElement.getElementsByTagName(RoadLoadingConstants.MAX_VALUE_TAG).item(0).getTextContent());
                generator.setQueueSize(minValue, maxValue);
            }
        }

        // load all car parameters
        Element carParamsElement = (Element) generatorElement.getElementsByTagName(RoadLoadingConstants.CAR_PARAMS_TAG).item(0);
        NodeList paramNodes = carParamsElement.getChildNodes();
        for (int i = 0; i < paramNodes.getLength(); i++) {
            if (paramNodes.item(i) instanceof Element paramElement) {
                String key = paramElement.getTagName();
                String name = paramElement.getElementsByTagName(RoadLoadingConstants.NAME_TAG).item(0).getTextContent();
                double minValue = Double.parseDouble(paramElement.getElementsByTagName(RoadLoadingConstants.MIN_VALUE_TAG).item(0).getTextContent());
                double maxValue = Double.parseDouble(paramElement.getElementsByTagName(RoadLoadingConstants.MAX_VALUE_TAG).item(0).getTextContent());
                generator.addComParameter(key, name, minValue, maxValue);
            }
        }

        generator.copyComParametersToRealParameters(AppContext.CAR_FOLLOWING_MODEL.getType(), AppContext.CAR_FOLLOWING_MODEL.getCellSize());

        return generator;
    }

}
