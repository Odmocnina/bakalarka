package models.carFollowingModels;

import core.utils.RequestConstants;
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

    @Test
    void whenDistanceGreaterThanOne_returnsSpeedOne() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 5.0);

        double newSpeed = model.getNewSpeed(params);

        assertEquals(1.0, newSpeed);
    }

    @Test
    void whenDistanceEqualOne_returnsSpeedZero() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 1.0);

        double newSpeed = model.getNewSpeed(params);

        assertEquals(0.0, newSpeed);
    }

    @Test
    void whenDistanceLessThanOne_returnsSpeedZero() {
        HashMap<String, Double> params = new HashMap<>();
        params.put(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, 0.5);

        double newSpeed = model.getNewSpeed(params);

        assertEquals(0.0, newSpeed);
    }

    @Test
    void getCellSize_isFiveMeters() {
        // podle implementace: private final double CELL_SIZE = 5.0;
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
        String expected = RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST;
        assertEquals(expected, model.requestParameters());
    }

    @Test
    void getParametersForGeneration_isEmpty() {
        String expected = RequestConstants.LENGTH_REQUEST;
        assertEquals(expected, model.getParametersForGeneration());
    }
}

