package models.carFollowingModels;

import core.utils.Constants;
import models.ICarFollowingModel;

import java.util.HashMap;

public class IDMN implements ICarFollowingModel {

    private String type;

    private double exponent = 4.0; // typically set to 4

    public IDMN() {
        this.type = Constants.CONTINOUS;
    }

    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        double currentSpeed = parameters.get(Constants.CURRENT_SPEED_REQUEST);
        double maxSpeed = parameters.get(Constants.MAX_SPEED_REQUEST);
        //double distance = parameters.get(Constants.DISTANCE_TO_NEXT_CAR_REQUEST);
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
        //double speedDifferenceToTheNextCar = parameters.get(Constants.SPEED_DIFFERENCE_TO_NEXT_CAR_REQUEST);
        double currentSpeedNextCar = parameters.get(Constants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST);
        double speedDifferenceToTheNextCar;
        if (currentSpeedNextCar != Constants.NO_CAR_THERE) {
            speedDifferenceToTheNextCar = currentSpeed - currentSpeedNextCar;
        } else {
            speedDifferenceToTheNextCar = 0.0;
        }

        double minimumGapToNextCar = parameters.get(Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        double decelerationComfort = parameters.get(Constants.DECELERATION_COMFORT_REQUEST);
        double desiredTimeHeadway = parameters.get(Constants.DESIRED_TIME_HEADWAY_REQUEST);

        double newSpeed = currentSpeed + getAcceleration(currentSpeed, maxSpeed, distance,
                maxAcceleration, speedDifferenceToTheNextCar, minimumGapToNextCar,
                decelerationComfort, desiredTimeHeadway);

        return newSpeed;
    }

    private double getAcceleration(double currentSpeed, double maxSpeed, double distance,
                                   double maxAcceleration, double speedDifferenceToTheNextCar,
                                   double minimumGapToNextCar, double decelerationComfort,
                                   double desiredTimeHeadway) {
        double vPart = (Math.pow(currentSpeed / maxSpeed, this.exponent));
        double sPart = getDesiredGap(currentSpeed, speedDifferenceToTheNextCar,
                minimumGapToNextCar, desiredTimeHeadway, maxAcceleration, decelerationComfort);
        double acceleration = maxAcceleration * (1 - vPart - Math.pow(sPart / distance, 2));
        return acceleration;
    }

    private double getDesiredGap(double currentSpeed, double speedDifferenceToTheNextCar,
                                 double minimumGapToNextCar, double desiredTimeHeadway,
                                 double maxAcceleration, double decelerationComfort) {
        double gapPlus = currentSpeed * desiredTimeHeadway +
                (currentSpeed * speedDifferenceToTheNextCar) / (2 * Math.sqrt(maxAcceleration * decelerationComfort));
        return minimumGapToNextCar + Math.max(0, gapPlus);
    }

    @Override
    public String getID() {
        return "idmn";
    }

    @Override
    public String getType() {
        return this.type;
    }

    public String requestParameters() {
        return Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.CURRENT_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
                //Constants.DISTANCE_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.X_POSITION_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.X_POSITION_STRAIGHT_FORWARD_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.LENGTH_STRAIGHT_FORWARD_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.MAX_ACCELERATION_REQUEST + Constants.REQUEST_SEPARATOR +
                //Constants.SPEED_DIFFERENCE_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DECELERATION_COMFORT_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DESIRED_TIME_HEADWAY_REQUEST;
    }

    @Override
    public String getParametersForGeneration() {
        return Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.MAX_ACCELERATION_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.DECELERATION_COMFORT_REQUEST + Constants.REQUEST_SEPARATOR +
               Constants.DESIRED_TIME_HEADWAY_REQUEST + Constants.REQUEST_SEPARATOR
                + Constants.LENGTH_REQUEST;
    }

    public double getCellSize() {
        return -1.0;
    }

    @Override
    public String getName() {
        return "Intelligent Driver Model (IDM)";
    }
}
