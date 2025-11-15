package ui.render;

import app.AppContext;
import core.model.CarParams;
import core.model.Road;
import core.utils.Constants;

import core.utils.RequestConstants;
import core.utils.RunDetails;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Iterator;
import java.util.LinkedList;

/********************************************
 * Continuous road renderer implementation, used for drawing continuous roads
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class ContinuousRoadRenderer implements IRoadRenderer {

    /**
     * function to draw the continuous road on the given graphics context
     *
     * @param gc GraphicsContext to draw on
     * @param road Road to be drawn
     * @param width Width of the drawing area
     * @param height Height of the drawing area
     * @param laneWidth Width of a single lane
     **/
    @Override
    public void draw(GraphicsContext gc, Road road, double width, double height, double laneWidth) {
        int lanes = road.getNumberOfLanes();

        if (lanes <= 0) {
            return;
        }

        // get size of road for drawing
        double laneHpx = laneWidth;
        double roadWidthPx = road.getLength();
        double roadHeightPx = lanes * laneHpx;

        // centering of the road
        double offsetX = 0;
        double offsetY = (height - roadHeightPx) / 2.0;

        double separatingLinesWidth = 5.0;
        double carSmallingFactor = 0.8;
        double carUpLiftFactor = 0.1;
        // width multiplayer so that it looks better
        double widthMultiplayer = Constants.CONTINOUS_ROAD_DRAWING_SCALE_FACTOR;

        // cars in lanes
        Object roadContent = road.getContent();
        if (!(roadContent instanceof LinkedList[])) {
            return;
        }
        LinkedList<CarParams>[] vehicles = (LinkedList<CarParams>[]) roadContent;

        // pattern for dashed lines
        if (AppContext.RUN_DETAILS.laneChange) {
            gc.setLineDashes(separatingLinesWidth);
        }

        Color carColor;

        for (int i = 0; i < lanes; i++) {
            double y = offsetY + i * laneHpx;

            // lane
            gc.setFill(Constants.ROAD_COLOR);
            gc.fillRect(offsetX, y, roadWidthPx * widthMultiplayer, laneHpx);


            if (AppContext.SIMULATION.getStepCount() > 10) {
                int oi = 0;
            }
            // cars in lane on index i
            Iterator<CarParams> it = vehicles[i].iterator();
            while (it.hasNext()) {
                CarParams car = it.next();

                // x place of car
                double carX = offsetX + car.xPosition * widthMultiplayer;
                double carW = car.getParameter(RequestConstants.LENGTH_REQUEST) * widthMultiplayer;

                // y place of car
                double carY = y + laneHpx * carUpLiftFactor;
                double carH = laneHpx * carSmallingFactor;

                if (car.color != null) {
                    carColor = car.color;
                } else {
                    carColor = Color.RED;
                }

                if (AppContext.RUN_DETAILS.debug) {
                    gc.setFill(Color.PURPLE);
                    String idInString = Integer.toString(car.id);
                    gc.fillText(idInString, carX - carW / 2, carY);
                }

                gc.setFill(carColor);
                gc.fillRect(carX - carW, carY, carW, carH);
//                gc.setStroke(Color.BLACK);
//                gc.strokeRect(carX - carW, carY, carW, carH);
            }

            // separating line between lanes
            if (i != lanes - 1) {
                gc.setStroke(Constants.LINE_SEPERATOR_COLOR);
                gc.strokeLine(offsetX, y + laneHpx, offsetX + roadWidthPx * widthMultiplayer, y + laneHpx);
            }
        }

        // turn off dashed line pattern, probably useless but whatever
        gc.setLineDashes(null);
    }
}
