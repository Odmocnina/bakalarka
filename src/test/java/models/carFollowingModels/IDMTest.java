package models.carFollowingModels;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class IDMTest {

    /**
     * helper to create a fresh IDM instance for each test
     */
    private IDM createIDM() {
        return new IDM();
    }

    /**
     * helper to build parameter map for IDM.getNewSpeed
     */
    private HashMap<String, Double> buildParameters(
            double maxSpeed,
            double currentSpeed,
            double xPosition,
            double xPositionNextCar,
            double lengthNextCar,
            double maxAcceleration,
            double currentSpeedNextCar,
            double minimumGapToNextCar,
            double decelerationComfort,
            double desiredTimeHeadway
    ) {
        HashMap<String, Double> params = new HashMap<>();

        params.put(RequestConstants.MAX_SPEED_REQUEST, maxSpeed);
        params.put(RequestConstants.CURRENT_SPEED_REQUEST, currentSpeed);
        params.put(RequestConstants.X_POSITION_REQUEST, xPosition);
        params.put(RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST, xPositionNextCar);
        params.put(RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST, lengthNextCar);
        params.put(RequestConstants.MAX_ACCELERATION_REQUEST, maxAcceleration);
        params.put(RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, currentSpeedNextCar);
        params.put(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, minimumGapToNextCar);
        params.put(RequestConstants.DECELERATION_COMFORT_REQUEST, decelerationComfort);
        params.put(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, desiredTimeHeadway);

        return params;
    }

    @Test
    void getId_shouldReturnIdm() {
        IDM idm = createIDM();

        assertEquals("idm", idm.getID(), "Model ID should be 'idm'.");
    }

    @Test
    void getType_shouldReturnContinuousTypeConstant() {
        IDM idm = createIDM();

        assertEquals(Constants.CONTINOUS, idm.getType(),
                "Model type should match Constants.CONTINOUS.");
    }

    @Test
    void getCellSize_shouldReturnParameterUndefined() {
        IDM idm = createIDM();

        assertEquals(Constants.PARAMETER_UNDEFINED, idm.getCellSize(),
                "Cell size should be PARAMETER_UNDEFINED for continuous model.");
    }

    @Test
    void getName_shouldReturnReadableName() {
        IDM idm = createIDM();

        assertEquals("Intelligent Driver Model (IDM)", idm.getName(),
                "Model name should be human readable IDM name.");
    }

    @Test
    void requestParameters_shouldReturnCorrectRequestString() {
        IDM idm = createIDM();

        String[] expectedRequests = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.CURRENT_SPEED_REQUEST,
                RequestConstants.X_POSITION_REQUEST,
                RequestConstants.X_POSITION_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.LENGTH_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.MAX_ACCELERATION_REQUEST,
                RequestConstants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.DECELERATION_COMFORT_REQUEST,
                RequestConstants.DESIRED_TIME_HEADWAY_REQUEST
        };

        String expected = String.join(RequestConstants.REQUEST_SEPARATOR, expectedRequests);

        assertEquals(expected, idm.requestParameters(),
                "Request parameters string should contain all required parameters in the correct order.");
    }

    @Test
    void getParametersForGeneration_shouldReturnCorrectRequestString() {
        IDM idm = createIDM();

        String[] expectedRequests = {
                RequestConstants.MAX_SPEED_REQUEST,
                RequestConstants.MAX_ACCELERATION_REQUEST,
                RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                RequestConstants.DECELERATION_COMFORT_REQUEST,
                RequestConstants.DESIRED_TIME_HEADWAY_REQUEST,
                RequestConstants.LENGTH_REQUEST
        };

        String expected = String.join(RequestConstants.REQUEST_SEPARATOR, expectedRequests);

        assertEquals(expected, idm.getParametersForGeneration(),
                "Generation parameters string should contain all required parameters in the correct order.");
    }

    @Test
    void getNewSpeed_noCarAhead_shouldAccelerateTowardsMaxSpeed() {
        IDM idm = createIDM();

        // given: car on empty road, no leader in front
        double maxSpeed = 30.0;
        double currentSpeed = 10.0;
        double xPosition = 0.0;

        // encode "no car ahead" with NO_CAR_THERE constants
        double xPositionNextCar = Constants.NO_CAR_THERE;
        double lengthNextCar = 0.0;
        double currentSpeedNextCar = Constants.NO_CAR_THERE;

        double maxAcceleration = 1.0;
        double minimumGapToNextCar = 2.0;
        double decelerationComfort = 2.0;
        double desiredTimeHeadway = 1.5;

        HashMap<String, Double> params = buildParameters(
                maxSpeed,
                currentSpeed,
                xPosition,
                xPositionNextCar,
                lengthNextCar,
                maxAcceleration,
                currentSpeedNextCar,
                minimumGapToNextCar,
                decelerationComfort,
                desiredTimeHeadway
        );

        // when
        double newSpeed = idm.getNewSpeed(params);

        // then
        assertFalse(Double.isNaN(newSpeed), "New speed should not be NaN.");
        assertTrue(newSpeed > currentSpeed,
                "New speed should be higher than current speed when there is no car ahead and speed is below max.");
        assertTrue(newSpeed <= maxSpeed,
                "New speed should never exceed maximum speed.");

        // optional: check approximate expected value based on IDM formula without interaction term
        double exponent = 4.0;
        double vPart = Math.pow(currentSpeed / maxSpeed, exponent);
        double expectedNewSpeed = currentSpeed + maxAcceleration * (1.0 - vPart);
        assertEquals(expectedNewSpeed, newSpeed, 1e-9,
                "New speed should follow IDM free-flow acceleration (interaction term is negligible for infinite distance).");
    }

    @Test
    void getNewSpeed_withSlowerCarAhead_shouldDecelerateStrongly() {
        IDM idm = createIDM();

        // given: follower is faster and closer to a slower leader car
        double maxSpeed = 30.0;
        double currentSpeed = 20.0;
        double xPosition = 0.0;

        double xPositionNextCar = 30.0;    // leader position
        double lengthNextCar = 5.0;        // leader length => distance = 25
        double currentSpeedNextCar = 10.0; // leader is slower

        double maxAcceleration = 1.5;
        double minimumGapToNextCar = 2.0;
        double decelerationComfort = 2.0;
        double desiredTimeHeadway = 1.5;

        HashMap<String, Double> params = buildParameters(
                maxSpeed,
                currentSpeed,
                xPosition,
                xPositionNextCar,
                lengthNextCar,
                maxAcceleration,
                currentSpeedNextCar,
                minimumGapToNextCar,
                decelerationComfort,
                desiredTimeHeadway
        );

        // when
        double newSpeed = idm.getNewSpeed(params);

        // then
        assertFalse(Double.isNaN(newSpeed), "New speed should not be NaN.");
        assertTrue(newSpeed < currentSpeed,
                "New speed should be lower than current speed when approaching a slower car at close distance.");

        // expected value numerically computed from IDM formula for regression stability
        double expectedNewSpeed = 1.8780035689510512;
        assertEquals(expectedNewSpeed, newSpeed, 1e-9,
                "New speed should match IDM interaction braking behavior within numerical tolerance.");
    }
}

