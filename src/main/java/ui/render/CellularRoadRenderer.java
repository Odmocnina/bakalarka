package ui.render;

import app.AppContext;
import core.model.Road;
import core.model.cellular.Cell;
import core.utils.constants.Constants;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/********************************************
 * Cellular road renderer implementation, used for drawing cellular roads
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class CellularRoadRenderer implements IRoadRenderer {

    /**
     * function to draw the cellular road on the given graphics context
     *
     * @param gc GraphicsContext to draw on
     * @param road Road to be drawn
     * @param width Width of the drawing area
     * @param height Height of the drawing area
     * @param laneWidth Width of a single lane
     **/
    @Override
    public void draw(GraphicsContext gc, Road road, double width, double height, double laneWidth) {
        Object roadContent = road.getContent();

        if (!(roadContent instanceof Cell[][] cells)) { // sanity check, if not cellular road, return
            return;
        }

        // sanity check
        if (cells.length == 0 || cells[0].length == 0) { // if no cells, return
            return;
        }

        int lanes = cells.length;          // number of lanes
        int cols  = cells[0].length;       // number of cells for length

        double cellSize = laneWidth;

        // real size of road for drawing
        double roadWidthPx  = cols  * cellSize;

        // centering of the road
        double offsetX = 0;
        double offsetY = 0;
        Color carColor = Color.RED;

        // drawing cells
        for (int lane = 0; lane < lanes; lane++) {
            double y = offsetY + lane * cellSize;

            if (road.isLaneGreen(lane)) {
                gc.setFill(Color.LIGHTGREEN);
            } else {
                gc.setFill(Color.RED);
            }
            gc.fillRect(roadWidthPx, y, cellSize, cellSize);

            for (int col = cols - 1; col >= 0; col--) {
                Cell cell = cells[lane][col];
                if (cell == null) {
                    continue;
                }

                double x = offsetX + col * cellSize;

                if (cell.isOccupied() && cell.isHead() && cell.getCarParams() != null) {
                    carColor = cell.getCarParams().color;
                }

                gc.setFill(cell.isOccupied() ? carColor : Constants.ROAD_COLOR);
                gc.fillRect(x, y, cellSize, cellSize);

                if (AppContext.RUN_DETAILS.drawCells) {
                    gc.setLineWidth(Constants.CELL_SEPARATOR_WIDTH);
                    gc.setStroke(Constants.CELL_SEPARATOR_COLOR);
                    gc.strokeRect(x, y, cellSize, cellSize);
                }
            }

            // separating line between lanes
            if (lane < lanes - 1) {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(Constants.LINE_SEPARATOR_WIDTH);
                if (AppContext.RUN_DETAILS.laneChange) {
                    gc.setLineDashes(12, 8);
                }
                double sepY = y + cellSize;
                gc.strokeLine(offsetX, sepY, offsetX + roadWidthPx, sepY);
                gc.setLineDashes(null);
            }
        }
    }
}