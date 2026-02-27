package core.utils;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.model.Parameter;
import core.utils.constants.Constants;
import core.utils.constants.DefaultValues;
import core.utils.constants.RequestConstants;
import models.ICarFollowingModel;
import models.ILaneChangingModel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*******************************
 * Unit tests for DefaultStuffMaker class, focusing on the creation of default LightPlans and CarGenerators
 *
 * @author Michael Hladky
 * @version 1.0
 *******************************/
@ExtendWith(MockitoExtension.class)
public class DefaultStuffMakerTest {

    /** Mocked car following model used to provide necessary parameters for generator creation without relying on actual
     *  model implementations **/
    @Mock
    private ICarFollowingModel mockCarFollowingModel;

    /** Mocked lane changing model used to provide necessary parameters for generator creation without relying on actual
     *  model implementations **/
    @Mock
    private ILaneChangingModel mockLaneChangingModel;

    /** Setup method to prepare the AppContext with mocked dependencies before each test, ensuring that the createDefaultGenerator
     *  method can execute without issues and that we have control over the parameters it uses during creation **/
    @BeforeEach
    void setUp() {
        // Mock the AppContext dependencies needed by createDefaultGenerator
        AppContext.CAR_FOLLOWING_MODEL = mockCarFollowingModel;
        AppContext.LANE_CHANGING_MODEL = mockLaneChangingModel;

        // Leniently stub the methods called during generator creation
        lenient().when(mockCarFollowingModel.getParametersForGeneration()).thenReturn(RequestConstants.CURRENT_SPEED_REQUEST);
        lenient().when(mockLaneChangingModel.getParametersForGeneration()).thenReturn(RequestConstants.X_POSITION_REQUEST);
        lenient().when(mockCarFollowingModel.getType()).thenReturn(Constants.CONTINUOUS);
        lenient().when(mockCarFollowingModel.getCellSize()).thenReturn(1.0);
    }

    /**
     * Teardown method to clean up the AppContext after each test, ensuring that we do not leave any mocked
     * dependencies that could interfere with other tests in the suite
     **/
    @AfterEach
    void tearDown() {
        // Clean up AppContext to avoid side effects on other tests
        AppContext.CAR_FOLLOWING_MODEL = null;
        AppContext.LANE_CHANGING_MODEL = null;
    }

    /**
     * test to verify that createDefaultLightPlan returns a LightPlan initialized
     * with the correct default values from DefaultValues class
     **/
    @Test
    void createDefaultLightPlan_ShouldReturnCorrectlyInitializedLightPlan() {
        // Act
        LightPlan lp = DefaultStuffMaker.createDefaultLightPlan();

        // Assert
        assertNotNull(lp, "Created LightPlan should not be null");
        assertEquals(DefaultValues.DEFAULT_LIGHT_PLAN_CYCLE_DURATION, lp.getCycleTime(),
                "Cycle time should match default value");
        assertEquals(DefaultValues.DEFAULT_LIGHT_PLAN_START_WITH_GREEN, lp.isBeginsOnGreen(),
                "Start with green flag should match default value");
    }

    /**
     * test to verify that createDefaultLightPlan(int) returns a LinkedList
     * of the exact requested size containing properly initialized LightPlans
     **/
    @Test
    void createDefaultLightPlan_WithLanes_ShouldReturnListWithCorrectSize() {
        // Act
        int numberOfLanes = 3;
        LinkedList<LightPlan> lightPlans = DefaultStuffMaker.createDefaultLightPlan(numberOfLanes);

        // Assert
        assertNotNull(lightPlans, "Created list should not be null");
        assertEquals(numberOfLanes, lightPlans.size(), "List should contain exactly one LightPlan per lane");

        // Verify that the objects inside are properly initialized
        assertNotNull(lightPlans.get(0), "Elements inside the list should not be null");
    }

    /**
     * test to verify that createDefaultGenerator builds a CarGenerator
     * correctly configured with default values, queue settings, and parameters
     **/
    @Test
    void createDefaultGenerator_ShouldReturnCorrectlyConfiguredGenerator() {
        // Act
        CarGenerator generator = DefaultStuffMaker.createDefaultGenerator();

        // Assert
        assertNotNull(generator, "Created CarGenerator should not be null");

        // check default flow rate and queue settings
        assertEquals(DefaultValues.DEFAULT_FLOW_RATE, generator.getFlowRate(),
                "Flow rate should match default value");
        assertFalse(generator.generatingToQueue(), "Queue should be disabled by default");
        assertEquals(DefaultValues.DEFAULT_QUEUE_MIN_SIZE, generator.getMinQueueSize(),
                "Min queue size should match default value");
        assertEquals(DefaultValues.DEFAULT_QUEUE_MAX_SIZE, generator.getMaxQueueSize(),
                "Max queue size should match default value");

        // check that the expected parameters are present
        Map<String, Parameter> params = generator.getAllComParameters();
        assertNotNull(params, "Parameters map should be initialized");
        assertTrue(params.containsKey(RequestConstants.MAX_SPEED_REQUEST),
                "Generator should contain the max speed parameter");
        assertTrue(params.containsKey(RequestConstants.LENGTH_REQUEST),
                "Generator should contain the length parameter");

        // check that the parameters have the correct default min and max values
        Parameter lengthParam = params.get(RequestConstants.LENGTH_REQUEST);
        assertEquals(DefaultValues.DEFAULT_LENGTH_MIN, lengthParam.minValue, "Min length should match default");
        assertEquals(DefaultValues.DEFAULT_LENGTH_MAX, lengthParam.maxValue, "Max length should match default");
    }

    /**
     * test to verify that createDefaultGenerator(int) returns a LinkedList
     * of the exact requested size containing properly initialized CarGenerators
     **/
    @Test
    void createDefaultGenerator_WithLanes_ShouldReturnListWithCorrectSize() {
        // Act
        int numberOfLanes = 4;
        LinkedList<CarGenerator> generators = DefaultStuffMaker.createDefaultGenerator(numberOfLanes);

        // Assert
        assertNotNull(generators, "Created list should not be null");
        assertEquals(numberOfLanes, generators.size(), "List should contain exactly one CarGenerator per lane");

        // Verify that elements are valid CarGenerators
        assertNotNull(generators.get(0), "Elements inside the list should not be null");
        assertEquals(DefaultValues.DEFAULT_FLOW_RATE, generators.get(0).getFlowRate(),
                "Generators in list should be properly initialized");
    }
}