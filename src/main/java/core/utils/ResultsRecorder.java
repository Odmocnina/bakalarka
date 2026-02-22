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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

    /** Lane changes count **/
    private int[] laneChangesCount;

    /** records of stopped cars on roads **/
    private StoppedCarsOnRoadRecord[] stoppedCarsOnRoadRecord;

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
     * @param roads The array of roads in the simulation, used to determine the size of the carsPassedPerRoad array.
     * @param fileName The output file name for writing results.
     **/
    public void initialize(Road[] roads, String fileName) {
        int numberOfRoads = roads.length;
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
        this.laneChangesCount = new int[numberOfRoads];
        for (int i = 0; i < numberOfRoads; i++) {
            this.laneChangesCount[i] = 0;
        }
        this.stoppedCarsOnRoadRecord = new StoppedCarsOnRoadRecord[numberOfRoads];
        for (int i = 0; i < numberOfRoads; i++) {
            this.stoppedCarsOnRoadRecord[i] = new StoppedCarsOnRoadRecord(roads[i].getNumberOfLanes());
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
     * removes the .txt or .csv extension from the file name if it exists, it checks if the file name ends with either
     * ".txt" or ".csv" and removes the extension if found, a d puts extension back to the file name based on the output
     * type (txt or csv).
     *
     * @param fileName The file name to resolve.
     * @return The resolved file name with the correct extension based on the output type.
     */
    private String resolveFileName(String fileName) {
        if (!fileName.endsWith(".txt") && outputType.equalsIgnoreCase(Constants.RESULTS_OUTPUT_TXT)) {
            String[] parts = fileName.split("\\.(?=[^.]+$)"); // Split on the last dot
            String name = parts[0]; // Get the name part before the extension
            return name + ".txt";
        } else if (!fileName.endsWith(".csv") && outputType.equalsIgnoreCase(Constants.RESULTS_OUTPUT_CSV)) {
            String[] parts = fileName.split("\\.(?=[^.]+$)"); // Split on the last dot
            String name = parts[0]; // Get the name part before the extension
            return name + ".csv";
        }
        return fileName;
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
                File file = new File(resolveFileName(this.fileName));
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

    /**
     * writes the time when each road was empty during the simulation to the BufferedWriter. It iterates through the
     * whenWasRoadEmpty array and writes the step count when each road was empty, or indicates if a road was never
     * empty during the simulation. Used when all generators are queue based, and we want to see when the roads
     * (and queues) were empty during the simulation.
     *
     * @param bw The BufferedWriter to write to.
     * @throws IOException If an I/O error occurs.
     **/
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
        if (outputDetails.writePart(ConfigConstants.LANE_CHANGES_COUNT_TAG)) {
            this.writeLaneChangesCount(bw);
        }
        if (outputDetails.writePart(ConfigConstants.AVERAGE_LANE_QUEUE_LENGTH_TAG)) {
            this.writeAverageLaneQueueLength(bw);
        }
        if (outputDetails.writePart(ConfigConstants.DETAILED_LANE_QUEUE_LENGTH_TAG)) {
            if (outputDetails.writePart(ConfigConstants.EXPORT_DETAILED_TO_SEPARATE_FILES_TAG)) {
                this.processDetailedLaneQueueOutputSeparateFiles(outputDetails, AppContext.SIMULATION.getStepCount());
            } else {
                this.processDetailedLaneQueueOutput(outputDetails, AppContext.SIMULATION.getStepCount());
            }
        }
        if (outputDetails.writePart(ConfigConstants.DETAILED_LIGHT_PLANS_TAG)) {
            if (outputDetails.writePart(ConfigConstants.EXPORT_DETAILED_TO_SEPARATE_FILES_TAG)) {
                this.processLightPlanOfAllRoadsSeparateFiles(outputDetails, AppContext.SIMULATION.getStepCount());
            } else {
                this.processLightPlanOfAllRoads(outputDetails, AppContext.SIMULATION.getStepCount());
            }
        }
        if (outputDetails.writePart(ConfigConstants.COLLISION_COUNT_TAG)) {
            this.writeCollisionsCount(bw);
        }
        if (outputDetails.writePart(ConfigConstants.ROAD_DETAILS_TAG)) {
            this.writeRoadDetails(bw);
        }
    }

    /**
     * writes the lane changes count results to the BufferedWriter, it iterates through the laneChangesCount array and
     * writes the number of lane changes for each road, as well as the total number of lane changes across all roads.
     *
     * @param bw The BufferedWriter to write to.
     * @throws IOException If an I/O error occurs.
     **/
    private void writeLaneChangesCount(BufferedWriter bw) throws IOException {
        bw.write("=== Lane Changes Count ===\n");
        int totalLaneChanges = 0;
        for (int i = 0; i < laneChangesCount.length; i++) {
            bw.write("Road " + i + ": " + laneChangesCount[i] + " lane changes.\n");
            totalLaneChanges += laneChangesCount[i];
        }
        bw.write("Total Lane Changes: " + totalLaneChanges + "\n\n");
    }

    /**
     * writes the average lane queue length results to the BufferedWriter, it iterates through the stoppedCarsOnRoadRecord
     * array and writes the average lane queue length for each road, as well as the overall average lane queue length
     * across all roads.
     *
     * @param bw The BufferedWriter to write to.
     * @throws IOException If an I/O error occurs.
     **/
    private void writeAverageLaneQueueLength(BufferedWriter bw) throws IOException {
        bw.write("=== Average Lane Queue Length ===\n");
        double totalAverageQueueLength = 0.0;
        int numberOfRoads = stoppedCarsOnRoadRecord.length;
        for (int i = 0; i < stoppedCarsOnRoadRecord.length; i++) {
            double averageQueueLength = getAverageLaneQueueLength(i);
            bw.write("Road " + i + ": Average Lane Queue Length: " + averageQueueLength + "\n");
            totalAverageQueueLength += averageQueueLength;
        }
        bw.write("Overall Average Lane Queue Length: " + (totalAverageQueueLength / numberOfRoads) + "\n\n");
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
        if (outputDetails.writePart(ConfigConstants.LANE_CHANGES_COUNT_TAG)) {
            header = header + "Lane Changes Count" + csvSeparator;
        }
        if (outputDetails.writePart(ConfigConstants.AVERAGE_LANE_QUEUE_LENGTH_TAG)) {
            header = header + "Average Lane Queue Length" + csvSeparator;
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
            if (outputDetails.writePart(ConfigConstants.LANE_CHANGES_COUNT_TAG)) {
                bw.write(this.laneChangesCount[i] + csvSeparator);
            }
            if (outputDetails.writePart(ConfigConstants.AVERAGE_LANE_QUEUE_LENGTH_TAG)) {
                double averageQueueLength = getAverageLaneQueueLength(i);
                bw.write(averageQueueLength + csvSeparator);
            }
            if (outputDetails.writePart(ConfigConstants.COLLISION_COUNT_TAG)) {
                bw.write(this.collisionsCount[i] + csvSeparator);
            }
            if (outputDetails.writePart(ConfigConstants.ROAD_DETAILS_TAG)) {
                bw.write(AppContext.SIMULATION.getRoads()[i].getLength() + csvSeparator +
                        AppContext.SIMULATION.getRoads()[i].getNumberOfLanes() + csvSeparator +
                        AppContext.SIMULATION.getRoads()[i].getSpeedLimit() + csvSeparator);
            }
            bw.write("\n");
        }
    }

    /**
     * Retrieves the average lane queue length for a specific road.
     *
     * @param roadIndex The index of the road to retrieve the average lane queue length for.
     * @return The average lane queue length for the specified road.
     **/
    private double getAverageLaneQueueLength(int roadIndex) {
        StoppedCarsOnRoadRecord record = stoppedCarsOnRoadRecord[roadIndex];
        return record.getAverageStoppedCars();
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
        if (this.laneChangesCount != null) {
            Arrays.fill(this.laneChangesCount, 0);
        }
        if (this.whenWasRoadEmpty != null) {
            Arrays.fill(this.whenWasRoadEmpty, Constants.NO_RECORD_YET);
        }
        if (this.stoppedCarsOnRoadRecord != null) {
            for (StoppedCarsOnRoadRecord record : this.stoppedCarsOnRoadRecord) {
                record.reset();
            }
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

    /**
     * records a lane change on the specified road, it increments the laneChangesCount array on the given road index by
     * 1.
     *
     * @param roadIndex The index of the road where the lane change occurred.
     **/
    public void recordLaneChange(int roadIndex) {
        this.laneChangesCount[roadIndex]++;
    }

    /**
     * records the number of stopped cars on a specific road and lane, it updates the stoppedCarsOnRoadRecord for the
     * given road index by recording the count of stopped cars, whether they were on red light, and the lane index.
     *
     * @param count The number of stopped cars to record.
     * @param onRed A boolean indicating whether the cars were stopped on a red light.
     * @param roadIndex The index of the road where the cars are stopped.
     * @param lane The index of the lane where the cars are stopped.
     **/
    public void recordNumberOfStoppedCars(int count, boolean onRed, int roadIndex, int lane) {
        this.stoppedCarsOnRoadRecord[roadIndex].recordStoppedCars(count, onRed, lane);
    }

    /**
     * processes the detailed lane queue output by writing the data for each road and lane to a single CSV file, it
     * creates a file name for the detailed lane queue output by appending "DetailedLaneQueue.csv" to the base name of
     * the output file and then calls the writeDetailedLaneQueue method to write the data to that file.
     *
     * @param outputDetails The OutputDetails object containing the settings for output generation.
     * @param stepCount The total number of steps in the simulation.
     **/
    private void processDetailedLaneQueueOutput(OutputDetails outputDetails, int stepCount) {
        String[] baseFileNameParts = this.fileName.split("\\.(?=[^.]+$)"); // Split on the last dot
        String filePath = baseFileNameParts[0] + "DetailedLaneQueue.csv"; // Append suffix to the base name
        writeDetailedLaneQueue(filePath, outputDetails.csvSeparator, stepCount);
    }

    /**
     * writes the detailed lane queue data to a CSV file, it creates a BufferedWriter to write the data to the specified
     * file path, it writes the header with road and lane information and then iterates through each time step and lane
     * to write the count of stopped cars for that lane and step based on the stopped cars record.
     *
     * @param filePath The path of the file to write the detailed lane queue data to.
     * @param csvSeparator The separator to use in the CSV file.
     * @param stepCount The total number of steps in the simulation.
     **/
    private void writeDetailedLaneQueue(String filePath, String csvSeparator, int stepCount) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            StringBuilder firstLine = new StringBuilder("Step" + csvSeparator);
            for (int i = 0; i < stoppedCarsOnRoadRecord.length; i++) {
                for (int j = 0; j < stoppedCarsOnRoadRecord[i].stoppedCarsPerStep.size(); j++) {
                    firstLine.append("Road ").append(i).append(" Lane ").append(j).append(csvSeparator);
                }
            }
            bw.write(firstLine.toString().trim() + "\n");
            for (int step = 0; step < stepCount; step++) {
                StringBuilder line = new StringBuilder(step + csvSeparator);
                for (StoppedCarsOnRoadRecord record : stoppedCarsOnRoadRecord) {
                    for (int lane = 0; lane < record.stoppedCarsPerStep.size(); lane++) {
                        addNumberOfSoppedCarsToString(csvSeparator, record, step, line, lane);
                    }
                }
                bw.write(line.toString().trim() + "\n");
            }
        } catch (IOException e) {
            MyLogger.log("Error writing standing cars data to file: " + e.getMessage(), Constants.ERROR_FOR_LOGGING);
        }
    }

    /**
     * processes the light plan of all roads by writing the data for each road and lane to a single CSV file, it creates
     * a file name for the light plan output by appending "LightPlanOfAllRoads.csv" to the base name of the output file
     * and then calls the writeLightPlanOfAllRoads method to write the data to that file.
     *
     * @param outputDetails The OutputDetails object containing the settings for output generation.
     * @param stepCount The total number of steps in the simulation.
     **/
    private void processLightPlanOfAllRoads(OutputDetails outputDetails, int stepCount) {
        String[] baseFileNameParts = this.fileName.split("\\.(?=[^.]+$)"); // Split on the last dot
        String filePath = baseFileNameParts[0] + "LightPlanOfAllRoads.csv"; // Append suffix to the base name
        writeLightPlanOfAllRoads(filePath, outputDetails.csvSeparator, stepCount);
    }

    /**
     * writes the light plan of all roads to a CSV file, it creates a BufferedWriter to write the data to the specified
     * file path, it writes the header with road and lane information and then iterates through each time step and lane
     * to write whether the light was red or green for that lane and step based on the stopped cars record.
     *
     * @param fileName The path of the file to write the light plan data to.
     * @param csvSeparator The separator to use in the CSV file.
     * @param stepCount The total number of steps in the simulation.
     **/
    private void writeLightPlanOfAllRoads(String fileName, String csvSeparator, int stepCount) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            StringBuilder firstLine = new StringBuilder("Step" + csvSeparator);
            int road = 0;
            for (StoppedCarsOnRoadRecord record : stoppedCarsOnRoadRecord) {
                for (int lane = 0; lane < record.stoppedCarsPerStep.size(); lane++) {
                    firstLine.append("Road ").append(road).append(" Lane ").append(lane).append(csvSeparator);
                }
                road++;
            }
            bw.write(firstLine.toString().trim() + "\n");
            for (int step = 0; step < stepCount; step++) {
                StringBuilder line = new StringBuilder(step + csvSeparator);
                for (StoppedCarsOnRoadRecord record : stoppedCarsOnRoadRecord) {
                    int numberOfLanes = record.stoppedCarsPerStep.size();
                    for (int lane = 0; lane < numberOfLanes; lane++) {
                        NumberOfStandingCars entry = record.getStoppedCarsAtStep(lane, step);
                        if (entry.onRed) {
                            line.append("Was red").append(csvSeparator);
                        } else {
                            line.append("Was green").append(csvSeparator);
                        }
                    }
                    road++;
                }
                bw.write(line.toString().trim() + "\n");
            }
        } catch (IOException e) {
            MyLogger.log("Error writing light plan data to file: " + e.getMessage(), Constants.ERROR_FOR_LOGGING);
        }
    }

    /**
     * processes the detailed lane queue output by writing the data for each road to separate files, it creates a directory
     * for detailed outputs and then iterates through each road to write the lane queue data for that road to a separate
     * file using the writeDetailedLaneQueue method.
     *
     * @param outputDetails The OutputDetails object containing the settings for output generation.
     * @param stepCount The total number of steps in the simulation.
     **/
    private void processDetailedLaneQueueOutputSeparateFiles(OutputDetails outputDetails, int stepCount) {
        String[] baseFileNameParts = this.fileName.split("\\.(?=[^.]+$)");
        String baseDirPath = baseFileNameParts[0] + "DetailedExport"; // folder for detailed outputs

        for (int i = 0; i < stoppedCarsOnRoadRecord.length; i++) {
            // create separate directory for each road
            String roadDirPath = baseDirPath + File.separator + "road" + i;
            if (!createFolderForRoadDetailedOutputs(roadDirPath)) {
                MyLogger.log("Skipping light plan output for road " + i + " due to directory creation failure.",
                        Constants.ERROR_FOR_LOGGING);
                continue; // Skip this road if we couldn't create the directory
            }

            String filePath = roadDirPath + File.separator + "DetailedLaneQueue.csv";
            // give the record for each road and write it to a separate file
            writeDetailedLaneQueueOneRoad(filePath, outputDetails.csvSeparator, stepCount, stoppedCarsOnRoadRecord[i]);
        }
    }

    /**
     * writes the detailed lane queue data for one road to a CSV file, it creates a BufferedWriter to write the data to the
     * specified file path, it writes the header with lane numbers and then iterates through each time step and lane to write
     * the number of stopped cars for that lane and step based on the stopped cars record.
     *
     * @param filePath The path of the file to write the detailed lane queue data to.
     * @param csvSeparator The separator to use in the CSV file.
     * @param stepCount The total number of steps in the simulation.
     * @param record The StoppedCarsOnRoadRecord containing the data for one road of stopped cars for each lane and step.
     **/
    private void writeDetailedLaneQueueOneRoad(String filePath, String csvSeparator, int stepCount, StoppedCarsOnRoadRecord record) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            int numberOfLanes = record.stoppedCarsPerStep.size();
            bw.write(createFirstLineForDetailedExportSeparateFile(numberOfLanes, csvSeparator));

            for (int step = 0; step < stepCount; step++) {
                StringBuilder line = new StringBuilder(step + csvSeparator);
                for (int lane = 0; lane < numberOfLanes; lane++) {
                    addNumberOfSoppedCarsToString(csvSeparator, record, step, line, lane);
                }
                bw.write(line.toString().trim() + "\n");
            }
        } catch (IOException e) {
            MyLogger.log("Error writing standing cars data to file: " + e.getMessage(), Constants.ERROR_FOR_LOGGING);
        }
    }

    /**
     * adds the number of stopped cars for a specific lane and step to the line string, it retrieves the list of stopped
     * cars for the given lane from the record and checks if there is an entry for the current step. If there is an
     * entry, it appends the count of stopped cars to the line string, otherwise it appends "0" to indicate no data for
     * that step.
     *
     * @param csvSeparator The separator to use in the CSV file.
     * @param record The StoppedCarsOnRoadRecord containing the data for one road of stopped cars for each lane and step.
     * @param step The current time step in the simulation.
     * @param line The StringBuilder object representing the current line being constructed for the CSV file.
     * @param lane The index of the lane to retrieve the stopped cars data for.
     **/
    private void addNumberOfSoppedCarsToString(String csvSeparator, StoppedCarsOnRoadRecord record, int step,
                                               StringBuilder line, int lane) {
        LinkedList<NumberOfStandingCars> stoppedCarsList = record.stoppedCarsPerStep.get(lane);
        if (step < stoppedCarsList.size()) {
            NumberOfStandingCars entry = stoppedCarsList.get(step);
            line.append(entry.count).append(csvSeparator);
        } else {
            line.append("0").append(csvSeparator); // No data for this step
        }
    }

    /**
     * creates the first line (header) for the detailed lane queue output CSV file for one road, it constructs a header
     * string that includes the step and lane information based on the number of lanes in the given record.
     *
     * @param numberOfLanes The number of lanes for the road to create the header for
     * @param csvSeparator The separator to use in the CSV file
     * @return A string representing the first line (header) for the detailed lane queue output CSV file for one road.
     **/
    private String createFirstLineForDetailedExportSeparateFile(int numberOfLanes, String csvSeparator) {
        // write header with lane numbers
        StringBuilder firstLine = new StringBuilder("Step" + csvSeparator);
        for (int lane = 0; lane < numberOfLanes; lane++) {
            firstLine.append("Lane ").append(lane).append(csvSeparator);
        }

        return firstLine.toString().trim() + "\n";
    }

    /**
     * writes the light plan of all roads to separate CSV files (and folders), it creates a directory for each road and
     * writes the light plan data for that road to a separate file using the writeLightPlanOfOneRoad method.
     *
     * @param outputDetails The OutputDetails object containing the settings for output generation.
     * @param stepCount The total number of steps in the simulation.
     **/
    private void processLightPlanOfAllRoadsSeparateFiles(OutputDetails outputDetails, int stepCount) {
        String[] baseFileNameParts = this.fileName.split("\\.(?=[^.]+$)");
        String baseDirPath = baseFileNameParts[0] + "DetailedExport"; // folder for light plan outputs

        for (int i = 0; i < stoppedCarsOnRoadRecord.length; i++) {
            // create separate directory for each road
            String roadDirPath = baseDirPath + File.separator + "road" + i;
            if (!createFolderForRoadDetailedOutputs(roadDirPath)) {
                MyLogger.log("Skipping light plan output for road " + i + " due to directory creation failure.",
                        Constants.ERROR_FOR_LOGGING);
                continue; // Skip this road if we couldn't create the directory
            }

            String filePath = roadDirPath + File.separator + "LightPlanOfAllRoads.csv";
            // give the record for each road and write it to a separate file
            writeLightPlanOfOneRoad(filePath, outputDetails.csvSeparator, stepCount, stoppedCarsOnRoadRecord[i]);
        }
    }

    /**
     * creates a folder for detailed outputs of road if it does not already exist, it checks if the specified directory
     * path exists and if not, it attempts to create the directory.
     *
     * @param roadDirPath The path of the road directory to create for detailed outputs.
     * @return true if the directory exists or was created successfully, false if there was an error creating the
     *         directory.
     **/
    private boolean createFolderForRoadDetailedOutputs(String roadDirPath) {
        File roadDir = new File(roadDirPath);
        if (!roadDir.exists()) {
            boolean success = roadDir.mkdirs();
            if (!success) {
                MyLogger.log("Failed to create directory: " + roadDirPath, Constants.ERROR_FOR_LOGGING);
                return false;
            }
        }

        return true;
    }

    /**
     * writes the light plan of one road to a CSV file, it creates a BufferedWriter to write the data to the specified
     * file path, it writes the header with lane numbers and then iterates through each time step and lane to write
     * whether the lane was on red or green light based on the stopped cars record.
     *
     * @param filePath The path of the file to write the light plan data to.
     * @param csvSeparator The separator to use in the CSV file.
     * @param stepCount The total number of steps in the simulation.
     * @param record The StoppedCarsOnRoadRecord containing the data for one road of stopped cars and light status for
     *               each lane and step.
     **/
    private void writeLightPlanOfOneRoad(String filePath, String csvSeparator, int stepCount, StoppedCarsOnRoadRecord record) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            int numberOfLanes = record.stoppedCarsPerStep.size();

            // write header with lane numbers
            StringBuilder firstLine = new StringBuilder("Step" + csvSeparator);
            for (int lane = 0; lane < numberOfLanes; lane++) {
                firstLine.append("Lane ").append(lane).append(csvSeparator);
            }
            bw.write(firstLine.toString().trim() + "\n");

            for (int step = 0; step < stepCount; step++) {
                StringBuilder line = new StringBuilder(step + csvSeparator);
                for (int lane = 0; lane < numberOfLanes; lane++) {
                    NumberOfStandingCars entry = record.getStoppedCarsAtStep(lane, step);

                    // write whether the lane was on red or green light based on the stopped cars record, if there is
                    // an entry for the given step and lane
                    if (entry != null) {
                        if (entry.onRed) {
                            line.append("Was red").append(csvSeparator);
                        } else {
                            line.append("Was green").append(csvSeparator);
                        }
                    } else {
                        line.append("Unknown").append(csvSeparator);
                    }
                }
                bw.write(line.toString().trim() + "\n");
            }
        } catch (IOException e) {
            MyLogger.log("Error writing light plan data to file: " + e.getMessage(), Constants.ERROR_FOR_LOGGING);
        }
    }

    /************************************
     * Class representing the record of stopped cars on a road during the simulation, it maintains a list (lanes)
     * of linked lists (steps) that store the number of standing cars and whether they were on red light for each lane
     * of the road at each time step (instance of NumberOfStandingCars class)
     *
     * @author Michael Hladky
     * @version 1.0
     ************************************/
    private static class StoppedCarsOnRoadRecord {

        /** A list of linked lists that store the number of standing cars and whether they were on red light for each
         * lane of the road at each time step. Each index in the outer list represents a lane, and each linked list
         * contains entries for each time step, where each entry is an instance of the NumberOfStandingCars class. **/
        List<LinkedList<NumberOfStandingCars>> stoppedCarsPerStep;

        /**
         * Constructor to initialize the stoppedCarsPerStep list based on the number of lanes on the road. It creates a
         * new ArrayList to hold the linked lists for each lane, and for each lane, it initializes a new LinkedList to
         * store the number of standing cars and red light status for each time step.
         *
         * @param numberOfLanes The number of lanes on the road
         */
        public StoppedCarsOnRoadRecord(int numberOfLanes) {
            stoppedCarsPerStep = new ArrayList<>(numberOfLanes);
            for (int i = 0; i < numberOfLanes; i++) {
                stoppedCarsPerStep.add(new LinkedList<>());
            }
        }

        /**
         * Records the number of stopped cars and whether they were on red light for a specific lane at a given time
         * step. It checks if the lane index is within bounds and then adds a new instance of NumberOfStandingCars to
         * the corresponding linked list for that lane in the stoppedCarsPerStep list.
         *
         * @param count The number of stopped cars to record.
         * @param onRed A boolean indicating whether the light was red at the end of the lane
         * @param lane The index of the lane where the cars are stopped.
         */
        public void recordStoppedCars(int count, boolean onRed, int lane) {
            if (lane < stoppedCarsPerStep.size()) {
                stoppedCarsPerStep.get(lane).add(new NumberOfStandingCars(count, onRed));
            }
        }

        /**
         * Retrieves the number of stopped cars and whether they were on red light for a specific lane at a given time
         * step. It checks if the lane index and step index are within bounds and returns the corresponding entry from
         * the linked list for that lane. If the lane or step index is out of bounds, it returns a default instance of
         * NumberOfStandingCars with count 0 and onRed false.
         *
         * @param lane The index of the lane to retrieve the data for.
         * @param step The index of the time step to retrieve the data for.
         * @return An instance of NumberOfStandingCars containing the count and red light status for the specified lane
         *         and time step, or a default instance if the indices are out of bounds.
         */
        public NumberOfStandingCars getStoppedCarsAtStep(int lane, int step) {
            if (lane < stoppedCarsPerStep.size()) {
                LinkedList<NumberOfStandingCars> stoppedCarsList = stoppedCarsPerStep.get(lane);
                if (step < stoppedCarsList.size()) {
                    return stoppedCarsList.get(step);
                }
            }
            return new NumberOfStandingCars(0, false); // Return default if lane or step is out of bounds
        }

        /**
         * Resets the recorded stopped cars data by clearing the lists for each lane. It iterates through the
         * stoppedCarsPerStep list and calls the clear method on each LinkedList to remove all recorded entries of
         * stopped cars for each lane.
         **/
        public void reset() {
            for (LinkedList<NumberOfStandingCars> laneList : stoppedCarsPerStep) {
                laneList.clear();
            }
        }

        /**
         * Calculates the average number of stopped cars across all lanes and time steps, considering only those entries
         * where the cars were stopped at a red light. It iterates through the stoppedCarsPerStep list, sums up the
         * counts of stopped cars for entries where onRed is true, and counts the number of such entries to calculate
         * the average.
         *
         * @return The average number of stopped cars at red lights across all lanes and time steps. If there are no
         *         entries with onRed true, it returns 0.0 to avoid division by zero.
         */
        public double getAverageStoppedCars() {
            int totalCount = 0;
            int totalEntries = 0;

            for (LinkedList<NumberOfStandingCars> laneList : stoppedCarsPerStep) {
                for (NumberOfStandingCars entry : laneList) {
                    if (entry.onRed) {
                        totalCount += entry.count;
                        totalEntries++;
                    }
                }
            }

            return totalEntries > 0 ? (double) totalCount / totalEntries : 0.0;
        }
    }

    /**********************************
     * Class representing the number of standing cars on lane at a given time step and whether the lane is on red light
     * or not on lane, this is used for recording the number of stopped cars on each lane of the road during the
     * simulation
     *
     * @author Michael Hladky
     * @version 1.0
     **********************************/
    private static class NumberOfStandingCars {

        /** The number of standing cars at a given time step. **/
        int count;

        /** A boolean indicating whether the cars are stopped at a red light (true) or not (false). **/
        boolean onRed;

        /**
         *  Constructor to initialize the number of standing cars and whether they are on red light.
         *
         * @param count The number of standing cars.
         * @param onRed A boolean indicating if the cars are stopped at a red light.
         **/
        public NumberOfStandingCars(int count, boolean onRed) {
            this.count = count;
            this.onRed = onRed;
        }
    }
}
