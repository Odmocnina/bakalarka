package app;

import core.model.CarGenerator;
import core.model.Road;
import core.sim.Simulation;
import core.utils.*;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import ui.Window;
import ui.render.CellularRoadRenderer;
import ui.render.ContinuousRoadRenderer;
import ui.render.IRoadRenderer;


/*****************************
 * Main application class, starts application and loads data from configuration
 *
 * @author Michael Hladky
 * @version 1.0
 *********************************/
public class Main {

    /*
     * main function of application, loads configuration and starts gui if gui is supposed to be used, returns nothing
     *
     * @param args command line arguments, first argument is path to configuration file, if not provided default config
     *             file is used
     */
    public static void main(String[] args) {
        String configFile;
        if (args.length == 0) { // use default config file if none provided
            MyLogger.logBeforeLoading("No config file provided, using default: " + Constants.CONFIG_FILE,
                    Constants.WARN_FOR_LOGGING);
            configFile = Constants.CONFIG_FILE;
        } else {
            MyLogger.logBeforeLoading("Config file provided: " + args[0], Constants.INFO_FOR_LOGGING);
            configFile = args[0];
        }

        if (ConfigLoader.giveConfigFile(configFile)) { // wierd thing that I thought of, config file was opened multiple
                                                // times when loading config info so now its opened only once and class
                                                // remembers it
            MyLogger.logBeforeLoading("Config file opened successfully: " + configFile, Constants.INFO_FOR_LOGGING);
        } else {
            MyLogger.logBeforeLoading("Failed to open config file: " + configFile + ", exiting.",
                    Constants.FATAL_FOR_LOGGING);
            return;
        }

        // load car following model
        ICarFollowingModel carFollowingModel = ConfigLoader.loadCarFollowingModel();
        if (carFollowingModel == null) {
            MyLogger.logBeforeLoading("Failed to load car following model, exiting."
                    , Constants.FATAL_FOR_LOGGING);
            return;
        } else {
            MyLogger.logBeforeLoading("Loaded car following model: " + carFollowingModel.getID(),
                    Constants.INFO_FOR_LOGGING);
        }
        AppContext.CAR_FOLLOWING_MODEL = carFollowingModel; // store model in app context for later use

        // load lane changing model
        ILaneChangingModel laneChangingModel = ConfigLoader.loadLaneChangingModel();
        if (laneChangingModel == null) {
            MyLogger.logBeforeLoading("Failed to load lane changing model, exiting.", Constants.FATAL_FOR_LOGGING);
            return;
        } else {
            MyLogger.logBeforeLoading("Loaded lane changing model: " + laneChangingModel.getID(),
                    Constants.INFO_FOR_LOGGING);
        }
        AppContext.LANE_CHANGING_MODEL = laneChangingModel; // store model in app context for later use

        // load car generator, thing that decides when cars are generated and what params do they have
        CarGenerator carGenerator = ConfigLoader.loadCarGenerator();
        if (carGenerator == null) {
            MyLogger.logBeforeLoading("Failed to load car generator, exiting.", Constants.FATAL_FOR_LOGGING);
            return;
        } else {
            MyLogger.logBeforeLoading("Loaded car generator: " + carGenerator, Constants.INFO_FOR_LOGGING);
        }

        String requestedParams = StringEditor.mergeRequestParameters(carFollowingModel.getParametersForGeneration(),
                laneChangingModel.getParametersForGeneration());
        carGenerator.setCarGenerationParameters(requestedParams);

        // check if car generator has all parameters needed for the selected car following model, for example person
        // loads car following model which need max speed, the model needs those parameters to work
        // so generator gives needs to generate cars with max speed parameter, otherwise model won't work properly, lol
        if (carGenerator.checkIfAllParametersAreLoaded()) {
            MyLogger.logBeforeLoading("Car generator parameters are valid for the selected car following model.",
                    Constants.INFO_FOR_LOGGING);
        } else { // missing some parameters, exit
            MyLogger.logBeforeLoading("Car generator parameters are NOT valid for the selected " +
                            "car following model, exiting.", Constants.FATAL_FOR_LOGGING);
            return;
        }

        // load roads from config
        Road[] roads = ConfigLoader.loadRoads();

        if (roads == null) {
            MyLogger.logBeforeLoading("Failed to load road configuration, exiting."
                    , Constants.FATAL_FOR_LOGGING);
            return;
        } else {
            MyLogger.logBeforeLoading("Loaded road: " + roads[0].toString() + ", number of roads: "
                            + roads.length, Constants.INFO_FOR_LOGGING);
        }

        // check if type of road matches type of car following model
        if (!roads[0].getType().equals(carFollowingModel.getType())) {
            MyLogger.logBeforeLoading("Types of car following model and road do not match: model type=" +
                    carFollowingModel.getType() + ", road type=" + roads[0].getType(), Constants.FATAL_FOR_LOGGING);
            return;
        } else {
            MyLogger.logBeforeLoading("Types of car following model and road match: " + roads[0].getType(),
                    Constants.INFO_FOR_LOGGING);
        }

        // set type of road for generator and store roads in road instance
        carGenerator.setType(roads[0]);
        for (Road road : roads) {
            road.setCarGenerator(carGenerator);

            if (carGenerator.generatingToQueue()) {
                MyLogger.logBeforeLoading("Car generator is generating cars to queue."
                        , Constants.INFO_FOR_LOGGING);
                road.initializeCarQueues();
            } else {
                MyLogger.logBeforeLoading("Car generator is generating cars directly on road."
                        , Constants.INFO_FOR_LOGGING);
            }
        }

        IRoadRenderer renderer; // renderer for drawing roads in gui, depends on road type, because different road types
                                // have different content
        if (roads[0].getType().equals(Constants.CELLULAR)) {
            renderer = new CellularRoadRenderer();
        } else if (roads[0].getType().equals(Constants.CONTINOUS)) {
            renderer = new ContinuousRoadRenderer();
        } else {
            MyLogger.logBeforeLoading("Unknown road type: " + roads[0].getType(), Constants.FATAL_FOR_LOGGING);
            return;
        }
        AppContext.RENDERER = renderer;

        RunDetails runDetails = ConfigLoader.loadRunDetails(); // loading run details (show gui, duration...)
        if (runDetails == null) {
            MyLogger.logBeforeLoading("Failed to load run details, exiting.", Constants.FATAL_FOR_LOGGING);
            return;
        } else {
            MyLogger.logBeforeLoading("Loaded run details: duration=" + runDetails.duration + ", timeStep=" +
                    runDetails.timeStep + ", showGui=" + runDetails.showGui + ", outputFile=" + runDetails.outputFile +
                    ", drawCells=" + runDetails.drawCells, Constants.INFO_FOR_LOGGING);
        }
        AppContext.RUN_DETAILS = runDetails;

        ResultsRecorder.getResultsRecorder().initialize(roads.length, runDetails.outputFile);

        // create simulation and store it in app context, simulation is the thing that updates all roads and cars
        Simulation sim = new Simulation(roads);
        AppContext.SIMULATION = sim;


        if (runDetails.showGui) {
            MyLogger.logBeforeLoading("GUI enabled, starting GUI.", Constants.INFO_FOR_LOGGING);
            Window.main(args); // start gui
        } else { // if no gui, run simulation in console mode
            MyLogger.logBeforeLoading("Starting simulation in console mode.", Constants.INFO_FOR_LOGGING);
            sim.runSimulation(runDetails.duration);
            MyLogger.logBeforeLoading("Simulation finished, exiting.", Constants.INFO_FOR_LOGGING);
            if (runDetails.writingResults()) {
                ResultsRecorder.getResultsRecorder().writeResults();
            }
        }
    }
}