package ui.render;

import core.model.Road;

import javafx.scene.canvas.GraphicsContext;

/***********************************
 * Interface for road renderers for gui
 *
 * @author Michael Hladky
 * @version 1.0
 ***********************************/
public interface IRoadRenderer {

    /*
     * draws the road on the given graphics context, dependent on the implementation of the renderer
     *
     * @param gc the graphics context to draw on
     * @param road the road to draw
     * @param width the width of the drawing area
     * @param height the height of the drawing area
     * @param laneWidth the width of a single lane
     */
    void draw(GraphicsContext gc, Road road, double width, double height, double laneWidth);

}
