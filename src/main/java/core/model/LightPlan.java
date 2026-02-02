package core.model;

/********************************************
 * Class representing a traffic light plan
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class LightPlan {

    /** cycle time of the traffic light **/
    private int cycleTime;

    /** time of switch from green to red and vice versa **/
    private int timeOfSwitch;

    /** whether the light begins on green or red **/
    private boolean beginsOnGreen;

    /** current state of the light, true if green **/
    private boolean isGreen;

    /** constructor for light plan **/
    public LightPlan(int cycleTime, int timeOfSwitch, boolean beginsOnGreen) {
        this.cycleTime = cycleTime;
        this.timeOfSwitch = timeOfSwitch;
        this.beginsOnGreen = beginsOnGreen;
        this.isGreen = beginsOnGreen;
    }

    /**
     * method to try to switch the light based on the current time
     *
     * @param currentTime current time in the simulation
     */
    public void tryToSwitchLight(int currentTime) {
        if (currentTime % timeOfSwitch == 0) {
            isGreen = !isGreen;
        } else if (currentTime % cycleTime == 0) {
            isGreen = beginsOnGreen;
        }
    }

    /**
     * method to check if the light plan is legitimate
     *
     * @return boolean whether the light plan is legitimate
     */
    public boolean isLegitimate() {
        return timeOfSwitch < cycleTime;
    }

    /**
     * method to check if the light is green
     *
     * @return boolean whether the light is green
     */
    public boolean isGreen() {
        return isGreen;
    }

    /**
     * method to get the cycle time
     *
     * @return int cycle time
     */
    public int getCycleTime() {
        return cycleTime;
    }

    /**
     * method to get the time of switch
     *
     * @return int time of switch
     */
    public int getTimeOfSwitch() {
        return timeOfSwitch;
    }

    /**
     * method to check if the light begins on green
     *
     * @return boolean whether the light begins on green
     */
    public boolean isBeginsOnGreen() {
        return beginsOnGreen;
    }

    /**
     * Setter for beginsOnGreen
     *
     * @param beginsOnGreen whether the light begins on green
     */
    public void setBeginsOnGreen(boolean beginsOnGreen) {
        this.beginsOnGreen = beginsOnGreen;
    }

    /**
     * Setter for cycleTime
     *
     * @param cycleTime cycle time of the traffic light
     */
    public void setCycleTime(int cycleTime) {
        this.cycleTime = cycleTime;
    }

    /**
     * Setter for timeOfSwitch
     *
     * @param timeOfSwitch time of switch from green to red and vice versa
     */
    public void setTimeOfSwitch(int timeOfSwitch) {
        this.timeOfSwitch = timeOfSwitch;
    }

    public LightPlan clone() {
        return new LightPlan(cycleTime, timeOfSwitch, beginsOnGreen);
    }

    public String toString() {
        return "LightPlan(cycleTime=" + cycleTime + ", timeOfSwitch=" + timeOfSwitch + ", beginsOnGreen=" + beginsOnGreen + ")";
    }

}
