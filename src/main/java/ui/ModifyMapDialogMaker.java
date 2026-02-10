package ui;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.model.Road;
import core.utils.DefaultStuffMaker;
import core.utils.MyLogger;
import core.utils.RoadParameters;
import core.utils.constants.Constants;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.LinkedList;

/***************************************
 * Class with functions/methods connected to modification of the map, like changing properties of roads, adding new
 * road, etc.
 *
 * @author Michael Hladky
 * @version 1.0.0
 ***************************************/
public class ModifyMapDialogMaker extends DialogMaker {

    /**
     * show dialog to modify the map, with options to change properties of all roads, one road, or add a new road
     *
     * @param primaryStage owner stage
     * @param roadParameters list of road parameters to be modified and then converted to roads for the simulation
     * @param paintAll function to repaint the map after modification
     **/
    public static void modifyMapDialog(Stage primaryStage, ArrayList<RoadParameters> roadParameters, Runnable paintAll) {
        // Create main dialog window
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modify map");
        dialog.setHeaderText("Map modification");
        dialog.initOwner(primaryStage);
        dialog.setWidth(420);

        // Buttons (Create / Cancel)
        ButtonType createButtonType = new ButtonType("Modify", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Main layout panel
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        Button changeAllRoadsButton = new Button("Change all roads...");
        Button changeOneRoadButton = new Button("Change one road...");
        Button addRoadButton = new Button("Add road...");
        grid.add(changeAllRoadsButton, 0, 1);
        grid.add(changeOneRoadButton, 1, 1);
        grid.add(addRoadButton, 2, 1);

        changeAllRoadsButton.setOnAction(e ->
                changeRoadsDialog(primaryStage, roadParameters, roadParameters.size(), true, -1));

        changeOneRoadButton.setOnAction(e -> {
            // open dialog to select and modify one road
            selectRoadDialog(primaryStage, roadParameters, roadParameters.size());
        });

        addRoadButton.setOnAction(e -> addRoadDialog(primaryStage, roadParameters, paintAll));

        dialog.getDialogPane().setContent(grid);

        // Handle Create button click
        dialog.showAndWait().ifPresent(result -> {
            if (result == createButtonType) {
                Road[] roads = RoadParameters.roadParametersToRoads(roadParameters);
                for (Road road : roads) {
                    road.setUpQueuesIfNeeded();
                }
                AppContext.SIMULATION.resetSimulationWithNewRoads(roads);
                MyLogger.log("Modified map with " + roads.length + " roads.", Constants.INFO_FOR_LOGGING);
                paintAll.run();
            }
        });
    }

    /**
     * show dialog to add a new road, with options to set properties of the new road, and then add it to the list of
     * road parameters
     *
     * @param stage owner stage
     * @param roadParameters list of road parameters to which the new road parameters will be added if the dialog is
     *                       confirmed
     * @param paintAll function to repaint the map after adding the new road
     **/
    private static void addRoadDialog(Stage stage, ArrayList<RoadParameters> roadParameters, Runnable paintAll) {
        Dialog<ButtonType> dialog = new Dialog<>();
        String speed;
        String length;
        int lanes;
        LinkedList<LightPlan> lightPlan;
        LinkedList<CarGenerator> generators;
        dialog.setTitle("Add new road");
        dialog.setHeaderText("Modify new road properties");
        speed = STOCK_MAX_SPEED;
        length = STOCK_LENGTH;
        lanes = Integer.parseInt(STOCK_NUMBER_OF_LANES);
        lightPlan = new LinkedList<>();
        for (int i = 0; i < lanes; i++) {
            lightPlan.add(DefaultStuffMaker.createDefaultLightPlan());
        }
        generators = new LinkedList<>();
        for (int i = 0; i < lanes; i++) {
            generators.add(DefaultStuffMaker.createDefaultGenerator());
        }
        dialog.initOwner(stage);

        ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField lengthField = new TextField(length);
        TextField speedLimitField = new TextField(speed);
        Spinner<Integer> lanesSpinner = new Spinner<>(1, 10, lanes);
        Spinner<Integer> laneIndexSpinner = new Spinner<>(1, lanes, 1);

        grid.add(new Label("Length (m):"), 0, 0);
        grid.add(lengthField, 1, 0);

        grid.add(new Label("Speed limit (km/h):"), 0, 1);
        grid.add(speedLimitField, 1, 1);

        grid.add(new Label("Number of lanes:"), 0, 2);
        grid.add(lanesSpinner, 1, 2);

        grid.add(new Label("Add road to position:"), 0, 3);
        Spinner<Integer> positionSpinner = new Spinner<>(1, roadParameters.size() + 1, 1, 1);
        grid.add(positionSpinner, 1, 3);

        // listener to update lane index max value when number of lanes changes
        lanesSpinner.valueProperty().addListener((obs, oldVal, newVal) ->
                //laneIndexSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, newVal, 1)));
                updateNumberOfLanes(lightPlan, generators, newVal, laneIndexSpinner));

        grid.add(new Label("Edit light plan or generator on lane:"), 0, 4);
        grid.add(laneIndexSpinner, 1, 4);

        Button lightPlanButton = new Button("Edit one light plan...");
        grid.add(lightPlanButton, 1, 5);

        Button generatorButton = new Button("Edit one car generator...");
        grid.add(generatorButton, 1, 6);

        Button editAllLightPlansButton = new Button("Edit all light plans...");
        grid.add(editAllLightPlansButton, 0, 5);

        Button editAllGeneratorsButton = new Button("Edit all car generators...");
        grid.add(editAllGeneratorsButton, 0, 6);

        lightPlanButton.setOnAction(e -> {
            int listIndex = laneIndexSpinner.getValue() - 1;
            LightPlan clone = lightPlan.get(listIndex).clone();
            if (editLightPlanDialog(stage, clone, laneIndexSpinner.getValue())) {
                // replace light plan with edited clone
                lightPlan.set(listIndex, clone);
            }
        });

        generatorButton.setOnAction(e ->
                editGeneratorDialog(stage, generators.get(laneIndexSpinner.getValue() - 1), laneIndexSpinner.getValue()));

        editAllLightPlansButton.setOnAction(e -> {
            LightPlan lp = DefaultStuffMaker.createDefaultLightPlan();
            editLightPlanDialog(stage, lp, 0);
            //for (int i = 0; i < lightPlan.size(); i++) {lightPlan.set(i, lp.clone());}
            lightPlan.replaceAll(ignored -> lp.clone());
        });

        editAllGeneratorsButton.setOnAction(e -> {
            CarGenerator cg = DefaultStuffMaker.createDefaultGenerator();
            editGeneratorDialog(stage, cg, 0);
            //for (int i = 0; i < generators.size(); i++) { generators.set(i, cg.clone()); }
            generators.replaceAll(ignored -> cg.clone());
        });

        dialog.getDialogPane().setContent(grid);

        // show the dialog and wait for user response
        dialog.showAndWait().ifPresent(response -> {
            if (response == applyButtonType) {
                if (checkRoadInputs(lanes, lengthField.getText(), speedLimitField.getText(), generators, lightPlan)) {
                    RoadParameters rp = new RoadParameters();
                    rp.length = Double.parseDouble(lengthField.getText());
                    rp.maxSpeed = Double.parseDouble(speedLimitField.getText());
                    rp.lanes = lanes;
                    rp.lightPlan = lightPlan;
                    rp.carGenerators = generators;
                    roadParameters.add(positionSpinner.getValue() - 1, rp);
                    MyLogger.log("Added new road.", Constants.INFO_FOR_LOGGING);
                    AppContext.RUN_DETAILS.mapChanged = true;
                    paintAll.run();
                }
            } else {
                MyLogger.log("Dialog cancelled.", Constants.INFO_FOR_LOGGING);
            }
        });
    }
}
