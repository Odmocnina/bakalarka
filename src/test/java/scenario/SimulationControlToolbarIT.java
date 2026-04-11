package scenario;

import javafx.scene.control.ButtonBase;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import org.mockito.MockedStatic;
import ui.Actions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import javafx.scene.control.ToggleButton;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SimulationControlToolbarIT extends BaseWindowIT {

    // TEST: Check that when map is loaded, the main control buttons (start, reset, next step) are enabled, and when map
    // is not loaded, they are disabled. The rule toggles should be enabled regardless of map state.
    @Test
    public void testButtonsStateWhenMapIsLoaded() {
        // check that the buttons are enabled when the map is loaded
        setMapLoadedState(true);

        // find the buttons by their tooltips
        ButtonBase startBtn = getButtonByTooltip("Start/Stop simulation");
        ButtonBase resetBtn = getButtonByTooltip("Reset simulation");
        ButtonBase nextStepBtn = getButtonByTooltip("Next simulation step");
        ButtonBase laneChangeBtn = getButtonByTooltip("Toggle lane change ban");
        ButtonBase collisionBtn = getButtonByTooltip("Ban collisions (toggle)");
        ButtonBase timeBetweenStepsBtn = getButtonByTooltip("Set time between simulation steps (ms)");

        // check that the main control buttons are enabled when the map is loaded, and the rule toggles are always enabled
        assertFalse(startBtn.isDisabled(), "Start should be enabled when map is loaded.");
        assertFalse(resetBtn.isDisabled(), "Reset should be enabled when map is loaded.");
        assertFalse(nextStepBtn.isDisabled(), "Next Step should be enabled when map is loaded.");
        assertFalse(laneChangeBtn.isDisabled(), "Lane change ban should be enabled always.");
        assertFalse(collisionBtn.isDisabled(), "Collision ban should be enabled always.");
        assertFalse(timeBetweenStepsBtn.isDisabled(), "Time between steps should be enabled always.");

        // simulate clicking the lane change toggle to ensure it works even when the map is loaded
        clickOn(laneChangeBtn);
        WaitForAsyncUtils.waitForFxEvents();
    }

    // TEST: Check that when map is not loaded, the main control buttons (start, reset, next step) are disabled, but the
    // rule toggles remain enabled.
    @Test
    public void testButtonsStateWhenMapIsNotLoaded() {
        // Změníme stav - "odstraníme" mapu
        setMapLoadedState(false);

        // find the buttons by their tooltips
        ButtonBase startBtn = getButtonByTooltip("Start/Stop simulation");
        ButtonBase resetBtn = getButtonByTooltip("Reset simulation");
        ButtonBase nextStepBtn = getButtonByTooltip("Next simulation step");
        ButtonBase laneChangeBtn = getButtonByTooltip("Toggle lane change ban");
        ButtonBase collisionBtn = getButtonByTooltip("Ban collisions (toggle)");
        ButtonBase timeBetweenStepsBtn = getButtonByTooltip("Set time between simulation steps (ms)");

        // CHECK that the main control buttons are disabled when the map is not loaded, but the rule toggles remain enabled
        assertTrue(startBtn.isDisabled(), "Start should be disabled without a map.");
        assertTrue(resetBtn.isDisabled(), "Reset should be disabled without a map.");
        assertTrue(nextStepBtn.isDisabled(), "Next Step should be disabled without a map.");

        // Rule toggles should still be enabled even without a map, because we want to allow users to set them before loading a map.
        assertFalse(laneChangeBtn.isDisabled(), "Lane change should be enabled even without a map.");
        assertFalse(collisionBtn.isDisabled(), "Collision ban should be enabled even without a map.");
        assertFalse(timeBetweenStepsBtn.isDisabled(), "Time between steps should be enabled even without a map.");
    }

    // Test: When "Next simulation step" is clicked and map is loaded, it should call the nextStepAction in Actions
    // class with the current simulation and a Runnable callback.
    @Test
    public void testNextStepEffect_ShouldCallNextStepAction() {
        setMapLoadedState(true);
        when(mockEngine.getRunning()).thenReturn(false);

        ButtonBase nextBtn = getButtonByTooltip("Next simulation step");

        // interact() forces the code to run on the JavaFX Application Thread, which is necessary for interacting with
        // UI components and also ensures that our Mockito static mock works correctly when the button's action is
        // executed on the same thread.
        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {

                // Simulate clicking the "Next simulation step" button
                nextBtn.fire();

                actionsMock.verify(() -> Actions.nextStepAction(eq(mockSimulation), any(Runnable.class)), times(1));
            }
        });
    }

    // Test: When "Reset simulation" is clicked while the engine is running and the simulation is at step 0, it should
    // first call the engine's stop method to stop the simulation, then call the resetSimulationAction in the Actions
    // class with the current simulation and a Runnable callback, and finally start the engine again.
    @Test
    public void testResetEffect_WhenRunning_ShouldStopEngineAndReset() {
        // mock so that the engine is running and the simulation is at step 0, which are the conditions for the reset button to be enabled
        setMapLoadedState(true);
        when(mockEngine.getRunning()).thenReturn(true);
        when(mockSimulation.getStepCount()).thenReturn(0);

        // find the reset button by its tooltip
        ButtonBase resetBtn = getButtonByTooltip("Reset simulation");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {

                // click the reset button
                resetBtn.fire();

                // verify that the engine's stop method was called to stop the simulation before resetting
                verify(mockEngine, times(1)).stop();
                actionsMock.verify(() -> Actions.resetSimulationAction(eq(mockSimulation), any(Runnable.class)), times(1));
                verify(mockEngine, times(1)).start();
            }
        });
    }

    // Test: When the lane change ban toggle is clicked, it should toggle the button's selected state and call the
    // appropriate action in the Actions class with the current simulation and a Runnable callback.
    @Test
    public void testToggleLaneChangeBanEffect_ShouldTogglePropertyAndCallAction() {
        setMapLoadedState(false);

        // get the lane change toggle button
        ToggleButton laneBtn = (ToggleButton) getButtonByTooltip("Toggle lane change ban");

        // get the initial state of the toggle (selected or not)
        boolean initialState = laneBtn.isSelected();

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {

                // click the toggle button to change its state
                laneBtn.fire();

                // check that the button's selected state has toggled
                assertEquals(!initialState, laneBtn.isSelected(), "Button did not toggle to the opposite state!");

                // verify that the appropriate action was called with the current simulation and a Runnable callback
                actionsMock.verify(() -> Actions.changeLaneChangingAction(eq(mockSimulation), any(Runnable.class)), times(1));
            }
        });
    }

    // Test: When the collision ban toggle is clicked, it should toggle the button's selected state and call the
    @Test
    public void testToggleCollisionBanEffect_ShouldTogglePropertyAndCallAction() {
        // collision ban toggle should work regardless of whether the map is loaded or not, so we can test it in the
        // "map not loaded" state to ensure it works in that scenario as well
        setMapLoadedState(false);

        // get the collision ban toggle button
        ToggleButton collisionBtn = (ToggleButton) getButtonByTooltip("Ban collisions (toggle)");

        // get the initial state of the toggle (selected or not)
        boolean initialState = collisionBtn.isSelected();

        // Using interact() to ensure that the button's action is executed on the JavaFX Application Thread, which is
        // necessary for the Mockito static mock to work correctly when the button's action is executed on the same thread.
        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {

                // Simulate clicking the collision ban toggle button to change its state
                collisionBtn.fire();

                // check that the button's selected state has toggled
                assertEquals(!initialState, collisionBtn.isSelected(), "Tlačítko pro zákaz kolizí se nepřepnulo!");

                // check that the appropriate action was called with the current simulation and a Runnable callback
                actionsMock.verify(() -> Actions.collisionBanAction(eq(mockSimulation), any(Runnable.class)), times(1));
            }
        });
    }

    // Test: When the "Set time between simulation steps (ms)" button is clicked, it should call the setTimeBetweenStepsAction
    @Test
    public void testSetTimeBetweenStepsEffect_ShouldBeAlwaysEnabledAndCallAction() {
        // set the map loaded state false to ensure that the button is enabled even without a map
        setMapLoadedState(false);

        // find the button by its tooltip
        ButtonBase timeBtn = getButtonByTooltip("Set time between simulation steps (ms)");

        // CHECK that the button is enabled even without a map, because we want to allow users to set the time between steps before loading a map.
        assertFalse(timeBtn.isDisabled(), "Tlačítko pro nastavení času by mělo být vždy aktivní, i bez mapy.");

        // interact() is used to ensure that the button's action is executed on the JavaFX Application Thread, which is
        // necessary for the Mockito static mock to work correctly when the button's action is executed on the same thread.
        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {

                // click the button to trigger the action
                timeBtn.fire();

                // verify that the setTimeBetweenStepsAction was called with the primary stage and the current simulation engine
                actionsMock.verify(() -> Actions.setTimeBetweenStepsAction(eq(primaryStage), eq(mockEngine)), times(1));
            }
        });
    }

    // Test: When "Start/Stop simulation" is clicked and engine is NOT running, it should call engine.start()
    @Test
    public void testStartStopEffect_WhenStopped_ShouldStartEngine() {
        // map must be loaded for the button to be enabled
        setMapLoadedState(true);

        // set the mock to pretend that the engine is not running
        when(mockEngine.getRunning()).thenReturn(false);

        ButtonBase startBtn = getButtonByTooltip("Start/Stop simulation");

        interact(() -> {
            // click the button (which should now function as Start)
            startBtn.fire();

            // VERIFY: Check that the start() method was called on our engine
            verify(mockEngine, times(1)).start();
            // Also verify that stop() was never called, since we were starting the simulation, not stopping it
            verify(mockEngine, never()).stop();
        });
    }

    // Test: When "Start/Stop simulation" is clicked and engine IS running, it should call engine.stop()
    @Test
    public void testStartStopEffect_WhenRunning_ShouldStopEngine() {
        // map must be loaded for the button to be enabled
        setMapLoadedState(true);

        // set the mock to pretend that the engine is running
        when(mockEngine.getRunning()).thenReturn(true);

        ButtonBase startBtn = getButtonByTooltip("Start/Stop simulation");

        interact(() -> {
            // click the button (which should now function as Stop)
            startBtn.fire();

            // VERIFY: Check that the stop() method was called on our engine
            verify(mockEngine, times(1)).stop();
            // Also verify that start() was never called, since we were stopping the simulation, not starting it
            verify(mockEngine, never()).start();
        });
    }
}