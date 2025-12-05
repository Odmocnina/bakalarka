package core.model.cellular;

import app.AppContext;
import core.model.CarParams;
import core.model.Direction;
import core.model.Orientation;
import core.model.Road;
import core.utils.*;

import java.util.HashMap;
import java.util.LinkedList;

/*****************************
 * CellularRoad class representing a road using cellular automaton model, extends Road class
 *
 * @author Michael Hladky
 * @version 1.0
 *******************************/
public class CellularRoad extends Road {

    /** 2D array representing lanes and positions **/
    private Cell[][] cells;

    /** number of cells in straight lane (length of the road in cells **/
    private int numberOfCells;

    /** size of each cell in meters, used to translate length given in config to length in cells **/
    private double cellSize;

    private int speedLimitInCells;

    /**
     * Constructor for CellularRoad, creates the road and initializes cells, and other parameters, like cell size
     *
     * @param length length of the road in meters
     * @param numberOfLanes number of lanes on the road
     * @param speedLimit speed limit on the road in m/s
     * @param cellSize size of each cell in meters
     **/
    public CellularRoad(double length, int numberOfLanes, double speedLimit, double cellSize) {
        super(length, numberOfLanes, speedLimit, Constants.CELLULAR);
        this.cellSize = cellSize;
        this.speedLimitInCells = (int) Math.ceil(speedLimit / cellSize);
        createRoad();
    }

    /**
     * Creates the road by initializing the cells array based on the length and number of lanes and length
     **/
    private void createRoad() {
        this.numberOfCells = (int) Math.ceil(length / this.cellSize);
        cells = new Cell[numberOfLanes][numberOfCells];
        for (int lane = 0; lane < numberOfLanes; lane++) {
            for (int position = 0; position < numberOfCells; position++) {
                cells[lane][position] = new Cell();
            }
        }

        // first test of occupied cells, DELETE LATER
       /* cells[0][5].setOccupied(true);
        CarParams carParams = new CarParams();
        carParams.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0);
        carParams.setParameter(RequestConstants.MAX_SPEED_REQUEST, 1.0);
        carParams.xPosition = 5;
        carParams.lane = 0;
        carParams.setParameter(RequestConstants.LENGTH_REQUEST, 1);
        carParams.color = Constants.CAR_COLORS[0];
        cells[0][5].setCarParams(carParams);
        cells[0][5].setHead(true);

        cells[0][0].setOccupied(true);
        cells[0][1].setOccupied(true);
        carParams = new CarParams();
        carParams.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0);
        carParams.setParameter(RequestConstants.MAX_SPEED_REQUEST, 5.0);
        carParams.xPosition = 1;
        carParams.lane = 0;
        carParams.setParameter(RequestConstants.LENGTH_REQUEST, 2);
        carParams.color = Constants.CAR_COLORS[0];
        cells[0][1].setCarParams(carParams);
        cells[0][1].setHead(true);*/

    }

    /**
     * function to check if it is ok to put a car at the beginning of the lane, i.e., if there is enough space for the
     * car, if it isn't blocked by other cars already on the road
     *
     * @param car CarParams of the car to be placed
     * @param lane lane number where the car is to be placed
     * @return true if it is ok to place the car, false otherwise
     **/
    @Override
    protected boolean okToPutCarAtStart(CarParams car, int lane) {
        for (int i = 0; i <= car.getParameter(RequestConstants.LENGTH_REQUEST) + 1; i++) {
            if (i >= numberOfCells || cells[lane][i].isOccupied()) {
                return false;

            }
        }

        return true;
    }

    /**
     * function to find all cars that want to change lanes and return a list of lane change results, cars that were
     * marked to change lanes are not changed yet, that is done in a separate function to avoid conflicts
     *
     * @return LinkedList of LaneChangeResult objects representing cars that want to change lanes
     **/
    private LinkedList<LaneChangeResult> findLaneChanges() {
        LinkedList<LaneChangeResult> changedCars = new LinkedList<>();

        for (int position = this.numberOfCells - 1; position >= 0; position--) {
            for (int lane = numberOfLanes - 1; lane >= 0; lane--) {
                if (cells[lane][position].isOccupied() && cells[lane][position].isHead()) {
                    Direction laneChangeResult = attemptLaneChange(cells[lane][position]);
                    if (laneChangeResult != Direction.STRAIGHT) {
                        LaneChangeResult lcr = new LaneChangeResult(laneChangeResult, cells[lane][position].getCarParams());
                        changedCars.add(lcr);
                    }
                }
            }
        }

        return changedCars;
    }

    /**
     * function to process lane changes for cars that were marked to change lanes, actually changing their lanes
     *
     * @param changedCars LinkedList of LaneChangeResult objects representing cars that want to change lanes
     **/
    private void processLaneChanges(LinkedList<LaneChangeResult> changedCars) {
        for (LaneChangeResult lcr : changedCars) {
            Direction direction = lcr.direction;
            CarParams carParams = lcr.carParams;
            if (direction == Direction.LEFT && AppContext.SIMULATION.getStepCount() % 2 == 0) {
                int currentLane = carParams.lane;
                if (currentLane > 0) {
                    int targetLane = currentLane - 1;
                    carParams.lane = targetLane;
                    this.placeCar(carParams, (int) carParams.xPosition, targetLane);
                    this.removeCar(currentLane, (int) carParams.xPosition);
                }
            } else if (direction == Direction.RIGHT && AppContext.SIMULATION.getStepCount() % 2 == 1) {
                int currentLane = carParams.lane;
                if (currentLane < numberOfLanes - 1) {
                    int targetLane = currentLane + 1;
                    carParams.lane = targetLane;
                    this.placeCar(carParams, (int) carParams.xPosition, targetLane);
                    this.removeCar(currentLane, (int) carParams.xPosition);
                }
            }
        }
    }

    /**
     * function to perform lane change step, finding cars that want to change lanes and processing their lane changes
     **/
    private void laneChangeStep() {
        LinkedList<LaneChangeResult> changedCars = findLaneChanges();
        this.processLaneChanges(changedCars);
    }

    /**
     * function to perform forward step, updating the positions of all cars on the road-based on their speeds
     *
     * @return number of cars that have passed the end of the road
     **/
    private int forwardStep() {
        int carsPassed = 0;
        for (int position = this.numberOfCells - 1; position >= 0; position--) {
            for (int lane = numberOfLanes - 1; lane >= 0; lane--) {
                if (cells[lane][position].isOccupied() && cells[lane][position].isHead()) {
                    String requestParameters = AppContext.CAR_FOLLOWING_MODEL.requestParameters();
                    HashMap<String, Double> parameters = getParameters(lane, position, requestParameters);

                    if (parameters == null) { // this would be very fucked up if it happened
                        MyLogger.log("Error getting parameters for car at lane " + lane + ", position "
                                        + position, Constants.ERROR_FOR_LOGGING);
                        continue;
                    }

                    double newSpeed = AppContext.CAR_FOLLOWING_MODEL.getNewSpeed(parameters);

                    if (newSpeed > this.speedLimitInCells) {
                        newSpeed = this.speedLimitInCells;
                    }

                    if (isCarAtEnd(cells[lane][position].getCarParams(), (int) newSpeed)) {
                        if (checkIfCarStillRelevant(cells[lane][position].getCarParams(), (int) newSpeed)) {
                            moveCarHead(cells[lane][position].getCarParams(), (int) newSpeed);
                        } else {
                            carsPassed++;
                        }
                        //removeCar(lane, position); //easier and probably better solution, if car touches the end
                        //delete it whole but im a retard and keep trying to make that only
                        //part that is outside will delete, viz voodoo at top, fucking thing
                    } else {
                        cells[lane][position].getCarParams().setParameter(RequestConstants.CURRENT_SPEED_REQUEST
                                , newSpeed);
                        this.moveCar(cells[lane][position]);
                    }
                }
            }
        }

        return carsPassed;
    }

    /**
     * update position of the cars on road
     *
     * @return int number of cars that have passed the entire road
     **/
    @Override
    public int updateRoad() {

        if (AppContext.RUN_DETAILS.laneChange) {
            this.laneChangeStep();
        }

        int carsPassed = this.forwardStep();

        if (true)
            super.tryToAddCar(); // Attempt to add a new car at the beginning of each lane
        return carsPassed;
    }

    /**
     * move car form old position to new position depending on its current speed
     *
     * @param cell Cell representing the head of the car to be moved, head contains the CarParams, including speed,
     *             length and other parameters needed for movement
     **/
    private void moveCar(Cell cell) {
        if (cell == null || !cell.isOccupied() || !cell.isHead()) {
            MyLogger.log("Cannot move car: cell is null, unoccupied, or not head", Constants.DEBUG_FOR_LOGGING);
            return;
        }
        cell.setOccupied(false);
        cell.setHead(false);
        CarParams carParams = cell.getCarParams();
        int oldX = (int) carParams.xPosition;
        int currentSpeed = (int) carParams.getParameter(RequestConstants.CURRENT_SPEED_REQUEST);
        MyLogger.log("Moving car from position " + oldX + " to " + (oldX + currentSpeed) +
                " with speed " + currentSpeed, Constants.DEBUG_FOR_LOGGING);
        Cell newCellOfHead = cells[carParams.lane][(int) (carParams.xPosition + currentSpeed)];
        carParams.xPosition = carParams.xPosition + currentSpeed; // update position of the car head
        newCellOfHead.setOccupied(true); // occupy the new cell of the car head
        newCellOfHead.setHead(true);
        newCellOfHead.setCarParams(carParams);
        for (int i = 1; i < carParams.getParameter(RequestConstants.LENGTH_REQUEST); i++) { // move the rest of the car
            if (oldX - i >= 0) {
                // clear the old cells behind the car
                cells[carParams.lane][oldX - i].setOccupied(false);
                cells[carParams.lane][oldX - i].setCarParams(null);
                // occupy the new cells behind the car
                if ((int) carParams.xPosition - i >= 0) {
                    cells[carParams.lane][(int) carParams.xPosition - i].setOccupied(true);
                    cells[carParams.lane][(int) carParams.xPosition - i].setCarParams(carParams);
                }
            }
        }
    }

    /**
     * function to get all requested parameters for a car at a given lane and position
     *
     * @param lane lane number of the car
     * @param position position of the car on the road
     * @param requestedParameters String of requested parameters separated by Constants.REQUEST_SEPARATOR
     * @return HashMap of requested parameters with their values
     **/
    private HashMap<String, Double> getParameters(int lane, int position, String requestedParameters) {
        HashMap<String, Double> parameters = new HashMap<>();
        String[] params = requestedParameters.split(RequestConstants.REQUEST_SEPARATOR);
        if (params.length == 0) {
            MyLogger.log("No parameters requested", Constants.DEBUG_FOR_LOGGING);
            return null;
        }
        String[] carGeneratedParams = super.generators[lane].getCarGenerationParameters(); //parameters that car has
        String[] roadSimulationParams = {RequestConstants.TIME_STEP_REQUEST, RequestConstants.MAX_ROAD_SPEED_REQUEST};
        // parameters that road has

        CarParams car = this.cells[lane][position].getCarParams();
        for (String param : params) {
            if (StringEditor.isInArray(carGeneratedParams, param) || param.equals(RequestConstants.X_POSITION_REQUEST)
                    || param.equals(RequestConstants.CURRENT_SPEED_REQUEST)) { // get parameters from car that is being
                parameters.put(param, car.getParameter(param));                // inspected
            } else if (StringEditor.isInArray(roadSimulationParams, param)) {  //get from road/simulation
                super.getRoadSimulationParameter(parameters, param, car);
            } else {                                                           // get parameters about different car in
                this.getParametersAboutDifferentCar(parameters, param, car);   // proximity of the inspected car
            }
        }
        return parameters;
    }

    /**
     * gets parameter about different car in proximity of car for witch are we using model
     *
     * @param parameters hashmap to put parameter into
     * @param param parameter to get
     * @param car car for which we are getting parameter about different car
     **/
    private void getParametersAboutDifferentCar(HashMap<String, Double> parameters, String param, CarParams car) {
        String[] paramSeparate = param.split(RequestConstants.SUBREQUEST_SEPARATOR);
        String wantedParam = paramSeparate[0];
        Direction direction = Direction.valueOf(paramSeparate[1]);
        Orientation orientation = Orientation.valueOf(paramSeparate[2]);

        CarParams otherCar = getCarInProximity(direction, orientation, car);

        if (otherCar != null) {
            parameters.put(param, otherCar.getParameter(wantedParam));
        } else {
            if (car.lane == 0 && direction == Direction.LEFT) {
                parameters.put(param, (double) Constants.NO_LANE_THERE);
                return;
            }
            if (car.lane == this.numberOfLanes - 1 && direction == Direction.RIGHT) {
                parameters.put(param, (double) Constants.NO_LANE_THERE);
                return;
            }
            parameters.put(param, Constants.NO_CAR_THERE);
        }
    }

    /**
     * function to get the car in proximity based on direction and orientation
     *
     * @param direction Direction to look for the car (STRAIGHT, LEFT, RIGHT)
     * @param orientation Orientation to look for the car (FORWARD, BACKWARD)
     * @param car CarParams of the car for which we are looking for another car in proximity
     * @return CarParams of the car in proximity, or null if no car is found
     **/
    private CarParams getCarInProximity(Direction direction, Orientation orientation, CarParams car) {
        int lane = car.lane;
        int position = (int) car.xPosition;

        if (direction == Direction.STRAIGHT) {
            if (orientation == Orientation.FORWARD) {
                if (position < this.numberOfCells - 1) {
                    return this.getNextCarInLane(lane, position);
                } else {
                    return null;
                }
            } else {
                if (position > 0) {
                    return this.getPreviousCarInLane(lane, position);
                } else {
                    return null;
                }
            }
        } else if (direction == Direction.LEFT) {
            if (lane == 0) {
                return null;
            }

            if (orientation == Orientation.FORWARD) {
                return this.getNextCarInLane(lane - 1, position);
            } else {
                return this.getPreviousCarInLane(lane - 1, position);
            }
        } else if (direction == Direction.RIGHT) {
            if (lane == numberOfLanes - 1) {
                return null;
            }

            if (orientation == Orientation.FORWARD) {
                return this.getNextCarInLane(lane + 1, position);
            } else {
                return this.getPreviousCarInLane(lane + 1, position);
            }
        }

        return null;

    }

    /**
     * function to get the next car in the lane ahead of the given position
     *
     * @param lane lane number to search in
     * @param position position to start searching from
     * @return CarParams of the next car ahead, or null if no car is found
     **/
    private CarParams getNextCarInLane(int lane, int position) {
        for (int pos = position + 1; pos < this.numberOfCells; pos++) {
            if (cells[lane][pos].isOccupied() && cells[lane][pos].isHead()) {
                return cells[lane][pos].getCarParams();
            }
        }
        return null;
    }

    /**
     * function to get the previous car in the lane behind the given position
     *
     * @param lane lane number to search in
     * @param position position to start searching from
     * @return CarParams of the previous car behind, or null if no car is found
     **/
    private CarParams getPreviousCarInLane(int lane, int position) {
        for (int pos = position - 1; pos >= 0; pos--) {
            if (cells[lane][pos].isOccupied() && cells[lane][pos].isHead()) {
                return cells[lane][pos].getCarParams();
            }
        }
        return null;
    }

    /**
     * debug function to print the road state to console, occupied cells are represented by 'X', unoccupied by 'O'
     **/
    public void debugPrintRoad() {
        for (int lane = 0; lane < numberOfLanes; lane++) {
            for (int position = 0; position < this.numberOfCells; position++) {
                System.out.print(cells[lane][position].isOccupied() ? "X" : "O");
            }
            System.out.println();
        }
    }

    /**
     * function to remove a car from the road at a specified position and lane
     *
     * @param lane lane number where the car is to be removed
     * @param position position on the road where the head of the car is located
     **/
    private void removeCar(int lane, int position) {
        if (lane < 0 || lane >= numberOfLanes || position < 0 || position >= numberOfCells) {
            MyLogger.log("Invalid lane or position to remove car", Constants.DEBUG_FOR_LOGGING);
            return;
        }
        if (!cells[lane][position].isOccupied() || !cells[lane][position].isHead()) {
            MyLogger.log("No car head at the specified position to remove", Constants.DEBUG_FOR_LOGGING);
            return;
        }
        CarParams carParams = cells[lane][position].getCarParams();
        for (int i = 0; i < carParams.getParameter(RequestConstants.LENGTH_REQUEST); i++) {
            int posToClear = (int) (carParams.xPosition - i);
            if (posToClear >= 0 && posToClear < numberOfCells) {
                cells[lane][posToClear].setOccupied(false);
                cells[lane][posToClear].setHead(false);
                cells[lane][posToClear].setCarParams(null);
            }
        }
    }

    /**
     * function to get the content of the road, i.e., the cells array
     *
     * @return 2D array of Cell objects representing the road content
     **/
    @Override
    public Cell[][] getContent() {
        return cells;
    }

    /**
     * function to place a car on the road at the start of the lane
     *
     * @param car CarParams of the car to be placed
     * @param length length of the car in cells (used to determine position of head and -1 because of zero indexing
     *               shinanigans)
     * @param lane lane number where the car is to be placed
     **/
    @Override
    protected void placeCarAtStart(CarParams car, double length, int lane) {
        placeCar(car, (int) (length - 1), lane);
    }

    /**
     * function to place a car on the road at a specified position and lane
     *
     * @param car CarParams of the car to be placed
     * @param x position on the road where the head of the car is to be placed
     * @param lane lane number where the car is to be placed
     **/
    private void placeCar(CarParams car, int x, int lane) {
        if (lane < 0 || lane >= numberOfLanes || x < 0 || x >= numberOfCells) {
            MyLogger.log("Invalid lane or position to place car", Constants.DEBUG_FOR_LOGGING);
            return;
        }
        for (int i = 0; i < car.getParameter(RequestConstants.LENGTH_REQUEST); i++) {
            int posToOccupy = x - i;
            if (posToOccupy >= 0 && posToOccupy < numberOfCells) {
                cells[lane][posToOccupy].setOccupied(true);
                cells[lane][posToOccupy].setCarParams(car);
                if (i == 0) {
                    cells[lane][posToOccupy].setHead(true);
                    car.xPosition = x;
                    car.lane = lane;
                } else {
                    cells[lane][posToOccupy].setHead(false);
                }
            } else {
                MyLogger.log("Car length exceeds road boundaries or is negative during placing car",
                        Constants.DEBUG_FOR_LOGGING);
            }
        }
    }

    /**
     * function to check if a car is still relevant on the road, i.e., if it has not completely passed the end of the
     * road
     *
     * @param car CarParams of the car to be checked
     * @param newSpeed new speed of the car
     * @return true if the car is still relevant, false otherwise
     **/
    private boolean checkIfCarStillRelevant(CarParams car, int newSpeed) {
        if (car == null) {
            return false;
        }

        if ((car.xPosition + newSpeed - car.getParameter(RequestConstants.LENGTH_REQUEST) + 1) >= this.numberOfCells) { // fuck zero base indexing
            removeCar(car.lane, (int) car.xPosition);
            return false;
        }

        return true;
    }

    /**
     * Function to check if car is over or is touching the end of the road
     *
     * @param car car that we are checking
     * @param newSpeed speed of the car (that puts it outside the road)
     **/
    private boolean isCarAtEnd(CarParams car, int newSpeed) {
        if (car == null) { // sanity check
            return false;
        }

        return (car.xPosition + newSpeed) >= this.numberOfCells;
    }

    /**
     * moves car head to the body cell in case it would be out of the road but the rest of the cars body would still be
     * on the road
     *
     * @param car cars which head is supposed to be moved
     * @param newSpeed int value of speed which moved head of the car out of the road
     **/
    private void moveCarHead(CarParams car, int newSpeed) {
        if (car == null) {
            return;
        }

        int oldX = (int) car.xPosition;
        int howMuchOverflow = (int) (car.xPosition + newSpeed - this.numberOfCells + 1);

        for (int i = 0; i < howMuchOverflow; i++) {
            int posToClear = oldX - i;
            if (posToClear >= 0 && posToClear < this.numberOfCells) {
                cells[car.lane][posToClear].setOccupied(false);
                cells[car.lane][posToClear].setHead(false);
                cells[car.lane][posToClear].setCarParams(null);
            }
        }

        int newHeadX = oldX - howMuchOverflow;
        car.setParameter(RequestConstants.LENGTH_REQUEST, car.getParameter(RequestConstants.LENGTH_REQUEST)
                - howMuchOverflow);

        cells[car.lane][newHeadX].setOccupied(true);
        cells[car.lane][newHeadX].setHead(true);
        cells[car.lane][newHeadX].setCarParams(car);
        car.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, newSpeed);
        car.xPosition = newHeadX;

        MyLogger.log("Old head position: " + oldX, Constants.DEBUG_FOR_LOGGING);
        MyLogger.log("Car at lane " + car.lane + " reached the end of the road and is partially removed.",
                Constants.DEBUG_FOR_LOGGING);
        MyLogger.log("New head position: " + newHeadX + ", New length: " +
                car.getParameter(RequestConstants.LENGTH_REQUEST) + ", Current speed: " +
                car.getParameter(RequestConstants.CURRENT_SPEED_REQUEST), Constants.DEBUG_FOR_LOGGING);
        this.moveCar(cells[car.lane][newHeadX]);
    }

    /**
     * function to attempt a lane change for a car in a given cell
     *
     * @param cell Cell representing the head of the car attempting to change lanes
     * @return Direction enum representing the desired direction of lane change (LEFT, RIGHT, STRAIGHT)
     **/
    private Direction attemptLaneChange(Cell cell) {
        String requestParameters = AppContext.LANE_CHANGING_MODEL.requestParameters();
        HashMap<String, Double> parameters = getParameters(cell.getCarParams().lane,
                (int) cell.getCarParams().xPosition, requestParameters);
        if (parameters == null) {
            MyLogger.log("Error getting parameters for lane change for car at lane " +
                    cell.getCarParams().lane + ", position " + (int) cell.getCarParams().xPosition,
                    Constants.ERROR_FOR_LOGGING);
            return null;
        }

        Direction desiredDirection = AppContext.LANE_CHANGING_MODEL.changeLaneIfDesired(parameters);

        if (desiredDirection == Direction.LEFT) {
            int currentLane = cell.getCarParams().lane;
            if (currentLane > 0) {
                return Direction.LEFT;
            }
        } else if (desiredDirection == Direction.RIGHT) {
            int currentLane = cell.getCarParams().lane;
            if (currentLane < numberOfLanes - 1) {
                return Direction.RIGHT;
            }
        }

        return Direction.STRAIGHT;
    }

    private int resolveCollisions(CarParams car, int newSpeed) {
        int lane = car.lane;
        int oldX = (int) car.xPosition;
        int newX = oldX + newSpeed;
        for (int pos = oldX + 1; pos <= newX; pos++) {
            if (pos >= numberOfCells) {
                break; // beyond road end
            }
            if (cells[lane][pos].isOccupied()) {
                ResultsRecorder.getResultsRecorder().addCollision();
                if (AppContext.RUN_DETAILS.preventCollisions) {
                    return pos - 1 - oldX; // return distance to the cell before collision
                }
            }
        }

        return newSpeed;
    }

    /**
     * Function to get the number of cars currently on the road
     *
     * @return number of cars on the road
     **/
    @Override
    public int getNumberOfCarsOnRoad() {
        int carCount = 0;
        for (int lane = 0; lane < numberOfLanes; lane++) {
            for (int position = 0; position < this.numberOfCells; position++) {
                if (cells[lane][position].isOccupied() && cells[lane][position].isHead()) {
                    carCount++;
                }
            }
        }
        return carCount;
    }

    /**
     * Function to get the length of the road in cells
     *
     * @return length of the road in cells
     **/
    public double getLengthInCells() {
        return this.numberOfCells;
    }

    /***
     * Function to get the size of each cell in meters
     *
     * @return size of each cell in meters
     **/
    public double getCellSize() {
        return this.cellSize;
    }

    /*******************************
     * Class to represent the result of a lane change attempt, including the direction and car parameters
     *
     * @author Michael Hladky
     * @version 1.0
     *******************************/
    private static class LaneChangeResult {

        /** direction of the lane change **/
        public Direction direction;

        /** car parameters of the car attempting to change lanes **/
        public CarParams carParams;

        /**
         * Constructor for LaneChangeResult
         *
         * @param direction direction of the lane change
         * @param carParams car parameters of the car attempting to change lanes
         **/
        public LaneChangeResult(Direction direction, CarParams carParams) {
            this.direction = direction;
            this.carParams = carParams;
        }
    }
}
