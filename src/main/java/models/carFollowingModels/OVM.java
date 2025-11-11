package models.carFollowingModels;

import core.utils.Constants;
import core.utils.RequestConstants;
import models.ICarFollowingModel;

/********************************************
 * Optimal Velocity Model (OVM) car following model implementation (continuous)
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class OVM implements ICarFollowingModel {

    /**
     * function to get new speed based on OVM algorithm
     *
     * @param parameters HashMap of parameters needed for calculation
     **/
    @Override
    public double getNewSpeed(java.util.HashMap<String, Double> parameters) {
        double currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST);
        double distance = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST);
        double speedDifferenceSensitivityParameter =
                parameters.get(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST);

        return currentSpeed + speedDifferenceSensitivityParameter *
               (optimalVelocity(distance) - currentSpeed);
    }

    /**
     * function to calculate optimal velocity based on distance to the next car
     *
     * @param distance distance to the next car
     * @return optimal velocity as double
     **/
    private double optimalVelocity(double distance) {
        return 16.8 * (Math.tanh(0.086 * (distance - 25) + 0.913));
    }

    /**
     * function to request parameters needed for OVM model
     *
     * @return String of requested parameters
     **/
    @Override
    public String requestParameters() {
        return RequestConstants.CURRENT_SPEED_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.MAX_SPEED_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST;

    }

    /**
     * function to request parameters needed for generation of OVM model
     *
     * @return String of requested parameters for generation
     **/
    @Override
    public String getParametersForGeneration() {
        return RequestConstants.MAX_SPEED_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST + RequestConstants.REQUEST_SEPARATOR +
                RequestConstants.LENGTH_REQUEST;
    }

    /**
     * function to get ID of the model
     *
     * @return ID as String
     **/
    @Override
    public String getID() {
        return "ovm";
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
        return "Optimal Velocity Model";
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
