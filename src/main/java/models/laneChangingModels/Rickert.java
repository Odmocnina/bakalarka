package models.laneChangingModels;

import core.model.Direction;
import core.utils.Constants;
import core.utils.RequestConstants;
import models.ILaneChangingModel;

import java.util.HashMap;
import static core.model.Direction.*;

/****************************************************
 * Rickert lane changing model class for deciding lane changes
 *
 * @author Michael Hladky
 * @version 1.0
 *****************************************************/
public class Rickert implements ILaneChangingModel {

    /**
     * gives the unique ID of the rickert model
     *
     * @return the unique ID of the rickert model
     **/
    public String getID() {
        return "rickert";
    }

    /*
     * gives the list of parameters that the rickert model needs to make a decision
     *
     * @return the list of parameters that the rickert model needs to make a decision
     **/
    public String requestParameters() {
        String[] requests = {
                RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST,
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST,
                RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST,
                RequestConstants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST,
                RequestConstants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
    }

    public String requestParameters(Direction direction) {
        if (direction == LEFT) {
            String[] requests = {
                    RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST,
                    RequestConstants.MAX_SPEED_REQUEST,
                    RequestConstants.CURRENT_SPEED_REQUEST,
                    RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST,
                    RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST
            };

            return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
        } else if (direction == RIGHT) {
            String[] requests = {
                    RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST,
                    RequestConstants.MAX_SPEED_REQUEST,
                    RequestConstants.CURRENT_SPEED_REQUEST,
                    RequestConstants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST,
                    RequestConstants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST
            };

            return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
        } else {
            return "";
        }
    }

    /**
     * gives the list of parameters that the rickert model needs for generation
     *
     * @return the list of parameters that the rickert model needs for generation
     **/
    public String getParametersForGeneration() {
        return RequestConstants.MAX_SPEED_REQUEST;
    }

    /**
     * decides whether to change lane or not based on the rickert model
     *
     * @param parameters the parameters needed to make a decision in hashmap form, where key is the parameter name and
     *                   value is the parameter value in double
     * @return the direction to change lane or go straight
     **/
    public Direction changeLaneIfDesired(HashMap<String, Double> parameters) {
        int distanceToNextCar = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST).intValue();
        int maxSpeed = parameters.get(RequestConstants.MAX_SPEED_REQUEST).intValue();
        int currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST).intValue() + 1;
        int forwardGap = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST).intValue();
        int previousGap = parameters.get(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST).intValue();

        if (makeDecision(distanceToNextCar, maxSpeed, currentSpeed, forwardGap, previousGap)) {
            return LEFT;
        }

        forwardGap = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST).intValue();
        previousGap = parameters.get(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST).intValue();

        if (makeDecision(distanceToNextCar, maxSpeed, currentSpeed, forwardGap, previousGap)) {
            return RIGHT;
        }


        return STRAIGHT;
    }

    public Direction changeLaneIfDesired(HashMap<String, Double> parameters, Direction direction) {
        int distanceToNextCar = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST).intValue();
        int maxSpeed = parameters.get(RequestConstants.MAX_SPEED_REQUEST).intValue();
        int currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST).intValue() + 1;
        int forwardGap = 0;
        int previousGap = 0;
        if (direction == LEFT) {
            forwardGap = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST).intValue();
            previousGap = parameters.get(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST).intValue();
        } else if (direction == RIGHT) {
            forwardGap = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST).intValue();
            previousGap = parameters.get(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST).intValue();
        } else {
            return STRAIGHT;
        }

        if (makeDecision(distanceToNextCar, maxSpeed, currentSpeed, forwardGap, previousGap)) {
            return direction;
        }

        return STRAIGHT;
    }

    /**
     * makes the decision to change lane or not based on the rickert model, returns true if the decision is to change
     * lane, false otherwise
     *
     * @param distanceToNextCar the distance to the next car in the current lane
     * @param maxSpeed the maximum speed of the car
     * @param currentSpeed the current speed of the car
     * @param newLaneForwardGap the gap to the next car in the new lane
     * @param newLanePreviousGap the gap to the previous car in the new lane
     * @return true if the decision is to change lane, false otherwise
     **/
    private boolean makeDecision(int distanceToNextCar, int maxSpeed, int currentSpeed, int newLaneForwardGap,
                                 int newLanePreviousGap) {

        if (newLaneForwardGap <= 0 || newLanePreviousGap <= 0) {
            return false;
        }
        int weight1;
        if (distanceToNextCar < currentSpeed && newLaneForwardGap > distanceToNextCar) {
            weight1 = 1;
        } else {
            weight1 = 0;
        }

        int weight2 = currentSpeed - newLaneForwardGap;
        int weight3 = maxSpeed - newLanePreviousGap;
        if (weight2 < 0) {
            weight2 = 0;
        }
        if (weight3 < 0) {
            weight3 = 0;
        }

        return weight1 > weight2 && weight1 > weight3;
    }

    /**
     * gives the name of the rickert model, used for display purposes
     *
     * @return the name of the rickert model
     **/
    @Override
    public String getName() {
        return "Rickert Model";
    }

}
