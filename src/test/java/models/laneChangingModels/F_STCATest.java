package models.laneChangingModels;

import core.model.Direction;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/****************************************************
 * Unit tests for the F_STCA lane changing model, focusing on model properties,
 * parameter requests, and lane changing decision logic.
 *
 * @author Michael Hladky
 * @version 1.0
 *****************************************************/
public class F_STCATest {

    /** instance of the F_STCA model to be used in tests **/
    private F_STCA fStcaModel;

    /**
     * setup method to initialize a fresh instance of F_STCA before each test
     **/
    @BeforeEach
    void setUp() {
        fStcaModel = new F_STCA();
    }

    /**
     * test to verify basic getter methods of the model (ID, Name, Type, Gen Parameters)
     **/
    @Test
    void testBasicProperties() {
        assertEquals("f-stca", fStcaModel.getID(), "ID should be 'f-stca'");
        assertEquals("F-STCA (Symmetric Two-lane Cellular Automata)", fStcaModel.getName(), "Name should match the predefined string");
        assertEquals(Constants.CELLULAR, fStcaModel.getType(), "Type should be CELLULAR");
        assertEquals(RequestConstants.MAX_SPEED_REQUEST, fStcaModel.getParametersForGeneration(), "Should request MAX_SPEED for generation");
    }

    /**
     * test to verify that requestParameters returns a properly formatted string containing required constants
     **/
    @Test
    void requestParameters_ShouldReturnDelimitedString() {
        String requests = fStcaModel.requestParameters();
        assertNotNull(requests);
        assertTrue(requests.contains(RequestConstants.X_POSITION_REQUEST), "Should contain X_POSITION_REQUEST");
        assertTrue(requests.contains(RequestConstants.MAX_SPEED_REQUEST), "Should contain MAX_SPEED_REQUEST");
        assertTrue(requests.contains(RequestConstants.REQUEST_SEPARATOR), "Should use the correct separator");
    }

    /**
     * test to verify that requestParameters with Direction returns the correct specific requests
     **/
    @Test
    void requestParameters_WithDirection_ShouldReturnSpecificRequests() {
        String leftRequests = fStcaModel.requestParameters(Direction.LEFT);
        assertTrue(leftRequests.contains(RequestConstants.X_POSITION_LEFT_FORWARD_REQUEST), "Left request should contain left forward parameter");
        assertFalse(leftRequests.contains(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST), "Left request should not contain right forward parameter");

        String rightRequests = fStcaModel.requestParameters(Direction.RIGHT);
        assertTrue(rightRequests.contains(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST), "Right request should contain right forward parameter");

        String straightRequests = fStcaModel.requestParameters(Direction.STRAIGHT);
        assertEquals("", straightRequests, "Straight request should return an empty string");
    }

    /**
     * test to verify that changeLaneIfDesired (with specific direction) decides to change lane
     * when all conditions (need to change, advantage, safety) are met
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnLeft_WhenConditionsAreMet() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 2.0); // Car is close (needs to change)
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0); // Internal speed will be 2+1 = 3

        // Left lane conditions
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST, 6.0); // Big gap forward (advantage)
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST, 5.0); // Big gap backward (safe)

        Direction decision = fStcaModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.LEFT, decision, "Should decide to change to LEFT lane because it is advantageous and safe");
    }

    /**
     * test to verify that changeLaneIfDesired decides NOT to change lane
     * when the car has enough space in front (no need to change)
     **/
    @Test
    void changeLaneIfDesired_WithDirection_ShouldReturnStraight_WhenNoNeedToChange() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 10.0); // Big gap ahead, no need to change
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 4.0);

        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST, 15.0);
        params.put(RequestConstants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST, 10.0);

        Direction decision = fStcaModel.changeLaneIfDesired(params, Direction.LEFT);

        assertEquals(Direction.STRAIGHT, decision, "Should stay STRAIGHT if there is enough space in the current lane");
    }

    /**
     * test to verify that changeLaneIfDesired based on X positions successfully evaluates
     * a right lane change when the left lane is unavailable or not advantageous
     **/
    @Test
    void changeLaneIfDesired_ByPositions_ShouldReturnRight_WhenLeftIsBlocked() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.X_POSITION_REQUEST, 10.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 4.0); // Internal speed = 5

        // Car right in front
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 14.0);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 1.0); // Gap = 14 - 10 - 1 = 3 (needs change)

        // Left lane is blocked by a car right next to us
        params.put(RequestConstants.X_POSITION_LEFT_FORWARD_REQUEST, 12.0);
        params.put(RequestConstants.LENGTH_LEFT_FORWARD_REQUEST, 1.0); // Gap = 1
        params.put(RequestConstants.X_POSITION_LEFT_BACKWARD_REQUEST, 8.0);

        // Right lane is completely free
        params.put(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST, (double) Constants.NO_CAR_THERE);
        params.put(RequestConstants.LENGTH_RIGHT_FORWARD_REQUEST, 0.0);
        params.put(RequestConstants.X_POSITION_RIGHT_BACKWARD_REQUEST, (double) Constants.NO_CAR_THERE);

        Direction decision = fStcaModel.changeLaneIfDesired(params);

        assertEquals(Direction.RIGHT, decision, "Should decide to change RIGHT because left lane is blocked and right is free");
    }

    /**
     * test to verify that changeLaneIfDesired by positions returns STRAIGHT if both lanes are unsafe
     **/
    @Test
    void changeLaneIfDesired_ByPositions_ShouldReturnStraight_WhenBothLanesUnsafe() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.X_POSITION_REQUEST, 10.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 4.0);

        // Blocked in front
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 12.0);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 1.0);

        // Blocked on the left (unsafe backward gap)
        params.put(RequestConstants.X_POSITION_LEFT_FORWARD_REQUEST, 20.0);
        params.put(RequestConstants.LENGTH_LEFT_FORWARD_REQUEST, 1.0);
        params.put(RequestConstants.X_POSITION_LEFT_BACKWARD_REQUEST, 9.0); // Car right behind us in left lane

        // Blocked on the right (no advantage forward)
        params.put(RequestConstants.X_POSITION_RIGHT_FORWARD_REQUEST, 11.0);
        params.put(RequestConstants.LENGTH_RIGHT_FORWARD_REQUEST, 1.0);
        params.put(RequestConstants.X_POSITION_RIGHT_BACKWARD_REQUEST, 5.0);

        Direction decision = fStcaModel.changeLaneIfDesired(params);

        assertEquals(Direction.STRAIGHT, decision, "Should stay STRAIGHT if changing lanes is unsafe or not advantageous");
    }
}
