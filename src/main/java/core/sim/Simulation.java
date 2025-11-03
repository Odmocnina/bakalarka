package core.sim;

import app.AppContext;
import core.model.Road;
import core.utils.ResultsRecorder;

public class Simulation {

    private final Road[] roads;

    int stepCount = 0;

    public Simulation(Road[] roads) {
        this.roads = roads;
    }

    public void step() {
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
    }

    public Road[] getRoads() {
        return roads;
    }

    public void runSimulation(double time, double timeStep) {
        int timeSteps = (int) Math.ceil(time / timeStep);
        for (int i = 0; i < timeSteps; i++) {
            step();
        }
    }
}
