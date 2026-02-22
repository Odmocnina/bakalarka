package core.utils;

/************************************
 * Class to hold details about the current simulation run such as duration, time step, whether to show GUI, output
 * file name, whether to draw cells, logging settings and time between steps for GUI updates.
 *
 * @author Michael Hladky
 * @version 1.0
 ************************************/
public class RunDetails {

    /** Duration of the simulation in seconds **/
    public int duration;

    /** Time step of the simulation in seconds **/
    public double timeStep;

    /** Whether to show the GUI during the simulation **/
    public boolean showGui;

    /** Output file name for writing simulation results **/
    public OutputDetails outputDetails = null;

    /** Whether to draw cells in the GUI if cellular model **/
    public boolean drawCells;

    /** Whether lane changing is enabled **/
    public boolean laneChange;

    /** Logging settings: [general, info, warn, error, fatal, debug] **/
    public boolean[] log = new boolean[] { true, true, true, true, true, true };

    /** Time between steps for GUI updates in milliseconds **/
    public int timeBetweenSteps;

    /** Whether to prevent collisions during the simulation **/
    public boolean preventCollisions = true;

    /** Map file to be used for the simulation, null if no map file is specified **/
    public String mapFile = null;

    /** Flag to indicate if the map has changed used for controlling if map should be saved when turning off the app **/
    public boolean mapChanged = false;

    /** Enable debug mode, writing id of cars, controlling if cars aren't duplicated or in collision **/
    public boolean debug = false;

    /** Whether the map has been loaded successfully **/
    public boolean mapLoaded = false;

    /** Random seed for the simulation, can be set for reproducibility **/
    public long seed;

    /**
     * Checks if the simulation results should be written to an output file (null or empty output file/details
     * means results should not be recorded).
     *
     * @return true if an output file is specified, false otherwise.
     **/
    public boolean writingResults() {
        return this.outputDetails != null && outputDetails.outputFile != null && !outputDetails.outputFile.isEmpty();
    }

    /**
     * Checks if a specific part of the output is enabled based on its name.
     *
     * @param tag The name of the output part to check (e.g., from ConfigConstants).
     * @return true if enabled, false otherwise.
     **/
    public boolean getOutputDetail(String tag) {
        if (outputDetails == null || !outputDetails.whatToOutput.containsKey(tag)) {
            return false; // Return false if outputDetails is null or tag is not found
        }
        return outputDetails.whatToOutput.get(tag);
    }

    /**
     * Returns a string representation of the RunDetails object, including all its fields and their values.
     *
     * @return a string representation of the RunDetails object.
     **/
    @Override
    public String toString() {
        return "RunDetails{" +
                "duration=" + duration +
                ", timeStep=" + timeStep +
                ", showGui=" + showGui +
                ", output='" + outputDetails + '\'' +
                ", drawCells=" + drawCells +
                ", timeBetweenSteps=" + timeBetweenSteps +
                ", random seed=" + seed +
                '}';
    }

    /**
     * sets a new map file for the simulation, it updates the map file in the simulation context with the given value
     * and resets the mapChanged flag to false
     *
     * @param mapFile new map file to be set in the simulation context
     **/
    public void setNewMapFile(String mapFile) {
        this.mapChanged = false;
        this.mapFile = mapFile;
    }
}
