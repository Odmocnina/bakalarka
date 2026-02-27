package models.laneChangingModels;

import core.model.Direction;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/****************************************************
 * Unit tests for the MobilSimple lane changing model, focusing on basic properties,
 * parameter requests, and the simplified lane changing decision logic.
 *
 * @author Michael Hladky
 * @version 1.0
 *****************************************************/
public class MobilSimpleTest {

    /** instance of the MobilSimple model to be used in tests **/
    private MobilSimple mobilSimpleModel;

    /**
     * setup method to initialize a fresh instance of the MobilSimple model before each test
     **/
    @BeforeEach
    void setUp() {
        mobilSimpleModel = new MobilSimple();
    }

    /**
     * test to verify basic getter methods of the model
     **/
    @Test
    void testBasicProperties() {
        // Note: The original code returns "mobil" for getID(), even though class is MobilSimple.
        // We test what is actually in the code.
        assertEquals("mobil", mobilSimpleModel.getID(), "ID should be 'mobil'");
        assertEquals("MOBIL", mobilSimpleModel.getName(), "Name should be 'MOBIL'");
        assertEquals(Constants.CONTINUOUS, mobilSimpleModel.getType(), "Type should be CONTINUOUS");

        String genParams = mobilSimpleModel.getParametersForGeneration();
        assertTrue(genParams.contains(RequestConstants.POLITENESS_FACTOR_REQUEST), "Generation params should include politeness factor");
        assertTrue(genParams.contains(RequestConstants.MAX_SPEED_REQUEST), "Generation params should include max speed");
    }

    /**
     * test to verify that requestParameters returns a properly formatted string containing required constants
     **/
    @Test
    void requestParameters_ShouldReturnDelimitedString() {
        String requests = mobilSimpleModel.requestParameters();
        assertNotNull(requests);
        assertTrue(requests.contains(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST));
        assertTrue(requests.contains(RequestConstants.THEORETICAL_ACCELERATION_REQUEST));
        // MobilSimple drops the straight backward parameters from general requests
        assertFalse(requests.contains(RequestConstants.NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST), "Simplified Mobil should not request straight backward acceleration");
    }

    /**
     * test to verify that requestParameters with Direction returns specific correct requests
     **/
    @Test
    void requestParameters_WithDirection_ShouldReturnSpecificRequests() {
        String leftRequests = mobilSimpleModel.requestParameters(Direction.LEFT);
        assertTrue(leftRequests.contains(RequestConstants.DECELERATION_COMFORT_LEFT_BACKWARD_REQUEST), "Left request should contain left backward parameter");
        assertFalse(leftRequests.contains(RequestConstants.DECELERATION_COMFORT_RIGHT_BACKWARD_REQUEST), "Left request should not contain right backward parameter");

        String rightRequests = mobilSimpleModel.requestParameters(Direction.RIGHT);
        assertTrue(rightRequests.contains(RequestConstants.DECELERATION_COMFORT_RIGHT_BACKWARD_REQUEST), "Right request should contain right backward parameter");

        String straightRequests = mobilSimpleModel.requestParameters(Direction.STRAIGHT);
        assertEquals("", straightRequests, "Straight request should return an empty string");
    }

    /**
     * test to verify that changeLaneIfDesired without direction parameter currently defaults to STRAIGHT
     * (as explicitly coded in MobilSimple class)
     **/
    @Test
    void changeLaneIfDesired_NoDirection_ShouldReturnStraight() {
        HashMap<String, Double> params = new HashMap<>();
        // Filling dummy parameters to prevent exceptions
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

        Direction decision = mobilSimpleModel.changeLaneIfDesired(params);
        assertEquals(Direction.STRAIGHT, decision, "Method should return STRAIGHT according to current implementation");
    }

    /**
     * test to verify that the safety check prevents a lane change if the required deceleration
     * for the following car exceeds its comfort level
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnStraight_WhenUnsafe() {
        HashMap<String, Double> params = new HashMap<>();
        // Extreme braking needed -> abs(-5.0) = 5.0
        params.put(RequestConstants.THEORETICAL_ACCELERATION_REQUEST, -5.0);
        // Max comfort deceleration is only 3.0 (Unsafe!)
        params.put(RequestConstants.DECELERATION_COMFORT_LEFT_BACKWARD_REQUEST, 3.0);

        // Populate other keys to avoid NullPointerException
        params.put(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 0.1);
        params.put(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.5);
        params.put(RequestConstants.NOW_ACCELERATION_REQUEST, 1.0);
        params.put(RequestConstants.NOW_ACCELERATION_LEFT_BACKWARD_REQUEST, 1.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_LEFT_BACKWARD_REQUEST, 1.0);

        Direction decision = mobilSimpleModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.STRAIGHT, decision, "Should return STRAIGHT because safety deceleration is greater than comfort");
    }

    /**
     * test to verify that a lane change is executed if it is safe and the calculated
     * advantage (gain) outweighs the politeness penalty using the SIMPLIFIED logic
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnRight_WhenSafeAndAdvantageous() {
        HashMap<String, Double> params = new HashMap<>();

        // --- Safety Check ---
        params.put(RequestConstants.THEORETICAL_ACCELERATION_REQUEST, 2.0); // abs(2.0) = 2.0
        params.put(RequestConstants.DECELERATION_COMFORT_RIGHT_BACKWARD_REQUEST, 4.0); // 2.0 < 4.0 -> Safe

        // --- Advantage Check ---
        params.put(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 0.2);
        params.put(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.5);

        // Our gain: theoretical(3.0) - now(1.0) = 2.0
        params.put(RequestConstants.NOW_ACCELERATION_REQUEST, 1.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_REQUEST, 3.0);

        // Neighbor's penalty: now(1.0) - theoretical(1.0) = 0.0
        params.put(RequestConstants.NOW_ACCELERATION_RIGHT_BACKWARD_REQUEST, 1.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_RIGHT_BACKWARD_REQUEST, 1.0);

        // Eq: 2.0 > 0.5 * 0.0 + 0.2 -> 2.0 > 0.2 (True)
        Direction decision = mobilSimpleModel.changeLaneIfDesired(params, Direction.RIGHT);

        assertEquals(Direction.RIGHT, decision, "Should return RIGHT because the change is safe and meets simplified advantage criteria");
    }

    /**
     * test to verify that the model correctly bypasses the comfort deceleration safety check
     * when there is no car behind in the target lane
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldHandleNoCarBehind() {
        HashMap<String, Double> params = new HashMap<>();

        // Extreme deceleration, normally unsafe
        params.put(RequestConstants.THEORETICAL_ACCELERATION_REQUEST, -6.0);
        // But NO_CAR_THERE safely overrides it to Double.MAX_VALUE
        params.put(RequestConstants.DECELERATION_COMFORT_LEFT_BACKWARD_REQUEST, (double) Constants.NO_CAR_THERE);

        params.put(RequestConstants.NOW_ACCELERATION_REQUEST, -6.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_REQUEST, 2.0); // Gain = 8.0

        params.put(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 0.1);
        params.put(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.0);

        params.put(RequestConstants.NOW_ACCELERATION_LEFT_BACKWARD_REQUEST, 0.0);
        params.put(RequestConstants.THEORETICAL_ACCELERATION_LEFT_BACKWARD_REQUEST, 0.0);

        Direction decision = mobilSimpleModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.LEFT, decision, "Should return LEFT because NO_CAR_THERE bypasses the safety check");
    }
}
