package models.carFollowingModels;

import core.utils.Constants;
import models.ICarFollowingModel;

public class FVDM implements ICarFollowingModel {

    @Override
    public double getNewSpeed(java.util.HashMap<String, Double> parameters) {
        double currentSpeed = parameters.get(Constants.CURRENT_SPEED_REQUEST);
        double maxSpeed = parameters.get(Constants.MAX_SPEED_REQUEST);
        double distance = parameters.get(Constants.DISTANCE_TO_NEXT_CAR_REQUEST);
        return 0;
    }

    @Override
    public String requestParameters() {
        return Constants.CURRENT_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.DISTANCE_TO_NEXT_CAR_REQUEST;
    }

    @Override
    public String getParametersForGeneration() {
        return "";
    }

    @Override
    public String getID() {
        return "ovm";
    }

    @Override
    public String getType() {
        return Constants.CONTINOUS;
    }

    @Override
    public String getName() {
        return "Optimal Velocity Model";
    }

    @Override
    public double getCellSize() {
        return 0;
    }

}
