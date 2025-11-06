package core.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResultsRecorder {
    private static final Logger logger = LogManager.getLogger(ResultsRecorder.class);
    private static ResultsRecorder instance = null;
    private int[] carsPassedPerRoad;
    private BigInteger timeStart;
    private BigInteger timeEnd;
    private String fileName;

    private ResultsRecorder() {
    }

    public static ResultsRecorder getResultsRecorder() {
        if (instance == null) {
            instance = new ResultsRecorder();
        }
        return instance;
    }

    public void initialize(int numberOfRoads, String fileName) {
        this.carsPassedPerRoad = new int[numberOfRoads];
        for (int i = 0; i < numberOfRoads; i++) {
            this.carsPassedPerRoad[i] = 0;
        }
        this.fileName = fileName;
    }

    public void recordCarPassed(int roadIndex) {
        if (carsPassedPerRoad != null && roadIndex >= 0 && roadIndex < carsPassedPerRoad.length) {
            carsPassedPerRoad[roadIndex]++;
        }
    }

    public void recordCarsPassed(int roadIndex, int count) {
        if (carsPassedPerRoad != null && roadIndex >= 0 && roadIndex < carsPassedPerRoad.length) {
            carsPassedPerRoad[roadIndex] = carsPassedPerRoad[roadIndex] + count;
        }
    }

    public void startTimer() {
        timeStart = BigInteger.valueOf(System.nanoTime());
    }

    public void stopTimer() {
        timeEnd = BigInteger.valueOf(System.nanoTime());
    }

    public BigInteger getElapsedTimeNs() {
        if (timeStart == null) {
            return BigInteger.ZERO;
        }
        return timeEnd.subtract(timeStart);
    }

    public void writeResults() {
        writeCarsPassedResults();
    }

    private void writeCarsPassedResults() {
        if (this.fileName != null && !this.fileName.isEmpty()) {
            try {
                File file = new File(fileName);
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("=== Simulation Time Results ===\n");
                BigInteger elapsedTime = getElapsedTimeNs();
                int timeMillis = elapsedTime.divide(BigInteger.valueOf(1_000_000)).intValue();
                bw.write("Total Simulation Time: " + timeMillis + " ms\n\n");
                bw.write("=== Cars Passed Results ===\n");
                for (int i = 0; i < carsPassedPerRoad.length; i++) {
                    bw.write("Road " + i + ": " + carsPassedPerRoad[i] + " cars passed.\n");
                }
                bw.close();
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            logger.warn("Output file name is not set. Cannot write results.");
        }
    }

    public int getCarsPassedOnRoad(int index) {
        if (carsPassedPerRoad != null && index >= 0 && index < carsPassedPerRoad.length) {
            return carsPassedPerRoad[index];
        }
        return 0;
    }

}
