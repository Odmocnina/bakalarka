package models.carFollowingModels;

import core.utils.Constants;
import models.ICarFollowingModel;

import java.util.HashMap;

public class NagelSchreckenberg implements ICarFollowingModel {

    private final double CELL_SIZE = 7.5; // in meters
    private String type;
    private double slowDownChance = 0.3; // probability of random slowing down

    public NagelSchreckenberg() {
        this.type = Constants.CELLULAR;
    }

    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        int currentSpeed = parameters.get(Constants.CURRENT_SPEED_REQUEST).intValue();
        int maxSpeed = parameters.get(Constants.MAX_SPEED_REQUEST).intValue();
        double distance = parameters.get(Constants.DISTANCE_TO_NEXT_CAR_REQUEST).intValue();
        int distanceInCells = (int) Math.round(distance); // convert distance to number of cells
        // Step 1: Acceleration
        if (currentSpeed < maxSpeed) {
            currentSpeed++;
        }
        // Step 2: Slowing down
        if (distanceInCells <= currentSpeed) {
            currentSpeed = distanceInCells - 1;
        }
        // Step 3: Randomization
        if (currentSpeed > 0 && Math.random() < this.slowDownChance) { // 30% chance to slow down
            currentSpeed--;
        }
        return Math.max(0, currentSpeed);
    }

    public double getCellSize() {
        return this.CELL_SIZE;
    }

    @Override
    public String getID() {
        return "nagelschreckenberg";
    }

    @Override
    public String getType() {
        return this.type;
    }

    public String requestParameters() {
        return Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.CURRENT_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DISTANCE_TO_NEXT_CAR_REQUEST;
    }

    public String getParametersForGeneration() {
        return Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR
                + Constants.LENGTH_REQUEST + Constants.REQUEST_SEPARATOR;
    }

    @Override
    public String getName() {
        return "Nagel-Schreckenberg Model";
    }
}
