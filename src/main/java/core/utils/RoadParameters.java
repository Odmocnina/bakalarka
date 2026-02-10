package core.utils;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.model.Road;
import core.model.cellular.CellularRoad;
import core.model.continous.ContinuosRoad;
import core.utils.constants.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/******************************************************
 * Class representing the parameters of a road, this is used for saving/loading roads and for changing all roads/lanes
 * at the same time in gui, it has methods for converting existing Road objects to RoadParameters and for converting
 * RoadParameters back to Road objects, used for communication with user in gui
 *
 * @author Michael Hladky
 * @version 1.0
 ******************************************************/
public class RoadParameters {

    /** max allowed speed on the road **/
    public double maxSpeed;

    /** length of the road **/
    public double length;

    /** number of lanes on the road **/
    public int lanes;

    /** light plan for the road, a linked list of LightPlan objects, one for each lane (first light plan in linked list
        is for fist lane **/
    public LinkedList<LightPlan> lightPlan;

    /** car generators for the road, a linked list of CarGenerator objects, one for each lane (first generator in linked list
     * is for fist lane, if there are more generators than lanes, the extra generators are ignored, if there are
     * fewer generators, but if that happens, something is seriously fucked **/
    public LinkedList<CarGenerator> carGenerators;

    /**
     * method that changes array of roads to array list of road parameters, it iterates through the given array of roads
     * and for each non-null road, it creates a new RoadParameters object and fills its fields with the parameters of
     * the road, then it adds the created RoadParameters to the output array list
     *
     * @param roads array of roads to be converted to array list of road parameters
     * @return array list of road parameters created from the given array of roads
     **/
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

    /**
     * method that changes array list of road parameters to array of roads, it iterates through the given array list of
     * road parameters and for each non-null road parameter, it creates a new Road object and fills its fields with the
     * parameters from the road parameter, then it adds the created Road to the output array of roads, used when changes
     * are done the gui and the changed roads need to be converted back to Road objects for the simulation
     *
     * @param roadParametersList array list of road parameters to be converted to array of roads
     * @return array of roads created from the given array list of road parameters
     **/
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
                } else if (type.equals(Constants.CONTINUOUS)) {
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
