package models;

import core.model.Direction;

import java.util.HashMap;

/********************************************
 * Interface for lane changing models, including methods for lane change decision and
 * parameter requests so that model implemented can be used in a generic way
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public interface ILaneChangingModel {

    /**
     * function to get ID of the model
     *
     * @return String of ID
     **/
    String getID();

    /**
     * function to request parameters needed for specific lane changing model
     *
     * @return String of requested parameters
     **/
    String requestParameters();

    /**
     * function to request parameters needed for specific lane changing model, with direction consideration (so when
     * changing to left lane, parameters for left lane change are requested)
     *
     * @return String of requested parameters
     **/
    String requestParameters(Direction direction);

    /**
     * function to request parameters needed for generation of specific lane changing model
     *
     * @return String of requested parameters for generation
     **/
    String getParametersForGeneration();

    /**
     * decides whether to change lane or not based on the specific lane changing model
     *
     * @param parameters the parameters needed to make a decision in hashmap form, where key is the parameter name and
     *                   value is the parameter value in double
     * @return the direction to change lane or go straight
     **/
    Direction changeLaneIfDesired(HashMap<String, Double> parameters);

    /**
     * function to get the name of the model
     *
     * @return name of the model as String
     **/
    String getName();
}
