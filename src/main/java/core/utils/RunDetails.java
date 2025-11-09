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
    public String outputFile;

    /** Whether to draw cells in the GUI if cellular model **/
    public boolean drawCells;

    /** Logging settings: [general, info, warn, error, fatal, debug] **/
    public boolean[] log = new boolean[] { true, true, true, true, true, true };

    /** Time between steps for GUI updates in milliseconds **/
    public int timeBetweenSteps;

    /**
     * Checks if the simulation results should be written to an output file (null or empty output file means results
     * should not be recorded).
     *
     * @return true if an output file is specified, false otherwise.
     **/
    public boolean writingResults() {
        return this.outputFile != null && !this.outputFile.isEmpty();
    }
}
