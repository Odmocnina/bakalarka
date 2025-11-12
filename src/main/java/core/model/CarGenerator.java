package core.model;

import core.model.cellular.CellularRoad;
import core.utils.Constants;
import core.utils.MyLogger;

import core.utils.RequestConstants;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;

/********************************************
 * class representing a car generator, generates cars in queue or one by one. One by one generation is dependent on
 * lambda parameter, exponential distribution used to model inter-arrival times.
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class CarGenerator {

    /** parameters need for car generation, which cars need for models (for example model needs, politeness factor ->
     politeness factor will be in here, and it will be generated, if it is not necessary it will not be here and won't
     be generated **/
    private HashMap<String, Parameter> parameters = new HashMap<>();

    /** parameters requested by car following model, needed for generation **/
    private String[] carGenerationParameters;

    /** type of road the generator is assigned to **/
    private String type;

    /** unique id for generated cars **/
    private int id = 0;

    /** lambda parameter for exponential distribution of inter-arrival times **/
    private double lambdaPerSec;

    /** time to next arrival **/
    private double timeToNext = Double.NaN;

    /** random number generator **/
    private final Random RNG = new Random();

    /** allow multiple cars to be generated per tick **/
    private boolean allowMultiplePerTick = false;

    /** available car colors **/
    private final Color[] COLORS = Constants.CAR_COLORS;

    /**
     * constructor for car generator
     *
     * @param density lambda parameter for exponential distribution of inter-arrival times (cars per second)
     **/
    public CarGenerator(double density) {
        this.lambdaPerSec = Math.max(0.0, density); // lambda parameter for exponential distribution
    }

    /**
     * function to decide if new car should be generated this tick based on Bernoulli process, for single car per tick
     * generation
     *
     * @return boolean whether new car should be generated this tick
     **/
    private boolean decideBernoulliTick() {
        if (lambdaPerSec <= 0) {
            return false;
        }
        double p = 1.0 - Math.exp(-lambdaPerSec);
        return RNG.nextDouble() < p;
    }

    /**
     * function to schedule next arrival time based on exponential distribution
     **/
    private void scheduleNext() {
        if (lambdaPerSec <= 0) {
            timeToNext = Double.POSITIVE_INFINITY;
        } else {
            // T = -ln(U)/λ, U ~ U(0,1]
            double u = 1.0 - RNG.nextDouble();
            timeToNext = -Math.log(u) / lambdaPerSec;
        }
    }

    /**
     * function to determine number of arrivals this tick based on exponential distribution
     *
     * @return number of arrivals this tick
     **/
    public int arrivalsThisTick() {
        int count = 0;
        timeToNext -= 1.0; // assuming tick duration is 1 second
        while (timeToNext <= 0.0) {
            count++;
            double overshoot = -timeToNext;
            scheduleNext();
            timeToNext -= overshoot;
        }
        return count;
    }

    /**
     * function to decide if new car should be generated this tick
     *
     * @return boolean whether new car should be generated this tick
     **/
    public boolean decideIfNewCar() {
        if (allowMultiplePerTick) {
            return arrivalsThisTick() > 0;
        } else {
            return decideBernoulliTick();
        }
    }

    /**
     * function to generate a new car with parameters based on generator settings
     *
     * @return CarParams generated car parameters
     **/
    public CarParams generateCar() {
        CarParams car = null;

        if (this.type.equals(Constants.CELLULAR)) {
            car = generateCarCellular();
        } else if (this.type.equals(Constants.CONTINOUS)) {
            car = generateCarContinuous();
        } else {
            MyLogger.logBeforeLoading("Unknown car generator type: " + this.type, Constants.WARN_FOR_LOGGING);
        }

        this.id++;
        return car;
    }

    /**
     * function to generate a new car with parameters based on generator settings, continuous road
     *
     * @return CarParams generated car parameters
     **/
    private CarParams generateCarContinuous() {
        CarParams car = new CarParams();

        for (String key : carGenerationParameters) {
            double value = getParameterValueContinuous(key);
            car.setParameter(key, value);
        }

        car.color = COLORS[(int) (Math.random() * COLORS.length)];
        car.id = this.id;

        return car;
    }

    /**
     * function to generate a new car with parameters based on generator settings, cellular road
     *
     * @return CarParams generated car parameters
     **/
    private CarParams generateCarCellular() {
        CarParams car = new CarParams();

        for (String key : carGenerationParameters) {
            double value = getParameterValueCellular(key);
            car.setParameter(key, value);
        }

        car.color = COLORS[(int) (Math.random() * COLORS.length)];
        car.id = this.id;

        return car;
    }

    /**
     * function to get parameter value based on generator settings, continuous road (parameters are double)
     *
     * @param key parameter key
     * @return double parameter value
     **/
    private double getParameterValueContinuous(String key) {
        Parameter param = parameters.get(key);
        if (param != null) {
            if (param.range == 0) { // no range, fixed value
                return param.minValue;
            } else {
                Random rand = new Random();
                return param.minValue + (rand.nextDouble() * param.range);
            }
        } else {
            MyLogger.log("Parameter " + key + " not found in generator parameters.", Constants.WARN_FOR_LOGGING);
            return Double.NaN;
        }
    }

    /**
     * function to get parameter value based on generator settings, cellular road (parameters are int)
     *
     * @param key parameter key
     * @return int parameter value
     **/
    private int getParameterValueCellular(String key) {
        Parameter param = parameters.get(key);
        if (param != null) {
            if (param.range == 0) {
                return (int) param.minValue;
            } else {
                Random rand = new Random();
                return (int) (param.minValue + rand.nextInt((int) param.range));
            }
        } else {
            MyLogger.log("Parameter " + key + " not found in generator parameters."
                    , Constants.WARN_FOR_LOGGING);
            return (int) Constants.PARAMETER_UNDEFINED;
        }
    }

    /**
     * function to add parameter range to generator settings
     *
     * @param key parameter key
     * @param minValue minimum value
     * @param maxValue maximum value
     **/
    public void addParameter(String key, Double minValue, Double maxValue) {
        Parameter param = new Parameter(minValue, maxValue);
        parameters.put(key, param);
    }

    /**
     * function to set road type for generator, used to translate parameters if necessary
     *
     * @param road road the generator is assigned to
     **/
    public void setType(Road road) {
        String type = road.getType();
        this.type = type;
        if (type.equals(Constants.CELLULAR)) {
            double cellSize = ((CellularRoad) road).getCellSize();
            translateParametersToCellular(cellSize);
        }
    }

    /**
     * function to set whether multiple cars can be generated per tick
     *
     * @param allow boolean whether multiple cars can be generated per tick
     **/
    public void setAllowMultiplePerTick(boolean allow) {
        this.allowMultiplePerTick = allow;
    }

    /**
     * function to set lambda parameter for exponential distribution of inter-arrival times
     *
     * @param lambda lambda parameter (cars per second)
     **/
    public void setLambdaPerSec(double lambda) {
        this.lambdaPerSec = Math.max(0.0, lambda);
        scheduleNext();
    }

    /**
     * function to translate parameters from continuous to cellular road if cellular road is used
     *
     * @param cellSize size of cell in cellular road
     **/
    private void translateParametersToCellular(double cellSize) {
        for (String key : parameters.keySet()) {
            Parameter param = parameters.get(key);
            param.minValue = Math.ceil(param.minValue / cellSize);
            param.maxValue = Math.ceil(param.maxValue / cellSize);
            param.range = param.maxValue - param.minValue;
            parameters.put(key, param);
        }
    }

    /**
     * function to generate multiple cars into a queue based on generator settings
     *
     * @return Queue<CarParams> queue of generated cars
     **/
    public Queue<CarParams> generateCarsInToQueue() {
        Queue<CarParams> queue = new java.util.LinkedList<>();
        Random rand = new Random();
        Parameter p = parameters.get(Constants.GENERATOR_QUEUE);
        int minNumberOfCars = (int) p.minValue;
        int range = (int) p.range;
        int numberOfCars = rand.nextInt(range + 1) + minNumberOfCars;

        while (queue.size() < numberOfCars) {
            CarParams car = generateCar();
            queue.add(car);
        }

        return queue;
    }

    /**
     * function to check if all required parameters are loaded in generator and are valid, if not simulation cannot
     * start, because models will not have all necessary parameters for their calculations
     *
     * @return boolean whether all required parameters are loaded
     **/
    public boolean checkIfAllParametersAreLoaded() {
        String[] requiredParams = this.carGenerationParameters;

        if (requiredParams.length == 0) {
            MyLogger.logBeforeLoading("CarGenerator: No parameters requested by the car following model."
                    , Constants.FATAL_FOR_LOGGING);
            return false;
        }

        for (String param : requiredParams) {
            Parameter p = parameters.get(param);

            if (p == null) {
                MyLogger.logBeforeLoading("CarGenerator: Parameter " + param + " not set in car generator."
                        , Constants.FATAL_FOR_LOGGING);
                return false;
            }

            if (!p.checkIfValid()) {
                MyLogger.logBeforeLoading("CarGenerator: Parameter " + param + " has invalid range: min=" + p.minValue +
                        ", max=" + p.maxValue, Constants.FATAL_FOR_LOGGING);
                return false;
            }

        }

        return true;
    }

    /**
     * function to set parameters requested by car following model, needed for generation
     *
     * @param requestedParameters string of requested parameters separated by Constants.REQUEST_SEPARATOR
     **/
    public void setCarGenerationParameters(String requestedParameters) {
        this.carGenerationParameters = requestedParameters.split(RequestConstants.REQUEST_SEPARATOR);
    }

    /**
     * function to get parameters requested by car following model, needed for generation
     *
     * @return String[] array of requested parameters
     **/
    public String[] getCarGenerationParameters() {
        return this.carGenerationParameters;
    }

/**
     * function to check if generator is set to generate cars into queue
     *
     * @return boolean whether generator generates cars into queue
     **/
    public boolean generatingToQueue() {
        Parameter p = parameters.get(Constants.GENERATOR_QUEUE);
        return p != null;
    }

    /**
     * toString method for CarGenerator
     *
     * @return String representation of CarGenerator in String
     **/
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("CarGenerator{type=" + type + ", lambdaPerSec=" + lambdaPerSec + ", parameters=");
        for (String key : parameters.keySet()) {
            Parameter param = parameters.get(key);
            string.append(key).append("=[min=").append(param.minValue).append(", max=").append(param.maxValue).append("], ");
        }

        return string + "}";
    }

    /*********************
     * class representing a parameter with min and max values
     *
     * @author Michael Hladky
     * @version 1.0
     ********************/
    private static class Parameter {

        /** minimum value of parameter **/
        double minValue;

        /** maximum value of parameter **/
        double maxValue;

        /** range of parameter **/
        double range;

        /**
         * constructor for parameter
         *
         * @param minValue minimum value of parameter
         * @param maxValue maximum value of parameter
         */
        private Parameter(double minValue, double maxValue) {
            this.maxValue = maxValue;
            this.minValue = minValue;
            this.range = maxValue - minValue;
        }

        /**
         * function to check if parameter range is valid
         *
         * @return boolean whether parameter range is valid
         */
        private boolean checkIfValid() {
            return minValue <= maxValue && range >= 0;
        }
    }
}
