package ui.render;

import core.model.Road;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;

public interface IRoadRenderer {
    public void draw(GraphicsContext gc, Road road, double width, double height);

}
