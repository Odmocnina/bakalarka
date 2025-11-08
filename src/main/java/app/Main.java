package app;

import core.model.CarGenerator;
import core.model.Road;
import core.sim.Simulation;
import core.utils.*;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import ui.Window;
import ui.render.CellularRoadRenderer;
import ui.render.ContinousRoadRenderer;
import ui.render.IRoadRenderer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/*****************************
 * Main application class, starts application and loads data from configuration
 *
 * @author Michael Hladky
 * @version 1.0
 *********************************/
public class Main {

    /** logger for logging process of program in main class **/
    private static final Logger logger = LogManager.getLogger(Main.class);

    /*
     * main function of application, loads configuration and starts gui if gui is supposed to be used, returns nothing
     *
     * @param args command line arguments, first argument is path to configuration file, if not provided default \
     *              config file is used
     */
    public static void main(String[] args) {
        String configFile;
        if (args.length == 0) { // use default config file if none provided
            logger.warn("No config file provided, using default: " + Constants.CONFIG_FILE);
            MyLogger.log("No config file provided, using default: " + Constants.CONFIG_FILE,
                    Constants.WARN_FOR_LOGGING);
            configFile = Constants.CONFIG_FILE;
        } else {
            configFile = args[0];
        }

        if (ConfigLoader.giveConfigFile(configFile)) { // wierd thing that I thought of, config file was opened multiple
                                                // times when loading config info so now its opened only once and class
                                                // remembers it
            logger.info("Config file opened successfully: " + configFile);
        } else {
            logger.fatal("Failed to open config file: " + configFile + ", exiting.");
            return;
        }

        // load car following model
        ICarFollowingModel carFollowingModel = ConfigLoader.loadCarFollowingModel();
        if (carFollowingModel == null) {
            logger.fatal("Failed to load car following model, exiting.");
            return;
        } else {
            logger.info("Loaded car following model: " + carFollowingModel.getID());
        }
        AppContext.CAR_FOLLOWING_MODEL = carFollowingModel; // store model in app context for later use

        // load lane changing model
        ILaneChangingModel laneChangingModel = ConfigLoader.loadLaneChangingModel();
        if (laneChangingModel == null) {
            logger.fatal("Failed to load lane changing model, exiting.");
            return;
        } else {
            logger.info("Loaded lane changing model: " + laneChangingModel.getID());
        }
        AppContext.LANE_CHANGING_MODEL = laneChangingModel; // store model in app context for later use

        // load car generator, thing that decides when cars are generated and what params do they have
        CarGenerator carGenerator = ConfigLoader.loadCarGenerator();
        if (carGenerator == null) {
            logger.fatal("Failed to load car generator, exiting.");
            return;
        } else {
            logger.info("Loaded car generator: " + carGenerator);
        }

        String requestedParams = StringEditor.mergeRequestParameters(carFollowingModel.getParametersForGeneration(),
                laneChangingModel.getParametersForGeneration());
        carGenerator.setCarGenerationParameters(requestedParams);

        // check if car generator has all parameters needed for the selected car following model, for example person
        // loads car following model which need max speed, the model needs those parameters to work
        // so generator gives needs to generate cars with max speed parameter, otherwise model won't work properly, lol
        if (carGenerator.checkIfAllParametresAreLoaded()) {
            logger.info("Car generator parameters are valid for the selected car following model.");
        } else { // missing some parameters, exit
            logger.fatal("Car generator parameters are NOT valid for the selected car following model, exiting.");
            return;
        }

        // load roads from config
        Road[] roads = ConfigLoader.loadRoads();

        if (roads == null) {
            logger.fatal("Failed to load road configuration, exiting.");
            return;
        } else {
            logger.info("Loaded road: " + roads[0].toString() + ", number of roads: " + roads.length);
        }

        // check if type of road matches type of car following model
        if (!roads[0].getType().equals(carFollowingModel.getType())) {
            logger.fatal("Types of car following model and road do not match, exiting.");
            return;
        } else {
            logger.info("Types of car following model and road match: " + roads[0].getType());
        }

        // set type of road for generator and store roads in road instance
        carGenerator.setType(roads[0].getType());
        for (Road road : roads) {
            road.setCarGenerator(carGenerator);

            if (carGenerator.generatingToQueue()) {
                logger.info("Car generator is generating cars to queue.");
                road.initializeCarQueues();
            } else {
                logger.info("Car generator is generating cars directly on road.");
            }
        }

        IRoadRenderer renderer; // renderer for drawing roads in gui, depends on road type, because different road types
                                // have different content
        if (roads[0].getType().equals(Constants.CELLULAR)) {
            renderer = new CellularRoadRenderer();
        } else if (roads[0].getType().equals(Constants.CONTINOUS)) {
            renderer = new ContinousRoadRenderer();
        } else {
            logger.fatal("Unknown road type: " + roads[0].getType() + ", exiting.");
            return;
        }
        AppContext.RENDERER = renderer;

        RunDetails runDetails = ConfigLoader.loadRunDetails(); // loading run details (show gui, duration...)
        if (runDetails == null) {
            logger.fatal("Failed to load run details, exiting.");
            return;
        } else {
            logger.info("Loaded run details: duration=" + runDetails.duration + ", timeStep=" + runDetails.timeStep +
                    ", showGui=" + runDetails.showGui + ", outputFile=" + runDetails.outputFile +
                    ", drawCells=" + runDetails.drawCells);
        }
        AppContext.RUN_DETAILS = runDetails;

        ResultsRecorder.getResultsRecorder().initialize(roads.length, runDetails.outputFile);

        // create simulation and store it in app context, simulation is the thing that updates all roads and cars
        Simulation sim = new Simulation(roads);
        AppContext.SIMULATION = sim;


        if (runDetails.showGui) {
            logger.info("GUI enabled, starting GUI.");
            Window.main(args); // start gui
        } else { // if no gui, run simulation in console mode
            logger.info("GUI disabled, running simulation in console mode.");
            sim.runSimulation(runDetails.duration, runDetails.timeStep);
            logger.info("Simulation finished, exiting.");
            if (runDetails.writingResults()) {
                ResultsRecorder.getResultsRecorder().writeResults();
            }
        }
    }
}