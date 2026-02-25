package ui.render;

import app.AppContext;
import core.model.CarParams;
import core.model.Road;
import core.model.cellular.Cell;
import core.utils.RunDetails;
import core.utils.constants.Constants;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CellularRoadRenderer class, using Mockito to mock dependencies and verify interactions with GraphicsContext
 *
 * @author Michael Hladky
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class CellularRoadRendererTest {

    /** instance of the class we are testing, created fresh for each test to ensure isolation **/
    private CellularRoadRenderer renderer;

    /** mock object for GraphicsContext, that allows us to verify interactions with it (like setFill, fillRect,
     * etc.)**/
    @Mock
    private GraphicsContext mockGc;

    /** mock object for Road, that returns specific content when getContent() is called, here Cell[][] **/
    @Mock
    private Road mockRoad;

    /**
     * set up method that runs before each test, initializes the renderer and sets up AppContext for testing
     * **/
    @BeforeEach
    void setUp() {
        renderer = new CellularRoadRenderer();
        
        // AppContext used in CellularRoadRenderer, we need to set it up for testing, especially the RunDetails that it
        // uses for drawing
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.RUN_DETAILS.drawCells = false;
        AppContext.RUN_DETAILS.laneChange = false;
    }

    /**
     * test to verify that if the road content is not of type Cell[][], the draw method returns early and does not
     * interact with the GraphicsContext, simulating a non-cellular road scenario
     **/
    @Test
    void draw_NonCellularRoad_ShouldReturnEarly() {
        // road will return different type of content than Cell[][], simulating non-cellular road
        when(mockRoad.getContent()).thenReturn("Not a cell array");

        // Act
        renderer.draw(mockGc, mockRoad, 800.0, 600.0, 20.0);

        // graphic context should not be interacted with, because the method should return early when it detects
        // non-cellular road
        verifyNoInteractions(mockGc);
    }

    /**
     * test to verify that if the road content is an empty Cell[][] array, the draw method returns early and does not
     * interact with the GraphicsContext, simulating a scenario where there are no cells to draw
     **/
    @Test
    void draw_EmptyCellArray_ShouldReturnEarly() {
        // Arrange: empty Cell[][] array, simulating scenario where there are no cells to draw
        Cell[][] emptyCells = new Cell[0][0];
        when(mockRoad.getContent()).thenReturn(emptyCells);

        // Act
        renderer.draw(mockGc, mockRoad, 800.0, 600.0, 20.0);

        // Assert: GraphicsContext should not be interacted with, because the method should return early when it detects
        // empty cell array
        verifyNoInteractions(mockGc);
    }

    /**
     * test to verify that if the road content is a valid Cell[][] array with one empty cell, the draw method interacts
     * with the GraphicsContext to set the fill color for the road background and draws a rectangle for the cell,
     * simulating a scenario where there is one lane with one empty cell
     **/
    @Test
    void draw_ValidEmptyRoad_ShouldDrawRoadBackground() {
        // Arrange: 1 lane, 1 cell length (1x1 Cell[][]), cell is empty (not occupied)
        Cell[][] cells = new Cell[1][1];
        Cell mockCell = mock(Cell.class);
        when(mockCell.isOccupied()).thenReturn(false);
        cells[0][0] = mockCell;
        
        when(mockRoad.getContent()).thenReturn(cells);
        when(mockRoad.isLaneGreen(0)).thenReturn(true); //green at the end of the lane

        // Act
        renderer.draw(mockGc, mockRoad, 800.0, 600.0, 20.0);

        // Assert
        // control that end is green
        verify(mockGc, atLeastOnce()).setFill(Color.LIGHTGREEN);
        
        // Control that the road background color is set for the cell (even if it's empty, it should still be drawn with
        // road color)
        verify(mockGc, atLeastOnce()).setFill(Constants.ROAD_COLOR);
        
        // control that fillRect is called to draw the cell, with correct parameters (20x20 size for 1 cell, position
        // can be any
        verify(mockGc, times(2)).fillRect(anyDouble(), anyDouble(), eq(20.0), eq(20.0));
    }

    /**
     * test to verify that if the road content is a valid Cell[][] array with one occupied cell, the draw method
     * interacts with the GraphicsContext to set the fill color for the car and draws a rectangle for the cell,
     * simulating a scenario where there is one lane with one cell occupied by a car with a specific color, and the end
     * of the lane is red
     **/
    @Test
    void draw_OccupiedCell_ShouldDrawCarColor() {
        // Arrange: 1 lane, 1 cell length (1x1 Cell[][]), cell is occupied by a car with specific color, and end of lane
        // is red
        Cell[][] cells = new Cell[1][1];
        Cell mockCell = mock(Cell.class);
        
        // create fake (mock) car with blue color
        var mockCarParams = new CarParams();
        mockCarParams.color = Color.BLUE;

        when(mockCell.isOccupied()).thenReturn(true);
        when(mockCell.isHead()).thenReturn(true);
        when(mockCell.getCarParams()).thenReturn(mockCarParams);
        cells[0][0] = mockCell;

        when(mockRoad.getContent()).thenReturn(cells);
        when(mockRoad.isLaneGreen(0)).thenReturn(false); // red at the end of the road

        // Act
        renderer.draw(mockGc, mockRoad, 800.0, 600.0, 20.0);

        // Assert
        // red at the end of the road
        verify(mockGc, atLeastOnce()).setFill(Color.RED);
        // blue color
        verify(mockGc, atLeastOnce()).setFill(Color.BLUE);
    }

    /**
     * test to verify that if the road content is a valid Cell[][] array with one null cell, the draw method skips
     * drawing that cell and does not throw a NullPointerException, simulating a scenario where there is one lane with
     * one cell that is null (missing), and the other cell is empty
     **/
    @Test
    void draw_WithNullCell_ShouldSkipDrawingThatCell() {
        // Arrange: 1 lan, 2 cells length (1x2 Cell[][]), one cell is null (missing), the other cell is empty, and the
        // end of the lane is green
        Cell[][] cells = new Cell[1][2];
        cells[0][0] = null; // null cell

        Cell mockCell = mock(Cell.class);
        when(mockCell.isOccupied()).thenReturn(false);
        cells[0][1] = mockCell;

        when(mockRoad.getContent()).thenReturn(cells);
        when(mockRoad.isLaneGreen(0)).thenReturn(true);

        // Act
        renderer.draw(mockGc, mockRoad, 800.0, 600.0, 20.0);

        // Assert
        // should run without null pointer exception, and should draw the second cell (the first one is null, so it
        // should be skipped)
        verify(mockGc, times(2)).fillRect(anyDouble(), anyDouble(), eq(20.0), eq(20.0));
    }

    /**
     * test to verify that if the road has multiple lanes and lane change is disabled, the draw method interacts with
     * the GraphicsContext to set a solid line for lane separation, simulating a scenario where there are multiple lanes
     * and lane changing is not allowed, so the lane separators should be drawn as solid lines
     **/
    @Test
    void draw_MultipleLanes_LaneChangeDisabled_ShouldDrawSolidLine() {
        // Arrange: 1 lanes, 1 cell length
        Cell[][] cells = new Cell[2][1];
        cells[0][0] = mock(Cell.class);
        cells[1][0] = mock(Cell.class);
        when(mockRoad.getContent()).thenReturn(cells);

        // set lane change to false
        AppContext.RUN_DETAILS.laneChange = false;

        // Act
        renderer.draw(mockGc, mockRoad, 800.0, 600.0, 20.0);

        // Assert
        // check if color and width for lane separation line are set for solid line
        verify(mockGc, atLeastOnce()).setStroke(Color.WHITE);
        verify(mockGc, atLeastOnce()).setLineWidth(Constants.LINE_SEPARATOR_WIDTH);
        // check if strokeLine is called to draw the lane separation line, with any parameters (position can be any, but
        // it should be called at least once)
        verify(mockGc, atLeastOnce()).strokeLine(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        // check that setLineDashes is NOT called with parameters for dashed line, but it can be called with null to
        // reset the line dashes
        verify(mockGc, never()).setLineDashes(12, 8);
        verify(mockGc, atLeastOnce()).setLineDashes(null);
    }

    /**
     * test to verify that if the road has multiple lanes and lane change is enabled, the draw method interacts with
     * the GraphicsContext to set a dashed line for lane separation, simulating a scenario where there are multiple
     * lanes and lane changing is allowed, so the lane separators should be drawn as dashed lines
     **/
    @Test
    void draw_MultipleLanes_LaneChangeEnabled_ShouldDrawDashedLine() {
        // Arrange: 2 lanes, 1 cell length
        Cell[][] cells = new Cell[2][1];
        cells[0][0] = mock(Cell.class);
        cells[1][0] = mock(Cell.class);
        when(mockRoad.getContent()).thenReturn(cells);

        AppContext.RUN_DETAILS.laneChange = true;

        // Act
        renderer.draw(mockGc, mockRoad, 800.0, 600.0, 20.0);

        // Assert
        // check if color and width for lane separation line are set for dashed line
        verify(mockGc, atLeastOnce()).setLineDashes(12, 8);
        verify(mockGc, atLeastOnce()).setLineDashes(null);
    }
}