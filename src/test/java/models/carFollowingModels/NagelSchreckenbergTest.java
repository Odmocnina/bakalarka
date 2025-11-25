package models.carFollowingModels;

import core.utils.Constants;
import core.utils.RequestConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

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
    private HashMap<String, Double> buildParams(double currentSpeed,
                                                double maxSpeed,
                                                double distanceToNextCar) {
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

    // ------------------------------------------------------------------
    // basic metadata & getters
    // ------------------------------------------------------------------

    @Test
    void getId_shouldReturnNagelSchreckenberg() {
        NagelSchreckenberg model = createModel();

        assertEquals("nagelschreckenberg", model.getID(),
                "Model ID should be 'nagelschreckenberg'.");
    }

    @Test
    void getName_shouldReturnReadableName() {
        NagelSchreckenberg model = createModel();

        assertEquals("Nagel-Schreckenberg Model", model.getName(),
                "Model name should be the human readable Nagel-Schreckenberg name.");
    }

    @Test
    void getType_shouldReturnCellularTypeConstant() {
        NagelSchreckenberg model = createModel();

        assertEquals(Constants.CELLULAR, model.getType(),
                "Model type should be Constants.CELLULAR.");
    }

    @Test
    void getCellSize_shouldReturnExpectedCellSize() {
        NagelSchreckenberg model = createModel();

        assertEquals(7.5, model.getCellSize(), 1e-9,
                "Cell size should be 7.5 meters.");
    }

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

    @Test
    void getNewSpeed_largeGap_shouldIncreaseByAtMostOneAndNotDecreaseBelowCurrent() {
        NagelSchreckenberg model = createModel();

        // For a large distance: step 1 increases by +1 (if below max),
        // step 2 does nothing (no need to brake),
        // step 3 may or may not decrease by 1 due to randomization.
        //
        // So final speed is either:
        //  - currentSpeed + 1 (no random slowdown), or
        //  - currentSpeed     (random slowdown applied).
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

    @Test
    void getNewSpeed_smallGap_shouldLimitByDistanceToNextCar() {
        NagelSchreckenberg model = createModel();

        // Here we force a small gap: after acceleration, car must brake to keep at least one empty cell.
        //
        // Example:
        //   currentSpeed = 4 -> after accel = 5
        //   distanceInCells = 2 => since 2 <= 5, speed becomes 1 = distanceInCells - 1
        //   randomization may reduce to 0, but never above 1.
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
