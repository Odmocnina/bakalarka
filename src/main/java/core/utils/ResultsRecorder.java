package core.utils;

import app.AppContext;
import core.model.Road;
import core.utils.constants.ConfigConstants;
import core.utils.constants.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

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

    /** Array to store the time when the road was empty (if needed for future extensions) **/
    private int[] whenWasRoadEmpty;

    /** BigInt value used as a start of timer when simulation starts **/
    private BigInteger timeStart;

    /** BigInt value used as a stop of timer when simulation stop **/
    private BigInteger timeEnd;

    /** Output file name for writing results **/
    private String fileName;

    /** Output type (txt/csv)**/
    private String outputType = "txt";

    /** Count of collisions during the simulation **/
    private int[] collisionsCount;

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
        this.collisionsCount = new int[numberOfRoads];
        for (int i = 0; i < numberOfRoads; i++) {
            this.collisionsCount[i] = 0;
        }
        this.whenWasRoadEmpty = new int[numberOfRoads];
        for (int i = 0; i < numberOfRoads; i++) {
            this.whenWasRoadEmpty[i] = Constants.NO_RECORD_YET;
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
        BigInteger currentEnd = (timeEnd == null) ? BigInteger.valueOf(System.nanoTime()) : timeEnd;
        return currentEnd.subtract(timeStart);
        //return timeEnd.subtract(timeStart);
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
     * writes the results in TXT format to the output file. It sets the output type to TXT and calls the write method.
     **/
    public void writeResultsTxt() {
        this.outputType = Constants.RESULTS_OUTPUT_TXT;
        write();
    }

    /**
     * writes the results in CSV format to the output file. It sets the output type to CSV and calls the write method.
     **/
    public void writeResultsCsv() {
        this.outputType = Constants.RESULTS_OUTPUT_CSV;
        write();
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
        if (outputType.equalsIgnoreCase(Constants.RESULTS_OUTPUT_TXT)) {
            this.writeTXT(bw);
        } else if (outputType.equalsIgnoreCase(Constants.RESULTS_OUTPUT_CSV)) {
            this.writeCSV(bw);
        } else {
            MyLogger.log("Unknown output type: " + outputType + ". Defaulting to TXT format.",
                    Constants.WARN_FOR_LOGGING);
            this.writeTXT(bw);
        }
        return bw;
    }

    /**
     * Writes detailed information about each road to the BufferedWriter.
     *
     * @param bw The BufferedWriter to write to.
     * @throws IOException If an I/O error occurs.
     **/
    private void writeRoadDetails(BufferedWriter bw) throws IOException {
        bw.write("=== Road Details ===\n");
        Road[] roads = AppContext.SIMULATION.getRoads();
        for (int i = 0; i < roads.length; i++) {
            bw.write("Road " + i + ": " + roads[i].toString() + "\n");
        }
        bw.write("\n");
    }

    /**
     * Writes simulation details to the BufferedWriter.
     *
     * @param bw The BufferedWriter to write to.
     * @throws IOException If an I/O error occurs.
     **/
    private void writeSimulationDetails(BufferedWriter bw) throws IOException {
        bw.write("=== Simulation details ===\n"); // simulation details
        bw.write("Forward model used: " + AppContext.CAR_FOLLOWING_MODEL.getName() + "(" +
                AppContext.CAR_FOLLOWING_MODEL.getType() + ")" + "\n"); // car following model
        bw.write("Lane changing model used: " + AppContext.LANE_CHANGING_MODEL.getName() + "\n");//lane change model
        bw.write("Simulation parameters: " + AppContext.RUN_DETAILS.toString() + "\n"); // simulation parameters

       /* boolean queueUsed = AppContext.SIMULATION.getRoads()[0].generatingToQueue();
        if (queueUsed) {
            bw.write("Car generation: Cars were generated to queues before entering the roads.\n");
            if (AppContext.SIMULATION.areAllQueuesEmpty(AppContext.SIMULATION.getRoads())) {
                bw.write("All car queues were emptied during the simulation. Number of steps in simulation: " +
                        AppContext.SIMULATION.getStepCount() + "\n\n");
            } else {
                bw.write("During the simulation time (" + AppContext.RUN_DETAILS.duration + " steps), not all cars "
                        + " in queues were emptied.\n\n");
            }
        } else {
            bw.write("Car generation: Cars were generated directly on the roads.\n\n");
        }*/
        bw.write("\n");
    }

    /**
     * Writes the total simulation time results to the BufferedWriter.
     *
     * @param bw The BufferedWriter to write to.
     * @throws IOException If an I/O error occurs.
     **/
    private void writeSimulationTimeResults(BufferedWriter bw) throws IOException {
        bw.write("=== Simulation Time Results ===\n");
        BigInteger elapsedTime = getElapsedTimeNs();
        int timeMillis = elapsedTime.divide(BigInteger.valueOf(1_000_000)).intValue();
        bw.write("Total Simulation Time: " + timeMillis + " ms\n\n");
    }

    /**
     * Writes the number of cars passed results to the BufferedWriter.
     *
     * @param bw The BufferedWriter to write to.
     * @throws IOException If an I/O error occurs.
     **/
    private void writeCarsPassedResults(BufferedWriter bw) throws IOException {
        bw.write("=== Cars Passed Results ===\n");
        int totalCarsPassed = 0;
        for (int i = 0; i < carsPassedPerRoad.length; i++) {
            bw.write("Road " + i + ": " + carsPassedPerRoad[i] + " cars passed.\n");
            totalCarsPassed += carsPassedPerRoad[i];
        }
        bw.write("Total Cars Passed: " + totalCarsPassed + "\n\n");
    }

    /**
     * Writes the car generation parameters for each road to the BufferedWriter.
     *
     * @param bw The BufferedWriter to write to.
     * @throws IOException If an I/O error occurs.
     **/
    private void writeGenerationParams(BufferedWriter bw) throws IOException {
        bw.write("=== Car Generation Parameters ===\n");
        Road[] roads = AppContext.SIMULATION.getRoads();
        for (int i = 0; i < roads.length; i++) {
            bw.write("Road " + i + " Generation Params: " + roads[i].getCarGenerator().toString() + "\n");
        }
    }

    /**
     * writes the number of cars currently on the road for each road to the BufferedWriter.
     *
     * @param bw The BufferedWriter to write to.
     * @throws IOException If an I/O error occurs.
     **/
    private void writeCarsOnTheRoad(BufferedWriter bw) throws IOException {
        bw.write("=== Cars Currently on the Road ===\n");
        Road[] roads = AppContext.SIMULATION.getRoads();
        int totalCarsOnRoad = 0;
        for (int i = 0; i < roads.length; i++) {
            bw.write("Road " + i + " Cars: " + roads[i].getNumberOfCarsOnRoad() + "\n");
            totalCarsOnRoad += roads[i].getNumberOfCarsOnRoad();
        }
        bw.write("Total Cars on Road: " + totalCarsOnRoad + "\n\n");
    }

    /**
     * writes the total number of collisions that occurred during the simulation to the BufferedWriter.
     *
     * @param bw The BufferedWriter to write to.
     * @throws IOException If an I/O error occurs.
     **/
    private void writeCollisionsCount(BufferedWriter bw) throws IOException {
        bw.write("=== Collisions Count ===\n");
        int totalCollisions = 0;
        for (int i = 0; i < collisionsCount.length; i++) {
            bw.write("Road " + i + ": " + collisionsCount[i] + " collisions.\n");
            totalCollisions += collisionsCount[i];
        }
        bw.write("Total Collisions: " + totalCollisions + "\n\n");
    }

    private void writeWhenWasRoadEmpty(BufferedWriter bw) throws IOException {
        bw.write("=== When Was Road Empty ===\n");
        for (int i = 0; i < whenWasRoadEmpty.length; i++) {
            if (whenWasRoadEmpty[i] != Constants.NO_RECORD_YET) {
                bw.write("Road " + i + " was empty at step: " + whenWasRoadEmpty[i] + "\n");
            } else {
                bw.write("Road " + i + " was never empty during the simulation.\n");
            }
        }
        bw.write("\n");
    }

    /**
     * writes the recorded results in TXT format to the BufferedWriter. It checks the output details settings and writes
     * the corresponding sections to the BufferedWriter.
     *
     * @param bw The BufferedWriter to write to.
     * @throws IOException If an I/O error occurs.
     **/
    private void writeTXT(BufferedWriter bw) throws IOException {
        // Implementation for writing results in TXT format
        bw.write("=== Traffic Simulation Results ===\n\n"); // header of the results file
        OutputDetails outputDetails = AppContext.RUN_DETAILS.outputDetails;
        if (outputDetails == null) {
            MyLogger.log("RunDetails is null. Cannot write simulation details.", Constants.ERROR_FOR_LOGGING);
            return;
        }
        if (outputDetails.writePart(ConfigConstants.SIMULATION_DETAILS_TAG)) {
            this.writeSimulationDetails(bw);
        }
        if (outputDetails.writePart(ConfigConstants.SIMULATION_TIME_TAG)) {
            this.writeSimulationTimeResults(bw);
        }
        if (outputDetails.writePart(ConfigConstants.CARS_PASSED_TAG)) {
            this.writeCarsPassedResults(bw);
        }
        if (outputDetails.writePart(ConfigConstants.CARS_ON_ROAD_TAG)) {
            this.writeCarsOnTheRoad(bw);
        }
        if (outputDetails.writePart(ConfigConstants.WHEN_WAS_ROAD_EMPTY_TAG)) {
            this.writeWhenWasRoadEmpty(bw);
        }
        if (outputDetails.writePart(ConfigConstants.COLLISION_COUNT_TAG)) {
            this.writeCollisionsCount(bw);
        }
        if (outputDetails.writePart(ConfigConstants.ROAD_DETAILS_TAG)) {
            this.writeRoadDetails(bw);
        }
    }

    /**
     * writes the recorded results in CSV format to the BufferedWriter. It checks the output details settings and writes
     * the corresponding sections to the BufferedWriter in CSV format.
     *
     * @param bw The BufferedWriter to write to.
     * @throws IOException If an I/O error occurs.
     **/
    private void writeCSV(BufferedWriter bw) throws IOException {
        // Implementation for writing results in CSV format
        OutputDetails outputDetails = AppContext.RUN_DETAILS.outputDetails;
        String csvSeparator = outputDetails.csvSeparator;
        String header = "";
        header = header + "Road Index" + csvSeparator;
        if (outputDetails.writePart(ConfigConstants.CARS_PASSED_TAG)) {
            header = header + "Cars Passed" + csvSeparator;
        }
        if (outputDetails.writePart(ConfigConstants.CARS_ON_ROAD_TAG)) {
            header = header + "Cars on Road" + csvSeparator;
        }
        if (outputDetails.writePart(ConfigConstants.WHEN_WAS_ROAD_EMPTY_TAG)) {
            header = header + "When was road empty" + csvSeparator;
        }
        if (outputDetails.writePart(ConfigConstants.COLLISION_COUNT_TAG)) {
            header = header + "Collisions Count" + csvSeparator;
        }
        if (outputDetails.writePart(ConfigConstants.ROAD_DETAILS_TAG)) {
            header = header + "Road length" + csvSeparator + "Number of lanes" + csvSeparator + "Max speed" +
                    csvSeparator;
        }
        header = header.trim();

        bw.write(header + "\n");
        for (int i = 0; i < carsPassedPerRoad.length; i++) {
            bw.write(i + csvSeparator);
            if (outputDetails.writePart(ConfigConstants.CARS_PASSED_TAG)) {
                bw.write(carsPassedPerRoad[i] + csvSeparator);
            }
            if (outputDetails.writePart(ConfigConstants.CARS_ON_ROAD_TAG)) {
                bw.write(AppContext.SIMULATION.getRoads()[i].getNumberOfCarsOnRoad() + csvSeparator);
            }
            if (outputDetails.writePart(ConfigConstants.WHEN_WAS_ROAD_EMPTY_TAG)) {
                int emptyStep = whenWasRoadEmpty[i];
                String emptyStepStr = (emptyStep != Constants.NO_RECORD_YET) ?
                        String.valueOf(emptyStep) : "Never empty"; // write when was road empty or not empty at all
                bw.write(emptyStepStr + csvSeparator);
            }
            if (outputDetails.writePart(ConfigConstants.COLLISION_COUNT_TAG)) {
                bw.write(this.collisionsCount[i] + csvSeparator);
            }
            if (outputDetails.writePart(ConfigConstants.ROAD_DETAILS_TAG)) {
                //bw.write(AppContext.SIMULATION.getRoads()[i].toString() + csvSeparator);
                bw.write(AppContext.SIMULATION.getRoads()[i].getLength() + csvSeparator +
                        AppContext.SIMULATION.getRoads()[i].getNumberOfLanes() + csvSeparator +
                        AppContext.SIMULATION.getRoads()[i].getSpeedLimit() + csvSeparator);
            }
            bw.write("\n");
        }
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

    /**
     * add collision to the total count of collisions that occurred during the simulation, it increments the
     * collisionsCount array on road index by 1.
     *
     * @param roadIndex The index of the road where the collision occurred
     **/
    public void addCollision(int roadIndex) {
        this.collisionsCount[roadIndex]++;
    }

    /**
     * sets the output type for writing results, it updates the outputType variable with the given value (e.g., "txt" or
     * "csv").
     **/
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    /**
     * resets the recorded results, it sets all values in the carsPassedPerRoad array to 0 and resets the collisions
     * count to 0.
     **/
    public void resetCarNumbers() {
        if (this.carsPassedPerRoad != null) {
            Arrays.fill(this.carsPassedPerRoad, 0);
        }
        if (this.collisionsCount != null) {
            Arrays.fill(this.collisionsCount, 0);
        }
    }

    /**
     * sets the output file name for writing results.
     *
     * @param fileName The output file name to set.
     **/
    public void setOutFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * records the time when a road was empty, it updates the whenWasRoadEmpty array on the given road index with the
     * current step count of the simulation.
     *
     * @param roadIndex The index of the road that was empty.
     **/
    public void recordRoadEmpty(int roadIndex, int stepCount) {
        if (whenWasRoadEmpty != null && roadIndex >= 0 && roadIndex < whenWasRoadEmpty.length) {
            whenWasRoadEmpty[roadIndex] = stepCount;
        }
    }

    /**
     * checks if a road was already empty during the simulation, it checks the whenWasRoadEmpty array on the given road
     * index and returns true if the value is not 0 (indicating that the road was empty at some point), otherwise it
     * returns false.
     *
     * @param roadIndex The index of the road to check.
     * @return true if the road was already empty during the simulation, false otherwise.
     **/
    public boolean wasRoadAlreadyEmpty(int roadIndex) {
        if (whenWasRoadEmpty != null && roadIndex >= 0 && roadIndex < whenWasRoadEmpty.length) {
            return whenWasRoadEmpty[roadIndex] != Constants.NO_RECORD_YET;
        }
        return false;
    }
}
