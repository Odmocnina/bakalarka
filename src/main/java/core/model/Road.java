package core.model;

import java.util.Queue;

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

    public Road(double length, int numberOfLanes, double speedLimit, String type) {
        this.length = length;
        this.numberOfLanes = numberOfLanes;
        this.speedLimit = speedLimit;
        this.type = type;
    }

    public double getLength() {
        return length;
    }

    public int getNumberOfLanes() {
        return numberOfLanes;
    }

    public double getSpeedLimit() {
        return speedLimit;
    }

    public String toString() {
        return "Road[length=" + length + ", numberOfLanes=" + numberOfLanes + ", speedLimit=" + speedLimit +
                ", type=" + type + "]";
    }

    public String getType() {
        return type;
    }

    public abstract Object getContent();

    public abstract int updateRoad();

    public abstract int getNumberOfCarsOnRoad();

    public void setCarGenerator(CarGenerator generator) {
        this.generator = generator;
    }

    public void initializeCarQueues() {
        carQueuesPerLane = new Queue[numberOfLanes];
        for (int i = 0; i < numberOfLanes; i++) {
            this.carQueuesPerLane[i] = this.generator.generateCarsInToQueue();
        }
    }

}
