package models.carFollowingModels;

public class OVM_Different extends OVM_Original {

    private static final double DELTA = 5.0;

    @Override
    protected double optimalVelocity(double distance, double maxSpeedRoad, double minGap) {
        // d  = distance
        // dc = minGap
        double dcOverDelta = minGap / DELTA;

        double numerator = Math.tanh((distance - minGap) / DELTA) + Math.tanh(dcOverDelta);
        double denominator = 1.0 + Math.tanh(dcOverDelta);

        return maxSpeedRoad * (numerator / denominator);
    }

    @Override
    public String getName() {
        return "Optimal Velocity Model (different)";
    }

    @Override
    public String getID() {
        return "ovm-different";
    }

}
