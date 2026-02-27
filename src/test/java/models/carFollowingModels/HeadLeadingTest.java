package models.carFollowingModels;

import core.utils.RandomNumberGenerator;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/****************************************************
 * Unit tests for the HeadLeading (JUTS) car-following model, focusing on basic properties,
 * parameter requests, and the specific cellular rules for acceleration, deceleration,
 * and randomized slowing down (dawdling).
 *
 * @author Michael Hladky
 * @version 1.0
 *****************************************************/
public class HeadLeadingTest {

    /** instance of the HeadLeading model to be used in tests **/
    private HeadLeading headLeadingModel;

    /**
     * setup method to initialize a fresh instance of the HeadLeading model before each test
     **/
    @BeforeEach
    void setUp() {
        headLeadingModel = new HeadLeading();
    }

    /**
     * Helper method to dynamically find and set a seed for RandomNumberGenerator.
     * This ensures the tests are 100% deterministic regardless of JDK version.
     *
     * @param triggerProbability true if we want the random event to happen (val < threshold), false to bypass it
     * @param threshold the probability threshold (e.g., 0.3 or 0.5)
     */
    private void setSeedForTest(boolean triggerProbability, double threshold) {
        long seed = 0;
        while (true) {
            java.util.Random tempRandom = new java.util.Random(seed);
            double val = tempRandom.nextDouble();
            if (triggerProbability && val < threshold) {
                RandomNumberGenerator.getInstance(0).resetSeed(seed);
                break;
            } else if (!triggerProbability && val >= threshold) {
                RandomNumberGenerator.getInstance(0).resetSeed(seed);
                break;
            }
            seed++;
        }
    }

    /**
     * test to verify basic getter methods of the model
     **/
    @Test
    void testBasicProperties() {
        assertEquals("juts", headLeadingModel.getID(), "ID should be 'juts'");
        assertEquals("JUTS (Java urban traffic simulation) Model", headLeadingModel.getName(), "Name should match JUTS");
        assertEquals(Constants.CELLULAR, headLeadingModel.getType(), "Type should be CELLULAR");
        assertEquals(2.5, headLeadingModel.getCellSize(), "Cell size should be exactly 2.5 meters");

        String genParams = headLeadingModel.getParametersForGeneration();
        assertTrue(genParams.contains(RequestConstants.MAX_SPEED_REQUEST), "Generation params should include max speed");
        assertTrue(genParams.contains(RequestConstants.LENGTH_REQUEST), "Generation params should include length");
    }

    /**
     * test to verify that requestParameters returns a properly formatted string containing required constants
     **/
    @Test
    void requestParameters_ShouldReturnDelimitedString() {
        String requests = headLeadingModel.requestParameters();
        assertNotNull(requests);
        assertTrue(requests.contains(RequestConstants.CURRENT_SPEED_REQUEST), "Should request current speed");
        assertTrue(requests.contains(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST), "Should request leading car position");
        assertTrue(requests.contains(RequestConstants.REQUEST_SEPARATOR), "Should use the correct separator");
    }

    /**
     * test to verify that the car accelerates by 1 when the road is clear and
     * no random slow-down occurs
     **/
    @Test
    void getNewSpeed_ShouldAccelerate_WhenRoadIsClear() {
        // Find a seed where nextDouble() >= 0.3 (random slow-down will NOT happen)
        setSeedForTest(false, 0.3);

        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 2.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.X_POSITION_REQUEST, 10.0);

        // No car ahead
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, (double) Constants.NO_CAR_THERE);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 0.0);

        double newSpeed = headLeadingModel.getNewSpeed(params);

        // Current speed 2 -> accelerates to 3 -> no obstacle -> no random slowdown -> returns 3
        assertEquals(3.0, newSpeed, "Should accelerate by 1 cell/step when the road ahead is clear");
    }

    /**
     * test to verify that the car slows down to distanceInCells - 1 when approaching an obstacle
     **/
    @Test
    void getNewSpeed_ShouldDecelerate_WhenCarAheadIsClose() {
        // Find a seed where nextDouble() >= 0.3 (random slow-down will NOT happen)
        setSeedForTest(false, 0.3);

        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 4.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.X_POSITION_REQUEST, 10.0);

        // Car ahead is at position 14, length is 1. Distance = 14 - 10 - 1 = 3 cells.
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 14.0);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 1.0);

        double newSpeed = headLeadingModel.getNewSpeed(params);

        // Speed increases to 5, but distance is 3.
        // Rule: if distance <= speed, speed = distance - 1. So speed becomes 3 - 1 = 2.
        assertEquals(2.0, newSpeed, "Should decelerate to avoid collision (speed = distance - 1)");
    }

    /**
     * test to verify the randomized dawdling (slow down) effect when the car is already moving.
     * The model has a 30% chance to reduce speed by 1.
     **/
    @Test
    void getNewSpeed_ShouldRandomlySlowDown_WhenMoving() {
        // Find a seed where nextDouble() < 0.3 (random slow-down WILL happen)
        setSeedForTest(true, 0.3);

        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 3.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.X_POSITION_REQUEST, 10.0);

        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, (double) Constants.NO_CAR_THERE);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 0.0);

        double newSpeed = headLeadingModel.getNewSpeed(params);

        // Speed increases to 4 -> Random check passes (< 0.3) -> Speed reduced by 1 -> Returns 3
        assertEquals(3.0, newSpeed, "Should accelerate to 4, but then randomly slow down back to 3 due to the dawdling probability");
    }

    /**
     * test to verify the randomized dawdling effect when the car is starting from 0.
     * The model uses a higher 50% chance to stay at 0 to simulate delayed starts.
     **/
    @Test
    void getNewSpeed_ShouldRandomlySlowDown_WhenStarting() {
        // Find a seed where nextDouble() < 0.5 (random slow-down WILL happen)
        setSeedForTest(true, 0.5);

        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 0.0); // Car is stopped (starting = true)
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.X_POSITION_REQUEST, 10.0);

        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, (double) Constants.NO_CAR_THERE);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 0.0);

        double newSpeed = headLeadingModel.getNewSpeed(params);

        // Starts at 0 -> accelerates to 1 -> Random check passes (< 0.5) -> reduced to 0 -> Returns 0
        assertEquals(0.0, newSpeed, "Should fail to start (speed stays 0) due to the higher starting slow-down probability");
    }

    /**
     * test to verify that the math.max(0, currentSpeed) safeguard prevents negative speeds
     * if the distance to the next car is 0 or negative.
     **/
    @Test
    void getNewSpeed_ShouldNotReturnNegativeSpeed_WhenDistanceIsZero() {
        // Seed doesn't matter here because currentSpeed drops to -1, bypassing the random check
        setSeedForTest(false, 0.3);

        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 0.0);
        params.put(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        params.put(RequestConstants.X_POSITION_REQUEST, 10.0);

        // Car right in front of us, distance = 11 - 10 - 1 = 0
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 11.0);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 1.0);

        double newSpeed = headLeadingModel.getNewSpeed(params);

        // Speed increases to 1. Distance is 0. Speed becomes 0 - 1 = -1.
        // Math.max(0, -1) limits it to 0.
        assertEquals(0.0, newSpeed, "Should cap the minimum speed at 0 to prevent negative speeds in a collision state");
    }
}