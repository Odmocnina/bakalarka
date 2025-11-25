package models.carFollowingModels;

import core.utils.RequestConstants;
import core.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class Rule184Test {

    private Rule184 model;

    @BeforeEach
    void setUp() {
        model = new Rule184();
    }

    /**
     * helper: builds params using positions and length so that
     * distanceToNextCar = x2 - x1 - length2
     */
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

    @Test
    void whenDistanceGreaterThanOne_returnsSpeedOne() {
        HashMap<String, Double> params = buildParams(5.0);

        double newSpeed = model.getNewSpeed(params);

        assertEquals(1.0, newSpeed);
    }

    @Test
    void whenDistanceEqualOne_returnsSpeedZero() {
        HashMap<String, Double> params = buildParams(1.0);

        double newSpeed = model.getNewSpeed(params);

        assertEquals(0.0, newSpeed);
    }

    @Test
    void whenDistanceLessThanOne_returnsSpeedZero() {
        HashMap<String, Double> params = buildParams(0.5);

        double newSpeed = model.getNewSpeed(params);

        assertEquals(0.0, newSpeed);
    }

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

    @Test
    void getCellSize_isFiveMeters() {
        assertEquals(5.0, model.getCellSize());
    }

    @Test
    void getType_isCellular() {
        assertEquals("cellular", model.getType());
    }

    @Test
    void getName_isCorrect() {
        assertEquals("Rule 184", model.getName());
    }

    @Test
    void getID_isRule184() {
        assertEquals("rule184", model.getID());
    }

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

    @Test
    void getParametersForGeneration_isCorrect() {
        assertEquals(RequestConstants.LENGTH_REQUEST, model.getParametersForGeneration());
    }
}
