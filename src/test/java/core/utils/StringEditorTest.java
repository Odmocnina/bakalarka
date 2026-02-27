package core.utils;

import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/************************************
 * Unit tests for StringEditor utility class
 *
 * @author Michael Hladky
 * @version 1.0
 ************************************/
public class StringEditorTest {

    /**
     * test to verify that if the first parameter string is null or empty,
     * the second one is returned unchanged
     **/
    @Test
    void mergeRequestParameters_ShouldReturnParam2_WhenParam1IsNullOrEmpty() {
        String param2 = "speed" + RequestConstants.REQUEST_SEPARATOR + "length";

        assertEquals(param2, StringEditor.mergeRequestParameters(null, param2), "Should return param2 when param1 is null");
        assertEquals(param2, StringEditor.mergeRequestParameters("", param2), "Should return param2 when param1 is empty");
    }

    /**
     * test to verify that if the second parameter string is null or empty,
     * the first one is returned unchanged
     **/
    @Test
    void mergeRequestParameters_ShouldReturnParam1_WhenParam2IsNullOrEmpty() {
        String param1 = "speed" + RequestConstants.REQUEST_SEPARATOR + "length";

        assertEquals(param1, StringEditor.mergeRequestParameters(param1, null), "Should return param1 when param2 is null");
        assertEquals(param1, StringEditor.mergeRequestParameters(param1, ""), "Should return param1 when param2 is empty");
    }

    /**
     * test to verify that if both parameter strings are identical,
     * it just returns one of them without processing
     **/
    @Test
    void mergeRequestParameters_ShouldReturnParam1_WhenParamsAreIdentical() {
        String param1 = "speed" + RequestConstants.REQUEST_SEPARATOR + "length";
        String param2 = "speed" + RequestConstants.REQUEST_SEPARATOR + "length";

        // Testing the fast-return path "if (params1.equals(params2))"
        assertEquals(param1, StringEditor.mergeRequestParameters(param1, param2), "Should return the same string if both are equal");
    }

    /**
     * test to verify that merging two strings removes duplicates, maintains insertion order,
     * and trims any extra whitespaces
     **/
    @Test
    void mergeRequestParameters_ShouldMergeAndRemoveDuplicatesAndTrimSpaces() {
        // Arrange
        // param1 has an extra space around "acceleration"
        String param1 = "speed" + RequestConstants.REQUEST_SEPARATOR + " acceleration ";
        // param2 has an extra space around "length" and a duplicate "speed"
        String param2 = " length " + RequestConstants.REQUEST_SEPARATOR + "speed" + RequestConstants.REQUEST_SEPARATOR + "lane";

        // Act
        String result = StringEditor.mergeRequestParameters(param1, param2);

        // Assert
        // Expected order: speed, acceleration, length, lane (with trimmed whitespaces and no duplicate 'speed')
        String expected = "speed" + RequestConstants.REQUEST_SEPARATOR +
                "acceleration" + RequestConstants.REQUEST_SEPARATOR +
                "length" + RequestConstants.REQUEST_SEPARATOR +
                "lane";

        assertEquals(expected, result, "Should merge strings, remove duplicates, and trim whitespaces");
    }

    /**
     * test to verify that isInArray returns true when the target string is in the array
     **/
    @Test
    void isInArray_ShouldReturnTrue_WhenValueExists() {
        String[] array = {"speed", "length", "acceleration"};

        assertTrue(StringEditor.isInArray(array, "length"), "Should return true for value in the middle");
        assertTrue(StringEditor.isInArray(array, "speed"), "Should return true for first element");
        assertTrue(StringEditor.isInArray(array, "acceleration"), "Should return true for last element");
    }

    /**
     * test to verify that isInArray returns false when the target string is not in the array
     **/
    @Test
    void isInArray_ShouldReturnFalse_WhenValueDoesNotExist() {
        String[] array = {"speed", "length", "acceleration"};

        assertFalse(StringEditor.isInArray(array, "lane"), "Should return false for non-existing value");
        assertFalse(StringEditor.isInArray(array, "Speed"), "Should return false for value with different casing (case-sensitive check)");
    }

    /**
     * test to verify that isInArray handles empty arrays safely without throwing exceptions
     **/
    @Test
    void isInArray_ShouldReturnFalse_WhenArrayIsEmpty() {
        String[] array = new String[0];

        assertFalse(StringEditor.isInArray(array, "speed"), "Should return false when array is empty");
    }
}