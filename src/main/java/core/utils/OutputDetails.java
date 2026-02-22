package core.utils;

import core.utils.constants.ConfigConstants;
import core.utils.constants.Constants;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.HashMap;
import java.util.Map;

/************************************
 * Class to hold details about the output configuration for the simulation, such as output file name, which parts of the
 * simulation results to write, and the CSV separator character.
 *
 * @author Michael Hladky
 * @version 1.0
 ************************************/
public class OutputDetails {

    /** Output file name for writing simulation results **/
    public String outputFile;

    /** Map holding output settings using ConfigConstants tags as keys **/
    public Map<String, Boolean> whatToOutput = new HashMap<>();

    /** CSV separator character **/
    public String csvSeparator = Constants.DEFAULT_CSV_SEPARATOR;

    /**
     * Constructor to initialize the OutputDetails with default settings.
     **/
    public OutputDetails() {
        // Initialize the whatToOutput map with default values based on ConfigConstants tags
        whatToOutput.put(ConfigConstants.SIMULATION_DETAILS_TAG, true);
        whatToOutput.put(ConfigConstants.SIMULATION_TIME_TAG, true);
        whatToOutput.put(ConfigConstants.CARS_PASSED_TAG, true);
        whatToOutput.put(ConfigConstants.CARS_ON_ROAD_TAG, true);
        whatToOutput.put(ConfigConstants.WHEN_WAS_ROAD_EMPTY_TAG, true);
        whatToOutput.put(ConfigConstants.LANE_CHANGES_COUNT_TAG, true);
        whatToOutput.put(ConfigConstants.AVERAGE_LANE_QUEUE_LENGTH_TAG, true);
        whatToOutput.put(ConfigConstants.DETAILED_LANE_QUEUE_LENGTH_TAG, false);
        whatToOutput.put(ConfigConstants.DETAILED_LIGHT_PLANS_TAG, false);
        whatToOutput.put(ConfigConstants.EXPORT_DETAILED_TO_SEPARATE_FILES_TAG, false);
        whatToOutput.put(ConfigConstants.COLLISION_COUNT_TAG, true);
        whatToOutput.put(ConfigConstants.ROAD_DETAILS_TAG, true);
    }

    /**
     * Changes the 'whatToOutput' map settings based on the provided XML element.
     *
     * @param outputElement The XML Element containing output configuration.
     **/
    public void changeWhatToOutput(Element outputElement) {
        // go through each child node of the outputElement and update the whatToOutput map based on the tag name and value
        NodeList childNodes = outputElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i) instanceof Element childElement) {
                String tagName = childElement.getTagName();
                boolean value = Boolean.parseBoolean(childElement.getTextContent().trim());
                whatToOutput.put(tagName, value);
            }
        }
    }

    /**
     * Checks if a specific part of the output is enabled based on its name.
     *
     * @param partName The name of the output part to check (e.g., from ConfigConstants).
     * @return true if enabled, false otherwise.
     **/
    public boolean writePart(String partName) {
        // getOrDefault returns false if the partName is not found in the map, which is a safe default
        return whatToOutput.getOrDefault(partName, false);
    }

    /**
     * Sets the output setting for a specific part of the output.
     *
     * @param partName The name of the output part to set (e.g., from ConfigConstants).
     * @param value    The boolean value to set for the specified part (true to enable, false to disable).
     **/
    public void setPart(String partName, boolean value) {
        whatToOutput.put(partName, value);
    }

    /**
     * gets the map of output settings, it returns the map that holds the output settings for different parts of the
     * simulation results, where the keys are the part names (e.g., from ConfigConstants) and the values are booleans
     * indicating whether that part should be included in the output or not
     *
     * @return the map of output settings for different parts of the simulation results
     **/
    public Map<String, Boolean> getWhatToOutput() {
        return whatToOutput;
    }

    /**
     * Overrides the default toString method to provide a string representation.
     **/
    @Override
    public String toString() {
        return "OutputDetails{" +
                "outputFile='" + outputFile + '\'' +
                ", whatToOutput=" + whatToOutput +
                ", csvSeparator='" + csvSeparator + '\'' +
                '}';
    }
}
