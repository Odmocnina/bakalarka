package models.carFollowingModels;

import core.utils.Constants;
import models.ICarFollowingModel;

import java.util.HashMap;

public class Helly implements ICarFollowingModel {

    public double getNewSpeed(HashMap<String, Double> parameters) {
        double currentSpeed = parameters.get(Constants.CURRENT_SPEED_REQUEST);
        double xPosition = parameters.get(Constants.X_POSITION_REQUEST);
        double xPositionNextCar = parameters.get(Constants.X_POSITION_STRAIGHT_FORWARD_REQUEST);
        double distanceToNextCar;
        if (xPositionNextCar != Constants.NO_CAR_THERE) {
            double lengthNextCar = parameters.get(Constants.LENGTH_STRAIGHT_FORWARD_REQUEST);
            distanceToNextCar = xPositionNextCar - xPosition - lengthNextCar;
        } else {
            distanceToNextCar = Double.MAX_VALUE;
        }

        double currentSpeedStraightForward = parameters.get(Constants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST);
        double speedDifferenceToTheNextCar;
        if (currentSpeedStraightForward != Constants.NO_CAR_THERE) {
            speedDifferenceToTheNextCar = currentSpeedStraightForward - currentSpeed;
        } else {
            speedDifferenceToTheNextCar = 0.0;
        }

        double speedDifferenceSensitivityParameter = parameters.get(Constants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST);
        double distanceDifferenceSensitivityParameter = parameters.get(Constants.DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST);
        double minimumGapToNextCar = parameters.get(Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        double maxSpeed = parameters.get(Constants.MAX_SPEED_REQUEST);

        double acceleration = speedDifferenceSensitivityParameter * (speedDifferenceToTheNextCar) +
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
               Constants.X_POSITION_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.X_POSITION_STRAIGHT_FORWARD_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.LENGTH_STRAIGHT_FORWARD_REQUEST + Constants.REQUEST_SEPARATOR +
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
