package ui;

import app.AppContext;
import core.engine.CoreEngine;
import core.sim.Simulation;
import core.utils.*;
import core.utils.constants.Constants;
import core.utils.loading.RoadLoader;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

/**********************************************
 * Class responsible for having functions/method when button/another action is taken in gui
 *
 * @author Michael Hladky
 * @version 1.0.0
 **********************************************/
public class Actions {

    /**
     * method for opening new map file, with check for unsaved changes, and then loading the map file and repainting
     * the map
     *
     * @param primaryStage the primary stage of the application, used for showing file chooser and dialogs
     * @param paintAll the function to repaint the map after loading the new map file
     **/
    public static void openMapAction(Stage primaryStage, Runnable paintAll) {
        boolean continueWithOpening = true;
        if (AppContext.RUN_DETAILS.mapChanged) {
            continueWithOpening = DialogMaker.onCloseUnsavedChangesDialog(primaryStage);
        }

        if (!continueWithOpening) {
            MyLogger.log("Map file opening cancelled due to unsaved changes.", Constants.INFO_FOR_LOGGING);
            return;
        }

        MyLogger.log("Opening map file...", Constants.INFO_FOR_LOGGING);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose map file to open");

        File currentDir = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(currentDir);

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Map files", "*.xml")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            boolean success = RoadLoader.loadMap(selectedFile.getAbsolutePath());
            if (success) {
                AppContext.RUN_DETAILS.setNewMapFile(selectedFile.getAbsolutePath());
                MyLogger.log("Map file loaded successfully: " + selectedFile.getAbsolutePath(),
                        Constants.INFO_FOR_LOGGING);
                paintAll.run();
            } else {
                MyLogger.log("Failed to load map file: " + selectedFile.getAbsolutePath(),
                        Constants.ERROR_FOR_LOGGING);
            }
        } else {
            MyLogger.log("Map file opening cancelled.", Constants.INFO_FOR_LOGGING);
        }
    }

    /**
     * method for toggling collision ban, which changes the configuration and then repaints the map to reflect the
     * change
     *
     * @param paintAll the function to repaint the map after toggling the collision ban
     **/
    public static void collisionBanAction(Runnable paintAll) {
        ConfigModification.changePreventCollision();
        MyLogger.log("Toggled collision ban", Constants.INFO_FOR_LOGGING);
        paintAll.run();
    }

    /**
     * method for saving the current map file, which saves the map file and then logs the action
     *
     * @param primaryStage the primary stage of the application, used for showing file chooser and dialogs
     **/
    public static void saveMapAsAction(Stage primaryStage) {
        MyLogger.log("Saving map file as...", Constants.INFO_FOR_LOGGING);
        DialogMaker.saveAsDialog(primaryStage);
    }

    /**
     * method for saving the current map file, which saves the map file and then logs the action
     **/
    public static void saveMapAction() {
        RoadXml.saveCurrentMap();
        MyLogger.log("Saving map file...", Constants.INFO_FOR_LOGGING);
    }

    /**
     * method for toggling lane change ban, which changes the configuration and then repaints the map to reflect the
     * change
     *
     * @param paintAll the function to repaint the map after toggling the lane change ban
     **/
    public static void changeLaneChangingAction(Runnable paintAll) {
        ConfigModification.changeLaneChangeBan();
        MyLogger.log("Toggled lane change ban", Constants.INFO_FOR_LOGGING);
        paintAll.run();
    }

    /**
     * method for creating new map, which opens the new map dialog and then repaints the map to reflect the change
     *
     * @param primaryStage the primary stage of the application, used for showing file chooser and dialogs
     * @param paintAll the function to repaint the map after creating the new map
     **/
    public static void newMapAction(Stage primaryStage, Runnable paintAll) {
        MyLogger.log("Creating new map pressed...", Constants.INFO_FOR_LOGGING);
        NewMapDialogMaker.newMapDialog(primaryStage, paintAll);
    }

    /**
     * method for editing the map file, which opens the edit map dialog and then repaints the map to reflect the change
     *
     * @param primaryStage the primary stage of the application, used for showing file chooser and dialogs
     * @param paintAll the function to repaint the map after editing the map file
     **/
    public static void editMapFile(Stage primaryStage, Runnable paintAll) {
        MyLogger.log("Modifying map file...", Constants.INFO_FOR_LOGGING);
        ModifyMapDialogMaker.modifyMapDialog(primaryStage,
                RoadParameters.existingRoadsToRoadParameters(AppContext.SIMULATION.getRoads()), paintAll);
    }

    /**
     * method for exporting the results to txt file, which writes the results to txt file and then logs the action
     **/
    public static void exportResultsToTxtAction() {
        MyLogger.log("Exporting results to txt...", Constants.INFO_FOR_LOGGING);
        ResultsRecorder.getResultsRecorder().writeResultsTxt();
        MyLogger.log("Results exporting to txt finished.", Constants.INFO_FOR_LOGGING);
    }

    /**
     * method for exporting the results to csv file, which writes the results to csv file and then logs the action
     **/
    public static void exportResultsToCsvAction() {
        MyLogger.log("Exporting results to csv...", Constants.INFO_FOR_LOGGING);
        ResultsRecorder.getResultsRecorder().writeResultsCsv();
        MyLogger.log("Results exporting to csv finished.", Constants.INFO_FOR_LOGGING);
    }

    /**
     * method for toggling the logging of a specific index, which changes the configuration and then logs the action
     *
     * @param logIndex the index of the logging to be toggled
     **/
    public static void setLoggingAction(int logIndex) {
        MyLogger.log("Toggling logging index " + logIndex, Constants.INFO_FOR_LOGGING);
        ConfigModification.changeLogging(logIndex);
    }

    /**
     * method for setting the time between steps, which opens the dialog for setting the time between steps and then
     * logs the action
     *
     * @param stage the stage of the application, used for showing dialogs
     * @param engine the core engine of the simulation, used for getting and setting the time between steps
     **/
    public static void setTimeBetweenStepsAction(Stage stage, CoreEngine engine) {
        MyLogger.log("Set time between steps was pressed", Constants.INFO_FOR_LOGGING);
        DialogMaker.changeTimeBetweenSteps(stage, engine);
    }

    /**
     * method for performing the next step of the simulation, which steps the simulation and then repaints the map to
     * reflect the change
     *
     * @param simulation the simulation to be stepped
     * @param paintAll the function to repaint the map after stepping the simulation
     **/
    public static void nextStepAction(Simulation simulation, Runnable paintAll) {
        MyLogger.log("Next step action triggered", Constants.INFO_FOR_LOGGING);
        simulation.step();
        paintAll.run();
    }

    /**
     * method for setting the output file, which opens the dialog for setting the output file and then logs the action
     **/
    public static void setOutputFileAction() {
        MyLogger.log("Set output file action triggered", Constants.INFO_FOR_LOGGING);
        DialogMaker.setOutFileDialog();
    }

    /**
     * method for setting the csv separator, which opens the dialog for setting the csv separator and then logs the action
     *
     * @param primaryStage the primary stage of the application, used for showing dialogs
     **/
    public static void setCsvSeparatorAction(Stage primaryStage) {
        MyLogger.log("Set CSV separator action triggered", Constants.INFO_FOR_LOGGING);
        DialogMaker.setCsvSeparatorDialog(primaryStage);
    }

    /**
     * method for toggling the output of a specific detail, which changes the configuration and then logs the action
     *
     * @param detailIndex the index of the output detail to be toggled
     **/
    public static void setOutputAction(int detailIndex) {
        MyLogger.log("Toggling output detail index " + detailIndex, Constants.INFO_FOR_LOGGING);
        ConfigModification.changeOutput(detailIndex);
    }
}
