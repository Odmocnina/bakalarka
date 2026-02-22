package core.utils;

import app.AppContext;

/****************************************
 * Class responsible for changing the configuration of the simulation, for example changing the time between steps or
 * changing the lane change ban, this is used in the gui to change the configuration of the simulation during run of the
 * application
 *
 * @author Michael Hladky
 * @version 1.0
 ****************************************/
public class ConfigModification {

    /**
     * method to change the lane change ban, it toggles the lane change ban (on/off) in the simulation context
     **/
    public static void changeLaneChangeBan() {
        AppContext.RUN_DETAILS.laneChange = !AppContext.RUN_DETAILS.laneChange;
    }

    /**
     * method to set the time between steps, it updates the time between steps in the simulation context with the given
     * value
     *
     * @param timeBetweenSteps new time between steps to be set in the simulation context
     **/
    public static void setTimeBetweenSteps(int timeBetweenSteps) {
        AppContext.RUN_DETAILS.timeBetweenSteps = timeBetweenSteps;
    }

    /**
     * method to change the draw cells setting, it toggles the draw cells setting (on/off) in the simulation context
     **/
    public static void changeDrawCells() {
        AppContext.RUN_DETAILS.drawCells = !AppContext.RUN_DETAILS.drawCells;
    }

    /**
     * method to change the prevention collision setting, it toggles the forbid collision setting (on/off) in the simulation context
     **/
    public static void changePreventCollision() {
        AppContext.RUN_DETAILS.preventCollisions = !AppContext.RUN_DETAILS.preventCollisions;
    }

    /**
     * method to change the logging settings, it toggles the logging setting at the specified index (on/off) in the
     * simulation context
     *
     * @param index index of the logging setting to be toggled in the simulation context
     **/
    public static void changeLogging(int index) {
        AppContext.RUN_DETAILS.log[index] = !AppContext.RUN_DETAILS.log[index];
    }

    /**
     * method to change the output settings, it toggles the output setting for the specified key (on/off) in the
     * simulation context
     *
     * @param key key of the output setting to be toggled in the simulation context
     **/
    public static void changeOutput(String key) {
        AppContext.RUN_DETAILS.outputDetails.setPart(key, !AppContext.RUN_DETAILS.outputDetails.writePart(key));
    }
}
