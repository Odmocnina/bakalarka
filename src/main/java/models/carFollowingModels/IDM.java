package models.carFollowingModels;

import core.utils.Constants;
import models.ICarFollowingModel;

import java.util.HashMap;

public class IDM implements ICarFollowingModel {

    private String type;

    private double exponent = 4.0; // typically set to 4

    public IDM() {
        this.type = Constants.CONTINOUS;
    }

    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        double currentSpeed = parameters.get(Constants.CURRENT_SPEED_REQUEST);
        double maxSpeed = parameters.get(Constants.MAX_SPEED_REQUEST);
        double distance = parameters.get(Constants.DISTANCE_TO_NEXT_CAR_REQUEST);
        double maxAcceleration = parameters.get(Constants.MAX_ACCELERATION_REQUEST);
        double speedDifferenceToTheNextCar = parameters.get(Constants.SPEED_DIFFERENCE_TO_NEXT_CAR_REQUEST);
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
        return "idm";
    }

    @Override
    public String getType() {
        return this.type;
    }

    public String requestParameters() {
        return Constants.MAX_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.CURRENT_SPEED_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DISTANCE_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.MAX_ACCELERATION_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.SPEED_DIFFERENCE_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DECELERATION_COMFORT_REQUEST + Constants.REQUEST_SEPARATOR +
                Constants.DESIRED_TIME_HEADWAY_REQUEST;
    }

    public double getCellSize() {
        return -1.0;
    }

    @Override
    public String getName() {
        return "Intelligent Driver Model (IDM)";
    }
}
