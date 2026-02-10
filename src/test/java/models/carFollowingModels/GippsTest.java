package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**************************************************
 * Unit tests for Gipps car-following model class.
 *
 * @author Michael Hladky
 * @version 1.0
 **************************************************/
class GippsTest {

    /**
     * helper to create a fresh Gipps instance for each test
     *
     * @return new Gipps instance
     **/
    private Gipps createGipps() {
        return new Gipps();
    }

    /**
     * helper to build parameter map for Gipps.getNewSpeed
     *
     * @param currentSpeed current speed of the vehicle
     * @param maxAcceleration maximum acceleration of the vehicle
     * @param maxSpeed maximum speed of the vehicle
     * @param maxRoadSpeed maximum speed allowed by the road
     * @param timeStep simulation time step
     * @param currentSpeedForward speed of the leading vehicle
     * @param minimumGap minimum gap to the leading vehicle
     * @param decelerationComfort comfortable deceleration of the vehicle
     * @param decelerationComfortForward comfortable deceleration of the leading vehicle
     * @param xPosition current x position of the vehicle
     * @param xPositionForward x position of the leading vehicle
     * @return HashMap with all parameters set
     **/
    private HashMap<String, Double> buildParameters(
            double currentSpeed,
            double maxAcceleration,
            double maxSpeed,
            double maxRoadSpeed,
            double timeStep,
            double currentSpeedForward,
            double minimumGap,
            double decelerationComfort,
            double decelerationComfortForward,
            double xPosition,
            double xPositionForward
    ) {
        HashMap<String, Double> params = new HashMap<>();

        params.put(RequestConstants.CURRENT_SPEED_REQUEST, currentSpeed);
        params.put(RequestConstants.MAX_ACCELERATION_REQUEST, maxAcceleration);
        params.put(RequestConstants.MAX_SPEED_REQUEST, maxSpeed);
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, maxRoadSpeed);
        params.put(RequestConstants.TIME_STEP_REQUEST, timeStep);
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, currentSpeedForward);
        params.put(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, minimumGap);
        params.put(RequestConstants.DECELERATION_COMFORT_REQUEST, decelerationComfort);
        params.put(RequestConstants.DECELERATION_COMFORT_REQUEST_STRAIGHT_FORWARD, decelerationComfortForward);
        params.put(RequestConstants.X_POSITION_REQUEST, xPosition);
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, xPositionForward);

        return params;
    }

    // ------------------------------------------------------------
    // simple getters and metadata tests
    // ------------------------------------------------------------

    /**
     * Test: getID() should return "gipps"
     **/
    @Test
    void getId_shouldReturnGipps() {
        Gipps gipps = createGipps();

        assertEquals("gipps", gipps.getID(), "Model ID should be 'gipps'.");
    }

    /**
     * Test: getName() should return: Gipps Car-Following Model
     **/
    @Test
    void getName_shouldReturnReadableName() {
        Gipps gipps = createGipps();

        assertEquals("Gipps Car-Following Model", gipps.getName(),
                "Model name should be human readable Gipps model name.");
    }

    /**
     * Test: getType() should return Constants.CONTINOUS
     **/
    @Test
    void getType_shouldReturnContinuousTypeConstant() {
        Gipps gipps = createGipps();

        assertEquals(Constants.CONTINUOUS, gipps.getType(),
                "Model type should match Constants.CONTINOUS.");
    }

    /**
     * Test: getCellSize() should return Constants.PARAMETER_UNDEFINED
     **/
    @Test
    void getCellSize_shouldReturnParameterUndefined() {
        Gipps gipps = createGipps();

        assertEquals(Constants.PARAMETER_UNDEFINED, gipps.getCellSize(),
                "Cell size should be PARAMETER_UNDEFINED for continuous model.");
    }

    /**
     * Test: requestParameters() should return correct request string
     **/
    @Test
    void requestParameters_shouldReturnCorrectRequestString() {
        Gipps gipps = createGipps();

        String[] expectedParams = {
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.MAX_ACCELERATION_REQUEST,
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.MAX_ROAD_SPEED_REQUEST,
                RequestConstants.TIME_STEP_REQUEST,
                RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.DECELERATION_COMFORT_REQUEST,
                RequestConstants.DECELERATION_COMFORT_REQUEST_STRAIGHT_FORWARD,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST
        };

        String expected = String.join(RequestConstants.REQUEST_SEPARATOR, expectedParams);

        assertEquals(expected, gipps.requestParameters(),
                "Request parameters string should contain all required parameters in the correct order.");
    }

    /**
     * Test: getParametersForGeneration() should return correct request string for generation
     **/
    @Test
    void getParametersForGeneration_shouldReturnCorrectRequestString() {
        Gipps gipps = createGipps();

        String[] expectedParams = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.MAX_ACCELERATION_REQUEST,
                RequestConstants.DECELERATION_COMFORT_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.LENGTH_REQUEST
        };

        String expected = String.join(RequestConstants.REQUEST_SEPARATOR, expectedParams);

        assertEquals(expected, gipps.getParametersForGeneration(),
                "Generation parameters string should contain all required parameters in the correct order.");
    }

    // ------------------------------------------------------------
    // getNewSpeed behaviour tests
    // ------------------------------------------------------------

    /**
     * Test: getNewSpeed should use free-flow speed formula when no leading car is present
     **/
    @Test
    void getNewSpeed_noLeadingCar_shouldUseFreeFlowSpeed() {
        Gipps gipps = createGipps();

        // given: car on an empty road (no leader)
        double currentSpeed = 10.0;
        double maxAcceleration = 1.0;
        double maxSpeed = 30.0;
        double maxRoadSpeed = 25.0; // lower than vehicle max -> should be used as desired speed
        double desiredSpeed = Math.min(maxRoadSpeed, maxSpeed); // = 25
        double timeStep = 1.0;

        double currentSpeedForward = Constants.NO_CAR_THERE;
        double minimumGap = 2.0;
        double decelerationComfort = 2.0;
        double decelerationComfortForward = 2.0;
        double xPosition = 0.0;
        double xPositionForward = Constants.NO_CAR_THERE;

        HashMap<String, Double> params = buildParameters(
                currentSpeed,
                maxAcceleration,
                maxSpeed,
                maxRoadSpeed,
                timeStep,
                currentSpeedForward,
                minimumGap,
                decelerationComfort,
                decelerationComfortForward,
                xPosition,
                xPositionForward
        );

        // when
        double newSpeed = gipps.getNewSpeed(params);

        // then
        assertFalse(Double.isNaN(newSpeed), "New speed should not be NaN.");
        assertTrue(newSpeed > currentSpeed,
                "New speed should be higher than current speed in free-flow conditions.");
        // expected value from the Gipps free-flow formula
        double expectedFreeFlowSpeed = currentSpeed
                + 2.5 * maxAcceleration * timeStep
                * (1 - currentSpeed / desiredSpeed)
                * Math.sqrt(0.025 + currentSpeed / desiredSpeed);

        assertEquals(expectedFreeFlowSpeed, newSpeed, 1e-9,
                "New speed should follow Gipps free-flow speed formula when no leader is present.");
    }

    /**
     * Test: getNewSpeed should limit speed by safe speed formula when a leading car is present
     **/
    @Test
    void getNewSpeed_withLeader_shouldBeLimitedBySafeSpeed() {
        Gipps gipps = createGipps();

        // given: follower approaches a leader -> safe speed should be restrictive
        double currentSpeed = 20.0;
        double maxAcceleration = 1.0;
        double maxSpeed = 30.0;
        double maxRoadSpeed = 30.0;
        double desiredSpeed = Math.min(maxRoadSpeed, maxSpeed);
        double timeStep = 1.0;

        double leadingSpeed = 15.0;
        double minimumGap = 2.0;
        double decelerationComfort = 2.0;            // > 0 in parameters
        double decelerationComfortForward = 2.0;     // > 0 in parameters
        double maxDeceleration = -decelerationComfort;
        double maxDecelerationFront = -decelerationComfortForward;

        double xPosition = 0.0;
        double leadingXPosition = 70.0; // finite but not too large gap

        HashMap<String, Double> params = buildParameters(
                currentSpeed,
                maxAcceleration,
                maxSpeed,
                maxRoadSpeed,
                timeStep,
                leadingSpeed,
                minimumGap,
                decelerationComfort,
                decelerationComfortForward,
                xPosition,
                leadingXPosition
        );

        // when
        double newSpeed = gipps.getNewSpeed(params);

        // then
        assertFalse(Double.isNaN(newSpeed), "New speed should not be NaN.");

        // compute expected free flow speed
        double freeFlowSpeed = currentSpeed
                + 2.5 * maxAcceleration * timeStep
                * (1 - currentSpeed / desiredSpeed)
                * Math.sqrt(0.025 + currentSpeed / desiredSpeed);

        // compute expected safe speed using the same formula as in the model
        double firstPart = maxDeceleration * timeStep;
        double positionGap = leadingXPosition - xPosition - minimumGap;
        double firstPartUnderSqrt = maxDeceleration * maxDeceleration * timeStep * timeStep;
        double endingPartBraces = currentSpeed * timeStep
                - (leadingSpeed * leadingSpeed) / maxDecelerationFront;
        double partUnderSqrt = firstPartUnderSqrt
                - maxDeceleration * (2 * positionGap - endingPartBraces);
        double safeSpeed = firstPart + Math.sqrt(partUnderSqrt);

        double expectedNewSpeed = Math.min(freeFlowSpeed, safeSpeed);

        assertEquals(expectedNewSpeed, newSpeed, 1e-9,
                "New speed should be the minimum of free-flow speed and safe speed.");
        assertTrue(newSpeed <= freeFlowSpeed + 1e-9,
                "New speed must not exceed free-flow speed when a leader is present.");
    }

    /**
     * Test: getNewSpeed should clamp safe speed to zero if it becomes NaN or negative
     **/
    @Test
    void getNewSpeed_whenSafeSpeedNaNOrNegative_shouldClampToZero() {
        Gipps gipps = createGipps();

        // given: parameters chosen such that term under sqrt in safeSpeed is negative
        // -> safeSpeed becomes NaN in internal computation and should be clamped to 0
        double currentSpeed = 30.0;
        double maxAcceleration = 1.0;
        double maxSpeed = 30.0;
        double maxRoadSpeed = 30.0;
        double timeStep = 1.0;

        double leadingSpeed = 0.0;
        double minimumGap = 10.0;
        double decelerationComfort = 2.0;
        double decelerationComfortForward = 2.0;
        double xPosition = 0.0;
        double leadingXPosition = 5.0; // closer than minimum gap -> problematic safe speed

        HashMap<String, Double> params = buildParameters(
                currentSpeed,
                maxAcceleration,
                maxSpeed,
                maxRoadSpeed,
                timeStep,
                leadingSpeed,
                minimumGap,
                decelerationComfort,
                decelerationComfortForward,
                xPosition,
                leadingXPosition
        );

        // when
        double newSpeed = gipps.getNewSpeed(params);

        // then
        // In this configuration, safeSpeed becomes NaN -> clamped to 0 -> min(freeFlowSpeed, 0) = 0
        assertEquals(0.0, newSpeed, 1e-9,
                "New speed should be clamped to 0 when safe speed is NaN or negative.");
    }
}

