package app;

import core.model.CarGenerator;
import core.model.Road;
import core.utils.ConfigLoader;
import core.utils.Constants;
import models.ICarFollowingModel;
import ui.Window;
import ui.render.CellularRoadRenderer;
import ui.render.ContinousRoadRenderer;
import ui.render.IRoadRenderer;

public class Main {
    public static void main(String[] args) {
        String configFile;
        if (args.length == 0) {
            System.out.println("No config file provided, using default: " + Constants.CONFIG_FILE);
            configFile = Constants.CONFIG_FILE;
        } else {
            configFile = args[0];
        }

        ICarFollowingModel carFollowingModel = ConfigLoader.loadCarFollowingModel(configFile);
        if (carFollowingModel == null) {
            System.out.println("Failed to load car following model, exiting.");
            return;
        } else {
            System.out.println("Loaded car following model: " + carFollowingModel.getID());
        }
        AppContext.CAR_FOLLOWING_MODEL = carFollowingModel;

        CarGenerator carGenerator = ConfigLoader.loadCarGenerator(configFile);
        if (carGenerator == null) {
            System.out.println("Failed to load car generator, exiting.");
            return;
        } else {
            System.out.println("Loaded car generator: " + carGenerator.toString());
        }
        AppContext.CAR_GENERATOR = carGenerator;

        Road road = ConfigLoader.loadRoad(configFile);
        if (road == null) {
            System.out.println("Failed to load road configuration, exiting.");
            return;
        } else {
            System.out.println("Loaded road: " + road.toString());
        }
        if (!road.getType().equals(carFollowingModel.getType())) {
            System.out.println("Types of car following model and road do not match, exiting.");
            return;
        }
        AppContext.CAR_GENERATOR.setType(road.getType());
        AppContext.ROAD = road;

        IRoadRenderer renderer;
        if (road.getType().equals(Constants.CELLULAR)) {
            renderer = new CellularRoadRenderer();
        } else if (road.getType().equals(Constants.CONTINOUS)) {
            renderer = new ContinousRoadRenderer();
        } else {
            System.out.println("Unknown road type: " + road.getType());
            return;
        }
        AppContext.RENDERER = renderer;

        Window.main(args);
    }
}