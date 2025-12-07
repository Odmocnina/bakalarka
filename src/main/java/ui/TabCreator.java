package ui;

import core.utils.constants.ConfigConstants;
import core.utils.ConfigXml;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TabCreator {

    protected Tab createRoadConfigTab(ConfigXml configXml) {
        Tab tab = new Tab("Road Configuration");

        // 1. Get initial values for the summary label
        // Using generic getValue to fetch data safely
        String currentRoads = configXml != null ? configXml.getValue("numberOfRoads") : "N/A";
        String currentFile  = configXml != null ? configXml.getValue("roadFile") : "N/A";

        // Shorten the file path for display if it's too long
        if (currentFile != null && currentFile.length() > 25) {
            currentFile = "..." + currentFile.substring(currentFile.length() - 20);
        }

        // 2. Summary Label
        Label infoSummary = new Label("Roads: " + currentRoads + " | File: " + currentFile);
        infoSummary.setStyle("-fx-text-fill: #555;");

        // 3. Button to open settings
        Button openRoadSettingsBtn = new Button("Open road settings...");

        // Action -> Open Popup
//        openRoadSettingsBtn.setOnAction(e ->
//                openRoadSettingsPopup(infoSummary, infoLabel, configXml)
//        );

        // 4. Layout
        HBox strip = new HBox(15, openRoadSettingsBtn, infoSummary);
        strip.setAlignment(Pos.CENTER_LEFT);
        strip.setPadding(new Insets(10));

        tab.setContent(strip);
        return tab;
    }

    protected Tab createRunDetailsTab(ConfigXml configXml, Label infoLabel) {
        Tab tab = new Tab("Run details");

        // Tlačítko pro otevření nastavení
        Button openOutputSettingsBtn = new Button("Open output settings...");
        Button openLoggingSettingsBtn = new Button("Open logging settings...");
        Button openRunningFlagsSettingsBtn = new Button("Open running flags settings...");

        // Informativní label (aby uživatel viděl základní info bez otevírání okna)
        String dur = "1000";
        String step = "1";
        Label infoSummary = new Label("Duration: " + dur + " ms | Time step: " + step);

        // Akce tlačítka -> otevře popup
        openOutputSettingsBtn.setOnAction(e -> openOutputPopup(infoLabel, configXml));
        openLoggingSettingsBtn.setOnAction(e -> openLoggingPopup(infoLabel, configXml));
        openRunningFlagsSettingsBtn.setOnAction(e -> openSimulationSettingsPopup(infoLabel, configXml));

        // Jednoduchý horizontální layout (stejný styl jako Generator tab)
        HBox strip = new HBox(15, openOutputSettingsBtn, openLoggingSettingsBtn, openRunningFlagsSettingsBtn,
                infoSummary);
        strip.setAlignment(Pos.CENTER_LEFT);
        strip.setPadding(new Insets(10));

        tab.setContent(strip);
        return tab;
    }

    private void openLoggingPopup(Label infoLabel, ConfigXml configXml) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Logging Configuration");
        // Set the owner to prevent the popup from being an orphan in the taskbar
        popupStage.initOwner(infoLabel.getScene().getWindow());

        // --- 1. Load Initial Values from ConfigXml ---
        // Safe loading: if config is null, default to true (or false for debug)
        String[] request = {ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.LOGGING_TAG,
                ConfigConstants.LOG_GENERAL_TAG};
        String getLogString = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean logVal = configXml == null || configXml.getBool(getLogString);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.LOGGING_TAG,
                ConfigConstants.LOG_INFO_TAG};
        String getLogInfoString = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean infoVal  = configXml == null || configXml.getBool(getLogInfoString);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.LOGGING_TAG,
                ConfigConstants.LOG_WARN_TAG};
        String getLogWarnString = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean warnVal  = configXml == null || configXml.getBool(getLogWarnString);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.LOGGING_TAG,
                ConfigConstants.LOG_DEBUG_TAG};
        String getLogDebugString = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean debugVal = configXml == null || configXml.getBool(getLogDebugString);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.LOGGING_TAG,
                ConfigConstants.LOG_ERROR_TAG};
        String getLogErrorString = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean errorVal = configXml == null || configXml.getBool(getLogErrorString);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.LOGGING_TAG,
                ConfigConstants.LOG_FATAL_TAG};
        String getLogFatalString = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean fatalVal = configXml == null || configXml.getBool(getLogFatalString);


        // creating gui elements
        // Header
        Label headerLabel = new Label("Select Log Levels:");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // CheckBoxes
        CheckBox cbLog = new CheckBox("Log (General)");
        cbLog.setSelected(logVal);

        CheckBox cbInfo = new CheckBox("Info");
        cbInfo.setSelected(infoVal);

        CheckBox cbWarn = new CheckBox("Warning");
        cbWarn.setSelected(warnVal);
        cbWarn.setStyle("-fx-text-fill: #b36b00;");

        CheckBox cbDebug = new CheckBox("Debug");
        cbDebug.setSelected(debugVal);
        cbDebug.setStyle("-fx-opacity: 0.9;"); // Visual hint it's usually off

        CheckBox cbError = new CheckBox("Error");
        cbError.setSelected(errorVal);
        cbError.setStyle("-fx-text-fill: red;");

        CheckBox cbFatal = new CheckBox("Fatal");
        cbFatal.setSelected(fatalVal);
        cbFatal.setStyle("-fx-text-fill: darkred; -fx-font-weight: bold;");

        // Layout: GridPane for organized 2-column view
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        // Column 1
        grid.add(cbLog, 0, 0);
        grid.add(cbInfo, 0, 1);
        grid.add(cbDebug, 0, 2);

        // Column 2
        grid.add(cbWarn, 1, 0);
        grid.add(cbError, 1, 1);
        grid.add(cbFatal, 1, 2);

        // Container for settings with a border
        VBox settingsBox = new VBox(10, headerLabel, grid);
        settingsBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #fafafa; -fx-padding: 15;");

        // --- 3. Save & Close Buttons ---

        Button saveBtn = new Button("Save Configuration");
        saveBtn.setStyle("-fx-base: #b6e7c9; -fx-font-weight: bold;");
        saveBtn.setDefaultButton(true); // Trigger on Enter key

        saveBtn.setOnAction(e -> {
            if (configXml != null) {
                try {
                    // Save values back to XML
                    configXml.setBool(getLogString, cbLog.isSelected());
                    configXml.setBool(getLogInfoString, cbInfo.isSelected());
                    configXml.setBool(getLogWarnString, cbWarn.isSelected());
                    configXml.setBool(getLogDebugString, cbDebug.isSelected());
                    configXml.setBool(getLogErrorString, cbError.isSelected());
                    configXml.setBool(getLogFatalString, cbFatal.isSelected());

                    configXml.save();

                    infoLabel.setText("Logging settings saved!");
                    popupStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    infoLabel.setText("Error saving logging settings!");
                }
            } else {
                System.out.println("ConfigXML is null, cannot save.");
                popupStage.close();
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> popupStage.close());

        HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        // --- 4. Final Scene Setup ---
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(settingsBox, buttonBox);

        Scene popupScene = new Scene(mainLayout, 350, 280);
        popupStage.setScene(popupScene);
        popupStage.show();
    }

    private void openOutputPopup(Label infoLabel, ConfigXml configXml) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Run Details Configuration");
        // Set the owner to prevent the popup from being an orphan in the taskbar
        popupStage.initOwner(infoLabel.getScene().getWindow());

        // --- 1. Load Initial Values from ConfigXml ---
        // Using default values if configXml is null or values are missing
        String[] request = {ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.OUTPUT_TAG,
                ConfigConstants.WRITE_OUTPUT_TAG};
        String getWriteOutput = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean writeOutputVal = configXml == null || configXml.getBool(getWriteOutput);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.OUTPUT_TAG,
                ConfigConstants.FILE_TAG};
        String getOutputFile = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        String outputFileVal = configXml.getValue(getOutputFile);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.OUTPUT_TAG,
                ConfigConstants.TYPE_TAG};
        String getOutputType = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        String outputTypeVal = configXml.getValue(getOutputType);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.OUTPUT_TAG,
                ConfigConstants.CSV_SEPARATOR_TAG};
        String getCsvSeparator = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        String csvSeparatorVal = configXml.getValue(getCsvSeparator);

        // Loading 'whatToWrite' flags (assuming getter methods exist in your ConfigXml class)
        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.OUTPUT_TAG,
                ConfigConstants.WHAT_TO_WRITE_TAG, ConfigConstants.SIMULATION_DETAILS_TAG};
        String getSimDetails = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean simDetailsVal = configXml.getBool(getSimDetails);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.OUTPUT_TAG,
                ConfigConstants.WHAT_TO_WRITE_TAG, ConfigConstants.SIMULATION_TIME_TAG};
        String getSimTime = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean simTimeVal = configXml.getBool(getSimTime);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.OUTPUT_TAG,
                ConfigConstants.WHAT_TO_WRITE_TAG, ConfigConstants.CARS_PASSED_TAG};
        String getCarsPassed = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean carsPassedVal = configXml.getBool(getCarsPassed);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.OUTPUT_TAG,
                ConfigConstants.WHAT_TO_WRITE_TAG, ConfigConstants.CARS_ON_ROAD_TAG};
        String getCarsOnRoad = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean carsOnRoadVal = configXml.getBool(getCarsOnRoad);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.OUTPUT_TAG,
                ConfigConstants.WHAT_TO_WRITE_TAG, ConfigConstants.ROAD_DETAILS_TAG};
        String getRoadDetails = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean roadDetailsVal = configXml.getBool(getRoadDetails);

        request = new String[]{ConfigConstants.RUN_DETAILS_TAG, ConfigConstants.OUTPUT_TAG,
                ConfigConstants.WHAT_TO_WRITE_TAG, ConfigConstants.GENERATION_DETAILS_TAG};
        String getGenDetails = String.join(ConfigConstants.CONFIG_REQUEST_SEPARATOR, request);
        boolean genDetailsVal = configXml.getBool(getGenDetails);

        // --- 2. Create GUI Components ---

        // Main Toggle
        CheckBox cbWriteOutput = new CheckBox("Enable File Output");
        cbWriteOutput.setSelected(writeOutputVal);
        cbWriteOutput.setStyle("-fx-font-weight: bold;");

        // File Settings
        Label lblFile = new Label("File Path:");
        TextField tfOutputFile = new TextField(outputFileVal);
        tfOutputFile.setPrefWidth(200);

        Label lblType = new Label("Format:");
        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("txt", "csv");
        cbType.setValue(outputTypeVal);

        Label lblSeparator = new Label("CSV Separator:");
        TextField tfSeparator = new TextField(csvSeparatorVal);
        tfSeparator.setPrefWidth(50);

        // Logic: Disable separator input if format is NOT csv
        tfSeparator.setDisable(!"csv".equals(outputTypeVal));
        cbType.setOnAction(e -> tfSeparator.setDisable(!"csv".equals(cbType.getValue())));

        // Layout for File Settings
        GridPane fileSettingsGrid = new GridPane();
        fileSettingsGrid.setHgap(10);
        fileSettingsGrid.setVgap(10);
        fileSettingsGrid.add(lblFile, 0, 0);
        fileSettingsGrid.add(tfOutputFile, 1, 0, 3, 1); // Span 3 columns
        fileSettingsGrid.add(lblType, 0, 1);
        fileSettingsGrid.add(cbType, 1, 1);
        fileSettingsGrid.add(lblSeparator, 2, 1);
        fileSettingsGrid.add(tfSeparator, 3, 1);

        // 'What To Write' Flags
        Label lblWhatToWrite = new Label("Data to Include:");
        lblWhatToWrite.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        CheckBox cbSimDetails = new CheckBox("Simulation Details");
        cbSimDetails.setSelected(simDetailsVal);

        CheckBox cbSimTime = new CheckBox("Simulation Time");
        cbSimTime.setSelected(simTimeVal);

        CheckBox cbCarsPassed = new CheckBox("Cars Passed");
        cbCarsPassed.setSelected(carsPassedVal);

        CheckBox cbCarsOnRoad = new CheckBox("Cars On Road");
        cbCarsOnRoad.setSelected(carsOnRoadVal);

        CheckBox cbRoadDetails = new CheckBox("Road Details");
        cbRoadDetails.setSelected(roadDetailsVal);

        CheckBox cbGenDetails = new CheckBox("Generation Details");
        cbGenDetails.setSelected(genDetailsVal);

        // Layout for Flags (2 columns)
        GridPane flagsGrid = new GridPane();
        flagsGrid.setHgap(20);
        flagsGrid.setVgap(10);
        flagsGrid.add(cbSimDetails, 0, 0);
        flagsGrid.add(cbSimTime, 1, 0);
        flagsGrid.add(cbCarsPassed, 0, 1);
        flagsGrid.add(cbCarsOnRoad, 1, 1);
        flagsGrid.add(cbRoadDetails, 0, 2);
        flagsGrid.add(cbGenDetails, 1, 2);

        // Wrapper for all output settings (to easily disable them if main toggle is off)
        VBox settingsContainer = new VBox(10, fileSettingsGrid, new Separator(), lblWhatToWrite, flagsGrid);
        settingsContainer.setPadding(new Insets(10));
        settingsContainer.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #fafafa;");

        // Logic: Disable entire settings container if "Enable File Output" is unchecked
        settingsContainer.disableProperty().bind(cbWriteOutput.selectedProperty().not());

        // --- 3. Save & Close Buttons ---

        Button saveBtn = new Button("Save Configuration");
        saveBtn.setStyle("-fx-base: #b6e7c9; -fx-font-weight: bold;");
        saveBtn.setDefaultButton(true); // Trigger on Enter key

        saveBtn.setOnAction(e -> {
            if (configXml != null) {
                try {
                    // Save Main Output Settings
                    configXml.setBool(getWriteOutput, cbWriteOutput.isSelected());
                    configXml.setValue(getOutputFile, tfOutputFile.getText().trim());
                    configXml.setValue(getOutputType, cbType.getValue());
                    configXml.setValue(getCsvSeparator, tfSeparator.getText().trim());
                    // Save 'What To Write' Flags
                    configXml.setBool(getSimDetails, cbSimDetails.isSelected());
                    configXml.setBool(getSimTime, cbSimTime.isSelected());
                    configXml.setBool(getCarsPassed, cbCarsPassed.isSelected());
                    configXml.setBool(getCarsOnRoad, cbCarsOnRoad.isSelected());
                    configXml.setBool(getRoadDetails, cbRoadDetails.isSelected());
                    configXml.setBool(getGenDetails, cbGenDetails.isSelected());

                    configXml.save();

                    // Update info label in main window
                    infoLabel.setText("Configuration saved successfully!");
                    popupStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    infoLabel.setText("Error saving configuration!");
                }
            } else {
                System.out.println("ConfigXML is null, cannot save.");
                popupStage.close();
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> popupStage.close());

        HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        // --- 4. Final Layout Composition ---
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(
                cbWriteOutput,
                settingsContainer,
                buttonBox
        );

        Scene popupScene = new Scene(mainLayout, 450, 400); // Adjusted height for more content
        popupStage.setScene(popupScene);
        popupStage.show();
    }

    private void openSimulationSettingsPopup(Label infoLabel, ConfigXml configXml) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Simulation Settings");
        // Set the owner to keep the window on top of the main application
        popupStage.initOwner(infoLabel.getScene().getWindow());

        // --- 1. Load Initial Values ---
        // Using default values in case configXml is null or tags are missing
        /*boolean showGuiVal    = configXml != null ? configXml.getRunDetailBool("showGui") : true;
        boolean drawCellsVal  = configXml != null ? configXml.getRunDetailBool("drawCells") : false;
        boolean laneChangeVal = configXml != null ? configXml.getRunDetailBool("laneChange") : true;
        boolean debugVal      = configXml != null ? configXml.getRunDetailBool("debug") : false;
       */ boolean showGuiVal = true;
        boolean drawCellsVal = false;
        boolean laneChangeVal = true;
        boolean debugVal = false;

        // --- 2. Create GUI Components ---

        Label headerLabel = new Label("Runtime Flags:");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // CheckBoxes
        CheckBox cbShowGui = new CheckBox("Show GUI");
        cbShowGui.setSelected(showGuiVal);
        cbShowGui.setTooltip(new Tooltip("Toggle the graphical user interface"));

        CheckBox cbDrawCells = new CheckBox("Draw Cells");
        cbDrawCells.setSelected(drawCellsVal);
        cbDrawCells.setTooltip(new Tooltip("Visualize grid cells (performance heavy)"));

        CheckBox cbLaneChange = new CheckBox("Lane Change");
        cbLaneChange.setSelected(laneChangeVal);
        cbLaneChange.setTooltip(new Tooltip("Enable vehicle lane changing logic"));

        CheckBox cbDebug = new CheckBox("Debug Mode");
        cbDebug.setSelected(debugVal);
        cbDebug.setStyle("-fx-text-fill: #555;"); // Slightly different color for debug

        // Layout: 2x2 Grid for a clean look
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        grid.add(cbShowGui, 0, 0);
        grid.add(cbDrawCells, 1, 0);
        grid.add(cbLaneChange, 0, 1);
        grid.add(cbDebug, 1, 1);

        // Container styling
        VBox settingsBox = new VBox(10, headerLabel, grid);
        settingsBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #fafafa; -fx-padding: 15;");

        // --- 3. Buttons (Save & Cancel) ---

        Button saveBtn = new Button("Save Settings");
        saveBtn.setStyle("-fx-base: #b6e7c9; -fx-font-weight: bold;");
        saveBtn.setDefaultButton(true);

        saveBtn.setOnAction(e -> {
            if (configXml != null) {
                try {
                    // Save values to XML
                  /*  configXml.setRunDetailBool("showGui", cbShowGui.isSelected());
                    configXml.setRunDetailBool("drawCells", cbDrawCells.isSelected());
                    configXml.setRunDetailBool("laneChange", cbLaneChange.isSelected());
                    configXml.setRunDetailBool("debug", cbDebug.isSelected());

                    configXml.save();*/

                    infoLabel.setText("Simulation settings saved!");
                    popupStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    infoLabel.setText("Error saving settings!");
                }
            } else {
                System.out.println("ConfigXML is null.");
                popupStage.close();
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> popupStage.close());

        HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        // --- 4. Final Scene ---
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(settingsBox, buttonBox);

        Scene popupScene = new Scene(mainLayout, 320, 250);
        popupStage.setScene(popupScene);
        popupStage.show();
    }
}
