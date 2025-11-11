package models.carFollowingModels;

import core.utils.Constants;
import core.utils.RequestConstants;
import models.ICarFollowingModel;

/********************************************
 * Rule 184 car following model implementation (cellular)
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
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
        double distanceDouble = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST);
        int distance = (int) distanceDouble; // convert distance to number of cells

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
        return RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST;
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
