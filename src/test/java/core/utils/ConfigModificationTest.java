package core.utils;

import app.AppContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*******************************
 * Unit tests for ConfigModification class, focusing on the toggling of various configuration settings in the simulation
 * context
 *
 * @author Michael Hladky
 * @version 1.0
 *******************************/
public class ConfigModificationTest {

    /**
     * Setup method to initialize a fresh RunDetails and OutputDetails before each test, ensuring that tests do not
     * interfere with each other's state and that we always start with a clean slate
     **/
    @BeforeEach
    void setUp() {
        // Initialize fresh RunDetails and OutputDetails before each test
        // to ensure isolated testing environment
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.RUN_DETAILS.outputDetails = new OutputDetails();

        // Initialize log array manually just to be completely safe
        AppContext.RUN_DETAILS.log = new boolean[] {true, true, true, true, true, true};
    }

    /**
     * Teardown method to clean up the AppContext after each test, ensuring that we do not leave any modified state that
     * could interfere with other tests in the suite
     **/
    @AfterEach
    void tearDown() {
        // Clean up AppContext after tests
        AppContext.RUN_DETAILS = null;
    }

    /**
     * test to verify that the lane change ban setting is correctly toggled
     **/
    @Test
    void changeLaneChangeBan_ShouldToggleValue() {
        // Arrange
        AppContext.RUN_DETAILS.laneChange = false;

        // Act & Assert - Toggle to true
        ConfigModification.changeLaneChangeBan();
        assertTrue(AppContext.RUN_DETAILS.laneChange, "Lane change should be toggled to true");

        // Act & Assert - Toggle back to false
        ConfigModification.changeLaneChangeBan();
        assertFalse(AppContext.RUN_DETAILS.laneChange, "Lane change should be toggled back to false");
    }

    /**
     * test to verify that the time between steps is accurately updated
     **/
    @Test
    void setTimeBetweenSteps_ShouldUpdateValue() {
        // Act
        ConfigModification.setTimeBetweenSteps(500);

        // Assert
        assertEquals(500, AppContext.RUN_DETAILS.timeBetweenSteps, "Time between steps should be updated to 500");
    }

    /**
     * test to verify that the draw cells setting is correctly toggled
     **/
    @Test
    void changeDrawCells_ShouldToggleValue() {
        // Arrange
        AppContext.RUN_DETAILS.drawCells = true;

        // Act & Assert - Toggle to false
        ConfigModification.changeDrawCells();
        assertFalse(AppContext.RUN_DETAILS.drawCells, "Draw cells should be toggled to false");

        // Act & Assert - Toggle back to true
        ConfigModification.changeDrawCells();
        assertTrue(AppContext.RUN_DETAILS.drawCells, "Draw cells should be toggled back to true");
    }

    /**
     * test to verify that the prevention collisions setting is correctly toggled
     **/
    @Test
    void changePreventCollision_ShouldToggleValue() {
        // Arrange
        AppContext.RUN_DETAILS.preventCollisions = false;

        // Act & Assert - Toggle to true
        ConfigModification.changePreventCollision();
        assertTrue(AppContext.RUN_DETAILS.preventCollisions, "Prevent collisions should be toggled to true");

        // Act & Assert - Toggle back to false
        ConfigModification.changePreventCollision();
        assertFalse(AppContext.RUN_DETAILS.preventCollisions, "Prevent collisions should be toggled back to false");
    }

    /**
     * test to verify that a specific logging setting is toggled based on the given index
     **/
    @Test
    void changeLogging_ShouldToggleValueAtSpecificIndex() {
        // Arrange - Index 2 is true by default from our setUp
        int indexToTest = 2;
        assertTrue(AppContext.RUN_DETAILS.log[indexToTest], "Initial log state at index 2 should be true");

        // Act - Toggle to false
        ConfigModification.changeLogging(indexToTest);

        // Assert
        assertFalse(AppContext.RUN_DETAILS.log[indexToTest], "Log state at index 2 should be toggled to false");

        // Ensure other indexes were not affected (e.g., index 1 should still be true)
        assertTrue(AppContext.RUN_DETAILS.log[1], "Log state at other indexes should remain unaffected");
    }

    /**
     * test to verify that a specific output setting is toggled correctly using its key string
     **/
    @Test
    void changeOutput_ShouldToggleValueForSpecificKey() {
        // Arrange
        String testKey = "CUSTOM_OUTPUT_TAG";
        AppContext.RUN_DETAILS.outputDetails.setPart(testKey, false);

        // Act & Assert - Toggle to true
        ConfigModification.changeOutput(testKey);
        assertTrue(AppContext.RUN_DETAILS.outputDetails.writePart(testKey), "Output setting should be toggled to true");

        // Act & Assert - Toggle back to false
        ConfigModification.changeOutput(testKey);
        assertFalse(AppContext.RUN_DETAILS.outputDetails.writePart(testKey), "Output setting should be toggled back to false");
    }
}