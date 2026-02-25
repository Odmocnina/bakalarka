package ui;

import app.AppContext;
import core.engine.CoreEngine;
import core.model.Road;
import core.sim.Simulation;
import core.utils.*;
import ui.DialogMaker;
import core.utils.loading.RoadLoader;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.File;
import java.util.ArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*************************************
 * Test class for Actions, which contains static methods that are triggered by user interactions in the UI, such as
 * opening maps, saving maps, modifying maps, and controlling the simulation. The tests verify that these actions
 * perform the expected operations, such as showing dialogs, modifying configurations, and interacting with the
 * simulation and file system correctly.
 *
 * @author Michael Hladky
 * @version 1.0.0
 *************************************/
@ExtendWith(MockitoExtension.class)
public class ActionsTest {

    /** mock Simulation for providing test data and verifying method calls **/
    @Mock
    private Simulation mockSimulation;

    /** mock Stage for simulating user interactions with dialogs **/
    @Mock
    private Stage mockStage;

    /** mock Runnable for verifying that paintAll is called when expected **/
    @Mock
    private Runnable mockPaintAll;

    /** mock CoreEngine for verifying interactions when changing time between steps **/
    @Mock
    private CoreEngine mockEngine;

    /** arrays for testing scenarios with no roads and valid roads in the simulation **/
    private Road[] emptyRoads;

    /* valid roads array with a single mocked road for testing scenarios where roads are present in the simulation * */
    private Road[] validRoads;

    /**
     * setup method to initialize the arrays and configure the global state for testing, ensuring that the tests can
     * run without NullPointerExceptions and that the AppContext is in a known state before each test
     **/
    @BeforeEach
    void setUp() {
        emptyRoads = new Road[0];
        validRoads = new Road[]{mock(Road.class)};

        // Ensure global state is safe for testing
        // Adjust initialization based on your actual AppContext and RunDetails implementation
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.RUN_DETAILS.mapChanged = false;
        AppContext.SIMULATION = mockSimulation;
    }

    /**
     * test to verify that if there are unsaved changes and the user cancels the dialog,
     * the open map action is aborted and no file chooser is opened
     **/
    @Test
    void openMapAction_UnsavedChanges_UserCancels_ShouldAbort() {
        // Arrange
        AppContext.RUN_DETAILS.mapChanged = true;

        try (MockedStatic<DialogMaker> dialogMock = mockStatic(DialogMaker.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            // user clicks 'cancel' on unsaved changes dialog
            dialogMock.when(() -> DialogMaker.onCloseUnsavedChangesDialog(mockStage)).thenReturn(false);

            // Act
            Actions.openMapAction(mockStage, mockPaintAll);

            // Assert
            // verify the dialog was shown and logger logged the cancellation
            dialogMock.verify(() -> DialogMaker.onCloseUnsavedChangesDialog(mockStage), times(1));
            loggerMock.verify(() -> MyLogger.log(anyString(), anyString()), atLeastOnce());
            // paintAll should not be called
            verifyNoInteractions(mockPaintAll);
        }
    }

    /**
     * test to verify that if map changes are saved or ignored, and a valid file is selected,
     * the file is loaded and the map is repainted
     **/
    @Test
    void openMapAction_ValidFileSelected_ShouldLoadAndRepaint() {
        // Arrange
        AppContext.RUN_DETAILS.mapChanged = false;
        File mockFile = mock(File.class);
        when(mockFile.getAbsolutePath()).thenReturn("dummy/path.xml");

        // We need to mock the FileChooser construction and its methods
        try (MockedConstruction<FileChooser> mockedFileChooser = mockConstruction(FileChooser.class,
                (mock, context) -> {
                    when(mock.showOpenDialog(mockStage)).thenReturn(mockFile);
                    when(mock.getExtensionFilters()).thenReturn(mock(javafx.collections.ObservableList.class));
                });
             MockedStatic<RoadLoader> loaderMock = mockStatic(RoadLoader.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            // loading is successful
            loaderMock.when(() -> RoadLoader.loadMap(anyString())).thenReturn(true);

            // Act
            Actions.openMapAction(mockStage, mockPaintAll);

            // Assert
            // verify the file was loaded and paintAll was executed
            loaderMock.verify(() -> RoadLoader.loadMap("dummy/path.xml"), times(1));
            verify(mockPaintAll, times(1)).run();
        }
    }

    /**
     * test to verify that toggling the collision ban updates the config and repaints the map
     * if there are valid roads in the simulation
     **/
    @Test
    void collisionBanAction_WithRoads_ShouldChangeConfigAndRepaint() {
        // Arrange
        when(mockSimulation.getRoads()).thenReturn(validRoads);

        try (MockedStatic<ConfigModification> configMock = mockStatic(ConfigModification.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            // Act
            Actions.collisionBanAction(mockSimulation, mockPaintAll);

            // Assert
            configMock.verify(ConfigModification::changePreventCollision, times(1));
            verify(mockPaintAll, times(1)).run();
        }
    }

    /**
     * test to verify that when saving map as, if there are no roads, it logs a warning and returns
     **/
    @Test
    void saveMapAsAction_NoRoads_ShouldLogWarningAndReturn() {
        // Arrange
        when(mockSimulation.getRoads()).thenReturn(emptyRoads);

        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<DialogMaker> dialogMock = mockStatic(DialogMaker.class)) {

            // Act
            Actions.saveMapAsAction(mockSimulation, mockStage);

            // Assert
            // dialog should not be shown if there are no roads
            dialogMock.verify(() -> DialogMaker.saveAsDialog(any()), never());
        }
    }

    /**
     * test to verify that when saving map as with valid roads, it opens the save as dialog
     **/
    @Test
    void saveMapAsAction_WithRoads_ShouldCallDialogMaker() {
        // Arrange
        when(mockSimulation.getRoads()).thenReturn(validRoads);

        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<DialogMaker> dialogMock = mockStatic(DialogMaker.class)) {

            // Act
            Actions.saveMapAsAction(mockSimulation, mockStage);

            // Assert
            // verify save as dialog is shown
            dialogMock.verify(() -> DialogMaker.saveAsDialog(mockStage), times(1));
        }
    }

    /**
     * test to verify that when saving map normally with valid roads, it calls RoadXml to save
     **/
    @Test
    void saveMapAction_WithRoads_ShouldCallRoadXmlSave() {
        // Arrange
        when(mockSimulation.getRoads()).thenReturn(validRoads);

        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<RoadXml> roadXmlMock = mockStatic(RoadXml.class)) {

            // Act
            Actions.saveMapAction(mockSimulation);

            // Assert
            roadXmlMock.verify(RoadXml::saveCurrentMap, times(1));
        }
    }

    /**
     * test to verify that editing map file with valid roads opens the modify map dialog
     **/
    @Test
    void editMapFile_WithRoads_ShouldOpenModifyDialog() {
        // Arrange
        when(mockSimulation.getRoads()).thenReturn(validRoads);

        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<RoadParameters> paramsMock = mockStatic(RoadParameters.class);
             MockedStatic<ui.ModifyMapDialogMaker> modifyMock = mockStatic(ui.ModifyMapDialogMaker.class)) {

            paramsMock.when(() -> RoadParameters.existingRoadsToRoadParameters(any())).thenReturn(new ArrayList<>());

            // Act
            Actions.editMapFile(mockSimulation, mockStage, mockPaintAll);

            // Assert
            // verify the modify map dialog is triggered
            modifyMock.verify(() -> ui.ModifyMapDialogMaker.modifyMapDialog(eq(mockStage), any(), eq(mockPaintAll)), times(1));
        }
    }

    /**
     * test to verify that exporting results to TXT with valid roads writes the results correctly
     **/
    @Test
    void exportResultsToTxtAction_WithRoads_ShouldWriteTxt() {
        // Arrange
        when(mockSimulation.getRoads()).thenReturn(validRoads);
        ResultsRecorder mockRecorder = mock(ResultsRecorder.class);

        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<ResultsRecorder> recorderStaticMock = mockStatic(ResultsRecorder.class)) {

            recorderStaticMock.when(ResultsRecorder::getResultsRecorder).thenReturn(mockRecorder);

            // Act
            Actions.exportResultsToTxtAction(mockSimulation);

            // Assert
            // verify that writeResultsTxt was called on the recorder instance
            verify(mockRecorder, times(1)).writeResultsTxt();
        }
    }

    /**
     * test to verify that performing next step with valid roads steps the simulation and repaints
     **/
    @Test
    void nextStepAction_WithRoads_ShouldStepAndRepaint() {
        // Arrange
        when(mockSimulation.getRoads()).thenReturn(validRoads);

        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            // Act
            Actions.nextStepAction(mockSimulation, mockPaintAll);

            // Assert
            // check that simulation logic advanced and map was repainted
            verify(mockSimulation, times(1)).step();
            verify(mockPaintAll, times(1)).run();
        }
    }

    /**
     * test to verify that resetting simulation with valid roads resets it and repaints
     **/
    @Test
    void resetSimulationAction_WithRoads_ShouldResetAndRepaint() {
        // Arrange
        when(mockSimulation.getRoads()).thenReturn(validRoads);

        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            // Act
            Actions.resetSimulationAction(mockSimulation, mockPaintAll);

            // Assert
            // verify reset logic is called and UI is updated
            verify(mockSimulation, times(1)).resetSimulationWithSameRoads();
            verify(mockPaintAll, times(1)).run();
        }
    }

    /**
     * test to verify other simple dialog and config toggling actions function without exceptions
     **/
    @Test
    void simpleActions_ShouldTriggerCorrespondingMethods() {
        try (MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class);
             MockedStatic<ConfigModification> configMock = mockStatic(ConfigModification.class);
             MockedStatic<DialogMaker> dialogMock = mockStatic(DialogMaker.class);
             MockedStatic<ui.NewMapDialogMaker> newMapMock = mockStatic(ui.NewMapDialogMaker.class)) {

            // Act & Assert
            Actions.setLoggingAction(1);
            configMock.verify(() -> ConfigModification.changeLogging(1), times(1));

            Actions.setTimeBetweenStepsAction(mockStage, mockEngine);
            dialogMock.verify(() -> DialogMaker.changeTimeBetweenSteps(mockStage, mockEngine), times(1));

            Actions.setOutputFileAction();
            dialogMock.verify(DialogMaker::setOutFileDialog, times(1));

            Actions.setCsvSeparatorAction(mockStage);
            dialogMock.verify(() -> DialogMaker.setCsvSeparatorDialog(mockStage), times(1));

            Actions.setOutputAction("testKey");
            configMock.verify(() -> ConfigModification.changeOutput("testKey"), times(1));

            Actions.newMapAction(mockStage, mockPaintAll);
            newMapMock.verify(() -> ui.NewMapDialogMaker.newMapDialog(mockStage, mockPaintAll), times(1));
        }
    }
}