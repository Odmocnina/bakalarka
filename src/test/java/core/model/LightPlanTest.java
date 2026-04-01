package core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**************************************
 * Unit tests for LightPlan class
 *
 * @author Michael Hladky
 * @version 1.0
 **************************************/
public class LightPlanTest {

    /**
     * test to verify that the constructor correctly initializes the fields
     * and sets the initial light state to beginsOnGreen
     **/
    @Test
    void constructorAndGetters_ShouldInitializeCorrectly() {
        // Arrange & Act
        LightPlan plan = new LightPlan(60, 20, true);

        // Assert
        assertEquals(60, plan.getCycleTime(), "Cycle time should be initialized correctly");
        assertEquals(20, plan.getTimeOfSwitch(), "Time of switch should be initialized correctly");
        assertTrue(plan.isBeginsOnGreen(), "Begins on green should be true");
        assertTrue(plan.isGreen(), "Initial state (isGreen) should match beginsOnGreen");
    }

    /**
     * test to verify that the setters correctly update the values of the light plan
     **/
    @Test
    void setters_ShouldUpdateValues() {
        // Arrange
        LightPlan plan = new LightPlan(60, 20, true);

        // Act
        plan.setCycleTime(100);
        plan.setTimeOfSwitch(45);
        plan.setBeginsOnGreen(false);

        // Assert
        assertEquals(100, plan.getCycleTime(), "Cycle time should be updated");
        assertEquals(45, plan.getTimeOfSwitch(), "Time of switch should be updated");
        assertFalse(plan.isBeginsOnGreen(), "Begins on green should be updated");
    }

    /**
     * test to verify that isLegitimate returns true when timeOfSwitch is strictly less than cycleTime
     **/
    @Test
    void isLegitimate_TimeOfSwitchLessThanCycleTime_ShouldReturnTrue() {
        // Arrange
        LightPlan plan = new LightPlan(60, 30, true);

        // Act & Assert
        assertTrue(plan.isLegitimate(), "Plan should be legitimate when timeOfSwitch < cycleTime");
    }

    /**
     * test to verify that isLegitimate returns false when timeOfSwitch is greater than cycleTime
     */
    @Test
    void isLegitimate_TimeOfSwitchGreaterThanCycleTime_ShouldReturnFalse() {
        // Arrange
        LightPlan planGreater = new LightPlan(50, 60, true);

        // Act & Assert
        assertFalse(planGreater.isLegitimate(), "Plan should not be legitimate when timeOfSwitch > cycleTime");
    }

    /**
     * test to verify that isLegitimate returns true when timeOfSwitch is equal to cycleTime and starts on green
     */
    @Test
    void isLegitimate_TimeOfSwitchEqualsCycleTimeAndStartsGreen_ShouldReturnTrue() {
        // Arrange
        LightPlan planEqualStartGreen = new LightPlan(50, 50, true);

        // Act & Assert
        assertTrue(planEqualStartGreen.isLegitimate(), "Plan should be legitimate when timeOfSwitch == cycleTime and starts on green");
    }

    /**
     * test to verify that isLegitimate returns false when timeOfSwitch is equal to cycleTime but starts on red
     */
    @Test
    void isLegitimate_TimeOfSwitchEqualsCycleTimeAndStartsRed_ShouldReturnFalse() {
        // Arrange
        LightPlan planEqualStartRed = new LightPlan(50, 50, false);

        // Act & Assert
        assertFalse(planEqualStartRed.isLegitimate(), "Plan should not be legitimate when timeOfSwitch == cycleTime and starts on red");
    }

    /**
     * test to verify that tryToSwitchLight toggles the light when currentTime is a multiple of timeOfSwitch
     **/
    @Test
    void tryToSwitchLight_MultipleOfTimeOfSwitch_ShouldToggleLight() {
        // Arrange: starts on green (true)
        LightPlan plan = new LightPlan(100, 20, true);

        // Act & Assert: step 20 (multiple of 20) -> should toggle to false
        plan.tryToSwitchLight(20);
        assertFalse(plan.isGreen(), "Light should toggle to red (false) at timeOfSwitch multiple");

        // Act & Assert: step 40 (multiple of 20) -> should toggle back to true
        plan.tryToSwitchLight(40);
        assertTrue(plan.isGreen(), "Light should toggle to green (true) at next timeOfSwitch multiple");
    }

    /**
     * test to verify that tryToSwitchLight resets to beginsOnGreen when currentTime is a multiple of cycleTime
     * (and NOT a multiple of timeOfSwitch, due to the else-if logic)
     **/
    @Test
    void tryToSwitchLight_MultipleOfCycleTime_ShouldResetToBeginsOnGreen() {
        // Arrange: cycle is 50, switch is 20, begins on true
        LightPlan plan = new LightPlan(50, 20, true);

        // Act: Manually set the light to false for verification of the reset behavior
        plan.tryToSwitchLight(20); // toggles to false
        assertFalse(plan.isGreen());

        // Act: Time is 50. It's a multiple of cycleTime (50), but NOT timeOfSwitch (20).
        // This triggers the else-if branch.
        plan.tryToSwitchLight(50);

        // Assert
        assertTrue(plan.isGreen(), "Light should reset to beginsOnGreen at cycleTime multiple");
    }

    /**
     * test to verify that tryToSwitchLight does nothing if the current time matches neither condition
     **/
    @Test
    void tryToSwitchLight_NoMultiple_ShouldNotChangeState() {
        // Arrange
        LightPlan plan = new LightPlan(60, 20, true);

        // Act
        plan.tryToSwitchLight(15);

        // Assert
        assertTrue(plan.isGreen(), "Light state should remain unchanged if time matches neither condition");
    }

    /**
     * test to verify that clone creates a new object with the identical properties
     **/
    @Test
    void clone_ShouldReturnNewInstanceWithSameValues() {
        // Arrange
        LightPlan original = new LightPlan(75, 25, false);

        // Act
        LightPlan cloned = original.clone();

        // Assert
        assertNotSame(original, cloned, "Cloned object should be a different instance in memory");
        assertEquals(original.getCycleTime(), cloned.getCycleTime(), "Cycle times should match");
        assertEquals(original.getTimeOfSwitch(), cloned.getTimeOfSwitch(), "Times of switch should match");
        assertEquals(original.isBeginsOnGreen(), cloned.isBeginsOnGreen(), "BeginsOnGreen should match");
    }

    /**
     * test to verify the reset method restores the light to its initial beginsOnGreen state
     **/
    @Test
    void reset_ShouldRestoreInitialState() {
        // Arrange
        LightPlan plan = new LightPlan(60, 20, false); // starts on red (false)

        // Act
        plan.tryToSwitchLight(20); // toggles to green (true)
        assertTrue(plan.isGreen(), "Light should be green after toggle");

        plan.reset(); // resets back to beginsOnGreen (false)

        // Assert
        assertFalse(plan.isGreen(), "Light should be reset to beginsOnGreen (false)");
    }

    /**
     * test to verify the toString method returns the correctly formatted string
     **/
    @Test
    void toString_ShouldReturnFormattedString() {
        // Arrange
        LightPlan plan = new LightPlan(80, 40, true);

        // Act
        String result = plan.toString();

        // Assert
        assertEquals("LightPlan(cycleTime=80, timeOfSwitch=40, beginsOnGreen=true)", result,
                "toString should return the correctly formatted representation");
    }
}