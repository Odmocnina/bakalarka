package models.laneChangingModels;

import core.model.Direction;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/****************************************************
 * Unit tests for the Mobil lane changing model, focusing on basic properties,
 * parameter requests, and the lane changing decision logic based on acceleration gain
 * and safety checks.
 *
 * @author Michael Hladky
 * @version 1.0
 *****************************************************/
public class MobilTest {

    /** instance of the Mobil model to be used in tests **/
    private Mobil mobilModel;

    /**
     * setup method to initialize a fresh instance of the Mobil model before each test
     **/
    @BeforeEach
    void setUp() {
        mobilModel = new Mobil();
    }

    /**
     * test to verify basic getter methods of the model (ID, Name, Type, Gen Parameters)
     **/
    @Test
    void testBasicProperties() {
        assertEquals("mobil", mobilModel.getID(), "ID should be 'mobil'");
        assertEquals("MOBIL", mobilModel.getName(), "Name should be 'MOBIL'");
        assertEquals(Constants.CONTINUOUS, mobilModel.getType(), "Type should be CONTINUOUS");

        String genParams = mobilModel.getParametersForGeneration();
        assertTrue(genParams.contains(RequestConstants.POLITENESS_FACTOR_REQUEST), "Generation params should include politeness factor");
        assertTrue(genParams.contains(RequestConstants.MAX_SPEED_REQUEST), "Generation params should include max speed");
    }

    /**
     * test to verify that requestParameters returns a properly formatted string containing required constants
     **/
    @Test
    void requestParameters_ShouldReturnDelimitedString() {
        String requests = mobilModel.requestParameters();
        assertNotNull(requests);
        assertTrue(requests.contains(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST));
        assertTrue(requests.contains(RequestConstants.THEORETICAL_ACCELERATION_REQUEST));
        assertTrue(requests.contains(RequestConstants.REQUEST_SEPARATOR));
    }

    /**
     * test to verify that requestParameters with Direction returns specific correct requests
     **/
    @Test
    void requestParameters_WithDirection_ShouldReturnSpecificRequests() {
        String leftRequests = mobilModel.requestParameters(Direction.LEFT);
        assertTrue(leftRequests.contains(RequestConstants.DECELERATION_COMFORT_LEFT_BACKWARD_REQUEST), "Left request should contain left backward parameter");
        assertFalse(leftRequests.contains(RequestConstants.DECELERATION_COMFORT_RIGHT_BACKWARD_REQUEST), "Left request should not contain right backward parameter");

        String rightRequests = mobilModel.requestParameters(Direction.RIGHT);
        assertTrue(rightRequests.contains(RequestConstants.DECELERATION_COMFORT_RIGHT_BACKWARD_REQUEST), "Right request should contain right backward parameter");

        String straightRequests = mobilModel.requestParameters(Direction.STRAIGHT);
        assertEquals("", straightRequests, "Straight request should return an empty string");
    }

    /**
     * test to verify that changeLaneIfDesired without direction parameter currently defaults to STRAIGHT
     * (as implemented in the current code version)
     **/
    @Test
    void changeLaneIfDesired_NoDirection_ShouldReturnStraight() {
        // We supply dummy values to prevent NullPointerException from HashMap
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 0.2);
        params.put(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.5);
        params.put(RequestConstants.NOW_ACCELERATION_REQUEST, 1.0);
        params.put(RequestConstants.NOW_ACCELERATION_LEFT_BACKWARD_REQUEST, 1.0);
        params.put(RequestConstants.NOW_ACCELERATION_RIGHT_BACKWARD_REQUEST, 1.0);
        params.put(RequestConstants.NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST, 1.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_REQUEST, 1.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_LEFT_BACKWARD_REQUEST, 1.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_RIGHT_BACKWARD_REQUEST, 1.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST, 1.0);

        Direction decision = mobilModel.changeLaneIfDesired(params);
        assertEquals(Direction.STRAIGHT, decision, "Current implementation should return STRAIGHT");
    }

    /**
     * test to verify that the safety check prevents a lane change if the required deceleration
     * for the following car exceeds its comfort level
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnStraight_WhenUnsafe() {
        HashMap<String, Double> params = new HashMap<>();
        // Set up an unsafe situation: changing lane would cause us to brake extremely hard
        params.put(RequestConstants.THEORETICAL_ACCELERATION_REQUEST, -6.0); // Extreme braking needed (absolute value 6.0)
        params.put(RequestConstants.DECELERATION_COMFORT_LEFT_BACKWARD_REQUEST, 3.0); // Max comfortable deceleration is 3.0

        // Populate other mandatory keys to avoid NullPointerException
        params.put(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 0.2);
        params.put(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.5);
        params.put(RequestConstants.NOW_ACCELERATION_REQUEST, 1.0);
        params.put(RequestConstants.NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST, 1.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST, 1.0);

        Direction decision = mobilModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.STRAIGHT, decision, "Should return STRAIGHT because decelerationForSafety > deceleration comfort");
    }

    /**
     * test to verify that a lane change is executed if it is safe and provides enough
     * acceleration gain to overcome the threshold and politeness penalty
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnLeft_WhenSafeAndAdvantageous() {
        HashMap<String, Double> params = new HashMap<>();

        //  deceleration safety check: we need to brake by 2.0, but comfort is 4.0, so it is safe
        params.put(RequestConstants.THEORETICAL_ACCELERATION_REQUEST, 2.0); // Positive acceleration, so deceleration = 2.0
        params.put(RequestConstants.DECELERATION_COMFORT_LEFT_BACKWARD_REQUEST, 4.0); // 2.0 < 4.0, so it is SAFE

        //  Advantage calculation
        params.put(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 0.2); // Threshold
        params.put(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.5); // Factor of caring about others

        //  gain
        params.put(RequestConstants.NOW_ACCELERATION_REQUEST, 1.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_REQUEST, 3.0);

        // Other cars acceleration differences
        params.put(RequestConstants.NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST, 1.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST, 1.0);
        params.put(RequestConstants.NOW_ACCELERATION_LEFT_BACKWARD_REQUEST, 1.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_LEFT_BACKWARD_REQUEST, 1.0);

        Direction decision = mobilModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.LEFT, decision, "Should return LEFT because the lane change is safe and highly advantageous");
    }

    /**
     * test to verify that the model allows a lane change if the new lane has no car behind
     * (deceleration safety check defaults to Double.MAX_VALUE)
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldHandleNoCarBehind() {
        HashMap<String, Double> params = new HashMap<>();

        // Hard braking, but nobody is behind us in the new lane
        params.put(RequestConstants.THEORETICAL_ACCELERATION_REQUEST, -5.0);
        params.put(RequestConstants.DECELERATION_COMFORT_RIGHT_BACKWARD_REQUEST, (double) Constants.NO_CAR_THERE);

        // Setup advantage to be extremely high so the change logic passes
        params.put(RequestConstants.NOW_ACCELERATION_REQUEST, -5.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_REQUEST, 2.0); // Gain = 7.0

        params.put(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 0.1);
        params.put(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.0); // We don't care about others

        params.put(RequestConstants.NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST, 0.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST, 0.0);
        params.put(RequestConstants.NOW_ACCELERATION_RIGHT_BACKWARD_REQUEST, 0.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_RIGHT_BACKWARD_REQUEST, 0.0);

        Direction decision = mobilModel.changeLaneIfDesired(params, Direction.RIGHT);

        assertEquals(Direction.RIGHT, decision, "Should return RIGHT because NO_CAR_THERE safely bypasses the comfort deceleration check");
    }
}