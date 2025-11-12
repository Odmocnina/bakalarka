package models.carFollowingModels;

import models.ICarFollowingModel;

import java.util.HashMap;

public class KKW {




    public double getNewSpeed(HashMap<String, Double> parameters) {
        double freeSpeed = parameters.get("maxSpeed");
        double currentSpeed = parameters.get("currentSpeed");
        double distanceToNextCar = parameters.get("distanceToNextCar");



        return 0;

    }

    private int getSynchronizationGap(double currentSpeed, double distanceToNextCar) {
        return (int) Math.floor(currentSpeed / 10) + 1;
    }


    public String getName() {
        return "Kerner-Klenov-Wolf";
    }




    public String getID() {
        return "kkw";
    }

}
