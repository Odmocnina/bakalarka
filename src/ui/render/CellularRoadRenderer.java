package ui.render;

import app.AppContext;
import core.model.Road;
import core.model.cellular.Cell;
import core.utils.Constants;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CellularRoadRenderer implements IRoadRenderer {
    @Override
    public void draw(GraphicsContext gc, Road road, double width, double height) {
        Object roadContent = road.getContent();

        if (roadContent == null || !(roadContent instanceof Cell[][])) {
            return;
        }
        Cell[][] cells = (Cell[][]) roadContent;

        // sanitity check
        if (cells == null || cells.length == 0 || cells[0].length == 0) {
            return;
        }

        int lanes = cells.length;          // number of lanes
        int cols  = cells[0].length;       // number of cells for length

        // base cell size
        double baseCell = (AppContext.cellSize + 0.5);

        // scaling bullshit
        double xScale = width  / (cols  * baseCell);
        double yScale = height / (lanes * baseCell);
        double scale  = Math.min(xScale, yScale);

        double cellSize = baseCell * scale; // cell size which is goint go be used for drawing

        // real size of road for drawing
        double roadWidthPx  = cols  * cellSize;
        double roadHeightPx = lanes * cellSize;

        // centing of the road
        double offsetX = (width  - roadWidthPx)  / 2.0;
        double offsetY = (height - roadHeightPx) / 2.0;
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

                if (AppContext.drawCells) {
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