package core.utils;

import app.AppContext;
import core.model.LightPlan;
import core.model.Road;
import core.model.cellular.CellularRoad;
import core.model.continous.ContinuosRoad;
import core.utils.constants.Constants;
import core.utils.constants.RoadLoadingConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class RoadLoader {

  /*  public static void loadMap(String filePath) {
        // Logic to read the configuration file for multiple roads
        File xmlFile;
        try {
            xmlFile = new File(filePath);
        } catch (NullPointerException e) {
            MyLogger.logBeforeLoading("Map file not found, loading default config",
                    Constants.ERROR_FOR_LOGGING);
            xmlFile = new File(Constants.MAP_CONFIG_FILE);
            if (!xmlFile.exists()) {
                MyLogger.logBeforeLoading("Default map file not found, exiting", Constants.FATAL_FOR_LOGGING);
                return;
            }
        }
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            int roadCount = doc.getElementsByTagName("road").getLength();
            MyLogger.logBeforeLoading("Loading map from config: number of roads=" + roadCount
                    , Constants.INFO_FOR_LOGGING);

            Road[] map = new Road[roadCount];

            for (int i = 0; i < roadCount; i++) {
                Element roadElement = (Element) doc.getElementsByTagName("road").item(i);
                Road road = loadRoad(roadFilePath);
                if (road != null) {
                    map[i] = road;
                } else {
                    MyLogger.logBeforeLoading("Failed to load road from file: " + roadFilePath
                            , Constants.ERROR_FOR_LOGGING);
                }
            }

        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error loading map file: " + e.getMessage()
                    , Constants.FATAL_FOR_LOGGING);
        }
    }

    /**
     * Method to load a single road from the specified file path
     *
     * @param roadElement XML Element containing road configuration
     * @return loaded Road object, or null if loading failed
     **/
    public static Road loadRoad(Element roadElement) {
        Element length = (Element) roadElement.getElementsByTagName(RoadLoadingConstants.ROAD_LENGTH_TAG).item(0);
        Element maxSpeed = (Element) roadElement.getElementsByTagName(RoadLoadingConstants.ROAD_MAX_SPEED_TAG).item(0);
        Element numberOfLanes = (Element) roadElement.getElementsByTagName(RoadLoadingConstants.NUMBER_OF_LANES_TAG).item(0);
        Element lightPlan = (Element) roadElement.getElementsByTagName(RoadLoadingConstants.LIGHT_PLAN_TAG).item(0);
        // get lane elements in array
        NodeList laneNodes = roadElement.getElementsByTagName(RoadLoadingConstants.ROAD_LANE_TAG);

        double lengthValue = Double.parseDouble(length.getTextContent());
        double maxSpeedValue = Double.parseDouble(maxSpeed.getTextContent());
        int numberOfLanesValue = Integer.parseInt(numberOfLanes.getTextContent());

        Road road = null;
        if (AppContext.CAR_FOLLOWING_MODEL.getType().equals(Constants.CELLULAR)) {
            road = new CellularRoad(lengthValue, numberOfLanesValue, maxSpeedValue, AppContext.CAR_FOLLOWING_MODEL.getCellSize());
        } else if (AppContext.CAR_FOLLOWING_MODEL.getType().equals(Constants.CONTINOUS)) {
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

        return road;
    }

}
