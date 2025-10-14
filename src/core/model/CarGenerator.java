package core.model;

import app.AppContext;
import core.utils.Constants;
import javafx.scene.paint.Color;

import java.util.Random;

public class CarGenerator {

    private double maxMaxSpeed;
    private double minMaxSpeed;
    private double maxAcceleration;
    private double minAcceleration;
    private double maxDeceleration;
    private double minDeceleration;
    private double maxLength = 3.0;
    private double minLength = 1.0;
    private double maxDesiredTimeHeadway;
    private double minDesiredTimeHeadway;
    private String type;

    private double density; // vehicles per kilometer per lane

    private Color[] colors = Constants.CAR_COLORS;

    public CarGenerator(double density) {
        this.density = density;
    }

    public boolean decideIfNewCar() {
        if (Math.random() < density) { // Convert to vehicles per meter
            return true;
        }
        return false;
    }

    public CarParams generateCar() {
        if (this.type.equals(Constants.CELLULAR)) {
            return generateCarCellular();
        } else if (this.type.equals(Constants.CONTINOUS)) {
            return generateCarContinuous();
        } else {
            System.out.println("Unknown car generator type: " + this.type);
            return null;
        }
    }

    private CarParams generateCarCellular() {
        CarParams car = new CarParams();
        Random rand = new Random();

        int minMaxSpeedInt = (int) (minMaxSpeed);
        int maxMaxSpeedInt = (int) (maxMaxSpeed);
        int speedRange = maxMaxSpeedInt - minMaxSpeedInt + 1;
        int minLengthInt = (int) (minLength);
        int maxLengthInt = (int) (maxLength);
        int lengthRange = maxLengthInt - minLengthInt + 1;

        car.maxSpeed = (int) rand.nextInt(speedRange) + minMaxSpeedInt;
        car.length = (int) rand.nextInt(lengthRange) + minLengthInt;
        car.color = colors[(int) (Math.random() * colors.length)];

        return car;
    }

    private CarParams generateCarContinuous() {
        CarParams car = new CarParams();
        Random rand = new Random();

        car.maxSpeed = minMaxSpeed + (rand.nextDouble() * (maxMaxSpeed - minMaxSpeed));
        car.length = minLength + (rand.nextDouble() * (maxLength - minLength));
        car.color = colors[(int) (Math.random() * colors.length)];

        return car;
    }

    public void setMaxLength(double maxLength) {
        this.maxLength = maxLength;
    }

    public void setMinLength(double minLength) {
        this.minLength = minLength;
    }

    public void setMaxMaxSpeed(double maxMaxSpeed) {
        this.maxMaxSpeed = maxMaxSpeed;
    }

    public void setMinMaxSpeed(double minMaxSpeed) {
        this.minMaxSpeed = minMaxSpeed;
    }

    public void setType(String type) {
        this.type = type;
        if (type.equals(Constants.CELLULAR)) {
            //translateParametersToCellular(AppContext.cellSize);
        }
    }

    private void translateParametersToCellular(double cellSize) {
        this.minMaxSpeed = Math.ceil(this.minMaxSpeed / cellSize);
        this.maxMaxSpeed = Math.round(this.maxMaxSpeed / cellSize);
        this.minLength = Math.ceil(this.minLength / cellSize);
        this.maxLength = Math.round(this.maxLength / cellSize);
    }


}
