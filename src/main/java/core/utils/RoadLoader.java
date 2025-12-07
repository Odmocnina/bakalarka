package core.utils;

import app.AppContext;
import core.model.Road;
import core.model.cellular.CellularRoad;
import core.model.continous.ContinuosRoad;
import core.utils.constants.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class RoadLoader {

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
            Element laneElement = (Element) road.getElementsByTagName("lane");
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

}
