package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import models.ICarFollowingModel;
import models.ModelId;

import java.util.HashMap;

/********************************************
 * Gipps car following model implementation (continuous), it has its own parameters for generation and its own requested
 * parameters for calculating new speed, annotated with @ModelId("gipps") for identification during reflexive loading
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
@ModelId("gipps")
public class Gipps implements ICarFollowingModel {

    /**
     * return id of the gipps car following model, used for reflexive loading
     *
     * @return String id of the gipps car following model
     **/
    @Override
    public String getID() {
        return "gipps";
    }

    /**
     * function to request parameters needed for Gipps model to the road
     *
     * @return String of requested parameters
     **/
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

    /**
     * function to get name of the Gipps car following model
     *
     * @return String name of the Gipps car following model
     **/
    @Override
    public String getName() {
        return "Gipps Car-Following Model";
    }

    /**
     * function to get cell size for Gipps model, since it is a continuous model, it returns
     * Constants.PARAMETER_UNDEFINED
     *
     * @return double cell size for Gipps model
     **/
    public double getCellSize() {
        return Constants.PARAMETER_UNDEFINED;
    }

    /**
     * function to request parameters needed for generation of Gipps model to the road
     *
     * @return String of requested parameters for generation
     **/
    @Override
    public String getParametersForGeneration() {
        String[] params = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.MAX_ACCELERATION_REQUEST,
                RequestConstants.DECELERATION_COMFORT_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.LENGTH_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, params);
    }

    /**
     * function to get type of the Gipps car following model (continuous)
     *
     * @return String type of the Gipps car following model
     **/
    @Override
    public String getType() {
        return Constants.CONTINUOUS;
    }

    /**
     * function to calculate new speed of the car based on the Gipps model, it uses the requested parameters to
     * calculate the new speed, it calculates free flow speed and safe speed and returns the minimum of the two
     *
     * @param parameters HashMap of parameters needed for calculating new speed, keys are defined in RequestConstants
     * @return double new speed calculated based on the Gipps model
     **/
    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        double currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST);
        double maxAcceleration = parameters.get(RequestConstants.MAX_ACCELERATION_REQUEST);
        double maxRoadSpeed = parameters.get(RequestConstants.MAX_ROAD_SPEED_REQUEST);
        double maxSpeed = parameters.get(RequestConstants.MAX_SPEED_REQUEST);
        double timeStep = parameters.get(RequestConstants.TIME_STEP_REQUEST);
        double minGap = parameters.get(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        double maxDeceleration = -parameters.get(RequestConstants.DECELERATION_COMFORT_REQUEST);
        //double maxDecelerationFront = -parameters.get(RequestConstants.DECELERATION_COMFORT_REQUEST_STRAIGHT_FORWARD);
        double maxDecelerationFront = Math.min(-3.0, (maxDeceleration - 3.0) / 2); //estimation of breaking capability
                                                                                   // of car in front, at least 3 m/s^2
        double xPosition = parameters.get(RequestConstants.X_POSITION_REQUEST);
        double leadingXPosition = parameters.get(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST);
        if (leadingXPosition == Constants.NO_CAR_THERE) {
            return this.getFreeFlowSpeed(currentSpeed, maxAcceleration,
                    Math.min(maxRoadSpeed, maxSpeed), timeStep);
        }
        double leadingSpeed = parameters.get(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST);
        double desiredSpeed = Math.min(maxRoadSpeed, maxSpeed);

        double freeFlowSpeed = getFreeFlowSpeed(currentSpeed, maxAcceleration, desiredSpeed, timeStep);
        double safeSpeed = safeSpeed(currentSpeed, maxDeceleration, leadingSpeed, minGap,
                maxDecelerationFront, timeStep, xPosition, leadingXPosition);
        if (safeSpeed < 0 || Double.isNaN(safeSpeed)) {
            safeSpeed = 0;
        }

        return Math.min(freeFlowSpeed, safeSpeed);
    }

    /**
     * free flow part off the gipps model, it calculates the free flow speed based on the current speed, maximum
     * acceleration, desired speed and time step
     *
     * @param currentSpeed current speed of the car
     * @param maxAcceleration maximum acceleration of the car
     * @param desiredSpeed desired speed of the car, which is the minimum of the maximum road speed and the maximum
     *                     speed of the car
     * @param timeStep time step of the simulation
     * @return double free flow speed calculated based on the Gipps model
     **/
    private double getFreeFlowSpeed(double currentSpeed, double maxAcceleration, double desiredSpeed, double timeStep) {
        return currentSpeed + 2.5 * maxAcceleration * timeStep * (1 - currentSpeed / desiredSpeed) *
                Math.sqrt(0.025 + currentSpeed / desiredSpeed);
    }

    /**
     * safe speed part of the gipps model, it calculates the safe speed based on the current speed, maximum
     * deceleration, leading speed, minimum gap, maximum deceleration of the car in front, time step, x position of the
     * car and x position of the leading car
     *
     * @param currentSpeed current speed of the car
     * @param maxDeceleration maximum deceleration of the car
     * @param leadingSpeed speed of the leading car
     * @param minGap minimum gap to the leading car
     * @param maxDecelerationFront maximum deceleration of the car in front
     * @param timeStep time step of the simulation
     * @param xPosition x position of the car
     * @param leadingXPosition x position of the leading car
     * @return double safe speed calculated based on the Gipps model
     **/
    private double safeSpeed(double currentSpeed, double maxDeceleration, double leadingSpeed, double minGap,
                             double maxDecelerationFront, double timeStep, double xPosition, double leadingXPosition) {
        double firstPart = maxDeceleration * timeStep;
        double positionGap = leadingXPosition - xPosition - minGap;
        double firstPartUnderSqrt = maxDeceleration * maxDeceleration * timeStep * timeStep;
        double endingPartBraces = currentSpeed * timeStep - (leadingSpeed * leadingSpeed)/maxDecelerationFront;
        double partUnderSqrt = firstPartUnderSqrt - maxDeceleration * (2 * positionGap - endingPartBraces);
        double secondTerm = Math.sqrt(partUnderSqrt);

        return firstPart + secondTerm;
    }

}
