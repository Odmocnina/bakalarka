package models.laneChangingModels;

import core.model.Direction;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import models.ILaneChangingModel;
import models.ModelId;

import java.util.HashMap;

/********************************************
 * MOBIL lane changing model class for deciding lane changes
 *
 * @author
 * @version 1.0
 *********************************************/
@ModelId("mobil")
public class Mobil implements ILaneChangingModel {

    /**
     * gives the unique ID of the MOBIL model
     *
     * @return the unique ID of the MOBIL model
     **/
    @Override
    public String getID() {
        return "mobil";
    }

    /*
     * gives the list of parameters that the MOBIL model needs to make a decision
     *
     * @return the list of parameters that the MOBIL model needs to make a decision
     **/
    @Override
    public String requestParameters() {
        String[] requests = {
                RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST,
                RequestConstants.POLITENESS_FACTOR_REQUEST,
                RequestConstants.NOW_ACCELERATION_REQUEST,
                RequestConstants.NOW_ACCELERATION_LEFT_BACKWARD_REQUEST,
                RequestConstants.NOW_ACCELERATION_RIGHT_BACKWARD_REQUEST,
                RequestConstants.NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST,
                RequestConstants.THEORETICAL_ACCELERATION_REQUEST,
                RequestConstants.THEORETICAL_ACCELERATION_LEFT_BACKWARD_REQUEST,
                RequestConstants.THEORETICAL_ACCELERATION_RIGHT_BACKWARD_REQUEST,
                RequestConstants.THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST,
                RequestConstants.DECELERATION_COMFORT_LEFT_BACKWARD_REQUEST,
                RequestConstants.DECELERATION_COMFORT_RIGHT_BACKWARD_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
    }

    @Override
    public String requestParameters(Direction direction) {
        String[] requests;
        if (direction == Direction.LEFT) {
            requests = new String[]{
                    RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST,
                    RequestConstants.POLITENESS_FACTOR_REQUEST,
                    RequestConstants.NOW_ACCELERATION_REQUEST,
                    RequestConstants.NOW_ACCELERATION_LEFT_BACKWARD_REQUEST,
                    RequestConstants.NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST,
                    RequestConstants.THEORETICAL_ACCELERATION_REQUEST,
                    RequestConstants.THEORETICAL_ACCELERATION_LEFT_BACKWARD_REQUEST,
                    RequestConstants.THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST,
                    RequestConstants.DECELERATION_COMFORT_LEFT_BACKWARD_REQUEST
            };

            return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
        } else if (direction == Direction.RIGHT) {
            requests = new String[]{
                    RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST,
                    RequestConstants.POLITENESS_FACTOR_REQUEST,
                    RequestConstants.NOW_ACCELERATION_REQUEST,
                    RequestConstants.NOW_ACCELERATION_RIGHT_BACKWARD_REQUEST,
                    RequestConstants.NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST,
                    RequestConstants.THEORETICAL_ACCELERATION_REQUEST,
                    RequestConstants.THEORETICAL_ACCELERATION_RIGHT_BACKWARD_REQUEST,
                    RequestConstants.THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST,
                    RequestConstants.DECELERATION_COMFORT_RIGHT_BACKWARD_REQUEST
            };

            return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
        }

        return "";
    }

    /**
     * function to request parameters needed for generation of MOBIL model
     *
     * @return String of requested parameters for generation
     **/
    @Override
    public String getParametersForGeneration() {
        return RequestConstants.MAX_SPEED_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.DECELERATION_COMFORT_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.POLITENESS_FACTOR_REQUEST;
    }

    /**
     * decides whether to change lane or not based on the MOBIL model
     *
     * @param parameters the parameters needed to make a decision in hashmap form, where key is the parameter name and
     *                   value is the parameter value in double
     * @return the direction to change lane or go straight
     **/
    @Override
    public Direction changeLaneIfDesired(HashMap<String, Double> parameters) {
        double edgeValueForLaneChange = parameters.get(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST);
        double politenessFactor = parameters.get(RequestConstants.POLITENESS_FACTOR_REQUEST);
        double nowAcceleration = parameters.get(RequestConstants.NOW_ACCELERATION_REQUEST);
        double nowAccelerationLeftBackward = parameters.get(RequestConstants.NOW_ACCELERATION_LEFT_BACKWARD_REQUEST);
        double nowAccelerationRightBackward = parameters.get(RequestConstants.NOW_ACCELERATION_RIGHT_BACKWARD_REQUEST);
        double nowAccelerationStraightBackward = parameters.get(RequestConstants.NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST);
        double theoreticalAcceleration = parameters.get(RequestConstants.THEORETICAL_ACCELERATION_REQUEST);
        double theoreticalAccelerationLeftBackward = parameters.get(RequestConstants.THEORETICAL_ACCELERATION_LEFT_BACKWARD_REQUEST);
        double theoreticalAccelerationRightBackward = parameters.get(RequestConstants.THEORETICAL_ACCELERATION_RIGHT_BACKWARD_REQUEST);
        double theoreticalAccelerationStraightBackward = parameters.get(RequestConstants.THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST);

        double accelerationGain = theoreticalAcceleration - nowAcceleration;
        boolean changeToRight;
        return Direction.STRAIGHT;
    }

    public Direction changeLaneIfDesired(HashMap<String, Double> parameters, Direction direction) {
        double edgeValueForLaneChange = parameters.get(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST);
        double politenessFactor = parameters.get(RequestConstants.POLITENESS_FACTOR_REQUEST);
        double nowAcceleration = parameters.get(RequestConstants.NOW_ACCELERATION_REQUEST);
        double theoreticalAcceleration = parameters.get(RequestConstants.THEORETICAL_ACCELERATION_REQUEST);
        double nowAccelerationStraightBackward = parameters.get(RequestConstants.
                NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST);
        double theoreticalAccelerationStraightBackward = parameters.get(RequestConstants.
                THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST);

        double decelerarion;
        if (direction == Direction.LEFT) {
            decelerarion = parameters.get(RequestConstants.DECELERATION_COMFORT_LEFT_BACKWARD_REQUEST);
        } else if (direction == Direction.RIGHT) {
            decelerarion = parameters.get(RequestConstants.DECELERATION_COMFORT_RIGHT_BACKWARD_REQUEST);
        } else {
            return Direction.STRAIGHT;
        }
        double deacelarationForSaftey = Math.abs(theoreticalAcceleration);
        if (decelerarion == Constants.NO_CAR_THERE) {
            decelerarion = Double.MAX_VALUE;
        }
        if (deacelarationForSaftey > decelerarion) { // safety check
            return Direction.STRAIGHT;
        }

        //return direction;

        double nowAccelerationNeighborBackward = 0;
        double theoreticalAccelerationNeighborBackward = 0;
        if (direction == Direction.LEFT) {
            nowAccelerationNeighborBackward = parameters.get(RequestConstants.NOW_ACCELERATION_LEFT_BACKWARD_REQUEST);
            theoreticalAccelerationNeighborBackward = parameters.get(RequestConstants.
                    THEORETICAL_ACCELERATION_LEFT_BACKWARD_REQUEST);
        } else if (direction == Direction.RIGHT) {
            nowAccelerationNeighborBackward = parameters.get(RequestConstants.NOW_ACCELERATION_RIGHT_BACKWARD_REQUEST);
            theoreticalAccelerationNeighborBackward = parameters.get(RequestConstants.
                    THEORETICAL_ACCELERATION_RIGHT_BACKWARD_REQUEST);
        }

        double accelerationGain = theoreticalAcceleration - nowAcceleration;
        double accelerationDifferenceDifferentCars = nowAccelerationStraightBackward + nowAccelerationNeighborBackward
                - theoreticalAccelerationStraightBackward - theoreticalAccelerationNeighborBackward;

        boolean change = accelerationGain > politenessFactor * accelerationDifferenceDifferentCars
                + edgeValueForLaneChange;

        if (change) {
            return direction;
        }

        return Direction.STRAIGHT;
    }

    /**
     * function to get the name of the model
     *
     * @return name of the model as String
     **/
    @Override
    public String getName() {
        return "MOBIL";
    }

}
