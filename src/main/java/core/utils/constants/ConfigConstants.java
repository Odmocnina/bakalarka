package core.utils.constants;

/***************************
 * Class holding configuration constants for simulation setup, string tags used in configuration file when loading
 *
 * @author Michael Hladky
 * @version 1.0
 ***************************/
public class ConfigConstants {

    /** road file tag in configuration file **/
    public static final String ROAD_FILE_TAG = "roadFile";

    /** tag for models section in configuration file **/
    public static final String MODELS_TAG = "models";

    /** tag for forward model in configuration file **/
    public static final String CAR_FOLLOWING_MODEL_TAG = "carFollowingModel";

    /** tag for lane changing model in configuration file **/
    public static final String LANE_CHANGING_MODEL_TAG = "laneChangingModel";

    /** id tag for models in configuration file **/
    public static final String ID_TAG = "id";

    /** tag for run details section in configuration file **/
    public static final String RUN_DETAILS_TAG = "runDetails";

    /** tag for draw cells option in configuration file **/
    public static final String DRAW_CELLS_TAG = "drawCells";

    /** tag for lane change toggle in configuration file **/
    public static final String LANE_CHANGE_TAG = "laneChange";

    /** tag for time step in configuration file **/
    public static final String TIME_STEP_TAG = "timeStep";

    /** tag for debug mode in configuration file **/
    public static final String DEBUG_TAG = "debug";

    /** tag for output section in configuration file **/
    public static final String OUTPUT_TAG = "output";

    /** tag for write output option in configuration file **/
    public static final String WRITE_OUTPUT_TAG = "writeOutput";

    /** tag for output file name in configuration file **/
    public static final String FILE_TAG = "file";

    /** tag for output type in configuration file (csv/txt) **/
    public static final String TYPE_TAG = "type";

    /** tag for csv separator in configuration file **/
    public static final String CSV_SEPARATOR_TAG = "csvSeparator";

    /** tag for time between steps (GUI mode) in configuration file **/
    public static final String TIME_BETWEEN_STEPS_TAG = "timeBetweenSteps";

    /** tag to prevent collisions toggle in configuration file **/
    public static final String PREVENT_COLLISION_TAG = "preventCollision";

    /** tag for random seed in configuration file **/
    public static final String SEED_TAG = "seed";


    // logging constants

    /** tag for logging section in configuration file **/
    public static final String LOGGING_TAG = "logging";

    /** tag for log toggle in configuration file **/
    public static final String LOG_GENERAL_TAG = "log";

    /** tag for log info toggle in configuration file **/
    public static final String LOG_INFO_TAG = "info";

    /** tag for log warning toggle in configuration file **/
    public static final String LOG_WARN_TAG = "warn";

    /** tag for log debug toggle in configuration file **/
    public static final String LOG_DEBUG_TAG = "debug";

    /** tag for log error toggle in configuration file **/
    public static final String LOG_ERROR_TAG = "error";

    /** tag for log fatal toggle in configuration file **/
    public static final String LOG_FATAL_TAG = "fatal";


    // what to write tags

    /** tag for what to write section in configuration file **/
    public static final String WHAT_TO_WRITE_TAG = "whatToWrite";

    /** tag for toggle writing simulation details in configuration file **/
    public static final String SIMULATION_DETAILS_TAG = "simulationDetails";

    /** tag for toggle writing simulation time in configuration file **/
    public static final String SIMULATION_TIME_TAG = "simulationTime";

    /** tag for toggle writing cars passed in configuration file **/
    public static final String CARS_PASSED_TAG = "carsPassed";

    /** tag for toggle writing cars on road in configuration file **/
    public static final String CARS_ON_ROAD_TAG = "carsOnRoad";

    /** tag for toggle writing road details in configuration file **/
    public static final String ROAD_DETAILS_TAG = "roadDetails";

    /** tag for toggle writing collision count in configuration file **/
    public static final String COLLISION_COUNT_TAG = "collisionCount";

    /** tag for toggle writing when road was empty in configuration file **/
    public static final String WHEN_WAS_ROAD_EMPTY_TAG = "whenWasRoadEmpty";

    /** tag for toggle writing lane changes count in configuration file **/
    public static final String LANE_CHANGES_COUNT_TAG = "laneChangesCount";

    /** tag for toggle writing average lane queue length in configuration file **/
    public static final String AVERAGE_LANE_QUEUE_LENGTH_TAG = "averageLaneQueueLength";

    /** tag for toggle writing average lane queue length at last red light in configuration file **/
    public static final String AVERAGE_LANE_QUEUE_LAST_RED_LENGTH_TAG = "averageLaneQueueLastRedLength";

    /** tag for toggle writing maximum lane queue length in configuration file **/
    public static final String MAX_LANE_QUEUE_LENGTH_TAG = "maxLaneQueueLength";

    /** tag for toggle writing detailed lane queue length in configuration file **/
    public static final String DETAILED_LANE_QUEUE_LENGTH_TAG = "detailedLaneQueueLength";

    /** tag for toggle writing detailed light plans in configuration file **/
    public static final String DETAILED_LIGHT_PLANS_TAG = "detailedLightPlans";

    /** tag for toggle writing detailed lane change, light plans in configuration file **/
    public static final String EXPORT_DETAILED_TO_SEPARATE_FILES_TAG = "exportDetailedToSeparateFiles";
}
