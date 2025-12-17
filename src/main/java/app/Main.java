package app;

import core.utils.*;
import core.utils.constants.Constants;
import core.utils.loading.ConfigLoader;
import ui.Window;

/*****************************
 * Main application class, starts application and loads data from configuration
 *
 * @author Michael Hladky
 * @version 1.0
 *********************************/
public class Main {

    /**
     * main function of application, loads configuration and starts gui if gui is supposed to be used, returns nothing
     *
     * @param args command line arguments, first argument is path to configuration file, if not provided default config
     *             file is used
     **/
    public static void main(String[] args) {
        // load run details from configuration file
        boolean success = ConfigLoader.loadAllConfig(args);

        if (!success) {
            MyLogger.logBeforeLoading("Failed to load configuration, exiting.", Constants.ERROR_FOR_LOGGING);
            return;
        }

        if (AppContext.RUN_DETAILS.showGui) {
            MyLogger.logBeforeLoading("GUI enabled, starting GUI.", Constants.INFO_FOR_LOGGING);
            Window.main(args); // start gui
        } else { // if no gui, run simulation in console mode
            MyLogger.logBeforeLoading("Starting simulation in console mode.", Constants.INFO_FOR_LOGGING);
            AppContext.SIMULATION.runSimulation(AppContext.RUN_DETAILS.duration);
            MyLogger.logBeforeLoading("Simulation finished, exiting.", Constants.INFO_FOR_LOGGING);
            if (AppContext.RUN_DETAILS.writingResults()) {
                ResultsRecorder.getResultsRecorder().writeResults();
            }
        }
    }
}