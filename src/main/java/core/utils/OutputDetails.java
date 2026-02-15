package core.utils;

import core.utils.constants.ConfigConstants;
import core.utils.constants.Constants;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/***********************************
 * Class to hold details about what simulation results to output, output file name, CSV separator.
 *
 * @author Michael Hladky
 * @version 1.0
 ***********************************/
public class OutputDetails {

    /** Output file name for writing simulation results **/
    public String outputFile;

    /** Output settings: [simulationDetails, simulationTime, carsPassed, carsOnRoad, whenWasRoadEmpty, collisions, roadDetails] **/
    public boolean[] output = new boolean[] { true, true, true, true, true, true, false, true };

    /** CSV separator character **/
    public String csvSeparator = Constants.DEFAULT_CSV_SEPARATOR;

    /** index of true/false in output array of simulation details **/
    final int SIMULATION_DETAILS_INDEX = Constants.SIMULATION_DETAILS_OUTPUT_INDEX;

    /** index of true/false in output array of simulation time **/
    final int SIMULATION_TIME_INDEX = Constants.SIMULATION_TIME_OUTPUT_INDEX;

    /** index of true/false in output array of cars passed **/
    final int CARS_PASSED_INDEX = Constants.CARS_PASSED_OUTPUT_INDEX;

    /** index of true/false in output array of cars on road **/
    final int CARS_ON_ROAD_INDEX = Constants.CARS_ON_ROAD_OUTPUT_INDEX;

    /** index of true/false in output array of when was road empty **/
    final int WHEN_WAS_ROAD_EMPTY_INDEX = Constants.WHEN_WAS_ROAD_EMPTY_OUTPUT_INDEX;

    /** index of true/false in output array of collision count **/
    final int COLLISION_COUNT_INDEX = Constants.COLLISION_COUNT_OUTPUT_INDEX;

    /** index of true/false in output array of road details **/
    final int ROAD_DETAILS_INDEX = Constants.ROAD_DETAILS_OUTPUT_INDEX;

    /** Names of the output details corresponding to their indices **/
    private final String[] DETAILS_NAME = {// Mapping detail names from XML to their corresponding indices, bullshit
            ConfigConstants.SIMULATION_DETAILS_TAG,
            ConfigConstants.SIMULATION_TIME_TAG,
            ConfigConstants.CARS_PASSED_TAG,
            ConfigConstants.CARS_ON_ROAD_TAG,
            ConfigConstants.WHEN_WAS_ROAD_EMPTY_TAG,
            ConfigConstants.COLLISION_COUNT_TAG,
            ConfigConstants.ROAD_DETAILS_TAG
    };

    /** Indices of the output details corresponding to their names **/
    private final int[] DETAIL_INDEXES = {
            SIMULATION_DETAILS_INDEX,
            SIMULATION_TIME_INDEX,
            CARS_PASSED_INDEX,
            CARS_ON_ROAD_INDEX,
            WHEN_WAS_ROAD_EMPTY_INDEX,
            COLLISION_COUNT_INDEX,
            ROAD_DETAILS_INDEX,
    };

    /**
     * changes the 'output' boolean array settings based on the provided XML element.
     * It assumes the XML element contains sub-elements corresponding to the output details.
     * * @param outputElement The XML Element containing output configuration.
     **/
    public void changeWhatToOutput(Element outputElement) {
        // Define indices for clarity and consistency

        // Iterate through all defined output details
        for (int i = 0; i < DETAILS_NAME.length; i++) {
            String name = DETAILS_NAME[i];
            int index = DETAIL_INDEXES[i];

            NodeList nodeList = outputElement.getElementsByTagName(name);

            if (nodeList.getLength() > 0) {
                Element detailElement = (Element) nodeList.item(0);

                // Get the text content (expected to be "true" or "false")
                String value = detailElement.getTextContent().trim();

                // Set the corresponding boolean value in the 'output' array
                if (value.equalsIgnoreCase("true")) {
                    this.output[index] = true;
                } else if (value.equalsIgnoreCase("false")) {
                    this.output[index] = false;
                }
            }
        }
    }

    /**
     * method to check if a specific part of the output is enabled based on its name.
     *
     * @param partName The name of the output part to check (e.g., "simulationDetails", "carsPassed").
     * @return true if the specified part is enabled for output, false otherwise.
     **/
    public boolean writePart(String partName) {
        for (int i = 0; i < DETAILS_NAME.length; i++) {
            if (DETAILS_NAME[i].equals(partName)) {
                return this.output[DETAIL_INDEXES[i]];
            }
        }
        return false; // Default to false if partName not found
    }

    /**
     * method to get the output setting for a specific index.
     *
     * @param index The index of the output setting to retrieve.
     * @return true if the output setting at the specified index is enabled, false otherwise.
     * @throws IndexOutOfBoundsException if the provided index is out of bounds for the output array.
     **/
    public boolean getOutputSetting(int index) {
        if (index >= 0 && index < output.length) {
            return output[index];
        }
        throw new IndexOutOfBoundsException("Invalid output setting index: " + index);
    }

    /**
     * overrides the default toString method to provide a string representation of the OutputDetails object, including
     * the output file name, output settings, and CSV separator.
     *
     * @return a string representation of the OutputDetails object
     **/
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OutputDetails{");
        sb.append("outputFile='").append(outputFile).append('\'');
        sb.append(", output=[");
        for (int i = 0; i < output.length; i++) {
            sb.append(output[i]);
            if (i < output.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("], csvSeparator='").append(csvSeparator).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
