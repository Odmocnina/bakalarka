package core.model.cellular;

import app.AppContext;
import core.model.CarGenerator;
import core.model.CarParams;
import core.utils.ResultsRecorder;
import core.utils.RunDetails;
import core.utils.constants.RequestConstants;
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

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/************************
 * Unit tests for CellularRoad class, focusing on car placement, movement, and collision resolution logic
 *
 * @author Michael Hladky
 * @version 1.0
 ************************/
@ExtendWith(MockitoExtension.class)
public class CellularRoadTest {

    /** instance of CellularRoad to be used in tests **/
    private CellularRoad road;

    /** mock for ICarFollowingModel to control car speed behavior in tests **/
    @Mock
    private ICarFollowingModel mockCarFollowingModel;

    /** mock for ILaneChangingModel to control lane changing behavior in tests **/
    @Mock
    private ILaneChangingModel mockLaneChangingModel;

    /** mock for CarGenerator to control car generation behavior in tests **/
    @Mock
    private CarGenerator mockGenerator;

    /** mocked static for ResultsRecorder to prevent actual logging during tests **/
    private MockedStatic<ResultsRecorder> mockedRecorderStatic;

    /** instance of ResultsRecorder returned by the mocked static method **/
    private ResultsRecorder mockRecorderInstance;

    @BeforeEach
    void setUp() {
        // app context set up
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.RUN_DETAILS.laneChange = false;
        AppContext.RUN_DETAILS.preventCollisions = true;
        AppContext.RUN_DETAILS.log = new boolean[6];

        AppContext.CAR_FOLLOWING_MODEL = mockCarFollowingModel;
        AppContext.LANE_CHANGING_MODEL = mockLaneChangingModel;

        lenient().when(mockCarFollowingModel.requestParameters()).thenReturn(RequestConstants.CURRENT_SPEED_REQUEST);
        lenient().when(mockLaneChangingModel.requestParameters(any())).thenReturn(RequestConstants.X_POSITION_REQUEST);
        lenient().when(mockGenerator.getCarGenerationParameters()).thenReturn(new String[0]);

        // turn off logger
        mockRecorderInstance = mock(ResultsRecorder.class);
        mockedRecorderStatic = mockStatic(ResultsRecorder.class);
        mockedRecorderStatic.when(ResultsRecorder::getResultsRecorder).thenReturn(mockRecorderInstance);

        // create CellularRoad with mocked dependencies. We need to mock the static method calls to DefaultStuffMaker to
        // return our mocks for generators and light plans.
        try (MockedStatic<core.utils.DefaultStuffMaker> mockedMaker = mockStatic(
                core.utils.DefaultStuffMaker.class,
                invocation -> {
                    String methodName = invocation.getMethod().getName();
                    Class<?> returnType = invocation.getMethod().getReturnType();

                    if (methodName != null && methodName.contains("Generator")) {
                        if (java.util.List.class.isAssignableFrom(returnType)) {
                            LinkedList<Object> list = new LinkedList<>();
                            list.add(mockGenerator);
                            list.add(mockGenerator);
                            return list;
                        }
                        if (returnType.isArray()) {
                            return new CarGenerator[]{mockGenerator, mockGenerator};
                        }
                    }
                    if (methodName != null && (methodName.contains("Light") || methodName.contains("Plan"))) {
                        core.model.LightPlan dummyPlan = mock(core.model.LightPlan.class);
                        if (java.util.List.class.isAssignableFrom(returnType)) {
                            LinkedList<Object> list = new LinkedList<>();
                            list.add(dummyPlan);
                            list.add(dummyPlan);
                            return list;
                        }
                        if (returnType.isArray()) {
                            return new core.model.LightPlan[]{dummyPlan, dummyPlan};
                        }
                    }
                    return Mockito.RETURNS_DEFAULTS.answer(invocation);
                })) {

            // 20 cells (100m / 5m) and 2 lanes, with mocked generators
            road = new CellularRoad(100.0, 2, 50.0, 5.0, 1);
        }

        CarGenerator[] generators = new CarGenerator[2];
        generators[0] = mockGenerator;
        generators[1] = mockGenerator;
        road.setCarGenerators(generators);
    }

    /**
     * tear down method to close mocked static after each test
     **/
    @AfterEach
    void tearDown() {
        if (mockedRecorderStatic != null) {
            mockedRecorderStatic.close();
        }
    }

    /**
     * test to verify that the constructor correctly translates meters into cell numbers
     **/
    @Test
    void constructor_ShouldInitializeCellsProperly() {
        assertEquals(20.0, road.getLengthInCells(), "100m road with 5m cells should have 20 cells");
        assertEquals(5.0, road.getCellSize(), "Cell size should be 5.0");
    }

    /**
     * test to verify that okToPutCarAtStart returns true when required cells are unoccupied
     **/
    @Test
    void okToPutCarAtStart_ShouldReturnTrue_WhenSpaceIsFree() {
        CarParams car = new CarParams();
        car.setParameter(RequestConstants.LENGTH_REQUEST, 2.0); // Potřebuje 2 buňky

        assertTrue(road.okToPutCarAtStart(car, 0), "Should be OK to place car in an empty lane");
    }

    /**
     * test to verify that okToPutCarAtStart returns false when a required cell is occupied
     **/
    @Test
    void okToPutCarAtStart_ShouldReturnFalse_WhenSpaceIsOccupied() {
        CarParams car = new CarParams();
        car.setParameter(RequestConstants.LENGTH_REQUEST, 2.0);

        // Ručně obsadíme buňku 1, kterou by auto potřebovalo
        Cell[][] cells = road.getContent();
        cells[0][1].setOccupied(true);

        assertFalse(road.okToPutCarAtStart(car, 0), "Should block placement if a required cell is occupied");
    }

    /**
     * test to verify that placeCarAtStart correctly occupies the cells for head and body
     **/
    @Test
    void placeCarAtStart_ShouldCorrectlyOccupyCells() {
        CarParams car = new CarParams();
        car.setParameter(RequestConstants.LENGTH_REQUEST, 2.0);

        // V metodě je length parameter převeden na počet buněk. Dáme mu 2.0 (takže x bude 1 a zabere 1 a 0)
        road.placeCarAtStart(car, 2.0, 0);

        Cell[][] cells = road.getContent();

        assertTrue(cells[0][1].isOccupied(), "Head cell 1 should be occupied");
        assertTrue(cells[0][1].isHead(), "Cell 1 should be marked as head");

        assertTrue(cells[0][0].isOccupied(), "Body cell 0 should be occupied");
        assertFalse(cells[0][0].isHead(), "Cell 0 should NOT be marked as head");

        assertEquals(1.0, car.xPosition, "Car head position should be updated to 1.0");
    }

    /**
     * test to verify that cars correctly move between cells based on speed
     **/
    @Test
    void updateRoad_CarMovement_ShouldUpdateCellPositions() {
        CarParams car = new CarParams();
        car.lane = 0;
        car.xPosition = 2.0;
        car.setParameter(RequestConstants.LENGTH_REQUEST, 2.0);
        car.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0.0);

        // Ručně vložíme auto na pozice 2 (hlava) a 1 (tělo)
        Cell[][] cells = road.getContent();
        cells[0][2].setOccupied(true);
        cells[0][2].setHead(true);
        cells[0][2].setCarParams(car);

        cells[0][1].setOccupied(true);
        cells[0][1].setHead(false);
        cells[0][1].setCarParams(car);

        // Model řekne: Zrychli o 3 buňky za krok
        when(mockCarFollowingModel.requestParameters()).thenReturn(RequestConstants.CURRENT_SPEED_REQUEST);
        when(mockCarFollowingModel.getNewSpeed(any())).thenReturn(3.0);

        road.updateRoad();

        // Staré pozice musí být prázdné
        assertFalse(cells[0][2].isOccupied(), "Old head cell 2 should be empty");
        assertFalse(cells[0][1].isOccupied(), "Old body cell 1 should be empty");

        // Nové pozice (2+3 = 5 a 1+3 = 4) musí být plné
        assertEquals(5.0, car.xPosition, "Car head position should move to 5.0");
        assertTrue(cells[0][5].isOccupied(), "New head cell 5 should be occupied");
        assertTrue(cells[0][5].isHead(), "Cell 5 should be head");
        assertTrue(cells[0][4].isOccupied(), "New body cell 4 should be occupied");
    }

    /**
     * test to verify that collision resolution stops a fast car from crashing into a slow car ahead
     **/
    @Test
    void updateRoad_CollisionResolution_ShouldStopBeforeCarAhead() {
        Cell[][] cells = road.getContent();

        // Přední auto (zdržuje provoz na indexu 8)
        CarParams frontCar = new CarParams();
        frontCar.lane = 0;
        frontCar.xPosition = 8.0;
        frontCar.setParameter(RequestConstants.LENGTH_REQUEST, 1.0);
        frontCar.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0.0);
        cells[0][8].setOccupied(true);
        cells[0][8].setHead(true);
        cells[0][8].setCarParams(frontCar);

        // Zadní auto (chce jet na index 5)
        CarParams backCar = new CarParams();
        backCar.lane = 0;
        backCar.xPosition = 5.0;
        backCar.setParameter(RequestConstants.LENGTH_REQUEST, 1.0);
        backCar.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0.0);
        cells[0][5].setOccupied(true);
        cells[0][5].setHead(true);
        cells[0][5].setCarParams(backCar);

        when(mockCarFollowingModel.requestParameters()).thenReturn(RequestConstants.CURRENT_SPEED_REQUEST);

        // Zpracování jede od konce (od indexu 19 k 0).
        // 1. volání pro frontCar -> rychlost 0.0 (stojí)
        // 2. volání pro backCar -> rychlost 5.0 (chce skočit až na buňku 10)
        when(mockCarFollowingModel.getNewSpeed(any())).thenReturn(0.0, 5.0);

        road.updateRoad();

        // backCar chtěl jet na 10, ale na 8 je frontCar. ResolveCollision mu povolí jet pouze na 7.
        assertEquals(7.0, backCar.xPosition, "Back car should stop exactly behind the front car at index 7");
        assertTrue(cells[0][7].isOccupied(), "Cell 7 should be occupied by backCar");
    }

    /**
     * test to verify that cars are removed when they leave the 2D grid
     **/
    @Test
    void updateRoad_CarExiting_ShouldBeRemovedFromRoad() {
        CarParams car = new CarParams();
        car.lane = 0;
        car.xPosition = 19.0; // Silnice má 20 buněk (indexy 0-19)
        car.setParameter(RequestConstants.LENGTH_REQUEST, 1.0);

        Cell[][] cells = road.getContent();
        cells[0][19].setOccupied(true);
        cells[0][19].setHead(true);
        cells[0][19].setCarParams(car);

        when(mockCarFollowingModel.requestParameters()).thenReturn(RequestConstants.CURRENT_SPEED_REQUEST);
        when(mockCarFollowingModel.getNewSpeed(any())).thenReturn(3.0); // Chce vyjet ven na index 22

        road.updateRoad();

        assertEquals(0, road.getNumberOfCarsOnRoad(), "Car should be fully removed from the road");
        assertFalse(cells[0][19].isOccupied(), "Cell 19 should be empty");
    }

    /**
     * test to verify that getNumberOfCarsOnRoad counts only head cells
     **/
    @Test
    void getNumberOfCarsOnRoad_ShouldCountOnlyHeads() {
        Cell[][] cells = road.getContent();

        // Auto 1 (délka 2)
        cells[0][5].setOccupied(true);
        cells[0][5].setHead(true);
        cells[0][4].setOccupied(true);
        cells[0][4].setHead(false);

        // Auto 2 (délka 1)
        cells[1][8].setOccupied(true);
        cells[1][8].setHead(true);

        assertEquals(2, road.getNumberOfCarsOnRoad(), "Should count exactly 2 car heads, ignoring body cells");
    }

    /**
     * test to verify that countStoppedCarsInLane accurately counts cars with 0 speed
     **/
    @Test
    void countStoppedCarsInLane_ShouldCountProperly() {
        Cell[][] cells = road.getContent();

        // Stojící auto
        CarParams stoppedCar = new CarParams();
        stoppedCar.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0.0);
        cells[0][5].setOccupied(true);
        cells[0][5].setHead(true);
        cells[0][5].setCarParams(stoppedCar);

        // Jedoucí auto
        CarParams movingCar = new CarParams();
        movingCar.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 5.0);
        cells[0][8].setOccupied(true);
        cells[0][8].setHead(true);
        cells[0][8].setCarParams(movingCar);

        int count = road.countStoppedCarsInLane(0);
        assertEquals(1, count, "Should find exactly 1 stopped car in lane 0");
    }

    /**
     * test to verify that removeAllCars successfully clears the entire 2D grid
     **/
    @Test
    void removeAllCars_ShouldClearAllCells() {
        Cell[][] cells = road.getContent();
        cells[0][5].setOccupied(true);
        cells[1][8].setOccupied(true);
        cells[1][8].setHead(true);

        road.removeAllCars();

        assertFalse(cells[0][5].isOccupied(), "Cell 0,5 should be cleared");
        assertFalse(cells[1][8].isOccupied(), "Cell 1,8 should be cleared");
        assertEquals(0, road.getNumberOfCarsOnRoad(), "Road should be completely empty");
    }
}