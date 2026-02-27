package core.utils;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.model.Road;
import core.model.cellular.CellularRoad;
import core.model.continous.ContinuosRoad;
import core.utils.constants.Constants;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/************************
 * Unit tests for RoadParameters class, focusing on conversion between Road and RoadParameters,
 * handling of different car following model types, and generator configuration
 *
 * @author Michael Hladky
 * @version 1.0
 ************************/
@ExtendWith(MockitoExtension.class)
public class RoadParametersTest {

    /** Mocked car following model to control the behavior of AppContext dependencies during tests **/
    @Mock
    private ICarFollowingModel mockCarFollowingModel;

    /** Mocked lane changing model to control the behavior of AppContext dependencies during tests **/
    @Mock
    private ILaneChangingModel mockLaneChangingModel;

    /** Mocked static for MyLogger to prevent actual logging during tests and to verify that logging occurs when
     * expected **/
    private MockedStatic<MyLogger> mockedLogger;

    /**
     * setup method to initialize the test environment before each test, it mocks AppContext dependencies and silences
     * the logger
     **/
    @BeforeEach
    void setUp() {
        // Mock AppContext dependencies
        AppContext.CAR_FOLLOWING_MODEL = mockCarFollowingModel;
        AppContext.LANE_CHANGING_MODEL = mockLaneChangingModel;

        // Silence the logger to keep console output clean during tests
        mockedLogger = mockStatic(MyLogger.class);
    }

    /**
     * teardown method to clean up after each test, it ensures that mocked static resources are released to prevent
     * interference between tests and to allow proper garbage collection
     **/
    @AfterEach
    void tearDown() {
        if (mockedLogger != null) {
            mockedLogger.close();
        }
    }

    /**
     * test to verify that existingRoadsToRoadParameters correctly extracts
     * data from Road objects and converts them to a list of RoadParameters
     **/
    @Test
    void existingRoadsToRoadParameters_ShouldConvertCorrectly() {
        // Arrange
        Road mockRoad = mock(Road.class);
        when(mockRoad.getSpeedLimit()).thenReturn(50.0);
        when(mockRoad.getLength()).thenReturn(100.0);
        when(mockRoad.getNumberOfLanes()).thenReturn(2);

        LightPlan[] dummyLights = new LightPlan[]{mock(LightPlan.class), mock(LightPlan.class)};
        CarGenerator[] dummyGenerators = new CarGenerator[]{mock(CarGenerator.class), mock(CarGenerator.class)};

        when(mockRoad.getLightPlans()).thenReturn(dummyLights);
        when(mockRoad.getCarGenerators()).thenReturn(dummyGenerators);

        // Array with one mock road and one null (to test null skipping)
        Road[] roads = new Road[]{mockRoad, null};

        // Act
        ArrayList<RoadParameters> result = RoadParameters.existingRoadsToRoadParameters(roads);

        // Assert
        assertEquals(1, result.size(), "Resulting list should contain exactly 1 item, skipping the null road");

        RoadParameters rp = result.get(0);
        assertEquals(50.0, rp.maxSpeed, "Max speed should be copied correctly");
        assertEquals(100.0, rp.length, "Length should be copied correctly");
        assertEquals(2, rp.lanes, "Number of lanes should be copied correctly");
        assertEquals(2, rp.lightPlan.size(), "Light plans should be copied correctly");
        assertEquals(2, rp.carGenerators.size(), "Car generators should be copied correctly");
    }

    /**
     * test to verify that roadParametersToRoads creates CellularRoad instances
     * when the AppContext specifies the CELLULAR model type
     **/
    @Test
    void roadParametersToRoads_ShouldCreateCellularRoad_WhenTypeIsCellular() {
        // Arrange
        when(mockCarFollowingModel.getType()).thenReturn(Constants.CELLULAR);
        when(mockCarFollowingModel.getCellSize()).thenReturn(5.0);
        when(mockCarFollowingModel.getParametersForGeneration()).thenReturn("speed");
        when(mockLaneChangingModel.getParametersForGeneration()).thenReturn("lane");

        ArrayList<RoadParameters> list = new ArrayList<>();
        RoadParameters rp = new RoadParameters();
        rp.length = 100.0;
        rp.lanes = 2;
        rp.maxSpeed = 50.0;
        rp.lightPlan = new LinkedList<>();
        rp.lightPlan.add(mock(LightPlan.class));
        rp.lightPlan.add(mock(LightPlan.class));
        rp.carGenerators = new LinkedList<>();
        rp.carGenerators.add(mock(CarGenerator.class));
        rp.carGenerators.add(mock(CarGenerator.class));
        list.add(rp);

        // Smart mock for DefaultStuffMaker to prevent NullPointerExceptions in Road constructor
        try (MockedStatic<DefaultStuffMaker> mockedMaker = mockStatic(DefaultStuffMaker.class, invocation -> {
            String methodName = invocation.getMethod().getName();
            if (methodName != null && methodName.contains("createDefault")) {
                LinkedList<Object> dummyList = new LinkedList<>();
                if (methodName.contains("Light")) {
                    dummyList.add(mock(LightPlan.class));
                    dummyList.add(mock(LightPlan.class));
                    dummyList.add(mock(LightPlan.class));
                } else {
                    dummyList.add(mock(CarGenerator.class));
                    dummyList.add(mock(CarGenerator.class));
                    dummyList.add(mock(CarGenerator.class));
                }
                return dummyList;
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        })) {
            // Act
            Road[] result = RoadParameters.roadParametersToRoads(list);

            // Assert
            assertEquals(1, result.length, "Result should contain 1 road");
            assertInstanceOf(CellularRoad.class, result[0], "The created road should be of type CellularRoad");
            assertEquals(100.0, result[0].getLength(), "Road length should be assigned correctly");
            assertEquals(2, result[0].getNumberOfLanes(), "Number of lanes should be assigned correctly");
        }
    }

    /**
     * test to verify that roadParametersToRoads creates ContinuosRoad instances
     * when the AppContext specifies the CONTINUOUS model type
     **/
    @Test
    void roadParametersToRoads_ShouldCreateContinuosRoad_WhenTypeIsContinuous() {
        // Arrange
        when(mockCarFollowingModel.getType()).thenReturn(Constants.CONTINUOUS);
        when(mockCarFollowingModel.getParametersForGeneration()).thenReturn("speed");
        when(mockLaneChangingModel.getParametersForGeneration()).thenReturn("lane");

        ArrayList<RoadParameters> list = new ArrayList<>();
        RoadParameters rp = new RoadParameters();
        rp.length = 100.0;
        rp.lanes = 1;
        rp.maxSpeed = 50.0;
        list.add(rp);

        try (MockedStatic<DefaultStuffMaker> mockedMaker = mockStatic(DefaultStuffMaker.class, invocation -> {
            String methodName = invocation.getMethod().getName();
            if (methodName != null && methodName.contains("createDefault")) {
                LinkedList<Object> dummyList = new LinkedList<>();
                if (methodName.contains("Light")) {
                    dummyList.add(mock(LightPlan.class));
                } else {
                    dummyList.add(mock(CarGenerator.class));
                }
                return dummyList;
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        })) {
            // Act
            Road[] result = RoadParameters.roadParametersToRoads(list);

            // Assert
            assertEquals(1, result.length, "Result should contain 1 road");
            assertTrue(result[0] instanceof ContinuosRoad, "The created road should be of type ContinuosRoad");
        }
    }

    /**
     * test to verify that an unknown model type logs an error and skips road creation
     **/
    @Test
    void roadParametersToRoads_ShouldLogAndSkip_WhenTypeIsUnknown() {
        // Arrange
        when(mockCarFollowingModel.getType()).thenReturn("UNKNOWN_MAGIC_TYPE"); // magic

        ArrayList<RoadParameters> list = new ArrayList<>();
        list.add(new RoadParameters()); // Add one empty valid parameter

        // Act
        Road[] result = RoadParameters.roadParametersToRoads(list);

        // Assert
        assertEquals(1, result.length, "Result array size should match input list size");
        assertNull(result[0], "The road should be null because the type was unknown and creation was skipped");

        // Verify that logger was called to report the error
        mockedLogger.verify(() -> MyLogger.log(contains("Unknown car following model type"), eq(Constants.ERROR_FOR_LOGGING)));
    }

    /**
     * test to verify that setGenerateLengthAsOneOnAllGenerators iterates through
     * all generators and updates their length generation flag
     **/
    @Test
    void setGenerateLengthAsOneOnAllGenerators_ShouldUpdateAllGenerators() {
        // Arrange
        ArrayList<RoadParameters> list = new ArrayList<>();
        RoadParameters rp = new RoadParameters();

        CarGenerator mockGenerator1 = mock(CarGenerator.class);
        CarGenerator mockGenerator2 = mock(CarGenerator.class);

        rp.carGenerators = new LinkedList<>();
        rp.carGenerators.add(mockGenerator1);
        rp.carGenerators.add(mockGenerator2);
        list.add(rp);

        // Act
        RoadParameters.setGenerateLengthAsOneOnAllGenerators(list, true);

        // Assert
        verify(mockGenerator1).setLengthReturnAsOne(true);
        verify(mockGenerator2).setLengthReturnAsOne(true);
    }

    /**
     * test to verify that handleSettingOfLengthGeneration activates the length flag
     * only for specific cellular models (nagel-schreckenberg, rule-184)
     **/
    @Test
    void handleSettingOfLengthGeneration_ShouldTriggerForSpecificModelsOnly() {
        // Arrange
        ArrayList<RoadParameters> list = new ArrayList<>();
        RoadParameters rp = new RoadParameters();
        CarGenerator mockGenerator = mock(CarGenerator.class);
        rp.carGenerators = new LinkedList<>();
        rp.carGenerators.add(mockGenerator);
        list.add(rp);

        // Act & Assert - Should trigger for Nagel-Schreckenberg
        RoadParameters.handleSettingOfLengthGeneration(list, "nagel-schreckenberg");
        verify(mockGenerator, times(1)).setLengthReturnAsOne(true);

        // Act & Assert - Should trigger for Rule-184
        RoadParameters.handleSettingOfLengthGeneration(list, "rule-184");
        verify(mockGenerator, times(2)).setLengthReturnAsOne(true);

        // Act & Assert - Should NOT trigger for IDM (continuous model)
        RoadParameters.handleSettingOfLengthGeneration(list, "idm");
        verify(mockGenerator, times(2)).setLengthReturnAsOne(true); // Call count should remain 2
    }
}