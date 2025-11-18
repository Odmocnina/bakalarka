package models.carFollowingModels;

import core.utils.Constants;
import core.utils.RequestConstants;

import java.util.HashMap;

/********************************************
 * Optimal Velocity Model (OVM) car following model implementation (continuous)
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class FVDM extends OVM_Different {

    /**
     * function to get new speed based on OVM algorithm
     *
     * @param parameters HashMap of parameters needed for calculation
     **/
    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        double speedDifferenceSensitivityParameter =
                parameters.get(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST);
        double currentSpeedStraightForward =
                parameters.get(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST);
        if (currentSpeedStraightForward == Constants.NO_CAR_THERE) {
            currentSpeedStraightForward = Double.MAX_VALUE;
        }
        double currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST);
        double speedDifference = currentSpeedStraightForward - currentSpeed;

        double newSpeed = super.getNewSpeed(parameters);

        return newSpeed + speedDifferenceSensitivityParameter * speedDifference;
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
                RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.MAX_ROAD_SPEED_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST
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
                RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST,
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
        return "fvdm";
    }

    /**
     * function to get name of the model
     *
     * @return name as String
     **/
    @Override
    public String getName() {
        return "Full Velocity Difference Model";
    }

}

