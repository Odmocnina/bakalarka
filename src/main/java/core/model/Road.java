package core.model;

import java.util.Queue;

public abstract class Road {

    protected double length;
    protected int numberOfLanes;
    protected double speedLimit;
    protected String type;
    protected CarGenerator generator;
    protected Queue<CarParams>[] carQueuesPerLane;

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

    private void initializeCarQueues() {
        carQueuesPerLane = new Queue[numberOfLanes];
        for (int i = 0; i < numberOfLanes; i++) {
            //carQueuesPerLane[i] = generator.generateCarsInToQueue()
        }
    }

}
