package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/****************************************************
 * Unit tests for the Gipps car-following model, focusing on basic properties,
 * parameter requests, and the mathematical logic for calculating new speeds
 * (free-flow speed vs. safe speed).
 *
 * @author Michael Hladky
 * @version 1.0
 *****************************************************/
public class GippsTest {

    /** instance of the Gipps model to be used in tests **/
    private Gipps gippsModel;

    /**
     * setup method to initialize a fresh instance of the Gipps model before each test
     **/
    @BeforeEach
    void setUp() {
        gippsModel = new Gipps();
    }

    /**
     * test to verify basic getter methods of the model
     **/
    @Test
    void testBasicProperties() {
        assertEquals("gipps", gippsModel.getID(), "ID should be 'gipps'");
        assertEquals("Gipps Car-Following Model", gippsModel.getName(), "Name should match the predefined string");
        assertEquals(Constants.CONTINUOUS, gippsModel.getType(), "Type should be CONTINUOUS");
        assertEquals(Constants.PARAMETER_UNDEFINED, gippsModel.getCellSize(), "Cell size should be UNDEFINED for continuous models");

        String genParams = gippsModel.getParametersForGeneration();
        assertTrue(genParams.contains(RequestConstants.MAX_SPEED_REQUEST), "Generation params should include max speed");
        assertTrue(genParams.contains(RequestConstants.DECELERATION_COMFORT_REQUEST), "Generation params should include deceleration comfort");
    }

    /**
     * test to verify that requestParameters returns a properly formatted string containing required constants
     **/
    @Test
    void requestParameters_ShouldReturnDelimitedString() {
        String requests = gippsModel.requestParameters();
        assertNotNull(requests);
        assertTrue(requests.contains(RequestConstants.CURRENT_SPEED_REQUEST), "Should request current speed");
        assertTrue(requests.contains(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST), "Should request leading car position");
        assertTrue(requests.contains(RequestConstants.REQUEST_SEPARATOR), "Should use the correct separator");
    }

    /**
     * test to verify that the model calculates and returns the free-flow speed
     * when there is no car ahead (leadingXPosition == Constants.NO_CAR_THERE)
     **/
    @Test
    void getNewSpeed_ShouldReturnFreeFlowSpeed_WhenNoCarAhead() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 10.0);
        params.put(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 20.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 30.0);
        params.put(RequestConstants.TIME_STEP_REQUEST, 1.0);
        params.put(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        params.put(RequestConstants.DECELERATION_COMFORT_REQUEST, 3.0);
        params.put(RequestConstants.X_POSITION_REQUEST, 0.0);

        // No car ahead
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, (double) Constants.NO_CAR_THERE);

        double newSpeed = gippsModel.getNewSpeed(params);

        // Desired speed = min(20, 30) = 20
        // Expected free-flow math: 10 + 2.5 * 2 * 1 * (1 - 10/20) * sqrt(0.025 + 10/20)
        // 10 + 5 * 0.5 * sqrt(0.525) = 10 + 2.5 * 0.7245688 = 11.8114...
        assertEquals(11.811, newSpeed, 0.01, "Should return correct free-flow speed when no car is ahead");
    }

    /**
     * test to verify that the model returns the free-flow speed when the car ahead
     * is so far away that the safe speed is much higher than the free-flow speed
     **/
    @Test
    void getNewSpeed_ShouldReturnFreeFlowSpeed_WhenCarAheadIsFar() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 10.0);
        params.put(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 20.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 30.0);
        params.put(RequestConstants.TIME_STEP_REQUEST, 1.0);
        params.put(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        params.put(RequestConstants.DECELERATION_COMFORT_REQUEST, 3.0);
        params.put(RequestConstants.X_POSITION_REQUEST, 0.0);

        // Car ahead is 1000 meters away (very far)
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 1000.0);
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, 20.0);

        double newSpeed = gippsModel.getNewSpeed(params);

        // Should be bounded by the same free-flow calculation as the NoCarAhead test
        assertEquals(11.811, newSpeed, 0.01, "Should return free-flow speed because safe speed is very high");
    }

    /**
     * test to verify that the model calculates and returns the safe speed
     * when the car ahead is close and driving slower, forcing the car to brake
     **/
    @Test
    void getNewSpeed_ShouldReturnSafeSpeed_WhenCarAheadIsClose() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 15.0); // We are fast
        params.put(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 20.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 30.0);
        params.put(RequestConstants.TIME_STEP_REQUEST, 1.0);
        params.put(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        params.put(RequestConstants.DECELERATION_COMFORT_REQUEST, 3.0);
        params.put(RequestConstants.X_POSITION_REQUEST, 0.0);

        // Car ahead is very close (20m) and stopped
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 20.0);
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, 0.0);

        double newSpeed = gippsModel.getNewSpeed(params);

        // Since the car is close and stopped, the safe speed must be significantly lower than the current speed
        assertTrue(newSpeed < 15.0, "The calculated safe speed should force the car to decelerate because of the obstacle");
    }

    /**
     * test to verify that if the math for safe speed breaks (e.g., negative square root leading to NaN,
     * or a negative speed calculation due to imminent collision), the model caps the speed at 0.0
     **/
    @Test
    void getNewSpeed_ShouldPreventNegativeOrNaNSpeed() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 20.0);
        params.put(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 20.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 20.0);
        params.put(RequestConstants.TIME_STEP_REQUEST, 1.0);
        params.put(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        params.put(RequestConstants.DECELERATION_COMFORT_REQUEST, 1.0);
        params.put(RequestConstants.X_POSITION_REQUEST, 50.0);
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 40.0);
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, 0.0);

        double newSpeed = gippsModel.getNewSpeed(params);

        // Model contains a failsafe: if (safeSpeed < 0 || Double.isNaN(safeSpeed)) { safeSpeed = 0; }
        assertEquals(0.0, newSpeed, "The speed should be bounded to 0.0 in case of an imminent collision or negative gap");
    }
}