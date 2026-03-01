package core.sim;

import app.AppContext;
import core.model.CarGenerator;
import core.model.Road;
import core.utils.MyLogger;
import core.utils.OutputDetails;
import core.utils.ResultsRecorder;
import core.utils.RunDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/*********************************************
 * Unit tests for the Simulation class, covering various scenarios of stepping through the simulation, checking for
 * empty roads, and resetting the simulation state. Uses Mockito to mock dependencies and verify interactions with the
 * ResultsRecorder and MyLogger.
 *
 * @author Michael Hladky
 * @version 1.0
 *********************************************/
@ExtendWith(MockitoExtension.class)
public class SimulationTest {

    /** instance of the Simulation class to be tested **/
    private Simulation simulation;

    /** mock road 1 to be used in the simulation **/
    @Mock
    private Road mockRoad1;

    /** mock road 2 to be used in the simulation **/
    @Mock
    private Road mockRoad2;

    /** mock ResultsRecorder to verify interactions when writing results **/
    @Mock
    private ResultsRecorder mockRecorder;

    /** mock RunDetails to control the simulation settings and verify interactions **/
    @Mock
    private RunDetails mockRunDetails;

    /**
     * method to set up the simulation instance and configure the AppContext before each test, ensuring that the
     * simulation is initialized with mocked roads and that the AppContext has a mock RunDetails to avoid
     * NullPointerExceptions
     **/
    @BeforeEach
    void setUp() {
        // initialize simulation with mocked roads
        Road[] roads = new Road[]{mockRoad1, mockRoad2};
        simulation = new Simulation(roads);

        // setup AppContext
        AppContext.RUN_DETAILS = mockRunDetails;
        AppContext.RUN_DETAILS.outputDetails = mock(OutputDetails.class);
    }

    /**
     * test to verify that step() correctly updates roads, records results, and starts/stops
     * the timer when writingResults is true
     **/
    @Test
    void step_WritingResultsTrue_ShouldStartTimerRecordAndStopTimerAtEnd() {
        // Arrange
        when(mockRunDetails.writingResults()).thenReturn(true);
        mockRunDetails.duration = 2; // simulation duration is 2 steps (0 and 1)

        when(mockRoad1.updateRoad()).thenReturn(5); // 5 cars passed
        when(mockRoad2.updateRoad()).thenReturn(3); // 3 cars passed

        try (MockedStatic<ResultsRecorder> recorderStaticMock = mockStatic(ResultsRecorder.class)) {
            recorderStaticMock.when(ResultsRecorder::getResultsRecorder).thenReturn(mockRecorder);

            // Act - Step 0 (First step)
            simulation.step();

            // Assert - First step
            verify(mockRecorder, times(1)).startTimer();
            verify(mockRecorder, times(1)).recordCarsPassed(0, 5);
            verify(mockRecorder, times(1)).recordCarsPassed(1, 3);

            verify(mockRoad1, times(1)).updateLights(1);
            assertEquals(1, simulation.getStepCount(), "Step count should be incremented to 1");
            verify(mockRecorder, never()).stopTimer(); // shouldn't stop timer yet because duration is not reached

            // Act - Step 1 (Last step)
            // we need to use reflection to set 'running' to true to satisfy the stopTimer condition
            setPrivateField(simulation, "running", true);
            simulation.step();

            // Assert - Last step
            verify(mockRecorder, times(1)).startTimer(); // still only 1 call from step 0
            verify(mockRecorder, times(2)).recordCarsPassed(0, 5);

            verify(mockRoad1, times(1)).updateLights(2);
            assertEquals(2, simulation.getStepCount(), "Step count should be incremented to 2");
            verify(mockRecorder, times(1)).stopTimer(); // timer should be stopped now
        }
    }

    /**
     * test to verify that runSimulation loops correctly and ends early if all roads and queues are empty
     **/
    @Test
    void runSimulation_AllRoadsEmpty_ShouldEndEarly() {
        // Arrange
        try (MockedStatic<ResultsRecorder> recorderStaticMock = mockStatic(ResultsRecorder.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            recorderStaticMock.when(ResultsRecorder::getResultsRecorder).thenReturn(mockRecorder);
            when(mockRunDetails.writingResults()).thenReturn(false);

            // simulate that roads become empty immediately
            when(mockRoad1.getNumberOfCarsOnRoad()).thenReturn(0);
            when(mockRoad1.areAllGeneratorsGeneratingToQueue()).thenReturn(true);
            when(mockRoad1.areAllQueuesEmpty()).thenReturn(true);
            when(mockRoad2.getNumberOfCarsOnRoad()).thenReturn(0);
            when(mockRoad2.areAllGeneratorsGeneratingToQueue()).thenReturn(true);
            when(mockRoad2.areAllQueuesEmpty()).thenReturn(true);

            // Act
            // run for 10 steps, but it should end at step 1 due to early exit
            simulation.runSimulation(10.0);

            // Assert
            assertEquals(1, simulation.getStepCount(), "Simulation should end early at step 1");
            verify(mockRecorder, times(1)).stopTimer();
            loggerMock.verify(() -> MyLogger.log(contains("ending simulation early"), anyString()), times(1));
        }
    }

    /**
     * test to verify that areAllRoadsAndQueuesEmpty logs when a road becomes empty for the first time
     **/
    @Test
    void areAllRoadsAndQueuesEmpty_RoadEmptyFirstTime_ShouldLogAndRecord() {
        // Arrange
        try (MockedStatic<ResultsRecorder> recorderStaticMock = mockStatic(ResultsRecorder.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            recorderStaticMock.when(ResultsRecorder::getResultsRecorder).thenReturn(mockRecorder);

            when(mockRoad1.getNumberOfCarsOnRoad()).thenReturn(0);
            when(mockRoad1.areAllGeneratorsGeneratingToQueue()).thenReturn(true);
            when(mockRoad1.areAllQueuesEmpty()).thenReturn(true);
            when(mockRoad1.getId()).thenReturn(1);

            // simulate that it wasn't empty before
            when(mockRecorder.wasRoadAlreadyEmpty(1)).thenReturn(false);

            // Act
            boolean result = simulation.areAllRoadsAndQueuesEmpty(new Road[]{mockRoad1});

            // Assert
            assertTrue(result, "Should return true for empty road");
            loggerMock.verify(() -> MyLogger.log(contains("Road 1 is empty"), anyString()), times(1));
            verify(mockRecorder, times(1)).recordRoadEmpty(eq(1), anyInt());
        }
    }

    /**
     * test to verify areAllQueuesEmpty and areAllRoadsEmpty methods
     **/
    @Test
    void simpleEmptyChecks_ShouldReturnCorrectBooleans() {
        // Arrange
        when(mockRoad1.areAllQueuesEmpty()).thenReturn(false);
        when(mockRoad1.getNumberOfCarsOnRoad()).thenReturn(5);

        // Act & Assert
        assertFalse(simulation.areAllQueuesEmpty(simulation.getRoads()), "Should return false as road1 queue is not empty");
        assertFalse(simulation.areAllRoadsEmpty(simulation.getRoads()), "Should return false as road1 has cars");
    }

    /**
     * test to verify getFlowRate extracts flow rate from the first road's generator
     **/
    @Test
    void getFlowRate_ShouldReturnLambdaFromFirstRoad() {
        // Arrange
        CarGenerator mockGenerator = mock(CarGenerator.class);
        when(mockRoad1.getCarGenerator()).thenReturn(mockGenerator);
        when(mockGenerator.getLambdaPerSec()).thenReturn(2.5);

        // Act
        double flowRate = simulation.getFlowRate();

        // Assert
        assertEquals(2.5, flowRate, "Flow rate should match the first road's generator lambda");
    }

    /**
     * test to verify step count even/odd logic
     **/
    @Test
    void isStepCountEven_ShouldReturnCorrectly() {
        // Act & Assert for step 0 (even)
        assertTrue(simulation.isStepCountEven(), "0 is even");

        // Act & Assert for step 1 (odd)
        when(mockRunDetails.writingResults()).thenReturn(false);
        simulation.step(); // increments stepCount to 1
        assertFalse(simulation.isStepCountEven(), "1 is odd");
    }

    /**
     * test to verify resetSimulationWithNewRoads resets everything and reinitialized recorder
     **/
    @Test
    void resetSimulationWithNewRoads_ShouldResetStateAndReinitializeRecorder() {
        // Arrange
        Road[] newRoads = new Road[]{mockRoad1};
        AppContext.RUN_DETAILS.outputDetails.outputFile = "test.csv";

        try (MockedStatic<ResultsRecorder> recorderStaticMock = mockStatic(ResultsRecorder.class)) {
            recorderStaticMock.when(ResultsRecorder::getResultsRecorder).thenReturn(mockRecorder);

            // Act
            simulation.resetSimulationWithNewRoads(newRoads);

            // Assert
            assertEquals(0, simulation.getStepCount(), "Step count should be 0");
            assertArrayEquals(newRoads, simulation.getRoads(), "Roads should be updated");
            verify(mockRoad1, times(1)).resetLightPlans();
            verify(mockRecorder, times(1)).resetCarNumbers();
            verify(mockRecorder, times(1)).initialize(newRoads, "test.csv");
        }
    }

    /**
     * test to verify that areAllRoadsEmpty returns true when no road has any cars on it
     **/
    @Test
    void areAllRoadsEmpty_AllEmpty_ShouldReturnTrue() {
        // Arrange: both roads return 0 for number of cars
        when(mockRoad1.getNumberOfCarsOnRoad()).thenReturn(0);
        when(mockRoad2.getNumberOfCarsOnRoad()).thenReturn(0);

        // Act
        boolean result = simulation.areAllRoadsEmpty(simulation.getRoads());

        // Assert
        assertTrue(result, "Should return true because all roads have 0 cars");
    }

    /**
     * test to verify that areAllQueuesEmpty returns true when all queues on all roads are empty
     **/
    @Test
    void areAllQueuesEmpty_AllEmpty_ShouldReturnTrue() {
        // Arrange: both roads report their queues are empty
        when(mockRoad1.areAllQueuesEmpty()).thenReturn(true);
        when(mockRoad2.areAllQueuesEmpty()).thenReturn(true);

        // Act
        boolean result = simulation.areAllQueuesEmpty(simulation.getRoads());

        // Assert
        assertTrue(result, "Should return true because all queues are explicitly empty");
    }

    /**
     * test to verify that areAllRoadsAndQueuesEmpty returns false if generators are NOT generating to queue
     * even if there are no cars on the road
     **/
    @Test
    void areAllRoadsAndQueuesEmpty_GeneratorsNotGeneratingToQueue_ShouldReturnFalse() {
        // Arrange
        try (MockedStatic<ResultsRecorder> recorderStaticMock = mockStatic(ResultsRecorder.class)) {
            recorderStaticMock.when(ResultsRecorder::getResultsRecorder).thenReturn(mockRecorder);

            // road has 0 cars, BUT generators are not generating to queue
            when(mockRoad1.getNumberOfCarsOnRoad()).thenReturn(0);
            when(mockRoad1.areAllGeneratorsGeneratingToQueue()).thenReturn(false);

            // Act
            boolean result = simulation.areAllRoadsAndQueuesEmpty(simulation.getRoads());

            // Assert
            assertFalse(result, "Should return false because not all generators are generating to queue");
        }
    }

    /**
     * test to verify that areAllRoadsAndQueuesEmpty returns false when there are cars waiting in the queue,
     * despite the actual road being empty of active cars
     **/
    @Test
    void areAllRoadsAndQueuesEmpty_QueuesNotEmpty_ShouldReturnFalse() {
        // Arrange
        try (MockedStatic<ResultsRecorder> recorderStaticMock = mockStatic(ResultsRecorder.class)) {
            recorderStaticMock.when(ResultsRecorder::getResultsRecorder).thenReturn(mockRecorder);

            // road has 0 cars, generators are generating to queue, BUT the queue itself is not empty
            when(mockRoad1.getNumberOfCarsOnRoad()).thenReturn(0);
            when(mockRoad1.areAllGeneratorsGeneratingToQueue()).thenReturn(true);
            when(mockRoad1.areAllQueuesEmpty()).thenReturn(false);

            // Act
            boolean result = simulation.areAllRoadsAndQueuesEmpty(simulation.getRoads());

            // Assert
            assertFalse(result, "Should return false because queues are not empty");
        }
    }

    /**
     * test to verify resetSimulationWithSameRoads clears cars and resets light plans
     **/
    @Test
    void resetSimulationWithSameRoads_ShouldClearCarsAndResetLightPlans() {
        // Arrange
        try (MockedStatic<ResultsRecorder> recorderStaticMock = mockStatic(ResultsRecorder.class)) {
            recorderStaticMock.when(ResultsRecorder::getResultsRecorder).thenReturn(mockRecorder);

            // Act
            simulation.resetSimulationWithSameRoads();

            // Assert
            assertEquals(0, simulation.getStepCount(), "Step count should be 0");
            verify(mockRoad1, times(1)).resetLightPlans();
            verify(mockRoad1, times(1)).removeAllCars(); // verified from private clearAllRoads
            verify(mockRecorder, times(1)).resetCarNumbers();
        }
    }

    /**
     * Helper to modify private fields via reflection
     */
    private void setPrivateField(Object object, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper to invoke private methods via reflection
     */
    private void invokePrivateMethod(String methodName) throws Exception {
        Method method = Simulation.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(simulation);
    }
}
