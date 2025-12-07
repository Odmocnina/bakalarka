package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class HeadLeadingTest {

    /**
     * Helper to create a HeadLeading instance
     *
     * @return HeadLeading instance
     **/
    private HeadLeading create() {
        return new HeadLeading();
    }

    /**
     * Helper to build parameter map for getNewSpeed()
     *
     * @param currentSpeed current speed of the car
     * @param maxSpeed     maximum speed of the car
     * @param distance     distance to the next car (in cells)
     * @return HashMap with parameters
     **/
    private HashMap<String, Double> buildParams(double currentSpeed, double maxSpeed, double distance) {
        HashMap<String, Double> map = new HashMap<>();

        map.put(RequestConstants.CURRENT_SPEED_REQUEST, currentSpeed);
        map.put(RequestConstants.MAX_SPEED_REQUEST, maxSpeed);

        // For distance = x2 - x1 - length. Choose x1 = 0, length = 1 → x2 = distance + 1.
        double xPosition = 0.0;
        double lengthStraightForward = 1.0;
        double xPositionStraightForward = distance + xPosition + lengthStraightForward;

        map.put(RequestConstants.X_POSITION_REQUEST, xPosition);
        map.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, xPositionStraightForward);
        map.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, lengthStraightForward);

        return map;
    }

    /**
     * Constructor helper with injected Random for testing purposes
     *
     * @param fixedValue the fixed value to return from Random.nextDouble()
     **/
    private HeadLeading createWithFixedRandom(double fixedValue) {
        Random fixedRandom = new Random() {
            @Override
            public double nextDouble() {
                return fixedValue;
            }
        };
        return new HeadLeading(fixedRandom);
    }

    // ------------------------------------------------------
    // Getter tests
    // ------------------------------------------------------

    /**
     * Test: getID() should return "head-leading"
     **/
    @Test
    void getId_shouldReturnHeadLeading() {
        HeadLeading hl = create();
        assertEquals("head-leading", hl.getID(), "ID should be 'head-leading'.");
    }

    /**
     * Test: getName() should return "Head-leading algorithm"
     **/
    @Test
    void getName_shouldReturnText() {
        HeadLeading hl = create();
        assertEquals("Head-leading algorithm", hl.getName(),
                "Name should be the readable algorithm name.");
    }

    /**
     * Test: getType() should return Constants.CELLULAR
     **/
    @Test
    void getType_shouldReturnCellular() {
        HeadLeading hl = create();
        assertEquals(Constants.CELLULAR, hl.getType(),
                "Type should be Constants.CELLULAR.");
    }

    /**
     * Test: getCellSize() should return 2.5
     **/
    @Test
    void getCellSize_shouldReturnFixedValue() {
        HeadLeading hl = create();
        assertEquals(2.5, hl.getCellSize(), 1e-9,
                "Cell size should be 2.5 meters.");
    }

    /**
     * Test: requestParameters() should return expected parameters
     **/
    @Test
    void requestParameters_shouldMatchExpected() {
        HeadLeading hl = create();

        String[] expected = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST
        };

        assertEquals(String.join(RequestConstants.REQUEST_SEPARATOR, expected),
                hl.requestParameters(),
                "Request parameters must match expected order.");
    }

    /**
     * Test: getParametersForGeneration() should return expected parameters
     **/
    @Test
    void getParametersForGeneration_shouldMatchExpected() {
        HeadLeading hl = create();

        String[] expected = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.LENGTH_REQUEST
        };

        assertEquals(String.join(RequestConstants.REQUEST_SEPARATOR, expected),
                hl.getParametersForGeneration(),
                "Generation parameters must match expected order.");
    }

    // ------------------------------------------------------
    // getNewSpeed() tests
    // ------------------------------------------------------

    /**
     * Test: getNewSpeed() should accelerate by +1 when below max speed
     **/
    @Test
    void getNewSpeed_shouldAccelerateWhenBelowMaxSpeed() {
        HeadLeading hl = createWithFixedRandom(1.0);
        var params = buildParams(2, 5, 10);

        double newSpeed = hl.getNewSpeed(params);

        assertEquals(3, newSpeed,
                "Car should accelerate by +1 when below max speed and no slowdown happens.");
    }

    /**
     * Test: getNewSpeed() should not crash to next car
     **/
    @Test
    void getNewSpeed_shouldLimitByDistanceToNextCar() {
        HeadLeading hl = createWithFixedRandom(1.0);
        var params = buildParams(4, 10, 3);

        double newSpeed = hl.getNewSpeed(params);

        assertEquals(2, newSpeed,
                "Speed should be distanceInCells - 1 when too close to the next car.");
    }

    /**
     * Test: getNewSpeed() should apply random slowdown based on slowdownChance
     **/
    @Test
    void getNewSpeed_startingCarShouldUseHigherRandomSlowdownChance() {
        HeadLeading hl = createWithFixedRandom(0.2);
        var params = buildParams(0, 5, 10);

        double newSpeed = hl.getNewSpeed(params);

        assertEquals(0, newSpeed,
                "Starting car should have higher slowdown chance and may drop back to 0.");
    }

    @Test
    void getNewSpeed_shouldNotSlowDownWhenRandomAboveThreshold() {
        HeadLeading hl = createWithFixedRandom(0.9);
        var params = buildParams(1, 5, 10);

        double newSpeed = hl.getNewSpeed(params);

        assertEquals(2, newSpeed,
                "Car should not slow down when randomness is above slowdown chance.");
    }

    @Test
    void getNewSpeed_getSpeedWhenNoCarAhead() {
        HeadLeading hl = createWithFixedRandom(1.0);
        var params = buildParams(3, 5, Double.MAX_VALUE);

        double newSpeed = hl.getNewSpeed(params);

        assertEquals(4, newSpeed,
                "When there is no car ahead, car should accelerate normally.");
    }

    @Test
    void getNewSpeed_neverReturnsNegativeSpeed() {
        HeadLeading hl = createWithFixedRandom(1.0);
        var params = buildParams(0, 3, 0);

        double newSpeed = hl.getNewSpeed(params);

        assertEquals(0, newSpeed,
                "Speed should never be negative (clamped to 0).");
    }
}
