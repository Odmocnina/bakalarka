package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import models.ICarFollowingModel;
import models.ModelId;

import java.util.HashMap;

/********************************************
 * Helly car following model implementation (continuous)
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
@ModelId("helly")
public class Helly implements ICarFollowingModel {

    /**
     * function to get new speed based on Helly algorithm
     *
     * @param parameters HashMap of parameters needed for calculation
     **/
    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        double currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST);
        double xPosition = parameters.get(RequestConstants.X_POSITION_REQUEST);
        double xPositionNextCar = parameters.get(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST);
        double distanceToNextCar;
        if (xPositionNextCar != Constants.NO_CAR_THERE) {
            double lengthNextCar = parameters.get(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST);
            distanceToNextCar = xPositionNextCar - xPosition - lengthNextCar;
        } else {
            distanceToNextCar = Double.MAX_VALUE;
        }

        double currentSpeedStraightForward = parameters.get(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST);
        double speedDifferenceToTheNextCar;
        if (currentSpeedStraightForward != Constants.NO_CAR_THERE) {
            speedDifferenceToTheNextCar = currentSpeedStraightForward - currentSpeed;
        } else {
            speedDifferenceToTheNextCar = 0.0;
        }

        double speedDifferenceSensitivityParameter = parameters.get(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST);
        double distanceDifferenceSensitivityParameter = parameters.get(RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST);
        double minimumGapToNextCar = parameters.get(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        double maxSpeed = parameters.get(RequestConstants.MAX_SPEED_REQUEST);

        double acceleration = speedDifferenceSensitivityParameter * (speedDifferenceToTheNextCar) +
                              distanceDifferenceSensitivityParameter * (distanceToNextCar - minimumGapToNextCar);

        double newSpeed = currentSpeed + acceleration;
        if (newSpeed < 0) {
            newSpeed = 0;
        }

        if (newSpeed > maxSpeed) {
            newSpeed = maxSpeed;
        }

        return newSpeed;
    }

    /**
     * function to get list of required parameters for the model when calculating new speed
     *
     * @return String of required parameters separated by Constants.REQUEST_SEPARATOR
     **/
    @Override
    public String requestParameters() {
        String[] requests = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
    }

    /**
     * function to get list of required parameters for the model when generating cars
     *
     * @return String of required parameters separated by Constants.REQUEST_SEPARATOR
     **/
    @Override
    public String getParametersForGeneration() {
        String[] requests = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.LENGTH_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
    }

    /**
     * getter for ID of the model
     *
     * @return ID as String
     **/
    @Override
    public String getID() {
        return "helly";
    }

    /**
     * getter for type of the model (continuous)
     *
     * @return type as String
     **/
    @Override
    public String getType() {
        return Constants.CONTINOUS;
    }

    /**
     * getter for name of the model
     *
     * @return name as String
     **/
    @Override
    public String getName() {
        return "Helly Car Following Model";
    }

    /**
     * getter for cell size
     *
     * @return cell size as double
     **/
    @Override
    public double getCellSize() {
        return Constants.PARAMETER_UNDEFINED;
    }


}
