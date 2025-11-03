package models;

import java.util.HashMap;

public interface ICarFollowingModel {

    public double getNewSpeed(HashMap<String, Double> parameters);

    public String getID();

    public String getType();

    public double getCellSize();

    public String requestParameters();
    public String getName();

}
