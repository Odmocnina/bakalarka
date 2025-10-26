package ui.render;

import core.model.Road;
import javafx.scene.canvas.GraphicsContext;

/***********************************
 * Interface for road rederers for gui
 *
 * @author Michael Hladky
 * @version 1.0
 ***********************************/
public interface IRoadRenderer {
    public void draw(GraphicsContext gc, Road road, double width, double height);

}
