package ui;

import app.AppContext;
import core.utils.DefaultStuffMaker;
import core.utils.MyLogger;
import core.utils.RoadParameters;
import core.utils.RoadXml;
import core.utils.constants.Constants;
import core.utils.loading.RoadLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;

public class NewMapDialogMaker extends DialogMaker {

    /**
     * show dialog to create a new map
     *
     * @param primaryStage owner stage
     **/
    public static void newMapDialog(Stage primaryStage, Runnable paintAll) {
        // Create main dialog window
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create new map");
        dialog.setHeaderText("New map creation");
        dialog.initOwner(primaryStage);
        dialog.setWidth(420);

        ArrayList<RoadParameters> roadParameters = new ArrayList<>();

        // Buttons (Create / Cancel)
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Spinner for number of roads
        Spinner<Integer> roadsSpinner = new Spinner<>(1, 20000, 1);

        // Main layout panel
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        // File name field for map file
        grid.add(new Label("Map file:"), 0, 0);
        TextField mapFileNameField = new TextField("map.xml");
        grid.add(mapFileNameField, 1, 0);

        grid.add(new Label("Number of roads:"), 0, 1);
        grid.add(roadsSpinner, 1, 1);

        Button changeAllRoadsButton = new Button("Change all roads...");
        Button changeOneRoadButton = new Button("Change one road...");
        grid.add(changeAllRoadsButton, 0, 2);
        grid.add(changeOneRoadButton, 1, 2);

        changeAllRoadsButton.setOnAction(e ->
                changeRoadsDialog(primaryStage, roadParameters, roadsSpinner.getValue(), true, -1));

        changeOneRoadButton.setOnAction(e -> {
            // open dialog to select and modify one road
            selectRoadDialog(primaryStage, roadParameters, roadsSpinner.getValue());

            // when dialog is closed, look if road was deleted and update spinner if needed
            if (roadParameters.size() != roadsSpinner.getValue()) {
                roadsSpinner.getValueFactory().setValue(roadParameters.size());
            }
        });

        int numberOfRoads = roadsSpinner.getValue();

        for (int i = 0; i < numberOfRoads; i++) {
            addRoadParameters(Integer.parseInt(STOCK_NUMBER_OF_LANES), Double.parseDouble(STOCK_MAX_SPEED),
                    Double.parseDouble(STOCK_LENGTH), DefaultStuffMaker.createDefaultLightPlan(Integer.parseInt(STOCK_NUMBER_OF_LANES)),
                    DefaultStuffMaker.createDefaultGenerator(Integer.parseInt(STOCK_NUMBER_OF_LANES)), roadParameters);
        }

        // if number of roads changes, update the lists
        roadsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            int newNumberOfRoads = newVal;
            if (newNumberOfRoads > roadParameters.size()) {
                for (int i = roadParameters.size(); i < newNumberOfRoads; i++) {
                    addRoadParameters(Integer.parseInt(STOCK_NUMBER_OF_LANES),
                            Double.parseDouble(STOCK_MAX_SPEED), Double.parseDouble(STOCK_LENGTH),
                            DefaultStuffMaker.createDefaultLightPlan(Integer.parseInt(STOCK_NUMBER_OF_LANES)),
                            DefaultStuffMaker.createDefaultGenerator(Integer.parseInt(STOCK_NUMBER_OF_LANES)), roadParameters);
                }
            } else if (newNumberOfRoads < roadParameters.size()) {
                roadParameters.subList(newNumberOfRoads, roadParameters.size()).clear();
            }
        });

        dialog.getDialogPane().setContent(grid);

        // Handle Create button click
        dialog.showAndWait().ifPresent(result -> {
            if (result == createButtonType) {
                String mapFileName = mapFileNameField.getText();
                AppContext.RUN_DETAILS.mapFile = mapFileName;
                boolean success = RoadXml.writeMapToXml(roadParameters, roadParameters.size(), mapFileName);
                MyLogger.log("Creating new map: " + mapFileName, Constants.INFO_FOR_LOGGING);
                if (success) {
                    MyLogger.log("New map created successfully: " + mapFileName, Constants.INFO_FOR_LOGGING);
                    openNewRoadDialog(primaryStage, mapFileName, paintAll);
                } else {
                    MyLogger.log("Failed to create new map: " + mapFileName, Constants.ERROR_FOR_LOGGING);
                }
            }
        });
    }

    private static void openNewRoadDialog(Stage primaryStage, String mapFileName, Runnable paintAll) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Open new map");
        dialog.setHeaderText("Do you wish to open the new map?");
        dialog.initOwner(primaryStage);
        dialog.setWidth(420);

        ButtonType openButtonType = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(openButtonType, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(result -> {
            if (result == openButtonType) {
                MyLogger.log("Opening new map.", Constants.INFO_FOR_LOGGING);
                boolean ok = RoadLoader.loadMap(mapFileName);
                if (ok) {
                    MyLogger.log("New map opened successfully.", Constants.INFO_FOR_LOGGING);
                    paintAll.run();
                } else {
                    MyLogger.log("Failed to open new map.", Constants.ERROR_FOR_LOGGING);
                }
            }
        });

        ButtonType cancelButtonType = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(cancelButtonType);
    }
}
