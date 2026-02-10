package models.carFollowingModels;

import models.ModelId;

/********************************************
 * Optimal Velocity Model (different) car following model implementation (continuous), it extends OVM_Original and uses
 * a different formula for calculating optimal velocity, annotated with @ModelId("ovm-different") for identification
 * during reflexive loading
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
@ModelId("ovm-different")
public class OVM_Different extends OVM_Original {

    /** parameter for the optimal velocity function, it determines how quickly the optimal velocity changes with
     * distance **/
    private static final double DELTA = 5.0;

    /**
     * function to calculate optimal velocity based on the OVM different model
     *
     * @param distance distance to the next car
     * @param maxSpeedRoad maximum speed allowed on the road
     * @param minGap minimum gap to the next car
     * @return optimal velocity as a double
     **/
    @Override
    protected double optimalVelocity(double distance, double maxSpeedRoad, double minGap) {
        // d  = distance
        // dc = minGap
        double dcOverDelta = minGap / DELTA;

        double numerator = Math.tanh((distance - minGap) / DELTA) + Math.tanh(dcOverDelta);
        double denominator = 1.0 + Math.tanh(dcOverDelta);

        return maxSpeedRoad * (numerator / denominator);
    }

    /**
     * function to get the name of the model, used for display and logging purposes
     *
     * @return name of the model as String
     **/
    @Override
    public String getName() {
        return "Optimal Velocity Model (different)";
    }

    /**
     * function to get the ID of the model, used for identification during reflexive loading
     *
     * @return ID of the model as String
     **/
    @Override
    public String getID() {
        return "ovm-different";
    }

}
