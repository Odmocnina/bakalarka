package core.model;

import core.model.cellular.CellularRoad;
import core.utils.constants.Constants;
import core.utils.MyLogger;
import core.utils.constants.RequestConstants;

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
public class CarGenerator implements Cloneable {

    /** parameters need for car generation, which cars need for models (for example model needs, politeness factor ->
     politeness factor will be in here, and it will be generated, if it is not necessary it will not be here and won't
     be generated **/
    private HashMap<String, Parameter> parameters = new HashMap<>();

    /** parameters for communication with ui, user, map file... these parameters are not used for generation, but they
     * are used for communication with ui, user, map file... they are copied to real parameters (and translated if
     * cellular models are used) used for generation when necessary, is here so that the translation is not happening
     * multiple times **/
    private HashMap<String, Parameter> parametersForCommunication = new HashMap<>();

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

    /** whether to generate cars into queue or one by one **/
    private boolean useQueue = false;

    /** minimum queue size for queue generation **/
    private int minQueueSize = 0;

    /** maximum queue size for queue generation **/
    private int maxQueueSize = 0;

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
        } else if (this.type.equals(Constants.CONTINUOUS)) {
            car = generateCarContinuous();
        } else {
            MyLogger.logLoadingOrSimulationStartEnd("Unknown car generator type: " + this.type, Constants.WARN_FOR_LOGGING);
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
     * function to add parameter to real parameters used for generation
     *
     * @param key parameter key
     * @param minValue minimum value
     * @param maxValue maximum value
     **/
    public void addParameter(String key, String name, Double minValue, Double maxValue) {
        Parameter param = new Parameter(name, minValue, maxValue);
        parameters.put(key, param);
    }

    /**
     * function to set road type for generator, used to translate parameters if necessary
     *
     * @param road road the generator is assigned to
     **/
    public void setType(Road road) {
        String newType = road.getType();
        // check if type is already cellular, if so no need to translate again, because it would make everything smaller
        if (this.type != null && this.type.equals(Constants.CELLULAR) && newType.equals(Constants.CELLULAR)) {
            return;
        }

        this.type = newType;
        if (newType.equals(Constants.CELLULAR)) {
            double cellSize = ((CellularRoad) road).getCellSize();
            translateParametersToCellular(cellSize);
        }
    }

    /**
     * function to set road type for generator, used to translate parameters if necessary
     *
     * @param type type of road the generator is assigned to
     * @param cellSize size of cell in cellular road
     **/
    public void setType(String type, double cellSize) {
        this.type = type;
        if (type.equals(Constants.CELLULAR)) {
            translateParametersToCellular(cellSize);
        }
    }

    /**
     * function to set road type for generator, used to translate parameters if necessary
     *
     * @param type type of road the generator is assigned to
     **/
    public void setType(String type) {
        this.type = type;
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
        int range = maxQueueSize - minQueueSize;
        int numberOfCars = rand.nextInt(range + 1) + minQueueSize;

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
            MyLogger.logLoadingOrSimulationStartEnd("CarGenerator: No parameters requested by the car following model."
                    , Constants.FATAL_FOR_LOGGING);
            return false;
        }

        for (String param : requiredParams) {
            Parameter p = parameters.get(param);

            if (p == null) {
                MyLogger.logLoadingOrSimulationStartEnd("CarGenerator: Parameter " + param + " not set in car generator."
                        , Constants.FATAL_FOR_LOGGING);
                return false;
            }

            if (!p.checkIfValid()) {
                MyLogger.logLoadingOrSimulationStartEnd("CarGenerator: Parameter " + param + " has invalid range: min=" + p.minValue +
                        ", max=" + p.maxValue, Constants.FATAL_FOR_LOGGING);
                return false;
            }

        }

        return true;
    }

    /**
     * function to get missing parameters that are required for generation, used for logging purposes to inform user
     * about missing parameters in generator settings
     *
     * @return String of missing parameters separated by Constants.REQUEST_SEPARATOR
     **/
    public String getMissingParameters() {
        String[] requiredParams = this.carGenerationParameters;
        StringBuilder missingParameters = new StringBuilder();

        if (requiredParams.length == 0) {
            MyLogger.logLoadingOrSimulationStartEnd("CarGenerator: No parameters requested by the car following model."
                    , Constants.FATAL_FOR_LOGGING);
            return missingParameters.toString();
        }

        for (String param : requiredParams) {
            Parameter p = parametersForCommunication.get(param);

            if (p == null) {
                MyLogger.logLoadingOrSimulationStartEnd("CarGenerator: Parameter " + param + " not set in car generator."
                        , Constants.FATAL_FOR_LOGGING);
                missingParameters.append(param).append(RequestConstants.REQUEST_SEPARATOR);
            } else if (!p.checkIfValid()) {
                MyLogger.logLoadingOrSimulationStartEnd("CarGenerator: Parameter " + param + " has invalid range: min=" + p.minValue +
                        ", max=" + p.maxValue, Constants.FATAL_FOR_LOGGING);
                missingParameters.append(param).append(RequestConstants.REQUEST_SEPARATOR);
            }

        }

        return missingParameters.toString();
    }

    /**
     * function to remove parameter from generator settings (real parameters used for generation)
     *
     * @param key parameter key
     **/
    public void removeParameter(String key) {
        this.parameters.remove(key);
    }

    /** getter for flow rate (lambda parameter)
     *
     * @return double flow rate (cars per second)
     **/
    public double getFlowRate() {
        return this.lambdaPerSec;
    }

    /** setter for flow rate (lambda parameter)
     *
     * @param flowRate flow rate (cars per second)
     **/
    public void setFlowRate(double flowRate) {
        this.lambdaPerSec = flowRate;
    }

    /** function to get all parameters in generator settings
     *
     * @return HashMap<String, Parameter> all parameters in generator settings
     **/
    public HashMap<String, Parameter> getAllParameters() {
        return this.parameters;
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
        return this.useQueue;
    }

    /**
     * toString method for CarGenerator
     *
     * @return String representation of CarGenerator in String
     **/
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("CarGenerator{type=" + type + ", lambdaPerSec=" + lambdaPerSec +
                ", useQueue=" + useQueue + ", minQueueSize=" + minQueueSize + ", maxQueueSize=" + maxQueueSize +
                ", parameters=");
        for (String key : parameters.keySet()) {
            Parameter param = parameters.get(key);
            string.append(key).append("=[min=").append(param.minValue).append(", max=").append(param.maxValue).append("], ");
        }

        return string + "}";
    }

    /**
     * clone method for CarGenerator
     *
     * @return CarGenerator cloned CarGenerator object
     **/
    @Override
    public CarGenerator clone() {
        CarGenerator copy = new CarGenerator(this.lambdaPerSec);
        copy.type = this.type;
        copy.id = this.id;
        copy.allowMultiplePerTick = this.allowMultiplePerTick;
        copy.carGenerationParameters = this.carGenerationParameters;
        copy.setFlowRate(this.getFlowRate());
        copy.setQueueSize(this.minQueueSize, this.maxQueueSize);
        if (!this.useQueue) {
            copy.disableQueue();
        }
        for (String key : this.parameters.keySet()) {
            Parameter param = this.parameters.get(key);
            copy.parameters.put(key, new Parameter(param.name, param.minValue, param.maxValue));
        }
        for (String key : this.parametersForCommunication.keySet()) {
            Parameter param = this.parametersForCommunication.get(key);
            copy.parametersForCommunication.put(key, new Parameter(param.name, param.minValue, param.maxValue));
        }
        return copy;
    }

    /**
     * function to check if parameter exists in generator settings
     *
     * @param key parameter key
     * @return boolean whether parameter exists
     **/
    public boolean keyExists(String key) {
        return parameters.containsKey(key);
    }

    /**
     * getter for lambda parameter of generator
     *
     * @return double lambda parameter (cars per second)
     **/
    public double getLambdaPerSec() {
        return this.lambdaPerSec;
    }

    /**
     * function to check if all parameters in generator settings are valid
     *
     * @return boolean whether all parameters are valid
     **/
    public boolean areAllParametersOk() {
        for (String key : parameters.keySet()) {
            Parameter p = parameters.get(key);
            if (!p.checkIfValid()) {
                return false;
            }
        }

        return true;
    }

    /**
     * function to check if generator is legitimate (all parameters valid)
     *
     * @return boolean whether generator is legitimate
     **/
    public boolean isLegitimate() {
        for (String key : parameters.keySet()) {
            Parameter p = parameters.get(key);
            if (!p.checkIfValid()) {
                return false;
            }
        }

        return true;
    }

    public void copyComParametersToRealParameters(String type, double cellSize) {
        for (String key : parametersForCommunication.keySet()) {
            Parameter param = parametersForCommunication.get(key);
            parameters.put(key, new Parameter(param.name, param.minValue, param.maxValue));
        }

        if (type.equals(Constants.CELLULAR)) {
            translateParametersToCellular(cellSize);
        }
    }

    public void addComParameter(String key, String name, Double minValue, Double maxValue) {
        Parameter param = new Parameter(name, minValue, maxValue);
        parametersForCommunication.put(key, param);
    }

    /**
     *  function to remove parameter from communication parameters (parameters used for communication with ui, user,
     *  map file...)
     *
     * @param key parameter key
     **/
    public void removeComParameter(String key) {
        this.parametersForCommunication.remove(key);
    }

    /**
     * function to get all communication parameters (parameters used for communication with ui, user, map file...)
     *
     * @return HashMap<String, Parameter> all communication parameters
     **/
    public HashMap<String, Parameter> getAllComParameters() {
        return this.parametersForCommunication;
    }

    /**
     * function to set queue generation settings for generator if generation in queue is desired, if not, generation
     * will be one by one
     *
     * @param minSize minimum queue size
     * @param maxSize maximum queue size
     **/
    public void setQueueSize(int minSize, int maxSize) {
        this.useQueue = true;
        this.minQueueSize = minSize;
        this.maxQueueSize = maxSize;
    }

    /**
     * function to disable queue generation and set generation to one by one
     **/
    public void disableQueue() {
        this.useQueue = false;
    }

    /**
     * getter for minimum queue size for queue generation
     *
     * @return int minimum queue size
     **/
    public int getMinQueueSize() {
        return this.minQueueSize;
    }

    /**
     * getter for maximum queue size for queue generation
     *
     * @return int maximum queue size
     **/
    public int getMaxQueueSize() {
        return this.maxQueueSize;
    }
}
