package core.engine;

import app.AppContext;
import core.model.Road;

public class Engine {

    private double timeStep; // in seconds

    public Engine(double timeStep) {
        this.timeStep = timeStep;
    }

    public double getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(double timeStep) {
        this.timeStep = timeStep;
    }

    public void update() {
        Road road = AppContext.ROAD;

        boolean isRunning = true;

        while (isRunning) {
            road.upadateRoad();
            try {
                Thread.sleep((long) (timeStep * 1000)); // convert to milliseconds
            } catch (InterruptedException e) {
                e.printStackTrace();
                isRunning = false;
            }
        }

        // update road everytimestep
    }

}
