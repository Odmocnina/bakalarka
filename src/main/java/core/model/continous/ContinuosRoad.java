package core.model.continous;

import app.AppContext;
import core.model.*;
import core.utils.Constants;

import java.util.HashMap;
import java.util.LinkedList;

import core.utils.StringEditor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ContinuosRoad extends Road {

    LinkedList<CarParams>[] vehicles;

    private static final Logger logger = LogManager.getLogger(ContinuosRoad.class);

    public ContinuosRoad(double length, int numberOfLanes, double speedLimit) {
        super(length, numberOfLanes, speedLimit, Constants.CONTINOUS);
        createRoad();
    }

    private void createRoad() {
        this.vehicles = new LinkedList[numberOfLanes];
        for (int lane = 0; lane < numberOfLanes; lane++) {
            this.vehicles[lane] = new LinkedList<>();
        }

        CarParams carParams = new CarParams();
        carParams.setParameter(Constants.CURRENT_SPEED_REQUEST, 0);
        carParams.setParameter(Constants.MAX_SPEED_REQUEST, 33.33);
        carParams.xPosition = 20;
        carParams.lane = 0;
        carParams.setParameter(Constants.LENGTH_REQUEST, 4.5);
        carParams.setParameter(Constants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams.setParameter(Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams.setParameter(Constants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams.setParameter(Constants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams.setParameter(Constants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        vehicles[0].add(carParams);

        CarParams carParams2 = new CarParams();
        carParams2.setParameter(Constants.CURRENT_SPEED_REQUEST, 0);
        carParams2.setParameter(Constants.MAX_SPEED_REQUEST, 10.0);
        carParams2.xPosition = 50;
        carParams2.lane = 0;
        carParams2.setParameter(Constants.LENGTH_REQUEST, 4.5);
        carParams2.setParameter(Constants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams2.setParameter(Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams2.setParameter(Constants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams2.setParameter(Constants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams2.setParameter(Constants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        vehicles[0].add(carParams2);
    }

    public void tryToAddCar() {
        for (int lane = 0; lane < numberOfLanes; lane++) {
            if (super.generator.decideIfNewCar()) {
                CarParams newCar = generator.generateCar();

                if (okToPutCar(newCar, lane)) {
                    placeCar(newCar, 0, lane);
                    logger.debug("New car placed at lane " + lane + " position 0, carParams: " + newCar.toString());
                }
            }
        }
    }

    @Override
    public int updateRoad() {

        int carsPassed = this.forwardStep();

        if (false)
            this.tryToAddCar();

        return carsPassed;
    }

    private int forwardStep() {
        int carsPassed = 0;
        for (int lane = this.numberOfLanes - 1; lane >= 0; lane--) {
            logger.debug("Updating lane " + lane + " with " + this.vehicles[lane].size() + " vehicles.");
            carsPassed = carsPassed + this.updateLane(lane);
        }

        return carsPassed;
    }

    private int updateLane(int lane) {
        int carsPassed = 0;
        for (CarParams car : this.vehicles[lane]) {
            String requestParameters = AppContext.CAR_FOLLOWING_MODEL.requestParameters();
            HashMap<String, Double> parameters = getParameters(lane, this.vehicles[lane].indexOf(car), requestParameters);
            if (parameters == null) {
                logger.debug("Error getting parameters for car at lane " + lane + ", position " +
                        this.vehicles[lane].indexOf(car));
                continue;
            }
            double newSpeed = AppContext.CAR_FOLLOWING_MODEL.getNewSpeed(parameters);

            if (newSpeed > super.speedLimit) {
                newSpeed = super.speedLimit;
            }

            car.setParameter(Constants.CURRENT_SPEED_REQUEST, newSpeed);
            car.xPosition += newSpeed;

            logger.debug("Car at lane " + lane + " updated to new speed " + newSpeed + " " +
                    "and new position " + car.xPosition);

            if (!checkIfCarStillRelevant(car, lane)) {
                carsPassed++;
            }
        }

        return carsPassed;
    }

    private void getParametersAboutDifferentCar(HashMap<String, Double> parameters, String param, CarParams car) {
        String[] paramSeparate = param.split(Constants.SUBREQUEST_SEPARATOR);
        String wantedParam = paramSeparate[0];
        Direction direction = Direction.valueOf(paramSeparate[1]);
        Orientation orientation = Orientation.valueOf(paramSeparate[2]);

        CarParams otherCar = getCarInProximity(direction, orientation, car);

        if (otherCar != null) {
            parameters.put(param, otherCar.getParameter(wantedParam));
        } else {
            parameters.put(param, Constants.NO_CAR_THERE);
        }
    }

    private void getRoadDependedParameters(HashMap<String, Double> parameters, String param, int lane, int position) {
        CarParams car = vehicles[lane].get(position);

        switch (param) {
            case Constants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST:
                CarParams forwardCar = getCarInProximity(Direction.STRAIGHT, Orientation.FORWARD, car);
                if (forwardCar != null) {
                    parameters.put(Constants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST,
                            forwardCar.getParameter(Constants.CURRENT_SPEED_REQUEST));
                } else {
                    parameters.put(Constants.CURRENT_SPEED_STRAIGHT_FORWARD_REQUEST, super.speedLimit);
                }
                break;

            case Constants.CURRENT_SPEED_REQUEST:
                parameters.put(Constants.CURRENT_SPEED_REQUEST, car.getParameter(Constants.CURRENT_SPEED_REQUEST));
                break;

            case Constants.DISTANCE_TO_NEXT_CAR_REQUEST:
                parameters.put(Constants.DISTANCE_TO_NEXT_CAR_REQUEST, getDistanceToNextCar(lane, position));
                int i = 0;
                break;

            case Constants.SPEED_DIFFERENCE_TO_NEXT_CAR_REQUEST:
                parameters.put(Constants.SPEED_DIFFERENCE_TO_NEXT_CAR_REQUEST,
                        getSpeedDifferenceToNextCar(lane, position));
                break;

            default:
                logger.debug("Unknown parameter requested: " + param);
        }
    }

    private CarParams getCarInProximityLane(Orientation orientation, CarParams car, LinkedList<CarParams> lane) {
        if (orientation == Orientation.FORWARD) {
            for (int i = 0; i < lane.size(); i++) {
                CarParams otherCar = lane.get(i);
                if (otherCar.xPosition > car.xPosition) {
                    return otherCar;
                }
            }
        } else if (orientation == Orientation.BACKWARD) {
            for (int i = lane.size() - 1; i >= 0; i--) {
                CarParams otherCar = lane.get(i);
                if (otherCar.xPosition < car.xPosition) {
                    return otherCar;
                }
            }
        }

        return null;
    }

    private CarParams getCarInProximity(Direction direction, Orientation orientation, CarParams car) {
        int lane = car.lane;
        int position = this.vehicles[lane].indexOf(car);

        if (direction == Direction.STRAIGHT) {
            if (orientation == Orientation.FORWARD) {
                if (position < this.vehicles[lane].size() - 1) {
                    return this.vehicles[lane].get(position + 1);
                } else {
                    return null;
                }
            } else {
                if (position > 0) {
                    return this.vehicles[lane].get(position - 1);
                } else {
                    return null;
                }
            }
        } else if (direction == Direction.LEFT) {
            if (lane == 0) {
                return null;
            }
            LinkedList<CarParams> laneForScan = this.vehicles[lane - 1];
            return this.getCarInProximityLane(orientation, car, laneForScan);
        } else if (direction == Direction.RIGHT) {
            if (lane == numberOfLanes - 1) {
                return null;
            }

            LinkedList<CarParams> laneForScan = this.vehicles[lane + 1];
            return this.getCarInProximityLane(orientation, car, laneForScan);
        }

        return null;


    }

    private HashMap<String, Double> getParameters(int lane, int position, String requestParameters) {
        HashMap<String, Double> parameters = new HashMap<>();
        String[] params = requestParameters.split(Constants.REQUEST_SEPARATOR);
        if (params.length == 0) {
            logger.debug("No parameters requested");
            return null;
        }
        String[] carGeneratedParams = this.generator.getCarGenerationParameters();

        CarParams car = vehicles[lane].get(position);
        for (String param : params) {
            if (StringEditor.isInArray(carGeneratedParams, param) || param.equals(Constants.X_POSITION_REQUEST) ||
                    param.equals(Constants.CURRENT_SPEED_REQUEST)) {
                int i = 0;
                parameters.put(param, car.getParameter(param));
            } else {
                //this.getRoadDependedParameters(parameters, param, lane, position);
                this.getParametersAboutDifferentCar(parameters, param, car);
            }
        }
        return parameters;
    }

    private double getDistanceToNextCar(int lane, int position) {
        if (position >= vehicles[lane].size() - 1) {
            return Double.MAX_VALUE; // No car in front
        }
        double distance = 0.0;
        distance = vehicles[lane].get(position + 1).xPosition - vehicles[lane].get(position).xPosition
                - vehicles[lane].get(position).getParameter(Constants.LENGTH_REQUEST);
        return distance;
    }

    private double getSpeedDifferenceToNextCar(int lane, int position) {
        if (position >= vehicles[lane].size() - 1) {
            return 0.0; // No car in front
        }
        return Math.abs(vehicles[lane].get(position).getParameter(Constants.CURRENT_SPEED_REQUEST) -
                vehicles[lane].get(position + 1).getParameter(Constants.CURRENT_SPEED_REQUEST));
    }

    @Override
    public Object getContent() {
        return vehicles;
    }

    private boolean okToPutCar(CarParams newCar, int lane) {
        if (vehicles[lane].isEmpty()) {
            return true;
        }
        CarParams firstCar = vehicles[lane].getFirst();
        double space = newCar.getParameter(Constants.LENGTH_REQUEST) +
                newCar.getParameter(Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);

        if (space <= firstCar.xPosition) {
            return true;
        }

        return false;
    }

    private void placeCar(CarParams newCar, double position, int lane) {
        newCar.xPosition = position;
        newCar.lane = lane;
        int place = findPlaceForCar(position, lane);
        vehicles[lane].add(place, newCar);
    }

    private int findPlaceForCar(double x, int lane) {
        if (vehicles[lane].isEmpty()) {
            return 0;
        }
        for (int i = 0; i < vehicles[lane].size(); i++) {
            if (vehicles[lane].get(i).xPosition > x) {
                return i;
            }
        }
        return vehicles[lane].size();
    }

    private boolean checkIfCarStillRelevant(CarParams car, int lane) {
        if ((car.xPosition - car.getParameter(Constants.LENGTH_REQUEST)) > this.length) {
            removeCar(car, lane);
            return false;
        }

        return true;
    }

    private void removeCar(CarParams car, int lane) {
        vehicles[lane].remove(car);
    }

    @Override
    public int getNumberOfCarsOnRoad() {
        int totalCars = 0;
        for (int lane = 0; lane < numberOfLanes; lane++) {
            totalCars += vehicles[lane].size();
        }
        return totalCars;
    }


}
