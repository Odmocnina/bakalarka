package core.model;

import core.utils.RandomNumberGenerator;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**************************************
 * Unit tests for CarGenerator class
 *
 * @author Michael Hladky
 * @version 1.0
 **************************************/
@ExtendWith(MockitoExtension.class)
public class CarGeneratorTest {

    /** The CarGenerator instance under test **/
    private CarGenerator generator;

    /** Mocked RandomNumberGenerator to control randomness in tests **/
    @Mock
    private RandomNumberGenerator mockRandom;

    /**
     * Set up method to initialize the CarGenerator and inject the mocked RandomNumberGenerator
     * before each test. Also initializes AppContext.RUN_DETAILS to prevent NPE in MyLogger.
     **/
    @BeforeEach
    void setUp() throws Exception {
        // Initialize AppContext.RUN_DETAILS to prevent NPE in MyLogger
        app.AppContext.RUN_DETAILS = new core.utils.RunDetails();
        app.AppContext.RUN_DETAILS.log = new boolean[6]; // Initialize logging array with default false values

        // Initialize the generator with a default lambda of 2.0 (cars per second)
        generator = new core.model.CarGenerator(2.0);

        // Inject the mocked RandomNumberGenerator via reflection
        Field randomField = CarGenerator.class.getDeclaredField("randomNumberGenerator");
        randomField.setAccessible(true);
        randomField.set(generator, mockRandom);
    }

    /**
     * test to verify that negative density is properly coerced to 0.0 in the constructor
     **/
    @Test
    void constructor_NegativeDensity_ShouldSetLambdaToZero() {
        // Arrange & Act
        CarGenerator negativeGen = new CarGenerator(-5.0);

        // Assert
        assertEquals(0.0, negativeGen.getLambdaPerSec(), "Lambda should not be negative");
    }

    /**
     * test to verify that decideIfNewCar returns true or false based on the Bernoulli process
     * when multiple cars per tick are NOT allowed
     **/
    @Test
    void decideIfNewCar_SinglePerTick_ShouldFollowBernoulli() {
        // Arrange
        generator.setAllowMultiplePerTick(false);
        // p = 1.0 - exp(-2.0) = ~0.864
        // if nextDouble() < 0.864, it should return true

        // Act & Assert - Should generate car
        when(mockRandom.nextDouble()).thenReturn(0.5);
        assertTrue(generator.decideIfNewCar(), "Should generate car when random < p");

        // Act & Assert - Should NOT generate car
        when(mockRandom.nextDouble()).thenReturn(0.99);
        assertFalse(generator.decideIfNewCar(), "Should not generate car when random > p");
    }

    /**
     * test to verify continuous car generation sets correct scaled parameters and assigns a color
     **/
    @Test
    void generateCar_Continuous_ShouldSetScaledParameters() {
        // Arrange
        generator.setType(Constants.CONTINUOUS);
        generator.setCarGenerationParameters("speed" + RequestConstants.REQUEST_SEPARATOR + "accel");

        // Add range parameter
        generator.addParameter("speed", "Speed Limit", 50.0, 100.0);
        // Add fixed parameter (range 0)
        generator.addParameter("accel", "Acceleration", 3.5, 3.5);

        // Mock random value for range calculation (0.5 means exact middle of the range -> 75.0)
        // Also used for color assignment
        when(mockRandom.nextDouble()).thenReturn(0.5);

        // Act
        CarParams car = generator.generateCar();

        // Assert
        assertNotNull(car, "Car should be generated");
        assertEquals(75.0, car.getParameter("speed"), "Speed should be interpolated to middle of range");
        assertEquals(3.5, car.getParameter("accel"), "Acceleration should be exact fixed value");
        assertNotNull(car.color, "Car must have a color assigned");
       // assertTrue(car.id > 0, "Car ID should be auto-incremented");
    }

    /**
     * test to verify cellular car generation sets correct integer parameters and respects lengthReturnAsOne
     **/
    @Test
    void generateCar_Cellular_ShouldSetIntegerParameters() {
        // Arrange
        generator.setType(Constants.CELLULAR);
        generator.setCarGenerationParameters(RequestConstants.LENGTH_REQUEST + RequestConstants.REQUEST_SEPARATOR + "speed");
        generator.setLengthReturnAsOne(true);
        generator.addParameter("speed", "Speed Limit", 1.0, 5.0);

        // Mock random integer generation for cellular speed
        when(mockRandom.nextInt(1, 5)).thenReturn(4);
        when(mockRandom.nextDouble()).thenReturn(0.1); // for color

        // Act
        CarParams car = generator.generateCar();

        // Assert
        assertEquals(1.0, car.getParameter(RequestConstants.LENGTH_REQUEST), "Length should be forced to 1 for cellular");
        assertEquals(4.0, car.getParameter("speed"), "Speed should be generated via nextInt");
    }

    /**
     * test to verify that setType with CELLULAR properly translates continuous parameters
     * by dividing by cell size and rounding up
     **/
    @Test
    void setType_Cellular_ShouldTranslateParameters() {
        // Arrange
        generator.addParameter("size", "Size", 10.0, 20.0);

        // Act - Translate to cellular with cell size 3.0
        // min = ceil(10/3) = 4, max = ceil(20/3) = 7
        generator.setType(Constants.CELLULAR, 3.0);

        // Assert
        Parameter param = generator.getAllParameters().get("size");
        assertEquals(4.0, param.minValue, "Min value should be correctly translated");
        assertEquals(7.0, param.maxValue, "Max value should be correctly translated");
        assertEquals(3.0, param.range, "Range should be correctly updated");
    }

    /**
     * test to verify generateCarsInToQueue correctly generates the specified random amount of cars
     **/
    @Test
    void generateCarsInToQueue_ShouldGenerateCorrectNumberOfCars() {
        // Arrange
        generator.setType(Constants.CONTINUOUS);
        generator.setCarGenerationParameters("");
        generator.setQueueSize(2, 5);

        // Mock nextInt to decide queue will have exactly 3 cars
        when(mockRandom.nextInt(2, 5)).thenReturn(3);
        when(mockRandom.nextDouble()).thenReturn(0.1); // for colors

        // Act
        Queue<CarParams> queue = generator.generateCarsInToQueue();

        // Assert
        assertEquals(3, queue.size(), "Queue should contain exactly 3 generated cars");
    }

    /**
     * test to verify checkIfAllParametersAreLoaded validates existence and ranges
     * of all requested parameters
     **/
    @Test
    void checkIfAllParametersAreLoaded_ShouldReturnTrueOnlyIfValid() {
        // Arrange
        String[] params = new String[]{"speed", "politeness"};
        generator.setCarGenerationParameters(String.join(RequestConstants.REQUEST_SEPARATOR, params));

        // Act & Assert 1: Parameters missing
        assertFalse(generator.checkIfAllParametersAreLoaded(), "Should fail if parameters are missing");

        // Act & Assert 2: Parameters exist but one is invalid (min > max)
        generator.addParameter("speed", "Speed", 100.0, 50.0);
        generator.addParameter("politeness", "Politeness", 0.0, 1.0);
        assertFalse(generator.checkIfAllParametersAreLoaded(), "Should fail if parameter range is invalid");

        // Act & Assert 3: All valid
        generator.addParameter("speed", "Speed", 50.0, 100.0); // Fix the invalid parameter
        assertTrue(generator.checkIfAllParametersAreLoaded(), "Should pass when all requested parameters are loaded and valid");
    }

    /**
     * test to verify getMissingParameters identifies missing or invalid communication parameters
     **/
    @Test
    void getMissingParameters_ShouldListMissingOrInvalidParams() {
        // Arrange
        String[] params = new String[]{"req1", "req2"};
        generator.setCarGenerationParameters(String.join(RequestConstants.REQUEST_SEPARATOR, params));

        // Only add req1, leave req2 missing. Make req1 invalid (min > max)
        generator.addComParameter("req1", "Requirement 1", 50.0, 10.0);

        // Act
        String missing = generator.getMissingParameters();

        // Assert
        assertTrue(missing.contains("req1"), "Should flag invalid parameter");
        assertTrue(missing.contains("req2"), "Should flag missing parameter");
    }

    /**
     * test to verify that copyComParametersToRealParameters safely clones communication parameters
     * over to real parameters and translates them if necessary
     **/
    @Test
    void copyComParametersToRealParameters_ShouldCopyAndTranslate() {
        // Arrange
        generator.addComParameter("testParam", "Test", 15.0, 25.0);

        // Act - Copy and translate to cellular with cell size 5.0
        // min = ceil(15/5) = 3, max = ceil(25/5) = 5
        generator.copyComParametersToRealParameters(Constants.CELLULAR, 5.0);

        // Assert
        Parameter realParam = generator.getAllParameters().get("testParam");
        assertNotNull(realParam, "Parameter should have been copied");
        assertEquals(3.0, realParam.minValue, "Parameter should have been translated during copy");
        assertEquals(5.0, realParam.maxValue, "Parameter should have been translated during copy");
    }

    /**
     * test to verify the clone method creates a deep copy of the generator and its parameter maps
     **/
    @Test
    void clone_ShouldCreateDeepCopy() {
        // Arrange
        generator.setType("TEST_TYPE");
        generator.setAllowMultiplePerTick(true);
        generator.setQueueSize(3, 8);
        generator.addParameter("p1", "P1", 1.0, 2.0);
        generator.addComParameter("c1", "C1", 5.0, 10.0);

        // Act
        CarGenerator cloned = generator.clone();

        // Assert basic fields
        assertNotSame(generator, cloned, "Cloned instance should be a different object");
        assertEquals(generator.getLambdaPerSec(), cloned.getLambdaPerSec(), "Lambda should match");
        assertEquals(generator.getMinQueueSize(), cloned.getMinQueueSize(), "Min queue size should match");
        assertEquals(generator.getMaxQueueSize(), cloned.getMaxQueueSize(), "Max queue size should match");
        assertTrue(cloned.generatingToQueue(), "Should be generating to queue");

        // Assert deep copy of parameter maps
        assertNotSame(generator.getAllParameters(), cloned.getAllParameters(), "Real parameters map should be independent");
        assertNotSame(generator.getAllComParameters(), cloned.getAllComParameters(), "Comm parameters map should be independent");

        // Verify values inside the cloned maps
        assertEquals(1.0, cloned.getAllParameters().get("p1").minValue);
        assertEquals(5.0, cloned.getAllComParameters().get("c1").minValue);
    }
}
