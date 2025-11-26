package ui;

import app.AppContext;
import core.engine.CoreEngine;
import core.model.Road;
import core.model.cellular.Cell;
import core.model.cellular.CellularRoad;
import core.utils.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ui.render.IRoadRenderer;
import core.sim.Simulation;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.LinkedList;

/********************************************
 * Main window for traffic simulator GUI, javaFX is used
 *
 * @author Michael Hladky
 * @version 1.1
 ********************************************/
public class Window extends Application {

    /** simulation for stepping **/
    private Simulation simulation;

    /** core engine for running simulation **/
    private CoreEngine engine;

    /** renderer for drawing roads **/
    private IRoadRenderer renderer;

    // Scrollbars added to class scope to be accessible
    private ScrollBar hScroll;
    private ScrollBar vScroll;
    private Label infoLabel; // Label for top info text

    private ConfigXml configXml;

    /**
     * start method for JavaFX application
     *
     * @param primaryStage primary stage for JavaFX application
     **/
    @Override
    public void start(Stage primaryStage) {
        // get renderer and simulation from AppContext
        this.simulation = AppContext.SIMULATION;
        this.renderer = AppContext.RENDERER;

        // canvas for drawing
        Canvas canvas = new Canvas();
        Pane canvasPane = new Pane(canvas);
        canvas.widthProperty().bind(canvasPane.widthProperty()); // this shit binds that graphics needs to only allocate
        canvas.heightProperty().bind(canvasPane.heightProperty()); // stuff we see

        // Scrollbars for navigation
        hScroll = new ScrollBar();
        hScroll.setOrientation(Orientation.HORIZONTAL);
        vScroll = new ScrollBar();
        vScroll.setOrientation(Orientation.VERTICAL);

        // Info label at the top (moved from canvas drawing)
        infoLabel = new Label("Initializing...");
        infoLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        infoLabel.setPadding(new Insets(5, 10, 5, 10));

        final String START_BUTTON_TEXT = "Start";
        final String STOP_BUTTON_TEXT = "Stop";
        final String NEXT_STEP_BUTTON_TEXT = "Next step";
        final String EXPORT_BUTTON_TEXT = "Export results";
        final String STATUS_RUNNING_BUTTON_TEXT = "State: running";
        final String STATUS_STOPPED_BUTTON_TEXT = "State: stopped";
        final String WINDOW_TEXT = "Traffic simulator";

        // panel with controls
        Button btnStart = new Button(START_BUTTON_TEXT);
        Button btnStop = new Button(STOP_BUTTON_TEXT);
        Button btnStep = new Button(NEXT_STEP_BUTTON_TEXT);
        Label statusLabel = new Label(STATUS_STOPPED_BUTTON_TEXT);
        Button exportBtn = new Button(EXPORT_BUTTON_TEXT);

        HBox controls = new HBox(10, btnStart, exportBtn, btnStop, btnStep, statusLabel);
        controls.setPadding(new Insets(10));

        // VBox to hold scrollbar and controls at the bottom
        VBox bottomLayout = new VBox(hScroll, controls);

        // Configuration bar

        TabPane configTabs = new TabPane();
        configTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // aby nešly zavírat

        Tab generatorTab    = createGeneratorTab();
        Tab runDetailsTab   = createRunDetailsTab();
        Tab modelsTab       = createModelsTab();
        Tab roadsTab        = createRoadsTab();

        configTabs.getTabs().addAll(generatorTab, runDetailsTab, modelsTab, roadsTab);

// top: info label + konfigurační taby
        VBox topPane = new VBox(infoLabel, configTabs);
        topPane.setSpacing(5);


        ////////////////////////////////////////


        // main layout of gui
        BorderPane root = new BorderPane();
        root.setTop(topPane);
       // root.setTop(infoLabel);      // Added info label to top
        root.setCenter(canvasPane);  // Changed to Pane with Canvas
        root.setRight(vScroll);      // Added vertical scrollbar
        root.setBottom(bottomLayout);// Controls + Horizontal Scrollbar

        Scene scene = new Scene(root, 1200, 700, Color.LIGHTGRAY);
        primaryStage.setScene(scene);
        primaryStage.setTitle(WINDOW_TEXT);
        primaryStage.show();

        // part that repaints all roads
        Runnable paintAll = () -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            Road[] roads = simulation.getRoads();

            // Update info string in the top label instead of drawing on canvas
            String infoString = "Forward model used: " + AppContext.CAR_FOLLOWING_MODEL.getName() +
                    " | lane changing model used: " + AppContext.LANE_CHANGING_MODEL.getName() + " | time steps: " +
                    simulation.getStepCount();
            Platform.runLater(() -> infoLabel.setText(infoString));

            if (roads == null || roads.length == 0) {
                MyLogger.log("Error while getting roads to render", Constants.ERROR_FOR_LOGGING);
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                return;
            }

            final double GAP = 20.0;           // gap between roads when drawing
            Object content = roads[0].getContent();

            // Determine what to draw
            if (content instanceof Cell[][]) {
                this.handleCellular(roads, canvas, gc, GAP);
            } else if (content instanceof LinkedList[]) {
                this.handleContinuous(roads, canvas, gc, GAP);
            }
        };

        // Listeners for scrollbars to repaint when scrolling
        hScroll.valueProperty().addListener(o -> paintAll.run());
        vScroll.valueProperty().addListener(o -> paintAll.run());

        // tick of engine - one simulation step and repaint
        Runnable tick = () -> {
            simulation.step();
            Platform.runLater(paintAll);
        };

        // engine initialization
        engine = new CoreEngine(tick, AppContext.RUN_DETAILS.timeBetweenSteps); // 1000 ms per step

        // starting engine when start button is pressed, simulation runs
        btnStart.setOnAction(e -> {
            engine.start();
            statusLabel.setText(STATUS_RUNNING_BUTTON_TEXT);
        });

        // stopping engine when stop button is pressed, simulation stops
        btnStop.setOnAction(e -> {
            engine.stop();
            statusLabel.setText(STATUS_STOPPED_BUTTON_TEXT);
        });

        // one simulation step when step button is pressed, manual step
        btnStep.setOnAction(e -> {
            simulation.step();
            paintAll.run();
        });

        // export results when export button is pressed
        exportBtn.setOnAction(e -> ResultsRecorder.getResultsRecorder().writeResults());

        // first paint
        paintAll.run();
    }

    /*
     * handles drawing of cellular roads
     *
     * @param roads array of roads to draw
     * @param canvas place to draw on
     * @param gc graphics context to draw on
     * @param GAP space between roads when drawing
     */
    private void handleCellular(Road[] roads, Canvas canvas, GraphicsContext gc, final double GAP) {
        final double CELL_PIXEL_SIZE = 8.0; // size of cell for ONLY DRAWING, CELL SIZE IN MODEL MAY, AND LIKELY IS
        // DIFFERENT

        double neededHeight = GAP;
        double neededWidth  = 0;

        for (Road r : roads) {
            CellularRoad cr = (CellularRoad) r;
            int lanes = 1;
            int cols = 1;

            if (cr.getLength() > 0 && cr.getNumberOfLanes() > 0) {
                lanes = cr.getNumberOfLanes();
                cols = (int) (cr.getLengthInCells());
            }

            neededHeight += lanes * CELL_PIXEL_SIZE + GAP;
            neededWidth = Math.max(neededWidth, cols * CELL_PIXEL_SIZE);
        }

        this.drawRoads(canvas, gc, roads, GAP, CELL_PIXEL_SIZE, neededWidth, neededHeight);
    }

    /*
     * handles drawing of continuous roads
     *
     * @param roads array of roads to draw
     * @param canvas place to draw on
     * @param gc graphics context to draw on
     * @param GAP space between roads when drawing
     */
    private void handleContinuous(Road[] roads, Canvas canvas, GraphicsContext gc, final double GAP) {
        final double LANE_WIDTH = 8.0; // size of lane

        double neededHeight = GAP;
        double neededWidth  = 0;

        for (Road r : roads) {
            int lanes = 1;
            int length = 1;

            if (r.getLength() > 0 && r.getNumberOfLanes() > 0) {
                lanes = r.getNumberOfLanes();
                length = (int) (r.getLength() * Constants.CONTINOUS_ROAD_DRAWING_SCALE_FACTOR);
            }

            neededHeight += lanes * LANE_WIDTH + GAP;
            neededWidth = Math.max(neededWidth, length);
        }

        this.drawRoads(canvas, gc, roads, GAP, LANE_WIDTH, neededWidth, neededHeight);
    }

    private Tab createGeneratorTab() {
        Tab tab = new Tab("Generator");

        double currentFlowRate = AppContext.SIMULATION.getFlowRate();
        boolean queueUse = false;

        if (configXml != null) {
            currentFlowRate = configXml.getFlowRate();
            queueUse = configXml.isQueueUsed();
        }

        Label flowLabel = new Label("Flow rate:");
        Label flowValueLabel = new Label(String.format("%.2f", currentFlowRate));

        Slider flowSlider = new Slider(0.0, 1.0, currentFlowRate);
        flowSlider.setShowTickMarks(true);
        flowSlider.setShowTickLabels(true);
        flowSlider.setMajorTickUnit(0.25);
        flowSlider.setBlockIncrement(0.01);

        // when value of slider changes, update label
        flowSlider.valueProperty().addListener((obs, oldV, newV) ->
                flowValueLabel.setText(String.format("%.2f", newV.doubleValue()))
        );

        // všechno pro flowRate v jedné horizontální skupině
        HBox flowBox = new HBox(5, flowLabel, flowSlider, flowValueLabel);
        flowBox.setAlignment(Pos.CENTER_LEFT);

        CheckBox queueUseCheck = new CheckBox("Use queue");
        queueUseCheck.setSelected(queueUse);

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            if (configXml != null) {
                configXml.setFlowRate(flowSlider.getValue());
                configXml.setQueueUsed(queueUseCheck.isSelected());
                try {
                    configXml.save();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // finální řada pro celý tab – pořád jen jeden „proužek“
        HBox row = new HBox(20, flowBox, queueUseCheck, saveBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 10, 10, 10));

        tab.setContent(row);
        return tab;
    }

    private Tab createRunDetailsTab() {
        Tab tab = new Tab("Run details");

        // Tlačítko pro otevření nastavení
        Button openSettingsBtn = new Button("Open Configuration...");

        // Informativní label (aby uživatel viděl základní info bez otevírání okna)
        String dur = "1000";
        String step = "1";
        Label infoSummary = new Label("Duration: " + dur + " ms | Time step: " + step);

        // Akce tlačítka -> otevře popup
        openSettingsBtn.setOnAction(e -> openRunDetailsPopup(infoSummary));

        // Jednoduchý horizontální layout (stejný styl jako Generator tab)
        HBox strip = new HBox(15, openSettingsBtn, infoSummary);
        strip.setAlignment(Pos.CENTER_LEFT);
        strip.setPadding(new Insets(10));

        tab.setContent(strip);
        return tab;
    }

    private void openRunDetailsPopup(Label summaryLabelToUpdate) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Run Details Configuration");
        popupStage.initOwner(infoLabel.getScene().getWindow()); // Nastaví hlavní okno jako vlastníka (aby to nebylo taskbar orphan)

        // --- Načtení hodnot (stejné jako předtím) ---
        // Používám dummy hodnoty, pokud configXml není (pro testování)
     /*   boolean showGuiVal = configXml != null ? configXml.getRunDetailBool("showGui") : true;
        boolean drawCellsVal = configXml != null ? configXml.getRunDetailBool("drawCells") : false;
        boolean laneChangeVal = configXml != null ? configXml.getRunDetailBool("laneChange") : true;
        boolean debugVal = configXml != null ? configXml.getRunDetailBool("debug") : false;

        String timeStepVal = configXml != null ? configXml.getRunDetailString("timeStep") : "1";
        String durationVal = configXml != null ? configXml.getRunDetailString("duration") : "1000";
        String timeBetweenVal = configXml != null ? configXml.getRunDetailString("timeBetweenSteps") : "100";

        boolean writeOutputVal = configXml != null ? configXml.getOutputBool("writeOutput") : true;
        String outputFileVal = configXml != null ? configXml.getOutputString("file") : "output/out.txt";*/

        boolean showGuiVal = true;
        boolean drawCellsVal = false;
        boolean laneChangeVal = true;
        boolean debugVal = false;

        String timeStepVal = "1";
        String durationVal = "1000";
        String timeBetweenVal = "100";

        boolean writeOutputVal = true;
        String outputFileVal = "output/out.txt";

        // --- Tvorba GUI prvků ---

        CheckBox cbShowGui = new CheckBox("Show GUI");
        cbShowGui.setSelected(showGuiVal);

        CheckBox cbDrawCells = new CheckBox("Draw Cells");
        cbDrawCells.setSelected(drawCellsVal);

        CheckBox cbLaneChange = new CheckBox("Lane Change");
        cbLaneChange.setSelected(laneChangeVal);

        CheckBox cbDebug = new CheckBox("Debug Mode");
        cbDebug.setSelected(debugVal);

        GridPane booleanGrid = new GridPane();
        booleanGrid.setHgap(15);
        booleanGrid.setVgap(10);
        booleanGrid.add(cbShowGui, 0, 0);
        booleanGrid.add(cbDrawCells, 1, 0);
        booleanGrid.add(cbLaneChange, 0, 1);
        booleanGrid.add(cbDebug, 1, 1);

        TextField tfTimeStep = new TextField(timeStepVal);
        TextField tfDuration = new TextField(durationVal);
        TextField tfTimeBetween = new TextField(timeBetweenVal);

        GridPane timingGrid = new GridPane();
        timingGrid.setHgap(10);
        timingGrid.setVgap(5);
        timingGrid.addRow(0, new Label("Time Step:"), tfTimeStep);
        timingGrid.addRow(1, new Label("Duration:"), tfDuration);
        timingGrid.addRow(2, new Label("Time Between (ms):"), tfTimeBetween);

        CheckBox cbWriteOutput = new CheckBox("Write Output");
        cbWriteOutput.setSelected(writeOutputVal);

        TextField tfOutputFile = new TextField(outputFileVal);
        tfOutputFile.setPrefWidth(250);

        VBox outputBox = new VBox(5, cbWriteOutput, new Label("Output File:"), tfOutputFile);
        outputBox.setStyle("-fx-border-color: #ddd; -fx-padding: 5; -fx-border-radius: 5;");

        // --- Tlačítka dole (Uložit a Zavřít) ---
        Button saveBtn = new Button("Save & Close");
        saveBtn.setStyle("-fx-base: #b6e7c9; -fx-font-weight: bold;");
        saveBtn.setDefaultButton(true); // Reaguje na Enter

        saveBtn.setOnAction(e -> {
            if (configXml != null) {
                try {
                    // Uložení booleanů
                /*    configXml.setRunDetailBool("showGui", cbShowGui.isSelected());
                    configXml.setRunDetailBool("drawCells", cbDrawCells.isSelected());
                    configXml.setRunDetailBool("laneChange", cbLaneChange.isSelected());
                    configXml.setRunDetailBool("debug", cbDebug.isSelected());

                    // Uložení čísel
                    configXml.setRunDetailString("timeStep", tfTimeStep.getText());
                    configXml.setRunDetailString("duration", tfDuration.getText());
                    configXml.setRunDetailString("timeBetweenSteps", tfTimeBetween.getText());

                    // Uložení Output
                    configXml.setOutputBool("writeOutput", cbWriteOutput.isSelected());
                    configXml.setOutputString("file", tfOutputFile.getText());

                    configXml.save();*/

                    // Aktualizace infa v hlavním okně
                    summaryLabelToUpdate.setText("Duration: " + tfDuration.getText() + " ms | Time step: " + tfTimeStep.getText());
                    infoLabel.setText("Konfigurace uložena!");

                    // Zavření okna po uložení
                    popupStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    infoLabel.setText("Chyba při ukládání!");
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

        // --- Layout Popupu ---
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(
                new Label("Simulation Flags"), booleanGrid,
                new Separator(),
                new Label("Timing Settings"), timingGrid,
                new Separator(),
                new Label("Output Settings"), outputBox,
                new Separator(),
                buttonBox
        );

        Scene popupScene = new Scene(mainLayout, 400, 500);
        popupStage.setScene(popupScene);
        popupStage.show(); // nebo .showAndWait() pokud chceš blokovat hlavní okno
    }


    private Tab createModelsTab() {
        Tab tab = new Tab("Models");

        ComboBox<String> cfModel = new ComboBox<>();
        cfModel.getItems().addAll("gipps", "ovm", "fvdm", "idm");
        cfModel.setValue("gipps");
        cfModel.setPrefWidth(120);

        ComboBox<String> lcModel = new ComboBox<>();
        lcModel.getItems().addAll("mobil", "mobil-simple");
        lcModel.setValue("mobil");
        lcModel.setPrefWidth(120);

        Button saveBtn = new Button("Uložit");

        VBox cfBox = new VBox(new Label("Car-following:"), cfModel);
        VBox lcBox = new VBox(new Label("Lane-changing:"), lcModel);

        cfBox.setSpacing(3);
        lcBox.setSpacing(3);

        HBox row = new HBox(20,
                cfBox,
                lcBox,
                saveBtn
        );

        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 10, 10, 10));

        tab.setContent(row);
        return tab;
    }

    private Tab createRoadsTab() {
        Tab tab = new Tab("Roads");

        int numberOfRoads = AppContext.SIMULATION.getRoads().length;
        Spinner<Integer> roadsSpinner = new Spinner<>(1, 1000000, numberOfRoads);
        roadsSpinner.setPrefWidth(80);

        TextField roadFileField = new TextField("config/roads/road1.xml");
        roadFileField.setPrefWidth(200);

        Button saveBtn = new Button("Uložit");

        VBox roadsBox = new VBox(new Label("Number of roads:"), roadsSpinner);
        VBox fileBox = new VBox(new Label("Road file:"), roadFileField);

        roadsBox.setSpacing(3);
        fileBox.setSpacing(3);

        HBox row = new HBox(20,
                roadsBox,
                fileBox,
                saveBtn
        );

        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 10, 10, 10));

        tab.setContent(row);
        return tab;
    }

    /*
     * draws all roads on the canvas
     *
     * @param canvas place to draw on
     * @param gc graphics context to draw on
     * @param roads array of roads to draw
     * @param GAP space between roads when drawing
     * @param CELL_PIXEL_SIZE size of cell or lane for drawing
     * @param neededWidth needed width of canvas
     * @param neededHeight needed height of canvas
     */
    private void drawRoads(Canvas canvas, GraphicsContext gc, Road[] roads, final double GAP,
                           final double CELL_PIXEL_SIZE, double neededWidth, double neededHeight) {

        // Setup Scrollbars based on content size
        double viewportW = canvas.getWidth();
        double viewportH = canvas.getHeight();

        double maxScrollH = Math.max(0, neededWidth - viewportW);
        double maxScrollV = Math.max(0, neededHeight - viewportH);

        hScroll.setMax(maxScrollH);
        hScroll.setVisibleAmount(viewportW);

        vScroll.setMax(maxScrollV);
        vScroll.setVisibleAmount(viewportH);

        // Get camera position
        double camX = hScroll.getValue();
        double camY = vScroll.getValue();

        // Clear Screen
        gc.clearRect(0, 0, viewportW, viewportH);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, viewportW, viewportH);

        // Translate camera
        gc.save();
        gc.translate(-camX, -camY);

        double y = GAP;
        int i = 0;
        for (Road road : roads) {
            int lanes = road.getNumberOfLanes();
            double roadHeight = lanes * CELL_PIXEL_SIZE;

            // Optimization: Don't draw if road is outside vertical view
            if (y + roadHeight < camY || y > camY + viewportH) {
                y += roadHeight + GAP;
                i++;
                continue;
            }

            // info string (kept per road)
            String info = "Road: " + i + " | Lanes: " + lanes + " | Length: " + road.getLength() + " | Cars on road: " +
                    road.getNumberOfCarsOnRoad() + " | Cars passed: " +
                    ResultsRecorder.getResultsRecorder().getCarsPassedOnRoad(i);
            gc.setFill(Color.BLACK);
            gc.fillText(info, 0, y - 5);
            gc.save();
            gc.translate(0, y);

            // Use max of neededWidth to ensure renderer doesn't cut off drawing loop
            synchronized (road) {
                renderer.draw(gc, road, Math.max(neededWidth, viewportW), roadHeight, CELL_PIXEL_SIZE);
            }
            gc.restore();

            y += roadHeight + GAP;
            i++;
        }
        gc.restore(); // Restore translation
    }

    /**
     * stop method for JavaFX application
     **/
    @Override
    public void stop() {
        if (engine != null && engine.getRunning()) {
            engine.stop();
        }
        Platform.exit();
    }

    /**
     * main method to launch JavaFX application
     *
     * @param args command line arguments
     **/
    public static void main(String[] args) {
        launch(args);
    }
}