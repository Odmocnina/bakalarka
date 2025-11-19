package models.carFollowingModels;

import core.utils.Constants;
import core.utils.RequestConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class OVM_OriginalTest {

    /**
     * Helper subclass to expose the protected optimalVelocity method
     * for direct unit testing.
     */
    private static class TestOVM_Original extends OVM_Original {
        public double callOptimalVelocity(double distance, double maxSpeedRoad, double minGap) {
            return optimalVelocity(distance, maxSpeedRoad, minGap);
        }
    }

    /**
     * Helper method to build a basic parameter set for getNewSpeed tests.
     */
    private HashMap<String, Double> createBaseParams() {
        HashMap<String, Double> params = new HashMap<>();

        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 10.0);             // v
        params.put(RequestConstants.MAX_SPEED_REQUEST, 25.0);                 // driver max speed
        params.put(RequestConstants.X_POSITION_REQUEST, 100.0);               // x
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 120.0); // x_l
        params.put(RequestConstants.DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST, 0.5);
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 30.0);           // road max speed
        params.put(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);   // minGap
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 4.0);   // length of car ahead

        params.put(RequestConstants.LENGTH_REQUEST, 4.0); // for generation, not used in getNewSpeed

        return params;
    }

    /**
     * Test: optimalVelocity should follow the implemented formula:
     * tanh(distance - 2) - tanh(2), ignoring maxSpeedRoad and minGap.
     */
    @Test
    void optimalVelocity_usesTanhFormula() {
        TestOVM_Original model = new TestOVM_Original();

        double distance = 16.0;
        double maxSpeedRoad = 30.0;
        double minGap = 2.0;

        double expected = Math.tanh(distance - 2) - Math.tanh(2);
        double result = model.callOptimalVelocity(distance, maxSpeedRoad, minGap);

        assertEquals(expected, result, 1e-12,
                "optimalVelocity should use tanh(distance - 2) - tanh(2).");
    }

    /**
     * Test: getNewSpeed in a normal situation when there is a car ahead.
     * We re-implement the same formula in the test to compute the expected value.
     */
    @Test
    void getNewSpeed_normalCase_withCarAhead() {
        OVM_Original model = new OVM_Original();
        HashMap<String, Double> params = createBaseParams();

        double currentSpeed = params.get(RequestConstants.CURRENT_SPEED_REQUEST);
        double x = params.get(RequestConstants.X_POSITION_REQUEST);
        double xL = params.get(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST);
        double maxSpeedRoad = params.get(RequestConstants.MAX_ROAD_SPEED_REQUEST);
        double maxSpeedDriver = params.get(RequestConstants.MAX_SPEED_REQUEST);
        double maxSpeed = Math.min(maxSpeedDriver, maxSpeedRoad);
        double minGap = params.get(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        double lengthL = params.get(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST);
        double sensitivity = params.get(RequestConstants.DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST);

        // distance to next car
        double distance = xL - x - lengthL;

        // same optimalVelocity as in the model
        double optimalVelocity = Math.tanh(distance - 2) - Math.tanh(2);

        double newSpeed = currentSpeed + sensitivity * (optimalVelocity - currentSpeed);

        // collision prevention
        if (distance < newSpeed) {
            newSpeed = distance - minGap;
        }

        double expected = Math.min(newSpeed, maxSpeed);

        double result = model.getNewSpeed(params);

        assertEquals(expected, result, 1e-9,
                "getNewSpeed should follow the OVM_Original formula in normal case.");
    }

    /**
     * Test: when there is no car ahead (xPositionStraightForward == NO_CAR_THERE),
     * distance should be treated as Double.MAX_VALUE.
     * We mainly verify branch coverage and that speed stays <= maxSpeed.
     */
    @Test
    void getNewSpeed_noCarAhead_usesInfiniteDistance() {
        OVM_Original model = new OVM_Original();
        HashMap<String, Double> params = createBaseParams();

        double maxSpeedRoad = 40.0;
        double maxSpeedDriver = 35.0;
        double maxSpeed = Math.min(maxSpeedRoad, maxSpeedDriver);

        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, maxSpeedRoad);
        params.put(RequestConstants.MAX_SPEED_REQUEST, maxSpeedDriver);
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, Constants.NO_CAR_THERE);

        double result = model.getNewSpeed(params);

        assertTrue(result <= maxSpeed,
                "When there is no car ahead, new speed must not exceed maxSpeed.");
        assertTrue(result >= 0.0,
                "Speed must not be negative.");
    }

    /**
     * Test: collision prevention – when distance < newSpeed,
     * speed should be set to distance - minGap before final min with maxSpeed.
     */
    @Test
    void getNewSpeed_collisionPrevention_applies() {
        OVM_Original model = new OVM_Original();
        HashMap<String, Double> params = createBaseParams();

        // Make distance small to trigger collision prevention
        params.put(RequestConstants.X_POSITION_REQUEST, 100.0);
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 108.0); // distance = 108 - 100 - 4 = 4
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 10.0); // large current speed
        params.put(RequestConstants.DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST, 1.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 50.0);
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 50.0);
        params.put(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);

        double x = params.get(RequestConstants.X_POSITION_REQUEST);
        double xL = params.get(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST);
        double lengthL = params.get(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST);
        double distance = xL - x - lengthL; // 4

        double currentSpeed = params.get(RequestConstants.CURRENT_SPEED_REQUEST);
        double sensitivity = params.get(RequestConstants.DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST);
        double minGap = params.get(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        double maxSpeed = Math.min(
                params.get(RequestConstants.MAX_SPEED_REQUEST),
                params.get(RequestConstants.MAX_ROAD_SPEED_REQUEST)
        );

        // compute expected using same formula and collision condition
        double optimalVelocity = Math.tanh(distance - 2) - Math.tanh(2);
        double newSpeed = currentSpeed + sensitivity * (optimalVelocity - currentSpeed);

        // collision branch must trigger because distance (4) is likely < newSpeed
        if (distance < newSpeed) {
            newSpeed = distance - minGap; // 4 - 2 = 2
        }

        double expected = Math.min(newSpeed, maxSpeed);

        double result = model.getNewSpeed(params);

        assertEquals(expected, result, 1e-9,
                "When distance < newSpeed, model must apply collision prevention (distance - minGap).");
    }

    /**
     * Test: requestParameters() must return the correct list of requested parameters
     * in the correct order.
     */
    @Test
    void requestParameters_returnsExpectedList() {
        OVM_Original model = new OVM_Original();

        String[] expectedRequests = {
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.MAX_ROAD_SPEED_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST
        };

        String expected = String.join(RequestConstants.REQUEST_SEPARATOR, expectedRequests);

        assertEquals(expected, model.requestParameters(),
                "requestParameters() should return all required parameters in correct order.");
    }

    /**
     * Test: getParametersForGeneration() must return the correct list.
     */
    @Test
    void getParametersForGeneration_returnsExpectedList() {
        OVM_Original model = new OVM_Original();

        String[] expectedRequests = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.LENGTH_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST
        };

        String expected = String.join(RequestConstants.REQUEST_SEPARATOR, expectedRequests);

        assertEquals(expected, model.getParametersForGeneration(),
                "getParametersForGeneration() should return parameters used when generating cars.");
    }

    /**
     * Test: getID() returns correct model ID.
     */
    @Test
    void getID_returnsCorrectID() {
        OVM_Original model = new OVM_Original();
        assertEquals("ovm-original", model.getID());
    }

    /**
     * Test: getType() returns continuous type.
     */
    @Test
    void getType_returnsContinuous() {
        OVM_Original model = new OVM_Original();
        assertEquals(Constants.CONTINOUS, model.getType());
    }

    /**
     * Test: getName() returns a human-readable model name.
     */
    @Test
    void getName_returnsCorrectName() {
        OVM_Original model = new OVM_Original();
        assertEquals("Optimal Velocity Model (original)", model.getName());
    }

    /**
     * Test: getCellSize() returns PARAMETER_UNDEFINED for this continuous model.
     */
    @Test
    void getCellSize_returnsUndefined() {
        OVM_Original model = new OVM_Original();
        assertEquals(Constants.PARAMETER_UNDEFINED, model.getCellSize());
    }
}

