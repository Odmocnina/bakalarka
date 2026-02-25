package app;

import core.utils.MyLogger;
import core.utils.constants.Constants;
import core.utils.loading.ConfigLoader;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import models.ModelId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/*****************************
 * Unit tests for InputParametersHandeler class
 *
 * @author Michael Hladky
 * @version 1.0
 ****************************/
@ExtendWith(MockitoExtension.class)
public class InputParametersHandelerTest {

    // --------------------------------------------------------------------------------
    // DUMMY CLASSES FOR REFLECTION TESTING
    // --------------------------------------------------------------------------------

    /**********************
     * Class for testing reflection logic in getCarFollowingModelById method. This class is annotated with
     * @ModelId("dummy-car") and implements ICarFollowingModel.
     *
     * @author Michael Hladky
     * @version 1.0
     **********************/
    @ModelId("dummy-car")
    public static abstract class DummyCarModel implements ICarFollowingModel {
        // Abstract class used for testing reflection logic.
        // Instantiating this will intentionally throw an InstantiationException,
        // which helps us cover the catch block in the getCarFollowingModelById method.
    }

    /**********************
     * Class for testing reflection logic in getLaneChangingModelById method. This class is annotated with
     * @ModelId("dummy-car") and implements ICarFollowingModel.
     *
     * @author Michael Hladky
     * @version 1.0
     **********************/
    @ModelId("dummy-lane")
    public static abstract class DummyLaneModel implements ILaneChangingModel {
        // Abstract class used for testing reflection logic.
    }

    // --------------------------------------------------------------------------------

    /**
     * test to verify that if the prefix exists in the arguments, the substring after the prefix is returned
     **/
    @Test
    void getSpecificParameter_PrefixExists_ShouldReturnSubstring() {
        String[] args = {"--time=10", "--file=test.xml"};
        String result = InputParametersHandeler.getSpecificParameter(args, "--file=");
        assertEquals("test.xml", result, "Should return the correct substring after the prefix");
    }

    /**
     * test to verify that if the prefix does not exist in any argument, null is returned
     **/
    @Test
    void getSpecificParameter_PrefixDoesNotExist_ShouldReturnNull() {
        String[] args = {"--time=10", "--file=test.xml"};
        String result = InputParametersHandeler.getSpecificParameter(args, "--config=");
        assertNull(result, "Should return null if prefix is not found");
    }

    /**
     * test to verify that if the argument starts with the given prefix, the rest of the string is returned
     **/
    @Test
    void getPartOfParameter_PrefixMatches_ShouldReturnSubstring() {
        String result = InputParametersHandeler.getPartOfParameter("--file=test.xml", "--file=");
        assertEquals("test.xml", result, "Should return substring when prefix matches");
    }

    /**
     * test to verify that if the argument does not start with the prefix, null is returned
     **/
    @Test
    void getPartOfParameter_PrefixDoesNotMatch_ShouldReturnNull() {
        String result = InputParametersHandeler.getPartOfParameter("--file=test.xml", "--config=");
        assertNull(result, "Should return null when prefix does not match");
    }

    /**
     * test to verify that if duration is null, the NO_DURATION_PROVIDED constant is returned
     **/
    @Test
    void getDurationFromParameter_NullDuration_ShouldReturnNoDurationProvided() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            int result = InputParametersHandeler.getDurationFromParameter(null);
            assertEquals(Constants.NO_DURATION_PROVIDED, result);
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(anyString(), anyString()), times(1));
        }
    }

    /**
     * test to verify that if duration is a negative number, the INVALID_INPUT_PARAMETERS constant is returned
     **/
    @Test
    void getDurationFromParameter_NegativeDuration_ShouldReturnInvalidInput() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            int result = InputParametersHandeler.getDurationFromParameter("-5");
            assertEquals(Constants.INVALID_INPUT_PARAMETERS, result);
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("cannot be negative"), anyString()), times(1));
        }
    }

    /**
     * test to verify that if duration is a valid number, the parsed integer value is returned
     **/
    @Test
    void getDurationFromParameter_ValidDuration_ShouldReturnParsedValue() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            int result = InputParametersHandeler.getDurationFromParameter("100");
            assertEquals(100, result);
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("Duration provided"), anyString()), times(1));
        }
    }

    /**
     * test to verify that if duration is an invalid string format, the INVALID_INPUT_PARAMETERS constant is returned
     **/
    @Test
    void getDurationFromParameter_InvalidFormat_ShouldReturnInvalidInput() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            int result = InputParametersHandeler.getDurationFromParameter("abc");
            assertEquals(Constants.INVALID_INPUT_PARAMETERS, result);
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("Invalid duration value"), anyString()), times(1));
        }
    }

    /**
     * test to verify that if config path is null or empty, the default config file path is returned
     **/
    @Test
    void getConfigPathFromParameter_NullOrEmpty_ShouldReturnDefault() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            assertEquals(Constants.DEFAULT_CONFIG_FILE, InputParametersHandeler.getConfigPathFromParameter(null));
            assertEquals(Constants.DEFAULT_CONFIG_FILE, InputParametersHandeler.getConfigPathFromParameter(""));
        }
    }

    /**
     * test to verify that if config path is valid, the provided path is returned
     **/
    @Test
    void getConfigPathFromParameter_ValidPath_ShouldReturnPath() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            assertEquals("custom.xml", InputParametersHandeler.getConfigPathFromParameter("custom.xml"));
        }
    }

    /**
     * test to verify that if output file path is null or empty, the default output file path is returned
     **/
    @Test
    void getOutputFilePathFromParameter_NullOrEmpty_ShouldReturnDefault() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            assertEquals(Constants.DEFAULT_OUTPUT_FILE, InputParametersHandeler.getOutputFilePathFromParameter(null));
            assertEquals(Constants.DEFAULT_OUTPUT_FILE, InputParametersHandeler.getOutputFilePathFromParameter(""));
        }
    }

    /**
     * test to verify that if output file path is valid, the provided path is returned
     **/
    @Test
    void getOutputFilePathFromParameter_ValidPath_ShouldReturnPath() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            assertEquals("out.csv", InputParametersHandeler.getOutputFilePathFromParameter("out.csv"));
        }
    }

    /**
     * test to verify that if logging is null or empty, the LOGGING_NOT_PROVIDED_IN_INPUT_PARAMETERS constant is returned
     **/
    @Test
    void getLoggingFromParameter_NullOrEmpty_ShouldReturnNotProvided() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            assertEquals(Constants.LOGGING_NOT_PROVIDED_IN_INPUT_PARAMETERS, InputParametersHandeler.getLoggingFromParameter(null));
            assertEquals(Constants.LOGGING_NOT_PROVIDED_IN_INPUT_PARAMETERS, InputParametersHandeler.getLoggingFromParameter(""));
        }
    }

    /**
     * test to verify that if logging parameter is 'true' (case-insensitive), it returns LOGGING_ON_FROM_INPUT_PARAMETERS
     **/
    @Test
    void getLoggingFromParameter_True_ShouldReturnOn() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            assertEquals(Constants.LOGGING_ON_FROM_INPUT_PARAMETERS, InputParametersHandeler.getLoggingFromParameter("true"));
            assertEquals(Constants.LOGGING_ON_FROM_INPUT_PARAMETERS, InputParametersHandeler.getLoggingFromParameter("TRUE"));
        }
    }

    /**
     * test to verify that if logging parameter is 'false' or invalid string, it evaluates to false and returns LOGGING_OFF_FROM_INPUT_PARAMETERS
     **/
    @Test
    void getLoggingFromParameter_FalseOrInvalid_ShouldReturnOff() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            assertEquals(Constants.LOGGING_OFF_FROM_INPUT_PARAMETERS, InputParametersHandeler.getLoggingFromParameter("false"));
            // Boolean.parseBoolean evaluates any non-"true" string as false, so "invalid" also goes to the else branch
            assertEquals(Constants.LOGGING_OFF_FROM_INPUT_PARAMETERS, InputParametersHandeler.getLoggingFromParameter("invalid"));
        }
    }

    /**
     * test to verify that if map file is null or empty, null is returned
     **/
    @Test
    void handleMapFileParameter_NullOrEmpty_ShouldReturnNull() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            assertNull(InputParametersHandeler.handleMapFileParameter(null));
            assertNull(InputParametersHandeler.handleMapFileParameter(""));
        }
    }

    /**
     * test to verify that if map file is valid, the provided file path is returned
     **/
    @Test
    void handleMapFileParameter_ValidPath_ShouldReturnPath() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            assertEquals("map.xml", InputParametersHandeler.handleMapFileParameter("map.xml"));
        }
    }

    // --------------------------------------------------------------------------------
    // TESTS FOR MODEL REFLECTION (CAR FOLLOWING & LANE CHANGING)
    // --------------------------------------------------------------------------------

    /**
     * test to verify that if car following model parameter is null or empty, it returns null
     **/
    @Test
    void getCarFollowingModelFromParameter_NullOrEmpty_ShouldReturnNull() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            assertNull(InputParametersHandeler.getCarFollowingModelFromParameter(null));
            assertNull(InputParametersHandeler.getCarFollowingModelFromParameter(""));
        }
    }

    /**
     * test to verify that if car following model parameter has a valid id, it calls getCarFollowingModelById and returns the model
     **/
    @Test
    void getCarFollowingModelFromParameter_ValidId_ShouldCallGetByIdAndReturn() {
        try (MockedStatic<InputParametersHandeler> spy = mockStatic(InputParametersHandeler.class, CALLS_REAL_METHODS);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            ICarFollowingModel mockModel = mock(ICarFollowingModel.class);
            spy.when(() -> InputParametersHandeler.getCarFollowingModelById("valid-id")).thenReturn(mockModel);

            ICarFollowingModel result = InputParametersHandeler.getCarFollowingModelFromParameter("valid-id");

            assertEquals(mockModel, result, "Should return the mocked model retrieved by ID");
        }
    }

    /**
     * test to verify that if car following model parameter has an invalid id, it logs fatal error and returns null
     **/
    @Test
    void getCarFollowingModelFromParameter_InvalidId_ShouldCallGetByIdAndReturnNull() {
        try (MockedStatic<InputParametersHandeler> spy = mockStatic(InputParametersHandeler.class, CALLS_REAL_METHODS);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            spy.when(() -> InputParametersHandeler.getCarFollowingModelById("invalid-id")).thenReturn(null);

            assertNull(InputParametersHandeler.getCarFollowingModelFromParameter("invalid-id"));
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("Invalid car following model"), anyString()), atLeastOnce());
        }
    }

    /**
     * test to verify that getCarFollowingModelById returns null when id is null or empty
     **/
    @Test
    void getCarFollowingModelById_NullOrEmpty_ShouldReturnNull() {
        assertNull(InputParametersHandeler.getCarFollowingModelById(null));
        assertNull(InputParametersHandeler.getCarFollowingModelById(""));
    }

    /**
     * test to verify that getCarFollowingModelById triggers exception on abstract class instantiation,
     * which covers the try-catch block for reflection failures
     **/
    @Test
    void getCarFollowingModelById_ClassFoundButAbstract_ShouldThrowExceptionAndReturnNull() {
        try (MockedStatic<ConfigLoader> configMock = mockStatic(ConfigLoader.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            configMock.when(() -> ConfigLoader.getClasses(anyString())).thenReturn(List.of(DummyCarModel.class));

            ICarFollowingModel result = InputParametersHandeler.getCarFollowingModelById("dummy-car");

            assertNull(result, "Should return null because abstract class throws InstantiationException");
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("Error searching for model by ID"), anyString()), times(1));
        }
    }

    /**
     * test to verify that getCarFollowingModelById returns null when no matching class is found
     **/
    @Test
    void getCarFollowingModelById_ClassNotFound_ShouldReturnNull() {
        try (MockedStatic<ConfigLoader> configMock = mockStatic(ConfigLoader.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            configMock.when(() -> ConfigLoader.getClasses(anyString())).thenReturn(List.of(String.class)); // String class is not ICarFollowingModel

            ICarFollowingModel result = InputParametersHandeler.getCarFollowingModelById("dummy-car");

            assertNull(result, "Should return null when class is not found");
        }
    }

    /**
     * test to verify that if lane changing model parameter is null or empty, it returns null
     **/
    @Test
    void getLaneChangingModelFromParameter_NullOrEmpty_ShouldReturnNull() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {
            assertNull(InputParametersHandeler.getLaneChangingModelFromParameter(null));
            assertNull(InputParametersHandeler.getLaneChangingModelFromParameter(""));
        }
    }

    /**
     * test to verify that if lane changing model parameter has a valid id, it calls getLaneChangingModelById and returns the model
     **/
    @Test
    void getLaneChangingModelFromParameter_ValidId_ShouldCallGetByIdAndReturn() {
        try (MockedStatic<InputParametersHandeler> spy = mockStatic(InputParametersHandeler.class, CALLS_REAL_METHODS);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            ILaneChangingModel mockModel = mock(ILaneChangingModel.class);
            spy.when(() -> InputParametersHandeler.getLaneChangingModelById("valid-id")).thenReturn(mockModel);

            ILaneChangingModel result = InputParametersHandeler.getLaneChangingModelFromParameter("valid-id");

            assertEquals(mockModel, result, "Should return the mocked model retrieved by ID");
        }
    }

    /**
     * test to verify that if lane changing model parameter has an invalid id, it logs fatal error and returns null
     **/
    @Test
    void getLaneChangingModelFromParameter_InvalidId_ShouldCallGetByIdAndReturnNull() {
        try (MockedStatic<InputParametersHandeler> spy = mockStatic(InputParametersHandeler.class, CALLS_REAL_METHODS);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            spy.when(() -> InputParametersHandeler.getLaneChangingModelById("invalid-id")).thenReturn(null);

            assertNull(InputParametersHandeler.getLaneChangingModelFromParameter("invalid-id"));
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("Invalid lane changing model"), anyString()), atLeastOnce());
        }
    }

    /**
     * test to verify that getLaneChangingModelById returns null when id is null or empty
     **/
    @Test
    void getLaneChangingModelById_NullOrEmpty_ShouldReturnNull() {
        assertNull(InputParametersHandeler.getLaneChangingModelById(null));
        assertNull(InputParametersHandeler.getLaneChangingModelById(""));
    }

    /**
     * test to verify that getLaneChangingModelById triggers exception on abstract class instantiation,
     * which covers the try-catch block for reflection failures
     **/
    @Test
    void getLaneChangingModelById_ClassFoundButAbstract_ShouldThrowExceptionAndReturnNull() {
        try (MockedStatic<ConfigLoader> configMock = mockStatic(ConfigLoader.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            configMock.when(() -> ConfigLoader.getClasses(anyString())).thenReturn(List.of(DummyLaneModel.class));

            ILaneChangingModel result = InputParametersHandeler.getLaneChangingModelById("dummy-lane");

            assertNull(result, "Should return null because abstract class throws InstantiationException");
            loggerMock.verify(() -> MyLogger.logLoadingOrSimulationStartEnd(contains("Error searching for model by ID"), anyString()), times(1));
        }
    }

    /**
     * test to verify that getLaneChangingModelById returns null when no matching class is found
     **/
    @Test
    void getLaneChangingModelById_ClassNotFound_ShouldReturnNull() {
        try (MockedStatic<ConfigLoader> configMock = mockStatic(ConfigLoader.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            configMock.when(() -> ConfigLoader.getClasses(anyString())).thenReturn(List.of(String.class));

            ILaneChangingModel result = InputParametersHandeler.getLaneChangingModelById("dummy-lane");

            assertNull(result, "Should return null when class is not found");
        }
    }
}