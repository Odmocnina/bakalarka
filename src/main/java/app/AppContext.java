package app;

import core.sim.Simulation;
import core.utils.Constants;
import core.utils.RunDetails;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import ui.render.IRoadRenderer;

/*****************************
 * Application context holding global static references to important objects
 *
 * @author Michael Hladky
 * @version 1.0
 ****************************/
public final class AppContext {

    /**
     * private constructor to prevent instantiation
     **/
    private AppContext() {}

    /** renderer for rendering roads in gui window **/
    public static IRoadRenderer RENDERER;

    /** car following model used in simulation **/
    public static ICarFollowingModel CAR_FOLLOWING_MODEL;

    /** lane changing model used in simulation **/
    public static ILaneChangingModel LANE_CHANGING_MODEL;

    /** size of cell in cellular automaton road, -1 if not using cellular automaton **/
    public static double cellSize = Constants.PARAMETER_UNDEFINED; // i dont like this shit, but have no idea how to do
                                                                   // it better without fucking the entire thing up

    /** simulation, for stepping **/
    public static Simulation SIMULATION;

    /** run detail such as if gui is to be shown, if results are to be written... **/
    public static RunDetails RUN_DETAILS;
}
