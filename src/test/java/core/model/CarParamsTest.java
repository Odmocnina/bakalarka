package core.model;

import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**************************************
 * Unit tests for CarParams class
 *
 * @author Michael Hladky
 * @version 1.0
 **************************************/
public class CarParamsTest {

    /**
     * test to verify that getParameter correctly maps special string keys
     * directly to the public fields (xPosition, lane, id)
     **/
    @Test
    void getParameter_SpecialKeys_ShouldReturnObjectFields() {
        // Arrange
        CarParams params = new CarParams();
        params.xPosition = 150.5;
        params.lane = 2;
        params.id = 42;

        // Act & Assert
        assertEquals(150.5, params.getParameter(RequestConstants.X_POSITION_REQUEST),
                "Should return xPosition field for X_POSITION_REQUEST key");
        assertEquals(2.0, params.getParameter("lane"),
                "Should return lane field for 'lane' key");
        assertEquals(42.0, params.getParameter("id"),
                "Should return id field for 'id' key");
    }

    /**
     * test to verify that setting and getting a custom parameter stores and retrieves
     * it correctly from the internal HashMap
     **/
    @Test
    void setAndGetParameter_CustomKeys_ShouldStoreInMap() {
        // Arrange
        CarParams params = new CarParams();

        // Act
        params.setParameter("speed", 80.5);
        params.setParameter("acceleration", 2.1);

        // Assert
        assertEquals(80.5, params.getParameter("speed"), "Should return the stored speed value");
        assertEquals(2.1, params.getParameter("acceleration"), "Should return the stored acceleration value");
    }

    /**
     * test to verify that requesting a parameter that has not been set returns
     * the predefined undefined constant
     **/
    @Test
    void getParameter_UnknownKey_ShouldReturnUndefinedConstant() {
        // Arrange
        CarParams params = new CarParams();

        // Act
        double result = params.getParameter("unknown_parameter");

        // Assert
        assertEquals(Constants.PARAMETER_UNDEFINED, result,
                "Should return Constants.PARAMETER_UNDEFINED for missing keys");
    }

    /**
     * test to verify that the toString method formats the object and its
     * parameters map correctly into a readable string
     **/
    @Test
    void toString_ShouldReturnFormattedString() {
        // Arrange
        CarParams params = new CarParams();
        params.id = 5;
        params.lane = 1;
        params.xPosition = 10.0;
        params.setParameter("speed", 60.0);

        // Act
        String result = params.toString();

        // Assert
        assertTrue(result.startsWith("CarParams{id=5, lane=1, xPosition=10.0, parameters={"),
                "String should start with properly formatted base fields");
        assertTrue(result.contains("speed=60.0"), "String should contain parameters from the HashMap");
        assertTrue(result.endsWith("}}"), "String should end with correct brackets");
    }

    /**
     * test to verify that the clone method creates a distinct instance with copied fields
     * and a deep copy of the parameters HashMap
     **/
    @Test
    void clone_ShouldReturnDeepCopyOfParameters() {
        // Arrange
        CarParams original = new CarParams();
        original.id = 99;
        original.lane = 0;
        original.xPosition = 50.0;
        original.color = Color.BLUE;
        original.processedInCurrentStep = true;
        original.setParameter("speed", 45.0);

        // Act
        CarParams cloned = original.clone();

        // Assert basic fields
        assertNotSame(original, cloned, "Cloned object should be a different instance in memory");
        assertEquals(original.id, cloned.id, "Ids should match");
        assertEquals(original.lane, cloned.lane, "Lanes should match");
        assertEquals(original.xPosition, cloned.xPosition, "xPositions should match");
        assertEquals(original.color, cloned.color, "Colors should match");
        assertEquals(original.processedInCurrentStep, cloned.processedInCurrentStep, "processed flag should match");

        // Assert deep copy of map
        assertEquals(original.getParameter("speed"), cloned.getParameter("speed"), "Cloned parameter should match");

        // Act: modify original parameter to verify the clone's map is completely independent
        original.setParameter("speed", 100.0);

        // Assert: clone should remain unaffected
        assertEquals(100.0, original.getParameter("speed"), "Original should be updated");
        assertEquals(45.0, cloned.getParameter("speed"), "Cloned map should be independent of original map");
    }
}
