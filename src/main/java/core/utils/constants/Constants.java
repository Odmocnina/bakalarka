package core.utils.constants;

import javafx.scene.paint.Color;

/********************************************
 * Class containing constant values/stuff that did not fit in the other constant classes, stuff like colors, tags of
 * road/model types, canvas sizes, logging tags, etc.
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class Constants {

    public static final int CANVAS_WIDTH = 600;
    public static final int CANVAS_HEIGHT = 300;
    public static final Color ROAD_COLOR = Color.DARKGRAY;
    public static final Color LINE_SEPERATOR_COLOR = Color.WHITE;
    public static final String DEFAULT_CONFIG_FILE = "config/config.xml";
    public static final String CELLULAR = "cellular";
    public static final String CONTINUOUS = "continous";
    public static final int LINE_SEPARATOR_WIDTH = 2;
    public static final int CELL_SEPARATOR_WIDTH = 1;
    public static final Color CELL_SEPARATOR_COLOR = Color.BLACK;

    public static final int NO_CAR_IN_FRONT = -1;
    public static final double PARAMETER_UNDEFINED = -1.0;
    public static final double LANE_WIDTH = 3.5; // in meters
    public static final int NO_LANE_THERE = -2;
    public static final Color[] CAR_COLORS = {
            Color.DARKRED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE,
            Color.PURPLE, Color.PINK, Color.BROWN, Color.CYAN, Color.MAGENTA
    };
    public static final double CONTINOUS_ROAD_DRAWING_SCALE_FACTOR = 2.0;
    public static final String GENERATOR_QUEUE = "queue";
    public static final double NO_CAR_THERE = -10000.0;
    public static final String WARN_FOR_LOGGING = "warn";
    public static final String DEBUG_FOR_LOGGING = "debug";
    public static final String FATAL_FOR_LOGGING = "fatal";
    public static final String ERROR_FOR_LOGGING = "error";
    public static final String INFO_FOR_LOGGING = "info";

    public static final String RESULTS_OUTPUT_CSV = "csv";
    public static final String RESULTS_OUTPUT_TXT = "txt";
    public static final String DEFAULT_OUTPUT_FILE = "output/output.txt";
    public static final String DEFAULT_CSV_SEPARATOR = ";";

    public static final String newMapFileName = "newMap.xml";

    public static final int GENERAL_LOGGING_INDEX = 0;
    public static final int INFO_LOGGING_INDEX = 1;
    public static final int WARN_LOGGING_INDEX = 2;
    public static final int ERROR_LOGGING_INDEX = 3;
    public static final int FATAL_LOGGING_INDEX = 4;
    public static final int DEBUG_LOGGING_INDEX = 5;

    // indices for output details, this is used in the OutputDetails class to determine which details to output based on
    // the configuration file, the indices correspond to the order of the details in the output array in the
    // OutputDetails class
    public static final int SIMULATION_DETAILS_OUTPUT_INDEX = 0;
    public static final int SIMULATION_TIME_OUTPUT_INDEX = 1;
    public static final int CARS_PASSED_OUTPUT_INDEX = 2;
    public static final int CARS_ON_ROAD_OUTPUT_INDEX = 3;
    public static final int WHEN_WAS_ROAD_EMPTY_OUTPUT_INDEX = 4;

    public static final int LANE_CHANGES_COUNT_OUTPUT_INDEX = 5;
    public static final int COLLISION_COUNT_OUTPUT_INDEX = 5;
    public static final int ROAD_DETAILS_OUTPUT_INDEX = 6;


    public static final int NO_DURATION_PROVIDED = -1;
    public static final int INVALID_INPUT_PARAMETERS = -2;
    public static final int NO_RECORD_YET = -3;

    public static final String DURATION_PARAMETER_PREFIX = "--dur=";
    public static final String CONFIG_PATH_PARAMETER_PREFIX = "--cfg=";
    public static final String OUTPUT_FILE_PARAMETER_PREFIX = "--out=";
    public static final String CAR_FOLLOWING_MODEL_PARAMETER_PREFIX = "--cfm=";
    public static final String LANE_CHANGING_MODEL_PARAMETER_PREFIX = "--lcm=";
    public static final String LOGGING_PARAMETER_PREFIX = "--log=";
    public static final String MAP_FILE_PARAMETER_PREFIX = "--map=";

    public static final int LOGGING_ON_FROM_INPUT_PARAMETERS = 1;
    public static final int LOGGING_NOT_PROVIDED_IN_INPUT_PARAMETERS = 0;
    public static final int LOGGING_OFF_FROM_INPUT_PARAMETERS = -1;
}
