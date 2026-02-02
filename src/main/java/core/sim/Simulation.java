package core.sim;

import app.AppContext;
import core.model.Road;
import core.utils.constants.Constants;
import core.utils.MyLogger;
import core.utils.ResultsRecorder;

/***************************
 * Class representing the simulation, holding roads and stepping through the simulation
 *
 * @author Michael Hladky
 * @version 1.0
 ***************************/
public class Simulation {

    /** array of roads in the simulation **/
    private Road[] roads;

    /** current step count of the simulation **/
    private int stepCount = 0;

    /** flag indicating if the simulation is running **/
    private boolean running = false;

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
            }
        }

        this.stepCount++;
        this.updateLights();

        // Stop the timer if writing results and the simulation duration has been reached
        boolean shouldRun = this.stepCount >= (AppContext.RUN_DETAILS.duration - 1) && this.running;
        if (AppContext.RUN_DETAILS.writingResults() && shouldRun) {
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
        this.running = true;

        while (this.stepCount < timeSteps && this.running) {
            step();
            if (areAllRoadsAndQueuesEmpty(this.roads)) {
                MyLogger.log("All car queues and roads are empty, ending simulation early at step " +
                        this.stepCount + ".", Constants.INFO_FOR_LOGGING);
                this.running = false;
            }
        }

        ResultsRecorder.getResultsRecorder().stopTimer();
    }

    /**
     * Checks if all roads and their queues are empty
     *
     * @param roads array of roads to check
     * @return true if all roads and queues are empty, false otherwise
     **/
    public boolean areAllRoadsAndQueuesEmpty(Road[] roads) {
        for (Road road : roads) {
            if (road.getNumberOfCarsOnRoad() > 0 || !road.areAllQueuesEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all queues on the given roads are empty
     *
     * @param roads array of roads to check
     * @return true if all queues are empty, false otherwise
     **/
    public boolean areAllQueuesEmpty(Road[] roads) {
        for (Road road : roads) {
            if (!road.areAllQueuesEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all roads are empty
     *
     * @param roads array of roads to check
     * @return true if all roads are empty, false otherwise
     **/
    public boolean areAllRoadsEmpty(Road[] roads) {
        for (Road road : roads) {
            if (road.getNumberOfCarsOnRoad() > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the traffic lights on all roads based on the current step count
     **/
    private void updateLights() {
        for (Road road : roads) {
            road.updateLights(this.stepCount);
        }
    }

    /**
     * Gets the flow rate of cars entering the simulation from the first road's car generator
     *
     * @return flow rate in cars per second
     **/
    public double getFlowRate() {
        return roads[0].getCarGenerator().getLambdaPerSec();
    }

    /**
     * Setter for roads in the simulation
     *
     * @param roads array of roads to set
     **/
    public void setRoads(Road[] roads) {
        this.roads = roads;
    }

    /**
     * Checks if the current step count is even, can be used by some models for deciding actions, like lane changes
     *
     * @return true if step count is even, false otherwise
     **/
    public boolean isStepCountEven() {
        return this.stepCount % 2 == 0;
    }

    /**
     * Resets the simulation with new roads and resets the step count and running state, used when new roads are loaded,
     * or current roads are modified.
     *
     * @param roads array of new roads for the simulation
     **/
    public void resetSimulationWithNewRoads(Road[] roads) {
        this.stepCount = 0;
        this.running = false;
        this.roads = roads;
        ResultsRecorder.getResultsRecorder().resetCarNumbers();
    }
}
