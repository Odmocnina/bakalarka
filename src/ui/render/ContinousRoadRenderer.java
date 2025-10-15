package ui.render;

import core.model.CarParams;
import core.model.Road;
import core.utils.Constants;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import java.util.LinkedList;

public class ContinousRoadRenderer implements IRoadRenderer {
    @Override
    public void draw(GraphicsContext gc, Road road, double width, double height) {
        int lanes = road.getNumberOfLanes();

        if (lanes <= 0) {
            return;
        }

        // size of road base units
        double roadLengthUnits = road.getLength();          // meters
        double laneHeightUnits = Constants.LANE_WIDTH;      // height of one lane in meters

        // scaling stuff
        double xScale = width  / roadLengthUnits;
        double yScale = height / (lanes * laneHeightUnits);
        double scale = Math.min(xScale, yScale);

        // get size of road for drawing
        double laneHpx = laneHeightUnits * scale;
        double roadWidthPx = roadLengthUnits   * scale;
        double roadHeightPx = lanes * laneHpx;

        // centering of the road
        double offsetX = (width  - roadWidthPx)  / 2.0;
        double offsetY = (height - roadHeightPx) / 2.0;

        // cars in lanes
        Object roadContent = road.getContent();
        if (roadContent == null || !(roadContent instanceof LinkedList[])) {
            return;
        }
        LinkedList<CarParams>[] vehicles = (LinkedList<CarParams>[]) roadContent;

        // pattern for dashed lines
        gc.setLineDashes(5 * scale);

        Color carColor = Color.RED;

        for (int i = 0; i < lanes; i++) {
            double y = offsetY + i * laneHpx;

            // lane
            gc.setFill(Constants.ROAD_COLOR);
            gc.fillRect(offsetX, y, roadWidthPx, laneHpx);

            // cars in lane on index i
            if (vehicles != null && i < vehicles.length && vehicles[i] != null) {
                for (CarParams car : vehicles[i]) {
                    // x place of car
                    double carX = offsetX + car.xPosition * scale;
                    double carW = car.length * scale;

                    // y palce of car
                    double carY = y + laneHpx * 0.1;
                    double carH = laneHpx * 0.8;

                    if (car.color != null) {
                        carColor = car.color;
                    } else {
                        carColor = Color.RED;
                    }

                    gc.setFill(carColor);
                    gc.fillRect(carX, carY, carW, carH);
                }
            }

            // seperating line between lanes
            if (i != lanes - 1) {
                gc.setStroke(Constants.LINE_SEPERATOR_COLOR);
                gc.strokeLine(offsetX, y + laneHpx, offsetX + roadWidthPx, y + laneHpx);
            }
        }

        // turn off dashed line pattern, probably useless but whatever
        gc.setLineDashes(null);
    }
}
