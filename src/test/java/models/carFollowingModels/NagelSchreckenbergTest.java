package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**********************************
 * Unit tests for the Nagel-Schreckenberg cellular automaton car-following model.
 * These tests focus on verifying the model's metadata, parameter requests, and
 * the behavior of the getNewSpeed() method under various conditions (e.g., large gap, small gap).
 *
 * @author Michael Hladky
 * @version 1.0
 **********************************/
class NagelSchreckenbergTest {

    /**
     * helper to create a fresh model instance
     */
    private NagelSchreckenberg createModel() {
        return new NagelSchreckenberg();
    }

    /**
     * helper to build parameter map for getNewSpeed()
     */
    private HashMap<String, Double> buildParams(double currentSpeed, double maxSpeed, double distanceToNextCar) {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, currentSpeed);
        params.put(RequestConstants.MAX_SPEED_REQUEST, maxSpeed);

        // encode distanceToNextCar using positions and length
        double xPosition = 0.0;
        double lengthStraightForward = 1.0;
        double xPositionStraightForward = xPosition + lengthStraightForward + distanceToNextCar;

        params.put(RequestConstants.X_POSITION_REQUEST, xPosition);
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, xPositionStraightForward);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, lengthStraightForward);

        return params;
    }

    /**
     * Helper method to dynamically find and set a seed for RandomNumberGenerator.
     * This ensures the tests are 100% deterministic regardless of JDK version.
     *
     * @param triggerProbability true if we want the random event to happen, false to bypass it
     * @param threshold the probability threshold to test against
     */
    private void setSeedForTest(boolean triggerProbability, double threshold) {
        long seed = 0;
        while (true) {
            java.util.Random tempRandom = new java.util.Random(seed);
            double val = tempRandom.nextDouble();
            if (triggerProbability && val < threshold) {
                core.utils.RandomNumberGenerator.getInstance(0).resetSeed(seed);
                break;
            } else if (!triggerProbability && val >= threshold) {
                core.utils.RandomNumberGenerator.getInstance(0).resetSeed(seed);
                break;
            }
            seed++;
        }
    }

    /**
     * test for id
     **/
    @Test
    void getId_shouldReturnNagelSchreckenberg() {
        NagelSchreckenberg model = createModel();

        assertEquals("nagel-schreckenberg", model.getID(),
                "Model ID should be 'nagel-schreckenberg'.");
    }

    /**
     * test for name
     **/
    @Test
    void getName_shouldReturnReadableName() {
        NagelSchreckenberg model = createModel();

        assertEquals("Nagel-Schreckenberg Model", model.getName(),
                "Model name should be the human readable Nagel-Schreckenberg name.");
    }

    /**
     * test for type
     **/
    @Test
    void getType_shouldReturnCellularTypeConstant() {
        NagelSchreckenberg model = createModel();

        assertEquals(Constants.CELLULAR, model.getType(),
                "Model type should be Constants.CELLULAR.");
    }

    /**
     * test for cell size
     **/
    @Test
    void getCellSize_shouldReturnExpectedCellSize() {
        NagelSchreckenberg model = createModel();

        assertEquals(7.5, model.getCellSize(), 1e-9,
                "Cell size should be 7.5 meters.");
    }

    /**
     * test for request parameters
     **/
    @Test
    void requestParameters_shouldReturnCorrectRequestString() {
        NagelSchreckenberg model = createModel();

        String[] expected = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST
        };
        String expectedString = String.join(RequestConstants.REQUEST_SEPARATOR, expected);

        assertEquals(expectedString, model.requestParameters(),
                "Request parameters string should contain all required parameters in the correct order.");
    }

    /**
     * test for generation parameters
     **/
    @Test
    void getParametersForGeneration_shouldReturnCorrectRequestString() {
        NagelSchreckenberg model = createModel();

        String[] expected = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.LENGTH_REQUEST
        };
        String expectedString = String.join(RequestConstants.REQUEST_SEPARATOR, expected);

        assertEquals(expectedString, model.getParametersForGeneration(),
                "Generation parameters string should contain all required parameters in the correct order.");
    }

    // ------------------------------------------------------------------
    // getNewSpeed() behaviour and invariants
    // ------------------------------------------------------------------

    /**
     * getNewSpeed() should never return a negative speed (should be clamped to 0).
     **/
    @Test
    void getNewSpeed_shouldNeverBeNegative() {
        NagelSchreckenberg model = createModel();

        // distanceInCells = 0 => after steps speed becomes -1 before max(..,0)
        HashMap<String, Double> params = buildParams(
                0.0,    // current speed
                5.0,    // max speed
                0.0     // distance to next car
        );

        double newSpeed = model.getNewSpeed(params);

        assertTrue(newSpeed >= 0.0,
                "New speed must never be negative (should be clamped to 0).");
    }

    /**
     * test to verify that the model correctly applies the random slowdown (dawdling)
     * when the random number falls below the probability threshold.
     **/
    @Test
    void getNewSpeed_ShouldRandomlySlowDown() {
        setSeedForTest(true, 0.1);

        NagelSchreckenberg model = createModel();

        HashMap<String, Double> params = buildParams(3.0, 5.0, 100.0);

        double newSpeed = model.getNewSpeed(params);

        assertEquals(3.0, newSpeed, "The speed should decrease by 1 due to random dawdling.");
    }

    /**
     * test to verify that the model does NOT apply the random slowdown
     * when the random number is above the probability threshold.
     **/
    @Test
    void getNewSpeed_ShouldNotRandomlySlowDown() {
        setSeedForTest(false, 0.9);

        NagelSchreckenberg model = createModel();

        HashMap<String, Double> params = buildParams(3.0, 5.0, 100.0);

        double newSpeed = model.getNewSpeed(params);

        assertEquals(4.0, newSpeed, "The speed should NOT decrease because the random chance was bypassed.");
    }

    /**
     * getNewSpeed() should never return a speed above the max speed (should be clamped to max speed).
     **/
    @Test
    void getNewSpeed_shouldNotExceedMaxSpeed() {
        NagelSchreckenberg model = createModel();

        HashMap<String, Double> params = buildParams(
                3.0,    // current speed
                5.0,    // max speed
                100.0   // large distance so only acceleration + possible random slowdown
        );

        double newSpeed = model.getNewSpeed(params);

        assertTrue(newSpeed <= 5.0,
                "New speed must never exceed max speed.");
    }

    /**
     * getNewSpeed() should increase by at most 1 cell per step when there is a large gap ahead,
     * and should not decrease below the current speed (except for random slowdown).
     **/
    @Test
    void getNewSpeed_largeGap_shouldIncreaseByAtMostOneAndNotDecreaseBelowCurrent() {
        NagelSchreckenberg model = createModel();
        double currentSpeed = 2.0;
        double maxSpeed = 5.0;

        HashMap<String, Double> params = buildParams(
                currentSpeed,
                maxSpeed,
                100.0   // large distance
        );

        double newSpeed = model.getNewSpeed(params);

        assertTrue(newSpeed >= currentSpeed,
                "With large gap, new speed should be at least current speed (or one step above).");
        assertTrue(newSpeed <= currentSpeed + 1.0,
                "With large gap, new speed should not increase by more than 1 cell per step.");
    }

    /**
     * getNewSpeed() should reduce speed to at most distanceInCells - 1 when the gap is small,
     * and should not go negative (should be clamped to 0).
     **/
    @Test
    void getNewSpeed_smallGap_shouldLimitByDistanceToNextCar() {
        NagelSchreckenberg model = createModel();

        double currentSpeed = 4.0;
        double maxSpeed = 5.0;
        double distanceToNextCar = 2.0; // distanceInCells = 2

        HashMap<String, Double> params = buildParams(
                currentSpeed,
                maxSpeed,
                distanceToNextCar
        );

        double newSpeed = model.getNewSpeed(params);

        assertTrue(newSpeed <= 1.0,
                "With small gap, speed must be reduced to at most distanceInCells - 1.");
        assertTrue(newSpeed >= 0.0,
                "Speed must still be non-negative after braking and randomization.");
    }

    /**
     * getNewSpeed() should always return an integral value (in cells per time step) since it's a cellular automaton model.
     **/
    @Test
    void getNewSpeed_resultShouldBeIntegralValue() {
        NagelSchreckenberg model = createModel();

        HashMap<String, Double> params = buildParams(
                3.0,
                5.0,
                10.0
        );

        double newSpeed = model.getNewSpeed(params);

        assertEquals(Math.rint(newSpeed), newSpeed,
                "Speed in Nagel-Schreckenberg cellular model should be integer-valued (in cells per time step).");
    }
}
