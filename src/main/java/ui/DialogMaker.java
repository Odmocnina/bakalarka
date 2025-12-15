package ui;

import core.model.LightPlan;
import core.utils.MyLogger;
import core.utils.RoadParameters;
import core.utils.RoadXml;
import core.utils.constants.Constants;
import core.utils.constants.DefaultValues;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.ArrayList;

public class DialogMaker {

    final static String STOCK_NUMBER_OF_LANES = String.valueOf(DefaultValues.DEFAULT_ROAD_LANES);
    final static String STOCK_MAX_SPEED = String.valueOf(DefaultValues.DEFAULT_ROAD_MAX_SPEED);
    final static String STOCK_LENGTH = String.valueOf(DefaultValues.DEFAULT_ROAD_LENGTH);

    private static void editLightPlanDialog(Stage stage, LightPlan lightPlan) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit light plan");
        dialog.setHeaderText("Edit the light plan for the road(s)");
        dialog.initOwner(stage);

        ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        int cycleTime = lightPlan.getCycleTime();
        int switchTime = lightPlan.getTimeOfSwitch();
        boolean beginOnGreen = lightPlan.isBeginsOnGreen();

        // cycle time
        Spinner<Integer> cycleTimeSpinner = new Spinner<>(1, 3600, cycleTime);
        grid.add(new Label("Cycle time (s):"), 0, 0);
        grid.add(cycleTimeSpinner, 1, 0);
        // green time
        Spinner<Integer> switchTimeSpinner = new Spinner<>(1, 3600, switchTime);
        grid.add(new Label("Switch time (s):"), 0, 1);
        grid.add(switchTimeSpinner, 1, 1);
        // begin on green
        CheckBox beginOnGreenBox = new CheckBox();
        beginOnGreenBox.setSelected(beginOnGreen);
        grid.add(new Label("Begin on green:"), 0, 2);
        grid.add(beginOnGreenBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // show the dialog and wait for user response
        dialog.showAndWait().ifPresent(response -> {
            if (response == applyButtonType) {
                lightPlan.setCycleTime(cycleTimeSpinner.getValue());
                lightPlan.setTimeOfSwitch(switchTimeSpinner.getValue());
                lightPlan.setBeginsOnGreen(beginOnGreenBox.isSelected());
                if (lightPlan.isLegitimate()) {
                    MyLogger.log("Light plan updated via dialog.", Constants.INFO_FOR_LOGGING);
                } else {
                    MyLogger.log("Light plan updated via dialog for all roads.", Constants.INFO_FOR_LOGGING);
                }
            } else {
                MyLogger.log("Light plan dialog cancelled.", Constants.INFO_FOR_LOGGING);
            }
        });
    }

    public static void changeRoadsDialog(Stage stage, ArrayList<RoadParameters> roadParameters, int numberOfRoads
                                         , boolean changingAll, int index) {
        Dialog<ButtonType> dialog = new Dialog<>();
        String speed;
        String length;
        int lanes;
        LightPlan[] lightPlan;
        if (!changingAll) {
            dialog.setTitle("Change selected road properties, road index: " + (index + 1));
            dialog.setHeaderText("Modify properties of the selected road: " + (index + 1));
            speed = String.valueOf(roadParameters.get(index).maxSpeed);
            length = String.valueOf(roadParameters.get(index).length);
            lanes = roadParameters.get(index).lanes;
            lightPlan = roadParameters.get(index).lightPlan;
        } else {
            dialog.setTitle("Change all roads properties");
            dialog.setHeaderText("Modify properties of the all roads");
            speed = STOCK_MAX_SPEED;
            length = STOCK_LENGTH;
            lanes = Integer.parseInt(STOCK_NUMBER_OF_LANES);
            lightPlan = new LightPlan[numberOfRoads];
            for (int i = 0; i < numberOfRoads; i++) {
                lightPlan[i] = createDefaultLightPlan();
            }
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

        // lisener to update lane index max value when number of lanes changes
        lanesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            laneIndexSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, newVal, 1));
        });

        grid.add(new Label("Edit light plan on lane:"), 0, 3);
        grid.add(laneIndexSpinner, 1, 3);

        Button lightPlanButton = new Button("Edit light plan...");
        grid.add(lightPlanButton, 1, 4);

        lightPlanButton.setOnAction(e -> {
            editLightPlanDialog(stage, lightPlan[laneIndexSpinner.getValue() - 1]);
        });

        dialog.getDialogPane().setContent(grid);

        // show the dialog and wait for user response
        dialog.showAndWait().ifPresent(response -> {
            if (response == applyButtonType) {
                if (!changingAll) {
                    MyLogger.log("Selected road properties updated via dialog.", Constants.INFO_FOR_LOGGING);
                    changeRoadParameters(index, lanesSpinner.getValue(), Double.parseDouble(speedLimitField.getText()),
                            Double.parseDouble(lengthField.getText()), lightPlan, roadParameters);
                    return;
                }
                MyLogger.log("All road properties updated via dialog.", Constants.INFO_FOR_LOGGING);
                for (int i = 0; i < numberOfRoads; i++) {
                    changeRoadParameters(i, lanesSpinner.getValue(), Double.parseDouble(speedLimitField.getText()),
                            Double.parseDouble(lengthField.getText()), lightPlan, roadParameters);
                }
            } else {
                MyLogger.log("Dialog cancelled.", Constants.INFO_FOR_LOGGING);
            }
        });
    }

    public static void selectRoadDialog(Stage stage, ArrayList<RoadParameters> roadParameters, int numberOfRoads) {
        if (numberOfRoads <= 0) {
            MyLogger.log("No roads available to select.", Constants.ERROR_FOR_LOGGING);
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select road");
        dialog.setHeaderText("Select a road by its index");
        dialog.initOwner(stage);

        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        Spinner<Integer> indexField = new Spinner<>(1, numberOfRoads, 1);

        grid.add(new Label("Road index (1 to " + (numberOfRoads) + "):"), 0, 0);
        grid.add(indexField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // show the dialog and wait for user response
        dialog.showAndWait().ifPresent(response -> {
            if (response == selectButtonType) {
                if (indexField.getValue() < 1 || indexField.getValue() > numberOfRoads) {
                    MyLogger.log("Invalid road index selected: " + indexField.getValue(),
                            Constants.ERROR_FOR_LOGGING);
                    return;
                }

                MyLogger.log("Selected road index: " + indexField.getValue(), Constants.INFO_FOR_LOGGING);
                changeRoadsDialog(stage, roadParameters, numberOfRoads, false, indexField.getValue()
                        - 1);
            } else {
                MyLogger.log("Dialog cancelled.", Constants.INFO_FOR_LOGGING);
            }
        });
    }

    public static void newMapDialog(Stage primaryStage) {
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

        changeAllRoadsButton.setOnAction(e -> {
            changeRoadsDialog(primaryStage, roadParameters, roadsSpinner.getValue(), true, -1);
        });

        changeOneRoadButton.setOnAction(e -> {
            selectRoadDialog(primaryStage, roadParameters, roadsSpinner.getValue());
        });

        int numberOfRoads = roadsSpinner.getValue();

        for (int i = 0; i < numberOfRoads; i++) {
            addRoadParameters(Integer.parseInt(STOCK_NUMBER_OF_LANES), Double.parseDouble(STOCK_MAX_SPEED),
                    Double.parseDouble(STOCK_LENGTH), createDefaultLightPlan(Integer.parseInt(STOCK_NUMBER_OF_LANES))
                    , roadParameters);
        }

        // if number of roads changes, update the lists
        roadsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            int newNumberOfRoads = newVal;
            if (newNumberOfRoads > roadParameters.size()) {
                for (int i = roadParameters.size(); i < newNumberOfRoads; i++) {
                    addRoadParameters(Integer.parseInt(STOCK_NUMBER_OF_LANES),
                            Double.parseDouble(STOCK_MAX_SPEED), Double.parseDouble(STOCK_LENGTH),
                            createDefaultLightPlan(Integer.parseInt(STOCK_NUMBER_OF_LANES)), roadParameters);
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
                RoadXml.writeMapToXml(roadParameters, roadParameters.size(), mapFileName);
                MyLogger.log("Creating new map: " + mapFileName, Constants.INFO_FOR_LOGGING);
            }
        });
    }

    public static void addRoadParameters(int numberOfLanes, double maxSpeed, double length, LightPlan[] lightPlan,
                                         ArrayList<RoadParameters> roadParameters) {
        RoadParameters rp = new RoadParameters();
        rp.lanes = numberOfLanes;
        rp.maxSpeed = maxSpeed;
        rp.length = length;
        rp.lightPlan = lightPlan;
        roadParameters.add(rp);
    }

    public static void changeRoadParameters(int index, int numberOfLanes, double maxSpeed, double length,
                                            LightPlan[] lightPlan, ArrayList<RoadParameters> roadParameters) {
        RoadParameters rp = new RoadParameters();
        rp.lanes = numberOfLanes;
        rp.maxSpeed = maxSpeed;
        rp.length = length;
        rp.lightPlan = lightPlan;
        roadParameters.set(index, rp);
    }

    private static LightPlan createDefaultLightPlan() {
        return new LightPlan(DefaultValues.DEFAULT_LIGHT_PLAN_CYCLE_DURATION,
                DefaultValues.DEFAULT_LIGHT_PLAN_GREEN_DURATION,
                DefaultValues.DEFAULT_LIGHT_PLAN_START_WITH_GREEN);
    }

    private static LightPlan[] createDefaultLightPlan(int numberOfLanes) {
        LightPlan[] lightPlan = new LightPlan[numberOfLanes];
        for (int i = 0; i < numberOfLanes; i++) {
            lightPlan[i] = createDefaultLightPlan();
        }
        return lightPlan;
    }





}
