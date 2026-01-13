package core.model;

import core.utils.constants.Constants;

import core.utils.constants.RequestConstants;
import javafx.scene.paint.Color;
import java.util.HashMap;

/********************************
 * Class representing cars on the road, including their parameters in HashMap except for position parameters (xPosition
 * , lane), id and color which are stored separately for easier access
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************/
public class CarParams implements Cloneable {

    /** x position of the car on the road **/
    public double xPosition = Constants.PARAMETER_UNDEFINED;

    /** lane of the car on the road **/
    public int lane = (int) Constants.PARAMETER_UNDEFINED;

    /** other parameters of the car stored in a HashMap **/
    private HashMap<String, Double> parameters = new HashMap<>();

    /** unique id of the car **/
    public int id = (int) Constants.PARAMETER_UNDEFINED;

    /** color of the car **/
    public Color color = null;

    /** flag indicating if the car has been processed in the current simulation step **/
    public boolean processedInCurrentStep = false;

    /**
     * setter for parameter in the parameters HashMap
     *
     * @param key key of the parameter
     * @param value value of the parameter
     **/
    public void setParameter(String key, double value) {
        parameters.put(key, value);
    }

    /**
     * getter for parameter from the parameters HashMap
     *
     * @param key key of the parameter
     * @return value of the parameter, or Constants.PARAMETER_UNDEFINED if not found
     **/
    public double getParameter(String key) {
        return switch (key) {
            case RequestConstants.X_POSITION_REQUEST -> xPosition;
            case "lane" -> lane;
            case "id" -> id;
            default -> parameters.getOrDefault(key, Constants.PARAMETER_UNDEFINED);
        };
    }

    /**
     * toString method for CarParams
     *
     * @return string representation of CarParams
     **/
    @Override
    public String toString() {
        StringBuilder paramsString = new StringBuilder("CarParams{id=" + id + ", lane=" + lane + ", xPosition=" + xPosition + ", parameters={");
        for (String key : parameters.keySet()) {
            paramsString.append(key).append("=").append(parameters.get(key)).append(", ");
        }

        return paramsString + "}}";
    }

    /**
     * clone method for CarParams
     *
     * @return cloned CarParams object
     **/
    @Override
    public CarParams clone() {
        try {
            CarParams cloned = (CarParams) super.clone();
            cloned.parameters = new HashMap<>(this.parameters);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
