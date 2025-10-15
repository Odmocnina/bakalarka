package core.model.cellular;

import app.AppContext;
import core.model.CarParams;
import core.model.Direction;
import core.model.CarGenerator;
import core.model.Road;
import core.utils.Constants;

import java.util.HashMap;

public class CellularRoad extends Road {
    private Cell[][] cells; // 2D array representing lanes and positions

    private int numberOfCells;
    private int cellSize; // size of each cell in meters
    private CarGenerator generator;
    public CellularRoad(double length, int numberOfLanes, double speedLimit) {
        super(length, numberOfLanes, speedLimit);
        createRoad();
        this.generator = AppContext.CAR_GENERATOR;
        super.type = Constants.CELLULAR;
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
        cells[0][0].setOccupied(true);
        CarParams carParams = new CarParams();
        carParams.currentSpeed = 0;
        carParams.maxSpeed = 5;
        carParams.xPosition = 0;
        carParams.lane = 0;
        carParams.length = 1;
        carParams.color = Constants.CAR_COLORS[0];
        cells[0][0].setCarParams(carParams);
        cells[0][0].setHead(true);

        cells[0][3].setOccupied(true);
        cells[0][2].setOccupied(true);
        carParams = new CarParams();
        carParams.currentSpeed = 0;
        carParams.maxSpeed = 3;
        carParams.xPosition = 3;
        carParams.lane = 0;
        carParams.length = 2;
        carParams.color = Constants.CAR_COLORS[0];
        cells[0][3].setCarParams(carParams);
        cells[0][3].setHead(true);
    }

    @Override
    public void upadateRoad() {
        // Attempt to add a new car at the beginning of each lane
        for (int lane = 0; lane < numberOfLanes; lane++) {
            if (generator.decideIfNewCar()) {
                CarParams newCar = generator.generateCar();
                for (int i = 0; i <= newCar.length; i++) {
                    if (i >= numberOfCells || cells[lane][i].isOccupied()) {
                        newCar = null;
                        break;
                    }
                }
                if (newCar != null) {
                    placeCar(newCar, 0, lane);
                }
            }
        }
        for (int position = this.numberOfCells - 1; position >= 0; position--) {
            for (int lane = numberOfLanes - 1; lane >= 0; lane--) {
                if (cells[lane][position].isOccupied() && cells[lane][position].isHead()) {
                    String requestParameters = AppContext.CAR_FOLLOWING_MODEL.requestParameters();
                    HashMap<String, Double> parameters = getParameters(lane, position, requestParameters);
                    if (parameters == null) {
                        System.out.println("Error getting parameters for car at lane " + lane + ", position " + position);
                        continue;
                    }

                    double newSpeed = AppContext.CAR_FOLLOWING_MODEL.getNewSpeed(parameters);

                    if (isCarAtEnd(cells[lane][position].getCarParams(), (int) newSpeed)) {
                        if (checkIfCarStillRelevant(cells[lane][position].getCarParams(), (int) newSpeed)) {
                            moveCarHead(cells[lane][position].getCarParams(), (int) newSpeed);
                        }
                        //removeCar(lane, position); //easier and probably better solution, if car touches the end
                                                   //delete it whole but im a retard and keep trying to make that only
                                                   //part that is outside will delete, viz voodo at top, fucking thing
                    } else {
                        cells[lane][position].getCarParams().currentSpeed = (int) newSpeed;
                        this.moveCar(cells[lane][position]);
                    }
                }
            }
        }
    }

    private void moveCar(Cell cell) {
        if (cell == null || !cell.isOccupied() || !cell.isHead()) {
            System.out.println("Cannot move car: cell is null, unoccupied, or not head");
            return;
        }
        cell.setOccupied(false);
        cell.setHead(false);
        CarParams carParams = cell.getCarParams();
        int oldX = (int) carParams.xPosition;
        int currentSpeed = (int) carParams.currentSpeed;
        System.out.println("Moving car from position " + oldX + " to " + (oldX + currentSpeed) +
                " with speed " + currentSpeed);
        Cell newCellOfHead = cells[carParams.lane][(int) (carParams.xPosition + currentSpeed)];
        carParams.xPosition = carParams.xPosition + currentSpeed;
        newCellOfHead.setOccupied(true);
        newCellOfHead.setHead(true);
        newCellOfHead.setCarParams(carParams);
        for (int i = 1; i < carParams.length; i++) {
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
            System.out.println("No parameters requested");
            return null;
        }
        for (String param : params) {
            switch (param) {
                case Constants.MAX_SPEED_REQUEST:
                    parameters.put(Constants.MAX_SPEED_REQUEST, (double) cells[lane][position].getCarParams().maxSpeed);
                    break;
                case Constants.CURRENT_SPEED_REQUEST:
                    parameters.put(Constants.CURRENT_SPEED_REQUEST,
                            (double) cells[lane][position].getCarParams().currentSpeed);
                    break;
                case Constants.DISTANCE_TO_NEXT_CAR_REQUEST:
                    int nextCarPos = getNextOcuppiedCell(lane, position, Direction.STRAIGHT);
                    if (nextCarPos == Constants.NO_CAR_IN_FRONT) {
                        parameters.put(Constants.DISTANCE_TO_NEXT_CAR_REQUEST,
                                Double.MAX_VALUE);
                    } else {
                        parameters.put(Constants.DISTANCE_TO_NEXT_CAR_REQUEST,
                                (double) (nextCarPos - position - 1));
                    }
                    break;
                case Constants.DISTANCE_TO_NEXT_CAR_LEFT_REQUEST:
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
                case Constants.DISTANCE_TO_NEXT_CAR_RIGHT_REQUEST:
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
                case Constants.DISTANCE_TO_PREVIOUS_CAR_REQUEST:
                    int prevCarPos = getPreviousOccupiedCell(lane, position, Direction.STRAIGHT);
                    if (prevCarPos == Constants.NO_CAR_IN_FRONT) {
                        parameters.put(Constants.DISTANCE_TO_PREVIOUS_CAR_REQUEST,
                                (double) (position + 1));
                    } else {
                        parameters.put(Constants.DISTANCE_TO_PREVIOUS_CAR_REQUEST,
                                (double) (position - prevCarPos - 1));
                    }
                    break;
                case Constants.DISTANCE_TO_PREVIOUS_CAR_LEFT_REQUEST:
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
                case Constants.DISTANCE_TO_PREVIOUS_CAR_RIGHT_REQUEST:
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
                    System.out.println("Unknown parameter requested: " + param);
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
            System.out.println("Invalid lane or position to remove car");
            return;
        }
        if (!cells[lane][position].isOccupied() || !cells[lane][position].isHead()) {
            System.out.println("No car head at the specified position to remove");
            return;
        }
        CarParams carParams = cells[lane][position].getCarParams();
        for (int i = 0; i < carParams.length; i++) {
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
            System.out.println("Invalid lane or position to place car");
            return;
        }
        for (int i = 0; i < car.length; i++) {
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
                System.out.println("Car length exceeds road boundaries when placing car");
            }
        }
    }

    private boolean checkIfCarStillRelevant(CarParams car, int newSpeed) {
        if (car == null) {
            return false;
        }

        if ((car.xPosition + newSpeed - car.length + 1) >= this.numberOfCells) { // fuck zero base indexing
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
        car.length = car.length - howMuchOverflow;

        cells[car.lane][newHeadX].setOccupied(true);
        cells[car.lane][newHeadX].setHead(true);
        cells[car.lane][newHeadX].setCarParams(car);
        car.currentSpeed = newSpeed;
        car.xPosition = newHeadX;

        System.out.println("Old head position: " + oldX);
        System.out.println("Car at lane " + car.lane + " reached the end of the road and is partially removed.");
        System.out.println("New head position: " + newHeadX + ", New length: " + car.length + ", Current speed: " + car.currentSpeed);
        this.moveCar(cells[car.lane][newHeadX]);
    }
}
