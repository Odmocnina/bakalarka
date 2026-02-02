package ui;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.model.Parameter;
import core.utils.DefaultStuffMaker;
import core.utils.MyLogger;
import core.utils.RoadParameters;
import core.utils.RoadXml;
import core.utils.constants.Constants;
import core.utils.constants.DefaultValues;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

/********************************************
 * class to create various dialogs for the UI
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class DialogMaker {

    /** stock values for new map dialog **/
    final static String STOCK_NUMBER_OF_LANES = String.valueOf(DefaultValues.DEFAULT_ROAD_LANES);

    /** stock values for new map dialog **/
    final static String STOCK_MAX_SPEED = String.valueOf(DefaultValues.DEFAULT_ROAD_MAX_SPEED);

    /** stock values for new map dialog **/
    final static String STOCK_LENGTH = String.valueOf(DefaultValues.DEFAULT_ROAD_LENGTH);

    /**
     * show dialog to edit a light plan
     *
     * @param stage owner stage
     * @param lightPlan light plan to edit
     **/
    protected static void editLightPlanDialog(Stage stage, LightPlan lightPlan, int lane) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit light plan on lane: " + lane);
        dialog.setHeaderText("Edit the light plan on lane: " + lane);
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
                    warningDialog(stage, "The light plan is not legitimate! Make sure that the switch time is less than the cycle time.");
                }
            } else {
                MyLogger.log("Light plan dialog cancelled.", Constants.INFO_FOR_LOGGING);
            }
        });
    }

    /**
     * method for showing warning dialog
     *
     * @param stage owner stage
     * @param message warning message
     **/
    protected static void warningDialog(Stage stage, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Warning");
        alert.setContentText(message);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    /**
     * show dialog to edit a car generator on lane
     *
     * @param stage owner stage
     * @param generator car generator to edit
     **/
    protected static void editGeneratorDialog(Stage stage, CarGenerator generator, int lane) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit car generator on lane: " + lane);
        dialog.setHeaderText("Edit the car generator parameters ona lane: " + lane);
        dialog.initOwner(stage);

        ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        dialog.getDialogPane().setContent(grid);

        CheckBox useQueueCheck = new CheckBox("Use queue");

        TextField flowRateInput = new TextField(String.valueOf(generator.getFlowRate()));
        TextField minQueueInput = new TextField(String.valueOf(generator.getMinQueueSize()));
        TextField maxQueueInput = new TextField(String.valueOf(generator.getMaxQueueSize()));
        useQueueCheck.setSelected(generator.generatingToQueue());

        // runnable to refresh the grid content, when changed
        Runnable refreshGrid = new Runnable() {
            @Override
            public void run() {
                // clear the grid
                grid.getChildren().clear();

                // check box queue/flow rate
                grid.add(useQueueCheck, 0, 0);

                if (useQueueCheck.isSelected()) {
                    grid.add(new Label("Min size of queue input:"), 0, 1);
                    grid.add(minQueueInput, 1, 1);

                    grid.add(new Label("Max size of queue input:"), 2, 1);
                    grid.add(maxQueueInput, 3, 1);
                } else {
                    Label flowRateLabel = new Label("Flow rate (cars/s):");
                    grid.add(flowRateLabel, 0, 1);
                    grid.add(flowRateInput, 1, 1);
                }


                // parameters
                HashMap<String, Parameter> parameters = generator.getAllComParameters();
                int i = 2;
                for (String paramKey : parameters.keySet()) {
                    Parameter param = parameters.get(paramKey);

                    Label paramLabel = new Label(param.name + " (" + paramKey + "):");
                    Label minValue = new Label("Minimum value: ");
                    //TextField minInput = new TextField(String.valueOf(param.minValue));
                    Label minValueValue = new Label(String.valueOf(param.minValue));
                    Label maxValue = new Label("Maximum value: ");
                    //TextField maxInput = new TextField(String.valueOf(param.maxValue));
                    Label maxValueValue = new Label(String.valueOf(param.maxValue));
                    Button deleteButton = new Button("Delete");
                    Button changeButton = new Button("Change");

                    deleteButton.setOnAction(e -> {
                        removeParameterFromGenerator(generator, param.name, paramKey, this, dialog);
                    });

                    changeButton.setOnAction(e -> {
                        changeParameterDialog(stage, generator, paramKey, param);
                        MyLogger.log("Change parameter button clicked for parameter: " + param.name
                                + " (" + paramKey + ").", Constants.INFO_FOR_LOGGING);
                        // refresh the grid to show the changed parameter
                        this.run();
                        // make window resize to fit new content
                        dialog.getDialogPane().getScene().getWindow().sizeToScene();
                    });

                    grid.add(paramLabel, 0, i);
                    grid.add(minValue, 1, i);
                    grid.add(minValueValue, 2, i);
                    grid.add(maxValue, 3, i);
                    grid.add(maxValueValue, 4, i);
                    grid.add(changeButton, 5, i);
                    grid.add(deleteButton, 6, i);
                    i++;
                }

                // add new parameter button
                Button newParamButton = new Button("Add new parameter");
                grid.add(newParamButton, 0, i);
                newParamButton.setOnAction(e -> {
                    // open new parameter dialog
                    newParameterDialog(stage, generator);
                    MyLogger.log("Add new parameter button clicked.", Constants.INFO_FOR_LOGGING);

                    // refresh the grid to show the new parameter
                    this.run();

                    // make window resize to fit new content
                    dialog.getDialogPane().getScene().getWindow().sizeToScene();
                });
            }
        };

        // refresh grid when queue/flow rate checkbox is changed
        useQueueCheck.setOnAction(event -> {
            refreshGrid.run();
            dialog.getDialogPane().getScene().getWindow().sizeToScene();
        });

        // fill the grid for the first time
        refreshGrid.run();

        final Button btApply = (Button) dialog.getDialogPane().lookupButton(applyButtonType);
        btApply.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                if (useQueueCheck.isSelected()) {
                    // queue selected
                    int minQueue = Integer.parseInt(minQueueInput.getText());
                    int maxQueue = Integer.parseInt(maxQueueInput.getText());

                    if (minQueue <= 0 || maxQueue <= 0) {
                        throw new NumberFormatException("Queue sizes must be positive");
                    }

                    if (minQueue > maxQueue) {
                        throw new NumberFormatException("Min queue size must be less or equal than max queue size");
                    }

                    // set queue values
                    generator.setQueueSize(minQueue, maxQueue);
                } else {
                    // is it number
                    String input = flowRateInput.getText();
                    double newFlowRate = Double.parseDouble(input);

                    // is it positive
                    if (newFlowRate <= 0) {
                        throw new NumberFormatException("Value must be positive");
                    }

                    // set value
                    generator.setFlowRate(newFlowRate);
                    // disable queue
                    generator.disableQueue();
                }
            } catch (NumberFormatException e) {
                // value is not valid
                MyLogger.log("Invalid flow rate or queue size value: " + e, Constants.ERROR_FOR_LOGGING);

                warningDialog(stage, "Invalid flow rate or queue size value: " + e);

                // kill event, do not close dialog
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(response -> {
            if (response == applyButtonType) {
                generator.copyComParametersToRealParameters(AppContext.CAR_FOLLOWING_MODEL.getType(), AppContext.CAR_FOLLOWING_MODEL.getCellSize());
                MyLogger.log("Car generator updated via dialog.", Constants.INFO_FOR_LOGGING);
            } else {
                MyLogger.log("Car generator dialog cancelled.", Constants.INFO_FOR_LOGGING);
            }
        });
    }


    private static void removeParameterFromGenerator(CarGenerator generator, String name, String paramKey,
                                                     Runnable refreshGrid, Dialog<ButtonType> dialog) {
        // pop up confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete parameter");
        alert.setHeaderText("Are you sure you want to delete parameter: " + name + "(" + paramKey + ")" + "?");
        alert.initOwner(dialog.getDialogPane().getScene().getWindow());
        alert.showAndWait().ifPresent(response -> {

            if (response != ButtonType.OK) {
                MyLogger.log("Parameter deletion cancelled for parameter: " + name + "(" + paramKey + ").",
                        Constants.INFO_FOR_LOGGING);
            } else {
                MyLogger.log("Parameter deletion confirmed for parameter: " + name + "(" + paramKey + ").",
                        Constants.INFO_FOR_LOGGING);
                generator.removeComParameter(paramKey);
                MyLogger.log("Parameter " + paramKey + " deleted from car generator.", Constants.INFO_FOR_LOGGING);
                refreshGrid.run();
                dialog.getDialogPane().getScene().getWindow().sizeToScene();
            }
        });
    }

    protected static void removeRoadFormMap(ArrayList<RoadParameters> roadParameters, int index,
                                            Stage stage, Dialog<ButtonType> dialog) {
        //sanity check
        if (roadParameters.isEmpty()) {
            MyLogger.log("No roads available to delete.", Constants.ERROR_FOR_LOGGING);
            warningDialog(stage, "No roads available to delete.");
            return;
        }

        if (index < 0 || index >= roadParameters.size()) {
            MyLogger.log("Invalid road index to delete: " + index, Constants.ERROR_FOR_LOGGING);
            warningDialog(stage, "Invalid road index to delete: " + (index + 1));
            return;
        }

        // pop up confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        int roadNumber = index + 1;
        alert.setTitle("Delete road");
        alert.setHeaderText("Are you sure you want to delete road: " + roadNumber + "?");
        alert.initOwner(stage);
        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                AppContext.RUN_DETAILS.mapChanged = true;
                MyLogger.log("Road deletion cancelled for road index: " + roadNumber + ".",
                        Constants.INFO_FOR_LOGGING);
            } else {
                MyLogger.log("Road deletion confirmed for road index: " + roadNumber + ".",
                        Constants.INFO_FOR_LOGGING);
                roadParameters.remove(index);
                MyLogger.log("Road " + roadNumber + " deleted from map.", Constants.INFO_FOR_LOGGING);
            }
        });
    }

    private static void changeParameterDialog(Stage stage, CarGenerator generator, String oldKey, Parameter parameter) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change parameter in car generator");
        dialog.setHeaderText("Change parameter in car generator");
        dialog.initOwner(stage);

        ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

        double minValueOld = parameter.minValue;
        double maxValueOld = parameter.maxValue;
        String name = parameter.name;

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        Label paramNameLabel = new Label("Parameter name:");
        TextField paramNameInput = new TextField();
        paramNameInput.setText(name);
        grid.add(paramNameLabel, 0, 0);
        grid.add(paramNameInput, 1, 0);

        Label paramKeyLabel = new Label("Parameter key:");
        TextField paramKeyInput = new TextField();
        paramKeyInput.setText(oldKey);
        grid.add(paramKeyLabel, 0, 1);
        grid.add(paramKeyInput, 1, 1);

        Label minValueLabel = new Label("Minimum value:");
        TextField minValueInput = new TextField();
        minValueInput.setText(String.valueOf(minValueOld));
        grid.add(minValueLabel, 0, 2);
        grid.add(minValueInput, 1, 2);

        Label maxValueLabel = new Label("Maximum value:");
        TextField maxValueInput = new TextField();
        maxValueInput.setText(String.valueOf(maxValueOld));
        grid.add(maxValueLabel, 0, 3);
        grid.add(maxValueInput, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // show the dialog and wait for user response
        dialog.showAndWait().ifPresent(response -> {
            if (response == applyButtonType) {
                double minValue = 0;
                double maxValue = 0;
                String key = paramKeyInput.getText();
                boolean ok = true;
                // check if parameter key and values are legit

                // min value
                try {
                    minValue = Double.parseDouble(minValueInput.getText());
                } catch (NumberFormatException e) {
                    MyLogger.log("Invalid minimum value for parameter: " + minValueInput.getText(),
                            Constants.ERROR_FOR_LOGGING);
                    warningDialog(stage, "Invalid minimum value for parameter: " + minValueInput.getText());
                    ok = false;
                }

                // max value
                try {
                    maxValue = Double.parseDouble(maxValueInput.getText());
                } catch (NumberFormatException e) {
                    MyLogger.log("Invalid maximum value for parameter: " + maxValueInput.getText(),
                            Constants.ERROR_FOR_LOGGING);
                    warningDialog(stage, "Invalid maximum value for parameter: " + maxValueInput.getText());
                    ok = false;
                }

                // if min < max
                if (minValue > maxValue) {
                    MyLogger.log("Minimum value must be less or equal than maximum value for parameter: "
                            + minValue + " > " + maxValue, Constants.ERROR_FOR_LOGGING);
                    warningDialog(stage, "Minimum value must be less or equal than maximum value for parameter: "
                            + minValue + " > " + maxValue);
                    ok = false;
                }

                // if key is ok
                if (key == null || key.isEmpty()) {
                    MyLogger.log("Parameter key cannot be empty .", Constants.ERROR_FOR_LOGGING);
                    warningDialog(stage, "Parameter key cannot be empty.");
                    ok = false;
                } else if (!key.equals(oldKey) && generator.keyExists(key)) { // id key isn't already used
                    MyLogger.log("Parameter key already exists: " + key, Constants.ERROR_FOR_LOGGING);
                    warningDialog(stage, "Parameter key already exists: " + key);
                    ok = false;
                }
                String nameOfParam = paramNameInput.getText();

                if (ok) {
                    generator.removeComParameter(oldKey);
                    generator.addComParameter(key, nameOfParam, minValue, maxValue);
                    MyLogger.log("Parameter changed via dialog.", Constants.INFO_FOR_LOGGING);
                }
            } else {
                MyLogger.log("Old parameter dialog cancelled.", Constants.INFO_FOR_LOGGING);
            }
        });
    }

    private static void newParameterDialog(Stage stage, CarGenerator generator) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add new parameter to car generator");
        dialog.setHeaderText("Add a new parameter to the car generator");
        dialog.initOwner(stage);

        ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        Label paramNameLabel = new Label("Parameter name:");
        TextField paramNameInput = new TextField();
        grid.add(paramNameLabel, 0, 0);
        grid.add(paramNameInput, 1, 0);

        Label paramKeyLabel = new Label("Parameter key:");
        TextField paramKeyInput = new TextField();
        grid.add(paramKeyLabel, 0, 1);
        grid.add(paramKeyInput, 1, 1);

        Label minValueLabel = new Label("Minimum value:");
        TextField minValueInput = new TextField();
        grid.add(minValueLabel, 0, 2);
        grid.add(minValueInput, 1, 2);

        Label maxValueLabel = new Label("Maximum value:");
        TextField maxValueInput = new TextField();
        grid.add(maxValueLabel, 0, 3);
        grid.add(maxValueInput, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // show the dialog and wait for user response
        dialog.showAndWait().ifPresent(response -> {
            if (response == applyButtonType) {
                double minValue = 0;
                double maxValue = 0;
                String key = paramKeyInput.getText();
                boolean ok = true;
                // check if parameter key and values are legit

                // min value
                try {
                    minValue = Double.parseDouble(minValueInput.getText());
                } catch (NumberFormatException e) {
                    MyLogger.log("Invalid minimum value for new parameter: " + minValueInput.getText(),
                            Constants.ERROR_FOR_LOGGING);
                    warningDialog(stage, "Invalid minimum value for new parameter: " + minValueInput.getText());
                    ok = false;
                }

                // max value
                try {
                    maxValue = Double.parseDouble(maxValueInput.getText());
                } catch (NumberFormatException e) {
                    MyLogger.log("Invalid maximum value for new parameter: " + maxValueInput.getText(),
                            Constants.ERROR_FOR_LOGGING);
                    warningDialog(stage, "Invalid maximum value for new parameter: " + maxValueInput.getText());
                    ok = false;
                }

                // if min < max
                if (minValue > maxValue) {
                    MyLogger.log("Minimum value must be less or equal than maximum value for new parameter: "
                            + minValue + " > " + maxValue, Constants.ERROR_FOR_LOGGING);
                    warningDialog(stage, "Minimum value must be less or equal than maximum value for new parameter: "
                            + minValue + " > " + maxValue);
                    ok = false;
                }

                // if key is ok
                if (key == null || key.isEmpty()) {
                    MyLogger.log("Parameter key cannot be empty .", Constants.ERROR_FOR_LOGGING);
                    warningDialog(stage, "Parameter key cannot be empty.");
                    ok = false;
                }

                // id key isn't already used
                if (generator.keyExists(key)) {
                    MyLogger.log("Parameter key already exists: " + key, Constants.ERROR_FOR_LOGGING);
                    warningDialog(stage, "Parameter key already exists: " + key);
                    ok = false;
                }
                String nameOfParam = paramNameInput.getText();

                if (ok) {
                    generator.addComParameter(key, nameOfParam, minValue, maxValue);
                    MyLogger.log("New parameter added via dialog.", Constants.INFO_FOR_LOGGING);
                }
            } else {
                MyLogger.log("New parameter dialog cancelled.", Constants.INFO_FOR_LOGGING);
            }
        });
    }

    /**
     * show dialog to change road properties
     *
     * @param stage owner stage
     * @param roadParameters list of road parameters
     * @param numberOfRoads number of roads
     * @param changingAll boolean if changing all roads
     * @param index index of road to change if not changing all
     **/
     protected static void changeRoadsDialog(Stage stage, ArrayList<RoadParameters> roadParameters, int numberOfRoads,
                                         boolean changingAll, int index) {
         Dialog<ButtonType> dialog = new Dialog<>();
         String speed;
         String length;
         int lanes;
         LinkedList<LightPlan> lightPlan;
         LinkedList<CarGenerator> generators;
         if (!changingAll) {
             dialog.setTitle("Change selected road properties, road index: " + (index + 1));
             dialog.setHeaderText("Modify properties of the selected road: " + (index + 1));
             speed = String.valueOf(roadParameters.get(index).maxSpeed);
             length = String.valueOf(roadParameters.get(index).length);
             lanes = roadParameters.get(index).lanes;
             lightPlan = roadParameters.get(index).lightPlan;
             generators = roadParameters.get(index).carGenerators;
         } else {
             dialog.setTitle("Change all roads properties");
             dialog.setHeaderText("Modify properties of the all roads");
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
         }
         dialog.initOwner(stage);

         ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
         ButtonType deleteButtonType = new ButtonType("Delete Road", ButtonBar.ButtonData.NO);
         if (!changingAll) { // if one road is being changed, give option to delete
             dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, deleteButtonType, ButtonType.CANCEL);
             Node deleteButton = dialog.getDialogPane().lookupButton(deleteButtonType);
             if (deleteButton != null) {
                 deleteButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
             }
         } else {
             dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);
         }

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

         // listener to update lane index max value when number of lanes changes
         lanesSpinner.valueProperty().addListener((obs, oldVal, newVal) ->
                 //laneIndexSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, newVal, 1)));
                 updateNumberOfLanes(lightPlan, generators, newVal, laneIndexSpinner));

         grid.add(new Label("Edit light plan or generator on lane:"), 0, 3);
         grid.add(laneIndexSpinner, 1, 3);

         Button lightPlanButton = new Button("Edit one light plan...");
         grid.add(lightPlanButton, 1, 4);

         Button generatorButton = new Button("Edit one car generator...");
         grid.add(generatorButton, 1, 5);

         Button editAllLightPlansButton = new Button("Edit all light plans...");
         grid.add(editAllLightPlansButton, 0, 4);

         Button editAllGeneratorsButton = new Button("Edit all car generators...");
         grid.add(editAllGeneratorsButton, 0, 5);

         lightPlanButton.setOnAction(e ->
                 editLightPlanDialog(stage, lightPlan.get(laneIndexSpinner.getValue() - 1), laneIndexSpinner.getValue()));

         generatorButton.setOnAction(e ->
                 editGeneratorDialog(stage, generators.get(laneIndexSpinner.getValue() - 1),
                         laneIndexSpinner.getValue()));

         editAllLightPlansButton.setOnAction(e -> {
             LightPlan lp = DefaultStuffMaker.createDefaultLightPlan();
             editLightPlanDialog(stage, lp, 0);
             for (int i = 0; i < lightPlan.size(); i++) {
                 lightPlan.set(i, lp.clone());
             }
         });

         editAllGeneratorsButton.setOnAction(e -> {
             CarGenerator cg = DefaultStuffMaker.createDefaultGenerator();
             editGeneratorDialog(stage, cg, 0);
             for (int i = 0; i < generators.size(); i++) {
                 generators.set(i, cg.clone());
             }
         });

         dialog.getDialogPane().setContent(grid);

         // show the dialog and wait for user response
         dialog.showAndWait().ifPresent(response -> {
             if (response == applyButtonType) {
                 if (checkRoadInputs(lanesSpinner.getValue(), speedLimitField.getText(), lengthField.getText(), generators, lightPlan)) {
                     AppContext.RUN_DETAILS.mapChanged = true;
                     if (!changingAll) {
                         MyLogger.log("Selected road properties updated via dialog.", Constants.INFO_FOR_LOGGING);
                         changeRoadParameters(index, lanesSpinner.getValue(), Double.parseDouble(speedLimitField.getText()),
                                 Double.parseDouble(lengthField.getText()), lightPlan, generators, roadParameters);
                         return;
                     }
                     MyLogger.log("All road properties updated via dialog.", Constants.INFO_FOR_LOGGING);
                     for (int i = 0; i < numberOfRoads; i++) {
                         changeRoadParameters(i, lanesSpinner.getValue(), Double.parseDouble(speedLimitField.getText()),
                                 Double.parseDouble(lengthField.getText()), lightPlan, generators, roadParameters);
                     }

                 } else {
                     MyLogger.log("Invalid road inputs provided in dialog.", Constants.ERROR_FOR_LOGGING);
                     warningDialog(stage, "Invalid road inputs provided. Please check number of lanes, max speed and length.");
                 }
             } else if (response == deleteButtonType) {
                 if (!changingAll) {
                     removeRoadFormMap(roadParameters, index, stage, dialog);
                     AppContext.RUN_DETAILS.mapChanged = true;
                 }
             } else {
                 MyLogger.log("Dialog cancelled.", Constants.INFO_FOR_LOGGING);
             }
         });
     }

    /**
     * show dialog to select a road to edit
     *
     * @param stage owner stage
     * @param roadParameters list of road parameters
     * @param numberOfRoads number of roads
     **/
    protected static void selectRoadDialog(Stage stage, ArrayList<RoadParameters> roadParameters, int numberOfRoads) {
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

    public static void saveAsDialog(Stage stage) {
        FileChooser fileChooser = new FileChooser(); // the thing that has save as in stuff like ps pad and word
        fileChooser.setTitle("Save map as..."); // title of the dialog

        // filter for xml files, so only xml files are shown
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml"),
                new FileChooser.ExtensionFilter("All files (*.*)", "*.*")
        );

        // set the initial directory to current working directory
        File currentDir = new File(System.getProperty("user.dir"));
        if (currentDir.exists()) {
            fileChooser.setInitialDirectory(currentDir);
        }

        // default file name
        fileChooser.setInitialFileName(Constants.newMapFileName);

        // Opens the save dialog and blocks until the user selects a file or cancels
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            // file was selected
            String filePath = file.getAbsolutePath();

            MyLogger.log("User saved map as: " + filePath, Constants.INFO_FOR_LOGGING);

            RoadXml.saveAs(filePath);
        } else {
            // user cancelled the dialog
            MyLogger.log("Save as canceled by user.", Constants.INFO_FOR_LOGGING);
        }
    }

    protected static void updateNumberOfLanes(LinkedList<LightPlan> lightPlans, LinkedList<CarGenerator> generators,
                                            int newNumberOfLanes, Spinner<Integer> lanesSpinner) {
        if (newNumberOfLanes > lightPlans.size()) {
            for (int i = lightPlans.size(); i < newNumberOfLanes; i++) {
                lightPlans.add(DefaultStuffMaker.createDefaultLightPlan());
            }
        } else if (newNumberOfLanes < lightPlans.size()) {
            lightPlans.subList(newNumberOfLanes, lightPlans.size()).clear();
        }
        if (newNumberOfLanes > generators.size()) {
            for (int i = generators.size(); i < newNumberOfLanes; i++) {
                generators.add(DefaultStuffMaker.createDefaultGenerator());
            }
        } else if (newNumberOfLanes < generators.size()) {
            generators.subList(newNumberOfLanes, generators.size()).clear();
        }

        lanesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, newNumberOfLanes, 1));
    }

    /**
     * add road parameters to list
     *
     * @param numberOfLanes number of lanes
     * @param maxSpeed max speed
     * @param length length of road
     * @param lightPlan light plan for the road
     * @param roadParameters list of road parameters
     **/
    protected static void addRoadParameters(int numberOfLanes, double maxSpeed, double length,
                                         LinkedList<LightPlan> lightPlan, LinkedList<CarGenerator> generators,
                                         ArrayList<RoadParameters> roadParameters) {
        RoadParameters rp = new RoadParameters();
        rp.lanes = numberOfLanes;
        rp.maxSpeed = maxSpeed;
        rp.length = length;
        rp.lightPlan = lightPlan;
        rp.carGenerators = generators;
        roadParameters.add(rp);
    }

    /**
     * change road parameters in list
     *
     * @param index index of road to change
     * @param numberOfLanes number of lanes
     * @param maxSpeed max speed
     * @param length length of road
     * @param lightPlan light plan for the road
     * @param roadParameters list of road parameters
     **/
    protected static void changeRoadParameters(int index, int numberOfLanes, double maxSpeed, double length,
                                            LinkedList<LightPlan> lightPlan, LinkedList<CarGenerator> generators,
                                            ArrayList<RoadParameters> roadParameters) {
        RoadParameters rp = new RoadParameters();
        rp.lanes = numberOfLanes;
        rp.maxSpeed = maxSpeed;
        rp.length = length;
        rp.lightPlan = lightPlan;
        rp.carGenerators = generators;
        roadParameters.set(index, rp);
    }

    protected static boolean checkRoadInputs(Integer numberOfLanes, String maxSpeed, String length, LinkedList<CarGenerator> generator,
                                             LinkedList<LightPlan> lightPlan) {
        if (numberOfLanes <= 0) {
            return false;
        }

        try {
            double speed = Double.parseDouble(maxSpeed);
            if (speed <= 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        try {
            double len = Double.parseDouble(length);
            if (len <= 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        for (CarGenerator cg : generator) {
            if (!cg.isLegitimate()) {
                return false;
            }
        }

        for (LightPlan lp : lightPlan) {
            if (!lp.isLegitimate()) {
                return false;
            }
        }

        return true;
    }

    public static boolean onCloseUnsavedChangesDialog(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes.");
        alert.setContentText("Do you want to save your changes before exiting?");
        alert.initOwner(stage);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.YES);
        ButtonType dontSaveButton = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == saveButton) {
                MyLogger.log("User chose to save changes before exiting.", Constants.INFO_FOR_LOGGING);
                RoadXml.saveAs(AppContext.RUN_DETAILS.mapFile);
                return true;
            } else if (result.get() == dontSaveButton) {
                MyLogger.log("User chose not to save changes before exiting.", Constants.INFO_FOR_LOGGING);
                return true;
            }
        }

        return false;
    }

    public static boolean showExitConfirmation(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved progress might be lost.");
        alert.initOwner(stage);

        Optional<ButtonType> result = alert.showAndWait();
        // return true if user clicked OK
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void changeTimeBetweenSteps(Stage stage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change time between simulation steps");
        dialog.setHeaderText("Change the time (in seconds) between each simulation step.");
        dialog.initOwner(stage);

        ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField timeInput = new TextField(String.valueOf(AppContext.RUN_DETAILS.timeBetweenSteps));
        grid.add(new Label("Time between steps (s):"), 0, 0);
        grid.add(timeInput, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // show the dialog and wait for user response
        dialog.showAndWait().ifPresent(response -> {
            if (response == applyButtonType) {
                try {
                    int timeBetweenSteps = Integer.parseInt(timeInput.getText());
                    if (timeBetweenSteps <= 0) {
                        throw new NumberFormatException("Value must be positive");
                    }

                    AppContext.RUN_DETAILS.timeBetweenSteps = timeBetweenSteps;
                    MyLogger.log("Time between simulation steps updated via dialog.", Constants.INFO_FOR_LOGGING);
                } catch (NumberFormatException e) {
                    MyLogger.log("Invalid time between steps value: " + e, Constants.ERROR_FOR_LOGGING);
                    warningDialog(stage, "Invalid time between steps value: " + e);
                }
            } else {
                MyLogger.log("Time between steps dialog cancelled.", Constants.INFO_FOR_LOGGING);
            }
        });
    }
}
