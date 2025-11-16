package models.carFollowingModels;

import core.utils.Constants;
import core.utils.RequestConstants;
import models.ICarFollowingModel;

import java.util.HashMap;
import java.util.Random;

public class KKW_Linear implements ICarFollowingModel {

    Random rand = new Random();
    private double cellSize = 1.5; // size of one cell in meters
    private double d = 2.0; // minimum gap

    protected double k = 1.0; // synchronization coefficient
    private double v_p = 3;
    private double pa1 = 0.1; // random probability parameter a1, probability of random acceleration when v_curr < v_p
                              // (when car is slow)
    private double pa2 = 0.02; // random probability parameter a2, probability of random acceleration when v_curr >=
                               // v_p, (when car is fast)
    // pa2 < pa1, pa2 should be smaller than pa1

    protected double acceleration = 1.0; // acceleration rate

    private double randomSlowdownChanceStart = 0.5; // random slowdown chance when starting
    private double randomSlowdownChance = 0.3; // random slowdown chance when moving

    @Override
    public String requestParameters() {
        String[] params = {
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST,
                RequestConstants.TIME_STEP_REQUEST,
                RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST,
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, params);
    }

    @Override
    public String getParametersForGeneration() {
        String[] params = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.LENGTH_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, params);
    }

    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        double freeSpeed = parameters.get(RequestConstants.MAX_SPEED_REQUEST);
        double currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST);
        double distanceToNextCar = parameters.get(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST);
        double timeStep = parameters.get(RequestConstants.TIME_STEP_REQUEST);
        double speedNextCar = parameters.get(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST);
        if (speedNextCar == Constants.NO_CAR_THERE) {
            speedNextCar = freeSpeed;
        }

        // deterministic part
        double safeSpeed = getSafeSpeed(distanceToNextCar, timeStep);
        int syncGap = getSynchronizationGap(currentSpeed, d, k, timeStep);
        double synchronizedSpeed = getSynchronizedSpeed(currentSpeed, syncGap, distanceToNextCar, timeStep,
                speedNextCar);
        double deterministicSpeed = Math.max(0, Math.min(freeSpeed, Math.min(safeSpeed, synchronizedSpeed)));

        // stochastic part
        double chanceA = getChanceOfRandomSlowdownA(currentSpeed);
        double chanceB = getChanceOfRandomSlowdownB(currentSpeed);
        double randomSpeedChance = getRandomSpeedModification(chanceA, chanceB);
        double randomAffectSpeed = deterministicSpeed + randomSpeedChance * acceleration * timeStep;

        double smallestSpeed = Math.min(currentSpeed + acceleration * timeStep, Math.min(freeSpeed, safeSpeed));
        double newSpeed = Math.max(0, Math.min(randomAffectSpeed, smallestSpeed));
        return newSpeed;

    }

    protected int getSynchronizationGap(double currentSpeed, double d, double k, double timeStep) {
        return (int) (d + k * currentSpeed * timeStep);
    }

    private double getSafeSpeed(double gap, double timeStep) {
        return gap / timeStep;
    }

    private double getSynchronizedSpeed(double currentSpeed, int syncGap, double distanceToNextCar, double timeStep,
                                        double speedNextCar) {
        if (distanceToNextCar > syncGap) {
            return currentSpeed + acceleration * timeStep;
        } else {
            return currentSpeed + acceleration * timeStep * Math.signum(speedNextCar - currentSpeed);
        }
    }

    private double getChanceOfRandomSlowdownB(double currentSpeed) {
        if (currentSpeed == 0) {
            return randomSlowdownChanceStart;
        } else {
            return randomSlowdownChance;
        }
    }

    private double getChanceOfRandomSlowdownA(double currentSpeed) {
        if (currentSpeed < v_p) {
            return pa1;
        } else {
            return pa2;
        }
    }

    private double getRandomSpeedModification(double chanceA, double chanceB) {
        double r = rand.nextDouble();
        if (r < chanceB) {
            return -1.0;
        } else if (r < chanceA + chanceB) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public String getName() {
        return "Kerner-Klenov-Wolf (linear)";
    }



    @Override
    public String getID() {
        return "kkw-linear";
    }

    @Override
    public String getType() {
        return Constants.CELLULAR;
    }

    public double getCellSize() {
        return this.cellSize;
    }

}
