package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import models.ICarFollowingModel;
import models.ModelId;

import java.util.HashMap;

@ModelId("test-model-3")
public class TestModel3 implements ICarFollowingModel {

    /** type of the model **/
    private final String type;

    /**
     * constructor for test model
     **/
    public TestModel3() {
        this.type = Constants.CONTINUOUS;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getID() {
        return "test-model-3";
    }


    @Override
    public double getCellSize() {
        return -1;
    }

    @Override
    public String requestParameters() {
        return "move";
    }

    @Override
    public String getParametersForGeneration() {
        return "move" + RequestConstants.REQUEST_SEPARATOR + RequestConstants.LENGTH_REQUEST;
    }

    @Override
    public String getName() {
        return "Test Model";
    }

    @Override
    public double getNewSpeed(HashMap<String, Double> parameters) {
        return parameters.get("move");
    }
}
