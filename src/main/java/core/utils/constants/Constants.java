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

    /** canvas width for GUI **/
    public static final int CANVAS_WIDTH = 600;

    /** canvas height for GUI **/
    public static final int CANVAS_HEIGHT = 300;

    /** color for drawing roads in GUI **/
    public static final Color ROAD_COLOR = Color.DARKGRAY;

    /** color for drawing lane separators in GUI **/
    public static final Color LINE_SEPERATOR_COLOR = Color.WHITE;

    /** default configuration file path for loading simulation setup, can be overridden by input parameters **/
    public static final String DEFAULT_CONFIG_FILE = "config/config.xml";

    /** tag for cellular type of model, used by models to identify themselves as cellular **/
    public static final String CELLULAR = "cellular";

    /** tag for continuous type of model, used by models to identify themselves as continuous **/
    public static final String CONTINUOUS = "continous";

    /** width of lane separators in GUI, in pixels **/
    public static final int LINE_SEPARATOR_WIDTH = 2;

    /** width of cell separators in GUI, in pixels **/
    public static final int CELL_SEPARATOR_WIDTH = 1;

    /** color for drawing cell separators in GUI **/
    public static final Color CELL_SEPARATOR_COLOR = Color.BLACK;

    /** value indicating that there is no car in front of the current car **/
    public static final int NO_CAR_IN_FRONT = -1;

    /** value indicating that parameter in car or generator is not defined **/
    public static final double PARAMETER_UNDEFINED = -1.0;

    /** value indicating that there is no lane in the given direction (for lane change models) for example when car is
     at most left lane and looks for left lane **/
    public static final int NO_LANE_THERE = -2;

    /** car colors for drawing cars in GUI **/
    public static final Color[] CAR_COLORS = {
            Color.DARKRED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE,
            Color.PURPLE, Color.PINK, Color.BROWN, Color.CYAN, Color.MAGENTA
    };

    /** scale factor for drawing roads in GUI when using continuous models for better visualization **/
    public static final double CONTINOUS_ROAD_DRAWING_SCALE_FACTOR = 2.0;

    /** value indicating that there is no car in place where was looked **/
    public static final double NO_CAR_THERE = -10000.0;

    /** warn tag used for logging **/
    public static final String WARN_FOR_LOGGING = "warn";

    /** debug tag used for logging **/
    public static final String DEBUG_FOR_LOGGING = "debug";

    /** fatal tag used for logging **/
    public static final String FATAL_FOR_LOGGING = "fatal";

    /** error tag used for logging **/
    public static final String ERROR_FOR_LOGGING = "error";

    /** info tag used for logging **/
    public static final String INFO_FOR_LOGGING = "info";

    /** tag used for type csv for output in program **/
    public static final String RESULTS_OUTPUT_CSV = "csv";

    /** tag used for type txt for output in program **/
    public static final String RESULTS_OUTPUT_TXT = "txt";

    /** default output file name for output when not provided in input parameters **/
    public static final String DEFAULT_OUTPUT_FILE = "output.txt";

    /** default csv separator for output when not provided in input parameters **/
    public static final String DEFAULT_CSV_SEPARATOR = ";";

    /** default output file name for output when not provided in input parameters **/
    public static final String newMapFileName = "newMap.xml";

    // logging indexes constants

    /** index for general logging in logging array **/
    public static final int GENERAL_LOGGING_INDEX = 0;

    /** index for info logging in logging array **/
    public static final int INFO_LOGGING_INDEX = 1;

    /** index for warning logging in logging array **/
    public static final int WARN_LOGGING_INDEX = 2;

    /** index for error logging in logging array **/
    public static final int ERROR_LOGGING_INDEX = 3;

    /** index for fatal logging in logging array **/
    public static final int FATAL_LOGGING_INDEX = 4;

    /** index for debug logging in logging array **/
    public static final int DEBUG_LOGGING_INDEX = 5;

    // duration from input parameters constants

    /** value indicating that duration was not provided in input parameters **/
    public static final int NO_DURATION_PROVIDED = -1;

    /** value indicating that input parameters provided for duration are invalid **/
    public static final int INVALID_INPUT_PARAMETERS = -2;

    /** value indicating that there is no record yet for duration **/
    public static final int NO_RECORD_YET = -3;

    // input parameters prefixes

    /** prefix for duration parameter in input parameters **/
    public static final String DURATION_PARAMETER_PREFIX = "--dur=";

    /** prefix for configuration file path parameter in input parameters **/
    public static final String CONFIG_PATH_PARAMETER_PREFIX = "--cfg=";

    /** prefix for output file path parameter in input parameters **/
    public static final String OUTPUT_FILE_PARAMETER_PREFIX = "--out=";

    /** prefix for forward model in input parameters **/
    public static final String CAR_FOLLOWING_MODEL_PARAMETER_PREFIX = "--cfm=";

    /** prefix for lane changing model in input parameters **/
    public static final String LANE_CHANGING_MODEL_PARAMETER_PREFIX = "--lcm=";

    /** prefix for toggle logging on/off in input parameters **/
    public static final String LOGGING_PARAMETER_PREFIX = "--log=";

    /** prefix for map file path parameter in input parameters, used for loading map from XML file **/
    public static final String MAP_FILE_PARAMETER_PREFIX = "--map=";

    /** prefix for help parameter in input parameters, used for showing help message in console **/
    public static final String HELP_PARAMETER_PREFIX = "--help";

    // state of logging in input parameters

    /** value indicating that logging is on from input parameters **/
    public static final int LOGGING_ON_FROM_INPUT_PARAMETERS = 1;

    /** value indicating that logging is not provided in input parameters **/
    public static final int LOGGING_NOT_PROVIDED_IN_INPUT_PARAMETERS = 0;

    /** value indicating that logging is off from input parameters **/
    public static final int LOGGING_OFF_FROM_INPUT_PARAMETERS = -1;
}
