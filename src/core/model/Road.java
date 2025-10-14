package core.model;

import java.util.HashMap;

public abstract class Road {

    protected double length;
    protected int numberOfLanes;
    protected double speedLimit;
    protected String type;
    protected CarGenerator generator;

    public Road(double length, int numberOfLanes, double speedLimit) {
        this.length = length;
        this.numberOfLanes = numberOfLanes;
        this.speedLimit = speedLimit;
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
        return "Road[length=" + length + ", numberOfLanes=" + numberOfLanes + ", speedLimit=" + speedLimit + "]";
    }

    public String getType() {
        return type;
    }

    public abstract Object getContent();

    public abstract void upadateRoad();

    public void setCarGenerator(CarGenerator generator) {
        this.generator = generator;
    }

}
