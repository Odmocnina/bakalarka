package ui;

import app.AppContext;
import core.engine.CoreEngine;
import core.model.Road;
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

public class Window extends Application {

    private Simulation simulation;
    private CoreEngine engine;
    private IRoadRenderer renderer;

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
        Button btnStart = new Button("Start");
        Button btnStop = new Button("Stop");
        Button btnStep = new Button("Další krok");
        Label statusLabel = new Label("Stav: zastaveno");

        HBox controls = new HBox(10, btnStart, btnStop, btnStep, statusLabel);
        controls.setPadding(new Insets(10));

        // main layout of gui
        BorderPane root = new BorderPane();
        root.setCenter(scrollPane);
        root.setBottom(controls);

        Scene scene = new Scene(root, 1200, 700, Color.LIGHTGRAY);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulátor dopravy");
        primaryStage.show();

        // part that repaints all roads
        Runnable paintAll = () -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight()); // clear canvas

            Road[] roads = simulation.getRoads();
            if (roads == null) {
                return;
            }

            double totalHeight = canvas.getHeight();
            double roadHeight = totalHeight / Math.max(roads.length, 1);

            for (int i = 0; i < roads.length; i++) {
                Road road = roads[i];
                if (road == null) { // if this happens, something is seriously fucked up
                    continue;
                }
                double yOffset = i * roadHeight;
                gc.save();
                gc.translate(0, yOffset);
                renderer.draw(gc, road, canvas.getWidth(), roadHeight); // draw road
                gc.restore();
            }
        };

        // tick of engine - one simulation step and repaint
        Runnable tick = () -> {
            simulation.step();
            Platform.runLater(paintAll);
        };

        // engine initialization
        engine = new CoreEngine(tick, 1000); // krok každých 500 ms

        // starting engine when start button is pressed, simulation runs
        btnStart.setOnAction(e -> {
            engine.start();
            statusLabel.setText("Stav: běží");
        });

        // stopping engine when stop button is pressed, simulation stops
        btnStop.setOnAction(e -> {
            engine.stop();
            statusLabel.setText("Stav: zastaveno");
        });

        // one simulation step when step button is pressed, manual step
        btnStep.setOnAction(e -> {
            simulation.step();
            paintAll.run();
            statusLabel.setText("Stav: krok proveden");
        });

        // first paint
        paintAll.run();
    }

    @Override
    public void stop() {
        if (engine != null && engine.isRunning()) {
            engine.stop();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
