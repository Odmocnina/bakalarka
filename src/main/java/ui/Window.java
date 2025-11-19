package ui;

import app.AppContext;
import core.engine.CoreEngine;
import core.model.Road;
import core.model.cellular.Cell;
import core.model.cellular.CellularRoad;
import core.utils.Constants;
import core.utils.MyLogger;
import core.utils.ResultsRecorder;
import ui.render.IRoadRenderer;
import core.sim.Simulation;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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

        // main layout of gui
        BorderPane root = new BorderPane();
        root.setTop(infoLabel);      // Added info label to top
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