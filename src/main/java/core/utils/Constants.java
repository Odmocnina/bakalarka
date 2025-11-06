package core.utils;

import javafx.scene.paint.Color;

public class Constants {

    public static final int CANVAS_WIDTH = 600;
    public static final int CANVAS_HEIGHT = 300;
    public static final Color ROAD_COLOR = Color.DARKGRAY;
    public static final Color LINE_SEPERATOR_COLOR = Color.WHITE;
    public static final String CONFIG_FILE = "config/config.xml";
    public static final String CELLULAR = "cellular";
    public static final String CONTINOUS = "continous";
    public static final int LINE_SEPARATOR_WIDTH = 2;
    public static final int CELL_SEPARATOR_WIDTH = 1;
    public static final Color CELL_SEPARATOR_COLOR = Color.BLACK;
    public static final String REQUEST_SEPARATOR = ";";
    public static final int NO_CAR_IN_FRONT = -1;
    public static final double PARAMETER_UNDEFINED = -1.0;
    public static final double LANE_WIDTH = 3.5; // in meters
    public static final int NO_LANE_THERE = -2;
    public static final Color[] CAR_COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE,
            Color.PURPLE, Color.PINK, Color.BROWN, Color.CYAN, Color.MAGENTA
    };

    // output file types
    public static final String CSV_TYPE = "csv";
    public static final String CONSOLE_TYPE = "console";
    public static final String TXT_TYPE = "txt";
    public static final double LANE_WIDTH_METERS = 8.0;


    // request string used when model is requesting parameters of road
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
    public static final String DISTANCE_DIFFRENCE_SENSITIVITY_PARAMETER_REQUEST = "distanceDifferenceSensitivity";
    public static final String SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST = "speedDifferenceSensitivity";
    public static final String CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST = "currentSpeed_straight_forward";
    public static final String CURRENT_SPEED_STRAIGHT_BACKWARDS_REQUEST = "currentSpeed_backward_forward";
    public static final String CURRENT_SPEED_LEFT_FORWARD_REQUEST = "currentSpeed_left_forward";
    public static final String CURRENT_SPEED_LEFT_BACKWARD_REQUEST = "currentSpeed_left_backward";
    public static final String CURRENT_SPEED_RIGHT_FORWARD_REQUEST = "currentSpeed_right_forward";
    public static final String CURRENT_SPEED_RIGHT_BACKWARD_REQUEST = "currentSpeed_right_backward";

    // stock values for car generation
    public static final double DEFAULT_MIN_LENGTH = 3.0;
    public static final double DEFAULT_MAX_LENGTH = 5.0;
    public static final double DEFAULT_MIN_MAX_SPEED = 15.0; // m/s
    public static final double DEFAULT_MAX_MAX_SPEED = 60.0; // m/s
    public static final double DEFAULT_MIN_ACCELERATION = 1.0; // m/s^2
    public static final double DEFAULT_MAX_ACCELERATION = 3.0; // m/s^2
    public static final double DEFAULT_MIN_DECELERATION = 2.0; // m/s^2
    public static final double DEFAULT_MAX_DECELERATION = 5.0; // m/s^2
    public static final double DEFAULT_MIN_DESIRED_TIME_HEADWAY = 1.0; // s
    public static final double DEFAULT_MAX_DESIRED_TIME_HEADWAY = 2.5; // s
    public static final double DEFAULT_MIN_MIN_GAP_TO_NEXT_CAR = 2.0; // m
    public static final double DEFAULT_MAX_MIN_GAP_TO_NEXT_CAR = 5.0; // m
    public static final double DEFAULT_MIN_REACTION = 1.0;
    public static final double DEFAULT_MAX_REACTION = 0.2;
    public static final double DEFAULT_MIN_POLITENESS_FACTOR = 0.0;
    public static final double DEFAULT_MAX_POLITENESS_FACTOR = 1.0;

    // constants for config loading
    public static final String GENERATOR_TAG = "generator";
    public static final String RUN_DETAILS_TAG = "runDetails";
    public static final String MODELS_TAG = "models";

    public static final String CAR_FOLLOWING_MODEL_TAG = "carFollowingModel";

}
