package core.model.cellular;

import app.AppContext;
import core.model.CarParams;
import core.model.Direction;
import core.model.CarGenerator;
import core.model.Road;
import core.utils.Constants;

import java.util.HashMap;
import java.util.LinkedList;

import core.utils.StringEditor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class CellularRoad extends Road {
    private static final Logger logger = LogManager.getLogger(CellularRoad.class);
    private Cell[][] cells; // 2D array representing lanes and positions

    private int numberOfCells;
    private int cellSize; // size of each cell in meters
    public CellularRoad(double length, int numberOfLanes, double speedLimit) {
        super(length, numberOfLanes, speedLimit, Constants.CELLULAR);
        createRoad();
    }
    private void createRoad() {
        this.numberOfCells = (int) Math.ceil(length / AppContext.cellSize); // to-do load cell size from config
        cells = new Cell[numberOfLanes][numberOfCells];
        for (int lane = 0; lane < numberOfLanes; lane++) {
            for (int position = 0; position < numberOfCells; position++) {
                cells[lane][position] = new Cell();
            }
        }

        // first test of occupied cells, DELETE LATER
        cells[0][5].setOccupied(true);
        CarParams carParams = new CarParams();
        /*carParams.currentSpeed = 0;
        carParams.maxSpeed = 3;
        carParams.xPosition = 5;
        carParams.lane = 0;
        carParams.length = 1;*/
        carParams.setParameter(Constants.CURRENT_SPEED_REQUEST, 0);
        carParams.setParameter(Constants.MAX_SPEED_REQUEST, 3.0);
        carParams.xPosition = 5;
        carParams.lane = 0;
        carParams.setParameter(Constants.LENGTH_REQUEST, 1);
        carParams.color = Constants.CAR_COLORS[0];
        cells[0][5].setCarParams(carParams);
        cells[0][5].setHead(true);

        cells[0][0].setOccupied(true);
        cells[0][1].setOccupied(true);
        carParams = new CarParams();
        /*carParams.currentSpeed = 0;
        carParams.maxSpeed = 5;
        carParams.xPosition = 1;
        carParams.lane = 0;
        carParams.length = 2;*/
        carParams.setParameter(Constants.CURRENT_SPEED_REQUEST, 0);
        carParams.setParameter(Constants.MAX_SPEED_REQUEST, 5.0);
        carParams.xPosition = 1;
        carParams.lane = 0;
        carParams.setParameter(Constants.LENGTH_REQUEST, 2);
        carParams.color = Constants.CAR_COLORS[0];
        cells[0][1].setCarParams(carParams);
        cells[0][1].setHead(true);

    }

    private void tryToAddCar() {
        for (int lane = 0; lane < numberOfLanes; lane++) {
            if (super.generator.decideIfNewCar()) {
                CarParams newCar = super.generator.generateCar();
                for (int i = 0; i <= newCar.getParameter(Constants.LENGTH_REQUEST); i++) {
                    if (i >= numberOfCells || cells[lane][i].isOccupied()) {
                        newCar = null;
                        break;
                    }
                }
                if (newCar != null) {
                    placeCar(newCar, (int) newCar.getParameter(Constants.LENGTH_REQUEST), lane);
                }
            }
        }
    }



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

    private void processLaneChanges(LinkedList<LaneChangeResult> changedCars) {
        for (LaneChangeResult lcr : changedCars) {
            Direction direction = lcr.direction;
            CarParams carParams = lcr.carParams;
            if (direction == Direction.LEFT) {
                int currentLane = carParams.lane;
                if (currentLane > 0) {
                    int targetLane = currentLane - 1;
                    carParams.lane = targetLane;
                    this.placeCar(carParams, (int) carParams.xPosition, targetLane);
                    this.removeCar(currentLane, (int) carParams.xPosition);
                }
            } else if (direction == Direction.RIGHT) {
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

    private void laneChangeStep() {
        LinkedList<LaneChangeResult> changedCars = findLaneChanges();
        this.processLaneChanges(changedCars);
    }

    private int forwardStep() {
        int carsPassed = 0;
        for (int position = this.numberOfCells - 1; position >= 0; position--) {
            for (int lane = numberOfLanes - 1; lane >= 0; lane--) {
                if (cells[lane][position].isOccupied() && cells[lane][position].isHead()) {
                    //Cell cell = cells[lane][position];

                    String requestParameters = AppContext.CAR_FOLLOWING_MODEL.requestParameters();
                    HashMap<String, Double> parameters = getParameters(lane, position, requestParameters);
                    if (parameters == null) {
                        logger.debug("Error getting parameters for car at lane " + lane + ", position " + position);
                        continue;
                    }

                    double newSpeed = AppContext.CAR_FOLLOWING_MODEL.getNewSpeed(parameters);

                    if (isCarAtEnd(cells[lane][position].getCarParams(), (int) newSpeed)) {
                        if (checkIfCarStillRelevant(cells[lane][position].getCarParams(), (int) newSpeed)) {
                            moveCarHead(cells[lane][position].getCarParams(), (int) newSpeed);
                        } else {
//                            if (AppContext.RUN_DETAILS.writingResults()) {
//                                ResultsRecorder.getResultsRecorder().recordCarPassed();
//                            }
                            carsPassed++;
                        }
                        //removeCar(lane, position); //easier and probably better solution, if car touches the end
                        //delete it whole but im a retard and keep trying to make that only
                        //part that is outside will delete, viz voodo at top, fucking thing
                    } else {
                        cells[lane][position].getCarParams().setParameter(Constants.CURRENT_SPEED_REQUEST, newSpeed);
                        this.moveCar(cells[lane][position]);
                    }
                }
            }
        }

        return carsPassed;
    }


    @Override
    public int updateRoad() {
        // Attempt to add a new car at the beginning of each lane
        if (true)
        this.tryToAddCar();

        this.laneChangeStep();
        int carsPassed = this.forwardStep();
        return carsPassed;
    }

    private void resetIsProcessed() {
        for (int position = this.numberOfCells - 1; position >= 0; position--) {
            for (int lane = numberOfLanes - 1; lane >= 0; lane--) {
                if (cells[lane][position].isOccupied() && cells[lane][position].isHead()
                        && cells[lane][position].getCarParams().isProcessed) {
                    cells[lane][position].getCarParams().isProcessed = false;
                }
            }
        }
    }

    private void moveCar(Cell cell) {
        if (cell == null || !cell.isOccupied() || !cell.isHead()) {
            logger.debug("Cannot move car: cell is null, unoccupied, or not head");
            return;
        }
        cell.setOccupied(false);
        cell.setHead(false);
        CarParams carParams = cell.getCarParams();
        int oldX = (int) carParams.xPosition;
        int currentSpeed = (int) carParams.getParameter(Constants.CURRENT_SPEED_REQUEST);
        logger.debug("Moving car from position " + oldX + " to " + (oldX + currentSpeed) +
                " with speed " + currentSpeed);
        Cell newCellOfHead = cells[carParams.lane][(int) (carParams.xPosition + currentSpeed)];
        carParams.xPosition = carParams.xPosition + currentSpeed;
        newCellOfHead.setOccupied(true);
        newCellOfHead.setHead(true);
        newCellOfHead.setCarParams(carParams);
        for (int i = 1; i < carParams.getParameter(Constants.LENGTH_REQUEST); i++) {
            if (oldX - i >= 0) {
                // unoccupy the old cells behind the car
                cells[carParams.lane][oldX - i].setOccupied(false);
                cells[carParams.lane][oldX - i].setCarParams(null);
                // occupy the new cells behind the car
                if (newCellOfHead != null && (int) carParams.xPosition - i >= 0) {
                    cells[carParams.lane][(int) carParams.xPosition - i].setOccupied(true);
                    cells[carParams.lane][(int) carParams.xPosition - i].setCarParams(carParams);
                }
            }
        }
    }

    private HashMap<String, Double> getParameters(int lane, int position, String requestedParameters) {
        HashMap<String, Double> parameters = new HashMap<>();
        String[] params = requestedParameters.split(Constants.REQUEST_SEPARATOR);
        if (params.length <= 0) {
            logger.debug("No parameters requested");
            return null;
        }
        String[] carGeneratedParams = this.generator.getCarGenerationParameters();

        CarParams car = cells[lane][position].getCarParams();
        for (String param : params) {
            if (StringEditor.isInArray(carGeneratedParams, param)) {
                int i = 0;
                parameters.put(param, car.getParameter(param));
            } else {
                switch (param) { // getting parameters for model
                    case Constants.MAX_SPEED_REQUEST: // max speed of vehicle
                        parameters.put(Constants.MAX_SPEED_REQUEST, (double) cells[lane][position].getCarParams().
                                getParameter(Constants.MAX_SPEED_REQUEST));
                        break;

                    case Constants.CURRENT_SPEED_REQUEST:   // current speed of vehicle
                        parameters.put(Constants.CURRENT_SPEED_REQUEST,
                                (double) car.getParameter(Constants.CURRENT_SPEED_REQUEST));
                        break;

                    case Constants.DISTANCE_TO_NEXT_CAR_REQUEST:   // distance to next car in the same lane
                        int nextCarPos = getNextOcuppiedCell(lane, position, Direction.STRAIGHT);
                        if (nextCarPos == Constants.NO_CAR_IN_FRONT) {
                            parameters.put(Constants.DISTANCE_TO_NEXT_CAR_REQUEST,
                                    Double.MAX_VALUE);
                        } else {
                            parameters.put(Constants.DISTANCE_TO_NEXT_CAR_REQUEST,
                                    (double) (nextCarPos - position - 1));
                        }
                        break;

                    case Constants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST:   // distance to next car in the left lane
                        nextCarPos = getNextOcuppiedCell(lane, position, Direction.LEFT);
                        if (nextCarPos == Constants.NO_CAR_IN_FRONT) {
                            parameters.put(Constants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST,
                                    Double.MAX_VALUE);
                        } else if (nextCarPos == Constants.NO_LANE_THERE) {
                            parameters.put(Constants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST,
                                    Constants.PARAMETER_UNDEFINED);
                        } else {
                            parameters.put(Constants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST,
                                    (double) (nextCarPos - position - 1));
                        }
                        break;

                    case Constants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST:    // distance to next car in the right lane
                        nextCarPos = getNextOcuppiedCell(lane, position, Direction.RIGHT);
                        if (nextCarPos == Constants.NO_CAR_IN_FRONT) {
                            parameters.put(Constants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST,
                                    Double.MAX_VALUE);
                        } else if (nextCarPos == Constants.NO_LANE_THERE) {
                            parameters.put(Constants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST,
                                    Constants.PARAMETER_UNDEFINED);
                        } else {
                            parameters.put(Constants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST,
                                    (double) (nextCarPos - position - 1));
                        }
                        break;

                    case Constants.DISTANCE_TO_PREVIOUS_CAR_REQUEST: // distance to previous car in the same lane
                        int prevCarPos = getPreviousOccupiedCell(lane, position, Direction.STRAIGHT);
                        if (prevCarPos == Constants.NO_CAR_IN_FRONT) {
                            parameters.put(Constants.DISTANCE_TO_PREVIOUS_CAR_REQUEST,
                                    (double) (position + 1));
                        } else {
                            parameters.put(Constants.DISTANCE_TO_PREVIOUS_CAR_REQUEST,
                                    (double) (position - prevCarPos - 1));
                        }
                        break;

                    case Constants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST:  // distance to previous car in the left lane
                        prevCarPos = getPreviousOccupiedCell(lane, position, Direction.LEFT);
                        if (prevCarPos == Constants.NO_CAR_IN_FRONT) {
                            parameters.put(Constants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST,
                                    (double) (position + 1));
                        } else if (prevCarPos == Constants.NO_LANE_THERE) {
                            parameters.put(Constants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST,
                                    (double) Constants.NO_LANE_THERE);
                        } else {
                            parameters.put(Constants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST,
                                    (double) (position - prevCarPos - 1));
                        }
                        break;

                    case Constants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST: // distance to previous car in the right lane
                        prevCarPos = getPreviousOccupiedCell(lane, position, Direction.RIGHT);
                        if (prevCarPos == Constants.NO_CAR_IN_FRONT) {
                            parameters.put(Constants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST,
                                    (double) (position + 1));
                        } else if (prevCarPos == Constants.NO_LANE_THERE) {
                            parameters.put(Constants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST,
                                    (double) Constants.NO_LANE_THERE);
                        } else {
                            parameters.put(Constants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST,
                                    (double) (position - prevCarPos - 1));
                        }
                        break;

                    default:
                        logger.debug("Unknown parameter requested: " + param);
                }
            }
        }
        return parameters;
    }

    private int getPreviousOccupiedCell(int lane, int position, Direction direction) {
        if (direction == Direction.LEFT) {
            if (lane > 0) {
                lane = lane - 1;
            } else {
                return Constants.NO_LANE_THERE;
            }

        } else if (direction == Direction.RIGHT) {
            if (lane < numberOfLanes - 1) {
                lane = lane + 1;
            } else {
                return Constants.NO_LANE_THERE;
            }
        }
        for (int pos = position - 1; pos >= 0; pos--) {
            if (cells[lane][pos].isOccupied()) {
                return pos;
            }
        }
        return Constants.NO_CAR_IN_FRONT;
    }

    private int getNextOcuppiedCell(int lane, int position, Direction direction) {
        if (direction == Direction.LEFT) {
            if (lane > 0) {
                lane = lane - 1;
            } else {
                return Constants.NO_LANE_THERE;
            }

        } else if (direction == Direction.RIGHT) {
            if (lane < numberOfLanes - 1) {
                lane = lane + 1;
            } else {
                return Constants.NO_LANE_THERE;
            }
        }
        for (int pos = position + 1; pos < this.numberOfCells; pos++) {
            if (cells[lane][pos].isOccupied()) {
                return pos;
            }
        }
        return Constants.NO_CAR_IN_FRONT;
    }

    public void debugPrintRoad() {
        for (int lane = 0; lane < numberOfLanes; lane++) {
            for (int position = 0; position < this.numberOfCells; position++) {
                System.out.print(cells[lane][position].isOccupied() ? "X" : "O");
            }
            System.out.println();
        }
    }

    private void removeCar(int lane, int position) {
        if (lane < 0 || lane >= numberOfLanes || position < 0 || position >= numberOfCells) {
            logger.debug("Invalid lane or position to remove car");
            return;
        }
        if (!cells[lane][position].isOccupied() || !cells[lane][position].isHead()) {
            logger.debug("No car head at the specified position to remove");
            return;
        }
        CarParams carParams = cells[lane][position].getCarParams();
        for (int i = 0; i < carParams.getParameter(Constants.LENGTH_REQUEST); i++) {
            int posToClear = (int) (carParams.xPosition - i);
            if (posToClear >= 0 && posToClear < numberOfCells) {
                cells[lane][posToClear].setOccupied(false);
                cells[lane][posToClear].setHead(false);
                cells[lane][posToClear].setCarParams(null);
            }
        }
    }

    @Override
    public Cell[][] getContent() {
        return cells;
    }

    private void placeCar(CarParams car, int x, int lane) {
        if (lane < 0 || lane >= numberOfLanes || x < 0 || x >= numberOfCells) {
            logger.debug("Invalid lane or position to place car");
            return;
        }
        for (int i = 0; i < car.getParameter(Constants.LENGTH_REQUEST); i++) {
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
                logger.debug("Car length exceeds road boundaries or is negative during placing car");
            }
        }
    }

    private boolean checkIfCarStillRelevant(CarParams car, int newSpeed) {
        if (car == null) {
            return false;
        }

        if ((car.xPosition + newSpeed - car.getParameter(Constants.LENGTH_REQUEST) + 1) >= this.numberOfCells) { // fuck zero base indexing
            removeCar(car.lane, (int) car.xPosition);
            return false;
        }

        return true;
    }

    private boolean isCarAtEnd(CarParams car, int newSpeed) {
        if (car == null) {
            return false;
        }

        return (car.xPosition + newSpeed) >= this.numberOfCells;
    }

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
        car.setParameter(Constants.LENGTH_REQUEST, car.getParameter(Constants.LENGTH_REQUEST) - howMuchOverflow);

        cells[car.lane][newHeadX].setOccupied(true);
        cells[car.lane][newHeadX].setHead(true);
        cells[car.lane][newHeadX].setCarParams(car);
        car.setParameter(Constants.CURRENT_SPEED_REQUEST, newSpeed);
        car.xPosition = newHeadX;

        logger.debug("Old head position: " + oldX);
        logger.debug("Car at lane " + car.lane + " reached the end of the road and is partially removed.");
        logger.debug("New head position: " + newHeadX + ", New length: " + car.getParameter(Constants.LENGTH_REQUEST)
                + ", Current speed: " + car.getParameter(Constants.CURRENT_SPEED_REQUEST));
        this.moveCar(cells[car.lane][newHeadX]);
    }

    private Direction attemptLaneChange(Cell cell) {
        String requestParameters = AppContext.LANE_CHANGING_MODEL.requestParameters();
        HashMap<String, Double> parameters = getParameters(cell.getCarParams().lane,
                (int) cell.getCarParams().xPosition, requestParameters);
        if (parameters == null) {
            logger.debug("Error getting parameters for lane change for car at lane " +
                    cell.getCarParams().lane + ", position " + (int) cell.getCarParams().xPosition);
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

    public double getLengthInCells() {
        return this.numberOfCells;
    }

    private class LaneChangeResult {
        public Direction direction;
        public CarParams carParams;

        public LaneChangeResult(Direction direction, CarParams carParams) {
            this.direction = direction;
            this.carParams = carParams;
        }
    }
}
