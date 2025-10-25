package core.utils;

import java.math.BigInteger;

public class ResultsRecorder {
    private static ResultsRecorder instance = null;
    private int[] carsPassedPerRoad;
    private BigInteger timeStart;

    private ResultsRecorder() {
    }

    public static ResultsRecorder getResultsRecorder() {
        if (instance == null) {
            instance = new ResultsRecorder();
        }
        return instance;
    }

    public void initialize(int numberOfRoads) {
        carsPassedPerRoad = new int[numberOfRoads];
        for (int i = 0; i < numberOfRoads; i++) {
            carsPassedPerRoad[i] = 0;
        }
    }

    public void recordCarPassed(int roadIndex) {
        if (carsPassedPerRoad != null && roadIndex >= 0 && roadIndex < carsPassedPerRoad.length) {
            carsPassedPerRoad[roadIndex]++;
        }
    }

    public void startTimer() {
        timeStart = BigInteger.valueOf(System.nanoTime());
    }

    public BigInteger getElapsedTimeNs() {
        if (timeStart == null) {
            return BigInteger.ZERO;
        }
        return BigInteger.valueOf(System.nanoTime()).subtract(timeStart);
    }

}
