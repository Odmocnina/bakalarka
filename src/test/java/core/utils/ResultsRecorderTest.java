package core.utils;

import app.AppContext;
import core.model.Road;
import core.utils.constants.ConfigConstants;
import core.utils.constants.Constants;
import models.ICarFollowingModel;
import models.ILaneChangingModel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/************************
 * Unit tests for ResultsRecorder class, focusing on singleton behavior, time measurement, metric recording, and file
 * output generation
 *
 * @author Michael Hladky
 * @version 1.0
 ************************/
@ExtendWith(MockitoExtension.class)
public class ResultsRecorderTest {

    /** Temporary directory provided by JUnit for file-based tests, it ensures isolation and cleanup after tests **/
    @TempDir
    File tempDir;

    /** Instance of ResultsRecorder to be used across tests, it will be reset before each test to ensure a clean
     * state **/
    private ResultsRecorder recorder;

    /** mock road object to be used in tests, it will be set up with default behavior to prevent NullPointerExceptions
     * when ResultsRecorder tries to access its properties **/
    private Road mockRoad;

    /** Mocked static class for MyLogger to prevent actual logging during tests, it allows us to verify that logging
     * calls are made without relying on the actual logging implementation, and it prevents side effects from
     * logging during test execution **/
    private MockedStatic<MyLogger> mockedLogger;

    /**
     * setup method to initialize the test environment before each test, it resets the ResultsRecorder singleton
     * instance to ensure that each test starts with a clean state, and it prepares necessary mocks for the road and
     * logging to prevent NullPointerExceptions and side effects during testing
     **/
    @BeforeEach
    void setUp() throws Exception {
        Field instanceField = ResultsRecorder.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        recorder = ResultsRecorder.getResultsRecorder();

        // silence the logger to prevent side effects during testing
        mockedLogger = mockStatic(MyLogger.class);

        // Reset AppContext and set up necessary mocks for RunDetails and OutputDetails to prevent NullPointerExceptions
        // during tests
        AppContext.RUN_DETAILS = new RunDetails();
        OutputDetails mockOutputDetails = spy(new OutputDetails());
        mockOutputDetails.csvSeparator = ",";
        lenient().doReturn(true).when(mockOutputDetails).writePart(anyString());
        AppContext.RUN_DETAILS.outputDetails = mockOutputDetails;

        // mocked road object
        mockRoad = mock(Road.class);
        lenient().when(mockRoad.getNumberOfLanes()).thenReturn(2);
        lenient().when(mockRoad.toString()).thenReturn("MockRoad_1");
        lenient().when(mockRoad.getNumberOfCarsOnRoad()).thenReturn(5);

        // mack car following and lane changing models to prevent NullPointerExceptions when ResultsRecorder tries to
        // access their properties
        ICarFollowingModel cfm = mock(ICarFollowingModel.class);
        lenient().when(cfm.getName()).thenReturn("IDM");
        lenient().when(cfm.getType()).thenReturn("continuous");
        AppContext.CAR_FOLLOWING_MODEL = cfm;

        ILaneChangingModel lcm = mock(ILaneChangingModel.class);
        lenient().when(lcm.getName()).thenReturn("MOBIL");
        AppContext.LANE_CHANGING_MODEL = lcm;

        // create a mock simulation object that returns our mocked road and a step count, and set it in AppContext using
        // reflection
        Field simField = AppContext.class.getDeclaredField("SIMULATION");
        simField.setAccessible(true);
        Object mockSimulation = mock(simField.getType(), invocation -> {
            String methodName = invocation.getMethod().getName();
            if (methodName.equals("getRoads")) {
                return new Road[]{mockRoad};
            }
            if (methodName.equals("getStepCount")) {
                return 10;
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        });
        simField.set(null, mockSimulation);
    }

    @AfterEach
    void tearDown() {
        if (mockedLogger != null) {
            mockedLogger.close();
        }
    }

    /**
     * test to verify that the Singleton pattern correctly returns the same instance
     **/
    @Test
    void getResultsRecorder_ShouldReturnSameSingletonInstance() {
        ResultsRecorder anotherReference = ResultsRecorder.getResultsRecorder();
        assertSame(recorder, anotherReference, "Singleton pattern should return the exact same instance");
    }

    /**
     * test to verify that time measuring logic accurately returns an elapsed time greater than 0
     **/
    @Test
    void timer_ShouldMeasureElapsedTime() throws InterruptedException {
        recorder.startTimer();
        Thread.sleep(5); // Wait briefly to accumulate time
        recorder.stopTimer();

        BigInteger elapsed = recorder.getElapsedTimeNs();
        assertTrue(elapsed.compareTo(BigInteger.ZERO) > 0, "Elapsed time should be logically greater than 0");
    }

    /**
     * test to verify that basic metrics like cars passed and road emptiness are recorded and retrieved
     **/
    @Test
    void recordMetrics_ShouldStoreAndRetrieveValuesCorrectly() {
        recorder.initialize(new Road[]{mockRoad}, "testFile.txt");

        // Act
        recorder.recordCarPassed(0);
        recorder.recordCarsPassed(0, 3);
        recorder.recordRoadEmpty(0, 42);

        // Assert
        assertEquals(4, recorder.getCarsPassedOnRoad(0), "Cars passed should sum up to 4 (1 + 3)");
        assertTrue(recorder.wasRoadAlreadyEmpty(0), "Road should be marked as empty");
    }

    /**
     * test to verify that resetCarNumbers clears all internal arrays and resets counters
     **/
    @Test
    void resetCarNumbers_ShouldClearAllData() {
        recorder.initialize(new Road[]{mockRoad}, "testFile.txt");
        recorder.recordCarPassed(0);
        recorder.recordRoadEmpty(0, 10);

        // Act
        recorder.resetCarNumbers();

        // Assert
        assertEquals(0, recorder.getCarsPassedOnRoad(0), "Cars passed should be reset to 0");
        assertFalse(recorder.wasRoadAlreadyEmpty(0), "Road emptiness flag should be reset");
    }

    /**
     * test to verify that writing results to TXT format outputs a file
     * with appropriate headers and accumulated text data
     **/
    @Test
    void writeResultsTxt_ShouldCreateAndPopulateTextFile() throws Exception {
        // Arrange
        File outputFile = new File(tempDir, "resultsOutput"); // Intentionally no extension
        recorder.initialize(new Road[]{mockRoad}, outputFile.getAbsolutePath());
        recorder.recordCarPassed(0);
        recorder.addCollision(0);

        // Disable separate file export to test inline generation
        doAnswer(invocation -> {
            String tag = invocation.getArgument(0);
            return !tag.equals(ConfigConstants.EXPORT_DETAILED_TO_SEPARATE_FILES_TAG);
        }).when(AppContext.RUN_DETAILS.outputDetails).writePart(anyString());

        // Act
        recorder.writeResultsTxt();

        // Assert
        File createdFile = new File(tempDir, "resultsOutput.txt"); // It should auto-append .txt
        assertTrue(createdFile.exists(), "The TXT file should be created");

        String content = Files.readString(createdFile.toPath());
        assertTrue(content.contains("=== Traffic Simulation Results ==="), "File should contain main header");
        assertTrue(content.contains("Road 0: 1 cars passed"), "File should contain cars passed data");
        assertTrue(content.contains("Road 0: 1 collisions"), "File should contain collisions data");
    }

    /**
     * test to verify that writing results to CSV format outputs a file
     * with the correct comma separator and columns
     **/
    @Test
    void writeResultsCsv_ShouldCreateAndPopulateCsvFile() throws Exception {
        // Arrange
        File outputFile = new File(tempDir, "dataOutput.csv");
        recorder.initialize(new Road[]{mockRoad}, outputFile.getAbsolutePath());
        recorder.recordCarPassed(0);

        // Act
        recorder.writeResultsCsv();

        // Assert
        assertTrue(outputFile.exists(), "The CSV file should be created");

        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("Road Index,Cars Passed"), "File should contain correct CSV headers");
        assertTrue(content.contains("0,1"), "File should contain the correct recorded values separated by comma");
    }

    /**
     * test to verify that processDetailedOutputs creates sub-folders for each road
     * and exports LightPlans and LaneQueues into separate specific CSV files
     **/
    @Test
    void processDetailedOutputs_ShouldCreateSeparateFilesAndFolders() throws Exception {
        // Arrange
        File outputFile = new File(tempDir, "baseName.txt");
        recorder.initialize(new Road[]{mockRoad}, outputFile.getAbsolutePath());

        // Record 5 standing cars on red in lane 0, and 2 standing on green in lane 1
        recorder.recordNumberOfStoppedCars(5, true, 0, 0);
        recorder.recordNumberOfStoppedCars(2, false, 0, 1);

        // Act - By default our spy returns true for EXPORT_DETAILED_TO_SEPARATE_FILES_TAG
        recorder.writeResultsTxt();

        // Assert
        File exportDir = new File(tempDir, "baseNameDetailedExport");
        assertTrue(exportDir.exists(), "Detailed export directory should be created");

        File roadDir = new File(exportDir, "road0");
        assertTrue(roadDir.exists(), "Directory specifically for road0 should be created");

        // Verify Lane Queue File
        File detailedLaneQueueFile = new File(roadDir, "DetailedLaneQueue.csv");
        assertTrue(detailedLaneQueueFile.exists(), "DetailedLaneQueue.csv should exist in road directory");
        String dlqContent = Files.readString(detailedLaneQueueFile.toPath());
        assertTrue(dlqContent.contains("Step,Lane 0,Lane 1"), "Queue file should contain lane headers");
        assertTrue(dlqContent.contains("5"), "Queue file should contain recorded stopped cars");

        // Verify Light Plan File
        File lightPlanFile = new File(roadDir, "LightPlanOfAllRoads.csv");
        assertTrue(lightPlanFile.exists(), "LightPlanOfAllRoads.csv should exist in road directory");
        String lpContent = Files.readString(lightPlanFile.toPath());
        assertTrue(lpContent.contains("Was red"), "Light plan file should record the red state");
        assertTrue(lpContent.contains("Was green"), "Light plan file should record the green state");
    }
}