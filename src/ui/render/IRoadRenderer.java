package ui.render;

import core.model.Road;
import javafx.scene.canvas.GraphicsContext;

public interface IRoadRenderer {
    void draw(GraphicsContext gc, Road road, double width, double height);
}
