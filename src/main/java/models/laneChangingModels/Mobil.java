package models.laneChangingModels;

import core.model.Direction;
import core.utils.RequestConstants;
import models.ILaneChangingModel;

import java.util.HashMap;

/********************************************
 * MOBIL lane changing model class for deciding lane changes
 *
 * @author
 * @version 1.0
 *********************************************/
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
        return RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.POLITENESS_FACTOR_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.NOW_ACCELERATION_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.NOW_ACCELERATION_LEFT_BACKWARD_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.NOW_ACCELERATION_RIGHT_BACKWARD_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.THEORETICAL_ACCELERATION_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.THEORETICAL_ACCELERATION_LEFT_BACKWARD_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.THEORETICAL_ACCELERATION_RIGHT_BACKWARD_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST;
    }

    @Override
    public String requestParameters(Direction direction) {
        if (direction == Direction.LEFT) {
            return RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                    RequestConstants.POLITENESS_FACTOR_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                    RequestConstants.NOW_ACCELERATION_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                    RequestConstants.NOW_ACCELERATION_LEFT_BACKWARD_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                    RequestConstants.NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                    RequestConstants.THEORETICAL_ACCELERATION_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                    RequestConstants.THEORETICAL_ACCELERATION_LEFT_BACKWARD_REQUEST + RequestConstants.REQUEST_SEPARATOR
                    + RequestConstants.THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST;

        } else if (direction == Direction.RIGHT) {
            return RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                    RequestConstants.POLITENESS_FACTOR_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                    RequestConstants.NOW_ACCELERATION_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                    RequestConstants.NOW_ACCELERATION_RIGHT_BACKWARD_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                    RequestConstants.NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                    RequestConstants.THEORETICAL_ACCELERATION_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                    RequestConstants.THEORETICAL_ACCELERATION_RIGHT_BACKWARD_REQUEST + RequestConstants.REQUEST_SEPARATOR
                    + RequestConstants.THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST;
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
