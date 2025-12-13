package ui;

import core.utils.MyLogger;
import core.utils.RoadXml;
import core.utils.constants.Constants;
import core.utils.constants.DefaultValues;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class DialogMaker {

    final static String STOCK_NUMBER_OF_LANES = String.valueOf(DefaultValues.DEFAULT_ROAD_LANES);
    final static String STOCK_MAX_SPEED = String.valueOf(DefaultValues.DEFAULT_ROAD_MAX_SPEED);
    final static String STOCK_LENGTH = String.valueOf(DefaultValues.DEFAULT_ROAD_LENGTH);

    public static void changeRoadsDialog(Stage stage, ArrayList<Double> roadLengths, ArrayList<Double> roadSpeeds,
                                         ArrayList<Integer> roadLanes, int numberOfRoads, boolean changingAll,
                                         int index) {
        Dialog<ButtonType> dialog = new Dialog<>();
        String speed;
        String length;
        int lanes;
        if (!changingAll) {
            dialog.setTitle("Change selected road properties, road index: " + (index + 1));
            dialog.setHeaderText("Modify properties of the selected road: " + (index + 1));
            speed = String.valueOf(roadSpeeds.get(index));
            length = String.valueOf(roadLengths.get(index));
            lanes = roadLanes.get(index);
        } else {
            dialog.setTitle("Change all roads properties");
            dialog.setHeaderText("Modify properties of the all roads");
            speed = STOCK_MAX_SPEED;
            length = STOCK_LENGTH;
            lanes = Integer.parseInt(STOCK_NUMBER_OF_LANES);
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
        Spinner<Integer> lanesField = new Spinner<>(1, 10, lanes);

        grid.add(new Label("Length (m):"), 0, 0);
        grid.add(lengthField, 1, 0);

        grid.add(new Label("Speed limit (km/h):"), 0, 1);
        grid.add(speedLimitField, 1, 1);

        grid.add(new Label("Number of lanes:"), 0, 2);
        grid.add(lanesField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // show the dialog and wait for user response
        dialog.showAndWait().ifPresent(response -> {
            if (response == applyButtonType) {
                if (!changingAll) {
                    MyLogger.log("Selected road properties updated via dialog.", Constants.INFO_FOR_LOGGING);
                    changeRoadParameters(index, lanesField.getValue(), Double.parseDouble(speedLimitField.getText()),
                            Double.parseDouble(lengthField.getText()), roadLengths, roadSpeeds, roadLanes);
                    return;
                }
                MyLogger.log("All road properties updated via dialog.", Constants.INFO_FOR_LOGGING);
                for (int i = 0; i < numberOfRoads; i++) {
                    changeRoadParameters(i, lanesField.getValue(), Double.parseDouble(speedLimitField.getText()),
                            Double.parseDouble(lengthField.getText()), roadLengths, roadSpeeds, roadLanes);
                }
            } else {
                MyLogger.log("Dialog cancelled.", Constants.INFO_FOR_LOGGING);
            }
        });
    }

    public static void selectRoadDialog(Stage stage, ArrayList<Double> roadLengths, ArrayList<Double> roadSpeeds,
                                        ArrayList<Integer> roadLanes, int numberOfRoads) {
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
                changeRoadsDialog(stage, roadLengths, roadSpeeds, roadLanes, numberOfRoads, false
                        , indexField.getValue() - 1);
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

        ArrayList<Double> roadLengths = new ArrayList<>();
        ArrayList<Double> roadSpeeds = new ArrayList<>();
        ArrayList<Integer> roadLanes = new ArrayList<>();

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

        // Checkbox: whether all roads should share the same lane settings
        grid.add(new Label("Changing all roads:"), 0, 2);
        CheckBox changingAllRoads = new CheckBox();
        changingAllRoads.setSelected(true);
        grid.add(changingAllRoads, 1, 2);

        Button changeAllRoadsButton = new Button("Change all roads...");
        Button changeOneRoadButton = new Button("Change one road...");
        grid.add(changeAllRoadsButton, 0, 3);
        grid.add(changeOneRoadButton, 1, 3);

        changeAllRoadsButton.setOnAction(e -> {
            changeRoadsDialog(primaryStage, roadLengths, roadSpeeds, roadLanes,
                    roadsSpinner.getValue(), true, -1);
        });

        changeOneRoadButton.setOnAction(e -> {
            selectRoadDialog(primaryStage, roadLengths, roadSpeeds, roadLanes,
                    roadsSpinner.getValue());
        });

        ArrayList<TextField> lengthFields = new ArrayList<>();
        ArrayList<TextField> speedFields = new ArrayList<>();
        ArrayList<Spinner<Integer>> laneSpinners = new ArrayList<>();
        int numberOfRoads = roadsSpinner.getValue();

        for (int i = 0; i < numberOfRoads; i++) {
            addRoadParameters(Integer.parseInt(STOCK_NUMBER_OF_LANES), Double.parseDouble(STOCK_MAX_SPEED),
                    Double.parseDouble(STOCK_LENGTH), roadLengths, roadSpeeds, roadLanes);
        }

        // if number of roads changes, update the lists
        roadsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            int newNumberOfRoads = newVal;
            if (newNumberOfRoads > lengthFields.size()) {
                for (int i = lengthFields.size(); i < newNumberOfRoads; i++) {
                    addRoadParameters(Integer.parseInt(STOCK_NUMBER_OF_LANES),
                            Double.parseDouble(STOCK_MAX_SPEED), Double.parseDouble(STOCK_LENGTH),
                            roadLengths, roadSpeeds, roadLanes);
                }
            } else if (newNumberOfRoads < lengthFields.size()) {
                lengthFields.subList(newNumberOfRoads, lengthFields.size()).clear();
                speedFields.subList(newNumberOfRoads, speedFields.size()).clear();
                laneSpinners.subList(newNumberOfRoads, laneSpinners.size()).clear();
            }
        });

        dialog.getDialogPane().setContent(grid);

        // Handle Create button click
        dialog.showAndWait().ifPresent(result -> {
            if (result == createButtonType) {
                String mapFileName = mapFileNameField.getText();
                RoadXml.writeMapToXml(roadLengths, roadSpeeds, roadLanes, roadSpeeds.size(), mapFileName);
                MyLogger.log("Creating new map: " + mapFileName, Constants.INFO_FOR_LOGGING);
            }
        });
    }

    public static void addRoadParameters(int numberOfLanes, double maxSpeed, double length,
                                          ArrayList<Double> roadLengths, ArrayList<Double> roadSpeeds,
                                          ArrayList<Integer> roadLanes) {
        roadLengths.add(length);
        roadSpeeds.add(maxSpeed);
        roadLanes.add(numberOfLanes);
    }

    public static void changeRoadParameters(int index, int numberOfLanes, double maxSpeed, double length,
                                          ArrayList<Double> roadLengths, ArrayList<Double> roadSpeeds,
                                          ArrayList<Integer> roadLanes) {
        roadLengths.set(index, length);
        roadSpeeds.set(index, maxSpeed);
        roadLanes.set(index, numberOfLanes);
    }





}
