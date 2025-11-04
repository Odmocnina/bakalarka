package core.model;

import core.utils.Constants;

import javafx.scene.paint.Color;

import java.util.HashMap;

public class CarParams {

    public double xPosition = Constants.PARAMETER_UNDEFINED;
    public int lane = (int) Constants.PARAMETER_UNDEFINED;
    private HashMap<String, Double> parameters = new HashMap<>();
    public int id = (int) Constants.PARAMETER_UNDEFINED;
    public Color color = null;
    public boolean isProcessed = false;

    public void setParameter(String key, double value) {
        parameters.put(key, value);
    }

    public double getParameter(String key) {
        return parameters.getOrDefault(key, Constants.PARAMETER_UNDEFINED);
    }

    public String toString() {
        String paramsString = "CarParams{id=" + id + ", lane=" + lane + ", xPosition=" + xPosition + ", parameters={";
        for (String key : parameters.keySet()) {
            paramsString += key + "=" + parameters.get(key) + ", ";
        }

        return paramsString + "}}";
    }

}
