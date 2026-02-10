package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class HellyTest {

    /**
     * Helper method to construct a baseline parameter set
     * used by most Helly model tests.
     */
    private HashMap<String, Double> createBaseParams() {
        HashMap<String, Double> params = new HashMap<>();

        params.put(RequestConstants.MAX_SPEED_REQUEST, 33.33);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 20.0);     // v
        params.put(RequestConstants.X_POSITION_REQUEST, 100.0);       // x
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 120.0); // x_l
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 4.0);
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, 25.0); // v_l
        params.put(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.5); // lambda
        params.put(RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.2); // alpha
        params.put(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        params.put(RequestConstants.LENGTH_REQUEST, 4.0);

        return params;
    }

    /**
     * Test: verifies that Helly computes new speed correctly
     * with normal conditions (next car exists & speed exists).
     */
    @Test
    void getNewSpeed_normalCase() {
        Helly helly = new Helly();
        HashMap<String, Double> params = createBaseParams();

        double v = 20.0;
        double vL = 25.0;
        double x = 100.0;
        double xL = 120.0;
        double L = 4.0;

        double lambda = 0.5;
        double alpha = 0.2;
        double gapMin = 2.0;

        double dist = xL - x - L; // 120 - 100 - 4 = 16
        double dv = vL - v;       // 25 - 20 = 5

        double acceleration =
                lambda * dv + alpha * (dist - gapMin); // 0.5*5 + 0.2*(16-2) = 2.5 + 2.8 = 5.3

        double expected = v + acceleration; // 20 + 5.3 = 25.3

        double result = helly.getNewSpeed(params);

        assertEquals(expected, result, 1e-9,
                "Helly should compute correct new speed in normal case.");
    }

    /**
     * Test: when there is no car ahead (NO_CAR_THERE),
     * distance = Double.MAX_VALUE and speed difference = 0.
     */
    @Test
    void getNewSpeed_noCarAhead() {
        Helly helly = new Helly();
        HashMap<String, Double> params = createBaseParams();

        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                Constants.NO_CAR_THERE);

        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST,
                Constants.NO_CAR_THERE);

        double result = helly.getNewSpeed(params);

        // Expected: velocity increases strongly → will be clamped to maxSpeed
        double maxSpeed = params.get(RequestConstants.MAX_SPEED_REQUEST);

        assertEquals(maxSpeed, result,
                "When no car is ahead, Helly should accelerate toward maxSpeed.");
    }

    /**
     * Test: if the computed new speed would be negative,
     * it must be clamped to 0.
     */
    @Test
    void getNewSpeed_clampedToZero() {
        Helly helly = new Helly();
        HashMap<String, Double> params = createBaseParams();

        // Force huge negative acceleration
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, 0.0);
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 101.0); // very small gap
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 4.0);
        params.put(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 50.0);
        params.put(RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 10.0);

        double result = helly.getNewSpeed(params);

        assertEquals(0.0, result,
                "Helly model must clamp negative speed to 0.");
    }

    /**
     * Test: new speed must not exceed maxSpeed.
     */
    @Test
    void getNewSpeed_clampedToMaxSpeed() {
        Helly helly = new Helly();
        HashMap<String, Double> params = createBaseParams();

        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 30.0);

        // Force huge positive acceleration
        params.put(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 10.0);
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, 100.0);

        double maxSpeed = params.get(RequestConstants.MAX_SPEED_REQUEST);
        double result = helly.getNewSpeed(params);

        assertEquals(maxSpeed, result,
                "Helly should never exceed max speed.");
    }

    /**
     * Test: verifies that requestParameters() returns exactly
     * the expected list in correct order.
     */
    @Test
    void requestParameters_returnsCorrectList() {
        Helly helly = new Helly();

        String[] expectedList = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST
        };

        String expected = String.join(RequestConstants.REQUEST_SEPARATOR, expectedList);

        assertEquals(expected, helly.requestParameters(),
                "requestParameters() must return exact required list in specified order.");
    }

    /**
     * Test: verifies parameters for generation.
     */
    @Test
    void getParametersForGeneration_returnsCorrectList() {
        Helly helly = new Helly();

        String[] expectedList = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.LENGTH_REQUEST
        };

        String expected = String.join(RequestConstants.REQUEST_SEPARATOR, expectedList);

        assertEquals(expected, helly.getParametersForGeneration(),
                "getParametersForGeneration() must return correct parameters.");
    }

    /**
     * Test: getID() must be "helly".
     */
    @Test
    void getID_returnsCorrectID() {
        assertEquals("helly", new Helly().getID());
    }

    /**
     * Test: getType() must return Constants.CONTINOUS.
     */
    @Test
    void getType_returnsCorrectType() {
        assertEquals(Constants.CONTINUOUS, new Helly().getType());
    }

    /**
     * Test: getName() returns readable model name.
     */
    @Test
    void getName_returnsCorrectName() {
        assertEquals("Helly Car Following Model", new Helly().getName());
    }

    /**
     * Test: getCellSize() should return PARAMETER_UNDEFINED.
     */
    @Test
    void getCellSize_returnsUndefined() {
        assertEquals(Constants.PARAMETER_UNDEFINED, new Helly().getCellSize());
    }
}

