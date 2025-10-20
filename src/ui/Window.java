package ui;

import app.AppContext;
import core.engine.Engine;
import core.model.Road;
import core.model.cellular.Cell;
import core.utils.Constants;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import ui.render.IRoadRenderer;

public class Window extends Application {

    // gap between roads
    private static final double GAP_Y = 16.0;

    // scale for continous roads
    private static final double PX_PER_M = 4.0;

    private Engine engine;

    @Override
    public void start(Stage primaryStage) {
        Road[] roads = AppContext.ROADS;                    // expects a array of roads
        IRoadRenderer renderer = AppContext.RENDERER;

        // cavas for drawing
        Canvas canvas = new Canvas();

        // dividing cavas for all roads
        layoutAndResizeCanvas(canvas, roads);

        // painter for all roads
        Runnable paintAll = () -> paintAllRoads(canvas, roads, renderer);

        // scroller
        ScrollPane scroller = new ScrollPane(canvas);
        scroller.setFitToWidth(false);
        scroller.setFitToHeight(false);
        scroller.setPannable(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // one tick of simulation
        Runnable tick = () -> {
            for (Road r : roads) {
                if (r != null) {
                    r.upadateRoad();
                }
            }
            paintAll.run();
        };

        // engine with 1 second interval
        engine = new Engine(tick, Duration.seconds(1));
        //engine = AppContext.ENGINE;

        // start/stop toggle button
        ToggleButton playPause = new ToggleButton("Start");
        playPause.setOnAction(e -> {
            if (playPause.isSelected()) {
                playPause.setText("Stop");
                engine.start();
            } else {
                playPause.setText("Start");
                engine.stop();
            }
        });

        // control for manual stepping
        Button button = new Button("Překreslit / další krok");
        button.setOnAction(e -> {
            // update all roads
            for (Road r : roads) {
                if (r != null) {
                    r.upadateRoad(); // update road to next step
                }
            }
            // if road sizes changed, relayout canvas, shouldnt happpen
            layoutAndResizeCanvas(canvas, roads);
            paintAll.run();
        });

        HBox top = new HBox(8, button, playPause);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(scroller);

        Scene scene = new Scene(root, Constants.CANVAS_WIDTH, Constants.CANVAS_HEIGHT + 40);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Více silnic pod sebou");
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(200);
        primaryStage.show();

        scroller.setHvalue(0);
        scroller.setVvalue(0);

        paintAll.run();
    }


    private void paintAllRoads(Canvas canvas, Road[] roads, IRoadRenderer renderer) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setLineDashes(null);

        double yOffset = 0.0;

        for (Road road : roads) {
            if (road == null) {
                continue;
            }

            // getting road size for road
            Size s = getRoadPixelSize(road);

            // draw offset for road
            gc.save();
            gc.translate(0, yOffset);
            renderer.draw(gc, road, s.w, s.h); //rederer get its own size
            gc.restore();

            yOffset += s.h + GAP_Y;
        }
    }

    private void layoutAndResizeCanvas(Canvas canvas, Road[] roads) {
        double maxW = 0.0;
        double sumH = 0.0;

        for (Road road : roads) {
            if (road == null) {
                continue;
            }
            Size s = getRoadPixelSize(road);
            maxW = Math.max(maxW, s.w);
            sumH += s.h + GAP_Y;
        }

        if (sumH > 0) {
            sumH -= GAP_Y;
        }

        if (maxW <= 0) {
            maxW = Constants.CANVAS_WIDTH;
        }
        if (sumH <= 0) {
            sumH = Constants.CANVAS_HEIGHT;
        }

        canvas.setWidth(maxW);
        canvas.setHeight(sumH);
    }

    private Size getRoadPixelSize(Road road) {
        if (Constants.CELLULAR.equals(road.getType())) {
            // same logic as in CellularRoadRenderer
            Object content = road.getContent();
            if (content instanceof Cell[][] cells && cells.length > 0 && cells[0].length > 0) {
                int lanes = cells.length;
                int cols  = cells[0].length;
                double baseCell = (AppContext.cellSize + 0.5);
                return new Size(cols * baseCell, lanes * baseCell);
            }
        } else if (Constants.CONTINOUS.equals(road.getType())) {
            // scale
            int lanes = road.getNumberOfLanes();
            double roadLenM = road.getLength();
            double laneH_M  = Constants.LANE_WIDTH;
            return new Size(roadLenM * PX_PER_M, lanes * laneH_M * PX_PER_M);
        }
        // fallback
        return new Size(Constants.CANVAS_WIDTH, Constants.CANVAS_HEIGHT);
    }

    private static class Size {
        final double w, h;
        Size(double w, double h) {
            this.w = w;
            this.h = h;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
