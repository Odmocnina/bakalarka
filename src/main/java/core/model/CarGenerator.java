package core.model;

import app.AppContext;
import core.utils.Constants;

import javafx.scene.paint.Color;
import java.util.Queue;
import java.util.Random;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class CarGenerator {
    private static final Logger logger = LogManager.getLogger(CarGenerator.class);
    private double maxMaxSpeed = Constants.DEFAULT_MAX_MAX_SPEED;
    private double minMaxSpeed = Constants.DEFAULT_MIN_MAX_SPEED;
    private double speedRange = Constants.PARAMETER_UNDEFINED;
    private double maxAcceleration = Constants.DEFAULT_MAX_ACCELERATION;
    private double minAcceleration = Constants.DEFAULT_MIN_ACCELERATION;
    private double accelerationRange = Constants.PARAMETER_UNDEFINED;
    private double maxDeceleration = Constants.DEFAULT_MAX_DECELERATION;
    private double minDeceleration = Constants.DEFAULT_MIN_DECELERATION;
    private double decelerationRange = Constants.PARAMETER_UNDEFINED;
    private double maxLength = Constants.DEFAULT_MAX_LENGTH;
    private double minLength = Constants.DEFAULT_MIN_LENGTH;
    private double lengthRange = Constants.PARAMETER_UNDEFINED;
    private double maxDesiredTimeHeadway = Constants.DEFAULT_MAX_DESIRED_TIME_HEADWAY;
    private double minDesiredTimeHeadway = Constants.DEFAULT_MIN_DESIRED_TIME_HEADWAY;
    private double desiredTimeHeadwayRange = Constants.PARAMETER_UNDEFINED;
    private double maxMinGapToNextCar = Constants.DEFAULT_MAX_MIN_GAP_TO_NEXT_CAR;
    private double minMinGapToNextCar = Constants.DEFAULT_MIN_MIN_GAP_TO_NEXT_CAR;
    private double minGapToNextCarRange = Constants.PARAMETER_UNDEFINED;
    private double maxReactionTime = Constants.DEFAULT_MAX_REACTION;
    private double minReactionTime = Constants.DEFAULT_MIN_REACTION;
    private double reactionTimeRange = Constants.PARAMETER_UNDEFINED;
    private double maxPolitnessFactor = Constants.DEFAULT_MAX_POLITENESS_FACTOR;
    private double minPolitnessFactor = Constants.DEFAULT_MIN_POLITENESS_FACTOR;
    private double politnessFactorRange = Constants.PARAMETER_UNDEFINED;
    private String type;
    private int id = 0;

    //private double density; // vehicles per kilometer per lane
    private double lambdaPerSec;
    private double timeToNext = Double.NaN;
    private Random rng = new Random();
    private boolean allowMultiplePerTick = false;

    private Color[] colors = Constants.CAR_COLORS;

    public CarGenerator(double density) {
        this.lambdaPerSec = Math.max(0.0, density); // lambda parameter for exponential distribution
    }

    private boolean decideBernoulliTick() {
        if (lambdaPerSec <= 0) return false;
        double p = 1.0 - Math.exp(-lambdaPerSec);
        return rng.nextDouble() < p;
    }

    private void scheduleNext() {
        if (lambdaPerSec <= 0) {
            timeToNext = Double.POSITIVE_INFINITY;
        } else {
            // T = -ln(U)/λ, U ~ U(0,1]
            double u = 1.0 - rng.nextDouble();
            timeToNext = -Math.log(u) / lambdaPerSec;
        }
    }

    public int arrivalsThisTick() {
        int count = 0;
        timeToNext -= 1.0; // pevný krok 1 s
        while (timeToNext <= 0.0) {
            count++;
            double overshoot = -timeToNext;
            scheduleNext();
            timeToNext -= overshoot;
        }
        return count;
    }

    public boolean decideIfNewCar() {
        if (allowMultiplePerTick) {
            return arrivalsThisTick() > 0;
        } else {
            return decideBernoulliTick();
        }
    }

    public CarParams generateCar() {
        CarParams car = null;

        if (this.type.equals(Constants.CELLULAR)) {
            car = generateCarCellular();
        } else if (this.type.equals(Constants.CONTINOUS)) {
            car = generateCarContinuous();
        } else {
            logger.warn("Unknown car generator type: " + this.type);
        }

        this.id++;
        return car;
    }

    private CarParams generateCarCellular() {
        CarParams car = new CarParams();
        Random rand = new Random();

        if (this.speedRange != Constants.PARAMETER_UNDEFINED) {
            if (this.speedRange == 0) {
                car.maxSpeed = (int) this.minMaxSpeed;
            } else {
                car.maxSpeed = (int) rand.nextInt((int) this.speedRange) + this.minMaxSpeed;
            }
        }

        if (this.lengthRange != Constants.PARAMETER_UNDEFINED) {
            if (this.lengthRange == 0) {
                car.length = (int) this.minLength;
            } else {
                car.length = (int) rand.nextInt((int) this.lengthRange) + this.minLength;
            }
        }

        if (this.accelerationRange != Constants.PARAMETER_UNDEFINED) {
            if (this.accelerationRange == 0) {
                car.maxAcceleration = this.minAcceleration;
            } else {
                car.maxAcceleration = minAcceleration + (rand.nextInt((int) this.accelerationRange));
            }
        }

        car.color = colors[(int) (Math.random() * colors.length)];
        car.id = this.id;

        return car;
    }

    private CarParams generateCarContinuous() {
        CarParams car = new CarParams();
        Random rand = new Random();

        if (this.speedRange != Constants.PARAMETER_UNDEFINED) {
            if (this.speedRange == 0) {
                car.maxSpeed = this.minMaxSpeed;
            } else {
                car.maxSpeed = minMaxSpeed + (rand.nextDouble() * speedRange);
            }
        }

        if (this.lengthRange != Constants.PARAMETER_UNDEFINED) {
            if (this.lengthRange == 0) {
                car.length = this.minLength;
            } else {
                car.length = minLength + (rand.nextDouble() * lengthRange);
            }
        }

        if (this.accelerationRange != Constants.PARAMETER_UNDEFINED) {
            int i = 0;
            if (this.accelerationRange == 0) {
                car.maxAcceleration = this.minAcceleration;
            } else {
                car.maxAcceleration = minAcceleration + (rand.nextDouble() * accelerationRange);
            }
        }

        if (this.decelerationRange != Constants.PARAMETER_UNDEFINED) {
            if (this.decelerationRange == 0) {
                car.maxConfortableDeceleration = this.minDeceleration;
            } else {
                car.maxConfortableDeceleration = minDeceleration + (rand.nextDouble() * decelerationRange);
            }
        }

        if (this.desiredTimeHeadwayRange != Constants.PARAMETER_UNDEFINED) {
            if (this.desiredTimeHeadwayRange == 0) {
                car.desiredTimeHeadway = this.minDesiredTimeHeadway;
            } else {
                car.desiredTimeHeadway = minDesiredTimeHeadway + (rand.nextDouble() * desiredTimeHeadwayRange);
            }
        }

        if (this.minGapToNextCarRange != Constants.PARAMETER_UNDEFINED) {
            if (this.minGapToNextCarRange == 0) {
                car.minGapToNextCar = this.minMinGapToNextCar;
            } else {
                car.minGapToNextCar = minMinGapToNextCar + (rand.nextDouble() * minGapToNextCarRange);
            }
        }

        car.color = colors[(int) (Math.random() * colors.length)];
        car.id = this.id;

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

    public void setMaxAcceleration(double maxAcceleration) {
        this.maxAcceleration = maxAcceleration;
    }

    public void setMinAcceleration(double minAcceleration) {
        this.minAcceleration = minAcceleration;
    }

    public void setMaxDeceleration(double maxDeceleration) {
        this.maxDeceleration = maxDeceleration;
    }

    public void setMinDeceleration(double minDeceleration) {
        this.minDeceleration = minDeceleration;
    }

    public void setMaxMinGapToNextCar(double maxMinGapToNextCar) {
        this.maxMinGapToNextCar = maxMinGapToNextCar;
    }

    public void setMinMinGapToNextCar(double minMinGapToNextCar) {
        this.minMinGapToNextCar = minMinGapToNextCar;
    }

    public void setMaxDesiredTimeHeadway(double maxDesiredTimeHeadway) {
        this.maxDesiredTimeHeadway = maxDesiredTimeHeadway;
    }

    public void setMinDesiredTimeHeadway(double minDesiredTimeHeadway) {
        this.minDesiredTimeHeadway = minDesiredTimeHeadway;
    }

    public void setType(String type) {
        this.type = type;
        if (type.equals(Constants.CELLULAR)) {
            translateParametersToCellular(AppContext.cellSize);
        }
    }

    public void setAllowMultiplePerTick(boolean allow) {
        this.allowMultiplePerTick = allow;
    }

    public void setLambdaPerSec(double lambda) {
        this.lambdaPerSec = Math.max(0.0, lambda);
        scheduleNext();
    }

    private void translateParametersToCellular(double cellSize) {
        this.minMaxSpeed = Math.ceil(this.minMaxSpeed / cellSize);
        this.maxMaxSpeed = Math.ceil(this.maxMaxSpeed / cellSize);
        this.minLength = Math.ceil(this.minLength / cellSize);
        this.maxLength = Math.ceil(this.maxLength / cellSize);
        this.calculateRanges();
    }

    public Queue<CarParams> generateCarsInToQueue(int minNumberOfCars, int maxNumberOfCars) {
        Queue<CarParams> queue = new java.util.LinkedList<>();
        Random rand = new Random();
        int numberOfCars = rand.nextInt(maxNumberOfCars - minNumberOfCars + 1) + minNumberOfCars;

        while (queue.size() < numberOfCars) {
            CarParams car = generateCar();
            queue.add(car);
        }

        return queue;
    }

    private void calculateRanges() {
        this.speedRange = this.maxMaxSpeed - this.minMaxSpeed;
        this.accelerationRange = this.maxAcceleration - this.minAcceleration;
        this.decelerationRange = this.maxDeceleration - this.minDeceleration;
        this.lengthRange = this.maxLength - this.minLength;
        this.desiredTimeHeadwayRange = this.maxDesiredTimeHeadway - this.minDesiredTimeHeadway;
        this.minGapToNextCarRange = this.maxMinGapToNextCar - this.minMinGapToNextCar;
        this.reactionTimeRange = this.maxReactionTime - this.minReactionTime;
        this.politnessFactorRange = this.maxPolitnessFactor - this.minPolitnessFactor;
    }

    public boolean checkIfAllParametresAreLoaded(String modelRequest) {
        this.calculateRanges();
        String[] requiredParams = modelRequest.split(Constants.REQUEST_SEPARATOR);

        if (requiredParams.length == 0) {
            logger.fatal("CarGenerator: No parameters requested by the car following model.");
            return false;
        }

        if (this.lengthRange == Constants.PARAMETER_UNDEFINED) {
            logger.fatal("CarGenerator: length parameters are not properly defined.");
            return false;
        }

        for (String param : requiredParams) {
            switch (param) {
                case Constants.MAX_SPEED_REQUEST:
                    if (this.speedRange == Constants.PARAMETER_UNDEFINED) {
                        logger.fatal("CarGenerator: maxSpeed parameters are not properly defined.");
                        return false;
                    }
                    break;
                case Constants.MAX_ACCELERATION_REQUEST:
                    if (this.accelerationRange == Constants.PARAMETER_UNDEFINED) {
                        logger.fatal("CarGenerator: maxAcceleration parameters are not properly defined.");
                        return false;
                    }
                    break;
                case Constants.DECELERATION_COMFORT_REQUEST:
                    if (this.decelerationRange == Constants.PARAMETER_UNDEFINED) {
                        logger.fatal("CarGenerator: maxDeceleration parameters are not properly defined.");
                        return false;
                    }
                    break;
                case Constants.DESIRED_TIME_HEADWAY_REQUEST:
                    if (this.desiredTimeHeadwayRange == Constants.PARAMETER_UNDEFINED) {
                        logger.fatal("CarGenerator: desiredTimeHeadway parameters are not properly defined.");
                        return false;
                    }
                    break;
                case Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST:
                    if (this.minGapToNextCarRange == Constants.PARAMETER_UNDEFINED) {
                        logger.fatal("CarGenerator: minGapToNextCar parameters are not properly defined.");
                        return false;
                    }
                    break;
                default:
                    // Parameter does not require generation
                    break;
            }
        }

        return true;
    }

    public String toString() {
        return "CarGenerator{" + "minMaxSpeed: " + minMaxSpeed + ", maxMaxSpeed: " + maxMaxSpeed +
                ", minAcceleration: " + minAcceleration + ", maxAcceleration: " + maxAcceleration +
                ", minDeceleration: " + minDeceleration + ", maxDeceleration: " + maxDeceleration +
                ", minLength: " + minLength + ", maxLength: " + maxLength +
                ", minDesiredTimeHeadway: " + minDesiredTimeHeadway + ", maxDesiredTimeHeadway: " + maxDesiredTimeHeadway +
                ", minMinGapToNextCar: " + minMinGapToNextCar + ", maxMinGapToNextCar: " + maxMinGapToNextCar +
                ", type: '" + type + '\'' +
                '}';
    }


}
