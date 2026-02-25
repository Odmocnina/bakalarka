package core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**************************************
 * Unit tests for Orientation enum
 *
 * @author Michael Hladky
 * @version 1.0
 **************************************/
public class OrientationTest {

    /**
     * dummy test to cover implicitly generated values() and valueOf() methods
     * by the Java compiler to achieve 100% code coverage.
     **/
    @Test
    void enumCoverage_ShouldCoverCompilerGeneratedMethods() {
        // test the values() method
        Orientation[] values = Orientation.values();
        assertEquals(2, values.length, "Should contain exactly 2 orientations");

        // test the valueOf() method
        assertEquals(Orientation.FORWARD, Orientation.valueOf("FORWARD"));
        assertEquals(Orientation.BACKWARD, Orientation.valueOf("BACKWARD"));
    }
}
