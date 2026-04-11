package scenario;

import javafx.scene.control.MenuItem;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ui.Actions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*********************************************
 * Class containing integration tests for map file related items in the top MenuBar of the Window class.
 *
 * @author Michael Hladky
 * @version 1.0
 *********************************************/
public class MapFileMenuIT extends BaseWindowIT {

    // --- TESTS IF MENU ITEMS ARE ENABLED/DISABLED CORRECTLY ---

    // Test: map is loaded -> all map-related menu items should be enabled
    @Test
    public void testMapMenuItemsStateWhenMapIsLoaded() {
        setMapLoadedState(true);

        MenuItem newMapItem = getMenuItemByText("New map file");
        MenuItem editMapItem = getMenuItemByText("Modify current map file");
        MenuItem openMapItem = getMenuItemByText("Open map file");
        MenuItem saveMapItem = getMenuItemByText("Save map file");
        MenuItem saveAsMapItem = getMenuItemByText("Save map file as...");

        // verify they are enabled
        assertFalse(newMapItem.isDisable(), "New map item should be enabled always.");
        assertFalse(editMapItem.isDisable(), "Edit map item should be enabled when map is loaded.");
        assertFalse(openMapItem.isDisable(), "Open map item should be enabled always.");
        assertFalse(saveMapItem.isDisable(), "Save map item should be enabled when map is loaded.");
        assertFalse(saveAsMapItem.isDisable(), "Save as map item should be enabled when map is loaded.");
    }

    // Test: map is not loaded -> only New and Open should be enabled, the rest should be disabled
    @Test
    public void testMapMenuItemsStateWhenMapIsNotLoaded() {
        setMapLoadedState(false);

        MenuItem newMapItem = getMenuItemByText("New map file");
        MenuItem editMapItem = getMenuItemByText("Modify current map file");
        MenuItem openMapItem = getMenuItemByText("Open map file");
        MenuItem saveMapItem = getMenuItemByText("Save map file");
        MenuItem saveAsMapItem = getMenuItemByText("Save map file as...");

        // these should be enabled even without a map
        assertFalse(newMapItem.isDisable(), "New map item should work even without map.");
        assertFalse(openMapItem.isDisable(), "Open map item should work even without map.");

        // these should be disabled without a map
        assertTrue(editMapItem.isDisable(), "Edit map item should be disabled.");
        assertTrue(saveMapItem.isDisable(), "Save map item should be disabled.");
        assertTrue(saveAsMapItem.isDisable(), "Save as map item should be disabled.");
    }

    // --- TEST OF EFFECTS (calling of actions methods) ---

    // Test: when we click the New Map item, it should call Actions.newMapAction
    @Test
    public void testNewMapMenuEffect_ShouldCallNewMapAction() {
        setMapLoadedState(false);
        MenuItem newMapItem = getMenuItemByText("New map file");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                newMapItem.fire();
                actionsMock.verify(() -> Actions.newMapAction(eq(primaryStage), any(Runnable.class)), times(1));
            }
        });
    }

    // Test: when we click the Open Map item, it should handle scrollbars and call Actions.openMapAction
    @Test
    public void testOpenMapMenuEffect_ShouldCallOpenMapAction() {
        setMapLoadedState(false);
        MenuItem openMapItem = getMenuItemByText("Open map file");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                openMapItem.fire();
                // This correctly verifies that Window.handleOpenNewMap delegates to Actions.openMapAction
                actionsMock.verify(() -> Actions.openMapAction(eq(primaryStage), any(Runnable.class)), times(1));
            }
        });
    }

    // Test: when we click the Edit Map item, it should call Actions.editMapFile
    @Test
    public void testEditMapMenuEffect_ShouldCallEditMapAction() {
        setMapLoadedState(true);
        MenuItem editMapItem = getMenuItemByText("Modify current map file");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                editMapItem.fire();
                actionsMock.verify(() -> Actions.editMapFile(eq(mockSimulation), eq(primaryStage), any(Runnable.class)), times(1));
            }
        });
    }

    // Test: when we click the Save Map item, it should call Actions.saveMapAction
    @Test
    public void testSaveMapMenuEffect_ShouldCallSaveMapAction() {
        setMapLoadedState(true);
        MenuItem saveMapItem = getMenuItemByText("Save map file");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                saveMapItem.fire();
                actionsMock.verify(() -> Actions.saveMapAction(eq(mockSimulation)), times(1));
            }
        });
    }

    // Test: when we click the Save As Map item, it should call Actions.saveMapAsAction
    @Test
    public void testSaveAsMapMenuEffect_ShouldCallSaveMapAsAction() {
        setMapLoadedState(true);
        MenuItem saveAsMapItem = getMenuItemByText("Save map file as...");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                saveAsMapItem.fire();
                actionsMock.verify(() -> Actions.saveMapAsAction(eq(mockSimulation), eq(primaryStage)), times(1));
            }
        });
    }
}
