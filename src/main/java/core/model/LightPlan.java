package core.model;

public class LightPlan {

    private int cycleTime;

    private int timeOfSwitch;

    private boolean beginsOnGreen;
    private boolean isGreen;

    public LightPlan(int cycleTime, int timeOfSwitch, boolean beginsOnGreen) {
        this.cycleTime = cycleTime;
        this.timeOfSwitch = timeOfSwitch;
        this.beginsOnGreen = beginsOnGreen;
        if (beginsOnGreen) {
            this.isGreen = true;
        } else {
            this.isGreen = false;
        }
    }

    public void tryToSwitchLight(int currentTime) {
        if (currentTime % timeOfSwitch == 0) {
            isGreen = !isGreen;
        } else if (currentTime % cycleTime == 0) {
            isGreen = beginsOnGreen;
        }
    }

}
