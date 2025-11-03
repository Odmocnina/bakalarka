package models.laneChangingModels;

import core.model.Direction;
import models.ILaneChangingModel;

import java.util.HashMap;

public class Mobil implements ILaneChangingModel {

    @Override
    public String getID() {
        return "mobil";
    }

    @Override
    public String requestParameters() {
        return "";
    }

    @Override
    public Direction changeLaneIfDesired(HashMap<String, Double> parameters) {


        return Direction.STRAIGHT;
    }

    @Override
    public String getName() {
        return "MOBIL";
    }

}
