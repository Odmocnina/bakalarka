package models.carFollowingModels;

import core.utils.RandomNumberGenerator;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import models.ICarFollowingModel;
import models.ModelId;

import java.util.HashMap;
import java.util.Random;

/********************************************
 * Head-leading car following model implementation (cellular), annotated with @ModelId("head-leading") for
 * identification during reflexive loading
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
@ModelId("head-leading")
public class HeadLeading implements ICarFollowingModel {

    /** size of one cell in meters **/
    private final double CELL_SIZE = 2.5; // in meters

    /** type of the model **/
    private final String type;

    /** random chance of slowing down when car is moving **/
    private final double slowDownChance = 0.3; // probability of random slowing down

    /** random chance of slowing down when car is starting **/
    private final double slowDownChanceStart = 0.5; // initial probability of random slowing down

    private RandomNumberGenerator randomNumberGenerator;

    /**
     * constructor for head-leading model
     **/
    public HeadLeading() {
        this.type = Constants.CELLULAR;
        this.randomNumberGenerator = RandomNumberGenerator.getInstance(0); // the seed set here is not relevant,
        // as the seed should be already set in the RandomNumberGenerator singleton before creating the model
    }

    /**
     * function to get new speed based on head-leading algorithm
     *
     * @param parameters HashMap of parameters needed for calculation
     * @return new speed as double (is converted to int later, returned as double for interface compatibility)
     **/
    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        int currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST).intValue();
        int maxSpeed = parameters.get(RequestConstants.MAX_SPEED_REQUEST).intValue();
        int xPosition = parameters.get(RequestConstants.X_POSITION_REQUEST).intValue();
        int xPositionStraightForward = parameters.get(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST).intValue();
        int lengthStraightForward = parameters.get(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST).intValue();
        double distance;
        if (xPositionStraightForward == Constants.NO_CAR_THERE) {
            distance = Double.MAX_VALUE; // no car ahead
        } else {
            distance = (xPositionStraightForward - xPosition - lengthStraightForward); // distance in cells
        }

        int distanceInCells = (int) distance;

       // int distanceInCells = distanceInCells; // convert distance to number of cells

        boolean starting = (currentSpeed == 0);

        // acceleration
        if (currentSpeed < maxSpeed) {
            currentSpeed++;
        }
        // slowing down
        if (distanceInCells <= currentSpeed) {
            currentSpeed = distanceInCells - 1;
        }
        // randomization
        // use chance for starting when starting from 0 speed (to simulate slower start, should be higher than normal
        // slow down chance
        double currentSlowDownChance = starting ? this.slowDownChanceStart : this.slowDownChance;
        if (currentSpeed > 0 && randomNumberGenerator.nextDouble() < currentSlowDownChance) { // 30% chance to slow down
            currentSpeed--;
        }
        return Math.max(0, currentSpeed);
    }

    /**
     * getter for cell size
     *
     * @return cell size as double
     **/
    public double getCellSize() {
        return this.CELL_SIZE;
    }

    /**
     * getter for ID of the model
     *
     * @return ID as String
     **/
    @Override
    public String getID() {
        return "head-leading";
    }

    /**
     * getter for type of the model (cellular)
     *
     * @return type as String
     **/
    @Override
    public String getType() {
        return this.type;
    }

    /**
     * function to get parameters needed for simulation step
     *
     * @return request parameters as String
     **/
    public String requestParameters() {
        String[] requests = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
    }

    /**
     * function to get parameters needed for car generation
     *
     * @return request parameters as String
     **/
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
        return "Head-leading algorithm";
    }

    /**
     * Constructor with injected Random for testing purposes
     *
     * @param random Random instance to use
     **/
    HeadLeading(Random random) {
        this.type = Constants.CELLULAR;
      //  this.random = random;
    }
}

