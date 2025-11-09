package core.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

/********************************
 * Singleton class responsible for recording and writing simulation results such as the number of cars passed on each
 * road and the total simulation time.
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************/
public class ResultsRecorder {

    /** Singleton instance **/
    private static ResultsRecorder instance = null;

    /** Array to store the number of cars passed per road **/
    private int[] carsPassedPerRoad;

    /** BigInt value used as a start of timer when simulation starts **/
    private BigInteger timeStart;

    /** BigInt value used as a stop of timer when simulation stop **/
    private BigInteger timeEnd;

    /** Output file name for writing results **/
    private String fileName;

    /**
     * Private constructor to prevent instantiation
     **/
    private ResultsRecorder() {}

    /**
     * Returns the singleton instance of ResultsRecorder.
     *
     * @return The singleton instance.
     **/
    public static ResultsRecorder getResultsRecorder() {
        if (instance == null) {
            instance = new ResultsRecorder();
        }
        return instance;
    }

    /**
     * Initializes the ResultsRecorder with the number of roads and output file name.
     *
     * @param numberOfRoads The number of roads in the simulation.
     * @param fileName The output file name for writing results.
     **/
    public void initialize(int numberOfRoads, String fileName) {
        this.carsPassedPerRoad = new int[numberOfRoads];
        for (int i = 0; i < numberOfRoads; i++) {
            this.carsPassedPerRoad[i] = 0;
        }
        this.fileName = fileName;
    }

    /**
     * Records that a car (only one) has passed on the specified road.
     *
     * @param roadIndex The index of the road where the car passed.
     **/
    public void recordCarPassed(int roadIndex) {
        if (carsPassedPerRoad != null && roadIndex >= 0 && roadIndex < carsPassedPerRoad.length) {
            carsPassedPerRoad[roadIndex]++;
        }
    }

    /**
     * Records that multiple cars have passed on the specified road.
     *
     * @param roadIndex The index of the road where the cars passed.
     * @param count The number of cars that passed.
     **/
    public void recordCarsPassed(int roadIndex, int count) {
        if (carsPassedPerRoad != null && roadIndex >= 0 && roadIndex < carsPassedPerRoad.length) {
            carsPassedPerRoad[roadIndex] = carsPassedPerRoad[roadIndex] + count;
        }
    }

    /**
     * Starts the timer for measuring simulation time.
     **/
    public void startTimer() {
        timeStart = BigInteger.valueOf(System.nanoTime());
    }

    /**
     * Stops the timer for measuring simulation time.
     **/
    public void stopTimer() {
        timeEnd = BigInteger.valueOf(System.nanoTime());
    }

    /**
     * Returns the elapsed simulation time in nanoseconds.
     *
     * @return The elapsed time in nanoseconds as a BigInteger.
     **/
    public BigInteger getElapsedTimeNs() {
        if (timeStart == null) {
            return BigInteger.ZERO;
        }
        return timeEnd.subtract(timeStart);
    }

    /**
     * Writes the recorded results to the output file.
     **/
    public void writeResults() {
        write();
    }

    /**
     * Writes the number of cars passed on each road and the total simulation time to the output file.
     **/
    private void write() {
        if (this.fileName != null && !this.fileName.isEmpty()) {
            try {
                File file = new File(fileName);
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = getBufferedWriter(fw);
                bw.close();
                fw.close();
            } catch (Exception e) {
                MyLogger.log("Error writing results to file: " + e.getMessage(), Constants.ERROR_FOR_LOGGING);
            }
        } else {
            MyLogger.log("Output file name is not set. Cannot write results.", Constants.WARN_FOR_LOGGING);
        }
    }

    /**
     * Creates a BufferedWriter and writes the simulation time and cars passed results to it.
     *
     * @param fw The FileWriter to wrap with BufferedWriter.
     * @return The BufferedWriter with written results.
     * @throws IOException If an I/O error occurs.
     **/
    private BufferedWriter getBufferedWriter(FileWriter fw) throws IOException {
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("=== Simulation Time Results ===\n");
        BigInteger elapsedTime = getElapsedTimeNs();
        int timeMillis = elapsedTime.divide(BigInteger.valueOf(1_000_000)).intValue();
        bw.write("Total Simulation Time: " + timeMillis + " ms\n\n");
        bw.write("=== Cars Passed Results ===\n");
        for (int i = 0; i < carsPassedPerRoad.length; i++) {
            bw.write("Road " + i + ": " + carsPassedPerRoad[i] + " cars passed.\n");
        }
        return bw;
    }

    /**
     * Retrieves the number of cars passed on a specific road.
     *
     * @param index The index of the road.
     * @return The number of cars passed on the specified road.
     **/
    public int getCarsPassedOnRoad(int index) {
        if (carsPassedPerRoad != null && index >= 0 && index < carsPassedPerRoad.length) {
            return carsPassedPerRoad[index];
        }
        return 0;
    }

}
