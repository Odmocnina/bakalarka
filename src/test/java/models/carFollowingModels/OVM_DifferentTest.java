package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class OVM_DifferentTest {

    /**
     * Helper subclass to expose the protected optimalVelocity() method.
     */
    private static class TestOVM_Different extends OVM_Different {
        public double callOptimalVelocity(double distance, double maxSpeedRoad, double minGap) {
            return optimalVelocity(distance, maxSpeedRoad, minGap);
        }
    }

    /**
     * Helper method: construct a minimal parameter set for getNewSpeed tests.
     */
    private HashMap<String, Double> baseParams() {
        HashMap<String, Double> p = new HashMap<>();

        p.put(RequestConstants.CURRENT_SPEED_REQUEST, 10.0);
        p.put(RequestConstants.MAX_SPEED_REQUEST, 30.0);
        p.put(RequestConstants.X_POSITION_REQUEST, 100.0);
        p.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 130.0); // gap 130 - 100 - 4 = 26
        p.put(RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.5);
        p.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 30.0);
        p.put(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 4.0);
        p.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 4.0);
        p.put(RequestConstants.LENGTH_REQUEST, 4.0);

        return p;
    }

    /***************************************************************************
     * TESTS FOR optimalVelocity()
     **************************************************************************/

    /**
     * Test: optimalVelocity follows exact implemented formula.
     */
    @Test
    void optimalVelocity_matchesFormula() {
        TestOVM_Different model = new TestOVM_Different();

        double distance = 25.0;
        double maxSpeedRoad = 30.0;
        double minGap = 4.0;
        double DELTA = 5.0;

        double dcOverDelta = minGap / DELTA;

        double expected =
                maxSpeedRoad *
                        ( (Math.tanh((distance - minGap) / DELTA) + Math.tanh(dcOverDelta))
                                / (1.0 + Math.tanh(dcOverDelta)) );

        double result = model.callOptimalVelocity(distance, maxSpeedRoad, minGap);

        assertEquals(expected, result, 1e-12,
                "optimalVelocity must follow the OVM_Different restricted tanh formula.");
    }

    /**
     * Test: extremely large distance should give velocity close to maxSpeedRoad.
     */
    @Test
    void optimalVelocity_largeDistance_approachesMaxSpeed() {
        TestOVM_Different model = new TestOVM_Different();

        double result = model.callOptimalVelocity(1e9, 25.0, 3.0);
        assertTrue(result > 24.0 && result <= 25.0,
                "optimalVelocity should approach maxSpeedRoad for huge distance.");
    }

    /**
     * Test: distance equal to minGap should give low or near-zero velocity.
     */
    @Test
    void optimalVelocity_distanceEqualsMinGap() {
        TestOVM_Different model = new TestOVM_Different();

        double minGap = 5.0;
        double result = model.callOptimalVelocity(minGap, 30.0, minGap);

        // tanh( (minGap - minGap)/DELTA ) = tanh(0) = 0
        // numerator = tanh(0) + tanh(minGap/DELTA)
        // denominator = 1 + tanh(minGap/DELTA)
        // result = maxSpeed * (tanh(minGap/DELTA) / (1 + tanh(minGap/DELTA)))
        // → some small positive, definitely below half max
        assertTrue(result >= 0.0 && result < 15.0,
                "When distance = minGap, optimalVelocity should be significantly lower than max speed.");
    }

    /***************************************************************************
     * TESTS FOR getNewSpeed()
     **************************************************************************/

    /**
     * Test: getNewSpeed must use the overridden optimalVelocity().
     *
     * We compute expected speed using the OVM_Different formula manually.
     */
    @Test
    void getNewSpeed_usesDifferentOptimalVelocity() {
        OVM_Different model = new OVM_Different();
        HashMap<String, Double> p = baseParams();

        double currentSpeed = p.get(RequestConstants.CURRENT_SPEED_REQUEST);
        double x = p.get(RequestConstants.X_POSITION_REQUEST);
        double xL = p.get(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST);
        double lengthL = p.get(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST);
        double distance = xL - x - lengthL;

        double maxSpeed = Math.min(
                p.get(RequestConstants.MAX_SPEED_REQUEST),
                p.get(RequestConstants.MAX_ROAD_SPEED_REQUEST)
        );

        double minGap = p.get(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        double sensitivity = p.get(RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST);
        double maxSpeedRoad = p.get(RequestConstants.MAX_ROAD_SPEED_REQUEST);

        // expected optimalVelocity using OVM_Different formula
        double DELTA = 5.0;
        double dcOverDelta = minGap / DELTA;
        double expectedOptVel = maxSpeedRoad *
                ((Math.tanh((distance - minGap) / DELTA) + Math.tanh(dcOverDelta))
                        / (1.0 + Math.tanh(dcOverDelta)));

        double newSpeed = currentSpeed + sensitivity * (expectedOptVel - currentSpeed);

        // collision check
        if (distance < newSpeed) {
            newSpeed = distance - minGap;
        }

        double expected = Math.min(newSpeed, maxSpeed);

        double result = model.getNewSpeed(p);

        assertEquals(expected, result, 1e-9,
                "getNewSpeed must use the overridden OVM_Different optimalVelocity formula.");
    }

    /***************************************************************************
     * SIMPLE METADATA TESTS
     **************************************************************************/

    @Test
    void getName_returnsCorrectName() {
        assertEquals("Optimal Velocity Model (different)", new OVM_Different().getName());
    }

    @Test
    void getID_returnsCorrectID() {
        assertEquals("ovm-different", new OVM_Different().getID());
    }

    /**
     * requestParameters() must stay the same as in OVM_Original.
     */
    @Test
    void requestParameters_inheritedCorrectly() {
        OVM_Original original = new OVM_Original();
        OVM_Different model = new OVM_Different();

        assertEquals(original.requestParameters(), model.requestParameters(),
                "OVM_Different should inherit requestParameters() unchanged.");
    }

    /**
     * getParametersForGeneration() also inherited from OVM_Original.
     */
    @Test
    void getParametersForGeneration_inheritedCorrectly() {
        OVM_Original original = new OVM_Original();
        OVM_Different model = new OVM_Different();

        assertEquals(original.getParametersForGeneration(), model.getParametersForGeneration(),
                "OVM_Different must inherit getParametersForGeneration() unchanged.");
    }

    /**
     * Cell size stays undefined for continuous models.
     */
    @Test
    void getCellSize_returnsUndefined() {
        assertEquals(Constants.PARAMETER_UNDEFINED, new OVM_Different().getCellSize());
    }
}
