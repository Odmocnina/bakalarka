package core.model.continous;

import app.AppContext;
import core.model.*;
import core.utils.ResultsRecorder;
import core.utils.RunDetails;
import core.utils.constants.RequestConstants;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.AfterEach;
import java.util.HashMap;
import java.util.LinkedList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/************************
 * Unit tests for ContinuosRoad class, focusing on the updateRoad method and its interactions with car following and
 * lane changing models
 *
 * @author Michael Hladky
 * @version 1.0
 ************************/
@ExtendWith(MockitoExtension.class)
public class ContinuosRoadTest {

    /** instance of ContinuosRoad to be used in tests, initialized in setUp method with mocked dependencies **/
    private ContinuosRoad road;

    /** mock for the car following model, which will be injected into the global app context for use by ContinuosRoad
     * methods **/
    @Mock
    private ICarFollowingModel mockCarFollowingModel;

    /** mock for the lane changing model, which will be injected into the global app context for use by ContinuosRoad
     methods **/
    @Mock
    private ILaneChangingModel mockLaneChangingModel;

    /** mock for the car generator, which will be returned by the mocked static method in DefaultStuffMaker to provide
     necessary generators for ContinuosRoad constructor without needing to mock them individually in each test **/
    @Mock
    private CarGenerator mockGenerator;

    /** mocked static for ResultsRecorder, to provide a mock instance that can be verified for interactions in lane
     * change tests **/
    private MockedStatic<ResultsRecorder> mockedRecorderStatic;

    /** instance of ResultsRecorder that will be returned by the mocked static method, allowing us to verify
     * interactions such as recording lane changes **/
    private ResultsRecorder mockRecorderInstance;

    /** setup method to initialize the ContinuosRoad instance with mocked dependencies and set up the global app context
     * for use in the tests **/
    @BeforeEach
    void setUp() {
        mockRecorderInstance = mock(ResultsRecorder.class);
        mockedRecorderStatic = mockStatic(ResultsRecorder.class);
        mockedRecorderStatic.when(ResultsRecorder::getResultsRecorder).thenReturn(mockRecorderInstance);

        // setup global app context with necessary details and mocks for the models, so that ContinuosRoad can use them
        // without needing to mock them individually in each test
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.RUN_DETAILS.laneChange = true;
        AppContext.RUN_DETAILS.preventCollisions = true;
        AppContext.RUN_DETAILS.log = new boolean[6];

        // give models to global app context so that ContinuosRoad can use them without needing to mock them
        // individually
        AppContext.CAR_FOLLOWING_MODEL = mockCarFollowingModel;
        AppContext.LANE_CHANGING_MODEL = mockLaneChangingModel;

        // mocks for the models to return some default parameters when asked, to avoid null pointer exceptions in
        // ContinuosRoad methods
        lenient().when(mockCarFollowingModel.requestParameters()).thenReturn(RequestConstants.CURRENT_SPEED_REQUEST);
        lenient().when(mockLaneChangingModel.requestParameters(any())).thenReturn(RequestConstants.X_POSITION_REQUEST);

        // mock for CarGenerator to return some default parameters when asked, to avoid null pointer exceptions in
        // ContinuosRoad constructor
        lenient().when(mockGenerator.getCarGenerationParameters()).thenReturn(new String[0]);

        // setup pro ContinuosRoad, that uses mocked static methods to provide the necessary generators and light plans
        // without needing to mock them individually
        try (MockedStatic<core.utils.DefaultStuffMaker> mockedMaker = mockStatic(
                core.utils.DefaultStuffMaker.class,
                invocation -> {
                    String methodName = invocation.getMethod().getName();
                    Class<?> returnType = invocation.getMethod().getReturnType();

                    // if the method is asking for CarGenerators, return a list/array with our mockGenerator
                    if (methodName != null && methodName.contains("Generator")) {
                        if (java.util.List.class.isAssignableFrom(returnType)) {
                            LinkedList<Object> list = new LinkedList<>();
                            list.add(mockGenerator);
                            list.add(mockGenerator);
                            return list;
                        }
                        if (returnType.isArray()) {
                            return new CarGenerator[] { mockGenerator, mockGenerator };
                        }
                    }

                    // if the method is asking for LightPlans, return a list/array with dummy LightPlan mocks
                    if (methodName != null && (methodName.contains("Light") || methodName.contains("Plan") || methodName.contains("Traffic"))) {
                        core.model.LightPlan dummyPlan = mock(core.model.LightPlan.class);
                        if (java.util.List.class.isAssignableFrom(returnType)) {
                            LinkedList<Object> list = new LinkedList<>();
                            list.add(dummyPlan);
                            list.add(dummyPlan);
                            return list;
                        }
                        if (returnType.isArray()) {
                            return new core.model.LightPlan[] { dummyPlan, dummyPlan };
                        }
                    }

                    // if something else mockito is asking for, just return the default mock behavior (which is usually
                    // null or empty)
                    return Mockito.RETURNS_DEFAULTS.answer(invocation);
                })) {

            road = new ContinuosRoad(100.0, 2, 50.0, 1);

        }

        // final check to ensure that the mocked static is working as intended
        CarGenerator[] generators = new CarGenerator[2];
        generators[0] = mockGenerator;
        generators[1] = mockGenerator;
        road.setCarGenerators(generators);
    }

    @AfterEach
    void tearDown() {
        // clean up the mocked static after each test to avoid interference between tests
        if (mockedRecorderStatic != null) {
            mockedRecorderStatic.close();
        }
    }

    /**
     * test to verify that cars are correctly moved forward in updateRoad
     * based on the speed provided by the car following model
     **/
    @Test
    @SuppressWarnings("unchecked")
    void updateRoad_CarMovement_ShouldUpdateXPosition() {
        CarParams car = new CarParams();
        car.id = 1;
        car.lane = 0;
        car.xPosition = 10.0;
        car.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 5.0);
        car.setParameter(RequestConstants.LENGTH_REQUEST, 5.0);

        ((LinkedList<CarParams>[]) road.getContent())[0].add(car);

        when(mockCarFollowingModel.requestParameters()).thenReturn(RequestConstants.X_POSITION_REQUEST);
        when(mockCarFollowingModel.getNewSpeed(any())).thenReturn(10.0);

        road.updateRoad();

        assertEquals(20.0, car.xPosition, "Car should move to 20.0 (10 initial + 10 speed)");
        assertEquals(10.0, car.getParameter(RequestConstants.CURRENT_SPEED_REQUEST), "Speed should be updated to 10.0");
        assertFalse(car.processedInCurrentStep, "The processed flag should be reset to false after the full update step");
    }

    /**
     * test to verify that collision resolution reduces speed when a car is too close
     * to the one in front
     **/
    @Test
    @SuppressWarnings("unchecked")
    void updateRoad_CollisionResolution_ShouldReduceSpeed() {
        LinkedList<CarParams>[] vehicles = (LinkedList<CarParams>[]) road.getContent();

        // Back car at 40m
        CarParams backCar = new CarParams();
        backCar.lane = 0;
        backCar.xPosition = 40.0;
        backCar.setParameter(RequestConstants.LENGTH_REQUEST, 5.0);
        vehicles[0].add(backCar);

        // Front car at 50m
        CarParams frontCar = new CarParams();
        frontCar.lane = 0;
        frontCar.xPosition = 50.0;
        frontCar.setParameter(RequestConstants.LENGTH_REQUEST, 5.0);
        vehicles[0].add(frontCar);

        // tell car following model what to ask
        when(mockCarFollowingModel.requestParameters()).thenReturn(RequestConstants.X_POSITION_REQUEST);
        // first ask return 0 (speed of first car, then return 20 for the back car, which would cause a collision if not
        // resolved
        when(mockCarFollowingModel.getNewSpeed(any())).thenReturn(0.0, 20.0);

        road.updateRoad();

        // resolveCollision should return gap - 1.0
        assertEquals(4.0, backCar.getParameter(RequestConstants.CURRENT_SPEED_REQUEST),
                "Speed should be limited to 4.0 to avoid hitting the car in front");
    }

    /**
     * test to verify that cars are removed when they pass the end of the road
     **/
    @Test
    @SuppressWarnings("unchecked")
    void updateRoad_CarExiting_ShouldBeRemoved() {
        CarParams car = new CarParams();
        car.lane = 0;
        car.xPosition = 98.0; // Near the end of 100m road
        car.setParameter(RequestConstants.LENGTH_REQUEST, 5.0);
        ((LinkedList<CarParams>[]) road.getContent())[0].add(car);

        when(mockCarFollowingModel.requestParameters()).thenReturn(RequestConstants.X_POSITION_REQUEST);
        when(mockCarFollowingModel.getNewSpeed(any())).thenReturn(10.0); // Would move to 108.0

        road.updateRoad();

        assertEquals(0, road.getNumberOfCarsOnRoad(), "Car should be removed from the list after passing 100m");
    }

    /**
     * test to verify that lane changing logic moves a car to a different lane
     * when requested by the lane changing model
     **/
    @Test
    @SuppressWarnings("unchecked")
    void updateRoad_LaneChange_ShouldMoveCarToAnotherLane() {
        LinkedList<CarParams>[] vehicles = (LinkedList<CarParams>[]) road.getContent();
        CarParams car = new CarParams();
        car.id = 777;
        car.lane = 1; // Starting in lane 1
        car.xPosition = 20.0;
        car.setParameter(RequestConstants.LENGTH_REQUEST, 5.0);
        vehicles[1].add(car);

        when(mockCarFollowingModel.requestParameters()).thenReturn(RequestConstants.X_POSITION_REQUEST);
        when(mockCarFollowingModel.getNewSpeed(any())).thenReturn(5.0);

        when(mockLaneChangingModel.requestParameters(any())).thenReturn(RequestConstants.X_POSITION_REQUEST);
        when(mockLaneChangingModel.changeLaneIfDesired(any(), eq(Direction.LEFT))).thenReturn(Direction.LEFT);

        road.updateRoad();

        assertEquals(0, vehicles[1].size(), "Car should be gone from lane 1");
        assertEquals(1, vehicles[0].size(), "Car should be present in lane 0");
        assertEquals(0, car.lane, "Car's internal lane index should be updated to 0");

        verify(mockRecorderInstance).recordLaneChange(road.getId());
    }

    /**
     * test to verify that getParameters correctly identifies the car in front
     * and provides its xPosition for the model
     **/
    @Test
    @SuppressWarnings("unchecked")
    void getParameters_ForwardCar_ShouldProvideXPosition() throws Exception {
        LinkedList<CarParams>[] vehicles = (LinkedList<CarParams>[]) road.getContent();

        CarParams backCar = new CarParams();
        backCar.id = 1;
        backCar.lane = 0;
        backCar.xPosition = 10.0;
        vehicles[0].add(backCar);

        CarParams frontCar = new CarParams();
        frontCar.id = 2;
        frontCar.lane = 0;
        frontCar.xPosition = 50.0;
        vehicles[0].add(frontCar);

        String request = RequestConstants.X_POSITION_REQUEST + RequestConstants.SUBREQUEST_SEPARATOR +
                "FRONT" + RequestConstants.SUBREQUEST_SEPARATOR + "FORWARD";

        java.lang.reflect.Method method = ContinuosRoad.class.getDeclaredMethod("getParameters",
                CarParams.class, LinkedList[].class, String.class);
        method.setAccessible(true);

        HashMap<String, Double> result = (HashMap<String, Double>) method.invoke(road, backCar, vehicles, request);

        assertNotNull(result);
        assertEquals(50.0, result.get(request), "The map should contain front car's xPosition (50.0)");
    }

    /**
     * test to verify that removeAllCars successfully clears all vehicles from all lanes
     **/
    @Test
    @SuppressWarnings("unchecked")
    void removeAllCars_ShouldClearAllLanes() {
        LinkedList<CarParams>[] vehicles = (LinkedList<CarParams>[]) road.getContent();
        CarParams car1 = new CarParams();
        car1.lane = 0;
        vehicles[0].add(car1);

        CarParams car2 = new CarParams();
        car2.lane = 1;
        vehicles[1].add(car2);

        assertEquals(2, road.getNumberOfCarsOnRoad());

        road.removeAllCars();

        assertEquals(0, road.getNumberOfCarsOnRoad(), "All lanes should be empty after removeAllCars()");
    }
}