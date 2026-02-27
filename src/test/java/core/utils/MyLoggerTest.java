package core.utils;

import app.AppContext;
import core.utils.constants.Constants;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

/****************************
 * Unit tests for MyLogger class
 *
 * @author Michael Hladky
 * @version 1.0
 ****************************/
@ExtendWith(MockitoExtension.class)
public class MyLoggerTest {

    /** Mocked logger to verify logging behavior without relying on actual log output **/
    @Mock
    private Logger mockLogger;

    /** Store the original logger to restore it after tests to avoid side effects on other test classes **/
    private Logger originalLogger;

    /**
     * Setup method to inject the mock logger into MyLogger before each test
     **/
    @BeforeEach
    void setUp() throws Exception {
        // Setup AppContext and default log settings (all true by default)
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.RUN_DETAILS.log = new boolean[] {true, true, true, true, true, true};
        Field loggerField = MyLogger.class.getDeclaredField("logger");
        loggerField.setAccessible(true);

        // Store the original logger, so we can restore it after the test
        originalLogger = (Logger) loggerField.get(null);

        // Inject the mock
        loggerField.set(null, mockLogger);
    }

    /**
     * Teardown method to restore the original logger after each test to prevent interference with other tests
     **/
    @AfterEach
    void tearDown() throws Exception {
        // Restore the original logger to not break other test classes
        Field loggerField = MyLogger.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(null, originalLogger);
    }

    /**
     * test to verify that messages are logged correctly to their respective levels
     * when general logging and specific level logging are enabled
     **/
    @Test
    void log_ShouldLogToCorrectLevel_WhenEnabled() {
        // Act
        MyLogger.log("Info message", Constants.INFO_FOR_LOGGING);
        MyLogger.log("Debug message", Constants.DEBUG_FOR_LOGGING);
        MyLogger.log("Error message", Constants.ERROR_FOR_LOGGING);
        MyLogger.log("Warn message", Constants.WARN_FOR_LOGGING);
        MyLogger.log("Fatal message", Constants.FATAL_FOR_LOGGING);

        // Assert
        verify(mockLogger).info("Info message");
        verify(mockLogger).debug("Debug message");
        verify(mockLogger).error("Error message");
        verify(mockLogger).warn("Warn message");
        verify(mockLogger).fatal("Fatal message");
    }

    /**
     * test to verify that absolutely nothing is logged when the general logging
     * switch is turned off, regardless of specific level settings
     **/
    @Test
    void log_ShouldNotLogAnything_WhenGeneralLoggingIsDisabled() {
        // Arrange - Turn off the main general logging switch
        AppContext.RUN_DETAILS.log[Constants.GENERAL_LOGGING_INDEX] = false;

        // Act
        MyLogger.log("This should not be logged", Constants.INFO_FOR_LOGGING);
        MyLogger.log("This should not be logged either", Constants.ERROR_FOR_LOGGING);
        MyLogger.log("This should also not be logged", Constants.DEBUG_FOR_LOGGING);
        MyLogger.log("This should not be logged as well", Constants.WARN_FOR_LOGGING);
        MyLogger.log("This should definitely not be logged", Constants.FATAL_FOR_LOGGING);

        // Assert
        verifyNoInteractions(mockLogger);
    }

    /**
     * test to verify that a specific level is ignored if its individual switch is off,
     * while other enabled levels still work fine
     **/
    @Test
    void log_ShouldNotLogSpecificLevel_WhenThatLevelIsDisabled() {
        // Arrange - Turn off ONLY the INFO logging switch
        AppContext.RUN_DETAILS.log[Constants.INFO_LOGGING_INDEX] = false;

        // Act
        MyLogger.log("Ignored info message", Constants.INFO_FOR_LOGGING);
        MyLogger.log("Allowed error message", Constants.ERROR_FOR_LOGGING);

        // Assert
        verify(mockLogger, never()).info(anyString()); // Info should be ignored
        verify(mockLogger).error("Allowed error message"); // Error should pass
    }

    /**
     * test to verify that logLoadingOrSimulationStartEnd logs messages directly
     * regardless of AppContext settings (since it's used before settings are loaded)
     **/
    @Test
    void logLoadingOrSimulationStartEnd_ShouldLogRegardlessOfSettings() {
        // Arrange - Turn off ALL logging in AppContext
        AppContext.RUN_DETAILS.log = new boolean[] {false, false, false, false, false, false};

        // Act
        MyLogger.logLoadingOrSimulationStartEnd("Critical boot info", Constants.INFO_FOR_LOGGING);
        MyLogger.logLoadingOrSimulationStartEnd("Critical boot error", Constants.ERROR_FOR_LOGGING);
        MyLogger.logLoadingOrSimulationStartEnd("Critical boot fatal", Constants.FATAL_FOR_LOGGING);
        MyLogger.logLoadingOrSimulationStartEnd("Critical boot warn", Constants.WARN_FOR_LOGGING);
        MyLogger.logLoadingOrSimulationStartEnd("Critical boot debug", Constants.DEBUG_FOR_LOGGING);

        // Assert - The logger should still record everything properly
        verify(mockLogger).info("Critical boot info");
        verify(mockLogger).error("Critical boot error");
        verify(mockLogger).fatal("Critical boot fatal");
        verify(mockLogger).warn("Critical boot warn");
        verify(mockLogger).debug("Critical boot debug");
    }

    /**
     * test to verify that invalid or unknown log levels are safely ignored
     * without causing crashes
     **/
    @Test
    void log_ShouldIgnoreUnknownLevels() {
        // Act
        MyLogger.log("Unknown level message", "UNKNOWN_LEVEL");
        MyLogger.logLoadingOrSimulationStartEnd("Unknown level loading message", "MAGIC_LEVEL");

        // Assert - No logging method should have been triggered
        verifyNoInteractions(mockLogger);
    }
}
