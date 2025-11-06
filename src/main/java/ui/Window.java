package ui;

import app.AppContext;
import core.engine.CoreEngine;
import core.model.Road;
import core.model.cellular.Cell;
import core.model.cellular.CellularRoad;
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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.LinkedList;

public class Window extends Application {

    private static final Logger logger = LogManager.getLogger(Window.class);
    private Simulation simulation;
    private CoreEngine engine;
    private IRoadRenderer renderer;
    private String startButtonText = "Start";
    private String stopButtonText = "Stop";
    private String stepButtonText = "Next step";
    private String exportButtonText = "Export results";
    private String statusRunningText = "State: running";
    private String statusStoppedText = "State: stopped";
    private String windowText = "Traffic simulator";

    @Override
    public void start(Stage primaryStage) {
        // get rederer and simulation from AppContext
        this.simulation = AppContext.SIMULATION;
        this.renderer = AppContext.RENDERER;

        // cavas for drawing
        Canvas canvas = new Canvas(2000, 800);
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);

        // panel with controls
        Button btnStart = new Button(this.startButtonText);
        Button btnStop = new Button(this.stopButtonText);
        Button btnStep = new Button(this.stepButtonText);
        Label statusLabel = new Label(this.statusStoppedText);
        Button exportBtn = new Button(this.exportButtonText);

        HBox controls = new HBox(10, btnStart, exportBtn, btnStop, btnStep, statusLabel);
        controls.setPadding(new Insets(10));

        // main layout of gui
        BorderPane root = new BorderPane();
        root.setCenter(scrollPane);
        root.setBottom(controls);

        Scene scene = new Scene(root, 1200, 700, Color.LIGHTGRAY);
        primaryStage.setScene(scene);
        primaryStage.setTitle(this.windowText);
        primaryStage.show();

        // part that repaints all roads
        Runnable paintAll = () -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            Road[] roads = simulation.getRoads();
            if (roads == null || roads.length == 0) {
                logger.error("Error while getting loads to render");
                return;
            }

            final double GAP = 20.0;           // mezera mezi silnicemi
            Object content = roads[0].getContent();
            if (content instanceof Cell[][]) {
                this.handleCellular(roads, canvas, gc, GAP);
            } else if (content instanceof LinkedList[]) {
                this.handleContinous(roads, canvas, gc, GAP);
            }
//            final double CELL_PIXEL_SIZE = 8.0; // pevná výška pruhu
//
//            double neededHeight = GAP;
//            double neededWidth  = 0;
//
//            for (Road r : roads) {
//                Object content = r.getContent();
//                int lanes = 1;
//                int cols = 1;
//
//                if (content instanceof Cell[][] cells) {
//                    if (cells.length > 0 && cells[0].length > 0) {
//                        lanes = cells.length;
//                        cols = cells[0].length;
//                    }
//                } else if (content instanceof LinkedList[]) {
//                    if (r.getNumberOfLanes() > 0) {
//                        lanes = r.getNumberOfLanes();
//                    }
//                }
//
//                neededHeight += lanes * CELL_PIXEL_SIZE + GAP;
//                neededWidth   = Math.max(neededWidth, cols * CELL_PIXEL_SIZE);
//            }
//
//            canvas.setWidth(Math.max(neededWidth, canvas.getWidth()));
//            canvas.setHeight(neededHeight);
//
//            gc.setFill(Color.LIGHTGRAY);
//            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//
//            double y = GAP;
//            for (Road road : roads) {
//                int lanes = (road.getContent() instanceof Cell[][] c) ? c.length : road.getNumberOfLanes();
//                double roadHeight = lanes * CELL_PIXEL_SIZE;
//
//                gc.save();
//                gc.translate(0, y);
//                renderer.draw(gc, road, canvas.getWidth(), roadHeight);
//                gc.restore();
//
//                y += roadHeight + GAP;
//            }

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
        engine = new CoreEngine(tick, 1000); // 1000 ms per step

        // starting engine when start button is pressed, simulation runs
        btnStart.setOnAction(e -> {
            engine.start();
            statusLabel.setText(this.statusRunningText);
        });

        // stopping engine when stop button is pressed, simulation stops
        btnStop.setOnAction(e -> {
            engine.stop();
            statusLabel.setText(this.statusStoppedText);
        });

        // one simulation step when step button is pressed, manual step
        btnStep.setOnAction(e -> {
            simulation.step();
            paintAll.run();
        });

        // export results when export button is pressed
        exportBtn.setOnAction(e -> {
            ResultsRecorder.getResultsRecorder().writeResults();
        });

        // first paint
        paintAll.run();
    }

    private void handleCellular(Road[] roads, Canvas canvas, GraphicsContext gc, final double GAP) {
        final double CELL_PIXEL_SIZE = 8.0; // size of cell for ONLY DRAWING, CELLSIZE IN MODEL MAY,
                                            // AND LIKELY IS DIFFERENT

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

    private void handleContinous(Road[] roads, Canvas canvas, GraphicsContext gc, final double GAP) {
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

        canvas.setWidth(Math.max(neededWidth, canvas.getWidth()));
        canvas.setHeight(neededHeight);

        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double y = GAP;
        int i = 0;
        for (Road road : roads) {
            int lanes = road.getNumberOfLanes();
            double roadHeight = lanes * LANE_WIDTH;

            // info string
            String info = "Road: " + i + ", Lanes: " + lanes + ", Length: " + road.getLength() + ", Cars on road: " +
                    road.getNumberOfCarsOnRoad() + ", Cars passed: " +
                    ResultsRecorder.getResultsRecorder().getCarsPassedOnRoad(i);
            gc.setFill(Color.BLACK);
            gc.fillText(info, 0, y - 5);
            gc.save();
            gc.translate(0, y);
            renderer.draw(gc, road, canvas.getWidth(), roadHeight, LANE_WIDTH);
            gc.restore();

            y += roadHeight + GAP;
            i++;
        }
    }

    @Override
    public void stop() {
        if (engine != null && engine.getRunning()) {
            engine.stop();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
