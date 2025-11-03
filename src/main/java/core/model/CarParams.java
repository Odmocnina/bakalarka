package core.model;

import core.utils.Constants;

import javafx.scene.paint.Color;

public class CarParams {

    public double currentSpeed = Constants.PARAMETER_UNDEFINED;
    public double maxSpeed = Constants.PARAMETER_UNDEFINED;
    public double xPosition = Constants.PARAMETER_UNDEFINED;
    public int lane = (int) Constants.PARAMETER_UNDEFINED;
    public double length = Constants.PARAMETER_UNDEFINED;
    public double maxAcceleration = Constants.PARAMETER_UNDEFINED;
    public double minGapToNextCar = Constants.PARAMETER_UNDEFINED;
    public double maxConfortableDeceleration = Constants.PARAMETER_UNDEFINED;
    public double desiredTimeHeadway = Constants.PARAMETER_UNDEFINED;
    public double reactionCoefficient = Constants.PARAMETER_UNDEFINED;
    public double politenessFactor = Constants.PARAMETER_UNDEFINED;
    public int id = (int) Constants.PARAMETER_UNDEFINED;
    public Color color = null;
    public boolean isProcessed = false;

    public String toString() {
        return "CarParams[id= " + id + ", currentSpeed=" + currentSpeed + ", maxSpeed=" + maxSpeed + ", xPosition=" + xPosition +
                ", lane=" + lane + ", length=" + length + ", maxAcceleration=" + maxAcceleration +
                ", minGapToNextCar=" + minGapToNextCar + ", maxConfortableDeceleration=" + maxConfortableDeceleration +
                ", desiredTimeHeadway=" + desiredTimeHeadway +
                ", color=" + color + "]";
    }

}
