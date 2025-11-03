package ui.render;

import app.AppContext;
import core.model.Road;
import core.model.cellular.Cell;
import core.utils.Constants;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CellularRoadRenderer implements IRoadRenderer {
    @Override
    public void draw(GraphicsContext gc, Road road, double width, double height, double laneWidth) {
        Object roadContent = road.getContent();

        if (roadContent == null || !(roadContent instanceof Cell[][])) { // sanity check
            return;
        }
        Cell[][] cells = (Cell[][]) roadContent;

        // sanitity check
        if (cells == null || cells.length == 0 || cells[0].length == 0) {
            return;
        }

        int lanes = cells.length;          // number of lanes
        int cols  = cells[0].length;       // number of cells for length

        double cellSize = laneWidth;

        // real size of road for drawing
        double roadWidthPx  = cols  * cellSize;
        double roadHeightPx = lanes * cellSize;
        /*double cellSize = AppContext.cellSize;          // fixní velikost buňky
        double roadWidthPx  = cols  * cellSize;
        double roadHeightPx = lanes * cellSize;*/

        // centing of the road
        double offsetX = 0;
        double offsetY = 0;
        Color carColor = Color.RED;

        // drawing cells
        for (int lane = 0; lane < lanes; lane++) {
            double y = offsetY + lane * cellSize;

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

            // seperating line between lanes
            if (lane < lanes - 1) {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(Constants.LINE_SEPARATOR_WIDTH);
                gc.setLineDashes(12, 8);
                double sepY = y + cellSize;
                gc.strokeLine(offsetX, sepY, offsetX + roadWidthPx, sepY);
                gc.setLineDashes(null);
            }
        }
    }
}