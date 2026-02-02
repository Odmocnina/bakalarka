package core.utils;

import app.AppContext;
import core.model.Road;
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

    /** BigInt value used as a start of timer when simulation starts **/
    private BigInteger timeStart;

    /** BigInt value used as a stop of timer when simulation stop **/
    private BigInteger timeEnd;

    /** Output file name for writing results **/
    private String fileName;

    /** Output type (txt/csv)**/
    private String outputType = "txt";

    private int collisionsCount = 0;

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

        boolean queueUsed = AppContext.SIMULATION.getRoads()[0].generatingToQueue();
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
        }
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
        for (int i = 0; i < carsPassedPerRoad.length; i++) {
            bw.write("Road " + i + ": " + carsPassedPerRoad[i] + " cars passed.\n");
        }
        bw.write("\n");
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

    private void writeCarsOnTheRoad(BufferedWriter bw) throws IOException {
        bw.write("=== Cars Currently on the Road ===\n");
        Road[] roads = AppContext.SIMULATION.getRoads();
        for (int i = 0; i < roads.length; i++) {
            bw.write("Road " + i + " Cars: " + roads[i].getNumberOfCarsOnRoad() + "\n");
        }
        bw.write("\n");
    }

    private void writeCollisionsCount(BufferedWriter bw) throws IOException {
        bw.write("=== Collisions Count ===\n");
        bw.write("Total Collisions: " + this.collisionsCount + "\n\n");
    }

    private void writeTXT(BufferedWriter bw) throws IOException {
        // Implementation for writing results in TXT format
        bw.write("=== Traffic Simulation Results ===\n\n"); // header of the results file
        OutputDetails outputDetails = AppContext.RUN_DETAILS.outputDetails;
        if (outputDetails == null) {
            MyLogger.log("RunDetails is null. Cannot write simulation details.", Constants.ERROR_FOR_LOGGING);
            return;
        }
        if (outputDetails.writePart("simulationDetails")) {
            this.writeSimulationDetails(bw);
        }
        if (outputDetails.writePart("simulationTime")) {
            this.writeSimulationTimeResults(bw);
        }
        if (outputDetails.writePart("carsPassed")) {
            this.writeCarsPassedResults(bw);
        }
        if (outputDetails.writePart("carsOnRoad")) {
            this.writeCarsOnTheRoad(bw);
        }
        if (outputDetails.writePart("collisionCount")) {
            this.writeCollisionsCount(bw);
        }
        if (outputDetails.writePart("roadDetails")) {
            this.writeRoadDetails(bw);
        }
        if (outputDetails.writePart("generationDetails")) {
            this.writeGenerationParams(bw);
        }
    }

    private void writeCSV(BufferedWriter bw) throws IOException {
        // Implementation for writing results in CSV format
        OutputDetails outputDetails = AppContext.RUN_DETAILS.outputDetails;
        String csvSeparator = outputDetails.csvSeparator;
        String header = "";
        header = header + "Road Index" + csvSeparator;
        if (outputDetails.writePart("carsPassed")) {
            header = header + "Cars Passed" + csvSeparator;
        }
        if (outputDetails.writePart("carsOnRoad")) {
            header = header + "Cars on Road" + csvSeparator;
        }
        if (outputDetails.writePart("roadDetails")) {
            header = header + "Road details" + csvSeparator;
        }
        if (outputDetails.writePart("collisionCount")) {
            header = header + "Collisions Count" + csvSeparator;
        }
        if (outputDetails.writePart("generationDetails")) {
            header = header + "Generation Params";
        }
        header = header.trim();

        bw.write(header + "\n");
        for (int i = 0; i < carsPassedPerRoad.length; i++) {
            bw.write(i + csvSeparator);
            if (outputDetails.writePart("carsPassed")) {
                bw.write(carsPassedPerRoad[i] + csvSeparator);
            }
            if (outputDetails.writePart("carsOnRoad")) {
                bw.write(AppContext.SIMULATION.getRoads()[i].getNumberOfCarsOnRoad() + csvSeparator);
            }
            if (outputDetails.writePart("roadDetails")) {
                bw.write(AppContext.SIMULATION.getRoads()[i].toString() + csvSeparator);
            }
            if (outputDetails.writePart("collisionCount")) {
                bw.write(this.collisionsCount + csvSeparator);
            }
            if (outputDetails.writePart("generationDetails")) {
                bw.write(AppContext.SIMULATION.getRoads()[i].getCarGenerator().toString() + "\n");
            }
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

    public void addCollision() {
        this.collisionsCount++;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public void resetCarNumbers() {
        if (this.carsPassedPerRoad != null) {
            Arrays.fill(this.carsPassedPerRoad, 0);
        }

        this.collisionsCount = 0;
    }

}
