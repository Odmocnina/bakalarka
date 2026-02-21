package core.utils;

import java.util.Random;

public class RandomNumberGenerator {

    /** Random seed for the simulation, can be set for reproducibility **/
    private long seed;

    /** Random number generator instance **/
    private Random random;

    private static RandomNumberGenerator randomNumberGenerator;

    /**
     * Constructor to initialize the random generator with a specific seed.
     *
     * @param seed the seed for the random number generator
     */
    private RandomNumberGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    /**
     * Static method to get the singleton instance of the RandomNumberGenerator.
     *
     * @param seed the seed for the random number generator
     * @return the singleton instance of RandomNumberGenerator
     **/
    public static RandomNumberGenerator getInstance(long seed) {
        if (randomNumberGenerator == null) {
            randomNumberGenerator = new RandomNumberGenerator(seed);
        }
        return randomNumberGenerator;
    }

    /**
     * Generates a random integer between min (inclusive) and max (inclusive).
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random integer between min and max
     */
    public int nextInt(int min, int max) {
        return random.nextInt((max - min + 1)) + min;
    }

    /**
     * Generates a random double between 0.0 (inclusive) and 1.0 (exclusive).
     *
     * @return a random double between 0.0 and 1.0
     */
    public double nextDouble() {
        return random.nextDouble();
    }

    /**
     * Resets the random number generator with a new seed.
     *
     * @param newSeed the new seed for the random number generator
     **/
    public void resetSeed(long newSeed) {
        this.seed = newSeed;
        this.random.setSeed(newSeed);
    }

    /**
     * reset the random number generator with the same seed, to ensure reproducibility in the simulation
     **/
    public void resetSeed() {
        this.random.setSeed(this.seed);
    }
}
