package models.carFollowingModels;

import core.utils.RandomNumberGenerator;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/****************************************************
 * Unit tests for the KKW_Linear car-following model, focusing on basic properties,
 * parameter requests, and the complex combination of deterministic speed
 * synchronization and stochastic (random) modifications.
 *
 * @author Michael Hladky
 * @version 1.0
 *****************************************************/
public class KKW_LinearTest {

    /** instance of the KKW_Linear model to be used in tests **/
    private KKW_Linear kkwLinearModel;

    /**
     * setup method to initialize a fresh instance of the model before each test
     **/
    @BeforeEach
    void setUp() {
        kkwLinearModel = new KKW_Linear();
    }

    /**
     * Helper method to dynamically find and set a seed for RandomNumberGenerator.
     * Ensures tests trigger specific probability branches in KKW_Linear.
     *
     * @param desiredOutcome -1 for slowdown, 1 for acceleration, 0 for no change
     * @param chanceB the threshold for slowdown
     * @param chanceA_plus_B the threshold for acceleration
     */
    private void setSeedForTest(int desiredOutcome, double chanceB, double chanceA_plus_B) {
        long seed = 0;
        while (true) {
            java.util.Random tempRandom = new java.util.Random(seed);
            double val = tempRandom.nextDouble();
            if (desiredOutcome == -1 && val < chanceB) {
                RandomNumberGenerator.getInstance(0).resetSeed(seed);
                break;
            } else if (desiredOutcome == 1 && val >= chanceB && val < chanceA_plus_B) {
                RandomNumberGenerator.getInstance(0).resetSeed(seed);
                break;
            } else if (desiredOutcome == 0 && val >= chanceA_plus_B) {
                RandomNumberGenerator.getInstance(0).resetSeed(seed);
                break;
            }
            seed++;
        }
    }

    /**
     * Helper method to create standard parameters for tests
     **/
    private HashMap<String, Double> createBaseParams() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 10.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 3.0);
        params.put(RequestConstants.TIME_STEP_REQUEST, 1.0);
        params.put(RequestConstants.X_POSITION_REQUEST, 0.0);

        // Setup distance to 10 cells
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 12.0);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 1.0);
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, 3.0);

        return params;
    }

    /**
     * test to verify basic getter methods of the model
     **/
    @Test
    void testBasicProperties() {
        assertEquals("kkw-linear", kkwLinearModel.getID(), "ID should be 'kkw-linear'");
        assertEquals("Kerner-Klenov-Wolf (linear)", kkwLinearModel.getName(), "Name should match predefined string");
        assertEquals(Constants.CELLULAR, kkwLinearModel.getType(), "Type should be CELLULAR");
        assertEquals(1.5, kkwLinearModel.getCellSize(), "Cell size should be 1.5");

        String genParams = kkwLinearModel.getParametersForGeneration();
        assertTrue(genParams.contains(RequestConstants.MAX_SPEED_REQUEST));
        assertTrue(genParams.contains(RequestConstants.LENGTH_REQUEST));
    }

    /**
     * test to verify that getSynchronizationGap calculates the correct value based on the formula
     **/
    @Test
    void getSynchronizationGap_ShouldWorkAsExpected() {
        double currentSpeed = 5.0;
        double d = 2.0;
        double k = 1.0;
        double dt = 1.5;

        // Formula: (int) (d + k * currentSpeed * dt) -> 2 + 1 * 5 * 1.5 = 9.5 -> int = 9
        int expected = 9;
        int result = kkwLinearModel.getSynchronizationGap(currentSpeed, d, k, dt);

        assertEquals(expected, result, "Synchronization gap should be calculated via floor(d + k * v * dt)");
    }

    /**
     * test to verify that the model returns deterministic speed when random condition hits the "no change" branch
     **/
    @Test
    void getNewSpeed_ShouldReturnDeterministicSpeed_WhenNoRandomChange() {
        // currentSpeed = 3.0 -> chanceB = 0.3, chanceA = 0.02 -> chanceA_plus_B = 0.32
        setSeedForTest(0, 0.3, 0.32);

        HashMap<String, Double> params = createBaseParams();
        double result = kkwLinearModel.getNewSpeed(params);

        // Distance = (12 - 0 - 1) - 1 = 10
        // SafeSpeed = 10 / 1 = 10
        // SyncGap = 2 + 1*3*1 = 5
        // SyncSpeed (distance > syncGap) = 3 + 1 = 4
        // Deterministic = min(10, 10, 4) = 4
        assertEquals(4.0, result, 0.001, "New speed should equal deterministic speed (4.0) with no random change");
    }

    /**
     * test to verify the random slowdown branch (random modification = -1)
     **/
    @Test
    void getNewSpeed_ShouldReduceSpeed_WhenRandomSlowdownTriggers() {
        setSeedForTest(-1, 0.3, 0.32);

        HashMap<String, Double> params = createBaseParams();
        double result = kkwLinearModel.getNewSpeed(params);

        // Deterministic is 4. Random drops it by 1 * acceleration(1.0) = 3.0
        assertEquals(3.0, result, 0.001, "Random slowdown should reduce deterministic speed by 1");
    }

    /**
     * test to verify the random acceleration branch (random modification = 1)
     * using a scenario where sync gap causes braking, but random chance accelerates it
     **/
    @Test
    void getNewSpeed_ShouldIncreaseSpeed_WhenRandomAccelerationTriggers() {
        // High speed scenario: speed = 10 -> chanceA = 0.02, chanceB = 0.3 -> A+B = 0.32
        setSeedForTest(1, 0.3, 0.32);

        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.MAX_SPEED_REQUEST, 30.0);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 10.0);
        params.put(RequestConstants.TIME_STEP_REQUEST, 1.0);
        params.put(RequestConstants.X_POSITION_REQUEST, 0.0);

        // distance to next car = 10 cells (12 - 0 - 1 = 11 -> distance math = 10)
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 12.0);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 1.0);
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, 5.0); // Slower car ahead

        double result = kkwLinearModel.getNewSpeed(params);

        // Deterministic speed would be 9. Random adds 1. SmallestSpeed limit is 10. Result = 10.
        assertEquals(10.0, result, 0.001, "Random acceleration should bump the speed back up to 10");
    }

    /**
     * test to verify handling when there is no car ahead (Constants.NO_CAR_THERE)
     **/
    @Test
    void getNewSpeed_ShouldAssumeFreeSpeed_WhenNoCarAhead() {
        setSeedForTest(0, 0.3, 0.32);

        HashMap<String, Double> params = createBaseParams();
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, (double) Constants.NO_CAR_THERE);
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, (double) Constants.NO_CAR_THERE);

        double result = kkwLinearModel.getNewSpeed(params);

        // Distance is MAX_VALUE. Deterministic should just accelerate by 1.
        assertEquals(4.0, result, 0.001, "Should accelerate safely when no car is ahead");
    }
}