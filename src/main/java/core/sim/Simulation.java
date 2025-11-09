package core.sim;

import app.AppContext;
import core.model.Road;
import core.utils.ResultsRecorder;

/***************************
 * Class representing the simulation, holding roads and stepping through the simulation
 *
 * @author Michael Hladky
 * @version 1.0
 ***************************/
public class Simulation {

    /** array of roads in the simulation **/
    private final Road[] roads;

    /** current step count of the simulation **/
    private int stepCount = 0;

    /**
     * Constructor for Simulation
     *
     * @param roads array of roads in the simulation
     **/
    public Simulation(Road[] roads) {
        this.roads = roads;
    }

    /**
     * Steps through the simulation, updating each road and recording results if writingResults is enabled
     **/
    public void step() {
        if (AppContext.RUN_DETAILS.writingResults() && this.stepCount == 0) {
            ResultsRecorder.getResultsRecorder().startTimer();
        }

        for (int i = 0; i < roads.length; i++) {
            Road r = roads[i];
            if (r != null) {
                int carsPassed = r.updateRoad();

                // Record the number of cars that have passed on this road if results are to be written
                if (AppContext.RUN_DETAILS.writingResults()) {
                    ResultsRecorder.getResultsRecorder().recordCarsPassed(i, carsPassed);
                }

                this.stepCount++;
            }
        }

        // Stop the timer if writing results and the simulation duration has been reached
        if (AppContext.RUN_DETAILS.writingResults() && this.stepCount >= AppContext.RUN_DETAILS.duration - 1) {
            ResultsRecorder.getResultsRecorder().stopTimer();
        }

    }

    /**
     * Getter for roads in the simulation
     *
     * @return array of roads in the simulation
     **/
    public Road[] getRoads() {
        return roads;
    }

    /**
     * Getter for the current step count of the simulation
     *
     * @return current step count
     **/
    public int getStepCount() {
        return stepCount;
    }

    /**
     * Runs the simulation for a specified amount of time
     *
     * @param time duration to run the simulation
     **/
    public void runSimulation(double time) {
        int timeSteps = (int) Math.ceil(time);
        for (int i = 0; i < timeSteps; i++) {
            step();
        }

        ResultsRecorder.getResultsRecorder().stopTimer();
    }
}
