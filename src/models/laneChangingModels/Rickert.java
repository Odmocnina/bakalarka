package models.laneChangingModels;

import core.model.Direction;
import core.utils.Constants;
import models.ILaneChangingModel;

import java.util.HashMap;

import static core.model.Direction.*;

public class Rickert implements ILaneChangingModel {

    public String getID() {
        return "rickert";
    }

    public String requestParameters() {
        return Constants.DISTANCE_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.CURRENT_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST;
    }

    public Direction changeLaneIfDesired(HashMap<String, Double> parameters) {
        int distanceToNextCar = parameters.get(Constants.DISTANCE_TO_NEXT_CAR_REQUEST).intValue();
        int maxSpeed = parameters.get(Constants.MAX_SPEED_REQUEST).intValue();
        int currentSpeed = parameters.get(Constants.CURRENT_SPEED_REQUEST).intValue();
        int forwardGap = parameters.get(Constants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST).intValue();
        int previousGap = parameters.get(Constants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST).intValue();

        if (makeDecision(distanceToNextCar, maxSpeed, currentSpeed, forwardGap, previousGap)) {
            return LEFT;
        }

        forwardGap = parameters.get(Constants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST).intValue();
        previousGap = parameters.get(Constants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST).intValue();

        if (makeDecision(distanceToNextCar, maxSpeed, currentSpeed, forwardGap, previousGap)) {
            return RIGHT;
        }


        return STRAIGHT;
    }

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

}
