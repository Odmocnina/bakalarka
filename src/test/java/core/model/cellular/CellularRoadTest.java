package core.model.cellular;

import core.model.CarParams;
import core.model.Direction;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CellularRoad.
 *
 * These tests focus on:
 *  - correct road geometry (number of cells, lanes, cell size)
 *  - lane helper logic (getLaneForInfo, getNextOccupiedCell, getPreviousOccupiedCell)
 *  - placing and removing cars (placeCar, removeCar)
 *  - movement of cars (moveCar, isCarAtEnd, checkIfCarStillRelevant, moveCarHead)
 *
 * NOTE: tests use reflection to access private methods. If you decide to make some methods
 *       package-private instead, you can simplify the tests by calling them directly.
 */
class CellularRoadTest {

    private CellularRoad road;
    private Cell[][] cells;

    @BeforeEach
    void setUp() {
        // length = 100 m, lanes = 3, speedLimit arbitrary, cellSize = 5 m -> 20 cells
        road = new CellularRoad(100.0, 3, 30.0, 5.0);
        cells = road.getContent();
        clearRoad(); // clear any debug cars created in createRoad()
    }

    /**
     * Helper method to clear the entire road to a known empty state.
     */
    private void clearRoad() {
        for (int lane = 0; lane < cells.length; lane++) {
            for (int pos = 0; pos < cells[0].length; pos++) {
                cells[lane][pos].setOccupied(false);
                cells[lane][pos].setHead(false);
                cells[lane][pos].setCarParams(null);
            }
        }
    }

    /**
     * Helper to get a private method via reflection.
     */
    private Method getPrivateMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method m = CellularRoad.class.getDeclaredMethod(name, parameterTypes);
        m.setAccessible(true);
        return m;
    }

    /**
     * Helper to create a simple CarParams with length and current speed set.
     */
    private CarParams createCarParams(int lane, int xPosition, int length, double currentSpeed) {
        CarParams car = new CarParams();
        car.lane = lane;
        car.xPosition = xPosition;
        car.setParameter(RequestConstants.LENGTH_REQUEST, length);
        car.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, currentSpeed);
        return car;
    }

    // -------------------------------------------------------------------------
    // Basic geometry & constructor behavior
    // -------------------------------------------------------------------------

    @Test
    void constructorShouldCreateCorrectGridAndCellSize() {
        assertEquals(3, cells.length, "road should have 3 lanes");
        assertEquals(20, cells[0].length, "road should have 20 cells per lane (100 / 5)");

        for (int lane = 0; lane < cells.length; lane++) {
            for (int pos = 0; pos < cells[0].length; pos++) {
                assertNotNull(cells[lane][pos], "each cell must be initialized (non-null)");
            }
        }

        assertEquals(20.0, road.getLengthInCells(), 0.0001, "getLengthInCells should return the computed number of cells");
        assertEquals(5.0, road.getCellSize(), 0.0001, "getCellSize should return the configured cell size");
    }

    @Test
    void newClearedRoadShouldHaveZeroCars() {
        assertEquals(0, road.getNumberOfCarsOnRoad(),
                "after clearing the debug cars, there should be zero cars on the road");
    }

    // -------------------------------------------------------------------------
    // getLaneForInfo tests
    // -------------------------------------------------------------------------

    @Test
    void getLaneForInfoShouldReturnSameLaneForStraight() throws Exception {
        Method m = getPrivateMethod("getLaneForInfo", int.class, Direction.class);

        int result = (int) m.invoke(road, 1, Direction.STRAIGHT);
        assertEquals(1, result, "for STRAIGHT direction the same lane index should be returned");
    }

    @Test
    void getLaneForInfoShouldHandleLeftAndRightAndBorders() throws Exception {
        Method m = getPrivateMethod("getLaneForInfo", int.class, Direction.class);

        // Lanes: 0,1,2

        // From lane 1, LEFT -> lane 0
        int leftFrom1 = (int) m.invoke(road, 1, Direction.LEFT);
        assertEquals(0, leftFrom1, "left from lane 1 should be lane 0");

        // From lane 1, RIGHT -> lane 2
        int rightFrom1 = (int) m.invoke(road, 1, Direction.RIGHT);
        assertEquals(2, rightFrom1, "right from lane 1 should be lane 2");

        // From lane 0, LEFT -> NO_LANE_THERE
        int leftFrom0 = (int) m.invoke(road, 0, Direction.LEFT);
        assertEquals(Constants.NO_LANE_THERE, leftFrom0, "left from leftmost lane should return NO_LANE_THERE");

        // From lane 2, RIGHT -> NO_LANE_THERE
        int rightFrom2 = (int) m.invoke(road, 2, Direction.RIGHT);
        assertEquals(Constants.NO_LANE_THERE, rightFrom2, "right from rightmost lane should return NO_LANE_THERE");
    }

    // -------------------------------------------------------------------------
    // getNextOccupiedCell / getPreviousOccupiedCell tests
    // -------------------------------------------------------------------------

    @Test
    void getNextOccupiedCellShouldReturnNoCarWhenLaneEmpty() throws Exception {
        Method m = getPrivateMethod("getNextOccupiedCell", int.class, int.class, Direction.class);

        int result = (int) m.invoke(road, 1, 5, Direction.STRAIGHT);
        assertEquals(Constants.NO_CAR_IN_FRONT, result,
                "on an empty lane, next occupied cell should be NO_CAR_IN_FRONT");
    }

    @Test
    void getNextOccupiedCellShouldFindCarInSameLane() throws Exception {
        Method m = getPrivateMethod("getNextOccupiedCell", int.class, int.class, Direction.class);

        // Put a car head at lane 1, position 10
        cells[1][10].setOccupied(true);
        cells[1][10].setHead(true);

        int result = (int) m.invoke(road, 1, 5, Direction.STRAIGHT);
        assertEquals(10, result, "next occupied cell after position 5 should be 10");
    }

    @Test
    void getPreviousOccupiedCellShouldFindCarBehind() throws Exception {
        Method m = getPrivateMethod("getPreviousOccupiedCell", int.class, int.class, Direction.class);

        // Put a car head at lane 2, position 3
        cells[2][3].setOccupied(true);
        cells[2][3].setHead(true);

        int result = (int) m.invoke(road, 2, 7, Direction.STRAIGHT);
        assertEquals(3, result, "previous occupied cell before position 7 should be 3");
    }

    @Test
    void getNextOccupiedCellShouldReturnNoLaneThereWhenDirectionInvalid() throws Exception {
        Method m = getPrivateMethod("getNextOccupiedCell", int.class, int.class, Direction.class);

        // lane 0, looking LEFT means no lane there
        int result = (int) m.invoke(road, 0, 5, Direction.LEFT);
        assertEquals(Constants.NO_LANE_THERE, result, "looking to non-existing lane should return NO_LANE_THERE");
    }

    // -------------------------------------------------------------------------
    // placeCar / removeCar tests
    // -------------------------------------------------------------------------

    @Test
    void placeCarShouldOccupyCellsAndMarkHead() throws Exception {
        Method placeCar = getPrivateMethod("placeCar", CarParams.class, int.class, int.class);

        int lane = 1;
        int headX = 5;
        int length = 3;

        CarParams car = createCarParams(lane, headX, length, 0.0);

        // call private placeCar
        placeCar.invoke(road, car, headX, lane);

        // head at position 5, body at 4 and 3
        for (int i = 0; i < length; i++) {
            int pos = headX - i;
            assertTrue(cells[lane][pos].isOccupied(), "car cell should be occupied at lane=" + lane + ", pos=" + pos);
            assertSame(car, cells[lane][pos].getCarParams(),
                    "carParams should be the same object in all cells of the car");
        }

        assertTrue(cells[lane][headX].isHead(), "head cell should be marked as head");
        assertFalse(cells[lane][headX - 1].isHead(), "body cell should not be marked as head");
        assertEquals(headX, car.xPosition, "car xPosition should be updated to head x");
        assertEquals(lane, car.lane, "car lane should be updated");
    }

    @Test
    void placeCarShouldLogAndNotCrashWhenOutOfBounds() throws Exception {
        Method placeCar = getPrivateMethod("placeCar", CarParams.class, int.class, int.class);

        int lane = 1;
        int headX = -1; // invalid
        int length = 2;
        CarParams car = createCarParams(lane, headX, length, 0.0);

        // Should not throw; it will log an error and return
        assertDoesNotThrow(() -> {
            try {
                placeCar.invoke(road, car, headX, lane);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, "placeCar should handle invalid indices gracefully without throwing");
    }

    @Test
    void removeCarShouldClearAllCellsOfThatCar() throws Exception {
        Method placeCar = getPrivateMethod("placeCar", CarParams.class, int.class, int.class);
        Method removeCar = getPrivateMethod("removeCar", int.class, int.class);

        int lane = 2;
        int headX = 6;
        int length = 3;
        CarParams car = createCarParams(lane, headX, length, 0.0);

        placeCar.invoke(road, car, headX, lane);

        // preconditions
        assertTrue(cells[lane][headX].isHead(), "precondition: head should be present before removing");

        // call removeCar on lane and head position
        removeCar.invoke(road, lane, headX);

        // all segments must be cleared
        for (int i = 0; i < length; i++) {
            int pos = headX - i;
            assertFalse(cells[lane][pos].isOccupied(), "cell should not be occupied after removing");
            assertFalse(cells[lane][pos].isHead(), "cell should not be head after removing");
            assertNull(cells[lane][pos].getCarParams(), "carParams should be null after removing");
        }
    }

    // -------------------------------------------------------------------------
    // isCarAtEnd / checkIfCarStillRelevant tests
    // -------------------------------------------------------------------------

    @Test
    void isCarAtEndShouldReturnFalseWhenStillInside() throws Exception {
        Method isCarAtEnd = getPrivateMethod("isCarAtEnd", CarParams.class, int.class);

        // numberOfCells = 20, car at x=10, speed=5 -> 10+5=15 < 20 => still inside
        CarParams car = createCarParams(1, 10, 2, 5.0);
        boolean result = (boolean) isCarAtEnd.invoke(road, car, 5);

        assertFalse(result, "car should not be at end if head + speed is still inside road");
    }

    @Test
    void isCarAtEndShouldReturnTrueWhenHeadBeyondLastCell() throws Exception {
        Method isCarAtEnd = getPrivateMethod("isCarAtEnd", CarParams.class, int.class);

        // numberOfCells = 20, car at x=18, speed=2 -> 18+2=20 >= 20 => at end
        CarParams car = createCarParams(1, 18, 2, 2.0);
        boolean result = (boolean) isCarAtEnd.invoke(road, car, 2);

        assertTrue(result, "car should be at end if head + speed reaches or exceeds last cell index");
    }

    @Test
    void checkIfCarStillRelevantShouldRemoveCarWhenCompletelyBeyondEnd() throws Exception {
        Method placeCar = getPrivateMethod("placeCar", CarParams.class, int.class, int.class);
        Method checkIfCarStillRelevant = getPrivateMethod("checkIfCarStillRelevant", CarParams.class, int.class);

        int lane = 0;
        int headX = 19; // last cell index = 19
        int length = 2; // body occupies 19 and 18
        CarParams car = createCarParams(lane, headX, length, 0.0);

        placeCar.invoke(road, car, headX, lane);

        // newSpeed = 3: head would go to 22, tail to 21 -> whole car is outside (>=20)
        boolean stillRelevant = (boolean) checkIfCarStillRelevant.invoke(road, car, 3);

        assertFalse(stillRelevant, "car should not be relevant if its entire body is beyond the end");
        // removeCar should have been called inside; all cells must be cleared
        for (int i = 0; i < length; i++) {
            int pos = headX - i;
            assertFalse(cells[lane][pos].isOccupied(), "cell should be cleared when car is no longer relevant");
        }
    }

    @Test
    void checkIfCarStillRelevantShouldKeepCarWhenBodyStillOnRoad() throws Exception {
        Method placeCar = getPrivateMethod("placeCar", CarParams.class, int.class, int.class);
        Method checkIfCarStillRelevant = getPrivateMethod("checkIfCarStillRelevant", CarParams.class, int.class);

        int lane = 0;
        int headX = 18;
        int length = 3; // occupies 18,17,16
        CarParams car = createCarParams(lane, headX, length, 0.0);

        placeCar.invoke(road, car, headX, lane);

        // newSpeed = 2, head -> 20, tail -> 18
        // some part will still be on road in next step => still relevant
        boolean stillRelevant = (boolean) checkIfCarStillRelevant.invoke(road, car, 2);

        assertTrue(stillRelevant, "car should remain relevant if some part of its body is still on the road");
    }

    // -------------------------------------------------------------------------
    // moveCar & moveCarHead tests
    // -------------------------------------------------------------------------

    @Test
    void moveCarShouldMoveHeadAndBodyForwardAccordingToSpeed() throws Exception {
        Method moveCar = getPrivateMethod("moveCar", Cell.class);

        int lane = 1;
        int headX = 5;
        int length = 3;
        int speed = 2;

        CarParams car = createCarParams(lane, headX, length, speed);

        // manually set up the car on cells: 5 (head), 4, 3 (body)
        for (int i = 0; i < length; i++) {
            int pos = headX - i;
            cells[lane][pos].setOccupied(true);
            cells[lane][pos].setCarParams(car);
            cells[lane][pos].setHead(i == 0);
        }

        // precondition
        assertTrue(cells[lane][headX].isHead(), "precondition: head must be at initial position");

        // invoke moveCar on current head cell
        moveCar.invoke(road, cells[lane][headX]);

        int newHeadX = headX + speed;

        // old head must be cleared
        assertFalse(cells[lane][headX].isOccupied(), "old head cell should be cleared");
        assertFalse(cells[lane][headX].isHead(), "old head cell should no longer be head");

        // new head cell
        assertTrue(cells[lane][newHeadX].isOccupied(), "new head cell should be occupied");
        assertTrue(cells[lane][newHeadX].isHead(), "new head cell should be marked as head");
        assertSame(car, cells[lane][newHeadX].getCarParams(), "new head cell should hold same carParams instance");

        // body moved as well: new positions: newHeadX (7), 6, 5
        assertTrue(cells[lane][newHeadX - 1].isOccupied(), "car body should occupy new cell behind head");
        assertTrue(cells[lane][newHeadX - 2].isOccupied(), "car body should occupy second new cell behind head");

        // tail of the car that was at position 3 should be cleared
        assertFalse(cells[lane][headX - (length - 1)].isOccupied(),
                "old tail cell should be cleared after movement");

        assertEquals(newHeadX, car.xPosition, "car xPosition should be updated to new head position");
    }

    @Test
    void moveCarHeadShouldShortenCarWhenOverflowingEndAndThenMove() throws Exception {
        Method placeCar = getPrivateMethod("placeCar", CarParams.class, int.class, int.class);
        Method moveCarHead = getPrivateMethod("moveCarHead", CarParams.class, int.class);

        int lane = 0;
        int headX = 18;
        int length = 4; // occupies 18,17,16,15
        int newSpeed = 3; // head attempt -> 21

        CarParams car = createCarParams(lane, headX, length, newSpeed);

        placeCar.invoke(road, car, headX, lane);

        // invoke moveCarHead (it will partially remove car and then delegate to moveCar)
        moveCarHead.invoke(road, car, newSpeed);

        // After moveCarHead:
        //   howMuchOverflow = headX + speed - numberOfCells + 1
        //                    = 18 + 3 - 20 + 1 = 2
        // newHeadX = oldX - howMuchOverflow = 18 - 2 = 16
        // length becomes length - howMuchOverflow = 4 - 2 = 2

        int expectedNewHeadX = 16;
        double expectedNewLength = 2.0;

        // The head position in car.xPosition is then updated and moveCar is called.
        // Because moveCar moves again by currentSpeed = newSpeed (3),
        // the final head xExpected = newHeadX + 3 = 19.
        int finalExpectedHeadX = expectedNewHeadX + newSpeed;

        // We verify that some head exists at finalExpectedHeadX for that lane
        assertTrue(cells[lane][finalExpectedHeadX].isHead(),
                "after moveCarHead + moveCar, there should be a head at expected final position");

        assertEquals(finalExpectedHeadX, (int) car.xPosition,
                "car.xPosition should match final head position");

        assertEquals(expectedNewLength, car.getParameter(RequestConstants.LENGTH_REQUEST),
                "car length should be reduced by the overflow amount");
    }

    // -------------------------------------------------------------------------
    // getRoadDependedParameters basic test
    // -------------------------------------------------------------------------

    @Test
    void getRoadDependedParametersShouldProvideDistanceToNextCar() throws Exception {
        Method getRoadDependedParameters =
                getPrivateMethod("getRoadDependedParameters", HashMap.class, String.class, CarParams.class);
        Method getNextOccupiedCell =
                getPrivateMethod("getNextOccupiedCell", int.class, int.class, Direction.class);

        int lane = 1;
        // car A at x = 5 (head)
        CarParams carA = createCarParams(lane, 5, 1, 0.0);
        cells[lane][5].setOccupied(true);
        cells[lane][5].setHead(true);
        cells[lane][5].setCarParams(carA);

        // car B at x = 10 (head)
        CarParams carB = createCarParams(lane, 10, 1, 0.0);
        cells[lane][10].setOccupied(true);
        cells[lane][10].setHead(true);
        cells[lane][10].setCarParams(carB);

        // Sanity check of getNextOccupiedCell
        int nextFrom5 = (int) getNextOccupiedCell.invoke(road, lane, 5, Direction.STRAIGHT);
        assertEquals(10, nextFrom5, "sanity check: next occupied after 5 should be 10");

        HashMap<String, Double> params = new HashMap<>();
        getRoadDependedParameters.invoke(road, params, RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST, carA);

        assertTrue(params.containsKey(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST),
                "parameters map should contain distance to next car");

        // distance is nextCarPos - position - 1 = 10 - 5 - 1 = 4
        assertEquals(4.0, params.get(RequestConstants.DISTANCE_TO_NEXT_CAR_REQUEST),
                "distance to next car should be computed as gap in cells between heads");
    }
}

