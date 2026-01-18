package core.utils;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.model.Road;
import core.model.cellular.CellularRoad;
import core.model.continous.ContinuosRoad;
import core.utils.constants.Constants;
import javafx.scene.effect.Light;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class RoadParameters {
    public double maxSpeed;
    public double length;
    public int lanes;
    public LinkedList<LightPlan> lightPlan;
    public LinkedList<CarGenerator> carGenerators;

    public static ArrayList<RoadParameters> existingRoadsToRoadParameters(Road[] roads) {
        ArrayList<RoadParameters> roadParametersList = new ArrayList<>();
        for (Road road : roads) {
            if (road != null) {
                RoadParameters rp = new RoadParameters();
                rp.maxSpeed = road.getSpeedLimit();
                rp.length = road.getLength();
                rp.lanes = road.getNumberOfLanes();
                LightPlan[] lpInArray = road.getLightPlans();
                rp.lightPlan = new LinkedList<>();
                rp.lightPlan.addAll(Arrays.asList(lpInArray));
                CarGenerator[] cgInArray = road.getCarGenerators();
                rp.carGenerators = new LinkedList<>();
                rp.carGenerators.addAll(Arrays.asList(cgInArray));
                roadParametersList.add(rp);
            }
        }

        return roadParametersList;
    }

    public static Road[] roadParametersToRoads(ArrayList<RoadParameters> roadParametersList) {
        String type = AppContext.CAR_FOLLOWING_MODEL.getType();
        double cellSize = -1;
        if (type.equals(Constants.CELLULAR)) {
            cellSize = AppContext.CAR_FOLLOWING_MODEL.getCellSize();
        }
        String requestedParameters = StringEditor.mergeRequestParameters(
                AppContext.CAR_FOLLOWING_MODEL.getParametersForGeneration(),
                AppContext.LANE_CHANGING_MODEL.getParametersForGeneration());
        Road[] roads = new Road[roadParametersList.size()];
        int i = 0;
        for (RoadParameters rp : roadParametersList) {
            if (rp != null) {
                Road road;
                if (type.equals(Constants.CELLULAR)) {
                    road = new CellularRoad(rp.length, rp.lanes, rp.maxSpeed, cellSize);
                } else if (type.equals(Constants.CONTINOUS)) {
                    road = new ContinuosRoad(rp.length, rp.lanes, rp.maxSpeed);
                } else {
                    MyLogger.log("Unknown car following model type: " + type + ". Cannot create road.",
                            Constants.ERROR_FOR_LOGGING);
                    continue;
                }
                if (rp.lightPlan != null) {
                    road.setLightPlan(rp.lightPlan.toArray(new LightPlan[0]));
                }
                if (rp.carGenerators != null) {
                    CarGenerator[] generatorsInArray = new CarGenerator[rp.carGenerators.size()];
                    for (int j = 0; j < rp.carGenerators.size(); j++) {
                        generatorsInArray[j] = rp.carGenerators.get(j);
                    }
                    road.setCarGenerators(generatorsInArray);
                }
                road.setTypeAndParametersOfGenerators(requestedParameters);
                roads[i] = road;
                i++;
            }
        }
        return roads;
    }

}
