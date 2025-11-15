package models.carFollowingModels;

import core.utils.Constants;
import core.utils.RequestConstants;
import models.ICarFollowingModel;

import java.util.HashMap;

public class Gipps implements ICarFollowingModel {


    @Override
    public String getID() {
        return "gipps";
    }

    @Override
    public String requestParameters() {
        String[] params = {
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.MAX_ACCELERATION_REQUEST,
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.MAX_ROAD_SPEED_REQUEST,
                RequestConstants.TIME_STEP_REQUEST,
                RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.DECELERATION_COMFORT_REQUEST,
                RequestConstants.DECELERATION_COMFORT_REQUEST_STRAIGHT_FORWARD,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, params);
    }

    @Override
    public String getName() {
        return "Gipps Car-Following Model";
    }

    public double getCellSize() {
        return Constants.PARAMETER_UNDEFINED;
    }

    @Override
    public String getParametersForGeneration() {
        String[] params = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.MAX_ACCELERATION_REQUEST,
                RequestConstants.DECELERATION_COMFORT_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, params);
    }

    @Override
    public String getType() {
        return Constants.CONTINOUS;
    }

    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        double currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST);
        double maxAcceleration = parameters.get(RequestConstants.MAX_ACCELERATION_REQUEST);
        double maxRoadSpeed = parameters.get(RequestConstants.MAX_ROAD_SPEED_REQUEST);
        double maxSpeed = parameters.get(RequestConstants.MAX_SPEED_REQUEST);
        double timeStep = parameters.get(RequestConstants.TIME_STEP_REQUEST);
        double leadingSpeed = parameters.get(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST);
        if (leadingSpeed == Constants.NO_CAR_THERE) {
            leadingSpeed = Double.MAX_VALUE;
        }
        double minGap = parameters.get(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        double maxDeceleration = parameters.get(RequestConstants.DECELERATION_COMFORT_REQUEST);
        double maxDecelerationBack = parameters.get(RequestConstants.DECELERATION_COMFORT_REQUEST_STRAIGHT_FORWARD);
        double xPosition = parameters.get(RequestConstants.X_POSITION_REQUEST);
        double leadingXPosition = parameters.get(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST);
        if (leadingXPosition == Constants.NO_CAR_THERE) {
            leadingXPosition = Double.MAX_VALUE;
        }

        double desiredSpeed = Math.min(maxRoadSpeed, maxSpeed);

        double freeFlowSpeed = getFreeFlowSpeed(currentSpeed, maxAcceleration, desiredSpeed, timeStep);
        double safeSpeed = safeSpeed(currentSpeed, maxDeceleration, leadingSpeed, minGap,
                maxDecelerationBack, timeStep, xPosition, leadingXPosition);

        return Math.min(freeFlowSpeed, safeSpeed);
    }

    private double getFreeFlowSpeed(double currentSpeed, double maxAcceleration, double desiredSpeed, double timeStep) {
        double newSpeed = currentSpeed + 2.5 * maxAcceleration * timeStep * (1 - currentSpeed / desiredSpeed) *
                Math.sqrt(0.025 + currentSpeed / desiredSpeed);
        return newSpeed;
    }

    private double safeSpeed(double currentSpeed, double maxDeceleration, double leadingSpeed, double minGap,
                             double maxDecelerationBack, double timeStep, double xPosition, double leadingXPosition) {
        double firstPart = maxDeceleration * timeStep;
        double positionGap = leadingXPosition - xPosition - minGap;
        double firstPartUnderSqrt = maxDeceleration * maxDeceleration * timeStep * timeStep;
        double endingPartBraces = currentSpeed * timeStep - (leadingSpeed * leadingSpeed)/maxDecelerationBack;
        double secondTerm = Math.sqrt(firstPartUnderSqrt - maxDeceleration * (2 * positionGap - endingPartBraces));
        double newSpeed = firstPart + secondTerm;

        return newSpeed;
    }

}
