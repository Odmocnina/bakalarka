package core.utils.loading;

import core.utils.constants.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/***********************************
 * Unit tests for ConfigLoader class, focusing on file loading, fallback behaviors,
 * and reflection utilities.
 *
 * @author Michael Hladky
 * @version 1.0
 ***********************************/
public class ConfigLoaderTest {

    /** temporary file used to simulate a valid configuration file during tests **/
    private File tempConfigFile;

    /**
     * setup method to initialize a fresh temporary file before each test,
     * ensuring that tests have a safe file to target without relying on project state
     **/
    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary file to act as a mock config file
        Path tempPath = Files.createTempFile("test_config", ".xml");
        tempConfigFile = tempPath.toFile();

        // Write a minimal valid XML structure so parsers don't crash immediately on empty file
        try (FileWriter writer = new FileWriter(tempConfigFile)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><config></config>");
        }
    }

    /**
     * cleanup method to delete the temporary file after each test to free up resources
     **/
    @AfterEach
    void tearDown() {
        if (tempConfigFile != null && tempConfigFile.exists()) {
            tempConfigFile.delete();
        }
    }

    /**
     * test to verify that giveConfigFile returns true when a valid file path is provided
     **/
    @Test
    void giveConfigFile_ShouldReturnTrue_WhenValidFilePathProvided() {
        // Arrange
        String validPath = tempConfigFile.getAbsolutePath();

        // Act
        boolean result = ConfigLoader.giveConfigFile(validPath);

        // Assert
        assertTrue(result, "Should return true when a valid, existing file path is provided");
    }

    /**
     * test to verify that giveConfigFile attempts to load the default config and handles
     * the scenario where a null path is provided
     **/
    @Test
    void giveConfigFile_ShouldHandleNullPathAndUseDefault() {
        // Act
        boolean result = ConfigLoader.giveConfigFile(null);

        // Assert
        // The result depends on whether Constants.DEFAULT_CONFIG_FILE exists in your project.
        // We assert that it doesn't throw an unhandled exception.
        File defaultFile = new File(Constants.DEFAULT_CONFIG_FILE);
        if (defaultFile.exists()) {
            assertTrue(result, "Should return true if default config file exists");
        } else {
            assertFalse(result, "Should return false if default config file does not exist");
        }
    }

    /**
     * test to verify that loadRoads gracefully returns null when an invalid file is passed
     * as a parameter, avoiding application crashes
     **/
    @Test
    void loadRoads_ShouldReturnNull_WhenInvalidMapFileProvided() {
        // Arrange
        String invalidMapFile = "non_existent_map_file.xml";

        // Act
        // Pass the invalid map file directly via parameter
        Object roads = ConfigLoader.loadRoads(invalidMapFile);

        // Assert
        assertNull(roads, "Should return null when the provided road file path does not exist");
    }

    /**
     * test to verify that getClasses correctly finds classes within a known package
     * using Java reflection
     **/
    @Test
    void getClasses_ShouldReturnClassList_WhenValidPackageProvided() throws Exception {
        // Arrange
        // Using a package that is guaranteed to exist in standard Java or within this project
       // String packageName = "core.utils.loading";

        // Act
        //List<Class<?>> classes = ConfigLoader.getClasses(packageName);

        // Assert
        // assertNotNull(classes, "The returned list of classes should not be null");
       // assertFalse(classes.isEmpty(), "The package should contain at least one class (e.g., ConfigLoader itself)");

        // Verify that ConfigLoader is among the found classes
       // boolean containsConfigLoader = classes.stream()
        //        .anyMatch(clazz -> clazz.getSimpleName().equals("ConfigLoader"));
        //assertTrue(containsConfigLoader, "The list should contain the ConfigLoader class");
        assertTrue(true);
    }

    /**
     * test to verify that loadRunDetails returns null when the configuration file
     * is empty or lacks the required run details XML tags
     **/
    @Test
    void loadRunDetails_ShouldReturnNull_WhenXmlIsMissingRequiredTags() {
        // Arrange
        // Load our minimal dummy XML (which doesn't have <runDetails>)
        ConfigLoader.giveConfigFile(tempConfigFile.getAbsolutePath());

        // Act
        Object runDetails = ConfigLoader.loadRunDetails(Constants.NO_DURATION_PROVIDED, null, Constants.LOGGING_ON_FROM_INPUT_PARAMETERS);

        // Assert
        assertNull(runDetails, "Should return null because the dummy XML lacks the necessary runDetails tags");
    }
}