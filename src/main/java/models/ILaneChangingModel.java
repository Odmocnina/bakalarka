package models;

import core.model.Direction;

import java.util.HashMap;

public interface ILaneChangingModel {

    public String getID();
    public String requestParameters();
    public Direction changeLaneIfDesired(HashMap<String, Double> parameters);
    public String getName();

}
