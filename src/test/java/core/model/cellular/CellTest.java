package core.model.cellular;

import core.model.CarParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {

    private Cell cell;

    @BeforeEach
    void setUp() {
        cell = new Cell();
    }

    @Test
    void newCellShouldBeUnoccupiedAndNotHeadAndHaveNullCarParams() {
        assertFalse(cell.isOccupied(), "new cell should be unoccupied");
        assertFalse(cell.isHead(), "new cell should not be head");
        assertNull(cell.getCarParams(), "new cell should have null carParams");
    }

    @Test
    void setOccupiedShouldChangeOccupiedFlag() {
        assertFalse(cell.isOccupied());

        cell.setOccupied(true);
        assertTrue(cell.isOccupied(), "setOccupied(true) should set occupied to true");

        cell.setOccupied(false);
        assertFalse(cell.isOccupied(), "setOccupied(false) should set occupied to false");
    }

    @Test
    void setHeadShouldChangeHeadFlag() {
        assertFalse(cell.isHead());

        cell.setHead(true);
        assertTrue(cell.isHead(), "setHead(true) should set isHead to true");

        cell.setHead(false);
        assertFalse(cell.isHead(), "setHead(false) should set isHead to false");
    }

    @Test
    void setCarParamsShouldStoreReference() {
        CarParams params = new CarParams();
        cell.setCarParams(params);

        assertSame(params, cell.getCarParams(), "setCarParams should store the reference to CarParams");
    }

    @Test
    void setCarParamsToNullShouldClearReference() {
        CarParams params = new CarParams();
        cell.setCarParams(params);
        assertNotNull(cell.getCarParams());

        cell.setCarParams(null);
        assertNull(cell.getCarParams(), "setCarParams(null) should clear the CarParams reference");
    }
}

