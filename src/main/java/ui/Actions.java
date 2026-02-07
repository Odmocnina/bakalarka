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

public class Actions {

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

    public static void collisionBanAction(Runnable paintAll) {
        ConfigModificator.changePreventCollision();
        MyLogger.log("Toggled collision ban", Constants.INFO_FOR_LOGGING);
        paintAll.run();
    }

    public static void saveMapAsAction(Stage primaryStage) {
        MyLogger.log("Saving map file as...", Constants.INFO_FOR_LOGGING);
        DialogMaker.saveAsDialog(primaryStage);
    }

    public static void saveMapAction() {
        RoadXml.saveCurrentMap();
        MyLogger.log("Saving map file...", Constants.INFO_FOR_LOGGING);
    }

    public static void changeLaneChangingAction(Runnable paintAll) {
        ConfigModificator.changeLaneChangeBan();
        MyLogger.log("Toggled lane change ban", Constants.INFO_FOR_LOGGING);
        paintAll.run();
    }

    public static void newMapAction(Stage primaryStage, Runnable paintAll) {
        MyLogger.log("Creating new map pressed...", Constants.INFO_FOR_LOGGING);
        NewMapDialogMaker.newMapDialog(primaryStage, paintAll);
    }

    public static void editMapFile(Stage primaryStage, Runnable paintAll) {
        MyLogger.log("Modifying map file...", Constants.INFO_FOR_LOGGING);
        ModifyMapDialogMaker.modifyMapDialog(primaryStage,
                RoadParameters.existingRoadsToRoadParameters(AppContext.SIMULATION.getRoads()), paintAll);
    }

    public static void exportResultsToTxtAction() {
        MyLogger.log("Exporting results to txt...", Constants.INFO_FOR_LOGGING);
        ResultsRecorder.getResultsRecorder().writeResultsTxt();
        MyLogger.log("Results exporting to txt finished.", Constants.INFO_FOR_LOGGING);
    }

    public static void exportResultsToCsvAction() {
        MyLogger.log("Exporting results to csv...", Constants.INFO_FOR_LOGGING);
        ResultsRecorder.getResultsRecorder().writeResultsCsv();
        MyLogger.log("Results exporting to csv finished.", Constants.INFO_FOR_LOGGING);
    }

    public static void setLoggingAction(int logIndex) {
        MyLogger.log("Toggling logging index " + logIndex, Constants.INFO_FOR_LOGGING);
        ConfigModificator.changeLogging(logIndex);
    }

    public static void setTimeBetweenStepsAction(Stage stage, CoreEngine engine) {
        MyLogger.log("Set time between steps was pressed", Constants.INFO_FOR_LOGGING);
        DialogMaker.changeTimeBetweenSteps(stage, engine);
    }

    public static void nextStepAction(Simulation simulation, Runnable paintAll) {
        MyLogger.log("Next step action triggered", Constants.INFO_FOR_LOGGING);
        simulation.step();
        paintAll.run();
    }

    public static void setOutputFileAction() {
        MyLogger.log("Set output file action triggered", Constants.INFO_FOR_LOGGING);
        DialogMaker.setOutFileDialog();
    }

    public static void setCsvSeparatorAction(Stage primaryStage) {
        MyLogger.log("Set CSV separator action triggered", Constants.INFO_FOR_LOGGING);
        DialogMaker.setCsvSeparatorDialog(primaryStage);
    }
}
