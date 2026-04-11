package scenario;

import core.model.Road;
import core.utils.MyLogger;
import core.utils.RoadParameters;
import core.utils.constants.Constants;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.testfx.util.WaitForAsyncUtils;
import ui.DialogMaker;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/*********************************************
 * Class containing integration tests for interactions with the canvas in the Window class, specifically testing the
 * behavior when clicking on a road (editing onr road) and when clicking outside of any road
 * (should log that we clicked outside).
 *
 * @author Michael Hladky
 * @version 1.0
 *********************************************/
public class CanvasInteractionIT extends BaseWindowIT {

    // Test: clicking on a road should calculate the correct index and log it
    @Test
    public void testClickOnRoad_ShouldCalculateIndexAndLogIt() {
        setMapLoadedState(true);

        Road mockRoad = mock(Road.class);
        when(mockRoad.getNumberOfLanes()).thenReturn(10);
        when(mockRoad.getLength()).thenReturn(50.0);
        when(mockRoad.getContent()).thenReturn(new java.util.LinkedList[0]);
        when(mockSimulation.getRoads()).thenReturn(new Road[]{ mockRoad });

        Platform.runLater(() -> window.start(primaryStage));
        WaitForAsyncUtils.waitForFxEvents();

        Canvas canvas = lookup(node -> node instanceof Canvas).query();
        Pane canvasPane = (Pane) canvas.getParent();

        // all wrap to interact() to ensure we are on the JavaFX thread, because we will be calling the event handler directly
        interact(() -> {
            try (MockedStatic<DialogMaker> dialogMock = mockStatic(DialogMaker.class);
                 MockedStatic<RoadParameters> roadParamsMock = mockStatic(RoadParameters.class);
                 MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

                roadParamsMock.when(() -> RoadParameters.existingRoadsToRoadParameters(any())).thenReturn(new ArrayList<>());

                // create a mock MouseEvent with Y coordinate that corresponds to the middle of the road (which has 10 lanes, so lane height is 5, middle is at 5*10/2 = 25)
                MouseEvent mockEvent = mock(MouseEvent.class);
                when(mockEvent.getY()).thenReturn(60.0); // 60 is in the middle of road

                // call event handler directly with our mock event
                canvasPane.getOnMouseClicked().handle(mockEvent);

                // check, one by logger should log the correct index (0 in this case, because we have only one road and it starts at Y=0)
                loggerMock.verify(() -> MyLogger.log(
                        contains("Clicked on road index: 0"),
                        eq(Constants.INFO_FOR_LOGGING)
                ), times(1));
            }
        });
    }

    // Test: clicking outside of any road should log that we clicked outside
    @Test
    public void testClickOutsideRoad_ShouldLogOutsideMessage() {
        setMapLoadedState(true);

        Road mockRoad = mock(Road.class);
        when(mockRoad.getNumberOfLanes()).thenReturn(1);
        when(mockRoad.getLength()).thenReturn(50.0);
        when(mockRoad.getContent()).thenReturn(new java.util.LinkedList[0]);
        when(mockSimulation.getRoads()).thenReturn(new Road[]{ mockRoad });

        Platform.runLater(() -> window.start(primaryStage));
        WaitForAsyncUtils.waitForFxEvents();

        Canvas canvas = lookup(node -> node instanceof Canvas).query();
        Pane canvasPane = (Pane) canvas.getParent();

        // again, wrap in interact() to ensure we are on the JavaFX thread, because we will be calling the event handler directly
        interact(() -> {
            try (MockedStatic<DialogMaker> dialogMock = mockStatic(DialogMaker.class);
                 MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

                // set up a mock MouseEvent with Y coordinate that is outside the road (road is only 1 lane, so it occupies Y=0 to Y=5, we will click at Y=200)
                MouseEvent mockEvent = mock(MouseEvent.class);
                when(mockEvent.getY()).thenReturn(200.0);

                // call event handler directly with our mock event
                canvasPane.getOnMouseClicked().handle(mockEvent);

                // verify that the logger was called with the message about clicking outside, and that no dialog was shown (since we only show a dialog when clicking on a road)
                loggerMock.verify(() -> MyLogger.log(
                        eq("Clicked outside of any road."),
                        eq(Constants.INFO_FOR_LOGGING)
                ), times(1));

                dialogMock.verifyNoInteractions();
            }
        });
    }
}