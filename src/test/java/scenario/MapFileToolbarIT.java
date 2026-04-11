package scenario;

import javafx.scene.control.ButtonBase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ui.Actions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*********************************************
 * Class containing integration tests for map file related buttons in toolbar in the Window class.
 *
 * @author Michael Hladky
 * @version 1.0
 *********************************************/
public class MapFileToolbarIT extends BaseWindowIT {

    // --- TESTS IF BUTTONS ARE ENABLED/DISABLED CORRECTLY ---

    // Test: map is loaded -> all map-related buttons should be enabled
    @Test
    public void testMapButtonsStateWhenMapIsLoaded() {
        // map is loaded, so all buttons should be enabled
        setMapLoadedState(true);

        // find the buttons by their tooltip text
        ButtonBase newMapBtn = getButtonByTooltip("New map file");
        ButtonBase editMapBtn = getButtonByTooltip("Modify current map file");
        ButtonBase openMapBtn = getButtonByTooltip("Open map file");
        ButtonBase saveMapBtn = getButtonByTooltip("Save map file");
        ButtonBase saveAsMapBtn = getButtonByTooltip("Save map file as...");

        // check if they are enabled (not disabled)
        assertFalse(newMapBtn.isDisabled(), "New map file should be enabled always.");
        assertFalse(editMapBtn.isDisabled(), "Edit map should be enabled when map is loaded.");
        assertFalse(openMapBtn.isDisabled(), "Open map should be enabled always.");
        assertFalse(saveMapBtn.isDisabled(), "Save map should be enabled when map is loaded.");
        assertFalse(saveAsMapBtn.isDisabled(), "Save as map should be enabled when map is loaded.");
    }

    // Test: map is not loaded -> only New and Open should be enabled, the rest should be disabled
    @Test
    public void testMapButtonsStateWhenMapIsNotLoaded() {
        // map is not loaded, so only New and Open should be enabled, the rest should be disabled
        setMapLoadedState(false);

        ButtonBase newMapBtn = getButtonByTooltip("New map file");
        ButtonBase editMapBtn = getButtonByTooltip("Modify current map file");
        ButtonBase openMapBtn = getButtonByTooltip("Open map file");
        ButtonBase saveMapBtn = getButtonByTooltip("Save map file");
        ButtonBase saveAsMapBtn = getButtonByTooltip("Save map file as...");

        // these should be enabled even without a map
        assertFalse(newMapBtn.isDisabled(), "New map should work even without map.");
        assertFalse(openMapBtn.isDisabled(), "Open map should work even without map.");

        // these should be disabled without a map
        assertTrue(editMapBtn.isDisabled(), "Edit map should be disabled.");
        assertTrue(saveMapBtn.isDisabled(), "Save map should be disabled.");
        assertTrue(saveAsMapBtn.isDisabled(), "Save as map should be disabled.");
    }

    // --- TEST OF EFFECTS (calling of actions methods) ---

    // Test: when we click the New Map button, it should call Actions.newMapAction with the correct parameters
    // (Stage and Runnable for repainting)
    @Test
    public void testNewMapEffect_ShouldCallNewMapAction() {
        setMapLoadedState(false); // clicking New Map should work even without a map
        ButtonBase newMapBtn = getButtonByTooltip("New map file");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                newMapBtn.fire();
                // check that Actions.newMapAction was called with the primaryStage and any Runnable (for repainting)
                actionsMock.verify(() -> Actions.newMapAction(eq(primaryStage), any(Runnable.class)), times(1));
            }
        });
    }

    // Test: when we click the Open Map button, it should call Actions.openMapAction with the correct parameters
    @Test
    public void testOpenMapEffect_ShouldCallOpenMapAction() {
        setMapLoadedState(false);
        ButtonBase openMapBtn = getButtonByTooltip("Open map file");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                openMapBtn.fire();
                // check that Actions.openMapAction was called with the primaryStage and any Runnable (for repainting)
                actionsMock.verify(() -> Actions.openMapAction(eq(primaryStage), any(Runnable.class)), times(1));
            }
        });
    }

    // Test: when we click the Edit Map button, it should call Actions.editMapFile with the correct parameters
    @Test
    public void testEditMapEffect_ShouldCallEditMapAction() {
        setMapLoadedState(true); // Edit Map button should be enabled only when a map is loaded
        ButtonBase editMapBtn = getButtonByTooltip("Modify current map file");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                editMapBtn.fire();
                // check that Actions.editMapFile was called with the mockSimulation, primaryStage, and any Runnable (for repainting)
                actionsMock.verify(() -> Actions.editMapFile(eq(mockSimulation), eq(primaryStage), any(Runnable.class)), times(1));
            }
        });
    }

    // Test: when we click the Save Map button, it should call Actions.saveMapAction with the correct parameters (the simulation)
    @Test
    public void testSaveMapEffect_ShouldCallSaveMapAction() {
        setMapLoadedState(true);
        ButtonBase saveMapBtn = getButtonByTooltip("Save map file");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                saveMapBtn.fire();
                // check that Actions.saveMapAction was called with the mockSimulation
                actionsMock.verify(() -> Actions.saveMapAction(eq(mockSimulation)), times(1));
            }
        });
    }

    // Test: when we click the Save As Map button, it should call Actions.saveMapAsAction with the correct parameters
    // (the simulation and Stage for file chooser)
    @Test
    public void testSaveAsMapEffect_ShouldCallSaveMapAsAction() {
        setMapLoadedState(true);
        ButtonBase saveAsMapBtn = getButtonByTooltip("Save map file as...");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                saveAsMapBtn.fire();
                // check that Actions.saveMapAsAction was called with the mockSimulation and primaryStage
                actionsMock.verify(() -> Actions.saveMapAsAction(eq(mockSimulation), eq(primaryStage)), times(1));
            }
        });
    }
}
