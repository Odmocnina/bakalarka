package core.model;

import app.AppContext;
import core.utils.RequestConstants;

import java.util.HashMap;
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

    /** car generator assigned to this road **/
    protected CarGenerator generator;

    /** queues of cars per lane **/
    protected Queue<CarParams>[] carQueuesPerLane = null;

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
    public void setCarGenerator(CarGenerator generator) {
        this.generator = generator;
    }

    /**
     * method to initialize car queues per lane, called if generator generates to queues
     **/
    public void initializeCarQueues() {
        carQueuesPerLane = new Queue[numberOfLanes];
        for (int i = 0; i < numberOfLanes; i++) {
            this.carQueuesPerLane[i] = this.generator.generateCarsInToQueue();
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

    protected void getRoadSimulationParameter(HashMap<String, Double> parameters, String param, CarParams car) {
        if (param.equals(RequestConstants.TIME_STEP_REQUEST)) {
            parameters.put(param, AppContext.RUN_DETAILS.timeStep);
        } else if (param.equals(RequestConstants.MAX_ROAD_SPEED_REQUEST)) {
            parameters.put(param, speedLimit);
        }
    }

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

    public boolean generatingToQueue() {
        return this.generator.generatingToQueue();
    }

    public CarGenerator getCarGenerator() {
        return this.generator;
    }




}
