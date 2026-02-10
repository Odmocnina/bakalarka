package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import models.ICarFollowingModel;
import models.ModelId;

/********************************************
 * Rule 184 car following model implementation (cellular), annotated with @ModelId("rule-184") for identification during
 * reflexive loading
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/

@ModelId("rule-184")
public class Rule184 implements ICarFollowingModel {

    /** type of the model **/
    private final String type;

    /** size of one cell in meters **/
    private final double CELL_SIZE = 5.0; // in meters

    /**
     * constructor for Rule 184 model
     **/
    public Rule184() {
        this.type = Constants.CELLULAR;
    }

    /**
     * function to get new speed based on Rule 184 algorithm
     *
     * @param parameters HashMap of parameters needed for calculation
     * @return new speed as double (is converted to int later, returned as double for interface compatibility)
     **/
    @Override
    public double getNewSpeed(java.util.HashMap<String, Double> parameters) {
        int xPosition = parameters.get(RequestConstants.X_POSITION_REQUEST).intValue();
        int xPositionStraightForward = parameters.get(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST).intValue();
        int lengthStraightForward = parameters.get(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST).intValue();
        double distance;
        if (xPositionStraightForward == Constants.NO_CAR_THERE) {
            distance = Double.MAX_VALUE; // no car ahead
        } else {
            distance = (xPositionStraightForward - xPosition - lengthStraightForward); // distance in cells
        }

        if (distance > 1) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * function to request parameters needed for Rule 184 model
     *
     * @return String of requested parameters
     **/
    @Override
    public String getID() {
        return "rule184";
    }

    /**
     * function to get type of the model
     *
     * @return String of type
     **/
    @Override
    public String getType() {
        return this.type;
    }

    /*
     * function to get cell size in meters
     *
     * @return cell size as double
     */
    @Override
    public double getCellSize() {
        return CELL_SIZE; // in meters
    }

    /**
     * function to request parameters needed for Rule 184 model
     *
     * @return String of requested parameters
     **/
    @Override
    public String requestParameters() {
        String[] params = {
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, params);
    }

    /**
     * function to request parameters needed for generation of Rule 184 model
     *
     * @return String of requested parameters for generation
     **/
    @Override
    public String getParametersForGeneration() {
        return RequestConstants.LENGTH_REQUEST;
    }

    /**
     * function to get name of the model
     *
     * @return name as String
     **/
    @Override
    public String getName() {
        return "Rule 184";
    }
}
