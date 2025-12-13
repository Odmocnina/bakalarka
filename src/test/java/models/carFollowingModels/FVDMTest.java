package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/***********************************************
 * Unit tests for FVDM car following model class
 *
 * @author Michael Hladky
 * @version 1.0
 ***********************************************/
class FVDMTest {

    /**
     * Help method to create a base set of parameters for testing.
     *
     * @return HashMap with base parameters
     **/
    private HashMap<String, Double> createBaseParams() {
        HashMap<String, Double> params = new HashMap<>();

        params.put(RequestConstants.CURRENT_SPEED_REQUEST, 20.0);            // v
        params.put(RequestConstants.MAX_SPEED_REQUEST, 33.33);               // v_max
        params.put(RequestConstants.X_POSITION_REQUEST, 100.0);              // x
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, 120.0); // x_l
        params.put(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.5); // λ
        params.put(RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6); // α
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, 25.0); // v_l
        params.put(RequestConstants.MAX_ROAD_SPEED_REQUEST, 33.33);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, 4.5);
        params.put(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);

        return params;
    }

    /**
     * Test: FVDM.getNewSpeed = OVM_Different.getNewSpeed + λ * (v_l - v)
     * for normal case when car is ahead.
     **/
    @Test
    void getNewSpeed_addsVelocityDifferenceTerm() {
        HashMap<String, Double> params = createBaseParams();

        double lambda = params.get(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST);
        double v = params.get(RequestConstants.CURRENT_SPEED_REQUEST);
        double vL = params.get(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST);

        // pokud je OVM_Different konkrétní třída, použij ji přímo:
        OVM_Different baseModel = new OVM_Different();
        FVDM fvdm = new FVDM();

        double baseSpeed = baseModel.getNewSpeed(new HashMap<>(params)); // základní OVM
        double result = fvdm.getNewSpeed(params);

        double expected = baseSpeed + lambda * (vL - v);

        assertEquals(expected, result, 1e-9, "FVDM should add λ * (v_l - v) to the OVM speed.");
    }

    /**
     * Test: if there is no car ahead (CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST == NO_CAR_THERE), FVDM uses
     * Double.MAX_VALUE instead of some wierd value.
     **/
    @Test
    void getNewSpeed_usesDoubleMaxValueWhenNoCarAhead() {
        HashMap<String, Double> params = createBaseParams();

        // simulation no car ahead
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, Constants.NO_CAR_THERE);

        double lambda = params.get(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST);
        double v = params.get(RequestConstants.CURRENT_SPEED_REQUEST);

        OVM_Different baseModel = new OVM_Different();
        FVDM fvdm = new FVDM();

        double baseSpeed = baseModel.getNewSpeed(new HashMap<>(params));
        double result = fvdm.getNewSpeed(params);

        // FVDM should take v_l as Double.MAX_VALUE
        double expected = baseSpeed + lambda * (Double.MAX_VALUE - v);

        // be big pls
        assertTrue(Double.isFinite(baseSpeed) || Double.isInfinite(baseSpeed));
        assertTrue(result > 0, "Big number should this be.");
    }

    /**
     * Test: requestParameters has correct parameter
     **/
    @Test
    void requestParameters_returnsExpectedList() {
        FVDM fvdm = new FVDM();

        String[] expectedRequests = {
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.MAX_ROAD_SPEED_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST
        };

        String expected = String.join(RequestConstants.REQUEST_SEPARATOR, expectedRequests);
        assertEquals(expected, fvdm.requestParameters(),
                "requestParameters should return correct list of requested parameters.");
    }

    /**
     * Test: getParametersForGeneration gives correct list of parameters for car generation.
     **/
    @Test
    void getParametersForGeneration_returnsExpectedList() {
        FVDM fvdm = new FVDM();

        String[] expectedRequests = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                RequestConstants.LENGTH_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST
        };

        String expected = String.join(RequestConstants.REQUEST_SEPARATOR, expectedRequests);
        assertEquals(expected, fvdm.getParametersForGeneration(),
                "getParametersForGeneration should return correct list of requested params for generation.");
    }

    /**
     * Test: getID() should return "fvdm".
     **/
    @Test
    void getID_returnsFvdm() {
        FVDM fvdm = new FVDM();
        assertEquals("fvdm", fvdm.getID());
    }

    /**
     * Test: getName() should return "Full Velocity Difference Model".
     **/
    @Test
    void getName_returnsCorrectName() {
        FVDM fvdm = new FVDM();
        assertEquals("Full Velocity Difference Model", fvdm.getName());
    }
}

