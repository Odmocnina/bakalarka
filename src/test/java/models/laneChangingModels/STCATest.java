package models.laneChangingModels;

import core.model.Direction;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/****************************************************
 * Unit tests for the STCA lane changing model, focusing on basic properties,
 * parameter requests, and the specific STCA lane changing decision logic
 * (advantage based on current gap vs new gap).
 *
 * @author Michael Hladky
 * @version 1.0
 *****************************************************/
public class STCATest {

    /** instance of the STCA model to be used in tests **/
    private STCA stcaModel;

    /**
     * setup method to initialize a fresh instance of the STCA model before each test
     **/
    @BeforeEach
    void setUp() {
        stcaModel = new STCA();
    }

    /**
     * test to verify basic getter methods of the model
     **/
    @Test
    void testBasicProperties() {
        assertEquals("stca", stcaModel.getID(), "ID should be 'stca'");
        assertEquals("STCA (Symmetric Two-lane Cellular Automata)", stcaModel.getName(), "Name should match the predefined string");
        assertEquals(Constants.CELLULAR, stcaModel.getType(), "Type should be CELLULAR");
        assertEquals(RequestConstants.MAX_SPEED_REQUEST, stcaModel.getParametersForGeneration(), "Should request MAX_SPEED for generation");
    }

    /**
     * test to verify that requestParameters returns a properly formatted string containing required constants
     **/
    @Test
    void requestParameters_ShouldReturnDelimitedString() {
        String requests = stcaModel.requestParameters();
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
        String leftRequests = stcaModel.requestParameters(Direction.LEFT);
        assertTrue(leftRequests.contains(RequestConstants.X_POSITION_LEFT_FORWARD_REQUEST), "Left request should contain left forward parameter");
        assertFalse(leftRequests.contains(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST), "Left request should not contain right forward parameter");

        String rightRequests = stcaModel.requestParameters(Direction.RIGHT);
        assertTrue(rightRequests.contains(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST), "Right request should contain right forward parameter");

        String straightRequests = stcaModel.requestParameters(Direction.STRAIGHT);
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
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // internal currentSpeed will be 3
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        // Gap is 10, theoretical speed is 3 -> 10 > 3, so we don't need to change lane
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 10.0);

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST, 15.0);
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST, 10.0);

        Direction decision = stcaModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.STRAIGHT, decision, "Should stay STRAIGHT if there is enough space in front (no need to change)");
    }

    /**
     * test to verify that the model returns STRAIGHT if the gap in the new lane is not larger
     * than the gap in the current lane (newLaneForwardGap <= distanceToNextCar)
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnStraight_WhenNewLaneForwardGapNotBetter() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 3.0); // internal currentSpeed will be 4
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 2.0); // Needs to change (2 <= 4)

        // The gap in the left lane is 2, which is NOT better than the current gap (2)
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST, 2.0);
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST, 10.0);

        Direction decision = stcaModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.STRAIGHT, decision, "Should stay STRAIGHT if the new lane does not offer a larger forward gap");
    }

    /**
     * test to verify that the model returns STRAIGHT if changing lane is unsafe backward
     * (newLanePreviousGap <= maxRoadSpeed)
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnStraight_WhenNewLaneBlockedBackward() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // internal currentSpeed = 3
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0); // max road speed = 5

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 2.0); // Needs to change
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST, 10.0); // Better gap forward

        // Backward gap is 4. Since 4 <= maxRoadSpeed(5), it's UNSAFE
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST, 4.0);

        Direction decision = stcaModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.STRAIGHT, decision, "Should stay STRAIGHT if changing lane would cut off a car behind us (unsafe backward gap)");
    }

    /**
     * test to verify that the model correctly decides to change lane when all STCA conditions
     * (need to change, new gap is larger, safe backward gap) are met.
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnRight_WhenConditionsMet() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // internal currentSpeed = 3
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 2.0); // Needs to change

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST, 6.0); // Better gap forward (6 > 2)
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST, 6.0); // Safe backward gap (6 > 5)

        Direction decision = stcaModel.changeLaneIfDesired(params, Direction.RIGHT);

        assertEquals(Direction.RIGHT, decision, "Should decide to change RIGHT because all conditions (advantage and safety) are met");
    }

    /**
     * test to verify the position-based evaluation logic, accurately choosing the right lane
     * when the left lane doesn't offer a better gap, but the right lane does
     **/
    @Test
    void changeLaneIfDesired_ByPositions_ShouldReturnRight_WhenLeftBlockedAndRightFree() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.X_POSITION_REQUEST, 10.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // theoretical = 3
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        // Current lane: Gap = 13 - 10 - 1 = 2 (needs to change)
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 13.0);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 1.0);

        // Left lane: Gap = 12 - 10 - 1 = 1 (NOT better than 2, so blocked)
        params.put(RequestConstants.X_POSITION_LEFT_FORWARD_REQUEST, 12.0);
        params.put(RequestConstants.LENGTH_LEFT_FORWARD_REQUEST, 1.0);
        params.put(RequestConstants.X_POSITION_LEFT_BACKWARD_REQUEST, (double) Constants.NO_CAR_THERE);

        // Right lane: Gap forward = 16 - 10 - 1 = 5 (Better than 2). Gap backward = 10 - 2 = 8 (> 5). Safe.
        params.put(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST, 16.0);
        params.put(RequestConstants.LENGTH_RIGHT_FORWARD_REQUEST, 1.0);
        params.put(RequestConstants.X_POSITION_RIGHT_BACKWARD_REQUEST, 2.0);

        Direction decision = stcaModel.changeLaneIfDesired(params);

        assertEquals(Direction.RIGHT, decision, "Should decide to change RIGHT based on raw positional calculations avoiding the less advantageous left lane");
    }
}
