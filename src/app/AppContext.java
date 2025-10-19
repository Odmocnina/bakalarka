package app;

import core.model.CarGenerator;
import core.model.Road;
import models.ICarFollowingModel;
import ui.render.IRoadRenderer;

public final class AppContext {
    private AppContext() {}
    public static Road[] ROADS;
    public static IRoadRenderer RENDERER;
    public static ICarFollowingModel CAR_FOLLOWING_MODEL;
    public static double cellSize = -1;
    public static boolean drawCells = false;
    public static CarGenerator CAR_GENERATOR;
}
