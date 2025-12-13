package core.utils.constants;

import core.model.CarGenerator;

public class DefaultValues {

    //default parameters for car generator
    public static final double DEFAULT_MAX_SPEED_MAX = 50;
    public static final double DEFAULT_MAX_SPEED_MIN = 20;
    public static final double DEFAULT_LENGTH_MAX = 8.0;
    public static final double DEFAULT_LENGTH_MIN = 1.0;
    public static final double DEFAULT_ACCELERATION_MAX = 2.0;
    public static final double DEFAULT_ACCELERATION_MIN = 1.0;
    public static final double DEFAULT_MINIMUM_GAP_TO_NEXT_CAR_MAX = 5.0;
    public static final double DEFAULT_MINIMUM_GAP_TO_NEXT_CAR_MIN = 2.0;
    public static final double DEFAULT_DECELERATION_COMFORT_MAX = 5.0;
    public static final double DEFAULT_DECELERATION_COMFORT_MIN = 2.0;
    public static final double DEFAULT_DESIRED_TIME_HEADWAY_MAX = 2.5;
    public static final double DEFAULT_DESIRED_TIME_HEADWAY_MIN = 1.0;
    public static final double DEFAULT_SPEED_DIFFERENCE_SENSITIVITY_MAX = 1.5;
    public static final double DEFAULT_SPEED_DIFFERENCE_SENSITIVITY_MIN = 0.5;
    public static final double DEFAULT_DISTANCE_DIFFERENCE_SENSITIVITY_MAX = 1.5;
    public static final double DEFAULT_DISTANCE_DIFFERENCE_SENSITIVITY_MIN = 0.5;
    /////////////////////////////////////////

    public static final double DEFAULT_ROAD_MAX_SPEED = 14.0; // m/s (~50 km/h)
    public static final int DEFAULT_ROAD_LENGTH = 1000; // meters
    public static final int DEFAULT_ROAD_LANES = 3;

    public static final double DEFAULT_FLOW_RATE = 0.2;
    public static final CarGenerator DEFAULT_CAR_GENERATOR = new CarGenerator(DEFAULT_FLOW_RATE) {{
        addParameter(RequestConstants.MAX_SPEED_REQUEST, DEFAULT_MAX_SPEED_MIN, DEFAULT_MAX_SPEED_MAX);
        addParameter(RequestConstants.LENGTH_REQUEST, DEFAULT_LENGTH_MIN, DEFAULT_LENGTH_MAX);
        addParameter(RequestConstants.MAX_ACCELERATION_REQUEST, DEFAULT_ACCELERATION_MIN, DEFAULT_ACCELERATION_MAX);
        addParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, DEFAULT_MINIMUM_GAP_TO_NEXT_CAR_MIN,
                DEFAULT_MINIMUM_GAP_TO_NEXT_CAR_MAX);
        addParameter(RequestConstants.DECELERATION_COMFORT_REQUEST, DEFAULT_DECELERATION_COMFORT_MIN,
                DEFAULT_DECELERATION_COMFORT_MAX);
        addParameter(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, DEFAULT_DESIRED_TIME_HEADWAY_MIN,
                DEFAULT_DESIRED_TIME_HEADWAY_MAX);
        addParameter(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                DEFAULT_SPEED_DIFFERENCE_SENSITIVITY_MIN, DEFAULT_SPEED_DIFFERENCE_SENSITIVITY_MAX);
        addParameter(RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST,
                DEFAULT_DISTANCE_DIFFERENCE_SENSITIVITY_MIN, DEFAULT_DISTANCE_DIFFERENCE_SENSITIVITY_MAX);
    }};

}
