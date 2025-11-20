package models.carFollowingModels;

import core.utils.Constants;
import core.utils.RequestConstants;
import models.ICarFollowingModel;

import java.util.HashMap;
import java.util.Random;

/********************************************
 * Nagel-Schreckenberg car following model implementation (cellular)
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class NagelSchreckenberg implements ICarFollowingModel {

    /** size of one cell in meters **/
    private final double CELL_SIZE = 7.5; // in meters

    /** type of the model **/
    private final String type;

    /** random chance of slowing down when car is moving **/
    private final double slowDownChance = 0.3; // probability of random slowing down

    private final Random random;

    /**
     * constructor for Nagel-Schreckenberg model
     **/
    public NagelSchreckenberg() {
        this.type = Constants.CELLULAR;
        this.random = new Random();
    }

    /**
     * function to get new speed based on Nagel-Schreckenberg algorithm
     *
     * @param parameters HashMap of parameters needed for calculation
     * @return new speed as double (is converted to int later, returned as double for interface compatibility)
     **/
    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        int currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST).intValue();
        int maxSpeed = parameters.get(RequestConstants.MAX_SPEED_REQUEST).intValue();
        double distance = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST).intValue();
        int distanceInCells = (int) Math.round(distance); // convert distance to number of cells
        // Step 1: Acceleration
        if (currentSpeed < maxSpeed) {
            currentSpeed++;
        }
        // Step 2: Slowing down
        if (distanceInCells <= currentSpeed) {
            currentSpeed = distanceInCells - 1;
        }
        // Step 3: Randomization
        if (currentSpeed > 0 && random.nextDouble() < this.slowDownChance) { // 30% chance to slow down
            currentSpeed--;
        }

        return Math.max(0, currentSpeed);
    }

    /*
     * function to get cell size in meters
     *
     * @return cell size as double
     */
    @Override
    public double getCellSize() {
        return this.CELL_SIZE;
    }

    /**
     * getter for ID of the model
     **/
    @Override
    public String getID() {
        return "nagelschreckenberg";
    }

    /**
     * getter for type of the model (cellular)
     **/
    @Override
    public String getType() {
        return this.type;
    }

    /**
     * function to get list of required parameters for the model when calculating new speed
     *
     * @return String of required parameters separated by Constants.REQUEST_SEPARATOR
     **/
    @Override
    public String requestParameters() {
        String[] requests = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
    }

    /**
     * function to get list of required parameters for the model when generating cars
     *
     * @return String of required parameters separated by Constants.REQUEST_SEPARATOR
     **/
    @Override
    public String getParametersForGeneration() {
        String[] requests = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.LENGTH_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
    }

    /**
     * getter for name of the model
     *
     * @return name as String
     **/
    @Override
    public String getName() {
        return "Nagel-Schreckenberg Model";
    }
}
