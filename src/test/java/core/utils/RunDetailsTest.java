package core.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/*********************************
 * Unit tests for RunDetails class, focusing on output configuration and map file management
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************/
public class RunDetailsTest {

    /** instance of RunDetails to be used in tests **/
    private RunDetails runDetails;

    /**
     * setup method to initialize RunDetails instance before each test
     **/
    @BeforeEach
    void setUp() {
        runDetails = new RunDetails();
    }

    /**
     * test to verify that writingResults returns false when outputDetails is null
     **/
    @Test
    void writingResults_ShouldReturnFalse_WhenOutputDetailsIsNull() {
        runDetails.outputDetails = null;
        assertFalse(runDetails.writingResults(), "Should return false if outputDetails is null");
    }

    /**
     * test to verify that writingResults returns false when outputFile is null or empty
     **/
    @Test
    void writingResults_ShouldReturnFalse_WhenOutputFileIsNullOrEmpty() {
        runDetails.outputDetails = new OutputDetails();

        // Null file
        runDetails.outputDetails.outputFile = null;
        assertFalse(runDetails.writingResults(), "Should return false if outputFile is null");

        // Empty file
        runDetails.outputDetails.outputFile = "";
        assertFalse(runDetails.writingResults(), "Should return false if outputFile is empty");
    }

    /**
     * test to verify that writingResults returns true when a valid output file is set
     **/
    @Test
    void writingResults_ShouldReturnTrue_WhenOutputFileIsValid() {
        runDetails.outputDetails = new OutputDetails();
        runDetails.outputDetails.outputFile = "results.csv";

        assertTrue(runDetails.writingResults(), "Should return true if outputFile has a valid name");
    }

    /**
     * test to verify that getOutputDetail returns false if outputDetails is null or tag is missing
     **/
    @Test
    void getOutputDetail_ShouldReturnFalse_WhenNullOrTagMissing() {
        // outputDetails is null
        runDetails.outputDetails = null;
        assertFalse(runDetails.getOutputDetail("speed"), "Should return false when outputDetails is null");

        // outputDetails exists, but map doesn't contain the tag
        runDetails.outputDetails = new OutputDetails();
        runDetails.outputDetails.whatToOutput = new HashMap<>();
        assertFalse(runDetails.getOutputDetail("speed"), "Should return false when tag is not in the map");
    }

    /**
     * test to verify that getOutputDetail correctly retrieves the boolean value for a specific tag
     **/
    @Test
    void getOutputDetail_ShouldReturnCorrectValue_WhenTagExists() {
        runDetails.outputDetails = new OutputDetails();
        runDetails.outputDetails.whatToOutput = new HashMap<>();

        // Setup map values
        runDetails.outputDetails.whatToOutput.put("speed", true);
        runDetails.outputDetails.whatToOutput.put("acceleration", false);

        assertTrue(runDetails.getOutputDetail("speed"), "Should return true for 'speed' tag");
        assertFalse(runDetails.getOutputDetail("acceleration"), "Should return false for 'acceleration' tag");
    }

    /**
     * test to verify that setNewMapFile properly updates the map file name and resets the mapChanged flag
     **/
    @Test
    void setNewMapFile_ShouldUpdateMapFileAndResetChangedFlag() {
        // Pre-condition: Set initial state as if the map was previously modified
        runDetails.mapChanged = true;
        runDetails.mapFile = "oldMap.xml";

        // Act
        runDetails.setNewMapFile("newMap.xml");

        // Assert
        assertEquals("newMap.xml", runDetails.mapFile, "Map file should be updated to newMap.xml");
        assertFalse(runDetails.mapChanged, "mapChanged flag should be safely reset to false");
    }

    /**
     * test to verify that toString returns a string containing key information about the configuration
     **/
    @Test
    void toString_ShouldContainKeyInformation() {
        // Arrange
        runDetails.duration = 3600;
        runDetails.timeStep = 0.5;
        runDetails.mapFile = "city.xml";

        // Act
        String result = runDetails.toString();

        // Assert
        assertTrue(result.contains("duration=3600"), "toString should contain duration");
        assertTrue(result.contains("timeStep=0.5"), "toString should contain timeStep");
        assertTrue(result.contains("mapFile='city.xml'"), "toString should contain mapFile");
    }
}