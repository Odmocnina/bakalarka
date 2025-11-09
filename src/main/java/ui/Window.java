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
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.LinkedList;

/********************************************
 * Main window for traffic simulator GUI, javaFX is used
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class Window extends Application {

    /** simulation for stepping **/
    private Simulation simulation;

    /** core engine for running simulation **/
    private CoreEngine engine;

    /** renderer for drawing roads **/
    private IRoadRenderer renderer;

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
        Canvas canvas = new Canvas(2000, 800);
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);

        final String startButtonText = "Start";
        final String stopButtonText = "Stop";
        final String stepButtonText = "Next step";
        final String exportButtonText = "Export results";
        final String statusRunningText = "State: running";
        final String statusStoppedText = "State: stopped";
        final String windowText = "Traffic simulator";

        // panel with controls
        Button btnStart = new Button(startButtonText);
        Button btnStop = new Button(stopButtonText);
        Button btnStep = new Button(stepButtonText);
        Label statusLabel = new Label(statusStoppedText);
        Button exportBtn = new Button(exportButtonText);

        HBox controls = new HBox(10, btnStart, exportBtn, btnStop, btnStep, statusLabel);
        controls.setPadding(new Insets(10));

        // main layout of gui
        BorderPane root = new BorderPane();
        root.setCenter(scrollPane);
        root.setBottom(controls);

        Scene scene = new Scene(root, 1200, 700, Color.LIGHTGRAY);
        primaryStage.setScene(scene);
        primaryStage.setTitle(windowText);
        primaryStage.show();

        // part that repaints all roads
        Runnable paintAll = () -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            Road[] roads = simulation.getRoads();
            if (roads == null || roads.length == 0) {
                MyLogger.log("Error while getting roads to render", Constants.ERROR_FOR_LOGGING);
                return;
            }

            final double GAP = 20.0;           // gap between roads when drawing
            Object content = roads[0].getContent();
            if (content instanceof Cell[][]) {
                this.handleCellular(roads, canvas, gc, GAP);
            } else if (content instanceof LinkedList[]) {
                this.handleContinuous(roads, canvas, gc, GAP);
            }

            String infoString = "Forward model used: " + AppContext.CAR_FOLLOWING_MODEL.getName() +
                    ", lane changing model used: " + AppContext.LANE_CHANGING_MODEL.getName();
            // draw info string under all roads
            gc.setFill(Color.BLACK);
            gc.fillText(infoString, 0, canvas.getHeight() - 5);
        };

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
            statusLabel.setText(statusRunningText);
        });

        // stopping engine when stop button is pressed, simulation stops
        btnStop.setOnAction(e -> {
            engine.stop();
            statusLabel.setText(statusStoppedText);
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
                length = (int) (r.getLength());
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
        canvas.setWidth(Math.max(neededWidth, canvas.getWidth()));
        canvas.setHeight(neededHeight);

        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double y = GAP;
        int i = 0;
        for (Road road : roads) {
            int lanes = road.getNumberOfLanes();
            double roadHeight = lanes * CELL_PIXEL_SIZE;

            // info string
            String info = "Road: " + i + ", Lanes: " + lanes + ", Length: " + road.getLength() + ", Cars on road: " +
                    road.getNumberOfCarsOnRoad() + ", Cars passed: " +
                    ResultsRecorder.getResultsRecorder().getCarsPassedOnRoad(i);
            gc.setFill(Color.BLACK);
            gc.fillText(info, 0, y - 5);
            gc.save();
            gc.translate(0, y);
            renderer.draw(gc, road, canvas.getWidth(), roadHeight, CELL_PIXEL_SIZE);
            gc.restore();

            y += roadHeight + GAP;
            i++;
        }
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
