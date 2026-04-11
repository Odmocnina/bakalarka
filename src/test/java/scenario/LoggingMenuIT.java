package scenario;

import core.utils.constants.Constants;
import javafx.scene.control.CheckMenuItem;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ui.Actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

/*********************************************
 * Class containing integration tests for logging related items in the top MenuBar of the Window class.
 *
 * @author Michael Hladky
 * @version 1.0
 *********************************************/
public class LoggingMenuIT extends BaseWindowIT {

    // --- TESTS IF MENU ITEMS ARE ENABLED/DISABLED CORRECTLY ---

    // Test: map is loaded -> all logging-related menu items should be enabled
    @Test
    public void testLoggingMenuItemsStateWhenMapIsLoaded() {
        setMapLoadedState(true);

        CheckMenuItem toggleAllLoggingItem = (CheckMenuItem) getMenuItemByText("Toggle all logging");
        CheckMenuItem logInfoItem = (CheckMenuItem) getMenuItemByText("Log info");
        // We test just one of the sub-items ("Log info") as it represents the whole group's behavior

        // check if they are enabled (not disabled)
        assertFalse(toggleAllLoggingItem.isDisable(), "Toggle all logging item should be enabled when map is loaded.");
        assertFalse(logInfoItem.isDisable(), "Log info item should be enabled when map is loaded.");
    }

    // Test: map is not loaded -> logging items must STILL be enabled (configuration before simulation)
    @Test
    public void testLoggingMenuItemsStateWhenMapIsNotLoaded() {
        setMapLoadedState(false);

        CheckMenuItem toggleAllLoggingItem = (CheckMenuItem) getMenuItemByText("Toggle all logging");
        CheckMenuItem logInfoItem = (CheckMenuItem) getMenuItemByText("Log info");

        // these should be enabled even without a map
        assertFalse(toggleAllLoggingItem.isDisable(), "Toggle all logging item should work even without map.");
        assertFalse(logInfoItem.isDisable(), "Log info item should work even without map.");
    }

    // --- TEST OF EFFECTS (calling of actions methods) ---

    // Test: when we toggle the Toggle All Logging item, it should trigger Actions.setLoggingAction
    @Test
    public void testToggleAllLoggingMenuEffect_ShouldCallSetLoggingAction() {
        setMapLoadedState(false);

        CheckMenuItem toggleAllLoggingItem = (CheckMenuItem) getMenuItemByText("Toggle all logging");
        boolean initialState = toggleAllLoggingItem.isSelected();

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {

                // ACT: bypass .fire() and set the state directly to guarantee listener execution
                toggleAllLoggingItem.setSelected(!initialState);

                // ASSERT: Verify state changed and correct action was called with GENERAL index
                assertEquals(!initialState, toggleAllLoggingItem.isSelected(), "Menu item did not toggle its state!");
                actionsMock.verify(() -> Actions.setLoggingAction(eq(Constants.GENERAL_LOGGING_INDEX)), times(1));
            }
        });
    }

    // Test: when we toggle an item like "Log info", it should trigger Actions.setLoggingAction with correct index
    @Test
    public void testWhatToLogMenuEffect_ShouldCallSetLoggingAction() {
        setMapLoadedState(false);

        CheckMenuItem logInfoItem = (CheckMenuItem) getMenuItemByText("Log info");
        boolean initialState = logInfoItem.isSelected();

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {

                // ACT: bypass .fire() and set the state directly
                logInfoItem.setSelected(!initialState);

                // ASSERT: Verify state changed and correct action was called with INFO index
                assertEquals(!initialState, logInfoItem.isSelected(), "Menu item did not toggle its state!");
                actionsMock.verify(() -> Actions.setLoggingAction(eq(Constants.INFO_LOGGING_INDEX)), times(1));
            }
        });
    }
}
