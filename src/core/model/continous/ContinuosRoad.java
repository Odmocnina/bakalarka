package core.model.continous;

import app.AppContext;
import core.model.CarGenerator;
import core.model.CarParams;
import core.model.Road;
import core.utils.Constants;
import core.utils.ResultsRecorder;

import java.util.HashMap;
import java.util.LinkedList;

public class ContinuosRoad extends Road {

    LinkedList<CarParams>[] vehicles;

    public ContinuosRoad(double length, int numberOfLanes, double speedLimit, CarGenerator generator) {
        super(length, numberOfLanes, speedLimit, Constants.CONTINOUS, generator);
        createRoad();
    }

    private void createRoad() {
        this.vehicles = new LinkedList[numberOfLanes];
        for (int lane = 0; lane < numberOfLanes; lane++) {
            this.vehicles[lane] = new LinkedList<>();
        }

        // For testing purposes, add some cars to the road, DELETE LATER
        vehicles[0].add(new CarParams() {{
            currentSpeed = 0;
            maxSpeed = 33.33; // 120 km/h
            xPosition = 0;
            lane = 0;
            length = 4.5;
            maxAcceleration = 2.0;
            minGapToNextCar = 2.0;
            maxConfortableDeceleration = 4.5;
            desiredTimeHeadway = 1.5;
        }});
        vehicles[0].add(new CarParams() {{
            currentSpeed = 0;
            maxSpeed = 10; // 120 km/h
            xPosition = 50;
            lane = 0;
            length = 4.5;
            maxAcceleration = 2.0;
            minGapToNextCar = 2.0;
            maxConfortableDeceleration = 4.5;
            desiredTimeHeadway = 1.5;
        }});
    }

    @Override
    public void upadateRoad() {
        for (int lane = 0; lane < numberOfLanes; lane++) {
            if (generator.decideIfNewCar()) {
                CarParams newCar = generator.generateCar();

                if (okToPutCar(newCar, lane)) {
                    placeCar(newCar, 0, lane);
                }
            }
        }
        for (int lane = this.numberOfLanes - 1; lane >= 0; lane--) {
            this.updateLane(lane);
        }
    }

    private void updateLane(int lane) {
        for (CarParams car : this.vehicles[lane]) {
            String requestParameters = AppContext.CAR_FOLLOWING_MODEL.requestParameters();
            HashMap<String, Double> parameters = getParameters(lane, this.vehicles[lane].indexOf(car), requestParameters);
            if (parameters == null) {
                System.out.println("Error getting parameters for car at lane " + lane + ", position " +
                        this.vehicles[lane].indexOf(car));
                continue;
            }
            double newSpeed = AppContext.CAR_FOLLOWING_MODEL.getNewSpeed(parameters);
            car.currentSpeed = newSpeed;
            car.xPosition += newSpeed;

            checkIfCarStillRelevant(car, lane);
        }
    }

    private HashMap<String, Double> getParameters(int lane, int position, String requestParameters) {
        HashMap<String, Double> parameters = new HashMap<>();
        String[] params = requestParameters.split(Constants.REQUEST_SEPARATOR);
        for (String param : params) {
            switch (param) {
                case Constants.CURRENT_SPEED_REQUEST:
                    parameters.put(Constants.CURRENT_SPEED_REQUEST, vehicles[lane].get(position).currentSpeed);
                    break;

                case Constants.MAX_SPEED_REQUEST:
                    parameters.put(Constants.MAX_SPEED_REQUEST, vehicles[lane].get(position).maxSpeed);
                    break;

                case Constants.DISTANCE_TO_NEXT_CAR_REQUEST:
                    parameters.put(Constants.DISTANCE_TO_NEXT_CAR_REQUEST, getDistanceToNextCar(lane, position));
                    break;

                case Constants.SPEED_DIFFERENCE_TO_NEXT_CAR_REQUEST:
                    parameters.put(Constants.SPEED_DIFFERENCE_TO_NEXT_CAR_REQUEST,
                            getSpeedDifferenceToNextCar(lane, position));
                    break;

                case Constants.MAX_ACCELERATION_REQUEST:
                    parameters.put(Constants.MAX_ACCELERATION_REQUEST, vehicles[lane].get(position).maxAcceleration);
                    break;

                case Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST:
                    parameters.put(Constants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST,
                            vehicles[lane].get(position).minGapToNextCar);
                    break;

                case Constants.DECELERATION_COMFORT_REQUEST:
                    parameters.put(Constants.DECELERATION_COMFORT_REQUEST,
                            vehicles[lane].get(position).maxConfortableDeceleration);
                    break;

                case Constants.DESIRED_TIME_HEADWAY_REQUEST:
                    parameters.put(Constants.DESIRED_TIME_HEADWAY_REQUEST,
                            vehicles[lane].get(position).desiredTimeHeadway);
                    break;

                default:
                    System.out.println("Unknown parameter requested: " + param);
                    return null;
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
                - vehicles[lane].get(position).length;
        return distance;
    }

    private double getSpeedDifferenceToNextCar(int lane, int position) {
        if (position >= vehicles[lane].size() - 1) {
            return 0.0; // No car in front
        }
        return Math.abs(vehicles[lane].get(position).currentSpeed - vehicles[lane].get(position + 1).currentSpeed);
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
        if (newCar.length + newCar.minGapToNextCar <= firstCar.xPosition) {
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
        if ((car.xPosition - car.length) > this.length) {
            removeCar(car, lane);
            return false;
        }

        return true;
    }

    private void removeCar(CarParams car, int lane) {
        vehicles[lane].remove(car);
        ResultsRecorder.getResultsRecorder().recordCarPassed(lane);
    }


}
