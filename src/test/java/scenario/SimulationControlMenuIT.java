package scenario;

import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ui.Actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*********************************************
 * Class containing integration tests for simulation control items in the top MenuBar of the Window class.
 *
 * @author Michael Hladky
 * @version 1.0
 *********************************************/
public class SimulationControlMenuIT extends BaseWindowIT {

    // --- TESTS IF MENU ITEMS ARE ENABLED/DISABLED CORRECTLY ---

    // Test: map is loaded -> all simulation menu items should be enabled
    @Test
    public void testSimulationMenuItemsStateWhenMapIsLoaded() {
        setMapLoadedState(true);

        MenuItem startItem = getMenuItemByText("Start/Stop simulation");
        MenuItem nextStepItem = getMenuItemByText("Next simulation step");
        MenuItem resetItem = getMenuItemByText("Reset simulation");
        MenuItem laneChangeItem = getMenuItemByText("Toggle lane change ban");
        MenuItem collisionBanItem = getMenuItemByText("Ban collisions (toggle)");
        MenuItem timeItem = getMenuItemByText("Set time between simulation steps (ms)");

        // verify all are enabled
        assertFalse(startItem.isDisable(), "Start/Stop item should be enabled when map is loaded.");
        assertFalse(nextStepItem.isDisable(), "Next Step item should be enabled when map is loaded.");
        assertFalse(resetItem.isDisable(), "Reset item should be enabled when map is loaded.");
        assertFalse(laneChangeItem.isDisable(), "Lane change ban item should be enabled always.");
        assertFalse(collisionBanItem.isDisable(), "Collision ban item should be enabled always.");
        assertFalse(timeItem.isDisable(), "Time between steps item should be enabled always.");
    }

    // Test: map is not loaded -> main control items should be disabled, rule toggles remain enabled
    @Test
    public void testSimulationMenuItemsStateWhenMapIsNotLoaded() {
        setMapLoadedState(false);

        MenuItem startItem = getMenuItemByText("Start/Stop simulation");
        MenuItem nextStepItem = getMenuItemByText("Next simulation step");
        MenuItem resetItem = getMenuItemByText("Reset simulation");
        MenuItem laneChangeItem = getMenuItemByText("Toggle lane change ban");
        MenuItem collisionBanItem = getMenuItemByText("Ban collisions (toggle)");
        MenuItem timeItem = getMenuItemByText("Set time between simulation steps (ms)");

        // verify core controls are disabled
        assertTrue(startItem.isDisable(), "Start/Stop item should be disabled without map.");
        assertTrue(nextStepItem.isDisable(), "Next Step item should be disabled without map.");
        assertTrue(resetItem.isDisable(), "Reset item should be disabled without map.");

        // verify settings remain enabled
        assertFalse(laneChangeItem.isDisable(), "Lane change ban item should be enabled without map.");
        assertFalse(collisionBanItem.isDisable(), "Collision ban item should be enabled without map.");
        assertFalse(timeItem.isDisable(), "Time between steps item should be enabled without map.");
    }

    // --- TEST OF EFFECTS (calling of actions methods) ---

    // Test: Next simulation step item fires correctly
    @Test
    public void testNextStepMenuEffect_ShouldCallNextStepAction() {
        setMapLoadedState(true);
        when(mockEngine.getRunning()).thenReturn(false);

        MenuItem nextStepItem = getMenuItemByText("Next simulation step");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                nextStepItem.fire();
                actionsMock.verify(() -> Actions.nextStepAction(eq(mockSimulation), any(Runnable.class)), times(1));
            }
        });
    }

    // Test: Reset simulation item fires correctly
    @Test
    public void testResetMenuEffect_WhenRunning_ShouldStopEngineAndReset() {
        setMapLoadedState(true);
        when(mockEngine.getRunning()).thenReturn(true);
        when(mockSimulation.getStepCount()).thenReturn(0);

        MenuItem resetItem = getMenuItemByText("Reset simulation");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                resetItem.fire();

                verify(mockEngine, times(1)).stop();
                actionsMock.verify(() -> Actions.resetSimulationAction(eq(mockSimulation), any(Runnable.class)), times(1));
                verify(mockEngine, times(1)).start();
            }
        });
    }

    // Test: Toggle lane change ban CheckMenuItem fires and updates boolean state
    @Test
    public void testToggleLaneChangeMenuEffect_ShouldTogglePropertyAndCallAction() {
        setMapLoadedState(false);
        CheckMenuItem laneChangeItem = (CheckMenuItem) getMenuItemByText("Toggle lane change ban");
        boolean initialState = laneChangeItem.isSelected();

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                // insetd of fire we use setSelected to toggle the state of the CheckMenuItem, which will trigger the
                // action without relying on TestFX's click simulation which can be flaky with menu items, so I heard
                laneChangeItem.setSelected(!initialState);

                assertEquals(!initialState, laneChangeItem.isSelected(), "Menu item did not toggle its state!");
                actionsMock.verify(() -> Actions.changeLaneChangingAction(eq(mockSimulation), any(Runnable.class)), times(1));
            }
        });
    }

    // Test: Ban collisions CheckMenuItem fires and updates boolean state
    @Test
    public void testToggleCollisionMenuEffect_ShouldTogglePropertyAndCallAction() {
        setMapLoadedState(false);
        CheckMenuItem collisionItem = (CheckMenuItem) getMenuItemByText("Ban collisions (toggle)");
        boolean initialState = collisionItem.isSelected();

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                // insetd of fire we use setSelected to toggle the state of the CheckMenuItem, which will trigger the
                // action without relying on TestFX's click simulation which can be flaky with menu items, so I heard
                collisionItem.setSelected(!initialState);

                assertEquals(!initialState, collisionItem.isSelected(), "Menu item did not toggle its state!");
                actionsMock.verify(() -> Actions.collisionBanAction(eq(mockSimulation), any(Runnable.class)), times(1));
            }
        });
    }

    // Test: Set time between steps item fires correctly
    @Test
    public void testSetTimeBetweenStepsMenuEffect_ShouldCallAction() {
        setMapLoadedState(false);
        MenuItem timeItem = getMenuItemByText("Set time between simulation steps (ms)");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                timeItem.fire();
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
