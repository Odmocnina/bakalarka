package core.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/*************************
 * Unit tests for OutputDetails class, focusing on configuration map management and XML parsing
 *
 * @author Michael Hladky
 * @version 1.0
 ************************/
public class OutputDetailsTest {

    /** instance of OutputDetails to be used in tests, it will be re-initialized before each test to ensure isolation **/
    private OutputDetails outputDetails;

    /**
     * setup method to initialize a fresh instance of OutputDetails before each test, ensuring that tests do not
     * interfere with each other's state and that we always start with a clean slate
     **/
    @BeforeEach
    void setUp() {
        // Initialize a fresh instance before each test
        outputDetails = new OutputDetails();
    }

    /**
     * test to verify that the constructor initializes the map with default values
     **/
    @Test
    void constructor_ShouldSetDefaultValues() {
        Map<String, Boolean> map = outputDetails.getWhatToOutput();

        // Assert that the map is populated with defaults
        assertFalse(map.isEmpty(), "The whatToOutput map should not be empty after initialization");

        // Pick a few known defaults to verify
        // (Assuming ConfigConstants tags are properly loaded in the actual class)
        assertTrue(outputDetails.writePart(core.utils.constants.ConfigConstants.SIMULATION_DETAILS_TAG),
                "SIMULATION_DETAILS_TAG should be true by default");
        assertFalse(outputDetails.writePart(core.utils.constants.ConfigConstants.DETAILED_LANE_QUEUE_LENGTH_TAG),
                "DETAILED_LANE_QUEUE_LENGTH_TAG should be false by default");
    }

    /**
     * test to verify that writePart returns the correct boolean or false if the tag is missing
     **/
    @Test
    void writePart_ShouldReturnCorrectValues() {
        // Arrange
        outputDetails.getWhatToOutput().put("existing_true_tag", true);
        outputDetails.getWhatToOutput().put("existing_false_tag", false);

        // Act & Assert
        assertTrue(outputDetails.writePart("existing_true_tag"), "Should return true for an existing true tag");
        assertFalse(outputDetails.writePart("existing_false_tag"), "Should return false for an existing false tag");
        assertFalse(outputDetails.writePart("non_existent_tag"), "Should safely return false for tags that are not in the map");
    }

    /**
     * test to verify that setPart correctly adds new entries or updates existing ones
     **/
    @Test
    void setPart_ShouldUpdateMap() {
        // Act - Set a new tag
        outputDetails.setPart("custom_test_tag", true);

        // Act - Update an existing default tag
        outputDetails.setPart(core.utils.constants.ConfigConstants.SIMULATION_DETAILS_TAG, false);

        // Assert
        assertTrue(outputDetails.writePart("custom_test_tag"), "New tag should be added and set to true");
        assertFalse(outputDetails.writePart(core.utils.constants.ConfigConstants.SIMULATION_DETAILS_TAG),
                "Existing tag should be updated to false");
    }

    /**
     * test to verify that changeWhatToOutput properly parses an XML Element
     * and updates the internal map accordingly
     **/
    @Test
    void changeWhatToOutput_ShouldParseXmlAndUpdateMap() throws Exception {
        // Arrange: Build a real, small XML document in memory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Create root element <outputSettings>
        Element rootElement = doc.createElement("outputSettings");

        // Create child <test_tag_1>true</test_tag_1>
        Element child1 = doc.createElement("test_tag_1");
        child1.setTextContent("true");
        rootElement.appendChild(child1);

        // Create child <test_tag_2>false</test_tag_2>
        Element child2 = doc.createElement("test_tag_2");
        child2.setTextContent("false");
        rootElement.appendChild(child2);

        // Act
        outputDetails.changeWhatToOutput(rootElement);

        // Assert
        assertTrue(outputDetails.writePart("test_tag_1"), "Map should be updated with 'test_tag_1' = true from XML");
        assertFalse(outputDetails.writePart("test_tag_2"), "Map should be updated with 'test_tag_2' = false from XML");
    }

    /**
     * test to verify that toString returns a string containing key configuration fields
     **/
    @Test
    void toString_ShouldContainKeyFields() {
        // Arrange
        outputDetails.outputFile = "test_results.csv";
        outputDetails.csvSeparator = ";";

        // Act
        String result = outputDetails.toString();

        // Assert
        assertTrue(result.contains("outputFile='test_results.csv'"), "toString should contain outputFile");
        assertTrue(result.contains("csvSeparator=';'"), "toString should contain csvSeparator");
        assertTrue(result.contains("whatToOutput="), "toString should contain whatToOutput map");
    }
}
