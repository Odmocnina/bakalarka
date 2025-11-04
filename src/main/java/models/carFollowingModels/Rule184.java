package models.carFollowingModels;

import core.utils.Constants;
import models.ICarFollowingModel;

public class Rule184 implements ICarFollowingModel {

    private String type;

    private final double CELL_SIZE = 5.0; // in meters

    public Rule184() {
        this.type = Constants.CELLULAR;
    }

    @Override
    public double getNewSpeed(java.util.HashMap<String, Double> parameters) {
        double distanceDouble = parameters.get(Constants.DISTANCE_TO_NEXT_CAR_REQUEST);
        int distance = (int) distanceDouble; // convert distance to number of cells

        if (distance > 1) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String getID() {
        return "rule184";
    }

    @Override
    public String getType() {
        return this.type;
    }

    public double getCellSize() {
        return CELL_SIZE; // in meters
    }

    public String requestParameters() {
        return Constants.DISTANCE_TO_NEXT_CAR_REQUEST;
    }

    public String getParametersForGeneration() {
        return Constants.LENGTH_REQUEST;
    }

    @Override
    public String getName() {
        return "Rule 184";
    }

}
