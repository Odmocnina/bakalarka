package app;

import core.utils.MyLogger;
import core.utils.constants.Constants;
import models.ICarFollowingModel;
import models.ILaneChangingModel;

public class InputParametersHandeler {

    /**
     * Handles input parameters from command line, checks if duration parameter is provided and if yes, sets duration in
     * run details and returns true to indicate that application should be started without gui, otherwise returns false
     * to indicate that application should be started with gui
     *
     * @param args input parameters in command line
     * @return int value of duration if provided, -2 (constant INVALID_INPUT_PARAMETERS) if any of the parameters was invalid
     *          (not number duration, non-existent models or something like that), otherwise -1
     *          (constant NO_DURATION_PROVIDED) to indicate that duration is not provided and app will run in gui mode
     **/
    public static int handleParameters(String[] args) {
        ICarFollowingModel carFollowingModel = null;
        ILaneChangingModel laneChangingModel = null;
        int duration = Constants.NO_DURATION_PROVIDED; // default value indicating duration is not provided
        for (String arg : args) {
            if (arg.startsWith("--d=")) {
                String durationStr = arg.substring("--d=".length());
                try {
                    duration = Integer.parseInt(durationStr);
                    AppContext.RUN_DETAILS.duration = duration;
                    MyLogger.logBeforeLoading("Duration provided: " + duration + " seconds (steps in simulation," +
                            " starting app without GUI.", Constants.INFO_FOR_LOGGING);
                } catch (NumberFormatException e) {
                    MyLogger.logBeforeLoading("Invalid duration value provided in input parameters: " +
                            durationStr + ", exiting.", Constants.FATAL_FOR_LOGGING);
                    return Constants.INVALID_INPUT_PARAMETERS;
                }
            } else if (arg.startsWith("--f=")) {
                String modelName = arg.substring("--carFollowingModel=".length());

                if (carFollowingModel == null) {
                    MyLogger.logBeforeLoading("Invalid car following model provided in input parameters: " +
                            modelName + ", exiting.", Constants.FATAL_FOR_LOGGING);
                    return Constants.INVALID_INPUT_PARAMETERS;
                }
                AppContext.CAR_FOLLOWING_MODEL = carFollowingModel;
                MyLogger.logBeforeLoading("Car following model provided: " + modelName, Constants.INFO_FOR_LOGGING);
            } else if (arg.startsWith("--l=")) {
                String modelName = arg.substring("--laneChangingModel=".length());

                if (laneChangingModel == null) {
                    MyLogger.logBeforeLoading("Invalid lane changing model provided in input parameters: " +
                            modelName + ", exiting.", Constants.FATAL_FOR_LOGGING);
                    return Constants.INVALID_INPUT_PARAMETERS;
                }
                AppContext.LANE_CHANGING_MODEL = laneChangingModel;
                MyLogger.logBeforeLoading("Lane changing model provided: " + modelName, Constants.INFO_FOR_LOGGING);
            } else {
                MyLogger.logBeforeLoading("Unknown input parameter provided: " + arg + ", exiting.", Constants.FATAL_FOR_LOGGING);
                return Constants.INVALID_INPUT_PARAMETERS;
            }
        }

        if (carFollowingModel == null) {
            MyLogger.logBeforeLoading("No car following model provided, using default model in config: " +
                    AppContext.CAR_FOLLOWING_MODEL.getName(), Constants.WARN_FOR_LOGGING);
        }

        if (laneChangingModel == null) {
            MyLogger.logBeforeLoading("No lane changing model provided, using default model in config: " +
                    AppContext.LANE_CHANGING_MODEL.getName(), Constants.WARN_FOR_LOGGING);
        }

        if (duration == Constants.NO_DURATION_PROVIDED) {
            MyLogger.logBeforeLoading("No duration provided, app will be started with GUI", Constants.WARN_FOR_LOGGING);
        }

        return duration;
    }
}
