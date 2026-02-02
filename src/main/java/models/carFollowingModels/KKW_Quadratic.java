package models.carFollowingModels;

import models.ModelId;

@ModelId("kkw-quadratic")
public class KKW_Quadratic extends KKW_Linear {

    private double beta = 0.5;

    @Override
    protected int getSynchronizationGap(double currentSpeed, double d, double k, double timeStep) {
        int v = (int) (d + currentSpeed * timeStep + currentSpeed + beta * currentSpeed / (2 * super.acceleration));
        return v;
    }

    @Override
    public String getName() {
        return "Kerner-Klenov-Wolf (quadratic)";
    }

    @Override
    public String getID() {
        return "kkw-quadratic";
    }

}
