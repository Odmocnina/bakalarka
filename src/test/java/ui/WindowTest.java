package ui;

import app.AppContext;
import core.engine.CoreEngine;
import core.model.Road;
import core.sim.Simulation;
import core.utils.RunDetails;
import ui.Actions;
import ui.DialogMaker;
import core.utils.MyLogger;
import core.utils.constants.Constants;
import javafx.application.Platform;
import javafx.stage.Stage;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import ui.render.IRoadRenderer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/***********************
 * Unit tests for the Window class, covering various scenarios of user interactions and internal logic
 * using Mockito to mock dependencies and verify interactions with static methods and the CoreEngine
 *
 * @author Michael Hladky
 * @version 1.0.0
 ************************/
@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
public class WindowTest {

    /** instance of the Window class to be tested, initialized in the @Start method to ensure JavaFX is properly set
     *  up **/
    private Window window;

    /** mocked object of Simulation to be injected into the AppContext for testing methods that rely on simulation state
     *  without needing a real simulation **/
    private Simulation mockSimulation = mock(Simulation.class);

    /** mocked object of CoreEngine to be injected into the Window instance for testing methods that rely on engine
     * state without needing a real engine **/
    private CoreEngine mockEngine = mock(CoreEngine.class);

    /** mocked object of ICarFollowingModel to be injected into the AppContext for testing methods that rely on car
     * following model without needing a real model **/
    private ICarFollowingModel mockCarFollowingModel = mock(ICarFollowingModel.class);

    /** mocked object of ILaneChangingModel to be injected into the AppContext for testing methods that rely on lane
     * changing model without needing a real model **/
    private ILaneChangingModel mockLaneChangingModel = mock(ILaneChangingModel.class);

    /** mocked object of IRoadRenderer to be injected into the AppContext for testing methods that rely on rendering
     * without needing a real renderer **/
    private IRoadRenderer mockRenderer = mock(IRoadRenderer.class);

    /**
     * Before all tests, initialize the JavaFX toolkit to prevent IllegalStateException when creating JavaFX components
     * in the Window class during testing
     **/
    @BeforeAll
    static void initJavaFX() {
        // initialize JavaFX toolkit to prevent IllegalStateException
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // toolkit is already initialized
        }
    }

    /**
     * Start method for TestFX. This sets up the completely mocked AppContext
     * required for the Window's massive UI initialization without throwing NullPointerExceptions.
     */
    @Start
    public void start(Stage stage) {
        // Setup RunDetails and its OutputDetails
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.RUN_DETAILS.mapChanged = false;
        AppContext.RUN_DETAILS.log = new boolean[6]; // avoid array out of bounds for logging indexes

        AppContext.RUN_DETAILS.timeBetweenSteps = 100; // default value to avoid issues in the window logic that relies
        // on it, can be overridden in specific tests if needed

        AppContext.RUN_DETAILS.outputDetails = mock(core.utils.OutputDetails.class);
        when(AppContext.RUN_DETAILS.outputDetails.getWhatToOutput()).thenReturn(new HashMap<>());

        // Setup other AppContext dependencies
        AppContext.SIMULATION = mockSimulation;
        AppContext.CAR_FOLLOWING_MODEL = mockCarFollowingModel;
        AppContext.LANE_CHANGING_MODEL = mockLaneChangingModel;
        AppContext.RENDERER = mockRenderer;

        when(mockCarFollowingModel.getName()).thenReturn("Mocked Car Model");
        when(mockLaneChangingModel.getName()).thenReturn("Mocked Lane Model");

        // initialize the window and trigger its start method to build the UI
        window = new Window();
        window.start(stage);
    }

    @BeforeEach
    void setUp() throws Exception {
        // inject the mocked CoreEngine into the window instance via reflection
        // because the start() method creates a real CoreEngine that we want to override for testing logic
        Field engineField = Window.class.getDeclaredField("engine");
        engineField.setAccessible(true);
        engineField.set(window, mockEngine);
    }

    /**
     * test to verify that if the simulation is currently running,
     * exporting results is blocked and a warning dialog is shown.
     **/
    @Test
    void handleExportResults_SimulationRunning_ShouldWarnAndReturn() throws Exception {
        // Arrange
        when(mockEngine.getRunning()).thenReturn(true);

        try (MockedStatic<DialogMaker> dialogMock = mockStatic(DialogMaker.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {

            // Act
            invokePrivateMethod("handleExportResults", new Class<?>[]{Stage.class, String.class},
                    null, Constants.RESULTS_OUTPUT_CSV);

            // Assert
            dialogMock.verify(() -> DialogMaker.warningDialog(any(), anyString()), times(1));
            loggerMock.verify(() -> MyLogger.log(anyString(), anyString()), atLeastOnce());
            // actions should never be called because it returns early
            actionsMock.verify(() -> Actions.exportResultsToCsvAction(any()), never());
        }
    }

    /**
     * test to verify that if the simulation is NOT running and CSV is requested,
     * the action to export CSV is triggered.
     **/
    @Test
    void handleExportResults_NotRunning_CSV_ShouldExportCsv() throws Exception {
        // Arrange
        when(mockEngine.getRunning()).thenReturn(false);

        try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
            // Act
            invokePrivateMethod("handleExportResults", new Class<?>[]{Stage.class, String.class},
                    null, Constants.RESULTS_OUTPUT_CSV);

            // Assert
            actionsMock.verify(() -> Actions.exportResultsToCsvAction(mockSimulation), times(1));
        }
    }

    /**
     * test to verify that the math in getClickedRoadIndex correctly identifies
     * which road was clicked based on Y-coordinates, gaps, and lane heights.
     **/
    @Test
    void getClickedRoadIndex_ShouldReturnCorrectIndex() throws Exception {
        // Arrange
        Road mockRoad1 = mock(Road.class);
        when(mockRoad1.getNumberOfLanes()).thenReturn(2); // Road 1 height: 2 * 8.0 = 16.0

        Road mockRoad2 = mock(Road.class);
        when(mockRoad2.getNumberOfLanes()).thenReturn(3); // Road 2 height: 3 * 8.0 = 24.0

        Road[] roads = new Road[]{mockRoad1, mockRoad2};
        double gap = 20.0;
        double laneHeight = 8.0;

        // Road 1 boundaries: Starts at 20.0, ends at 36.0
        // Gap: 36.0 to 56.0
        // Road 2 boundaries: Starts at 56.0, ends at 80.0

        // Act & Assert via reflection
        Method method = Window.class.getDeclaredMethod("getClickedRoadIndex",
                double.class, Road[].class, double.class, double.class);
        method.setAccessible(true);

        // Click before Road 1
        assertEquals(-1, (int) method.invoke(window, 10.0, roads, gap, laneHeight));
        // Click exactly on Road 1
        assertEquals(0, (int) method.invoke(window, 25.0, roads, gap, laneHeight));
        // Click in the gap between Road 1 and Road 2
        assertEquals(-1, (int) method.invoke(window, 45.0, roads, gap, laneHeight));
        // Click exactly on Road 2
        assertEquals(1, (int) method.invoke(window, 60.0, roads, gap, laneHeight));
        // Click after Road 2
        assertEquals(-1, (int) method.invoke(window, 100.0, roads, gap, laneHeight));
    }

    /**
     * test to verify that handling start/stop action when there are no roads logs a warning and returns
     **/
    @Test
    void handleStartStopAction_NoRoads_ShouldWarnAndReturn() throws Exception {
        // Arrange
        when(mockSimulation.getRoads()).thenReturn(new Road[0]);

        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            // Act
            invokePrivateMethod("handleStartStopAction", new Class<?>[]{Stage.class}, (Stage) null);

            // Assert
            loggerMock.verify(() -> MyLogger.log(contains("No roads"), eq(Constants.WARN_FOR_LOGGING)), times(1));
            verify(mockEngine, never()).stop();
            verify(mockEngine, never()).start();
        }
    }

    /**
     * test to verify that handleReset triggers the reset action if simulation has 0 steps
     **/
    @Test
    void handleReset_ZeroSteps_ShouldResetDirectly() throws Exception {
        // Arrange
        when(mockSimulation.getStepCount()).thenReturn(0);
        Runnable dummyPaintAll = () -> {};

        try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
            // Act
            invokePrivateMethod("handleReset", new Class<?>[]{Stage.class, Runnable.class},
                    null, dummyPaintAll);

            // Assert
            actionsMock.verify(() -> Actions.resetSimulationAction(mockSimulation, dummyPaintAll), times(1));
        }
    }

    /**
     * test to verify that handleReset asks for confirmation if steps > 0 and cancels if user says NO
     **/
    @Test
    void handleReset_HasSteps_UserCancels_ShouldReturn() throws Exception {
        // Arrange
        when(mockSimulation.getStepCount()).thenReturn(10);
        Runnable dummyPaintAll = () -> {};

        try (MockedStatic<DialogMaker> dialogMock = mockStatic(DialogMaker.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {

            // user clicks NO/Cancel
            dialogMock.when(() -> DialogMaker.confirmDialog(any(), anyString(), anyString())).thenReturn(false);

            // Act
            invokePrivateMethod("handleReset", new Class<?>[]{Stage.class, Runnable.class},
                    null, dummyPaintAll);

            // Assert
            loggerMock.verify(() -> MyLogger.log(contains("cancelled"), anyString()), times(1));
            actionsMock.verify(() -> Actions.resetSimulationAction(any(), any()), never());
        }
    }

    /**
     * Helper method to invoke private methods via reflection dynamically
     */
    private Object invokePrivateMethod(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = Window.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(window, args);
    }
}