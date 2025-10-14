package ui;

import app.AppContext;
import core.model.Road;
import core.model.cellular.CellularRoad;
import core.utils.Constants;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ui.render.IRoadRenderer;

public class Window extends Application {

    @Override
    public void start(Stage primaryStage) {
        Road road = AppContext.ROAD;
        IRoadRenderer renderer = AppContext.RENDERER;

        // vytvoření canvasu
        Canvas canvas = new Canvas(Constants.CANVAS_WIDTH, Constants.CANVAS_HEIGHT);

        // funkce pro překreslení
        Runnable paint = () -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setLineDashes(null); // reset pro jistotu
            renderer.draw(gc, road, canvas.getWidth(), canvas.getHeight());
        };

        // obalovací StackPane (umožní resize)
        StackPane centerWrapper = new StackPane(canvas);
        centerWrapper.setMinSize(0, 0); // důležité pro možnost zmenšování
        canvas.widthProperty().bind(centerWrapper.widthProperty());
        canvas.heightProperty().bind(centerWrapper.heightProperty());

        // posluchače na změnu velikosti → překreslení
        canvas.widthProperty().addListener((obs, o, n) -> paint.run());
        canvas.heightProperty().addListener((obs, o, n) -> paint.run());

        // tlačítko nahoře
        Button button = new Button("Překreslit");
        button.setOnAction(e -> this.nextSimulationStep(paint));

        // hlavní rozložení
        BorderPane root = new BorderPane();
        root.setTop(button);
        root.setCenter(centerWrapper);
        root.setMinSize(0, 0); // taky musí mít min size 0

        // scéna
        Scene scene = new Scene(root, Constants.CANVAS_WIDTH, Constants.CANVAS_HEIGHT + 40);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Silnice");

        // povolit malé minimum pro stage (aby šla zmenšovat)
        primaryStage.setMinWidth(200);
        primaryStage.setMinHeight(150);

        // zobrazit a první vykreslení
        primaryStage.show();
        paint.run();
    }

    private void nextSimulationStep(Runnable paint) {
        AppContext.ROAD.upadateRoad();
        // repaint
        paint.run();
    }

    public static void main(String[] args) {
        launch(args); // starts window
    }

}
