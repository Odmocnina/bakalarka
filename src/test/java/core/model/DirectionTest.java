package core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**************************************
 * Unit tests for Direction enum
 *
 * @author Michael Hladky
 * @version 1.0
 **************************************/
public class DirectionTest {

    /**
     * dummy test to cover implicitly generated values() and valueOf() methods
     * by the Java compiler to achieve 100% code coverage.
     **/
    @Test
    void enumCoverage_ShouldCoverCompilerGeneratedMethods() {
        // test the values() method
        Direction[] values = Direction.values();
        assertEquals(3, values.length, "Should contain exactly 3 directions");

        // test the valueOf() method
        assertEquals(Direction.LEFT, Direction.valueOf("LEFT"));
        assertEquals(Direction.RIGHT, Direction.valueOf("RIGHT"));
        assertEquals(Direction.STRAIGHT, Direction.valueOf("STRAIGHT"));
    }
}
