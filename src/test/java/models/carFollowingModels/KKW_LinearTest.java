package models.carFollowingModels;

import core.utils.Constants;
import core.utils.RequestConstants;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class KKW_LinearTest {

    /**
     * Helper method: create a base parameter set for a simple scenario
     * where deterministicSpeed = 4 and smallestSpeed = 4.
     *
     * Used to test slowdown / no-change behavior of the stochastic part.
     */
    private HashMap<String, Double> createBaseParamsScenario1() {
        HashMap<String, Double> params = new HashMap<>();

        // freeSpeed
        params.put(RequestConstants.MAX_SPEED_REQUEST, 10.0);
        // currentSpeed = 3
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 3.0);
        // distance to next car
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 10.0);
        // time step
        params.put(RequestConstants.TIME_STEP_REQUEST, 1.0);
        // speed of next car
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, 3.0);

        // LENGTH_REQUEST is only used when generating, but we keep things consistent
        params.put(RequestConstants.LENGTH_REQUEST, 4.0);

        return params;
    }

    /**
     * Helper method: create parameters for a scenario where
     * random acceleration (+1) actually changes the resulting speed
     * compared to the no-random-change case.
     */
    private HashMap<String, Double> createBaseParamsScenario2() {
        HashMap<String, Double> params = new HashMap<>();

        // high free speed
        params.put(RequestConstants.MAX_SPEED_REQUEST, 30.0);
        // currentSpeed = 10
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 10.0);
        // distance to next car - small enough to be within synchronization gap
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 10.0);
        // time step
        params.put(RequestConstants.TIME_STEP_REQUEST, 1.0);
        // next car is slower
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, 5.0);

        params.put(RequestConstants.LENGTH_REQUEST, 4.0);

        return params;
    }

    /**
     * Helper: inject a custom Random instance into KKW_Linear.rand via reflection,
     * so tests can control the stochastic behavior deterministically.
     */
    private void setRandom(KKW_Linear model, Random random) throws Exception {
        Field f = KKW_Linear.class.getDeclaredField("rand");
        f.setAccessible(true);
        f.set(model, random);
    }

    /**
     * Fixed Random that always returns a value < chanceB.
     * This forces the "random slowdown" branch (r < chanceB → -1.0).
     */
    private static class FixedRandomSlowdown extends Random {
        @Override
        public double nextDouble() {
            return 0.0; // always below chanceB (0.3)
        }
    }

    /**
     * Fixed Random that returns a value > chanceA + chanceB.
     * This forces "no random modification" (r >= chanceA + chanceB → 0.0).
     */
    private static class FixedRandomNoChange extends Random {
        @Override
        public double nextDouble() {
            return 0.99; // safely above chanceA + chanceB (0.32)
        }
    }

    /**
     * Fixed Random that returns a value between chanceB and chanceA + chanceB,
     * which triggers random acceleration (+1.0).
     *
     * For scenario1 and scenario2: chanceB = 0.3, chanceA = 0.02 → (0.3, 0.32)
     */
    private static class FixedRandomAcceleration extends Random {
        @Override
        public double nextDouble() {
            return 0.31; // between 0.3 and 0.32
        }
    }

    /**
     * Test: requestParameters() should return the correct list of required parameters
     * in the correct order.
     */
    @Test
    void requestParameters_returnsExpectedList() {
        KKW_Linear model = new KKW_Linear();

        String[] expected = {
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST,
                RequestConstants.TIME_STEP_REQUEST,
                RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST
        };

        assertEquals(
                String.join(RequestConstants.REQUEST_SEPARATOR, expected),
                model.requestParameters(),
                "requestParameters() should return the proper parameter list in correct order."
        );
    }

    /**
     * Test: getParametersForGeneration() should return correct parameters for car generation.
     */
    @Test
    void getParametersForGeneration_returnsExpectedList() {
        KKW_Linear model = new KKW_Linear();

        String[] expected = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.LENGTH_REQUEST
        };

        assertEquals(
                String.join(RequestConstants.REQUEST_SEPARATOR, expected),
                model.getParametersForGeneration(),
                "getParametersForGeneration() should return parameter list for generation."
        );
    }

    /**
     * Test the deterministic part in a simple scenario where random modification = 0.
     * For scenario1 + FixedRandomNoChange:
     *
     * deterministicSpeed = 4
     * smallestSpeed      = 4
     * randomSpeedChance  = 0 → newSpeed = 4
     */
    @Test
    void getNewSpeed_noRandomChange_scenario1() throws Exception {
        KKW_Linear model = new KKW_Linear();
        setRandom(model, new FixedRandomNoChange());

        HashMap<String, Double> params = createBaseParamsScenario1();

        double result = model.getNewSpeed(params);

        // Expected new speed = deterministicSpeed = 4.0
        assertEquals(4.0, result, 1e-9,
                "New speed should equal deterministic speed when there is no random modification.");
    }

    /**
     * Test slowdown branch: r < chanceB → randomSpeedChance = -1.0.
     *
     * In scenario1:
     * deterministicSpeed = 4
     * randomSpeedChance  = -1 → randomAffectSpeed = 3
     * smallestSpeed      = 4
     * => newSpeed = min(3, 4) = 3
     */
    @Test
    void getNewSpeed_randomSlowdown_scenario1() throws Exception {
        KKW_Linear model = new KKW_Linear();
        setRandom(model, new FixedRandomSlowdown());

        HashMap<String, Double> params = createBaseParamsScenario1();

        double result = model.getNewSpeed(params);

        assertEquals(3.0, result, 1e-9,
                "Random slowdown should reduce speed by one cell (within constraints).");
    }

    /**
     * Test acceleration branch: chanceB <= r < chanceA + chanceB → randomSpeedChance = +1.0.
     *
     * In scenario2:
     * currentSpeed = 10, timeStep = 1, acceleration = 1
     * gap = 10, safeSpeed = 10
     * syncGap = (int) (2 + 1 * 10 * 1) = 12 → distanceToNextCar <= syncGap
     * speedNextCar = 5 < currentSpeed → signum(5 - 10) = -1
     * synchronizedSpeed = currentSpeed + 1 * 1 * (-1) = 9
     * deterministicSpeed = min(freeSpeed=30, min(safe=10, sync=9)) = 9
     * smallestSpeed = min(current+1=11, min(free=30, safe=10)) = 10
     *
     * With randomSpeedChance = +1:
     * randomAffectSpeed = deterministicSpeed + 1 * 1 = 10
     * newSpeed = min(10, smallestSpeed=10) = 10
     *
     * For no random change, newSpeed would be 9 → acceleration branch has a visible effect.
     */
    @Test
    void getNewSpeed_randomAcceleration_scenario2() throws Exception {
        KKW_Linear model = new KKW_Linear();
        setRandom(model, new FixedRandomAcceleration());

        HashMap<String, Double> params = createBaseParamsScenario2();

        double result = model.getNewSpeed(params);

        assertEquals(10.0, result, 1e-9,
                "Random acceleration should increase speed by one cell (up to the smallestSpeed limit).");
    }

    /**
     * Test: when there is no car ahead (speedNextCar == NO_CAR_THERE),
     * getNewSpeed() should treat the next car speed as freeSpeed.
     * We mainly verify that the method runs and stays within [0, freeSpeed].
     */
    @Test
    void getNewSpeed_noCarAhead_usesFreeSpeedAsNextSpeed() throws Exception {
        KKW_Linear model = new KKW_Linear();
        setRandom(model, new FixedRandomNoChange()); // keep stochastic part neutral

        HashMap<String, Double> params = createBaseParamsScenario1();
        double freeSpeed = 15.0;
        params.put(RequestConstants.MAX_SPEED_REQUEST, freeSpeed);
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, Constants.NO_CAR_THERE);

        double result = model.getNewSpeed(params);

        assertTrue(result >= 0.0 && result <= freeSpeed,
                "When no car is ahead, new speed should remain within [0, freeSpeed].");
    }

    /**
     * Test: getSynchronizationGap() should follow the formula:
     * syncGap = (int) (d + k * currentSpeed * timeStep)
     *
     * Here, d = 2.0, k = 1.0.
     */
    @Test
    void getSynchronizationGap_worksAsExpected() {
        KKW_Linear model = new KKW_Linear();

        double currentSpeed = 5.0;
        double d = 2.0;
        double k = 1.0;
        double dt = 1.5;

        int expected = (int) (d + k * currentSpeed * dt); // (int)(2 + 5 * 1.5) = (int)9.5 = 9

        int gap = model.getSynchronizationGap(currentSpeed, d, k, dt);

        assertEquals(expected, gap,
                "Synchronization gap should be computed as (int)(d + k * v * dt).");
    }

    /**
     * Test: getName() returns readable model name.
     */
    @Test
    void getName_returnsCorrectName() {
        KKW_Linear model = new KKW_Linear();
        assertEquals("Kerner-Klenov-Wolf (linear)", model.getName());
    }

    /**
     * Test: getID() returns the correct identifier.
     */
    @Test
    void getID_returnsCorrectID() {
        KKW_Linear model = new KKW_Linear();
        assertEquals("kkw-linear", model.getID());
    }

    /**
     * Test: getType() returns cellular type.
     */
    @Test
    void getType_returnsCellular() {
        KKW_Linear model = new KKW_Linear();
        assertEquals(Constants.CELLULAR, model.getType());
    }

    /**
     * Test: getCellSize() returns the configured cell size.
     */
    @Test
    void getCellSize_returnsCorrectValue() {
        KKW_Linear model = new KKW_Linear();
        assertEquals(1.5, model.getCellSize(), 1e-9);
    }
}

