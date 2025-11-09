package models;

import java.util.HashMap;

/********************************************
 * Interface for car following models, both continuous and cellular, including methods for speed calculation and
 * parameter requests so that model implemented can be used in a generic way
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public interface ICarFollowingModel {

    /**
     * function to get new speed based on specific car following model algorithm
     *
     * @param parameters HashMap of parameters needed for calculation
     * @return new speed as double
     **/
    double getNewSpeed(HashMap<String, Double> parameters);

    /**
     * function to get ID of the model
     *
     * @return String of ID
     **/
    String getID();

    /**
     * function to get type of the model
     *
     * @return String of type
     **/
    String getType();

    /**
     * function to get cell size for cellular models
     *
     * @return double of cell size in meters
     **/
    double getCellSize();

    /**
     * function to request parameters needed for specific car following model
     *
     * @return String of requested parameters
     **/
    String requestParameters();

    /**
     * function to request parameters needed for generation of specific car following model
     *
     * @return String of requested parameters for generation
     **/
    String getParametersForGeneration();

    /**
     * function to get the name of the model
     *
     * @return name of the model as String
     **/
    String getName();
}
