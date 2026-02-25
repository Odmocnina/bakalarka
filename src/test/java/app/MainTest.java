package app;

import core.sim.Simulation;
import core.utils.MyLogger;
import core.utils.ResultsRecorder;
import core.utils.RunDetails;
import core.utils.constants.Constants;
import core.utils.loading.ConfigLoader;
import models.ICarFollowingModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ui.Window;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**********************************
 * Unit tests for Main class
 *
 * @author Michael Hladky
 * @version 1.0
 **********************************/
@ExtendWith(MockitoExtension.class)
public class MainTest {

    /** mock object of Simulation **/
    @Mock
    private Simulation mockSimulation;

    /** mock object of ResultsRecorder for verifying results writing behavior **/
    @Mock
    private ResultsRecorder mockRecorder;

    /** RunDetails instance to set in AppContext for tests **/
    private RunDetails runDetails;

    /**
     * set up method to initialize AppContext with mock objects before each test, ensuring that the Main class has the
     * necessary context to run without relying on actual implementations of Simulation or RunDetails
     **/
    @BeforeEach
    void setUp() {
        // Initialize AppContext with mocks
        runDetails = mock(RunDetails.class);
        AppContext.RUN_DETAILS = runDetails;
        AppContext.SIMULATION = mockSimulation;
    }

    /**
     * tear down method to reset AppContext after each test, ensuring that static fields do not interfere between tests
     * and maintaining test isolation
     **/
    @AfterEach
    void tearDown() {
        // Clean up AppContext
        AppContext.RUN_DETAILS = null;
        AppContext.SIMULATION = null;
    }

    /**
     * test to verify that if an invalid duration is provided, the application logs a fatal error and exits early
     **/
    @Test
    void main_InvalidDuration_ShouldExitEarly() {
        try (MockedStatic<InputParametersHandeler> inputMock = mockStatic(InputParametersHandeler.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<ConfigLoader> configMock = mockStatic(ConfigLoader.class)) {

            // simulate invalid duration
            inputMock.when(() -> InputParametersHandeler.getSpecificParameter(any(), anyString())).thenReturn(null);
            inputMock.when(() -> InputParametersHandeler.getDurationFromParameter(any())).thenReturn(Constants.INVALID_INPUT_PARAMETERS);

            // Act
            Main.main(new String[]{});

            // Assert
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("Invalid duration value"), anyString()), times(1));
            // Verify it returned before trying to load config
            configMock.verify(() -> ConfigLoader.loadAllConfig(any(), any(), any(), anyInt(), any(), anyInt(), any()), never());
        }
    }

    /**
     * test to verify that if a valid car following model ID is passed, but it is invalid/not found,
     * the application logs a fatal error and exits early
     **/
    @Test
    void main_InvalidCarFollowingModel_ShouldExitEarly() {
        try (MockedStatic<InputParametersHandeler> inputMock = mockStatic(InputParametersHandeler.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<ConfigLoader> configMock = mockStatic(ConfigLoader.class)) {

            // valid duration, but specific arg for car model is present
            inputMock.when(() -> InputParametersHandeler.getDurationFromParameter(any())).thenReturn(Constants.NO_DURATION_PROVIDED);
            // simulate returning "bad-id" for the car model parameter
            inputMock.when(() -> InputParametersHandeler.getSpecificParameter(any(), eq(Constants.CAR_FOLLOWING_MODEL_PARAMETER_PREFIX))).thenReturn("bad-id");
            // simulate failing to find the model
            inputMock.when(() -> InputParametersHandeler.getCarFollowingModelFromParameter("bad-id")).thenReturn(null);

            // Act
            Main.main(new String[]{"--carModel=bad-id"});

            // Assert
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("Invalid car following model"), anyString()), times(1));
            configMock.verify(() -> ConfigLoader.loadAllConfig(any(), any(), any(), anyInt(), any(), anyInt(), any()), never());
        }
    }

    /**
     * test to verify that if a valid lane changing model ID is passed, but it is invalid/not found,
     * the application logs a fatal error and exits early
     **/
    @Test
    void main_InvalidLaneChangingModel_ShouldExitEarly() {
        try (MockedStatic<InputParametersHandeler> inputMock = mockStatic(InputParametersHandeler.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<ConfigLoader> configMock = mockStatic(ConfigLoader.class)) {

            inputMock.when(() -> InputParametersHandeler.getDurationFromParameter(any())).thenReturn(Constants.NO_DURATION_PROVIDED);

            // mock successful car model retrieval
            inputMock.when(() -> InputParametersHandeler.getSpecificParameter(any(), eq(Constants.CAR_FOLLOWING_MODEL_PARAMETER_PREFIX))).thenReturn("good-car-id");
            inputMock.when(() -> InputParametersHandeler.getCarFollowingModelFromParameter("good-car-id")).thenReturn(mock(ICarFollowingModel.class));

            // mock failing lane changing model retrieval
            inputMock.when(() -> InputParametersHandeler.getSpecificParameter(any(), eq(Constants.LANE_CHANGING_MODEL_PARAMETER_PREFIX))).thenReturn("bad-lane-id");
            inputMock.when(() -> InputParametersHandeler.getLaneChangingModelFromParameter("bad-lane-id")).thenReturn(null);

            // Act
            Main.main(new String[]{"--carModel=good-car-id", "--laneModel=bad-lane-id"});

            // Assert
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("Invalid lane changing model"), anyString()), times(1));
            configMock.verify(() -> ConfigLoader.loadAllConfig(any(), any(), any(), anyInt(), any(), anyInt(), any()), never());
        }
    }

    /**
     * test to verify that if config loading fails, the application logs an error and exits
     **/
    @Test
    void main_ConfigLoadFails_ShouldExitEarly() {
        try (MockedStatic<InputParametersHandeler> inputMock = mockStatic(InputParametersHandeler.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<ConfigLoader> configMock = mockStatic(ConfigLoader.class);
             MockedStatic<Window> windowMock = mockStatic(Window.class)) {

            inputMock.when(() -> InputParametersHandeler.getDurationFromParameter(any())).thenReturn(100);

            // simulate config loading failure
            configMock.when(() -> ConfigLoader.loadAllConfig(any(), any(), any(), anyInt(), any(), anyInt(), any())).thenReturn(false);

            // Act
            Main.main(new String[]{});

            // Assert
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("Failed to load configuration"), anyString()), times(1));
            // verify GUI was never started
            windowMock.verify(() -> Window.main(any()), never());
        }
    }

    /**
     * test to verify that if GUI mode is enabled, it successfully starts the JavaFX Window
     **/
    @Test
    void main_GuiModeEnabled_ShouldStartWindow() {
        try (MockedStatic<InputParametersHandeler> inputMock = mockStatic(InputParametersHandeler.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<ConfigLoader> configMock = mockStatic(ConfigLoader.class);
             MockedStatic<Window> windowMock = mockStatic(Window.class)) {

            inputMock.when(() -> InputParametersHandeler.getDurationFromParameter(any())).thenReturn(Constants.NO_DURATION_PROVIDED);
            configMock.when(() -> ConfigLoader.loadAllConfig(any(), any(), any(), anyInt(), any(), anyInt(), any())).thenReturn(true);

            // simulate GUI mode being true in AppContext
            runDetails.showGui = true;

            // Act
            String[] args = new String[]{};
            Main.main(args);

            // Assert
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("GUI enabled"), anyString()), times(1));
            // verify Window.main was called to start the GUI
            windowMock.verify(() -> Window.main(args), times(1));
            // verify console simulation was NOT run
            verify(mockSimulation, never()).runSimulation(anyDouble());
        }
    }

    /**
     * test to verify that in console mode (GUI disabled) without writing results,
     * it runs the simulation and skips writing
     **/
    @Test
    void main_ConsoleMode_WritingResultsFalse_ShouldRunSimulationOnly() {
        try (MockedStatic<InputParametersHandeler> inputMock = mockStatic(InputParametersHandeler.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<ConfigLoader> configMock = mockStatic(ConfigLoader.class);
             MockedStatic<Window> windowMock = mockStatic(Window.class);
             MockedStatic<ResultsRecorder> recorderMock = mockStatic(ResultsRecorder.class)) {

            inputMock.when(() -> InputParametersHandeler.getDurationFromParameter(any())).thenReturn(50);
            configMock.when(() -> ConfigLoader.loadAllConfig(any(), any(), any(), anyInt(), any(), anyInt(), any())).thenReturn(true);

            runDetails.showGui = false;
            runDetails.duration = 50;
            when(runDetails.writingResults()).thenReturn(false);

            // Act
            Main.main(new String[]{});

            // Assert
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("Starting simulation in console mode"), anyString()), times(1));
            // verify simulation was run with the correct duration
            verify(mockSimulation, times(1)).runSimulation(50);
            // verify writing results was skipped
            recorderMock.verify(ResultsRecorder::getResultsRecorder, never());
        }
    }

    /**
     * test to verify that in console mode with writing results enabled,
     * it runs the simulation and successfully writes the results to file
     **/
    @Test
    void main_ConsoleMode_WritingResultsTrue_ShouldRunSimulationAndWriteResults() {
        try (MockedStatic<InputParametersHandeler> inputMock = mockStatic(InputParametersHandeler.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<ConfigLoader> configMock = mockStatic(ConfigLoader.class);
             MockedStatic<Window> windowMock = mockStatic(Window.class);
             MockedStatic<ResultsRecorder> recorderStaticMock = mockStatic(ResultsRecorder.class)) {

            inputMock.when(() -> InputParametersHandeler.getDurationFromParameter(any())).thenReturn(100);
            configMock.when(() -> ConfigLoader.loadAllConfig(any(), any(), any(), anyInt(), any(), anyInt(), any())).thenReturn(true);

            runDetails.showGui = false;
            runDetails.duration = 100;
            when(runDetails.writingResults()).thenReturn(true);

            // mock the singleton recorder instance
            recorderStaticMock.when(ResultsRecorder::getResultsRecorder).thenReturn(mockRecorder);

            // Act
            Main.main(new String[]{});

            // Assert
            verify(mockSimulation, times(1)).runSimulation(100);
            // verify results writing was triggered
            verify(mockRecorder, times(1)).writeResults();
        }
    }
}
