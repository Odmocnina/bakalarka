package core.model;

import app.AppContext;
import core.utils.DefaultStuffMaker;
import core.utils.ResultsRecorder;
import core.utils.RunDetails;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import models.ICarFollowingModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoadTest {

    /**************************
     * DummyRoad is a simple subclass of Road that allows us to test protected methods and abstract methods without
     * needing a full implementation. We override the abstract methods with empty implementations or fixed return values
     * as needed for our tests.
     *
     * @author Michael Hladky
     * @version 1.0
     **************************/
    private static class DummyRoad extends Road {

        /**
         * Constructor for DummyRoad that simply calls the superclass constructor with the provided parameters.
         *
         * @param length length of the road
         * @param numberOfLanes number of lanes on the road
         * @param speedLimit speed limit of the road
         * @param type type of the road (e.g., continuous, discrete)
         * @param id unique identifier for the road
         */
        public DummyRoad(double length, int numberOfLanes, double speedLimit, String type, int id) {
            super(length, numberOfLanes, speedLimit, type, id);
        }

        /**
         * placeCarAtStart is a protected method that we need to override, but for testing purposes we can leave it empty
         *
         * @param car the CarParams object representing the car to be placed
         * @param length the length of the car
         * @param lane the lane in which to place the car
         **/
        @Override
        protected void placeCarAtStart(CarParams car, double length, int lane) {
        }

        /**
         * okToPutCarAtStart is a protected method that we need to override, but for testing purposes we can simply
         * return true
         *
         * @param car the CarParams object representing the car to be placed
         * @param lane the lane in which to check if it's ok to place the car
         * @return true if it's ok to place the car at the start of the lane, false otherwise
         **/
        @Override
        protected boolean okToPutCarAtStart(CarParams car, int lane) {
            return true;
        }

        /**
         * getContent is an abstract method that we need to override, but for testing purposes we can simply return null
         *
         * @return always null
         **/
        @Override
        public Object getContent() {
            return null;
        }

        /**
         * updateRoad is an abstract method that we need to override, but for testing purposes we can simply return 0
         *
         * @return always 0
         **/
        @Override
        public int updateRoad() {
            return 0;
        }

        /**
         * getNumberOfCarsOnRoad is an abstract method that we need to override, but for testing purposes we can simply
         * return 0
         *
         * @return always 0
         **/
        @Override
        public int getNumberOfCarsOnRoad() {
            return 0;
        }

        /**
         * removeAllCars is an abstract method that we need to override, but for testing purposes we can leave it empty
         **/
        @Override
        public void removeAllCars() {
        }

        /**
         * countStoppedCarsInLane is a protected method that we need to override, but for testing purposes we can simply
         * return a fixed number (e.g., 5)
         *
         * @param lane the lane for which to count stopped cars
         * @return the number of stopped cars in the specified lane (fixed at 5 for testing)
         **/
        @Override
        protected int countStoppedCarsInLane(int lane) {
            return 5;
        }

        /**
         * public wrapper for the protected getRoadSimulationParameter method to allow testing of parameter retrieval
         * logic
         *
         * @param params the HashMap to which the retrieved parameter should be added
         * @param param the name of the parameter to retrieve
         **/
        public void publicGetRoadSimulationParameter(HashMap<String, Double> params, String param) {
            super.getRoadSimulationParameter(params, param);
        }
    }

    /** instance of DummyRoad used for testing **/
    private DummyRoad road;

    /** mocked generator **/
    @Mock
    private CarGenerator mockGenerator;

    /** mocked light plan **/
    @Mock
    private LightPlan mockLightPlan;

    /** mocked car following model **/
    @Mock
    private ICarFollowingModel mockCarFollowingModel;

    /** mocked static for ResultsRecorder to allow us to verify interactions with the singleton instance **/
    private MockedStatic<ResultsRecorder> mockedRecorderStatic;

    /** instance of the mocked ResultsRecorder that will be returned by the static getResultsRecorder method **/
    private ResultsRecorder mockRecorderInstance;

    /**
     * setup method to initialize the AppContext, mock static methods, and create a new DummyRoad instance before each
     * test
     **/
    @BeforeEach
    void setUp() {
        // 1. Setup AppContext
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.RUN_DETAILS.timeStep = 0.5; // for parameter testing
        AppContext.CAR_FOLLOWING_MODEL = mockCarFollowingModel;

        // 2. Mock ResultsRecorder globally for all tests
        mockRecorderInstance = mock(ResultsRecorder.class);
        mockedRecorderStatic = mockStatic(ResultsRecorder.class);
        mockedRecorderStatic.when(ResultsRecorder::getResultsRecorder).thenReturn(mockRecorderInstance);

        // 3. Mock DefaultStuffMaker to avoid NPEs during Road constructor
        try (MockedStatic<DefaultStuffMaker> mockedMaker = mockStatic(
                DefaultStuffMaker.class,
                invocation -> {
                    String methodName = invocation.getMethod().getName();
                    if (methodName != null && methodName.contains("Generator")) {
                        LinkedList<Object> list = new LinkedList<>();
                        list.add(mockGenerator);
                        list.add(mockGenerator);
                        return list;
                    }
                    if (methodName != null && (methodName.contains("Light") || methodName.contains("Plan"))) {
                        LinkedList<Object> list = new LinkedList<>();
                        list.add(mockLightPlan);
                        list.add(mockLightPlan);
                        return list;
                    }
                    return Mockito.RETURNS_DEFAULTS.answer(invocation);
                })) {

            // Create the DummyRoad (length: 100, lanes: 2, limit: 50, type: continuous, id: 1)
            road = new DummyRoad(100.0, 2, 50.0, Constants.CONTINUOUS, 1);
        }
    }

    /**
     * tearDown method to close the mocked static after each test to avoid interference between tests
     **/
    @AfterEach
    void tearDown() {
        if (mockedRecorderStatic != null) {
            mockedRecorderStatic.close();
        }
    }

    /**
     * test to verify that the constructor correctly initializes basic properties
     * and getters return correct values
     **/
    @Test
    void constructorAndGetters_ShouldReturnCorrectValues() {
        assertEquals(100.0, road.getLength(), "Length should be 100.0");
        assertEquals(2, road.getNumberOfLanes(), "Number of lanes should be 2");
        assertEquals(50.0, road.getSpeedLimit(), "Speed limit should be 50.0");
        assertEquals(Constants.CONTINUOUS, road.getType(), "Road type should be CONTINUOUS");
        assertEquals(1, road.getId(), "Road ID should be 1");
    }

    /**
     * test to verify that isLaneGreen correctly asks the light plan for its state,
     * and handles out-of-bounds lanes gracefully (returns true)
     **/
    @Test
    void isLaneGreen_ShouldReturnCorrectState() {
        // Arrange
        when(mockLightPlan.isGreen()).thenReturn(false);

        // Act & Assert
        assertFalse(road.isLaneGreen(0), "Should return false because mockLightPlan is red");
        assertTrue(road.isLaneGreen(99), "Should return true for out-of-bounds lane index");
    }

    /**
     * test to verify that updateLights calls tryToSwitchLight on all light plans
     **/
    @Test
    void updateLights_ShouldCallAllLightPlans() {
        int currentTime = 10;

        // Act
        road.updateLights(currentTime);

        // Assert - verify tryToSwitchLight was called on our mocks exactly 2 times (for 2 lanes)
        verify(mockLightPlan, times(2)).tryToSwitchLight(currentTime);
    }

    /**
     * test to verify that road parameters (like TIME_STEP and MAX_ROAD_SPEED)
     * are correctly retrieved and placed into the parameters map
     **/
    @Test
    void getRoadSimulationParameter_ShouldRetrieveCorrectParameters() {
        HashMap<String, Double> params = new HashMap<>();

        // Act - Request Time Step
        road.publicGetRoadSimulationParameter(params, RequestConstants.TIME_STEP_REQUEST);

        // Act - Request Max Speed
        road.publicGetRoadSimulationParameter(params, RequestConstants.MAX_ROAD_SPEED_REQUEST);

        // Assert
        assertEquals(0.5, params.get(RequestConstants.TIME_STEP_REQUEST), "Time step should match AppContext (0.5)");
        assertEquals(50.0, params.get(RequestConstants.MAX_ROAD_SPEED_REQUEST), "Max speed should be 50.0 for continuous road");
    }

    /**
     * test to verify that countStoppedCars properly gathers stopped cars from lanes
     * and delegates recording to the ResultsRecorder
     **/
    @Test
    void countStoppedCars_ShouldRecordResults() {
        // Arrange
        when(mockLightPlan.isGreen()).thenReturn(true);

        // Act
        road.countStoppedCars();

        // Assert - Should record 5 standing cars (from our stubbed DummyRoad method)
        // Called twice, because we have 2 lanes
        verify(mockRecorderInstance, times(2)).recordNumberOfStoppedCars(eq(5), eq(false), eq(road.getId()), anyInt());
    }

    /**
     * test to verify that addFromGenerator correctly generates a new car,
     * checks if it's ok to place it, increments the car ID, and attempts to place it
     **/
    @Test
    void addFromGenerator_ShouldGenerateAndAssignId() {
        // Arrange
        CarParams newCar = new CarParams();
        newCar.setParameter(RequestConstants.LENGTH_REQUEST, 5.0);

        when(mockGenerator.decideIfNewCar()).thenReturn(true);
        when(mockGenerator.generateCar()).thenReturn(newCar);

        int initialCarId = road.idOfCar;

        // Act
        road.addFromGenerator(0);

        // Assert
        assertEquals(initialCarId, newCar.id, "The generated car should be assigned the current road.idOfCar");
        assertEquals(initialCarId + 1, road.idOfCar, "The road.idOfCar counter should increment after adding a car");
    }

    /**
     * test to verify queue status methods when queues are not initialized or empty
     **/
    @Test
    void areAllQueuesEmpty_ShouldReturnTrueWhenNoCars() {
        // Initially queues are null
        assertTrue(road.areAllQueuesEmpty(), "Should return true if queues are null");
        assertFalse(road.areAllLanesQueue(), "Should return false if queues are null");

        // Initialize empty queues
        when(mockGenerator.generatingToQueue()).thenReturn(true);
        when(mockGenerator.generateCarsInToQueue()).thenReturn(new LinkedList<>());

        road.setUpQueuesIfNeeded();

        assertTrue(road.areAllQueuesEmpty(), "Should return true if initialized queues are empty");
        assertTrue(road.areAllLanesQueue(), "Should return true if all lanes have a queue object");
    }
}