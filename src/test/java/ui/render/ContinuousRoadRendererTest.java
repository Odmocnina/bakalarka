package ui.render;

import app.AppContext;
import core.model.CarParams;
import core.model.Road;
import core.utils.RunDetails;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContinuousRoadRenderer, covering various scenarios of drawing the road and cars
 * using Mockito to mock dependencies and verify interactions with the GraphicsContext
 *
 * @author Michael Hladky
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class ContinuousRoadRendererTest {

    /** instance of the renderer to be tested **/
    private ContinuousRoadRenderer renderer;

    /** mock GraphicsContext for verifying drawing interactions **/
    @Mock
    private GraphicsContext mockGc;

    /** mock Road for providing test data and verifying method calls **/
    @Mock
    private Road mockRoad;

    /**
     * setup method to initialize the renderer and configure the global state for testing, ensuring that the tests can
     * run without NullPointerExceptions and that the AppContext is in a known state before each test
     **/
    @BeforeEach
    void setUp() {
        renderer = new ContinuousRoadRenderer();

        // AppContext setUp for tests - we need to initialize the RUN_DETAILS to avoid NullPointerExceptions in the
        // renderer
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.RUN_DETAILS.laneChange = false;
        AppContext.RUN_DETAILS.debug = false;
    }

    /**
     * test to verify that if the road has zero or fewer lanes, the draw method returns early
     * and does not interact with the GraphicsContext
     **/
    @Test
    void drawZeroLanesShouldReturnEarly() {
        // Arrange: road with 0 lanes
        when(mockRoad.getNumberOfLanes()).thenReturn(0);

        // Act
        renderer.draw(mockGc, mockRoad, 800.0, 600.0, 20.0);

        // Assert
        // renderer should not draw anything
        verifyNoInteractions(mockGc);
    }

    /**
     * test to verify that if the road content is not an array of LinkedLists, the draw method
     * returns early and does not draw any lanes or cars
     **/
    @Test
    void drawInvalidContentShouldReturnEarly() {
        // Arrange: road has 1 lane but content is of invalid type (not LinkedList[])
        when(mockRoad.getNumberOfLanes()).thenReturn(1);
        when(mockRoad.getContent()).thenReturn("Invalid Content Type");

        // Act
        renderer.draw(mockGc, mockRoad, 800.0, 600.0, 20.0);

        // Assert
        // renderer should return early due to invalid content type, so no drawing interactions should occur
        verifyNoInteractions(mockGc);
    }

    /**
     * test to verify that an empty lane with a green light is drawn correctly without cars,
     * and that the line dashes are reset at the end of the method
     **/
    @Test
    void drawEmptyLaneGreenLightShouldDrawRoadAndGreenSemaphore() {
        // Arrange: 1 lane with empty content and green light
        when(mockRoad.getNumberOfLanes()).thenReturn(1);
        LinkedList<CarParams>[] vehicles = new LinkedList[1];
        vehicles[0] = new LinkedList<>(); // empty lane
        when(mockRoad.getContent()).thenReturn(vehicles);
        when(mockRoad.isLaneGreen(0)).thenReturn(true); // at the end of the lane is green light

        // Act
        renderer.draw(mockGc, mockRoad, 800.0, 600.0, 20.0);

        // Assert
        // control that the road is drawn with the correct color
        verify(mockGc, atLeastOnce()).setFill(Constants.ROAD_COLOR);
        // control that the green light is drawn
        verify(mockGc, atLeastOnce()).setFill(Color.LIGHTGREEN);
        verify(mockGc, never()).setFill(Color.RED);

        // since there are no cars, no text should be drawn
        verify(mockGc, never()).fillText(anyString(), anyDouble(), anyDouble());
        // since lane change is not enabled, line dashes should be reset to null at the end
        verify(mockGc, atLeastOnce()).setLineDashes(null);
    }

    /**
     * test to verify that multiple lanes are drawn correctly with dashed lines when lane change
     * is enabled, a red light at the end, and a car with a custom color is drawn properly
     **/
    @Test
    void drawMultipleLanesLaneChangeEnabledCustomCarColorShouldDrawDashedLineAndBlueCar() {
        // Arrange: 2 lanes
        when(mockRoad.getNumberOfLanes()).thenReturn(2);

        LinkedList<CarParams>[] vehicles = new LinkedList[2];
        vehicles[0] = new LinkedList<>();
        vehicles[1] = new LinkedList<>();

        // create car with custom color (not null, not default red)
        CarParams mockCar = mock(CarParams.class);
        mockCar.color = Color.BLUE; // car with custom color
        when(mockCar.getParameter(RequestConstants.LENGTH_REQUEST)).thenReturn(5.0);
        vehicles[0].add(mockCar); //  car in lane 0

        when(mockRoad.getContent()).thenReturn(vehicles);
        when(mockRoad.isLaneGreen(anyInt())).thenReturn(false); // both lanes have red light at the end

        AppContext.RUN_DETAILS.laneChange = true;

        // Act
        renderer.draw(mockGc, mockRoad, 800.0, 600.0, 20.0);

        // Assert
        // lane change is enabled, so line dashes should be set to the specified width for dashed lines
        verify(mockGc, atLeastOnce()).setLineDashes(5.0);
        // control red light at the end of both lanes
        verify(mockGc, atLeastOnce()).setFill(Color.RED);
        // control the custom color of the car is used for drawing
        verify(mockGc, atLeastOnce()).setFill(Color.BLUE);
        // since there is a car, it should be drawn as a filled rectangle, so we verify that strokeLine is called at
        // least once (for lane lines) and fillRect is called at least once (for the car)
        verify(mockGc, atLeastOnce()).strokeLine(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    /**
     * test to verify that if a car has no color assigned, it falls back to red, and if debug
     * mode is enabled, the car's ID is drawn on the graphics context
     **/
    @Test
    void drawDebugEnabledNullCarColorShouldDrawDebugTextAndRedCar() {
        // Arrange: 1 lane
        when(mockRoad.getNumberOfLanes()).thenReturn(1);

        LinkedList<CarParams>[] vehicles = new LinkedList[1];
        vehicles[0] = new LinkedList<>();

        // create car with null color to test fallback to red, and assign an ID for debug text verification
        CarParams mockCar = mock(CarParams.class);
        mockCar.color = null;
        mockCar.id = 42; // ID for debug text
        when(mockCar.getParameter(RequestConstants.LENGTH_REQUEST)).thenReturn(5.0);
        vehicles[0].add(mockCar);

        when(mockRoad.getContent()).thenReturn(vehicles);
        when(mockRoad.isLaneGreen(0)).thenReturn(true);

        AppContext.RUN_DETAILS.debug = true;

        // Act
        renderer.draw(mockGc, mockRoad, 800.0, 600.0, 20.0);

        // Assert
        // since the car has null color, it should fall back to red for drawing
        verify(mockGc, atLeastOnce()).setFill(Color.RED);
        // since debug mode is enabled, the car's ID should be drawn as text, and the color for debug text should be
        // purple
        verify(mockGc, atLeastOnce()).setFill(Color.PURPLE);
        // verify that the debug text with the car's ID is drawn at least once
        verify(mockGc, atLeastOnce()).fillText(eq("42"), anyDouble(), anyDouble());
    }
}