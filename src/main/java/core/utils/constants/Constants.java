package core.utils.constants;

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

}
