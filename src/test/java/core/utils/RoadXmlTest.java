package core.utils;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.model.Parameter;
import core.utils.constants.RoadLoadingConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/************************
 * Unit tests for RoadXml class, focusing on XML writing and map saving functionality
 *
 * @author Michael Hladky
 * @version 1.0
 ************************/
@ExtendWith(MockitoExtension.class)
public class RoadXmlTest {

    /** Temporary directory provided by JUnit for file-based tests, it ensures isolation and cleanup after tests **/
    @TempDir
    File tempDir;

    /** Mocked static classes to prevent side effects during testing, such as logging or default object creation **/
    private MockedStatic<MyLogger> mockedLogger;

    /** Mocked static for RoadParameters to control the behavior of existingRoadsToRoadParameters during tests,
     * allowing us to simulate different scenarios without relying on actual simulation data */
    private MockedStatic<RoadParameters> mockedRoadParameters;

    /** Mocked static for DefaultStuffMaker to provide controlled default objects when the code under test needs to fall
     * back on defaults, this allows us to verify that the fallback mechanism is working correctly without depending on
     * the actual implementation of DefaultStuffMaker **/
    private MockedStatic<DefaultStuffMaker> mockedStuffMaker;

    /**
     * setup method to initialize the test environment before each test, it resets the AppContext and prepares mocks
     **/
    @BeforeEach
    void setUp() throws Exception {
        AppContext.RUN_DETAILS = new RunDetails();

        // Silence the logger, so it doesn't spam the console or crash during tests
        mockedLogger = mockStatic(MyLogger.class);

        // Silence DefaultStuffMaker in case the code needs to supply missing elements
        mockedStuffMaker = mockStatic(DefaultStuffMaker.class);

        // create a mock simulation object and set it in AppContext using reflection
        Field simField = AppContext.class.getDeclaredField("SIMULATION");
        simField.setAccessible(true);
        Object mockSimulation = mock(simField.getType());
        simField.set(null, mockSimulation);

        // Prepare mocking for translating existing roads
        mockedRoadParameters = mockStatic(RoadParameters.class);
    }

    /**
     * tearDown method to clean up after each test, it ensures that all static mocks are properly closed to avoid
     * interference between tests and to release any resources they may be holding
     **/
    @AfterEach
    void tearDown() {
        // Proper cleanup of static mocks
        if (mockedLogger != null) mockedLogger.close();
        if (mockedRoadParameters != null) mockedRoadParameters.close();
        if (mockedStuffMaker != null) mockedStuffMaker.close();
    }

    /**
     * test to verify that writing an accurately populated road parameters list
     * successfully creates a valid XML file with proper root elements
     **/
    @Test
    void writeMapToXml_ShouldCreateValidXmlFile() throws Exception {
        // Arrange
        ArrayList<RoadParameters> list = new ArrayList<>();
        RoadParameters rp = new RoadParameters();
        rp.lanes = 1;
        rp.length = 100.0;
        rp.maxSpeed = 50.0;

        // Create a mock for LightPlan
        LightPlan mockLp = mock(LightPlan.class);
        when(mockLp.isLegitimate()).thenReturn(true);
        when(mockLp.getCycleTime()).thenReturn(60);
        LinkedList<LightPlan> lpList = new LinkedList<>();
        lpList.add(mockLp);
        rp.lightPlan = lpList;

        // Create a mock for CarGenerator
        CarGenerator mockCg = mock(CarGenerator.class);
        HashMap<String, Parameter> dummyParams = new HashMap<>();
        Parameter dummyParam = new Parameter("TestParam", 1.0, 5.0);
        dummyParams.put("TestTag", dummyParam);

        when(mockCg.getAllComParameters()).thenReturn(dummyParams);
        LinkedList<CarGenerator> cgList = new LinkedList<>();
        cgList.add(mockCg);
        rp.carGenerators = cgList;

        list.add(rp);

        // Create a path to the test file in the temporary folder
        File outputFile = new File(tempDir, "testMap.xml");

        // Act
        boolean result = RoadXml.writeMapToXml(list, 1, outputFile.getAbsolutePath());

        // Assert
        assertTrue(result, "writeMapToXml should return true indicating success");
        assertTrue(outputFile.exists(), "The XML file should be physically created on the disk");

        // Check the content of the generated file
        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("<" + RoadLoadingConstants.MAP_TAG + ">"), "File should contain root <map> element");
        assertTrue(content.contains("<" + RoadLoadingConstants.ROAD_TAG + ">"), "File should contain <road> element");
        assertTrue(content.contains("<TestTag>"), "File should contain the generated parameter tag");
    }

    /**
     * test to verify that invalid inputs (negative length/speed, missing plans)
     * are correctly caught and substituted with default fallback values
     **/
    @Test
    void writeMapToXml_WithInvalidInput_ShouldUseDefaults() throws Exception {
        // Arrange
        ArrayList<RoadParameters> list = new ArrayList<>();
        RoadParameters rp = new RoadParameters();
        rp.lanes = 1;
        rp.length = -5.0;        // bad length
        rp.maxSpeed = -10.0;     // bad speed
        rp.lightPlan = null;     // no light plan
        rp.carGenerators = null; // no car generator
        list.add(rp);

        // Prepare mocks for DefaultStuffMaker, so it has something to return as a fallback
        LightPlan defaultLp = mock(LightPlan.class);
        when(defaultLp.isLegitimate()).thenReturn(true);
        mockedStuffMaker.when(DefaultStuffMaker::createDefaultLightPlan).thenReturn(defaultLp);

        CarGenerator defaultCg = mock(CarGenerator.class);
        when(defaultCg.getAllComParameters()).thenReturn(new HashMap<>());
        mockedStuffMaker.when(DefaultStuffMaker::createDefaultGenerator).thenReturn(defaultCg);

        File outputFile = new File(tempDir, "defaultsMap.xml");

        // Act
        boolean result = RoadXml.writeMapToXml(list, 1, outputFile.getAbsolutePath());

        // Assert
        assertTrue(result, "Should successfully write XML despite invalid inputs");
        assertTrue(outputFile.exists(), "XML file should be generated using fallback defaults");

        // Verify that the code actually reached out to DefaultStuffMaker for default objects
        mockedStuffMaker.verify(DefaultStuffMaker::createDefaultLightPlan, atLeastOnce());
        mockedStuffMaker.verify(DefaultStuffMaker::createDefaultGenerator, atLeastOnce());
    }

    /**
     * test to verify that saveCurrentMap orchestrates the road extraction
     * and correctly passes data to the XML writer
     **/
    @Test
    void saveCurrentMap_ShouldCallWriteMapToXml_AndReturnTrue() {
        // Arrange
        AppContext.RUN_DETAILS.mapFile = new File(tempDir, "currentMap.xml").getAbsolutePath();
        AppContext.RUN_DETAILS.mapChanged = true;

        // Simulate that the current simulation returns some list of parameters
        ArrayList<RoadParameters> mockParams = new ArrayList<>();
        mockedRoadParameters.when(() -> RoadParameters.existingRoadsToRoadParameters(any()))
                .thenReturn(mockParams);

        // Act
        boolean result = RoadXml.saveCurrentMap();

        // Assert
        assertTrue(result, "saveCurrentMap should succeed on valid file path");
        assertFalse(AppContext.RUN_DETAILS.mapChanged, "mapChanged flag should be reset to false");
    }

    /**
     * test to verify that saveAs updates the map file name internally
     * and delegates successfully to saveCurrentMap
     **/
    @Test
    void saveAs_ShouldUpdateFileNameAndSave() {
        // Arrange
        String newFileName = new File(tempDir, "saveAsMap.xml").getAbsolutePath();

        // Empty list of roads, just so it passes without unnecessary operations
        mockedRoadParameters.when(() -> RoadParameters.existingRoadsToRoadParameters(any()))
                .thenReturn(new ArrayList<RoadParameters>());

        // Act
        boolean result = RoadXml.saveAs(newFileName);

        // Assert
        assertTrue(result, "saveAs should complete successfully");
        assertEquals(newFileName, AppContext.RUN_DETAILS.mapFile, "AppContext should be updated with the new map file name");
    }
}
