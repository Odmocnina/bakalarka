package core.model.cellular;

import core.model.CarParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/************************
 * Unit tests for Cell class
 *
 * @author Michael Hladky
 * @version 1.0
 ************************/
class CellTest {

    /** instance of Cell to be used in tests **/
    private Cell cell;

    /**
     * setup method to initialize a new Cell instance before each test
     **/
    @BeforeEach
    void setUp() {
        cell = new Cell();
    }

    /**
     * test to verify that a new Cell instance is initialized with expected default values:
     * - occupied should be false
     * - isHead should be false
     * - carParams should be null
     **/
    @Test
    void newCellShouldBeUnoccupiedAndNotHeadAndHaveNullCarParams() {
        assertFalse(cell.isOccupied(), "new cell should be unoccupied");
        assertFalse(cell.isHead(), "new cell should not be head");
        assertNull(cell.getCarParams(), "new cell should have null carParams");
    }

    /**
     * test to verify that setOccupied method correctly updates the occupied flag of the cell
     **/
    @Test
    void setOccupiedShouldChangeOccupiedFlag() {
        assertFalse(cell.isOccupied());

        cell.setOccupied(true);
        assertTrue(cell.isOccupied(), "setOccupied(true) should set occupied to true");

        cell.setOccupied(false);
        assertFalse(cell.isOccupied(), "setOccupied(false) should set occupied to false");
    }

    /**
     * test to verify that setHead method correctly updates the isHead flag of the cell
     **/
    @Test
    void setHeadShouldChangeHeadFlag() {
        assertFalse(cell.isHead());

        cell.setHead(true);
        assertTrue(cell.isHead(), "setHead(true) should set isHead to true");

        cell.setHead(false);
        assertFalse(cell.isHead(), "setHead(false) should set isHead to false");
    }

    /**
     * test to verify that setCarParams method correctly stores the reference to CarParams
     **/
    @Test
    void setCarParamsShouldStoreReference() {
        CarParams params = new CarParams();
        cell.setCarParams(params);

        assertSame(params, cell.getCarParams(), "setCarParams should store the reference to CarParams");
    }

    /**
     * test to verify that setting CarParams to null clears the reference and getCarParams returns null
     **/
    @Test
    void setCarParamsToNullShouldClearReference() {
        CarParams params = new CarParams();
        cell.setCarParams(params);
        assertNotNull(cell.getCarParams());

        cell.setCarParams(null);
        assertNull(cell.getCarParams(), "setCarParams(null) should clear the CarParams reference");
    }
}

