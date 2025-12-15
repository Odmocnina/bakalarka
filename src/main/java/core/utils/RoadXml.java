package core.utils;

import core.model.LightPlan;
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

public class RoadXml {

    public static void writeMapToXml(ArrayList<RoadParameters> roadParameters, int numberOfRoads, String mapFileName) {

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
                LightPlan[] lp = roadParameters.get(i).lightPlan;

                processRoad(lanes, lengthText, speedText, lp, doc, rootElement, i);
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

        } catch (Exception e) {
            MyLogger.logBeforeLoading("Error while writing map to XML: " + e.getMessage(),
                    Constants.ERROR_FOR_LOGGING);
        }
    }

    private static void processRoad(int lanes, String lengthText, String speedText, LightPlan[] lp, Document doc,
                                    Element rootElement, int i) {

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
                speed = Math.ceil(Double.parseDouble(speedText) / 3.6); // km/h to m/s
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
            lp = new LightPlan[lanes];
            for (int j = 0; j < lanes; j++) {
                lp[j] = new LightPlan(DefaultValues.DEFAULT_LIGHT_PLAN_CYCLE_DURATION,
                        DefaultValues.DEFAULT_LIGHT_PLAN_GREEN_DURATION,
                        DefaultValues.DEFAULT_LIGHT_PLAN_START_WITH_GREEN);
            }
            MyLogger.logBeforeLoading("No light plan defined for road: " + (i + 1) + ", setting to default " +
                    "value.", Constants.ERROR_FOR_LOGGING);
        }

        for (int lane = 0; lane < lp.length; lane++) {
            if (lp[lane] == null) {
                lp[lane] = new LightPlan(DefaultValues.DEFAULT_LIGHT_PLAN_CYCLE_DURATION,
                        DefaultValues.DEFAULT_LIGHT_PLAN_GREEN_DURATION,
                        DefaultValues.DEFAULT_LIGHT_PLAN_START_WITH_GREEN);
                MyLogger.logBeforeLoading("No light plan defined for lane: " + (lane + 1) + " in road: " + (i + 1) +
                        ", setting to default value.", Constants.ERROR_FOR_LOGGING);
            } else if (!lp[lane].isLegitimate()) {
                lp[lane] = new LightPlan(DefaultValues.DEFAULT_LIGHT_PLAN_CYCLE_DURATION,
                        DefaultValues.DEFAULT_LIGHT_PLAN_GREEN_DURATION,
                        DefaultValues.DEFAULT_LIGHT_PLAN_START_WITH_GREEN);
                MyLogger.logBeforeLoading("Wrong light plan defined for road: " + (i + 1) + ", setting to default " +
                        "value.", Constants.ERROR_FOR_LOGGING);
            }
        }

        addRoadToXml(lanes, length, speed, lp, doc, rootElement, i);
    }

    private static void addRoadToXml(int numberOfLanes, double length, double speed, LightPlan[] lp, Document doc,
                                     Element rootElement, int index) {
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
            lightPlanCycleDurationElement.appendChild(doc.createTextNode(String.valueOf(lp[lane].getCycleTime())));
            Element lightPlanTimeOfSwitchElement = doc.createElement(RoadLoadingConstants.TIME_OF_SWITCH_TAG);
            lightPlanElement.appendChild(lightPlanTimeOfSwitchElement);
            lightPlanTimeOfSwitchElement.appendChild(doc.createTextNode(String.valueOf(lp[lane].getTimeOfSwitch())));
            Element lightPlanStartWithGreenElement = doc.createElement(RoadLoadingConstants.START_WITH_GREEN_TAG);
            lightPlanElement.appendChild(lightPlanStartWithGreenElement);
            lightPlanStartWithGreenElement.appendChild(doc.createTextNode(String.valueOf(lp[lane].isBeginsOnGreen())));

            roadElement.appendChild(laneElement);
        }
        
    }

}
