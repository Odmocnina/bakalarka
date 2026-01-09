package core.utils;

import app.AppContext;

public class ConfigModificator {

    public static void changeLaneChangeBan() {
        AppContext.RUN_DETAILS.laneChange = !AppContext.RUN_DETAILS.laneChange;
    }

    public static void setCollisionBan() {
        AppContext.RUN_DETAILS.preventCollisions = true;
    }

    public static void setTimeBetweenSteps(int timeBetweenSteps) {
        AppContext.RUN_DETAILS.timeBetweenSteps = timeBetweenSteps;
    }

    public static void changeDrawCells() {
        AppContext.RUN_DETAILS.drawCells = !AppContext.RUN_DETAILS.drawCells;
    }

    public static void changePreventCollision() {
        AppContext.RUN_DETAILS.preventCollisions = !AppContext.RUN_DETAILS.preventCollisions;
    }




}
