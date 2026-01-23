package core.utils.constants;

/***************************
 * Class holding constants for request strings used in the model, this is used for communication between model and cars
 * model needs to get certain parameters for its calculations from the road, so it sends requests using these constants
 * model sends string of these divided by REQUEST_SEPARATOR and road responds with the values in hash map of double
 * values (even when they are integers, road or model will convert them as needed)
 *
 * @author Michael Hladky
 * @version 1.0
 ***************************/
public class RequestConstants {

    // directions
    public static final String STRAIGHT = "STRAIGHT";
    public static final String LEFT = "LEFT";
    public static final String RIGHT = "RIGHT";

    // orientations
    public static final String FORWARD = "FORWARD";
    public static final String BACKWARD = "BACKWARD";


    // request string used when model is requesting parameters of road
    public static final String REQUEST_SEPARATOR = ";";
    public static final String SUBREQUEST_SEPARATOR = "_";
    public static final String X_POSITION_REQUEST = "xPosition";
    public static final String MAX_SPEED_REQUEST = "maxSpeed";
    public static final String CURRENT_SPEED_REQUEST = "currentSpeed";
    public static final String DISTANCE_TO_NEXT_CAR_REQUEST = "distanceToNextCar";
    public static final String DISTANCE_TO_NEXT_CAR_LEFT_REQUEST = "distanceToNextCarLeft";
    public static final String DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST = "distanceToNextCarRight";
    public static final String MAX_ACCELERATION_REQUEST = "maxAcceleration";
    public static final String SPEED_DIFFERENCE_TO_NEXT_CAR_REQUEST = "speedDifferenceToNextCar";
    public static final String MINIMUM_GAP_TO_NEXT_CAR_REQUEST = "minimumGapToNextCar";
    public static final String DECELERATION_COMFORT_REQUEST = "decelerationComfort";
    public static final String DESIRED_TIME_HEADWAY_REQUEST = "desiredTimeHeadway";
    public static final String DISTANCE_TO_PREVIOUS_CAR_REQUEST = "distanceToPreviousCar";
    public static final String DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST = "distanceToPreviousCarLeft";
    public static final String DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST = "distanceToPreviousCarRight";
    public static final String LENGTH_REQUEST = "length";
    public static final String DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST = "distanceDifferenceSensitivity";
    public static final String SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST = "speedDifferenceSensitivity";

    // parameters of specific direction and orientation (different cars)
    public static final String CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST = CURRENT_SPEED_REQUEST + SUBREQUEST_SEPARATOR
            + STRAIGHT + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String CURRENT_SPEED_STRAIGHT_BACKWARDS_REQUEST = CURRENT_SPEED_REQUEST + SUBREQUEST_SEPARATOR
            + STRAIGHT + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String CURRENT_SPEED_LEFT_FORWARD_REQUEST = CURRENT_SPEED_REQUEST + SUBREQUEST_SEPARATOR
            + LEFT + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String CURRENT_SPEED_LEFT_BACKWARD_REQUEST = CURRENT_SPEED_REQUEST + SUBREQUEST_SEPARATOR
            + LEFT + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String CURRENT_SPEED_RIGHT_FORWARD_REQUEST = CURRENT_SPEED_REQUEST + SUBREQUEST_SEPARATOR
            + RIGHT + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String CURRENT_SPEED_RIGHT_BACKWARD_REQUEST = CURRENT_SPEED_REQUEST + SUBREQUEST_SEPARATOR
            + RIGHT + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String LENGTH_STRAIGHT_FORWARD_REQUEST = LENGTH_REQUEST + SUBREQUEST_SEPARATOR + STRAIGHT
            + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String LENGTH_STRAIGHT_BACKWARDS_REQUEST = LENGTH_REQUEST + SUBREQUEST_SEPARATOR + STRAIGHT
            + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String LENGTH_LEFT_FORWARD_REQUEST = LENGTH_REQUEST + SUBREQUEST_SEPARATOR + LEFT
            + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String LENGTH_LEFT_BACKWARD_REQUEST = LENGTH_REQUEST + SUBREQUEST_SEPARATOR + LEFT
            + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String LENGTH_RIGHT_FORWARD_REQUEST = LENGTH_REQUEST + SUBREQUEST_SEPARATOR + RIGHT
            + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String LENGTH_RIGHT_BACKWARD_REQUEST = LENGTH_REQUEST + SUBREQUEST_SEPARATOR + RIGHT
            + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String X_POSITION_STRAIGHT_FORWARD_REQUEST = X_POSITION_REQUEST + SUBREQUEST_SEPARATOR
            + STRAIGHT + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String X_POSITION_STRAIGHT_BACKWARDS_REQUEST = X_POSITION_REQUEST + SUBREQUEST_SEPARATOR
            + STRAIGHT + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String X_POSITION_LEFT_FORWARD_REQUEST = X_POSITION_REQUEST + SUBREQUEST_SEPARATOR
            + LEFT + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String X_POSITION_LEFT_BACKWARD_REQUEST = X_POSITION_REQUEST + SUBREQUEST_SEPARATOR
            + LEFT + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String X_POSITION_RIGHT_FORWARD_REQUEST = X_POSITION_REQUEST + SUBREQUEST_SEPARATOR
            + RIGHT + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String X_POSITION_RIGHT_BACKWARD_REQUEST = X_POSITION_REQUEST + SUBREQUEST_SEPARATOR
            + RIGHT + SUBREQUEST_SEPARATOR + BACKWARD;

    public static final String POLITENESS_FACTOR_REQUEST = "politenessFactor";
    public static final String EDGE_VALUE_FOR_LANE_CHANGE_REQUEST = "edgeValueForLaneChange";

    public static final String NOW_ACCELERATION_REQUEST = "nowAcceleration";
    public static final String NOW_ACCELERATION_STRAIGHT_FORWARD_REQUEST = NOW_ACCELERATION_REQUEST +
            SUBREQUEST_SEPARATOR + STRAIGHT + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String NOW_ACCELERATION_STRAIGHT_BACKWARD_REQUEST = NOW_ACCELERATION_REQUEST +
            SUBREQUEST_SEPARATOR + STRAIGHT + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String NOW_ACCELERATION_LEFT_FORWARD_REQUEST = NOW_ACCELERATION_REQUEST +
            SUBREQUEST_SEPARATOR + LEFT + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String NOW_ACCELERATION_LEFT_BACKWARD_REQUEST = NOW_ACCELERATION_REQUEST +
            SUBREQUEST_SEPARATOR + LEFT + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String NOW_ACCELERATION_RIGHT_FORWARD_REQUEST = NOW_ACCELERATION_REQUEST +
            SUBREQUEST_SEPARATOR + RIGHT + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String NOW_ACCELERATION_RIGHT_BACKWARD_REQUEST = NOW_ACCELERATION_REQUEST +
            SUBREQUEST_SEPARATOR + RIGHT + SUBREQUEST_SEPARATOR + BACKWARD;


    public static final String THEORETICAL_ACCELERATION_REQUEST = "theoreticalAcceleration";
    public static final String THEORETICAL_ACCELERATION_STRAIGHT_FORWARD_REQUEST = THEORETICAL_ACCELERATION_REQUEST +
            SUBREQUEST_SEPARATOR + STRAIGHT + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String THEORETICAL_ACCELERATION_STRAIGHT_BACKWARD_REQUEST = THEORETICAL_ACCELERATION_REQUEST +
            SUBREQUEST_SEPARATOR + STRAIGHT + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String THEORETICAL_ACCELERATION_LEFT_FORWARD_REQUEST = THEORETICAL_ACCELERATION_REQUEST +
            SUBREQUEST_SEPARATOR + LEFT + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String THEORETICAL_ACCELERATION_LEFT_BACKWARD_REQUEST = THEORETICAL_ACCELERATION_REQUEST +
            SUBREQUEST_SEPARATOR + LEFT + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String THEORETICAL_ACCELERATION_RIGHT_FORWARD_REQUEST = THEORETICAL_ACCELERATION_REQUEST +
            SUBREQUEST_SEPARATOR + RIGHT + SUBREQUEST_SEPARATOR + FORWARD;
    public static final String THEORETICAL_ACCELERATION_RIGHT_BACKWARD_REQUEST = THEORETICAL_ACCELERATION_REQUEST +
            SUBREQUEST_SEPARATOR + RIGHT + SUBREQUEST_SEPARATOR + BACKWARD;

    public static final String DECELERATION_COMFORT_LEFT_BACKWARD_REQUEST = DECELERATION_COMFORT_REQUEST +
            SUBREQUEST_SEPARATOR + LEFT + SUBREQUEST_SEPARATOR + BACKWARD;
    public static final String DECELERATION_COMFORT_RIGHT_BACKWARD_REQUEST = DECELERATION_COMFORT_REQUEST +
            SUBREQUEST_SEPARATOR + RIGHT + SUBREQUEST_SEPARATOR + BACKWARD;

    public static final String DECELERATION_COMFORT_REQUEST_STRAIGHT_FORWARD = DECELERATION_COMFORT_REQUEST +
            SUBREQUEST_SEPARATOR + STRAIGHT + SUBREQUEST_SEPARATOR + FORWARD;

    public static final String TIME_STEP_REQUEST = "timeStep";
    public static final String MAX_ROAD_SPEED_REQUEST = "maxRoadSpeed";

}
