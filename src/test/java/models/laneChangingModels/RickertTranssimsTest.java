package models.laneChangingModels;

import core.model.Direction;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/****************************************************
 * Unit tests for the RickertTranssims lane changing model, focusing on basic properties,
 * parameter requests, and the lane changing decision logic using the Transsims
 * weight-based rules (advantage vs. forward/backward penalties).
 *
 * @author Michael Hladky
 * @version 1.0
 *****************************************************/
public class RickertTranssimsTest {

    /** instance of the RickertTranssims model to be used in tests **/
    private RickertTranssims rickertTranssimsModel;

    /**
     * setup method to initialize a fresh instance of the model before each test
     **/
    @BeforeEach
    void setUp() {
        rickertTranssimsModel = new RickertTranssims();
    }

    /**
     * test to verify basic getter methods of the model
     **/
    @Test
    void testBasicProperties() {
        assertEquals("rickert-transsims", rickertTranssimsModel.getID(), "ID should be 'rickert-transsims'");
        assertEquals("Rickert Model, Transsims version", rickertTranssimsModel.getName(), "Name should match the predefined string");
        assertEquals(Constants.CELLULAR, rickertTranssimsModel.getType(), "Type should be CELLULAR");
        assertEquals(RequestConstants.MAX_SPEED_REQUEST, rickertTranssimsModel.getParametersForGeneration(), "Should request MAX_SPEED for generation");
    }

    /**
     * test to verify that requestParameters returns a properly formatted string containing required constants
     **/
    @Test
    void requestParameters_ShouldReturnDelimitedString() {
        String requests = rickertTranssimsModel.requestParameters();
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
        String leftRequests = rickertTranssimsModel.requestParameters(Direction.LEFT);
        assertTrue(leftRequests.contains(RequestConstants.X_POSITION_LEFT_FORWARD_REQUEST), "Left request should contain left forward parameter");
        assertFalse(leftRequests.contains(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST), "Left request should not contain right forward parameter");

        String rightRequests = rickertTranssimsModel.requestParameters(Direction.RIGHT);
        assertTrue(rightRequests.contains(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST), "Right request should contain right forward parameter");

        String straightRequests = rickertTranssimsModel.requestParameters(Direction.STRAIGHT);
        assertEquals("", straightRequests, "Straight request should return an empty string");
    }

    /**
     * test to verify that the model returns STRAIGHT if there is no advantage in changing lane
     * (e.g., distance to next car is greater than or equal to theoretical speed, so weight1 = 0)
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnStraight_WhenNoAdvantage() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // internal currentSpeed will be 2 + 1 = 3
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        // distance to next car is 5, theoretical speed is 3. Since 5 > 3, weight1 becomes 0.
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 5.0);

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST, 10.0);
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST, 10.0);

        Direction decision = rickertTranssimsModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.STRAIGHT, decision, "Should stay STRAIGHT if there is no advantage to change (plenty of space ahead)");
    }

    /**
     * test to verify that the model returns STRAIGHT if changing lane is unsafe forward
     * (weight2 penalty overrides weight1 advantage)
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnStraight_WhenNewLaneBlockedForward() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 4.0); // internal currentSpeed = 5
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 2.0); // Needs to change (2 < 5) -> weight1 = 1

        // Forward gap in left lane is 4. Weight 2 = max(0, theoreticalSpeed - newLaneForwardGap) = 5 - 4 = 1.
        // Condition: weight1 (1) > weight2 (1) is FALSE.
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST, 4.0);
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST, 10.0); // Safe backward

        Direction decision = rickertTranssimsModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.STRAIGHT, decision, "Should stay STRAIGHT if the forward gap in the new lane triggers a weight2 penalty");
    }

    /**
     * test to verify that the model returns STRAIGHT if changing lane is unsafe backward
     * (weight3 penalty overrides weight1 advantage)
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnStraight_WhenNewLaneBlockedBackward() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // internal currentSpeed = 3
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0); // max road speed = 5

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 2.0); // Needs to change -> weight1 = 1
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST, 10.0); // Safe forward

        // Backward gap is 4. Weight 3 = max(0, maxRoadSpeed - newLanePreviousGap) = 5 - 4 = 1.
        // Condition: weight1 (1) > weight3 (1) is FALSE.
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST, 4.0);

        Direction decision = rickertTranssimsModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.STRAIGHT, decision, "Should stay STRAIGHT if changing lane would cut off a car behind us (weight3 penalty)");
    }

    /**
     * test to verify that the model returns the requested direction if all conditions
     * (advantage, safe forward gap, safe backward gap) are met
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnRight_WhenConditionsMet() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0);
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 2.0);

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST, 6.0);
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST, 6.0);

        Direction decision = rickertTranssimsModel.changeLaneIfDesired(params, Direction.RIGHT);

        assertEquals(Direction.RIGHT, decision, "Should decide to change to RIGHT lane because advantage outweighs penalties (1 > 0 && 1 > 0)");
    }

    /**
     * test to verify the position-based evaluation logic, evaluating raw X coordinates and lengths
     * to correctly decide a lane change when left is blocked and right is free
     **/
    @Test
    void changeLaneIfDesired_ByPositions_ShouldReturnRight_WhenLeftBlockedAndRightFree() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.X_POSITION_REQUEST, 10.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // theoretical = 3
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);

        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 13.0);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 1.0);

        params.put(RequestConstants.X_POSITION_LEFT_FORWARD_REQUEST, 12.0);
        params.put(RequestConstants.LENGTH_LEFT_FORWARD_REQUEST, 1.0);
        params.put(RequestConstants.X_POSITION_LEFT_BACKWARD_REQUEST, (double) Constants.NO_CAR_THERE);

        params.put(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST, 16.0);
        params.put(RequestConstants.LENGTH_RIGHT_FORWARD_REQUEST, 1.0);
        params.put(RequestConstants.X_POSITION_RIGHT_BACKWARD_REQUEST, 2.0);

        Direction decision = rickertTranssimsModel.changeLaneIfDesired(params);

        assertEquals(Direction.RIGHT, decision, "Should decide to change RIGHT based on raw positional calculations evaluating to weight1 > weight2 && weight1 > weight3");
    }
}
