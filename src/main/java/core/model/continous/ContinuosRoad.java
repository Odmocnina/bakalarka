package core.model.continous;

import app.AppContext;
import core.model.*;
import core.utils.Constants;
import core.utils.MyLogger;
import core.utils.RequestConstants;
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

        CarParams carParams = new CarParams();
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
        vehicles[0].add(carParams);

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
        vehicles[1].add(carParams2);

        carParams2 = new CarParams();
        carParams2.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0);
        carParams2.setParameter(RequestConstants.MAX_SPEED_REQUEST, 10.0);
        carParams2.xPosition = 50;
        carParams2.lane = 0;
        carParams2.id = 3;
        carParams2.color = Constants.CAR_COLORS[2];
        carParams2.setParameter(RequestConstants.LENGTH_REQUEST, 6.5);
        carParams2.setParameter(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams2.setParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams2.setParameter(RequestConstants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams2.setParameter(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams2.setParameter(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        carParams2.setParameter(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.3);
        carParams2.setParameter(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 1.0);
        vehicles[0].add(carParams2);

        carParams2 = new CarParams();
        carParams2.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0);
        carParams2.setParameter(RequestConstants.MAX_SPEED_REQUEST, 10.0);
        carParams2.xPosition = 50;
        carParams2.lane = 5;
        carParams2.id = 4;
        carParams2.color = Constants.CAR_COLORS[2];
        carParams2.setParameter(RequestConstants.LENGTH_REQUEST, 6.5);
        carParams2.setParameter(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams2.setParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams2.setParameter(RequestConstants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams2.setParameter(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams2.setParameter(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        carParams2.setParameter(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.3);
        carParams2.setParameter(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 1.0);
        vehicles[5].add(carParams2);

        carParams2 = new CarParams();
        carParams2.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0);
        carParams2.setParameter(RequestConstants.MAX_SPEED_REQUEST, 10.0);
        carParams2.xPosition = 60;
        carParams2.lane = 5;
        carParams2.id = 5;
        carParams2.color = Constants.CAR_COLORS[2];
        carParams2.setParameter(RequestConstants.LENGTH_REQUEST, 6.5);
        carParams2.setParameter(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams2.setParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams2.setParameter(RequestConstants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams2.setParameter(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams2.setParameter(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        carParams2.setParameter(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.3);
        carParams2.setParameter(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 1.0);
        vehicles[5].add(carParams2);

        carParams2 = new CarParams();
        carParams2.setParameter(RequestConstants.CURRENT_SPEED_REQUEST, 0);
        carParams2.setParameter(RequestConstants.MAX_SPEED_REQUEST, 10.0);
        carParams2.xPosition = 70;
        carParams2.lane = 5;
        carParams2.id = 6;
        carParams2.color = Constants.CAR_COLORS[2];
        carParams2.setParameter(RequestConstants.LENGTH_REQUEST, 6.5);
        carParams2.setParameter(RequestConstants.MAX_ACCELERATION_REQUEST, 2.0);
        carParams2.setParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, 2.0);
        carParams2.setParameter(RequestConstants.DECELERATION_COMFORT_REQUEST, 4.5);
        carParams2.setParameter(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, 1.5);
        carParams2.setParameter(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, 0.6);
        carParams2.setParameter(RequestConstants.POLITENESS_FACTOR_REQUEST, 0.3);
        carParams2.setParameter(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, 1.0);
        vehicles[5].add(carParams2);
    }

    /**
     * method to try to add new car at the beginning of each lane
     **/
    public void tryToAddCar() {
        for (int lane = 0; lane < numberOfLanes; lane++) {
            if (super.generator.decideIfNewCar()) {
                CarParams newCar = generator.generateCar();

                if (okToPutCar(newCar, lane)) {
                    placeCar(newCar, newCar.getParameter(RequestConstants.LENGTH_REQUEST), lane);
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

        if (AppContext.SIMULATION.getStepCount() > 10) {
            int i = 0;
        }

        // safe iteration and removal using ListIterator
        final ListIterator<CarParams> it = this.vehicles[lane].listIterator(this.vehicles[lane].size());
        while (it.hasPrevious()) {
            CarParams car = it.previous();

            if (car.processedInCurrentStep) {
                continue;
            }

            // defensive check against broken car states
            if (car == null || Double.isNaN(car.xPosition) ||
                    Double.isNaN(car.getParameter(RequestConstants.CURRENT_SPEED_REQUEST))) {
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

            // try lane change
            Direction direction = Direction.STRAIGHT;
            if (AppContext.RUN_DETAILS.laneChange) {
                direction = this.tryLaneChange(car);
            }

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
            parameters.put(param, Constants.NO_CAR_THERE);
        }
    }

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

    private void getRoadSimulationParameter(HashMap<String, Double> parameters, String param, CarParams car) {
        if (param.equals(RequestConstants.TIME_STEP_REQUEST)) {
            parameters.put(param, AppContext.RUN_DETAILS.timeStep);
        } else if (param.equals(RequestConstants.MAX_ROAD_SPEED_REQUEST)) {
            parameters.put(param, super.speedLimit);
        }
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
        String[] params = requestParameters.split(RequestConstants.REQUEST_SEPARATOR);
        if (params.length == 0) {
            MyLogger.log("No parameters requested", Constants.DEBUG_FOR_LOGGING);
            return null;
        }
        String[] carGeneratedParams = this.generator.getCarGenerationParameters();
        String[] roadSimulationParams = {RequestConstants.TIME_STEP_REQUEST, RequestConstants.MAX_ROAD_SPEED_REQUEST};

        CarParams car = vehicles[lane].get(position);
        for (String param : params) {
            if (StringEditor.isInArray(carGeneratedParams, param) || param.equals(RequestConstants.X_POSITION_REQUEST)
                    || param.equals(RequestConstants.CURRENT_SPEED_REQUEST)) { //get directly from car we are inspecting
                parameters.put(param, car.getParameter(param));
            } else if (StringEditor.isInArray(roadSimulationParams, param)) { //get from road/simulation
                this.getRoadSimulationParameter(parameters, param, car);
            } else {   // get parameter from different car in proximity
                this.getParametersAboutDifferentCar(parameters, param, car);
            }
        }
        return parameters;
    }

    private HashMap<String, Double> getParametersLaneChange(CarParams carChangingLane, LinkedList<CarParams>[] road, String requestParameters) {
        HashMap<String, Double> parameters = new HashMap<>();
        String[] params = requestParameters.split(RequestConstants.REQUEST_SEPARATOR);
        if (params.length == 0) {
            MyLogger.log("No parameters requested", Constants.DEBUG_FOR_LOGGING);
            return null;
        }
        String[] carGeneratedParams = this.generator.getCarGenerationParameters();
        String[] roadSimulationParams = {RequestConstants.TIME_STEP_REQUEST, RequestConstants.MAX_ROAD_SPEED_REQUEST};

        CarParams car = this.getCarById(carChangingLane.id, road);
        for (String param : params) {
            if (StringEditor.isInArray(carGeneratedParams, param) || param.equals(RequestConstants.X_POSITION_REQUEST)
                    || param.equals(RequestConstants.CURRENT_SPEED_REQUEST)) { //get directly from car we are inspecting
                parameters.put(param, car.getParameter(param));
            } else if (param.contains("Acceleration")) {
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
                this. getParametersAboutDifferentCarN(parameters, param, car, road);
            }
        }
        return parameters;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    private void getParametersAboutDifferentCarN(HashMap<String, Double> parameters, String param, CarParams car,
                                                 LinkedList<CarParams>[] road) {
        String[] paramSeparate = param.split(RequestConstants.SUBREQUEST_SEPARATOR);
        String wantedParam = paramSeparate[0];
        Orientation orientation = Orientation.valueOf(paramSeparate[2]);

        CarParams otherCar = getCarInProximityN(orientation, car, road);

        if (otherCar != null) {
            parameters.put(param, otherCar.getParameter(wantedParam));
        } else {
            parameters.put(param, Constants.NO_CAR_THERE);
        }
    }


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

    private void resetProcessedFlags() {
        for (int lane = 0; lane < this.numberOfLanes; lane++) {
            for (CarParams car : this.vehicles[lane]) {
                car.processedInCurrentStep = false;
            }
        }
    }

    private CarParams getCarInProximityN(Orientation orientation, CarParams car, LinkedList<CarParams>[] road) {
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

    private Direction tryLaneChange(CarParams car) {
        int lane = car.lane;
        int index = vehicles[lane].indexOf(car);
        Direction direction = Direction.LEFT;
        String requestParameters;
        LinkedList<CarParams>[] fakeRoad;
        HashMap<String, Double> parameters;
        Direction desiredDirection;

        if (lane > 0) {
            requestParameters = AppContext.LANE_CHANGING_MODEL.requestParameters(direction);
            fakeRoad = this.createFakeRoad(direction, car);
            if (fakeRoad != null) {
                parameters = getParametersLaneChange(car, fakeRoad, requestParameters);
                desiredDirection = AppContext.LANE_CHANGING_MODEL.changeLaneIfDesired(parameters, direction);
                if (desiredDirection == Direction.LEFT) {
                    this.placeCarN(car, this.vehicles, Direction.LEFT);
                    MyLogger.log("Car at lane " + lane + " position " + index + " changed lane to LEFT.",
                            Constants.DEBUG_FOR_LOGGING);
                    return Direction.LEFT;
                }
            }
        }

        if (lane < this.numberOfLanes - 1) {
            direction = Direction.RIGHT;
            requestParameters = AppContext.LANE_CHANGING_MODEL.requestParameters(direction);
            fakeRoad = this.createFakeRoad(direction, car);
            if (fakeRoad != null) {
                if (AppContext.SIMULATION.getStepCount() > 100) {
                    int i = 0;
                }
                parameters = getParametersLaneChange(car, fakeRoad, requestParameters);
                desiredDirection = AppContext.LANE_CHANGING_MODEL.changeLaneIfDesired(parameters, direction);
                if (desiredDirection == Direction.RIGHT) {
                    this.placeCarN(car, this.vehicles, Direction.RIGHT);
                    MyLogger.log("Car at lane " + lane + " position " + index + " changed lane to RIGHT.",
                            Constants.DEBUG_FOR_LOGGING);
                    return Direction.RIGHT;
                }
            }
        }

        return Direction.STRAIGHT;

    }

    private double getAccelerationOfDifferentCar(CarParams car, String param, LinkedList<CarParams>[] road) {
        String[] paramSeparate = param.split(RequestConstants.SUBREQUEST_SEPARATOR);
        CarParams carToStudy;

        if (paramSeparate.length >= 3) {
            Direction direction = Direction.valueOf(paramSeparate[1]);
            Orientation orientation = Orientation.valueOf(paramSeparate[2]);
            carToStudy = getCarInProximityN(orientation, car, road);
        } else {
            carToStudy = car;
        }

        if (carToStudy == null) {
            return 0.0;
        }

        String requestParameters = AppContext.CAR_FOLLOWING_MODEL.requestParameters();
        int index = road[carToStudy.lane].indexOf(carToStudy);
        HashMap<String, Double> parameters = getParametersLaneChange(carToStudy, road, requestParameters);
        double newSpeed = AppContext.CAR_FOLLOWING_MODEL.getNewSpeed(parameters);
        double oldSpeed = carToStudy.getParameter(RequestConstants.CURRENT_SPEED_REQUEST);

        return newSpeed - oldSpeed;
    }

    private boolean isPlaceOkInLane(LinkedList<CarParams> lane, int place, CarParams car) {
        if (place < 0) {
            return false;
        }

        if (lane.size() == 0) {
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

            if (car.xPosition >= backXOfCarInFront) {
                return false;
            }
        }

        return true;
    }

    private boolean placeCarN(CarParams car, LinkedList<CarParams>[] road, Direction direction) {
        int lane = car.lane;
        double position = car.xPosition;
        if (direction == Direction.LEFT) {
            lane = lane - 1;
        } else if (direction == Direction.RIGHT) {
            lane = lane + 1;
        }

        if (lane >= 0 && lane < road.length) {
            int place = findPlaceForCarN(position, road[lane]);

            if (!isPlaceOkInLane(road[lane], place, car)) {
                return false;
            }

            road[lane].add(place, car);
            car.lane = lane;
            return true;
        }

        return false;
    }

    private int findPlaceForCarN(double x, LinkedList<CarParams> lane) {
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



    private LinkedList<CarParams>[] createFakeRoad(Direction direction, CarParams car) {
        LinkedList<CarParams>[] fakeRoad = this.copyRoadStructureDeep();
        int lane = car.lane;
        int index = vehicles[lane].indexOf(car);
        CarParams carForFakeRoad = fakeRoad[lane].get(index);
        boolean successfulCreation = this.placeCarN(carForFakeRoad, fakeRoad, direction);
        if (!successfulCreation) {
            return null;
        }
        int id = carForFakeRoad.id;
        carForFakeRoad = this.getCarById(id, fakeRoad);
        fakeRoad[lane].remove(carForFakeRoad);
        return fakeRoad;
    }

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
/////////////////////////////////////////////////////////////////////////////////////////

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

    private CarParams getCarById(int id, LinkedList<CarParams>[] road) {
        for (int lane = 0; lane < road.length; lane++) {
            for (CarParams car : road[lane]) {
                if (car.id == id) {
                    return car;
                }
            }
        }
        return null;
    }

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

    private void checkForCollisions() {
        for (int lane = 0; lane < numberOfLanes; lane++) {
            LinkedList<CarParams> laneList = vehicles[lane];
            for (int i = 0; i < laneList.size() - 1; i++) {
                CarParams carA = laneList.get(i);
                CarParams carB = laneList.get(i + 1);
                double carABack = carA.xPosition - carA.getParameter(RequestConstants.LENGTH_REQUEST);
                if (carB.xPosition < carABack) {
                    MyLogger.log("Collision detected in lane " + lane + " between cars ID: " + carA.id +
                            " and ID: " + carB.id, Constants.ERROR_FOR_LOGGING);
                }
            }
        }
    }


}
