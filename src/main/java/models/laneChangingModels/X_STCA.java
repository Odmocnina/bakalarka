package models.laneChangingModels;

import app.AppContext;
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
public class X_STCA implements ILaneChangingModel {

    /**
     * gives the unique ID of the x-stca model
     *
     * @return the unique ID of the x-stca model
     **/
    public String getID() {
        return "x-stca";
    }

    /*
     * gives the list of parameters that the x-stca model needs to make a decision
     *
     * @return the list of parameters that the x-stca model needs to make a decision
     **/
    public String requestParameters() {
        String[] requests = {
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.X_POSITION_LEFT_FORWARD_REQUEST,
                RequestConstants.LENGTH_LEFT_FORWARD_REQUEST,
                RequestConstants.X_POSITION_LEFT_BACKWARD_REQUEST,
                RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST,
                RequestConstants.LENGTH_RIGHT_FORWARD_REQUEST,
                RequestConstants.X_POSITION_RIGHT_BACKWARD_REQUEST,
                RequestConstants.MAX_ROAD_SPEED_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
    }

    public String requestParameters(Direction direction) {
        if (direction == LEFT) {
            String[] requests = {
                    RequestConstants.X_POSITION_REQUEST,
                    RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                    RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST,
                    RequestConstants.MAX_SPEED_REQUEST,
                    RequestConstants.CURRENT_SPEED_REQUEST,
                    RequestConstants.X_POSITION_LEFT_FORWARD_REQUEST,
                    RequestConstants.LENGTH_LEFT_FORWARD_REQUEST,
                    RequestConstants.X_POSITION_LEFT_BACKWARD_REQUEST,
                    RequestConstants.MAX_ROAD_SPEED_REQUEST
            };

            return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
        } else if (direction == RIGHT) {
            String[] requests = {
                    RequestConstants.X_POSITION_REQUEST,
                    RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                    RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST,
                    RequestConstants.MAX_SPEED_REQUEST,
                    RequestConstants.CURRENT_SPEED_REQUEST,
                    RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST,
                    RequestConstants.LENGTH_RIGHT_FORWARD_REQUEST,
                    RequestConstants.X_POSITION_RIGHT_BACKWARD_REQUEST,
                    RequestConstants.MAX_ROAD_SPEED_REQUEST
            };

            return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
        } else {
            return "";
        }
    }

    /**
     * gives the list of parameters that the x-stca model needs for generation
     *
     * @return the list of parameters that the x-stca model needs for generation
     **/
    public String getParametersForGeneration() {
        return RequestConstants.MAX_SPEED_REQUEST;
    }

    /**
     * decides whether to change lane or not based on the x-stca model
     *
     * @param parameters the parameters needed to make a decision in hashmap form, where key is the parameter name and
     *                   value is the parameter value in double
     * @return the direction to change lane or go straight
     **/
    public Direction changeLaneIfDesired(HashMap<String, Double> parameters) {
        if (AppContext.SIMULATION.getStepCount() > 2) {
            int i = 0;
        }
        int xPosition = parameters.get(RequestConstants.X_POSITION_REQUEST).intValue();
        int xPositionStraightForward = parameters.get(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST).intValue();
        int lengthStraightForward = parameters.get(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST).intValue();
        double distanceToNextCar;
        if (xPositionStraightForward == Constants.NO_CAR_THERE) {
            distanceToNextCar = Double.MAX_VALUE; // no car ahead
        } else {
            distanceToNextCar = (xPositionStraightForward - xPosition - lengthStraightForward); // distance in cells
        }
        int maxSpeed = parameters.get(RequestConstants.MAX_SPEED_REQUEST).intValue();
        int maxSpeedRoad = parameters.get(RequestConstants.MAX_ROAD_SPEED_REQUEST).intValue();
        int currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST).intValue() + 1;
        int xPositionInDifferentLaneForward = parameters.get(RequestConstants.X_POSITION_LEFT_FORWARD_REQUEST).intValue();
        int lengthInDifferentLaneForward;
        int forwardGap;
        int xPositionInDifferentLaneBackward;
        int previousGap;
        if (xPositionInDifferentLaneForward != Constants.NO_LANE_THERE) {
            lengthInDifferentLaneForward = parameters.get(RequestConstants.LENGTH_LEFT_FORWARD_REQUEST).intValue();
            if (xPositionInDifferentLaneForward != Constants.NO_CAR_THERE) {
                forwardGap = xPositionInDifferentLaneForward - xPosition - lengthInDifferentLaneForward;
            } else {
                forwardGap = Integer.MAX_VALUE;
            }

            xPositionInDifferentLaneBackward = parameters.get(RequestConstants.X_POSITION_LEFT_BACKWARD_REQUEST)
                    .intValue();
            if (xPositionInDifferentLaneBackward != Constants.NO_CAR_THERE) {
                previousGap = xPosition - xPositionInDifferentLaneBackward;
            } else {
                previousGap = Integer.MAX_VALUE;
            }

            if (makeDecision((int) distanceToNextCar, maxSpeed, currentSpeed, forwardGap, previousGap, maxSpeedRoad)) {
                return LEFT;
            }
        }

        xPositionInDifferentLaneForward = parameters.get(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST).intValue();
        if (xPositionInDifferentLaneForward != Constants.NO_LANE_THERE) {
            lengthInDifferentLaneForward = parameters.get(RequestConstants.LENGTH_RIGHT_FORWARD_REQUEST).intValue();
            if (xPositionInDifferentLaneForward != Constants.NO_CAR_THERE) {
                forwardGap = xPositionInDifferentLaneForward - xPosition - lengthInDifferentLaneForward;
            } else {
                forwardGap = Integer.MAX_VALUE;
            }

            xPositionInDifferentLaneBackward = parameters.get(RequestConstants.X_POSITION_RIGHT_BACKWARD_REQUEST)
                    .intValue();
            if (xPositionInDifferentLaneBackward != Constants.NO_CAR_THERE) {
                previousGap = xPosition - xPositionInDifferentLaneBackward;
            } else {
                previousGap = Integer.MAX_VALUE;
            }

            if (makeDecision((int) distanceToNextCar, maxSpeed, currentSpeed, forwardGap, previousGap, maxSpeedRoad)) {
                return RIGHT;
            }
        }


        return STRAIGHT;
    }

    public Direction changeLaneIfDesired(HashMap<String, Double> parameters, Direction direction) {
        int distanceToNextCar = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST).intValue();
        int maxSpeed = parameters.get(RequestConstants.MAX_SPEED_REQUEST).intValue();
        int currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST).intValue() + 1;
        int maxSpeedRoad = parameters.get(RequestConstants.MAX_ROAD_SPEED_REQUEST).intValue();
        int forwardGap;
        int previousGap;
        if (direction == LEFT) {
            forwardGap = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST).intValue();
            previousGap = parameters.get(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST).intValue();
        } else if (direction == RIGHT) {
            forwardGap = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST).intValue();
            previousGap = parameters.get(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST).intValue();
        } else {
            return STRAIGHT;
        }

        if (makeDecision(distanceToNextCar, maxSpeed, currentSpeed, forwardGap, previousGap, maxSpeedRoad)) {
            return direction;
        }

        return STRAIGHT;
    }

    /**
     * makes the decision to change lane or not based on the x-stca model, returns true if the decision is to change
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
                                 int newLanePreviousGap, int maxRoadSpeed) {

        if (newLaneForwardGap <= 0 || newLanePreviousGap <= 0) {
            return false;
        }

        int theoreticalSpeed = Math.min(currentSpeed, maxSpeed);

        if (distanceToNextCar > theoreticalSpeed) {
            return false;
        }

        int neededGapForward = 1 + Math.min(theoreticalSpeed, maxRoadSpeed);

        if (newLaneForwardGap <= distanceToNextCar) {
            return false;
        }

        int neededGapBack = 1 + maxSpeed - theoreticalSpeed;

        if (newLanePreviousGap <= neededGapBack) {
            return false;
        }

        return true;

    }

    /**
     * gives the name of the x-stca model, used for display purposes
     *
     * @return the name of the x-stca model
     **/
    @Override
    public String getName() {
        return "X-STCA (Symmetric Two-lane Cellular Automata)";
    }

}
