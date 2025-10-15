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
import javafx.scene.control.ScrollPane;
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

        // 1) Vytvoříme Canvas s "reálnými" rozměry silnice
        Canvas canvas = new Canvas();
        resizeCanvasToRoad(canvas, road); // <<< NOVÁ POMOCNÁ FUNKCE níže

        // 2) Kreslící funkce (žádné centrování, renderery si všimnou, že width/height == reálná velikost)
        Runnable paint = () -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setLineDashes(null);
            renderer.draw(gc, road, canvas.getWidth(), canvas.getHeight());
        };

        // 3) ScrollPane místo StackPane/bindingů
        ScrollPane scroller = new ScrollPane(canvas);
        scroller.setFitToWidth(false);
        scroller.setFitToHeight(false);
        scroller.setPannable(true);      // drag myší
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // 4) Tlačítko nahoře (ponecháno)
        Button button = new Button("Překreslit");
        button.setOnAction(e -> this.nextSimulationStep(paint, canvas, road, scroller));

        BorderPane root = new BorderPane();
        root.setTop(button);
        root.setCenter(scroller);

        Scene scene = new Scene(root, Constants.CANVAS_WIDTH, Constants.CANVAS_HEIGHT + 40);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Silnice");
        primaryStage.setMinWidth(200);
        primaryStage.setMinHeight(150);
        primaryStage.show();

        // po zobrazení skoč na horní levý roh
        scroller.setHvalue(0);
        scroller.setVvalue(0);

        paint.run();
    }

    private void nextSimulationStep(Runnable paint, Canvas canvas, Road road, ScrollPane scroller) {
        AppContext.ROAD.upadateRoad();
        // pokud by se někdy změnily rozměry silnice (počet pruhů / délka), přepočti canvas
        resizeCanvasToRoad(canvas, road);
        // držet nahoře (volitelné)
        // scroller.setVvalue(0);
        paint.run();
    }

    /**
     * Nastaví velikost canvasu podle typu silnice tak, aby renderery
     * počítaly scale=1 a offset=(0,0) → kreslení nahoře vlevo bez centrování.
     */
    private void resizeCanvasToRoad(Canvas canvas, Road road) {
        if (Constants.CELLULAR.equals(road.getType())) {
            // Cellular: vycházej z "base cell" v rendereru (cellSize = AppContext.cellSize + 0.5, scale=1)
            Object content = road.getContent();
            if (content instanceof core.model.cellular.Cell[][] cells && cells.length > 0) {
                int lanes = cells.length;
                int cols  = cells[0].length;
                double baseCell = (AppContext.cellSize + 0.5); // stejné jako v CellularRoadRenderer
                canvas.setWidth(cols  * baseCell);
                canvas.setHeight(lanes * baseCell);
            }
        } else if (Constants.CONTINOUS.equals(road.getType())) {
            // Continous: zvolíme fixní pixely na metr, aby to nebylo malinké
            double PX_PER_M = 4.0; // klidně dej do Constants, když chceš
            int lanes = road.getNumberOfLanes();
            double roadLengthUnits = road.getLength();          // metry
            double laneHeightUnits = Constants.LANE_WIDTH;      // metry na pruh

            canvas.setWidth(roadLengthUnits * PX_PER_M);
            canvas.setHeight(lanes * laneHeightUnits * PX_PER_M);
        } else {
            // fallback
            canvas.setWidth(Constants.CANVAS_WIDTH);
            canvas.setHeight(Constants.CANVAS_HEIGHT);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
