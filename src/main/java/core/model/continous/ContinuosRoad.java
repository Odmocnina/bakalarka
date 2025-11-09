package core.model.continous;

import app.AppContext;
import core.model.*;
import core.utils.Constants;
import core.utils.MyLogger;
import core.utils.StringEditor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

/*****************************
 * class representing continuous road
 *
 * @author Michael Hladky
 * @version 1.0
 ****************************/
public class ContinuosRoad extends Road {

    /** each linked list in array is one lane, CarParams in linked list are cars, so this represents entire road **/
    LinkedList<CarParams>[] vehicles;

    /**
     * constructor for continuous road
     *
     * @param length length of the road
     * @param numberOfLanes number of lanes on the road
     * @param speedLimit speed limit on the road
     **/
    public ContinuosRoad(double length, int numberOfLanes, double speedLimit) {
        super(length, numberOfLanes, speedLimit, Constants.CONTINOUS);
        createRoad();
    }

    /**
     * method to create road structure, initialize lanes (linked lists)
     **/
    private void createRoad() {
        this.vehicles = new LinkedList[numberOfLanes];
        for (int lane = 0; lane < numberOfLanes; lane++) {
            this.vehicles[lane] = new LinkedList<>();
        }

        /*CarParams carParams = new CarParams();
        carParams.setParameter(Constants.CURRENT_SPEED_REQUEST, 0);
        carParams.setParameter(Constants.MAX_SPEED_REQUEST, 40.33);
        carParams.xPosition = 20;
        carParams.lane = 0;
        carParams.id = 1;
        carParams.setParameter(Constants.LENGTH_REQUEST, 4.5);
        carParams.setParameter(Constants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams.setParameter(Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams.setParameter(Constants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams.setParameter(Constants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams.setParameter(Constants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        vehicles[0].add(carParams);

        /*CarParams carParams2 = new CarParams();
        carParams2.setParameter(Constants.CURRENT_SPEED_REQUEST, 0);
        carParams2.setParameter(Constants.MAX_SPEED_REQUEST, 10.0);
        carParams2.xPosition = 50;
        carParams2.lane = 0;
        carParams2.id = 2;
        carParams2.setParameter(Constants.LENGTH_REQUEST, 4.5);
        carParams2.setParameter(Constants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams2.setParameter(Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams2.setParameter(Constants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams2.setParameter(Constants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams2.setParameter(Constants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        vehicles[0].add(carParams2);*/
    }

    /**
     * method to try to add new car at the beginning of each lane
     **/
    public void tryToAddCar() {
        for (int lane = 0; lane < numberOfLanes; lane++) {
            if (super.generator.decideIfNewCar()) {
                CarParams newCar = generator.generateCar();

                if (okToPutCar(newCar, lane)) {
                    placeCar(newCar, 0, lane);
                    MyLogger.log("New car placed at lane " + lane + " position 0, carParams: " + newCar,
                            Constants.DEBUG_FOR_LOGGING);
                }
            }
        }
    }

    /**
     * method to update the road, move cars forward according to car following model
     *
     * @return number of cars that passed the end of the road
     **/
    @Override
    public int updateRoad() {
        int carsPassed = this.forwardStep();

        if (true)
            this.tryToAddCar();

        return carsPassed;
    }

    /**
     * method to move cars forward according to car following model
     *
     * @return number of cars that passed the end of the road
     **/
    private int forwardStep() {
        int carsPassed = 0;
        for (int lane = this.numberOfLanes - 1; lane >= 0; lane--) {
            MyLogger.log("Updating lane " + lane + " with " + this.vehicles[lane].size() + " vehicles.",
                    Constants.DEBUG_FOR_LOGGING);
            carsPassed = carsPassed + this.updateLane(lane);
        }

        return carsPassed;
    }

    /**
     * method to update a single lane, move cars forward according to car following model
     *
     * @param lane lane to update
     * @return number of cars that passed the end of the road in this lane
     **/
    private int updateLane(int lane) {
        int carsPassed = 0;

        // nothing to update if lane is empty
        if (this.vehicles[lane] == null || this.vehicles[lane].isEmpty()) {
            return 0;
        }

        // safe iteration and removal using ListIterator
        final ListIterator<CarParams> it = this.vehicles[lane].listIterator();
        while (it.hasNext()) {
            CarParams car = it.next();

            // defensive check against broken car states
            if (car == null || Double.isNaN(car.xPosition) ||
                    Double.isNaN(car.getParameter(Constants.CURRENT_SPEED_REQUEST))) {
                it.remove();
                continue;
            }

            String requestParameters = AppContext.CAR_FOLLOWING_MODEL.requestParameters();
            HashMap<String, Double> parameters = getParameters(lane, this.vehicles[lane].indexOf(car),
                    requestParameters);
            if (parameters == null) {
                MyLogger.log("Error getting parameters for car at lane " + lane + ", position " +
                        this.vehicles[lane].indexOf(car), Constants.ERROR_FOR_LOGGING);
                continue;
            }
            double newSpeed = AppContext.CAR_FOLLOWING_MODEL.getNewSpeed(parameters);

            if (newSpeed > super.speedLimit) {
                newSpeed = super.speedLimit;
            }

            if (Double.isNaN(newSpeed) || newSpeed < 0.0) {
                newSpeed = 0.0;
            }

            car.setParameter(Constants.CURRENT_SPEED_REQUEST, newSpeed);
            car.xPosition += newSpeed;

            MyLogger.log("Car at lane " + lane + " updated to new speed " + newSpeed + " " +
                    "and new position " + car.xPosition, Constants.DEBUG_FOR_LOGGING);

            MyLogger.log("car x:" + car.xPosition + ", length: " + car.getParameter(Constants.LENGTH_REQUEST),
                    Constants.DEBUG_FOR_LOGGING);
            if (!checkIfCarStillRelevant(car, lane)) {
                it.remove();
                carsPassed++;
            }

        }

        // after movement, keep the lane sorted by xPosition (ascending)
        // this ensures that "car ahead" logic remains consistent
        //this.vehicles[lane].sort(Comparator.comparingDouble(c -> c.xPosition));

        return carsPassed;
    }

    private int updateLaneO(int lane) {
        int carsPassed = 0;
        for (CarParams car : this.vehicles[lane]) {
            String requestParameters = AppContext.CAR_FOLLOWING_MODEL.requestParameters();
            HashMap<String, Double> parameters = getParameters(lane, this.vehicles[lane].indexOf(car), requestParameters);
            if (parameters == null) {
                MyLogger.log("Error getting parameters for car at lane " + lane + ", position " +
                        this.vehicles[lane].indexOf(car), Constants.ERROR_FOR_LOGGING);
                continue;
            }
            double newSpeed = AppContext.CAR_FOLLOWING_MODEL.getNewSpeed(parameters);

            if (newSpeed > super.speedLimit) {
                newSpeed = super.speedLimit;
            }

            if (newSpeed < 0) {
                newSpeed = 0;
            }

            car.setParameter(Constants.CURRENT_SPEED_REQUEST, newSpeed);
            car.xPosition += newSpeed;

            MyLogger.log("Car at lane " + lane + " updated to new speed " + newSpeed + " " +
                    "and new position " + car.xPosition, Constants.DEBUG_FOR_LOGGING);

            MyLogger.log("car x:" + car.xPosition + ", length: " + car.getParameter(Constants.LENGTH_REQUEST),
                    Constants.DEBUG_FOR_LOGGING);
            if (!checkIfCarStillRelevant(car, lane)) {
                carsPassed++;
            }
        }

        return carsPassed;
    }

    /**
     * gets parameter about different car in proximity of car for witch are we using model
     *
     * @param parameters hashmap to put parameter into
     * @param param parameter to get
     * @param car car for which we are getting parameter about different car
     **/
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

    /*private void getRoadDependedParameters(HashMap<String, Double> parameters, String param, int lane, int position) {
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
                break;

            case Constants.SPEED_DIFFERENCE_TO_NEXT_CAR_REQUEST:
                parameters.put(Constants.SPEED_DIFFERENCE_TO_NEXT_CAR_REQUEST,
                        getSpeedDifferenceToNextCar(lane, position));
                break;

            default:
                MyLogger.log("Unknown parameter requested: " + param, Constants.DEBUG_FOR_LOGGING);
        }
    }*/

    /**
     * function to get car in proximity lane in given orientation, can give for example same lane (car ahead/back of the
     * inspected car), or different lane (car ahead/back of the inspected car in left/right lane), which lane is decided
     * by the lane parameter, which is linked list of cars in that lane
     *
     * @param orientation orientation to look for car in (FORWARD or BACKWARD)
     * @param car car for which we are looking for other car in proximity lane
     * @param lane lane in which to look for car
     * @return car in proximity lane in given orientation, null if no car found (free space)
     **/
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

    /**
     * function to get car in proximity in given direction and orientation, for example car ahead in same lane is
     * STRAIGHT FORWARD, car behind in left lane is LEFT BACKWARD, car ahead in right lane is RIGHT FORWARD, etc., car
     * then is used when its parameters are wanted for model, like its speed, position, etc.
     *
     * @param direction direction (lane) to look for car in (STRAIGHT, LEFT, RIGHT)
     * @param orientation orientation to look for car in (FORWARD or BACKWARD)
     * @param car car for which we are looking for other car in proximity
     * @return car in proximity in given direction and orientation, null if no car found (free space) also technically
     *         will return null if no lane is there in given direction, so thread carefully, should be handled properly
     *         by caller method
     **/
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

    /**
     * method to get all parameters needed for car following / lane-changing model for given car on given position in
     * given lane
     *
     * @param lane lane of the car
     * @param position position of the car in the lane (index in linked list)
     * @param requestParameters string with all requested parameters separated by request separator
     * @return hashmap with all requested parameters and their values
     **/
    private HashMap<String, Double> getParameters(int lane, int position, String requestParameters) {
        HashMap<String, Double> parameters = new HashMap<>();
        String[] params = requestParameters.split(Constants.REQUEST_SEPARATOR);
        if (params.length == 0) {
            MyLogger.log("No parameters requested", Constants.DEBUG_FOR_LOGGING);
            return null;
        }
        String[] carGeneratedParams = this.generator.getCarGenerationParameters();

        CarParams car = vehicles[lane].get(position);
        for (String param : params) {
            if (StringEditor.isInArray(carGeneratedParams, param) || param.equals(Constants.X_POSITION_REQUEST) ||
                    param.equals(Constants.CURRENT_SPEED_REQUEST)) { // get directly from car we are inspecting
                parameters.put(param, car.getParameter(param));
            } else {   // get parameter from different car in proximity
                this.getParametersAboutDifferentCar(parameters, param, car);
            }
        }
        return parameters;
    }

    /*private double getDistanceToNextCar(int lane, int position) {
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
    }*/

    /**
     * getter for content of the road, in this case array of linked lists of cars, overriding abstract method in Road
     *
     * @return array of linked lists of cars representing the road
     **/
    @Override
    public Object getContent() {
        return vehicles;
    }

    /**
     * method to check if it is ok to put new car at the beginning of given lane
     *
     * @param newCar new car to put
     * @param lane lane to put the car in
     * @return true if it is ok to put the car, false otherwise
     **/
    private boolean okToPutCar(CarParams newCar, int lane) {
        if (vehicles[lane].isEmpty()) {
            return true;
        }
        double gap = newCar.getParameter(Constants.LENGTH_REQUEST);
        CarParams firstCar = vehicles[lane].getFirst();
        double space = newCar.getParameter(Constants.LENGTH_REQUEST) +
                newCar.getParameter(Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        space += gap; // add length of the new car as well for better spacing
        if (space <= firstCar.xPosition) { // space needed is smaller than
            return true;
        }

        MyLogger.log("Generation blocked by car at position " + firstCar.xPosition,
                Constants.DEBUG_FOR_LOGGING);

        return false;
    }

    /**
     * method to place new car at given position in given lane
     *
     * @param newCar new car to place
     * @param position position to place the car at
     * @param lane lane to place the car in
     **/
    private void placeCar(CarParams newCar, double position, int lane) {
        newCar.xPosition = position;
        newCar.lane = lane;
        int place = findPlaceForCar(position, lane);

        vehicles[lane].add(place, newCar);
    }

    /**
     * method to find the correct place for new car in given lane, so that the lane remains sorted by xPosition
     *
     * @param x x position of the new car
     * @param lane lane to find the place in
     * @return index in linked list of the place to put the new car at
     **/
    private int findPlaceForCar(double x, int lane) {
        if (vehicles[lane].isEmpty()) {
            return 0;
        }
        for (int i = 0; i < vehicles[lane].size(); i++) {
            if (vehicles[lane].get(i).xPosition > x) {
                return i;
            }
        }
        return vehicles[lane].size() - 1;
    }

    /**
     * method to check if car is still relevant (has not passed the end of the road)
     *
     * @param car car to check
     * @param lane lane in which the car is
     * @return true if car is still relevant, false otherwise
     **/
    private boolean checkIfCarStillRelevant(CarParams car, int lane) {
        if ((car.xPosition - car.getParameter(Constants.LENGTH_REQUEST)) > super.length) {
            MyLogger.log("Car passed the end of the road and is being removed, carParams: " + car,
                    Constants.DEBUG_FOR_LOGGING);
            return false;
        } else if (car.xPosition > super.length) {
            double length = car.getParameter(Constants.LENGTH_REQUEST);
            double x = car.xPosition;
            double overflow = x - super.length;
            car.setParameter(Constants.LENGTH_REQUEST, length - overflow);
            car.xPosition = super.length;
        }

        return true;
    }

    /*private void removeCar(CarParams car, int lane) {
        vehicles[lane].remove(car);
    }*/

    /**
     * method to get number of cars currently on the road
     *
     * @return number of cars on the road
     **/
    @Override
    public int getNumberOfCarsOnRoad() {
        int totalCars = 0;
        for (int lane = 0; lane < numberOfLanes; lane++) {
            totalCars += vehicles[lane].size();
        }
        return totalCars;
    }


}
