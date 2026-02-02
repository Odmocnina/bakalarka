package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import models.ICarFollowingModel;

import java.util.HashMap;

/********************************************
 * Intelligent Driver Model (IDM) car following model implementation (continuous)
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
@CarFollowingModelId("idm")
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
        double currentSpeed = parameters.get(RequestConstants.CURRENT_SPEED_REQUEST);
        double maxSpeed = parameters.get(RequestConstants.MAX_SPEED_REQUEST);
        double xPosition = parameters.get(RequestConstants.X_POSITION_REQUEST);
        double xPositionNextCar = parameters.get(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST);
        double distance;
        if (xPositionNextCar != Constants.NO_CAR_THERE) {
            double lengthNextCar = parameters.get(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST);
            distance = xPositionNextCar - xPosition - lengthNextCar;
        } else {
            distance = Double.MAX_VALUE;
        }

        double maxAcceleration = parameters.get(RequestConstants.MAX_ACCELERATION_REQUEST);
        double currentSpeedNextCar = parameters.get(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST);
        double speedDifferenceToTheNextCar;
        if (currentSpeedNextCar != Constants.NO_CAR_THERE) {
            speedDifferenceToTheNextCar = Math.abs(currentSpeed - currentSpeedNextCar);
        } else {
            speedDifferenceToTheNextCar = 0.0;
        }

        double minimumGapToNextCar = parameters.get(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        double decelerationComfort = parameters.get(RequestConstants.DECELERATION_COMFORT_REQUEST);
        double desiredTimeHeadway = parameters.get(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST);

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
        String[] requests = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.MAX_ACCELERATION_REQUEST,
                RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.DECELERATION_COMFORT_REQUEST,
                RequestConstants.DESIRED_TIME_HEADWAY_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
    }

    /**
     * function to get parameters needed for car generation
     *
     * @return request parameters as String
     **/
    @Override
    public String getParametersForGeneration() {
        String[] requests = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.MAX_ACCELERATION_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.DECELERATION_COMFORT_REQUEST,
                RequestConstants.DESIRED_TIME_HEADWAY_REQUEST,
                RequestConstants.LENGTH_REQUEST
        };

        return String.join(RequestConstants.REQUEST_SEPARATOR, requests);
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
