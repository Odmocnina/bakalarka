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
    public static final int DEFAULT_LIGHT_PLAN_CYCLE_DURATION = 60; // seconds
    public static final int DEFAULT_LIGHT_PLAN_GREEN_DURATION = 30; // seconds
    public static final boolean DEFAULT_LIGHT_PLAN_START_WITH_GREEN = true;

    public static final double DEFAULT_FLOW_RATE = 0.2;
    public static final CarGenerator DEFAULT_CAR_GENERATOR = new CarGenerator(DEFAULT_FLOW_RATE) {{

    }};

}
