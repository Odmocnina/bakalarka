package models.carFollowingModels;

import core.utils.Constants;
import models.ICarFollowingModel;

import java.util.HashMap;

/********************************************
 * Intelligent Driver Model (IDM) car following model implementation (continuous)
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class IDM implements ICarFollowingModel {

    /** type of the model **/
    private final String type;

    /** exponent used in speed part of acceleration calculation **/
    private final double exponent = 4.0; // typically set to 4

    /**
     * constructor for IDM model
     **/
    public IDM() {
        this.type = Constants.CONTINOUS;
    }

    /**
     * function to get new speed based on IDM algorithm
     *
     * @param parameters HashMap of parameters needed for calculation
     * @return new speed as double
     **/
    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        double currentSpeed = parameters.get(Constants.CURRENT_SPEED_REQUEST);
        double maxSpeed = parameters.get(Constants.MAX_SPEED_REQUEST);
        double xPosition = parameters.get(Constants.X_POSITION_REQUEST);
        double xPositionNextCar = parameters.get(Constants.X_POSITION_STRAIGHT_FORWARD_REQUEST);
        double distance;
        if (xPositionNextCar != Constants.NO_CAR_THERE) {
            double lengthNextCar = parameters.get(Constants.LENGTH_STRAIGHT_FORWARD_REQUEST);
            distance = xPositionNextCar - xPosition - lengthNextCar;
        } else {
            distance = Double.MAX_VALUE;
        }

        double maxAcceleration = parameters.get(Constants.MAX_ACCELERATION_REQUEST);
        double currentSpeedNextCar = parameters.get(Constants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST);
        double speedDifferenceToTheNextCar;
        if (currentSpeedNextCar != Constants.NO_CAR_THERE) {
            speedDifferenceToTheNextCar = Math.abs(currentSpeed - currentSpeedNextCar);
        } else {
            speedDifferenceToTheNextCar = 0.0;
        }

        double minimumGapToNextCar = parameters.get(Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        double decelerationComfort = parameters.get(Constants.DECELERATION_COMFORT_REQUEST);
        double desiredTimeHeadway = parameters.get(Constants.DESIRED_TIME_HEADWAY_REQUEST);

        return currentSpeed + getAcceleration(currentSpeed, maxSpeed, distance,
                maxAcceleration, speedDifferenceToTheNextCar, minimumGapToNextCar,
                decelerationComfort, desiredTimeHeadway);
    }

    /**
     * function to calculate acceleration based on IDM formula
     *
     * @return acceleration as double
     **/
    private double getAcceleration(double currentSpeed, double maxSpeed, double distance,
                                   double maxAcceleration, double speedDifferenceToTheNextCar,
                                   double minimumGapToNextCar, double decelerationComfort,
                                   double desiredTimeHeadway) {
        double vPart = (Math.pow(currentSpeed / maxSpeed, this.exponent));
        double sPart = getDesiredGap(currentSpeed, speedDifferenceToTheNextCar,
                minimumGapToNextCar, desiredTimeHeadway, maxAcceleration, decelerationComfort);

        return maxAcceleration * (1 - vPart - Math.pow(sPart / distance, 2));
    }

    /**
     * function to calculate desired gap to the next car based on IDM formula
     *
     * @return desired gap as double
     **/
    private double getDesiredGap(double currentSpeed, double speedDifferenceToTheNextCar,
                                 double minimumGapToNextCar, double desiredTimeHeadway,
                                 double maxAcceleration, double decelerationComfort) {
        double gapPlus = currentSpeed * desiredTimeHeadway +
                (currentSpeed * speedDifferenceToTheNextCar) / (2 * Math.sqrt(maxAcceleration * decelerationComfort));
        return minimumGapToNextCar + Math.max(0, gapPlus);
    }

    /**
     * getter for ID of the model
     **/
    @Override
    public String getID() {
        return "idm";
    }

    /**
     * getter for type of the model (continuous)
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
    @Override
    public String requestParameters() {
        return Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.CURRENT_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.X_POSITION_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.X_POSITION_STRAIGHT_FORWARD_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.LENGTH_STRAIGHT_FORWARD_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.MAX_ACCELERATION_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DECELERATION_COMFORT_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DESIRED_TIME_HEADWAY_REQUEST;
    }

    /**
     * function to get parameters needed for car generation
     *
     * @return request parameters as String
     **/
    @Override
    public String getParametersForGeneration() {
        return Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.MAX_ACCELERATION_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DECELERATION_COMFORT_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DESIRED_TIME_HEADWAY_REQUEST + Constants.REQUEST_SEPARATOR
                + Constants.LENGTH_REQUEST;
    }

    /**
     * getter for cell size
     *
     * @return cell size as double
     **/
    @Override
    public double getCellSize() {
        return Constants.PARAMETER_UNDEFINED;
    }

    /**
     * getter for name of the model
     *
     * @return name as String
     **/
    @Override
    public String getName() {
        return "Intelligent Driver Model (IDM)";
    }
}
