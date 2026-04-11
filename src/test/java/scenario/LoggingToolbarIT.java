package scenario;

import core.utils.constants.Constants;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ui.Actions;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

/*********************************************
 * Class containing integration tests for logging related buttons in toolbar in the Window class.
 *
 * @author Michael Hladky
 * @version 1.0
 *********************************************/
public class LoggingToolbarIT extends BaseWindowIT {

    // --- TESTS IF BUTTONS ARE ENABLED/DISABLED CORRECTLY ---

    // Test: map is loaded -> all logging-related buttons should be enabled
    @Test
    public void testLoggingButtonsStateWhenMapIsLoaded() {
        setMapLoadedState(true);

        ButtonBase toggleLoggingBtn = getButtonByTooltip("Toggle all logging");
        ButtonBase whatToLogBtn = getButtonByTooltip("What to log");

        // check if they are enabled (not disabled)
        assertFalse(toggleLoggingBtn.isDisabled(), "Toggle all logging should be enabled when map is loaded.");
        assertFalse(whatToLogBtn.isDisabled(), "What to log should be enabled when map is loaded.");
    }

    // Test: map is not loaded -> logging buttons must STILL be enabled (configuration before simulation)
    @Test
    public void testLoggingButtonsStateWhenMapIsNotLoaded() {
        setMapLoadedState(false);

        ButtonBase toggleLoggingBtn = getButtonByTooltip("Toggle all logging");
        ButtonBase whatToLogBtn = getButtonByTooltip("What to log");

        // these should be enabled even without a map
        assertFalse(toggleLoggingBtn.isDisabled(), "Toggle all logging should work even without map.");
        assertFalse(whatToLogBtn.isDisabled(), "What to log should work even without map.");
    }

    // --- TEST OF EFFECTS (calling of actions methods) ---

    // Test: when we click the Toggle All Logging button, it should toggle its state and call Actions.setLoggingAction
    @Test
    public void testToggleAllLoggingEffect_ShouldTogglePropertyAndCallAction() {
        setMapLoadedState(false);

        ToggleButton toggleLoggingBtn = (ToggleButton) getButtonByTooltip("Toggle all logging");
        boolean initialState = toggleLoggingBtn.isSelected();

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {

                toggleLoggingBtn.fire();

                // check that the button's selected state has toggled
                assertEquals(!initialState, toggleLoggingBtn.isSelected(), "Toggle all logging button did not toggle!");

                // check that Actions.setLoggingAction was called with the GENERAL_LOGGING_INDEX constant
                actionsMock.verify(() -> Actions.setLoggingAction(eq(Constants.GENERAL_LOGGING_INDEX)), times(1));
            }
        });
    }

    // Test: when we change a property tied to "What to log" menu, it should trigger Actions.setLoggingAction
    @Test
    public void testWhatToLogMenu_ShouldCallSetLoggingAction() {
        setMapLoadedState(false);

        ButtonBase whatToLogBtn = getButtonByTooltip("What to log");

        // 1. Physically click the button to verify it opens the ContextMenu without crashing
        clickOn(whatToLogBtn);
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();

        // 2. Use reflection to safely bypass unstable UI menu clicking and test the core logic (listeners) directly
        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {

                // get the private array of BooleanProperties from Window
                Field field = ui.Window.class.getDeclaredField("logSettingsProps");
                ((Field) field).setAccessible(true);
                BooleanProperty[] props = (javafx.beans.property.BooleanProperty[]) field.get(window);

                // extract the property for "Log info" (using the constant index)
                BooleanProperty infoLogProp = props[Constants.INFO_LOGGING_INDEX];

                // ACT: simulate the CheckMenuItem being clicked by toggling its underlying property
                infoLogProp.set(!infoLogProp.get());

                // ASSERT: Verify that the listener fired and called the correct action with the specific INFO index
                actionsMock.verify(() -> Actions.setLoggingAction(eq(Constants.INFO_LOGGING_INDEX)), times(1));

            } catch (Exception e) {
                Assertions.fail("Reflection error during whatToLog test: " + e.getMessage());
            }
        });
    }
}
