package core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**************************************
 * Unit tests for Parameter class
 *
 * @author Michael Hladky
 * @version 1.0
 **************************************/
public class ParameterTest {

    /**
     * test to verify that a parameter created with valid positive values correctly calculates its range
     * and is evaluated as valid
     **/
    @Test
    void checkIfValid_ValidPositiveValues_ShouldReturnTrue() {
        // Arrange: create a parameter where min < max
        Parameter param = new Parameter("Speed", 10.0, 50.0);

        // Act & Assert
        // verify correct range calculation
        assertEquals(40.0, param.range, 0.001, "Range should be correctly calculated as max - min");
        // verify name and values
        assertEquals("Speed", param.name);
        assertEquals(10.0, param.minValue);
        assertEquals(50.0, param.maxValue);
        // verify validity
        assertTrue(param.checkIfValid(), "Parameter should be valid when min < max");
    }

    /**
     * test to verify that a parameter created with invalid values (min > max) calculates a negative range
     * and is evaluated as invalid
     **/
    @Test
    void checkIfValid_InvalidValues_ShouldReturnFalse() {
        // Arrange: create a parameter where min > max
        Parameter param = new Parameter("InvalidParam", 100.0, 20.0);

        // Act & Assert
        // range will be negative (-80.0)
        assertEquals(-80.0, param.range, 0.001, "Range should be negative if min > max");
        // checkIfValid method should return false
        assertFalse(param.checkIfValid(), "Parameter should be invalid when min > max");
    }

    /**
     * test to verify that a parameter created with identical min and max values calculates a range of 0
     * and is still evaluated as valid
     **/
    @Test
    void checkIfValid_EqualValues_ShouldReturnTrue() {
        // Arrange: create a parameter where min == max
        Parameter param = new Parameter("ExactValue", 25.5, 25.5);

        // Act & Assert
        // range will be exactly 0
        assertEquals(0.0, param.range, 0.001, "Range should be 0 when min == max");
        // checkIfValid method should still return true
        assertTrue(param.checkIfValid(), "Parameter should be valid when min == max");
    }

    /**
     * test to verify that a parameter created with valid negative values correctly calculates its range
     * and is evaluated as valid
     **/
    @Test
    void checkIfValid_NegativeValues_ShouldReturnTrue() {
        // Arrange: create a parameter with negative values, min < max
        Parameter param = new Parameter("NegativeParam", -50.0, -10.0);

        // Act & Assert
        // range must be positive (-10 - (-50) = 40)
        assertEquals(40.0, param.range, 0.001, "Range should be positive even for negative inputs");
        // method must return true
        assertTrue(param.checkIfValid(), "Parameter should be valid for correct negative inputs");
    }
}
