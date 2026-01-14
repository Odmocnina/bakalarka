package core.model;

import app.AppContext;
import core.utils.DefaultStuffMaker;
import core.utils.constants.Constants;
import core.utils.MyLogger;
import core.utils.constants.RequestConstants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/********************************
 * Abstract class representing a road, including its basic properties
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************/
public abstract class Road {

    /** length of road **/
    protected double length;

    /** number of lanes on road **/
    protected int numberOfLanes;

    /** speed limit on road **/
    protected double speedLimit;

    /** type of road, cellular, continuous... **/
    protected String type;

    /** car generators assigned to this lanes **/
    protected CarGenerator[] generators;

    /** queues of cars per lane **/
    protected Queue<CarParams>[] carQueuesPerLane = null;

    /** id of cars **/
    public int idOfCar = 0;

    /** light plans on lanes **/
    protected LightPlan[] lightPlansOnLanes;

    /**
     * constructor for road, initializing its basic properties, used in constructors of subclasses
     *
     * @param length length of road
     * @param numberOfLanes number of lanes on road
     * @param speedLimit speed limit on road
     * @param type type of road
     **/
    public Road(double length, int numberOfLanes, double speedLimit, String type) {
        this.length = length;
        this.numberOfLanes = numberOfLanes;
        this.speedLimit = speedLimit;
        this.type = type;
        LinkedList<CarGenerator> carGenerators = DefaultStuffMaker.createDefaultGenerator(numberOfLanes);
        this.generators = new CarGenerator[numberOfLanes];
        for (int i = 0; i < numberOfLanes; i++) {
            this.generators[i] = carGenerators.get(i);
        }
        LinkedList<LightPlan> lightPlans = DefaultStuffMaker.createDefaultLightPlan(numberOfLanes);
        this.lightPlansOnLanes = new LightPlan[numberOfLanes];
        for (int i = 0; i < numberOfLanes; i++) {
            this.lightPlansOnLanes[i] = lightPlans.get(i);
        }
    }

    /**
     * getter for length of road
     *
     * @return length of road
     */
    public double getLength() {
        return length;
    }

    /**
     * getter for number of lanes on road
     *
     * @return number of lanes on road
     */
    public int getNumberOfLanes() {
        return numberOfLanes;
    }

    /**
     * getter for speed limit on road
     *
     * @return speed limit on road
     */
    public double getSpeedLimit() {
        return speedLimit;
    }

    /**
     * toString method for road
     *
     * @return string representation of road
     **/
    @Override
    public String toString() {
        return "Road[length=" + length + ", numberOfLanes=" + numberOfLanes + ", speedLimit=" + speedLimit +
                ", type=" + type + "]";
    }

    /**
     * getter for type of road
     *
     * @return type of road
     **/
    public String getType() {
        return type;
    }

    /**
     * setter for car generator
     *
     * @param generator car generator
     **/
    public void setCarGenerators(CarGenerator generator) {
        this.generators = new CarGenerator[numberOfLanes];
        for (int i = 0; i < numberOfLanes; i++) {
            this.generators[i] = generator;
        }
    }

    public void setCarGenerators(CarGenerator[] generators) {
        this.generators = generators;
    }

    /**
     * method to initialize car queues per lane, called if generator generates to queues
     **/
    public void initializeCarQueues() {
        carQueuesPerLane = new Queue[numberOfLanes];
        for (int i = 0; i < numberOfLanes; i++) {
            this.carQueuesPerLane[i] = this.generators[i].generateCarsInToQueue();
        }
    }

    /**
     * method to check if all car queues are empty, if they are used
     *
     * @return true if all queues are empty, false otherwise
     **/
    public boolean areAllQueuesEmpty() {
        if (carQueuesPerLane == null) {
            return true;
        }
        for (Queue<CarParams> queue : carQueuesPerLane) {
            if (queue != null && !queue.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * method to get road simulation parameter connected to road, like max road speed, time step...
     *
     * @param parameters map to put parameter in
     * @param param parameter to get
     * @param car car parameters of car requesting parameter
     **/
    protected void getRoadSimulationParameter(HashMap<String, Double> parameters, String param, CarParams car) {
        if (param.equals(RequestConstants.TIME_STEP_REQUEST)) {
            parameters.put(param, AppContext.RUN_DETAILS.timeStep);
        } else if (param.equals(RequestConstants.MAX_ROAD_SPEED_REQUEST)) {
            double speedLimit;
            if (type.equals(Constants.CELLULAR)) {
                speedLimit = this.getSpeedLimit() / AppContext.CAR_FOLLOWING_MODEL.getCellSize();
                speedLimit = Math.ceil(speedLimit);
            } else {
                speedLimit = this.getSpeedLimit();
            }
            parameters.put(param, speedLimit);
        }
    }

    /**
     * method to try to add car to road from generator or queue
     **/
    protected void tryToAddCar() {
        for (int lane = 0; lane < numberOfLanes; lane++) {
            if (this.generators[lane].generatingToQueue()) {
                this.addFromQueue(lane);
            } else {
                this.addFromGenerator(lane);
            }
        }
    }

    /**
     * method to add car from queue to road if adding form queue is enabled and queue is not empty
     *
     * @param lane lane to add car to
     */
    protected void addFromQueue(int lane) {
        CarParams cp = this.carQueuesPerLane[lane].peek();

        if (cp != null && this.okToPutCarAtStart(cp, lane)) {
            cp.id = idOfCar;
            idOfCar++;
            this.placeCarAtStart(cp, (int) (cp.getParameter(RequestConstants.LENGTH_REQUEST)), lane);
            MyLogger.log("New car placed at lane " + lane + " position: " +
                            cp.getParameter(RequestConstants.LENGTH_REQUEST) + ", carParams: " + cp,
                    Constants.DEBUG_FOR_LOGGING);
            this.carQueuesPerLane[lane].poll();
        }
    }

    /**
     * method to add car from generator to road if adding from generator is enabled
     *
     * @param lane lane to add car to
     */
    protected void addFromGenerator(int lane) {
        if (this.generators[lane].decideIfNewCar()) {
            CarParams newCar = generators[lane].generateCar();

            if (newCar != null && this.okToPutCarAtStart(newCar, lane)) {
                newCar.id = idOfCar;
                idOfCar++;
                this.placeCarAtStart(newCar, (int) (newCar.getParameter(RequestConstants.LENGTH_REQUEST)), lane);
                MyLogger.log("New car placed at lane " + lane + " position: " +
                                newCar.getParameter(RequestConstants.LENGTH_REQUEST) + ", carParams: " + newCar,
                                Constants.DEBUG_FOR_LOGGING);
            }
        }
    }

    /**
     * abstract method to place car at start of road, implemented in subclasses
     *
     * @param car car parameters of car to place
     * @param length length of car
     * @param lane lane to place car in
     **/
    protected abstract void placeCarAtStart(CarParams car, double length, int lane);

    /**
     * abstract method to check if it is ok to put car at start of road, implemented in subclasses
     *
     * @param car car parameters of car to check
     * @param lane lane to check
     * @return true if it is ok to put car at start, false otherwise
     **/
    protected abstract boolean okToPutCarAtStart(CarParams car, int lane);

    /**
     * abstract method to get content of road, implemented in subclasses
     *
     * @return content of road in Object form, method then casts it to the correct type, so thread carefully
     **/
    public abstract Object getContent();

    /**
     * abstract method to update road state, implemented in subclasses
     *
     * @return number of cars updated on the road
     **/
    public abstract int updateRoad();

    /**
     * abstract method to get number of cars currently on the road
     *
     * @return number of cars on the road
     **/
    public abstract int getNumberOfCarsOnRoad();

    /**
     * method to check if the generator is generating to queue
     *
     * @return true if generating to queue, false otherwise
     **/
    public boolean generatingToQueue() {
        return this.generators[0].generatingToQueue();
    }

    /**
     * getter for car generator
     *
     * @return car generator
     **/
    public CarGenerator getCarGenerator() {
        return this.generators[0];
    }

    /**
     * method to check if the light on the given lane is green
     *
     * @param lane lane to check
     * @return true if light is green, false otherwise
     **/
    public boolean isLaneGreen(int lane) {
        if (lane < 0 || lane >= lightPlansOnLanes.length) {
            return true;
        }
        return lightPlansOnLanes[lane].isGreen();
    }

    /**
     * method to update lights on the road based on current time
     *
     * @param currentTime current simulation time
     **/
    public void updateLights(int currentTime) {
        for (LightPlan lp : lightPlansOnLanes) {
            lp.tryToSwitchLight(currentTime);
        }
    }

    /**
     * setter for light plan on lane
     *
     * @param lightPlans light plans to set
     **/
    public void setLightPlan(LightPlan[] lightPlans) {
        this.lightPlansOnLanes = lightPlans;
    }

    /**
     * setter for light plan on lane
     *
     * @param lane lane to set light plan on
     * @param lightPlan light plan to set
     **/
    public void setLightPlan(int lane, LightPlan lightPlan) {
        if (lane >= 0 && lane < lightPlansOnLanes.length) {
            this.lightPlansOnLanes[lane] = lightPlan;
        }
    }

    /**
     * setter for road generator
     *
     * @param generator car generator
     **/
    public void setRoadGenerator(int lane, CarGenerator generator) {
        if (lane >= 0 && lane < generators.length) {
            this.generators[lane] = generator;
        }
    }

}
