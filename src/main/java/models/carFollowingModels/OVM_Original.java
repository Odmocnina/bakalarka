package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import models.ICarFollowingModel;

/********************************************
 * Optimal Velocity Model (OVM) car following model implementation (continuous)
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/

@CarFollowingModelId("ovm-original")
public class OVM_Original implements ICarFollowingModel {

    /**
     * function to get new speed based on OVM algorithm
     *
     * @param parameters HashMap of parameters needed for calculation
     **/
    @Override
    public double getNewSpeed(java.util.HashMap<String, Double> parameters) {
        double currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST);
        double distance;
        double xPosition = parameters.get(RequestConstants.X_POSITION_REQUEST);
        double xPositionStraightForward = parameters.get(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST);
        double maxSpeedRoad = parameters.get(RequestConstants.MAX_ROAD_SPEED_REQUEST);
        double maxSpeed = Math.min(parameters.get(RequestConstants.MAX_SPEED_REQUEST), maxSpeedRoad);
        double minGap = parameters.get(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        double lengthStraightForward = parameters.get(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST);
        if (xPositionStraightForward == Constants.NO_CAR_THERE) {
            distance = Double.MAX_VALUE;
        } else {
            distance = xPositionStraightForward - xPosition - lengthStraightForward;
        }
        double distanceDifferenceSensitivityParameter =
                parameters.get(RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST);

        double optimalVelocity = optimalVelocity(distance, maxSpeedRoad, minGap);
        double newSpeed = currentSpeed + distanceDifferenceSensitivityParameter * (optimalVelocity - currentSpeed);

        return Math.min(newSpeed, maxSpeed);
    }

    /**
     * function to calculate optimal velocity based on distance to the next car
     *
     * @param distance distance to the next car
     * @return optimal velocity as double
     **/
    protected double optimalVelocity(double distance, double maxSpeedRoad, double minGap) {
        //return (maxSpeedRoad / 2) * (Math.tanh(distance - minGap) + Math.tanh(minGap));
        return Math.tanh(distance - 2) - Math.tanh(2);
    }

    /**
     * function to request parameters needed for OVM model
     *
     * @return String of requested parameters
     **/
    @Override
    public String requestParameters() {
        String[] requests = {
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.MAX_ROAD_SPEED_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, requests);

    }

    /**
     * function to request parameters needed for generation of OVM model
     *
     * @return String of requested parameters for generation
     **/
    @Override
    public String getParametersForGeneration() {
        String[] requests = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.LENGTH_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
    }

    /**
     * function to get ID of the model
     *
     * @return ID as String
     **/
    @Override
    public String getID() {
        return "ovm-original";
    }

    /**
     * function to get type of the model
     *
     * @return type as String
     **/
    @Override
    public String getType() {
        return Constants.CONTINOUS;
    }

    /**
     * function to get name of the model
     *
     * @return name as String
     **/
    @Override
    public String getName() {
        return "Optimal Velocity Model (original)";
    }

    /**
     * function to get cell size in meters
     *
     * @return cell size as double
     **/
    @Override
    public double getCellSize() {
        return Constants.PARAMETER_UNDEFINED;
    }

}
