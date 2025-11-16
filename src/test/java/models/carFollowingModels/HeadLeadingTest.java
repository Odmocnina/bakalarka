package models.carFollowingModels;

import core.utils.Constants;
import core.utils.RequestConstants;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class HeadLeadingTest {

    private HeadLeading create() {
        return new HeadLeading();
    }

    /**
     * Helper to build parameter map for getNewSpeed()
     */
    private HashMap<String, Double> buildParams(double currentSpeed, double maxSpeed, double distance) {
        HashMap<String, Double> map = new HashMap<>();
        map.put(RequestConstants.CURRENT_SPEED_REQUEST, currentSpeed);
        map.put(RequestConstants.MAX_SPEED_REQUEST, maxSpeed);
        map.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, distance);
        return map;
    }

    // ------------------------------------------------------
    // Utility: temporarily override Math.random() using reflection
    // ------------------------------------------------------
    private static void overrideMathRandom(double fixedValue) {
        try {
            Field randomField = Math.class.getDeclaredField("randomNumberGenerator");
            randomField.setAccessible(true);

            // Replace default Random with a fixed-value Random
            java.util.Random fixedRandom = new java.util.Random() {
                @Override
                public double nextDouble() {
                    return fixedValue;
                }
            };

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(randomField, randomField.getModifiers() & ~Modifier.FINAL);

            randomField.set(null, fixedRandom);
        } catch (Exception ignored) {}
    }

    // ------------------------------------------------------
    // Getter tests
    // ------------------------------------------------------

    @Test
    void getId_shouldReturnHeadLeading() {
        HeadLeading hl = create();
        assertEquals("head-leading", hl.getID(), "ID should be 'head-leading'.");
    }

    @Test
    void getName_shouldReturnText() {
        HeadLeading hl = create();
        assertEquals("Head-leading algorithm", hl.getName(),
                "Name should be the readable algorithm name.");
    }

    @Test
    void getType_shouldReturnCellular() {
        HeadLeading hl = create();
        assertEquals(Constants.CELLULAR, hl.getType(),
                "Type should be Constants.CELLULAR.");
    }

    @Test
    void getCellSize_shouldReturnFixedValue() {
        HeadLeading hl = create();
        assertEquals(2.5, hl.getCellSize(), 1e-9,
                "Cell size should be 2.5 meters.");
    }

    @Test
    void requestParameters_shouldMatchExpected() {
        HeadLeading hl = create();

        String[] expected = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST
        };

        assertEquals(String.join(RequestConstants.REQUEST_SEPARATOR, expected),
                hl.requestParameters(),
                "Request parameters must match expected order.");
    }

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

    @Test
    void getNewSpeed_shouldAccelerateWhenBelowMaxSpeed() {
        overrideMathRandom(1.0); // no random slowdown

        HeadLeading hl = create();
        var params = buildParams(2, 5, 10);

        double newSpeed = hl.getNewSpeed(params);

        assertEquals(3, newSpeed,
                "Car should accelerate by +1 when below max speed and no slowdown happens.");
    }

    @Test
    void getNewSpeed_shouldLimitByDistanceToNextCar() {
        overrideMathRandom(1.0); // no slowdown

        HeadLeading hl = create();
        var params = buildParams(4, 10, 3); // distance = 3 -> distanceInCells=3

        double newSpeed = hl.getNewSpeed(params);

        assertEquals(2, newSpeed,
                "Speed should be distanceInCells - 1 when too close to the next car.");
    }

    @Test
    void getNewSpeed_startingCarShouldUseHigherRandomSlowdownChance() {
        // random < 0.5 => slowdown triggered
        overrideMathRandom(0.2);

        HeadLeading hl = create();
        var params = buildParams(0, 5, 10);

        double newSpeed = hl.getNewSpeed(params); // start: currentSpeed=0 -> becomes 1 -> slowed to 0

        assertEquals(0, newSpeed,
                "Starting car should have higher slowdown chance and may drop back to 0.");
    }

    @Test
    void getNewSpeed_shouldNotSlowDownWhenRandomAboveThreshold() {
        // random > slowdownChance => no slowdown
        overrideMathRandom(0.9);

        HeadLeading hl = create();
        var params = buildParams(1, 5, 10);

        double newSpeed = hl.getNewSpeed(params);

        assertEquals(2, newSpeed,
                "Car should not slow down when randomness is above slowdown chance.");
    }

    @Test
    void getNewSpeed_neverReturnsNegativeSpeed() {
        overrideMathRandom(1.0); // no random slowdown

        HeadLeading hl = create();
        var params = buildParams(0, 3, 0); // distanceInCells=0 -> new speed becomes -1

        double newSpeed = hl.getNewSpeed(params);

        assertEquals(0, newSpeed,
                "Speed should never be negative (clamped to 0).");
    }
}

