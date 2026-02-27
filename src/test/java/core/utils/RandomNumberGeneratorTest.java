package core.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/*************************
 * Unit tests for RandomNumberGenerator class, focusing on singleton behavior and random number generation
 *
 * @author Michael Hladky
 * @version 1.0
 ************************/
public class RandomNumberGeneratorTest {

    /**
     * setup method to reset the singleton instance before each test, ensuring that tests do not interfere with each
     * other's state
     **/
    @BeforeEach
    void setUp() throws Exception {
        // Reset the singleton instance before each test to ensure a clean state
        // This prevents tests from affecting each other's sequences
        Field instanceField = RandomNumberGenerator.class.getDeclaredField("randomNumberGenerator");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    /**
     * test to verify that the singleton pattern works and returns the same instance
     * even if a different seed is provided on the second call
     **/
    @Test
    void getInstance_ShouldReturnSameSingletonInstance() {
        RandomNumberGenerator instance1 = RandomNumberGenerator.getInstance(12345L);
        RandomNumberGenerator instance2 = RandomNumberGenerator.getInstance(54321L); // Seed should be ignored here

        assertSame(instance1, instance2, "Both instances should be exactly the same object in memory");
    }

    /**
     * test to verify that nextInt generates numbers strictly within the specified min and max bounds
     **/
    @Test
    void nextInt_ShouldGenerateNumbersWithinBounds() {
        RandomNumberGenerator rng = RandomNumberGenerator.getInstance(42L);
        int min = 5;
        int max = 10;

        // Run multiple times to ensure the bounds are consistently respected
        for (int i = 0; i < 100; i++) {
            int result = rng.nextInt(min, max);
            assertTrue(result >= min && result <= max, "Generated number " + result + " should be between " + min + " and " + max);
        }
    }

    /**
     * test to verify that nextDouble generates numbers between 0.0 (inclusive) and 1.0 (exclusive)
     **/
    @Test
    void nextDouble_ShouldGenerateNumbersWithinBounds() {
        RandomNumberGenerator rng = RandomNumberGenerator.getInstance(42L);

        // Run multiple times to ensure the bounds are consistently respected
        for (int i = 0; i < 100; i++) {
            double result = rng.nextDouble();
            assertTrue(result >= 0.0 && result < 1.0, "Generated double " + result + " should be >= 0.0 and < 1.0");
        }
    }

    /**
     * test to verify that calling resetSeed() without arguments restarts the sequence of random numbers,
     * ensuring exact reproducibility of the simulation
     **/
    @Test
    void resetSeed_WithoutArguments_ShouldRestartSameSequence() {
        RandomNumberGenerator rng = RandomNumberGenerator.getInstance(123L);

        // Generate a sequence of 5 numbers
        int[] firstSequence = new int[5];
        for (int i = 0; i < 5; i++) {
            firstSequence[i] = rng.nextInt(1, 100);
        }

        // Reset the seed using the internally stored seed
        rng.resetSeed();

        // Generate the sequence again, it should be identical to the first one
        int[] secondSequence = new int[5];
        for (int i = 0; i < 5; i++) {
            secondSequence[i] = rng.nextInt(1, 100);
        }

        assertArrayEquals(firstSequence, secondSequence, "The sequences should be exactly the same after calling resetSeed()");
    }

    /**
     * test to verify that resetting with a new explicit seed changes the sequence,
     * and that setting the same explicit seed twice produces matching numbers
     **/
    @Test
    void resetSeed_WithNewSeed_ShouldProduceConsistentSequenceForThatSeed() {
        RandomNumberGenerator rng = RandomNumberGenerator.getInstance(100L);

        // Set explicit seed to 999 and generate two numbers
        rng.resetSeed(999L);
        int val1 = rng.nextInt(1, 1000);
        int val2 = rng.nextInt(1, 1000);

        // Reset seed to 999 again and generate two numbers
        rng.resetSeed(999L);
        int val3 = rng.nextInt(1, 1000);
        int val4 = rng.nextInt(1, 1000);

        assertEquals(val1, val3, "First number of the sequence should match when seed is explicitly reset to 999");
        assertEquals(val2, val4, "Second number of the sequence should match when seed is explicitly reset to 999");
    }
}