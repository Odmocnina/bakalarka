package models.carFollowingModels;

import core.utils.constants.RequestConstants;
import core.utils.constants.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/***********************************************
 * Unit tests for Rule184 car following model class
 *
 * @author Michael Hladky
 * @version 1.0
 ***********************************************/
class Rule184Test {

    /**
     * helper to create a fresh Rule184 instance for each test
     **/
    private Rule184 model;

    /**
     * setup method to initialize the model before each test
     **/
    @BeforeEach
    void setUp() {
        model = new Rule184();
    }

    /**
     * helper: builds params using positions and length so that
     * distanceToNextCar = x2 - x1 - length2
     **/
    private HashMap<String, Double> buildParams(double distance) {
        HashMap<String, Double> params = new HashMap<>();

        double x1 = 0.0;
        double length2 = 1.0;
        double x2 = x1 + length2 + distance;

        params.put(RequestConstants.X_POSITION_REQUEST, x1);
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, x2);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, length2);

        return params;
    }

    /**
     * Tests for getNewSpeed() behavior based on distance to next car.
     * Rule 184 should return speed 1 if distance > 1, and speed 0 otherwise.
     **/
    @Test
    void whenDistanceGreaterThanOne_returnsSpeedOne() {
        HashMap<String, Double> params = buildParams(5.0);

        double newSpeed = model.getNewSpeed(params);

        assertEquals(1.0, newSpeed);
    }

    /**
     * Tests for getNewSpeed() behavior based on distance to next car.
     * Rule 184 should return speed 1 if distance > 1, and speed 0 otherwise.
     **/
    @Test
    void whenDistanceEqualOne_returnsSpeedZero() {
        HashMap<String, Double> params = buildParams(1.0);

        double newSpeed = model.getNewSpeed(params);

        assertEquals(0.0, newSpeed);
    }

    /**
     * Tests for getNewSpeed() behavior based on distance to next car.
     * Rule 184 should return speed 1 if distance > 1, and speed 0 otherwise.
     **/
    @Test
    void whenDistanceLessThanOne_returnsSpeedZero() {
        HashMap<String, Double> params = buildParams(0.5);

        double newSpeed = model.getNewSpeed(params);

        assertEquals(0.0, newSpeed);
    }

    /**
     * Tests for getNewSpeed() behavior when there is no car ahead (NO_CAR_THERE).
     * Rule 184 should return speed 1 in this case.
     **/
    @Test
    void whenNoCarAhead_returnsSpeedOne() {
        HashMap<String, Double> params = new HashMap<>();

        params.put(RequestConstants.X_POSITION_REQUEST, 0.0);
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, (double) Constants.NO_CAR_THERE);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 1.0);

        double newSpeed = model.getNewSpeed(params);

        assertEquals(1.0, newSpeed,
                "If there is no car ahead, speed should be 1.");
    }

    /**
     * Tests if cell size is correctly set to 5 meters.
     **/
    @Test
    void getCellSize_isFiveMeters() {
        assertEquals(5.0, model.getCellSize());
    }

    /**
     * Tests if model type is correctly set to "cellular".
     **/
    @Test
    void getType_isCellular() {
        assertEquals("cellular", model.getType());
    }

    /**
     * Tests if model name is correctly set to "Rule 184".
     **/
    @Test
    void getName_isCorrect() {
        assertEquals("Rule 184", model.getName());
    }

    /**
     * Tests if model ID is correctly set to "rule-184".
     **/
    @Test
    void getID_isRule184() {
        assertEquals("rule-184", model.getID());
    }

    /**
     * Tests if requestParameters returns the correct list of requested parameters.
     **/
    @Test
    void requestParameters_isCorrect() {
        String expected = String.join(
                RequestConstants.REQUEST_SEPARATOR,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST
        );

        assertEquals(expected, model.requestParameters());
    }

    /**
     * Tests if getParametersForGeneration returns the correct list of parameters for car generation.
     **/
    @Test
    void getParametersForGeneration_isCorrect() {
        assertEquals(RequestConstants.LENGTH_REQUEST, model.getParametersForGeneration());
    }
}
