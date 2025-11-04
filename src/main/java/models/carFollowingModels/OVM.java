package models.carFollowingModels;

import core.utils.Constants;
import models.ICarFollowingModel;

public class OVM implements ICarFollowingModel {

    @Override
    public double getNewSpeed(java.util.HashMap<String, Double> parameters) {
        double currentSpeed = parameters.get(Constants.CURRENT_SPEED_REQUEST);
        double distance = parameters.get(Constants.DISTANCE_TO_NEXT_CAR_REQUEST);
        double speedDiffrenceSensitivityParameter =
                parameters.get(Constants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST);

        return currentSpeed + speedDiffrenceSensitivityParameter *
               (optimalVelocity(distance) - currentSpeed);
    }

    private double optimalVelocity(double distance) {
        return 16.8 * (Math.tanh(0.086 * (distance - 25) + 0.913));
    }

    @Override
    public String requestParameters() {
        return Constants.CURRENT_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.DISTANCE_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST;

    }

    @Override
    public String getParametersForGeneration() {
        return Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.LENGTH_REQUEST;
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
