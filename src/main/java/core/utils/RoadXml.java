package core.utils;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.model.Parameter;
import core.utils.constants.Constants;
import core.utils.constants.DefaultValues;
import core.utils.constants.RoadLoadingConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/********************************************
 * Class responsible for saving exiting map (roads) to the xml file
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class RoadXml {

    /**
     * road parameters to XML, it creates a new XML document and adds the road parameters to it, then it writes the
     * document to the given file name, if there is an error while writing the file, it logs the error and returns
     * false, otherwise it returns true
     *
     * @param roadParameters roadParameters which were used for communication with user
     * @param numberOfRoads number of roads in the map, this is used for iterating through the road parameters, it
     *                      should be equal to the size of roadParameters list
     * @param mapFileName file name to which the XML document should be written
     * @return true if the XML document was successfully written to the file, false otherwise
     **/
    public static boolean writeMapToXml(ArrayList<RoadParameters> roadParameters, int numberOfRoads, String mapFileName) {

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(RoadLoadingConstants.MAP_TAG);
            doc.appendChild(rootElement);

            for (int i = 0; i < numberOfRoads; i++) {
                // getting values from fields
                int lanes = roadParameters.get(i).lanes;
                String lengthText = String.valueOf(roadParameters.get(i).length);
                String speedText = String.valueOf(roadParameters.get(i).maxSpeed);
                LinkedList<LightPlan> lp = roadParameters.get(i).lightPlan;
                LinkedList<CarGenerator> cg = roadParameters.get(i).carGenerators;

                processRoad(lanes, lengthText, speedText, lp, cg, doc, rootElement, i);
            }

            // writing the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // setting formatting options
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(mapFileName));

            // writing the content into xml file
            transformer.transform(source, result);
            return true;

        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error while writing map to XML: " + e.getMessage(),
                    Constants.ERROR_FOR_LOGGING);
            return false;
        }
    }

    /**
     * method that processes the road parameters and adds them to the XML document, it checks if the length and speed
     * are valid, if not it sets them to default values and logs the error, it also checks if the light plan and car
     * generator are defined for each lane, if not it sets them to default values and logs the error, finally it adds
     * the road to the XML document using the addRoadToXml method
     *
     * @param lanes number of lanes on the road
     * @param lengthText length of the road as a string, this is used for checking if the input is valid, if not it is
     *                   set to default value
     * @param speedText max speed on the road as a string, this is used for checking if the input is valid, if not it is
     *                  set to default value
     * @param lp light plan for the road, a linked list of LightPlan objects, one for each lane (first light plan in
     *           linked list
     * @param cg car generators for the road, a linked list of CarGenerator objects, one for each lane (first generator
     *           in linked
     * @param doc XML document to which the road should be added
     * @param rootElement root element of the XML document, this is used for adding the road element to it
     * @param i index of the road, this is used for logging purposes and for adding the index element to the XML
     *          document
     **/
    private static void processRoad(int lanes, String lengthText, String speedText, LinkedList<LightPlan> lp,
                                    LinkedList<CarGenerator> cg, Document doc, Element rootElement, int i) {

        double length;
        double speed;

        try {
            if (speedText.isEmpty()) {
                speed = DefaultValues.DEFAULT_ROAD_MAX_SPEED;
                MyLogger.logBeforeLoading("Wrong input speed in road: " + (i + 1) + "(empty), setting to" +
                        " default value.", Constants.ERROR_FOR_LOGGING);
            } else if (Double.parseDouble(speedText) <= 0) {
                speed = DefaultValues.DEFAULT_ROAD_MAX_SPEED;
                MyLogger.logBeforeLoading("Wrong input speed in road: " + (i + 1) + "(zero or smaller)," +
                        "setting to default value.", Constants.ERROR_FOR_LOGGING);
            } else {
              //  speed = Math.ceil(Double.parseDouble(speedText) / 3.6); // km/h to m/s
                speed = Math.ceil(Double.parseDouble(speedText));
            }
        } catch (NumberFormatException e) {
            speed = DefaultValues.DEFAULT_ROAD_MAX_SPEED;
            MyLogger.logBeforeLoading("Wrong input length in road: " + (i + 1) + "(not number), setting " +
                    "to default value.", Constants.ERROR_FOR_LOGGING);
        }

        try {
            if (lengthText.isEmpty()) {
                length = DefaultValues.DEFAULT_ROAD_LENGTH;
                MyLogger.logBeforeLoading("Wrong input length in road: " + (i + 1) + "(empty), setting to " +
                        "default value.", Constants.ERROR_FOR_LOGGING);
            } else if (Double.parseDouble(lengthText) <= 0) {
                length = DefaultValues.DEFAULT_ROAD_LENGTH;
                MyLogger.logBeforeLoading("Wrong input length in road: " + (i + 1) + "(zero or smaller)," +
                        " setting to default value.", Constants.ERROR_FOR_LOGGING);
            } else {
                length = Double.parseDouble(lengthText);
            }
        } catch (NumberFormatException e) {
            length = DefaultValues.DEFAULT_ROAD_LENGTH;
            MyLogger.logBeforeLoading("Wrong input length in road: " + (i + 1) + "(not number), setting to" +
                    " default value.", Constants.ERROR_FOR_LOGGING);
        }

        if (lp == null) {
            lp = new LinkedList<LightPlan>();
            for (int j = 0; j < lanes; j++) {
                lp.add(DefaultStuffMaker.createDefaultLightPlan());
            }
            MyLogger.logBeforeLoading("No light plan defined for road: " + (i + 1) + ", setting to default " +
                    "value.", Constants.ERROR_FOR_LOGGING);
        }

        for (int lane = 0; lane < lp.size(); lane++) {
            if (lp.get(lane) == null) {
                lp.add(lane, DefaultStuffMaker.createDefaultLightPlan());
                MyLogger.logBeforeLoading("No light plan defined for lane: " + (lane + 1) + " in road: " + (i + 1) +
                        ", setting to default value.", Constants.ERROR_FOR_LOGGING);
            } else if (!lp.get(lane).isLegitimate()) {
                lp.add(lane, DefaultStuffMaker.createDefaultLightPlan());
                MyLogger.logBeforeLoading("Wrong light plan defined for road: " + (i + 1) + ", setting to default " +
                        "value.", Constants.ERROR_FOR_LOGGING);
            }
        }

        if (cg == null) {
            cg = new LinkedList<CarGenerator>();
            for (int j = 0; j < lanes; j++) {
                cg.add(DefaultStuffMaker.createDefaultGenerator());
            }
            MyLogger.logBeforeLoading("No car generator defined for road: " + (i + 1) + ", setting to default " +
                    "value.", Constants.ERROR_FOR_LOGGING);
        }

        for (int lane = 0; lane < cg.size(); lane++) {
            if (cg.get(lane) == null) {
                //cg[lane] = new CarGenerator(DefaultValues.DEFAULT_FLOW_RATE);
                cg.add(lane, DefaultStuffMaker.createDefaultGenerator());
                MyLogger.logBeforeLoading("No car generator defined for lane: " + (lane + 1) + " in road: " + (i + 1) +
                        ", setting to default value.", Constants.ERROR_FOR_LOGGING);
            } /*else if (!cg[lane].isLegitimate()) {
                cg[lane] = new CarGenerator(DefaultValues.DEFAULT_FLOW_RATE);
                MyLogger.logBeforeLoading("Wrong car generator defined for lane: " + (lane + 1) + " in road: " + (i + 1) +
                        ", setting to default value.", Constants.ERROR_FOR_LOGGING);
            }*/
        }

        addRoadToXml(lanes, length, speed, lp, cg, doc, rootElement, i);
    }

    /**
     * writes road parameters to XML document, it creates a new road element and adds the road parameters to it, then it
     * adds the road element to the root element of the XML document, it also adds the light plan and car generator
     * parameters
     *
     * @param numberOfLanes number of lanes on the road, this is used for adding the number of lanes element to the XML
     *                      document and for
     * @param length length of the road, this is used for adding the length element to the XML document
     * @param speed max speed on the road, this is used for adding the max speed element to the XML document
     * @param lp light plan for the road, a linked list of LightPlan objects, one for each lane
     *           (in linked list)
     * @param cg car generators for the road, a linked list of CarGenerator objects, one for each lane (in linked list)
     * @param doc XML document to which the road should be added
     * @param rootElement root element of the XML document, this is used for adding the road element to it
     * @param index index of the road, this is used for adding the index element to the XML document
     **/
    private static void addRoadToXml(int numberOfLanes, double length, double speed, LinkedList<LightPlan> lp,
                                     LinkedList<CarGenerator> cg, Document doc, Element rootElement, int index) {
        Element roadElement = doc.createElement(RoadLoadingConstants.ROAD_TAG);
        rootElement.appendChild(roadElement);

        Element indexElement = doc.createElement(RoadLoadingConstants.ROAD_INDEX_TAG);
        indexElement.appendChild(doc.createTextNode(String.valueOf(index)));
        roadElement.appendChild(indexElement);

        Element lengthElement = doc.createElement(RoadLoadingConstants.ROAD_LENGTH_TAG);
        lengthElement.appendChild(doc.createTextNode(String.valueOf(length)));
        roadElement.appendChild(lengthElement);

        Element speedElement = doc.createElement(RoadLoadingConstants.ROAD_MAX_SPEED_TAG);
        speedElement.appendChild(doc.createTextNode(String.valueOf(speed)));
        roadElement.appendChild(speedElement);

        Element numberOfLanesElement = doc.createElement(RoadLoadingConstants.NUMBER_OF_LANES_TAG);
        numberOfLanesElement.appendChild(doc.createTextNode(String.valueOf(numberOfLanes)));
        roadElement.appendChild(numberOfLanesElement);

        for (int lane = 0; lane < numberOfLanes; lane++) {
            Element laneElement = doc.createElement(RoadLoadingConstants.ROAD_LANE_TAG);
            // add generator and light plan elements to lane element
            Element generatorElement = doc.createElement(RoadLoadingConstants.GENERATOR_TAG);
            laneElement.appendChild(generatorElement);
            Element lightPlanElement = doc.createElement(RoadLoadingConstants.LIGHT_PLAN_TAG);
            laneElement.appendChild(lightPlanElement);

            // write light plan to XML
            Element lightPlanCycleDurationElement = doc.createElement(RoadLoadingConstants.CYCLE_DURATION_TAG);
            lightPlanElement.appendChild(lightPlanCycleDurationElement);
            lightPlanCycleDurationElement.appendChild(doc.createTextNode(String.valueOf(lp.get(lane).getCycleTime())));
            Element lightPlanTimeOfSwitchElement = doc.createElement(RoadLoadingConstants.TIME_OF_SWITCH_TAG);
            lightPlanElement.appendChild(lightPlanTimeOfSwitchElement);
            lightPlanTimeOfSwitchElement.appendChild(doc.createTextNode(String.valueOf(lp.get(lane).getTimeOfSwitch())));
            Element lightPlanStartWithGreenElement = doc.createElement(RoadLoadingConstants.START_WITH_GREEN_TAG);
            lightPlanElement.appendChild(lightPlanStartWithGreenElement);
            lightPlanStartWithGreenElement.appendChild(doc.createTextNode(String.valueOf(lp.get(lane).isBeginsOnGreen())));

            //write generator to XML
            //flow rate
            CarGenerator generator = cg.get(lane);
            Element flowRateElement = doc.createElement(RoadLoadingConstants.FLOW_RATE_TAG);
            generatorElement.appendChild(flowRateElement);
            flowRateElement.appendChild(doc.createTextNode(String.valueOf(generator.getFlowRate())));
            //queue
            Element queueElement = doc.createElement(RoadLoadingConstants.QUEUE_TAG);
            generatorElement.appendChild(queueElement);
            Element useQueueElement = doc.createElement(RoadLoadingConstants.USE_TAG);
            queueElement.appendChild(useQueueElement);
            useQueueElement.appendChild(doc.createTextNode(String.valueOf(generator.generatingToQueue())));
            Element minSizeElement = doc.createElement(RoadLoadingConstants.MIN_VALUE_TAG);
            queueElement.appendChild(minSizeElement);
            minSizeElement.appendChild(doc.createTextNode(String.valueOf(generator.getMinQueueSize())));
            Element maxSizeElement = doc.createElement(RoadLoadingConstants.MAX_VALUE_TAG);
            queueElement.appendChild(maxSizeElement);
            maxSizeElement.appendChild(doc.createTextNode(String.valueOf(generator.getMaxQueueSize())));
            // car parameters
            Element paramElement = doc.createElement(RoadLoadingConstants.CAR_PARAMS_TAG);
            generatorElement.appendChild(paramElement);
            HashMap<String, Parameter> carParams = generator.getAllComParameters();
            for (String paramKey : carParams.keySet()) {
                Parameter parameter = carParams.get(paramKey);
                Element parameterElement = doc.createElement(paramKey);
                paramElement.appendChild(parameterElement);
                Element nameElement = doc.createElement(RoadLoadingConstants.NAME_TAG);
                nameElement.appendChild(doc.createTextNode(parameter.name));
                parameterElement.appendChild(nameElement);
                Element minElement = doc.createElement(RoadLoadingConstants.MIN_VALUE_TAG);
                minElement.appendChild(doc.createTextNode(String.valueOf(parameter.minValue)));
                parameterElement.appendChild(minElement);
                Element maxElement = doc.createElement(RoadLoadingConstants.MAX_VALUE_TAG);
                maxElement.appendChild(doc.createTextNode(String.valueOf(parameter.maxValue)));
                parameterElement.appendChild(maxElement);
            }

            roadElement.appendChild(laneElement);
        }
    }

    /**
     * method that saves the current map to the XML file, it gets the current road parameters from the existing roads in
     * the simulation using the existingRoadsToRoadParameters method, then it calls the writeMapToXml method to write
     * the road parameters to the XML file, if there is an error while writing the file, it logs the error and returns
     * false, otherwise it sets the mapChanged flag to false and logs the success and returns true
     *
     * @return true if the current map was successfully saved to the XML file, false otherwise
     */
    public static boolean saveCurrentMap() {
        ArrayList<RoadParameters> currentRoadParameters =
                RoadParameters.existingRoadsToRoadParameters(AppContext.SIMULATION.getRoads());
        boolean success = writeMapToXml(currentRoadParameters, currentRoadParameters.size(), AppContext.RUN_DETAILS.mapFile);
        if (!success) {
            MyLogger.logBeforeLoading("Error while saving current map to XML file: " + AppContext.RUN_DETAILS.mapFile,
                    Constants.ERROR_FOR_LOGGING);
            return false;
        } else {
            AppContext.RUN_DETAILS.mapChanged = false;
            MyLogger.logBeforeLoading("Current map saved to XML file: " + AppContext.RUN_DETAILS.mapFile,
                    Constants.INFO_FOR_LOGGING);
            return true;
        }
    }

    /**
     * method that saves the current map to a new XML file, it sets the map file name in the application context to
     * the given file name, then it calls the saveCurrentMap method to save the current map to the new XML file, if
     * there is an error while saving the file, it logs the error and returns false, otherwise it logs the success
     * and returns true, used for save as function
     *
     * @param fileName new file name to which the current map should be saved
     * @return true if the current map was successfully saved to the new XML file, false otherwise
     **/
    public static boolean saveAs(String fileName) {
        AppContext.RUN_DETAILS.mapFile = fileName;
        return saveCurrentMap();
    }

}
