package app;

import core.engine.Engine;
import core.model.CarGenerator;
import core.model.Road;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import ui.render.IRoadRenderer;

public final class AppContext {
    private AppContext() {}
    public static Road[] ROADS;
    public static IRoadRenderer RENDERER;
    public static ICarFollowingModel CAR_FOLLOWING_MODEL;
    public static ILaneChangingModel LANE_CHANGING_MODEL;
    public static double cellSize = -1;
    public static boolean drawCells = false;
    public static CarGenerator CAR_GENERATOR;
    public static Engine ENGINE;
}
