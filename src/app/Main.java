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
     * @param args command line arguments, first argument is path to configuration file, if not provided default \
     *              config file is used
     */
    public static void main(String[] args) {
        String configFile;
        if (args.length == 0) { // use default config file if none provided
            System.out.println("No config file provided, using default: " + Constants.CONFIG_FILE);
            configFile = Constants.CONFIG_FILE;
        } else {
            configFile = args[0];
        }

        ConfigLoader.giveConfigFile(configFile); // wierd thing that I thought of, config file was opened multiple
                                                 // times when loading config info so now its opened only once and class
                                                 // remembers it

        // load car following model
        ICarFollowingModel carFollowingModel = ConfigLoader.loadCarFollowingModel(configFile);
        if (carFollowingModel == null) {
            System.out.println("Failed to load car following model, exiting.");
            return;
        } else {
            System.out.println("Loaded car following model: " + carFollowingModel.getID());
        }
        AppContext.CAR_FOLLOWING_MODEL = carFollowingModel; // store model in app context for later use

        // load lane changing model
        ILaneChangingModel laneChangingModel = ConfigLoader.loadLaneChangingModel(configFile);
        if (laneChangingModel == null) {
            System.out.println("Failed to load lane changing model, exiting.");
            return;
        } else {
            System.out.println("Loaded lane changing model: " + laneChangingModel.getID());
        }
        AppContext.LANE_CHANGING_MODEL = laneChangingModel; // store model in app context for later use

        // load car generator, thing that decides when cars are generated and what params do they have
        CarGenerator carGenerator = ConfigLoader.loadCarGenerator(configFile);
        if (carGenerator == null) {
            System.out.println("Failed to load car generator, exiting.");
            return;
        } else {
            System.out.println("Loaded car generator: " + carGenerator.toString());
        }
        AppContext.CAR_GENERATOR = carGenerator; // store car generator in app context for later use

        // check if car generator has all parameters needed for the selected car following model, for example person
        // loads car following model which need max speed, the model needs those parameters to work
        // so generator gives needs to gerenerate cars with max speed parameter, otherwise model wont work properly, lol
        if (carGenerator.checkIfAllParametresAreLoaded(carFollowingModel.requestParameters())) {
            System.out.println("Car generator parameters are valid for the selected car following model.");
        } else { // missing some parameters, exit
            System.out.println("Car generator parameters are NOT valid for the selected car following model, exiting.");
            return;
        }

        // load roads from config
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
        // set type of road for generator and store roads in app context
        AppContext.CAR_GENERATOR.setType(roads[0].getType());
        AppContext.ROADS = roads;

        IRoadRenderer renderer; // renderer for drawing roads in gui, depends on road type, because different road types
                                // have different content
        if (roads[0].getType().equals(Constants.CELLULAR)) {
            renderer = new CellularRoadRenderer();
        } else if (roads[0].getType().equals(Constants.CONTINOUS)) {
            renderer = new ContinousRoadRenderer();
        } else {
            System.out.println("Unknown road type: " + roads[0].getType());
            return;
        }
        AppContext.RENDERER = renderer;

        ResultsRecorder.getResultsRecorder().initialize(roads.length, "output/outputFile.txt");

        // create simulation and store it in app context, simulation is the thing that updates all roads and cars
        Simulation sim = new Simulation(roads);
        AppContext.SIMULATION = sim;


        Window.main(args); // start gui
    }
}