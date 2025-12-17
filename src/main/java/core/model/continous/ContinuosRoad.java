package core.model.continous;

import app.AppContext;
import core.model.*;
import core.utils.*;
import core.utils.constants.Constants;
import core.utils.constants.RequestConstants;

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

    /** structure storing cars on the road, each linked list in array is one lane, CarParams in linked list are cars,
     * so this represents entire road **/
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
        carParams.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0);
        carParams.setParameter(RequestConstants.MAX_SPEED_REQUEST, 40.33);
        carParams.xPosition = 20;
        carParams.lane = 0;
        carParams.id = 1;
        carParams.setParameter(RequestConstants.LENGTH_REQUEST, 6.5);
        carParams.setParameter(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams.setParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams.setParameter(RequestConstants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams.setParameter(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams.setParameter(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        carParams.setParameter(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.9);
        carParams.setParameter(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 0.5);
        vehicles[0].add(0, carParams);

        CarParams carParams2 = new CarParams();
        carParams2.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0);
        carParams2.setParameter(RequestConstants.MAX_SPEED_REQUEST, 40.33);
        carParams2.xPosition = 20;
        carParams2.lane = 1;
        carParams2.id = 2;
        carParams.color = Constants.CAR_COLORS[1];
        carParams2.setParameter(RequestConstants.LENGTH_REQUEST, 6.5);
        carParams2.setParameter(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams2.setParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams2.setParameter(RequestConstants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams2.setParameter(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams2.setParameter(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        carParams2.setParameter(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.3);
        carParams2.setParameter(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 1.0);
        vehicles[1].add(0, carParams2);

        CarParams carParams3 = new CarParams();
        carParams3.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0);
        carParams3.setParameter(RequestConstants.MAX_SPEED_REQUEST, 10.0);
        carParams3.xPosition = 50;
        carParams3.lane = 0;
        carParams3.id = 3;
        carParams3.color = Constants.CAR_COLORS[2];
        carParams3.setParameter(RequestConstants.LENGTH_REQUEST, 6.5);
        carParams3.setParameter(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams3.setParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams3.setParameter(RequestConstants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams3.setParameter(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams3.setParameter(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        carParams3.setParameter(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.3);
        carParams3.setParameter(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 1.0);
        vehicles[0].add(1, carParams3);

        CarParams carParams4 = new CarParams();
        carParams4.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0);
        carParams4.setParameter(RequestConstants.MAX_SPEED_REQUEST, 10.0);
        carParams4.xPosition = 50;
        carParams4.lane = 5;
        carParams4.id = 4;
        carParams4.color = Constants.CAR_COLORS[2];
        carParams4.setParameter(RequestConstants.LENGTH_REQUEST, 6.5);
        carParams4.setParameter(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams4.setParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams4.setParameter(RequestConstants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams4.setParameter(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams4.setParameter(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        carParams4.setParameter(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.3);
        carParams4.setParameter(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 1.0);
        vehicles[5].add(0, carParams4);

        CarParams carParams5 = new CarParams();
        carParams5.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0);
        carParams5.setParameter(RequestConstants.MAX_SPEED_REQUEST, 10.0);
        carParams5.xPosition = 60;
        carParams5.lane = 5;
        carParams5.id = 5;
        carParams5.color = Constants.CAR_COLORS[2];
        carParams5.setParameter(RequestConstants.LENGTH_REQUEST, 6.5);
        carParams5.setParameter(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams5.setParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams5.setParameter(RequestConstants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams5.setParameter(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams5.setParameter(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        carParams5.setParameter(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.3);
        carParams5.setParameter(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 1.0);
        vehicles[5].add(1, carParams5);

        CarParams carParams6 = new CarParams();
        carParams6.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0);
        carParams6.setParameter(RequestConstants.MAX_SPEED_REQUEST, 10.0);
        carParams6.xPosition = 70;
        carParams6.lane = 5;
        carParams6.id = 6;
        carParams6.color = Constants.CAR_COLORS[2];
        carParams6.setParameter(RequestConstants.LENGTH_REQUEST, 6.5);
        carParams6.setParameter(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams6.setParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams6.setParameter(RequestConstants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams6.setParameter(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams6.setParameter(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        carParams6.setParameter(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.3);
        carParams6.setParameter(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 1.0);
        vehicles[5].add(2, carParams6);*/
    }

    /**
     * method to update the road, move cars forward according to car following model, synchronized to avoid concurrency
     * issues, because when gui is used, it's running in different thread than simulation and if too many cars are at
     * one time it can cause concurrent modification exception
     *
     * @return number of cars that passed the end of the road
     **/
    @Override
    public synchronized int updateRoad() {
        int carsPassed = this.forwardStep();

        if (true)
            super.tryToAddCar();

        return carsPassed;
    }

    /**
     * method to move cars forward according to car following model
     *
     * @return number of cars that passed the end of the road
     **/
    private int forwardStep() {
        for (int lane = this.numberOfLanes - 1; lane >= 0; lane--) {
            MyLogger.log("Updating lane " + lane + " with " + this.vehicles[lane].size() + " vehicles.",
                    Constants.DEBUG_FOR_LOGGING);

            this.updateLane(lane);
        }

        this.resetProcessedFlags();

        if (AppContext.RUN_DETAILS.debug) {
            this.checkForCollisions();
            this.checkForDuplicates();
        }

        return this.checkRelevancyOfCars();
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
        final ListIterator<CarParams> it = this.vehicles[lane].listIterator(this.vehicles[lane].size());
        while (it.hasPrevious()) {
            CarParams car = it.previous();

            if (car == null) {
                it.remove();
                continue;
            }

            if (car.processedInCurrentStep) {
                continue;
            }

            // defensive check against broken car states
            if (Double.isNaN(car.xPosition) || Double.isNaN(car.getParameter(RequestConstants.CURRENT_SPEED_REQUEST))) {
                it.remove();
                continue;
            }

            String requestParameters = AppContext.CAR_FOLLOWING_MODEL.requestParameters();
            HashMap<String, Double> parameters = getParameters(car, this.vehicles, requestParameters);
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

            // try lane change
            Direction direction = Direction.STRAIGHT;
            if (AppContext.RUN_DETAILS.laneChange) {
                direction = this.tryLaneChange(car);
            }

            newSpeed = this.resolveCollision(car, newSpeed);

            car.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, newSpeed);
            car.xPosition += newSpeed;

            MyLogger.log("Car at lane " + lane + " updated to new speed " + newSpeed + " " +
                    "and new position " + car.xPosition, Constants.DEBUG_FOR_LOGGING);

            MyLogger.log("car x:" + car.xPosition + ", length: " + car.getParameter(RequestConstants.
                    LENGTH_REQUEST), Constants.DEBUG_FOR_LOGGING);
            car.processedInCurrentStep = true;
            if (direction != Direction.STRAIGHT) {
                it.remove();
            }

        }

        // after movement, keep the lane sorted by xPosition (ascending)
        // this ensures that "car ahead" logic remains consistent
        //this.vehicles[lane].sort(Comparator.comparingDouble(c -> c.xPosition));

        return carsPassed;
    }

    /**
     * method to get all parameters needed for car following / lane-changing model for given car on given position in
     * given lane
     *
     * @param inspectedCar car for which the parameters are being gathered
     * @param road road structure to get parameters from, can be real road or fake road for lane change calculation
     * @param requestParameters string of requested parameters separated by REQUEST_SEPARATOR
     **/
    private HashMap<String, Double> getParameters(CarParams inspectedCar, LinkedList<CarParams>[] road,
                                                  String requestParameters) {
        HashMap<String, Double> parameters = new HashMap<>();
        String[] params = requestParameters.split(RequestConstants.REQUEST_SEPARATOR);
        if (params.length == 0) {
            MyLogger.log("No parameters requested", Constants.DEBUG_FOR_LOGGING);
            return null;
        }
        String[] carGeneratedParams = super.generators[inspectedCar.lane].getCarGenerationParameters();
        String[] roadSimulationParams = {RequestConstants.TIME_STEP_REQUEST, RequestConstants.MAX_ROAD_SPEED_REQUEST};

        CarParams car = this.getCarById(inspectedCar.id, road);
        for (String param : params) {
            if (StringEditor.isInArray(carGeneratedParams, param) || param.equals(RequestConstants.X_POSITION_REQUEST)
                    || param.equals(RequestConstants.CURRENT_SPEED_REQUEST)) { //get directly from car we are inspecting
                parameters.put(param, car.getParameter(param));
            } else if (param.contains("Acceleration")) {    //get acceleration what would be if lane change occurs
                double acceleration;
                if (param.contains("theoretical")) {
                    acceleration = this.getAccelerationOfDifferentCar(car, param, road);
                } else {
                    CarParams carForInspection = this.getCarById(car.id, this.vehicles);
                    acceleration = this.getAccelerationOfDifferentCar(carForInspection, param, vehicles);
                }
                parameters.put(param, acceleration);
            } else if (StringEditor.isInArray(roadSimulationParams, param)) { //get from road/simulation
                this.getRoadSimulationParameter(parameters, param, car);
            } else {   // get parameter from different car in proximity
                this.getParametersAboutDifferentCar(parameters, param, car, road);
            }
        }

        return parameters;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * method to get parameter about different car in proximity (used for lane changing models)
     *
     * @param parameters hashmap to put the parameter into
     * @param param parameter string containing info about which car to check
     * @param car car to check from
     * @param road road structure to check in
     **/
    private void getParametersAboutDifferentCar(HashMap<String, Double> parameters, String param, CarParams car,
                                                LinkedList<CarParams>[] road) {
        String[] paramSeparate = param.split(RequestConstants.SUBREQUEST_SEPARATOR);
        String wantedParam = paramSeparate[0];
        Orientation orientation = Orientation.valueOf(paramSeparate[2]);

        CarParams otherCar = getCarInProximity(orientation, car, road);

        if (otherCar != null) {
            parameters.put(param, otherCar.getParameter(wantedParam));
        } else {
            if (orientation == Orientation.FORWARD && !super.isLaneGreen(car.lane)) {
                if (wantedParam.equals(RequestConstants.X_POSITION_REQUEST)) {
                    parameters.put(param, super.length);
                } else {
                    parameters.put(param, 0.0);
                }
            } else {
                parameters.put(param, Constants.NO_CAR_THERE);
            }
        }
    }

    /**
     * method to check relevancy of cars on the road, remove cars that passed the end of the road
     *
     * @return number of cars that passed the end of the road
     **/
    private int checkRelevancyOfCars() {
        int carsPassed = 0;
        for (int lane = 0; lane < this.numberOfLanes; lane++) {
            final ListIterator<CarParams> it = this.vehicles[lane].listIterator();
            while (it.hasNext()) {
                CarParams car = it.next();
                if (!this.checkIfCarStillRelevant(car)) {
                    it.remove();
                    carsPassed++;
                }
            }
        }

        return carsPassed;
    }

    /**
     * method to reset processed flags of all cars after update step
     **/
    private void resetProcessedFlags() {
        for (int lane = 0; lane < this.numberOfLanes; lane++) {
            for (CarParams car : this.vehicles[lane]) {
                car.processedInCurrentStep = false;
            }
        }
    }

    /**
     * method to get car in proximity (forward or backward) on the same lane
     *
     * @param orientation orientation (forward or backward)
     * @param car car to check from
     * @param road road structure to check in
     * @return car in proximity or null if no car found
     **/
    private CarParams getCarInProximity(Orientation orientation, CarParams car, LinkedList<CarParams>[] road) {
        int lane = car.lane;
        int position = road[lane].indexOf(car);

        if (orientation == Orientation.FORWARD) {
            if (position < road[lane].size() - 1) {
                return road[lane].get(position + 1);
            } else {
                return null;
            }
        } else {
            if (position > 0) {
                return road[lane].get(position - 1);
            } else {
                return null;
            }
        }
    }

    /**
     * method that attempts to change lane for given car, if it does so, it updates the car's lane and position in the
     * road structure
     *
     * @param car car to attempt lane change for
     * @return direction of lane change (LEFT, RIGHT, STRAIGHT)
     */
    private Direction tryLaneChange(CarParams car) {
        int lane = car.lane;
        int index = vehicles[lane].indexOf(car);
        Direction direction = Direction.LEFT;
        String requestParameters;
        LinkedList<CarParams>[] fakeRoad;
        HashMap<String, Double> parameters;
        Direction desiredDirection;

        if (lane > 0) { // try to change lane to the left
            requestParameters = AppContext.LANE_CHANGING_MODEL.requestParameters(direction);
            fakeRoad = this.createFakeRoad(direction, car);
            if (fakeRoad != null) {
                parameters = getParameters(car, fakeRoad, requestParameters);
                desiredDirection = AppContext.LANE_CHANGING_MODEL.changeLaneIfDesired(parameters, direction);
                if (desiredDirection == Direction.LEFT) {
                    this.placeCar(car, this.vehicles, Direction.LEFT);
                    MyLogger.log("Car at lane " + lane + " position " + index + " changed lane to LEFT.",
                            Constants.DEBUG_FOR_LOGGING);
                    return Direction.LEFT;
                }
            }
        }

        if (lane < this.numberOfLanes - 1) { // try to change lane to the right
            direction = Direction.RIGHT;
            requestParameters = AppContext.LANE_CHANGING_MODEL.requestParameters(direction);
            fakeRoad = this.createFakeRoad(direction, car);
            if (fakeRoad != null) {
                parameters = getParameters(car, fakeRoad, requestParameters);
                desiredDirection = AppContext.LANE_CHANGING_MODEL.changeLaneIfDesired(parameters, direction);
                if (desiredDirection == Direction.RIGHT) {
                    this.placeCar(car, this.vehicles, Direction.RIGHT);
                    MyLogger.log("Car at lane " + lane + " position " + index + " changed lane to RIGHT.",
                            Constants.DEBUG_FOR_LOGGING);
                    return Direction.RIGHT;
                }
            }
        }

        return Direction.STRAIGHT; // if no lane change possible or desired, return straight
    }

    /**
     * method to get acceleration of different car in proximity (used for lane changing models)
     *
     * @param car car to check from
     * @param param parameter string containing info about which car to check
     * @param road road structure to check in
     * @return acceleration of the different car
     **/
    private double getAccelerationOfDifferentCar(CarParams car, String param, LinkedList<CarParams>[] road) {
        String[] paramSeparate = param.split(RequestConstants.SUBREQUEST_SEPARATOR);
        CarParams carToStudy;

        if (paramSeparate.length >= 3) {
            Orientation orientation = Orientation.valueOf(paramSeparate[2]);
            carToStudy = getCarInProximity(orientation, car, road);
        } else {
            carToStudy = car;
        }

        if (carToStudy == null) {
            return 0.0;
        }

        String requestParameters = AppContext.CAR_FOLLOWING_MODEL.requestParameters();
        HashMap<String, Double> parameters = getParameters(carToStudy, road, requestParameters);
        double newSpeed = AppContext.CAR_FOLLOWING_MODEL.getNewSpeed(parameters);
        double oldSpeed = carToStudy.getParameter(RequestConstants.CURRENT_SPEED_REQUEST);

        return newSpeed - oldSpeed;
    }

    /**
     * method to check if given place in given lane is ok to place the car in (no collisions with other cars)
     *
     * @param lane lane to check in
     * @param place index in linked list to check
     * @param car car to place
     * @return true if place is ok, false otherwise
     **/
    private boolean isPlaceOkInLane(LinkedList<CarParams> lane, int place, CarParams car) {
        if (place < 0) {
            return false;
        }

        if (lane.isEmpty()) {
            return true;
        }

        if (place > 0) {
            double backXOfCar = (car.xPosition - car.getParameter(RequestConstants.LENGTH_REQUEST));
            CarParams carBehind = lane.get(place - 1);
            if (carBehind.xPosition >= backXOfCar) {
               return false;
            }
        }

        if (place < lane.size()) {
            CarParams carInFront = lane.get(place);
            double backXOfCarInFront = carInFront.xPosition - carInFront.getParameter(RequestConstants.LENGTH_REQUEST);

            return !(car.xPosition >= backXOfCarInFront);
        }

        return true;
    }

    /**
     * method to place car in given lane in given direction (left, right or if on the same lane then straight)
     *
     * @param car car to place
     * @param road road structure to place the car in
     * @param direction direction to place the car in (left or right)
     * @return true if placement was successful, false otherwise
     **/
    private boolean placeCar(CarParams car, LinkedList<CarParams>[] road, Direction direction) {
        int lane = car.lane;
        double position = car.xPosition;
        if (direction == Direction.LEFT) {
            lane = lane - 1;
        } else if (direction == Direction.RIGHT) {
            lane = lane + 1;
        }

        if (lane >= 0 && lane < road.length) {
            int place = findPlaceForCar(position, road[lane]);

            if (!isPlaceOkInLane(road[lane], place, car)) {
                return false;
            }

            road[lane].add(place, car);
            car.lane = lane;
            return true;
        }

        return false;
    }

    /**
     * method to find place for car in given lane based on x position, DOES NOT CHECK IF PLACE IS OK, that must be done
     * separately
     *
     * @param x x position of the car
     * @param lane lane to find place in
     * @return index in linked list where the car should be placed
     **/
    private int findPlaceForCar(double x, LinkedList<CarParams> lane) {
        if (lane.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < lane.size(); i++) {
            if (lane.get(i).xPosition > x) {
                return i;
            }
        }
        return lane.size();
    }


    /**
     * method to create fake road structure for lane change calculation, where the car to be inspected is removed from
     * its current lane and placed in the target lane, used for lane change models to calculate accelerations
     *
     * @param direction direction to place the car in (left or right)
     * @param car car to place
     * @return fake road structure with car placed in target lane, null if placement was not successful
     **/
    private LinkedList<CarParams>[] createFakeRoad(Direction direction, CarParams car) {
        LinkedList<CarParams>[] fakeRoad = this.copyRoadStructureDeep();
        int lane = car.lane;
        int index = vehicles[lane].indexOf(car);
        CarParams carForFakeRoad = fakeRoad[lane].get(index);
        boolean successfulCreation = this.placeCar(carForFakeRoad, fakeRoad, direction);
        if (!successfulCreation) {
            return null;
        }
        int id = carForFakeRoad.id;
        carForFakeRoad = this.getCarById(id, fakeRoad);
        fakeRoad[lane].remove(carForFakeRoad);
        return fakeRoad;
    }

    /**
     * method to create a deep copy of the road structure, used for lane change, where models like mobil look what would
     * the acceleration be if the lane change occurs for car inspected and cars in proximity, change is done in fake
     * road structure and the values are calculated there
     *
     * @return deep copy of the road structure (array of linked lists of cars)
     */
    private LinkedList<CarParams>[] copyRoadStructureDeep() {
        LinkedList<CarParams>[] roadCopy = new LinkedList[this.numberOfLanes];
        for (int lane = 0; lane < this.numberOfLanes; lane++) {
            roadCopy[lane] = new LinkedList<>();
            for (CarParams car : this.vehicles[lane]) {
                roadCopy[lane].add(car.clone());
            }
        }

        return roadCopy;
    }

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
    @Override
    protected boolean okToPutCarAtStart(CarParams newCar, int lane) {
        if (vehicles[lane].isEmpty()) {
            return true;
        }
        double gap = newCar.getParameter(RequestConstants.LENGTH_REQUEST);
        CarParams firstCar = vehicles[lane].getFirst();
        double space = newCar.getParameter(RequestConstants.LENGTH_REQUEST) +
                newCar.getParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST);
        space += gap; // add length of the new car as well for better spacing
        double carBackPosition = firstCar.xPosition - firstCar.getParameter(RequestConstants.LENGTH_REQUEST);
        if (space <= carBackPosition) { // space needed is smaller than
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
    @Override
    protected void placeCarAtStart(CarParams newCar, double position, int lane) {
        newCar.xPosition = position;
        newCar.lane = lane;
        int place = findPlaceForCar(position, vehicles[lane]);

        vehicles[lane].add(place, newCar);
    }

    /**
     * method to check if car is still relevant (has not passed the end of the road)
     *
     * @param car car to check
     * @return true if car is still relevant, false otherwise
     **/
    private boolean checkIfCarStillRelevant(CarParams car) {
        if ((car.xPosition - car.getParameter(RequestConstants.LENGTH_REQUEST)) > super.length) {
            MyLogger.log("Car passed the end of the road and is being removed, carParams: " + car,
                    Constants.DEBUG_FOR_LOGGING);
            return false;
        } else if (car.xPosition > super.length) {
            double length = car.getParameter(RequestConstants.LENGTH_REQUEST);
            double x = car.xPosition;
            double overflow = x - super.length;
            car.setParameter(RequestConstants.LENGTH_REQUEST, length - overflow);
            car.xPosition = super.length;
        }

        return true;
    }

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

    /**
     * method to get car by its ID from the road structure
     *
     * @param id ID of the car to get
     * @param road road structure to search in
     * @return car with given ID or null if not found
     **/
    private CarParams getCarById(int id, LinkedList<CarParams>[] road) {
        for (LinkedList<CarParams> carParams : road) {
            for (CarParams car : carParams) {
                if (car.id == id) {
                    return car;
                }
            }
        }
        return null;
    }

    /**
     * method to check for duplicate cars on the road, for debugging purposes
     **/
    private void checkForDuplicates() {
        for (int lane = 0; lane < numberOfLanes; lane++) {
            LinkedList<CarParams> laneList = vehicles[lane];
            for (int i = 0; i < laneList.size(); i++) {
                CarParams carA = laneList.get(i);
                for (int j = i + 1; j < laneList.size(); j++) {
                    CarParams carB = laneList.get(j);
                    if (carA.id == carB.id) {
                        MyLogger.log("Duplicate car found in lane " + lane + " with ID: " + carA.id,
                                Constants.ERROR_FOR_LOGGING);
                    }
                }
            }
        }
    }

    /**
     * method to resolve collision for given car and new speed, if collision detected, log it and adjust speed
     *
     * @param car car to check for collision
     * @param newSpeed new speed of the car
     * @return adjusted speed to avoid collision
     **/
    private double resolveCollision(CarParams car, double newSpeed) {
        int lane = car.lane;
        int position = vehicles[lane].indexOf(car);

        if (position < vehicles[lane].size() - 1) {
            CarParams carInFront = vehicles[lane].get(position + 1);
            double distanceToCarInFront = carInFront.xPosition - carInFront.getParameter(RequestConstants.LENGTH_REQUEST)
                    - car.xPosition;
            if (distanceToCarInFront < newSpeed) {
                ResultsRecorder.getResultsRecorder().addCollision();
                if (AppContext.RUN_DETAILS.preventCollisions) {
                    return distanceToCarInFront - 1.0;
                }
            }
        }

        return newSpeed;
    }

    /**
     * method to check for collisions on the road, log error if collision detected
     **/
    private void checkForCollisions() {
        for (int lane = 0; lane < numberOfLanes; lane++) {
            LinkedList<CarParams> laneList = vehicles[lane];
            for (int i = 0; i < laneList.size() - 1; i++) {
                CarParams carA = laneList.get(i);     // back
                CarParams carB = laneList.get(i + 1); // front

                double aFront = carA.xPosition;
                double bBack = carB.xPosition - carB.getParameter(RequestConstants.LENGTH_REQUEST);

                if (bBack < aFront) { // intervals are overlapping
                    MyLogger.log("Collision detected in lane " + lane + " between cars ID: " + carA.id +
                            " and ID: " + carB.id, Constants.ERROR_FOR_LOGGING);
                }
            }
        }
    }

}
