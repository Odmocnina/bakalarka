package core.utils;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.model.Parameter;
import core.utils.constants.Constants;
import core.utils.constants.DefaultValues;
import core.utils.constants.RoadLoadingConstants;

import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
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

public class RoadXml {

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

            // wriring the content into xml file
            transformer.transform(source, result);
            return true;

        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error while writing map to XML: " + e.getMessage(),
                    Constants.ERROR_FOR_LOGGING);
            return false;
        }
    }

    private static void processRoad(int lanes, String lengthText, String speedText, LinkedList<LightPlan> lp,
                                    LinkedList<CarGenerator> cg, Document doc, Element rootElement, int i) {

        double length = 0;
        double speed = 0;

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
            //lp = new LightPlan[lanes];
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
            //cg = new CarGenerator[lanes];
            cg = new LinkedList<CarGenerator>();
            for (int j = 0; j < lanes; j++) {
                //cg[j] = new CarGenerator(DefaultValues.DEFAULT_FLOW_RATE);
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
            CarGenerator generator = cg.get(lane);
            Element flowRateElement = doc.createElement(RoadLoadingConstants.FLOW_RATE_TAG);
            generatorElement.appendChild(flowRateElement);
            flowRateElement.appendChild(doc.createTextNode(String.valueOf(generator.getFlowRate())));
            Element paramElement = doc.createElement(RoadLoadingConstants.CAR_PARAMS_TAG);
            generatorElement.appendChild(paramElement);
            HashMap<String, Parameter> carParams = generator.getAllParameters();
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

    public static boolean saveCurrentMap() {
        ArrayList<RoadParameters> currentRoadParameters =
                RoadParameters.existingRoadsToRoadParameters(AppContext.SIMULATION.getRoads());
        boolean success = writeMapToXml(currentRoadParameters, currentRoadParameters.size(), AppContext.RUN_DETAILS.mapFile);
        if (!success) {
            MyLogger.logBeforeLoading("Error while saving current map to XML file: " + AppContext.RUN_DETAILS.mapFile,
                    Constants.ERROR_FOR_LOGGING);
            return false;
        } else {
            MyLogger.logBeforeLoading("Current map saved to XML file: " + AppContext.RUN_DETAILS.mapFile,
                    Constants.INFO_FOR_LOGGING);
            return true;
        }
    }

    public static boolean saveAs(String fileName) {
        AppContext.RUN_DETAILS.mapFile = fileName;
        return saveCurrentMap();
    }

}
