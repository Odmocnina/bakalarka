package app;

import core.model.CarGenerator;
import core.model.Road;
import core.sim.Simulation;
import core.utils.ConfigLoader;
import core.utils.Constants;
import core.utils.ResultsRecorder;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
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

        ConfigLoader.giveConfigFile(configFile);

        ICarFollowingModel carFollowingModel = ConfigLoader.loadCarFollowingModel(configFile);
        if (carFollowingModel == null) {
            System.out.println("Failed to load car following model, exiting.");
            return;
        } else {
            System.out.println("Loaded car following model: " + carFollowingModel.getID());
        }
        AppContext.CAR_FOLLOWING_MODEL = carFollowingModel;

        ILaneChangingModel laneChangingModel = ConfigLoader.loadLaneChangingModel(configFile);
        if (laneChangingModel == null) {
            System.out.println("Failed to load lane changing model, exiting.");
            return;
        } else {
            System.out.println("Loaded lane changing model: " + laneChangingModel.getID());
        }
        AppContext.LANE_CHANGING_MODEL = laneChangingModel;

        CarGenerator carGenerator = ConfigLoader.loadCarGenerator(configFile);
        if (carGenerator == null) {
            System.out.println("Failed to load car generator, exiting.");
            return;
        } else {
            System.out.println("Loaded car generator: " + carGenerator.toString());
        }
        AppContext.CAR_GENERATOR = carGenerator;

        if (carGenerator.checkIfAllParametresAreLoaded(carFollowingModel.requestParameters())) {
            System.out.println("Car generator parameters are valid for the selected car following model.");
        } else {
            System.out.println("Car generator parameters are NOT valid for the selected car following model, exiting.");
            return;
        }

        Road[] roads = ConfigLoader.loadRoads(configFile);

        if (roads == null) {
            System.out.println("Failed to load road configuration, exiting.");
            return;
        } else {
            System.out.println("Loaded road: " + roads[0].toString());
        }
        if (!roads[0].getType().equals(carFollowingModel.getType())) {
            System.out.println("Types of car following model and road do not match, exiting.");
            return;
        }
        AppContext.CAR_GENERATOR.setType(roads[0].getType());
        AppContext.ROADS = roads;

        IRoadRenderer renderer;
        if (roads[0].getType().equals(Constants.CELLULAR)) {
            renderer = new CellularRoadRenderer();
        } else if (roads[0].getType().equals(Constants.CONTINOUS)) {
            renderer = new ContinousRoadRenderer();
        } else {
            System.out.println("Unknown road type: " + roads[0].getType());
            return;
        }
        AppContext.RENDERER = renderer;

        ResultsRecorder.getResultsRecorder().initialize(roads.length);
        Simulation sim = new Simulation(roads);
        AppContext.SIMULATION = sim;


        Window.main(args);
    }
}