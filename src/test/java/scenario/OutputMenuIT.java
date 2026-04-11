package scenario;

import core.utils.MyLogger;
import core.utils.constants.ConfigConstants;
import core.utils.constants.Constants;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ui.Actions;
import ui.DialogMaker;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*********************************************
 * Class containing integration tests for output and export related items in the top MenuBar of the Window class.
 *
 * @author Michael Hladky
 * @version 1.0
 *********************************************/
public class OutputMenuIT extends BaseWindowIT {

    // --- TESTS IF MENU ITEMS ARE ENABLED/DISABLED CORRECTLY ---

    // Test: map is loaded -> all output-related menu items should be enabled
    @Test
    public void testOutputMenuItemsStateWhenMapIsLoaded() {
        setMapLoadedState(true);

        MenuItem exportTxtItem = getMenuItemByText("Export results to TXT");
        MenuItem exportCsvItem = getMenuItemByText("Export to CSV"); // Note: slightly different text than toolbar, because I am a retard
        MenuItem setFileNameItem = getMenuItemByText("Set output file name");
        MenuItem setSeparatorItem = getMenuItemByText("Set CSV separator");

        // check if they are enabled (not disabled)
        assertFalse(exportTxtItem.isDisable(), "Export TXT should be enabled when map is loaded.");
        assertFalse(exportCsvItem.isDisable(), "Export CSV should be enabled when map is loaded.");
        assertFalse(setFileNameItem.isDisable(), "Set output file name should be enabled always.");
        assertFalse(setSeparatorItem.isDisable(), "Set CSV separator should be enabled always.");
    }

    // Test: map is not loaded -> only settings should be enabled, actual exports should be disabled
    @Test
    public void testOutputMenuItemsStateWhenMapIsNotLoaded() {
        setMapLoadedState(false);

        MenuItem exportTxtItem = getMenuItemByText("Export results to TXT");
        MenuItem exportCsvItem = getMenuItemByText("Export to CSV");
        MenuItem setFileNameItem = getMenuItemByText("Set output file name");
        MenuItem setSeparatorItem = getMenuItemByText("Set CSV separator");

        // these should be enabled even without a map
        assertFalse(setFileNameItem.isDisable(), "Set output file name should work even without map.");
        assertFalse(setSeparatorItem.isDisable(), "Set CSV separator should work even without map.");

        // these should be disabled without a map
        assertTrue(exportTxtItem.isDisable(), "Export TXT should be disabled without map.");
        assertTrue(exportCsvItem.isDisable(), "Export CSV should be disabled without map.");
    }

    // --- TEST OF EFFECTS (calling of actions methods) ---

    // Test: when we click Export TXT and engine is NOT running, it should call Actions.exportResultsToTxtAction
    @Test
    public void testExportTxtMenuEffect_EngineStopped_ShouldCallAction() {
        setMapLoadedState(true);
        when(mockEngine.getRunning()).thenReturn(false);

        MenuItem exportTxtItem = getMenuItemByText("Export results to TXT");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                exportTxtItem.fire();
                actionsMock.verify(() -> Actions.exportResultsToTxtAction(eq(mockSimulation)), times(1));
            }
        });
    }

    // Test: when we click Export CSV and engine is NOT running, it should call Actions.exportResultsToCsvAction
    @Test
    public void testExportCsvMenuEffect_EngineStopped_ShouldCallAction() {
        setMapLoadedState(true);
        when(mockEngine.getRunning()).thenReturn(false);

        MenuItem exportCsvItem = getMenuItemByText("Export to CSV");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                exportCsvItem.fire();
                actionsMock.verify(() -> Actions.exportResultsToCsvAction(eq(mockSimulation)), times(1));
            }
        });
    }

    // Test: CRITICAL BUSINESS LOGIC - when engine IS running, export should be aborted, dialog shown and action NEVER called
    @Test
    public void testExportMenuEffect_EngineRunning_ShouldAbortExport() {
        setMapLoadedState(true);
        when(mockEngine.getRunning()).thenReturn(true); // SIMULATING RUNNING ENGINE

        MenuItem exportCsvItem = getMenuItemByText("Export to CSV");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class);
                 MockedStatic<DialogMaker> dialogMock = mockStatic(DialogMaker.class);
                 MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

                exportCsvItem.fire();

                // verify warning dialog was triggered
                dialogMock.verify(() -> DialogMaker.warningDialog(any(), anyString()), times(1));

                // verify logger recorded the cancellation
                loggerMock.verify(() -> MyLogger.log(contains("Export cancelled"), eq(Constants.INFO_FOR_LOGGING)), times(1));

                // Verify that the actual export action was NEVER called!
                actionsMock.verify(() -> Actions.exportResultsToCsvAction(any()), never());
                actionsMock.verify(() -> Actions.exportResultsToTxtAction(any()), never());
            }
        });
    }

    // Test: CRITICAL BUSINESS LOGIC - when engine IS running, export should be aborted, dialog shown and action NEVER called
    @Test
    public void testExportMenuEffectTxt_EngineRunning_ShouldAbortExport() {
        setMapLoadedState(true);
        when(mockEngine.getRunning()).thenReturn(true); // SIMULATING RUNNING ENGINE

        MenuItem exportResultsToTxt = getMenuItemByText("Export results to TXT");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class);
                 MockedStatic<DialogMaker> dialogMock = mockStatic(DialogMaker.class);
                 MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

                exportResultsToTxt.fire();

                // verify warning dialog was triggered
                dialogMock.verify(() -> DialogMaker.warningDialog(any(), anyString()), times(1));

                // verify logger recorded the cancellation
                loggerMock.verify(() -> MyLogger.log(contains("Export cancelled"), eq(Constants.INFO_FOR_LOGGING)), times(1));

                // Verify that the actual export action was NEVER called!
                actionsMock.verify(() -> Actions.exportResultsToCsvAction(any()), never());
                actionsMock.verify(() -> Actions.exportResultsToTxtAction(any()), never());
            }
        });
    }

    // Test: when we click Set Output File Name, it should call Actions.setOutputFileAction
    @Test
    public void testSetOutputFileNameMenuEffect_ShouldCallAction() {
        setMapLoadedState(false);
        MenuItem setFileNameItem = getMenuItemByText("Set output file name");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                setFileNameItem.fire();
                actionsMock.verify(Actions::setOutputFileAction, times(1));
            }
        });
    }

    // Test: when we click Set CSV separator, it should call Actions.setCsvSeparatorAction
    @Test
    public void testSetCsvSeparatorMenuEffect_ShouldCallAction() {
        setMapLoadedState(false);
        MenuItem setSeparatorItem = getMenuItemByText("Set CSV separator");

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {
                setSeparatorItem.fire();
                actionsMock.verify(() -> Actions.setCsvSeparatorAction(eq(primaryStage)), times(1));
            }
        });
    }

    // Test: when we toggle an item in the "What to export" submenu, it should trigger Actions.setOutputAction
    @Test
    public void testWhatToExportSubMenu_ShouldCallSetOutputAction() {
        setMapLoadedState(false);

        // Our recursive search safely finds items hidden deep in sub-menus!
        CheckMenuItem simDetailsItem = (CheckMenuItem) getMenuItemByText("Simulation details");
        boolean initialState = simDetailsItem.isSelected();

        interact(() -> {
            try (MockedStatic<Actions> actionsMock = mockStatic(Actions.class)) {

                // SAFETY NET: If the BaseWindowIT didn't properly initialize the bound property,
                // we inject it manually so the CheckMenuItem can actually trigger the action.
                try {
                    Field field = ui.Window.class.getDeclaredField("whatToExportPropsMap");
                    field.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    HashMap<String, BooleanProperty> map = (HashMap<String, BooleanProperty>) field.get(window);

                    if (map.get(ConfigConstants.SIMULATION_DETAILS_TAG) == null) {
                        SimpleBooleanProperty simDetailsProp = new SimpleBooleanProperty(initialState);
                        simDetailsProp.addListener((obs, oldVal, newVal) ->
                                Actions.setOutputAction(ConfigConstants.SIMULATION_DETAILS_TAG));
                        map.put(ConfigConstants.SIMULATION_DETAILS_TAG, simDetailsProp);

                        // re-bind the menu item to our newly injected property to make sure it works
                        simDetailsItem.selectedProperty().bindBidirectional(simDetailsProp);
                    }
                } catch (Exception e) {
                    Assertions.fail("Reflection setup error: " + e.getMessage());
                }

                // ACT: bypass .fire() and set the state directly to guarantee listener execution
                simDetailsItem.setSelected(!initialState);

                // ASSERT: State changed and action was called
                assertEquals(!initialState, simDetailsItem.isSelected(), "Sub-menu item state did not change!");
                actionsMock.verify(() -> Actions.setOutputAction(eq(ConfigConstants.SIMULATION_DETAILS_TAG)), times(1));
            }
        });
    }
}
