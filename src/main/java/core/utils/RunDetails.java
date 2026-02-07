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

    public String mapFile = null;
    public boolean mapChanged = false;

    /** Enable debug mode, writing id of cars, controlling if cars aren't duplicated or in collision **/
    public boolean debug = false;

    /**
     * Checks if the simulation results should be written to an output file (null or empty output file/details
     * means results should not be recorded).
     *
     * @return true if an output file is specified, false otherwise.
     **/
    public boolean writingResults() {
        return this.outputDetails != null && outputDetails.outputFile != null && !outputDetails.outputFile.isEmpty();
    }

    @Override
    public String toString() {
        return "RunDetails{" +
                "duration=" + duration +
                ", timeStep=" + timeStep +
                ", showGui=" + showGui +
                ", output='" + outputDetails + '\'' +
                ", drawCells=" + drawCells +
                ", timeBetweenSteps=" + timeBetweenSteps +
                '}';
    }

    public void setNewMapFile(String mapFile) {
        this.mapChanged = false;
        this.mapFile = mapFile;
    }
}
