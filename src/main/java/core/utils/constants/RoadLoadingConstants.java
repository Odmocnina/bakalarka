package core.utils.constants;

/***************************
 * Class holding constants for loading roads from XML, this is used in the RoadLoader class to load roads from XML
 * files, it holds the tags used in the XML file
 *
 * @author Michael Hladky
 * @version 1.0
 ***************************/
public class RoadLoadingConstants {

    /** tag for road length in map XML file **/
    public static final String ROAD_LENGTH_TAG = "length";

    /** tag for road max speed in map XML file **/
    public static final String ROAD_MAX_SPEED_TAG = "maxSpeed";

    /** tag for lane in map XML file **/
    public static final String ROAD_LANE_TAG = "lane";

    /** tag for road in map XML file **/
    public static final String ROAD_TAG = "road";

    /** tag for map in map XML file **/
    public static final String MAP_TAG = "map";

    /** tag for index of road in map XML file **/
    public static final String ROAD_INDEX_TAG = "index";

    /** tag for traffic light plan in map XML file **/
    public static final String LIGHT_PLAN_TAG = "lightPlan";

    /** tag for generator in map XML file **/
    public static final String GENERATOR_TAG = "generator";

    /** tag for number of lanes in map XML file **/
    public static final String NUMBER_OF_LANES_TAG = "numberOfLanes";

    /** tag for cycle duration of traffic light plan in map XML file **/
    public static final String CYCLE_DURATION_TAG = "cycleDuration";

    /** tag for time of switch of traffic light plan in map XML file **/
    public static final String TIME_OF_SWITCH_TAG = "timeOfSwitch";

    /** tag if traffic light plan starts with green in map XML file **/
    public static final String START_WITH_GREEN_TAG = "startWithGreen";

    /** tag for flow rate of generator in map XML file **/
    public static final String FLOW_RATE_TAG = "flowRate";

    /** tag for parameters of generator in map XML file **/
    public static final String CAR_PARAMS_TAG = "carParams";

    /** tag for minimum value of parameter in generator in map XML file **/
    public static final String MIN_VALUE_TAG = "minValue";

    /** tag for maximum value of parameter in generator in map XML file **/
    public static final String MAX_VALUE_TAG = "maxValue";

    /** tag for parameter name in generator in map XML file **/
    public static final String NAME_TAG = "name";

    /** tag for queue in generator in map XML file **/
    public static final String QUEUE_TAG = "queue";

    /** tag queue is supposed to be used in generator in map XML file **/
    public static final String USE_TAG = "use";

}
