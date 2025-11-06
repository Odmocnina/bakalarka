package models.carFollowingModels;

import core.utils.Constants;
import models.ICarFollowingModel;

import java.util.HashMap;

public class Helly implements ICarFollowingModel {

    public double getNewSpeed(HashMap<String, Double> parameters) {
        double currentSpeed = parameters.get(Constants.CURRENT_SPEED_REQUEST);
        double distanceToNextCar = parameters.get(Constants.DISTANCE_TO_NEXT_CAR_REQUEST);
        double currentSpeedStraightForward = parameters.get(Constants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST);
        double speedDifferenceSensitivityParameter = parameters.get(Constants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST);
        double distanceDifferenceSensitivityParameter = parameters.get(Constants.DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST);
        double minimumGapToNextCar = parameters.get(Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        double maxSpeed = parameters.get(Constants.MAX_SPEED_REQUEST);

        double acceleration = speedDifferenceSensitivityParameter * (currentSpeedStraightForward - currentSpeed) +
                              distanceDifferenceSensitivityParameter * (distanceToNextCar + minimumGapToNextCar);

        double newSpeed = currentSpeed + acceleration;
        if (newSpeed < 0) {
            newSpeed = 0;
        }

        if (newSpeed > maxSpeed) {
            newSpeed = maxSpeed;
        }

        return newSpeed;
    }

    public String requestParameters() {
        return Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.CURRENT_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.DISTANCE_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST;
    }

    public String getParametersForGeneration() {
        return Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.LENGTH_REQUEST;
    }

    @Override
    public String getID() {
        return "helly";
    }


    @Override
    public String getType() {
        return Constants.CONTINOUS;
    }

    public String getName() {
        return "Helly Car Following Model";
    }

    @Override
    public double getCellSize() {
        return Constants.PARAMETER_UNDEFINED;
    }


}
