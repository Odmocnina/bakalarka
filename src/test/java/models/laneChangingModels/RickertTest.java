package models.laneChangingModels;

import core.model.Direction;
import core.utils.RandomNumberGenerator;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/****************************************************
 * Unit tests for the Rickert lane changing model, focusing on basic properties,
 * parameter requests, and the lane changing decision logic including safety gap checks
 * and handling of randomness.
 *
 * @author Michael Hladky
 * @version 1.0
 *****************************************************/
public class RickertTest {

    /** instance of the Rickert model to be used in tests **/
    private Rickert rickertModel;

    /**
     * setup method to initialize a fresh instance of the Rickert model before each test
     * and to set a fixed seed for the RandomNumberGenerator so that tests are deterministic
     **/
    @BeforeEach
    void setUp() {
        // Initialize the RandomNumberGenerator with a fixed seed (12345).
        // With seed 12345, the first nextDouble() call returns ~0.796, which is < 0.9 (chance).
        RandomNumberGenerator.getInstance(12345).resetSeed(12345);
        rickertModel = new Rickert();
    }

    /**
     * test to verify basic getter methods of the model
     **/
    @Test
    void testBasicProperties() {
        assertEquals("rickert", rickertModel.getID(), "ID should be 'rickert'");
        assertEquals("Rickert Model", rickertModel.getName(), "Name should be 'Rickert Model'");
        assertEquals(Constants.CELLULAR, rickertModel.getType(), "Type should be CELLULAR");
        assertEquals(RequestConstants.MAX_SPEED_REQUEST, rickertModel.getParametersForGeneration(), "Should request MAX_SPEED for generation");
    }

    /**
     * test to verify that requestParameters returns a properly formatted string containing required constants
     **/
    @Test
    void requestParameters_ShouldReturnDelimitedString() {
        String requests = rickertModel.requestParameters();
        assertNotNull(requests);
        assertTrue(requests.contains(RequestConstants.X_POSITION_REQUEST), "Should contain X_POSITION_REQUEST");
        assertTrue(requests.contains(RequestConstants.MAX_ROAD_SPEED_REQUEST), "Should contain MAX_ROAD_SPEED_REQUEST");
        assertTrue(requests.contains(RequestConstants.REQUEST_SEPARATOR), "Should use the correct separator");
    }

    /**
     * test to verify that requestParameters with Direction returns specific correct requests
     **/
    @Test
    void requestParameters_WithDirection_ShouldReturnSpecificRequests() {
        String leftRequests = rickertModel.requestParameters(Direction.LEFT);
        assertTrue(leftRequests.contains(RequestConstants.X_POSITION_LEFT_FORWARD_REQUEST), "Left request should contain left forward parameter");
        assertFalse(leftRequests.contains(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST), "Left request should not contain right forward parameter");

        String rightRequests = rickertModel.requestParameters(Direction.RIGHT);
        assertTrue(rightRequests.contains(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST), "Right request should contain right forward parameter");

        String straightRequests = rickertModel.requestParameters(Direction.STRAIGHT);
        assertEquals("", straightRequests, "Straight request should return an empty string");
    }

    /**
     * test to verify that the model returns STRAIGHT if the car has plenty of space in front
     * (distanceToNextCar > theoreticalSpeed)
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnStraight_WhenNoNeedToChange() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // internal currentSpeed will be 2 + 1 = 3
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        // distance to next car is 10, theoretical speed is 3. Since 10 > 3, there's no need to change lane
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 10.0);

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST, 15.0);
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST, 10.0);

        Direction decision = rickertModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.STRAIGHT, decision, "Should stay STRAIGHT if there is enough space in front (no need to change)");
    }

    /**
     * test to verify that the model returns STRAIGHT if the gap in the new lane is too small
     * in front (newLaneForwardGap <= theoreticalSpeed)
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnStraight_WhenNewLaneBlockedForward() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // internal currentSpeed will be 3
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 2.0); // Needs to change (2 <= 3)

        // The gap in the left lane is 2, which is <= theoreticalSpeed (3), so it's not advantageous/safe
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST, 2.0);
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST, 10.0);

        Direction decision = rickertModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.STRAIGHT, decision, "Should stay STRAIGHT if the new lane does not offer enough forward gap");
    }

    /**
     * test to verify that the model returns STRAIGHT if the gap in the new lane is too small
     * in the back (newLanePreviousGap <= maxRoadSpeed)
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnStraight_WhenNewLaneBlockedBackward() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // internal currentSpeed will be 3
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 2.0); // Needs to change

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST, 10.0); // Good forward gap

        // The gap behind us in the left lane is 4, which is <= maxRoadSpeed (5), so it's UNSAFE
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST, 4.0);

        Direction decision = rickertModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.STRAIGHT, decision, "Should stay STRAIGHT if changing lane would cut off a car behind us (unsafe backward gap)");
    }

    /**
     * test to verify that the model correctly decides to change lane when all conditions
     * (need to change, safe forward gap, safe backward gap) are met.
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnLeft_WhenConditionsMet() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // internal currentSpeed will be 3
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 2.0); // Needs to change

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST, 6.0); // Safe forward gap (> 3)
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST, 6.0); // Safe backward gap (> 5)

        Direction decision = rickertModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.LEFT, decision, "Should decide to change to LEFT lane because all conditions are met");
    }

    /**
     * test to verify that the position-based evaluation can successfully choose the right lane
     * when left is blocked, right is free, and the random chance allows it
     **/
    @Test
    void changeLaneIfDesired_ByPositions_ShouldReturnRight_WhenLeftBlockedAndRightFree() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.X_POSITION_REQUEST, 10.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // theoretical = 3
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        // Car in front: Gap is 13 - 10 - 1 = 2 (needs to change)
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 13.0);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 1.0);

        // Left lane does not exist (e.g., we are in the leftmost lane)
        params.put(RequestConstants.X_POSITION_LEFT_FORWARD_REQUEST, (double) Constants.NO_LANE_THERE);
        params.put(RequestConstants.LENGTH_LEFT_FORWARD_REQUEST, 0.0);
        params.put(RequestConstants.X_POSITION_LEFT_BACKWARD_REQUEST, (double) Constants.NO_LANE_THERE);

        // Right lane is free forward and backward
        params.put(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST, 16.0);
        params.put(RequestConstants.LENGTH_RIGHT_FORWARD_REQUEST, 1.0); // forward gap = 16 - 10 - 1 = 5 (> 3)

        params.put(RequestConstants.X_POSITION_RIGHT_BACKWARD_REQUEST, 2.0); // backward gap = 10 - 2 = 8 (> 5)

        // We already fixed the RNG seed in setUp() to ensure chance passes.
        Direction decision = rickertModel.changeLaneIfDesired(params);

        assertEquals(Direction.RIGHT, decision, "Should decide to change RIGHT based on positional calculations and fixed random seed");
    }
}
