package models.laneChangingModels;

import core.model.Direction;
import models.ILaneChangingModel;

import java.util.HashMap;

/********************************************
 * MOBIL lane changing model class for deciding lane changes
 *
 * @author
 * @version 1.0
 *********************************************/
public class Mobil implements ILaneChangingModel {

    /**
     * gives the unique ID of the MOBIL model
     *
     * @return the unique ID of the MOBIL model
     **/
    @Override
    public String getID() {
        return "mobil";
    }

    /*
     * gives the list of parameters that the MOBIL model needs to make a decision
     *
     * @return the list of parameters that the MOBIL model needs to make a decision
     **/
    @Override
    public String requestParameters() {
        return "";
    }

    /**
     * function to request parameters needed for generation of MOBIL model
     *
     * @return String of requested parameters for generation
     **/
    @Override
    public String getParametersForGeneration() {
        return "";
    }

    /**
     * decides whether to change lane or not based on the MOBIL model
     *
     * @param parameters the parameters needed to make a decision in hashmap form, where key is the parameter name and
     *                   value is the parameter value in double
     * @return the direction to change lane or go straight
     **/
    @Override
    public Direction changeLaneIfDesired(HashMap<String, Double> parameters) {


        return Direction.STRAIGHT;
    }

    /**
     * function to get the name of the model
     *
     * @return name of the model as String
     **/
    @Override
    public String getName() {
        return "MOBIL";
    }

}
