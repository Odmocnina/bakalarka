package core.utils;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.utils.constants.DefaultValues;
import core.utils.constants.RequestConstants;

import java.util.LinkedList;

/***************************************
 * Class responsible for creating default objects of various types, for example default light plans or default car
 * generators, this is used when new roads are created or all roads/lanes are being changed at the same time, usually
 * uses values form DefaultValues class for the default parameters of the created objects
 *
 * @author Michael Hladky
 * @version 1.0
 ***************************************/
public class DefaultStuffMaker {

    /**
     * create default light plan
     *
     * @return default light plan
     **/
    public static LightPlan createDefaultLightPlan() {
        return new LightPlan(DefaultValues.DEFAULT_LIGHT_PLAN_CYCLE_DURATION,
                DefaultValues.DEFAULT_LIGHT_PLAN_GREEN_DURATION,
                DefaultValues.DEFAULT_LIGHT_PLAN_START_WITH_GREEN);
    }

    /**
     * create default light plan linked list
     *
     * @param numberOfLanes number of lanes
     * @return default light plan linked list
     **/
    public static LinkedList<LightPlan> createDefaultLightPlan(int numberOfLanes) {
        LinkedList<LightPlan> lightPlan = new LinkedList<>();
        for (int i = 0; i < numberOfLanes; i++) {
            lightPlan.add(createDefaultLightPlan());
        }

        return lightPlan;
    }

    /**
     * create default car generator
     *
     * @return default car generator
     **/
    public static CarGenerator createDefaultGenerator() {
        CarGenerator generator = new CarGenerator(DefaultValues.DEFAULT_FLOW_RATE);
        generator.setQueueSize(DefaultValues.DEFAULT_QUEUE_MIN_SIZE, DefaultValues.DEFAULT_QUEUE_MAX_SIZE);
        generator.disableQueue();
        generator.addComParameter(RequestConstants.MAX_SPEED_REQUEST, "Max speed", DefaultValues.DEFAULT_MAX_SPEED_MIN, DefaultValues.DEFAULT_MAX_SPEED_MAX);
        generator.addComParameter(RequestConstants.LENGTH_REQUEST, "Length of vehicle",DefaultValues.DEFAULT_LENGTH_MIN, DefaultValues.DEFAULT_LENGTH_MAX);
        generator.addComParameter(RequestConstants.MAX_ACCELERATION_REQUEST, "Max acceleration",DefaultValues.DEFAULT_ACCELERATION_MIN, DefaultValues.DEFAULT_ACCELERATION_MAX);
        generator.addComParameter(RequestConstants.MINIMUM_GAP_TO_NEXT_CAR_REQUEST, "Minimum gap to the next car", DefaultValues.DEFAULT_MINIMUM_GAP_TO_NEXT_CAR_MIN,
                DefaultValues.DEFAULT_MINIMUM_GAP_TO_NEXT_CAR_MAX);
        generator.addComParameter(RequestConstants.DECELERATION_COMFORT_REQUEST, "Max conferable deceleration", DefaultValues.DEFAULT_DECELERATION_COMFORT_MIN,
                DefaultValues.DEFAULT_DECELERATION_COMFORT_MAX);
        generator.addComParameter(RequestConstants.DESIRED_TIME_HEADWAY_REQUEST, "Desired time headway", DefaultValues.DEFAULT_DESIRED_TIME_HEADWAY_MIN,
                DefaultValues.DEFAULT_DESIRED_TIME_HEADWAY_MAX);
        generator.addComParameter(RequestConstants.SPEED_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, "Speed difference sensitivity",
                DefaultValues.DEFAULT_SPEED_DIFFERENCE_SENSITIVITY_MIN, DefaultValues.DEFAULT_SPEED_DIFFERENCE_SENSITIVITY_MAX);
        generator.addComParameter(RequestConstants.DISTANCE_DIFFERENCE_SENSITIVITY_PARAMETER_REQUEST, "Distance difference sensitivity",
                DefaultValues.DEFAULT_DISTANCE_DIFFERENCE_SENSITIVITY_MIN, DefaultValues.DEFAULT_DISTANCE_DIFFERENCE_SENSITIVITY_MAX);
        generator.addComParameter(RequestConstants.EDGE_VALUE_FOR_LANE_CHANGE_REQUEST, "Edge value for lane change",
                DefaultValues.DEFAULT_EDGE_VALUE_FOR_LANE_CHANGE_MIN, DefaultValues.DEFAULT_EDGE_VALUE_FOR_LANE_CHANGE_MAX);
        generator.addComParameter(RequestConstants.POLITENESS_FACTOR_REQUEST, "Politeness factor", DefaultValues.POLITENESS_FACTOR_MIN,
                DefaultValues.POLITENESS_FACTOR_MAX);
        String neededParameters = StringEditor.mergeRequestParameters(AppContext.CAR_FOLLOWING_MODEL.getParametersForGeneration(),
                AppContext.LANE_CHANGING_MODEL.getParametersForGeneration());
        generator.setCarGenerationParameters(neededParameters);

        generator.copyComParametersToRealParameters(AppContext.CAR_FOLLOWING_MODEL.getType(), AppContext.CAR_FOLLOWING_MODEL.getCellSize());
        return generator;
    }

    /**
     * create multiple default car generators in linked list
     *
     * @param numberOfLanes number of lanes on the road
     * @return linked list of default car generators for lanes on road
     **/
    public static LinkedList<CarGenerator> createDefaultGenerator(int numberOfLanes) {
        LinkedList<CarGenerator> generators = new LinkedList<>();
        for (int i = 0; i < numberOfLanes; i++) {
            generators.add(createDefaultGenerator());
        }
        return generators;
    }
}
