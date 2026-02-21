package models.carFollowingModels;

import core.utils.RandomNumberGenerator;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import models.ICarFollowingModel;
import models.ModelId;

import java.util.HashMap;
import java.util.Random;

/********************************************
 * Kerner-Klenov-Wolf (linear) car following model implementation (cellular), annotated with @ModelId("kkw-linear") for
 * identification during reflexive loading
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
@ModelId("kkw-linear")
public class KKW_Linear implements ICarFollowingModel {

    /** Random number generator **/
    RandomNumberGenerator randomNumberGenerator;

    /** size of one cell in meters **/
    private double cellSize = 1.5;

    /** minimum gap **/
    private double d = 2.0;

    /** synchronization coefficient **/
    protected double k = 1.0;

    /** speed threshold for random acceleration probability **/
    private double v_p = 3;

    /** random acceleration probabilities, slow cars **/
    private double pa1 = 0.1; // random probability parameter a1, probability of random acceleration when v_curr < v_p
                              // (when car is slow)

    /** random acceleration probabilities, fast cars **/
    private double pa2 = 0.02; // random probability parameter a2, probability of random acceleration when v_curr >=
                               // v_p, (when car is fast)
    // pa2 < pa1, pa2 should be smaller than pa1

    /** acceleration rate (max acceleration in 1 step is 1 cell) **/
    protected double acceleration = 1.0; // acceleration rate

    /** random slowdown when car is starting **/
    private double randomSlowdownChanceStart = 0.5; // random slowdown chance when starting

    /** random slowdown when car is moving **/
    private double randomSlowdownChance = 0.3; // random slowdown chance when moving

    public KKW_Linear() {
        this.randomNumberGenerator = RandomNumberGenerator.getInstance(0); // the seed set here is not relevant
    }

    /**
     * function to request parameters needed for KKW linear model to the road
     *
     * @return String of requested parameters
     */
    @Override
    public String requestParameters() {
        String[] params = {
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.TIME_STEP_REQUEST,
                RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST,
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, params);
    }

    /**
     * function to request parameters needed for generation of KKW linear model
     *
     * @return String of requested parameters for generation
     **/
    @Override
    public String getParametersForGeneration() {
        String[] params = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.LENGTH_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, params);
    }

    /**
     * function to calculate new speed based on KKW linear model
     *
     * @param parameters HashMap of parameters needed for speed calculation
     * @return double new speed calculated based on KKW linear model
     **/
    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        double freeSpeed = parameters.get(RequestConstants.MAX_SPEED_REQUEST);
        double currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST);
        int xPosition = parameters.get(RequestConstants.X_POSITION_REQUEST).intValue();
        int xPositionStraightForward = parameters.get(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST).intValue();
        int lengthStraightForward = parameters.get(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST).intValue();
        double distance;
        if (xPositionStraightForward == Constants.NO_CAR_THERE) {
            distance = Double.MAX_VALUE; // no car ahead
        } else {
            distance = (xPositionStraightForward - xPosition - lengthStraightForward); // distance in cells
        }
        double timeStep = parameters.get(RequestConstants.TIME_STEP_REQUEST);
        double speedNextCar = parameters.get(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST);
        if (speedNextCar == Constants.NO_CAR_THERE) {
            speedNextCar = freeSpeed;
        }

        // deterministic part
        double safeSpeed = getSafeSpeed(distance, timeStep);
        int syncGap = getSynchronizationGap(currentSpeed, d, k, timeStep);
        double synchronizedSpeed = getSynchronizedSpeed(currentSpeed, syncGap, distance, timeStep,
                speedNextCar);
        double deterministicSpeed = Math.max(0, Math.min(freeSpeed, Math.min(safeSpeed, synchronizedSpeed)));

        // stochastic part
        double chanceA = getChanceOfRandomSlowdownA(currentSpeed);
        double chanceB = getChanceOfRandomSlowdownB(currentSpeed);
        double randomSpeedChance = getRandomSpeedModification(chanceA, chanceB);
        double randomAffectSpeed = deterministicSpeed + randomSpeedChance * acceleration * timeStep;

        double smallestSpeed = Math.min(currentSpeed + acceleration * timeStep, Math.min(freeSpeed, safeSpeed));
        return Math.max(0, Math.min(randomAffectSpeed, smallestSpeed));

    }

    /**
     * function to calculate synchronization gap based on current speed, minimum gap, synchronization coefficient and
     * time step
     *
     * @param currentSpeed current speed of the car
     * @param d minimum gap
     * @param k synchronization coefficient
     * @param timeStep time step of the simulation
     * @return synchronization gap as int (in cells)
     **/
    protected int getSynchronizationGap(double currentSpeed, double d, double k, double timeStep) {
        return (int) (d + k * currentSpeed * timeStep);
    }

    /**
     * function to calculate safe speed based on gap and time step
     *
     * @param gap gap to the next car in cells
     * @param timeStep time step of the simulation
     * @return safe speed as double
     **/
    private double getSafeSpeed(double gap, double timeStep) {
        return gap / timeStep;
    }

    /**
     * function to calculate synchronized speed based on current speed, synchronization gap, distance to the next car,
     * time step and speed of the next car
     *
     * @param currentSpeed current speed of the car
     * @param syncGap synchronization gap in cells
     * @param distanceToNextCar distance to the next car in cells
     * @param timeStep time step of the simulation
     * @param speedNextCar speed of the next car in m/s
     * @return synchronized speed as double
     **/
    private double getSynchronizedSpeed(double currentSpeed, int syncGap, double distanceToNextCar, double timeStep,
                                        double speedNextCar) {
        if (distanceToNextCar > syncGap) {
            return currentSpeed + acceleration * timeStep;
        } else {
            return currentSpeed + acceleration * timeStep * Math.signum(speedNextCar - currentSpeed);
        }
    }

    /**
     * function to calculate chance of random slowdown B based on current speed, it returns random slowdown chance when
     * starting if current speed is 0, otherwise it returns random slowdown chance when moving
     *
     * @param currentSpeed current speed of the car
     * @return chance of random slowdown B as double
     **/
    private double getChanceOfRandomSlowdownB(double currentSpeed) {
        if (currentSpeed == 0) {
            return randomSlowdownChanceStart;
        } else {
            return randomSlowdownChance;
        }
    }

    /**
     * function to calculate chance of random slowdown A based on current speed, it returns pa1 if current speed is less
     * than v_p, otherwise it returns pa2
     *
     * @param currentSpeed current speed of the car
     * @return chance of random slowdown A as double
     **/
    private double getChanceOfRandomSlowdownA(double currentSpeed) {
        if (currentSpeed < v_p) {
            return pa1;
        } else {
            return pa2;
        }
    }

    /**
     * function to calculate random speed modification based on chance A and chance B, it returns -1 if random number is
     * less than chance B, it returns 1 if random number is less than chance A + chance B, otherwise it returns 0
     *
     * @param chanceA chance of random slowdown A
     * @param chanceB chance of random slowdown B
     * @return random speed modification as double (-1, 0 or 1)
     **/
    private double getRandomSpeedModification(double chanceA, double chanceB) {
        double r = randomNumberGenerator.nextDouble();
        if (r < chanceB) {
            return -1.0;
        } else if (r < chanceA + chanceB) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    /**
     * function to get name of the model, for display, logging and stuff like that
     *
     * @return name of the model as String
     **/
    @Override
    public String getName() {
        return "Kerner-Klenov-Wolf (linear)";
    }

    /**
     * function to get ID of the model, for identification during reflexive loading, should match the value in @ModelId
     * annotation
     *
     * @return ID of the model as String
     **/
    @Override
    public String getID() {
        return "kkw-linear";
    }

    /**
     * function to get type of the model (cellular)
     *
     * @return type of the model as String
     **/
    @Override
    public String getType() {
        return Constants.CELLULAR;
    }

    /**
     * function to get cell size for the model, since it is a cellular model, it returns the cell size
     *
     * @return cell size as double
     **/
    public double getCellSize() {
        return this.cellSize;
    }

}
