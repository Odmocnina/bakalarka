package core.model;

import app.AppContext;
import core.utils.Constants;

import core.utils.MyLogger;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class CarGenerator {
    private HashMap<String, Parameter> parameters = new HashMap<>();
    private String[] carGenerationParameters;
    private String type;
    private int id = 0;
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
            MyLogger.log("Unknown car generator type: " + this.type, Constants.WARN_FOR_LOGGING);
        }

        this.id++;
        return car;
    }

    private CarParams
    generateCarContinuous() {
        CarParams car = new CarParams();

        for (String key : carGenerationParameters) {
            double value = getParameterValueContinuous(key);
            car.setParameter(key, value);
        }

        car.color = colors[(int) (Math.random() * colors.length)];
        car.id = this.id;

        return car;
    }

    private CarParams generateCarCellular() {
        CarParams car = new CarParams();

        for (String key : carGenerationParameters) {
            // TODO: dynamic adding of parameters to car params, will be nessesery to change CarParams class, so that it
            // can hold dynamic set of parameters, via hashmap or similar structure
            double value = getParameterValueCellular(key);
            car.setParameter(key, value);
        }

        car.color = colors[(int) (Math.random() * colors.length)];
        car.id = this.id;

        return car;
    }

    private double getParameterValueContinuous(String key) {
        Parameter param = parameters.get(key);
        if (param != null) {
            if (param.range == 0) {
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

    public void addParameter(String key, Double minValue, Double maxValue) {
        Parameter param = new Parameter(minValue, maxValue);
        parameters.put(key, param);
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
        for (String key : parameters.keySet()) {
            Parameter param = parameters.get(key);
            param.minValue = Math.ceil(param.minValue / cellSize);
            param.maxValue = Math.ceil(param.maxValue / cellSize);
            param.range = param.maxValue - param.minValue;
            parameters.put(key, param);
        }
    }

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

    public boolean checkIfAllParametresAreLoaded() {
        String[] requiredParams = this.carGenerationParameters;

        if (requiredParams.length == 0) {
            MyLogger.log("CarGenerator: No parameters requested by the car following model."
                    , Constants.FATAL_FOR_LOGGING);
            return false;
        }

        for (String param : requiredParams) {
            Parameter p = parameters.get(param);

            if (p == null) {
                MyLogger.log("CarGenerator: Parameter " + param + " not set in car generator."
                        , Constants.FATAL_FOR_LOGGING);
                return false;
            }

            if (!p.checkIfValid()) {
                MyLogger.log("CarGenerator: Parameter " + param + " has invalid range: min=" + p.minValue +
                        ", max=" + p.maxValue, Constants.FATAL_FOR_LOGGING);
                return false;
            }

        }

        return true;
    }

    public void setCarGenerationParameters(String requestedParameters) {
        this.carGenerationParameters = requestedParameters.split(Constants.REQUEST_SEPARATOR);
    }

    public String[] getCarGenerationParameters() {
        return this.carGenerationParameters;
    }

    @Override
    public String toString() {
        String string = "CarGenerator{type=" + type + ", lambdaPerSec=" + lambdaPerSec + ", parameters=";
        for (String key : parameters.keySet()) {
            Parameter param = parameters.get(key);
            string += key + "=[min=" + param.minValue + ", max=" + param.maxValue + "], ";
        }

        return string + "}";

    }

    private class Parameter {
        double minValue;
        double maxValue;
        double range;

        private Parameter(double minValue, double maxValue) {
            this.maxValue = maxValue;
            this.minValue = minValue;
            this.range = maxValue - minValue;
        }

        private boolean checkIfValid() {
            return minValue <= maxValue && range >= 0;
        }
    }

    public boolean generatingToQueue() {
        Parameter p = parameters.get(Constants.GENERATOR_QUEUE);
        return p != null;
    }


}
