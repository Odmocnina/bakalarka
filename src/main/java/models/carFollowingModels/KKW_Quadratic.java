package models.carFollowingModels;

import models.ModelId;

/********************************************
 * Kerner-Klenov-Wolf (quadratic) car following model implementation (discrete), it extends KKW_Linear and adds a
 * quadratic term to the calculation of synchronization gap, annotated with @ModelId("kkw-quadratic") for identification
 * during reflexive loading
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
@ModelId("kkw-quadratic")
public class KKW_Quadratic extends KKW_Linear {

    /** coefficient for the quadratic term in the synchronization gap calculation **/
    private double beta = 0.5;

    /**
     * function to calculate the synchronization gap based on the KKW quadratic model
     *
     * @param currentSpeed current speed of the car
     * @param d minimum gap
     * @param k synchronization coefficient
     * @param timeStep time step for the calculation
     * @return synchronization gap as an integer
     **/
    @Override
    protected int getSynchronizationGap(double currentSpeed, double d, double k, double timeStep) {
        return (int) (d + currentSpeed * timeStep + currentSpeed + beta * currentSpeed / (2 * super.acceleration));
    }

    /**
     * function to get the name of the model, used for display and logging purposes
     *
     * @return name of the model as String
     **/
    @Override
    public String getName() {
        return "Kerner-Klenov-Wolf (quadratic)";
    }

    /**
     * function to get the ID of the model, used for identification during reflexive loading
     *
     * @return ID of the model as String
     **/
    @Override
    public String getID() {
        return "kkw-quadratic";
    }

}
