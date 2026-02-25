package ui;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.model.Road;
import core.utils.DefaultStuffMaker;
import core.utils.MyLogger;
import core.utils.RoadParameters;
import core.utils.RunDetails;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import models.ICarFollowingModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/*******************************
 * Unit tests for ModifyMapDialogMaker, covering the main modify map dialog and the private add road dialog
 * using Mockito to mock dependencies and verify interactions with the simulation and UI components
 *
 * @author Michael Hladky
 * @version 1.0
 *******************************/
@ExtendWith(MockitoExtension.class)
public class ModifyMapDialogMakerTest {

    /** mock Stage for dialog ownership, we just need it to create the dialogs without exceptions, we don't verify any
     * interactions with it **/
    @Mock
    private Stage mockStage;

    /** mock Runnable for repainting the map, we verify that it's called when modifications are applied and not called
     * when cancelled **/
    @Mock
    private Runnable mockPaintAll;

    /** mock Simulation to verify that it's updated with new roads when modifications are applied **/
    @Mock
    private core.sim.Simulation mockSimulation;

    /** mock CarFollowingModel to provide a model ID for length generation logic during modification **/
    @Mock
    private ICarFollowingModel mockCarFollowingModel;

    /** setup method to initialize JavaFX toolkit before all tests, since we're creating Dialogs in the tests which
     * require JavaFX to be initialized **/
    @BeforeAll
    static void initJavaFX() {
        // initialize JavaFX toolkit to prevent IllegalStateException when creating UI components
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // toolkit is already initialized, we can ignore this
        }
    }

    /** setup method to initialize the global AppContext before each test, ensuring that the RUN_DETAILS and SIMULATION
     * are set up to avoid NullPointerExceptions during dialog creation and interaction **/
    @BeforeEach
    void setUp() {
        // setting up the global application context
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.RUN_DETAILS.mapChanged = false;
        AppContext.SIMULATION = mockSimulation;
        AppContext.CAR_FOLLOWING_MODEL = mockCarFollowingModel;
    }

    /**
     * test to verify that when the 'Modify' button is clicked in the main dialog,
     * the simulation is updated with the new road parameters and the map is repainted
     **/
    @Test
    void modifyMapDialog_ApplyClicked_ShouldUpdateSimulationAndRepaint() {
        // Arrange
        ArrayList<RoadParameters> roadParameters = new ArrayList<>();
        when(mockCarFollowingModel.getID()).thenReturn("test-model-id");

        // mock Dialog to auto-click the 'Modify' button
        try (MockedConstruction<Dialog> dialogMock = mockConstruction(Dialog.class, (mock, context) -> {
            DialogPane pane = new DialogPane();
            when(mock.getDialogPane()).thenReturn(pane);
            when(mock.showAndWait()).thenAnswer(inv -> pane.getButtonTypes().stream()
                    .filter(b -> b.getText().equals("Modify"))
                    .findFirst());
        });
             MockedStatic<RoadParameters> paramsMock = mockStatic(RoadParameters.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            // mock road parameters conversion logic
            Road mockRoad = mock(Road.class);
            Road[] roadsArray = new Road[]{mockRoad};
            paramsMock.when(() -> RoadParameters.roadParametersToRoads(roadParameters)).thenReturn(roadsArray);

            // Act
            ModifyMapDialogMaker.modifyMapDialog(mockStage, roadParameters, mockPaintAll);

            // Assert
            // verify the length generation was handled with the correct model ID
            paramsMock.verify(() -> RoadParameters.handleSettingOfLengthGeneration(roadParameters, "test-model-id"), times(1));
            // verify roads were set up and simulation was reset
            verify(mockRoad, times(1)).setUpQueuesIfNeeded();
            verify(mockSimulation, times(1)).resetSimulationWithNewRoads(roadsArray);
            // verify UI was repainted
            verify(mockPaintAll, times(1)).run();
            loggerMock.verify(() -> MyLogger.log(anyString(), anyString()), atLeastOnce());
        }
    }

    /**
     * test to verify that when the 'Cancel' button is clicked in the main dialog,
     * no changes are applied to the simulation and the map is not repainted
     **/
    @Test
    void modifyMapDialog_CancelClicked_ShouldNotUpdateSimulation() {
        // Arrange
        ArrayList<RoadParameters> roadParameters = new ArrayList<>();

        // mock Dialog to auto-click the 'Cancel' button
        try (MockedConstruction<Dialog> dialogMock = mockConstruction(Dialog.class, (mock, context) -> {
            DialogPane pane = new DialogPane();
            when(mock.getDialogPane()).thenReturn(pane);
            when(mock.showAndWait()).thenAnswer(inv -> pane.getButtonTypes().stream()
                    .filter(b -> b.getButtonData() == javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE)
                    .findFirst());
        });
             MockedStatic<RoadParameters> paramsMock = mockStatic(RoadParameters.class)) {

            // Act
            ModifyMapDialogMaker.modifyMapDialog(mockStage, roadParameters, mockPaintAll);

            // Assert
            // verify that logic for updating roads was completely bypassed
            paramsMock.verify(() -> RoadParameters.handleSettingOfLengthGeneration(any(), anyString()), never());
            verify(mockSimulation, never()).resetSimulationWithNewRoads(any());
            verify(mockPaintAll, never()).run();
        }
    }

    /**
     * test to verify that adding a road via the private addRoadDialog with valid inputs
     * successfully adds a new RoadParameters object to the list and repaints the map
     **/
    @Test
    void addRoadDialog_ApplyClicked_ValidInputs_ShouldAddRoad() throws Exception {
        // Arrange
        ArrayList<RoadParameters> roadParameters = new ArrayList<>();

        // mock Dialog to auto-click the 'Apply' button
        try (MockedConstruction<Dialog> dialogMock = mockConstruction(Dialog.class, (mock, context) -> {
            DialogPane pane = new DialogPane();
            when(mock.getDialogPane()).thenReturn(pane);
            when(mock.showAndWait()).thenAnswer(inv -> pane.getButtonTypes().stream()
                    .filter(b -> b.getText().equals("Apply"))
                    .findFirst());
        });
             MockedStatic<DefaultStuffMaker> defaultMakerMock = mockStatic(DefaultStuffMaker.class)) {

            // mock default generator and light plan to be legitimate so the validation passes
            CarGenerator mockGenerator = mock(CarGenerator.class);
            when(mockGenerator.isLegitimate()).thenReturn(true);
            // REMOVED: when(mockGenerator.clone()).thenReturn(mockGenerator); -> never called during 'Apply'

            LightPlan mockLightPlan = mock(LightPlan.class);
            when(mockLightPlan.isLegitimate()).thenReturn(true);
            // REMOVED: when(mockLightPlan.clone()).thenReturn(mockLightPlan); -> never called during 'Apply'

            defaultMakerMock.when(DefaultStuffMaker::createDefaultGenerator).thenReturn(mockGenerator);
            defaultMakerMock.when(DefaultStuffMaker::createDefaultLightPlan).thenReturn(mockLightPlan);

            // mock simulation step count to avoid NPE during UI creation (createWaringTextIfNeeded)
            when(mockSimulation.getStepCount()).thenReturn(0);

            // Act
            // invoke the private static method 'addRoadDialog' using reflection
            Method addRoadMethod = ModifyMapDialogMaker.class.getDeclaredMethod(
                    "addRoadDialog", Stage.class, ArrayList.class, Runnable.class);
            addRoadMethod.setAccessible(true);
            addRoadMethod.invoke(null, mockStage, roadParameters, mockPaintAll);

            // Assert
            // check if road parameter was added and map changed flag was set
            assertEquals(1, roadParameters.size(), "One road parameter should be added to the list");
            assertTrue(AppContext.RUN_DETAILS.mapChanged, "Map changed flag should be true");
            verify(mockPaintAll, times(1)).run();
        }
    }

    /**
     * test to verify that adding a road via the private addRoadDialog with invalid inputs
     * (illegitimate generators) stops the process and does not add the road
     **/
    @Test
    void addRoadDialog_ApplyClicked_InvalidInputs_ShouldNotAddRoad() throws Exception {
        // Arrange
        ArrayList<RoadParameters> roadParameters = new ArrayList<>();

        try (MockedConstruction<Dialog> dialogMock = mockConstruction(Dialog.class, (mock, context) -> {
            DialogPane pane = new DialogPane();
            when(mock.getDialogPane()).thenReturn(pane);
            when(mock.showAndWait()).thenAnswer(inv -> pane.getButtonTypes().stream()
                    .filter(b -> b.getText().equals("Apply"))
                    .findFirst());
        });
             MockedStatic<DefaultStuffMaker> defaultMakerMock = mockStatic(DefaultStuffMaker.class)) {

            // mock generator to return false on isLegitimate(), which will fail 'checkRoadInputs' validation
            CarGenerator mockGenerator = mock(CarGenerator.class);
            when(mockGenerator.isLegitimate()).thenReturn(false);

            LightPlan mockLightPlan = mock(LightPlan.class);
            // Used lenient() here because checkRoadInputs might return false on generator and never check light plans
            lenient().when(mockLightPlan.isLegitimate()).thenReturn(true);

            defaultMakerMock.when(DefaultStuffMaker::createDefaultGenerator).thenReturn(mockGenerator);
            defaultMakerMock.when(DefaultStuffMaker::createDefaultLightPlan).thenReturn(mockLightPlan);

            when(mockSimulation.getStepCount()).thenReturn(0);

            // Act
            Method addRoadMethod = ModifyMapDialogMaker.class.getDeclaredMethod(
                    "addRoadDialog", Stage.class, ArrayList.class, Runnable.class);
            addRoadMethod.setAccessible(true);
            addRoadMethod.invoke(null, mockStage, roadParameters, mockPaintAll);

            // Assert
            // check that list remained empty because validation failed
            assertTrue(roadParameters.isEmpty(), "List should remain empty due to invalid inputs");
            assertFalse(AppContext.RUN_DETAILS.mapChanged, "Map changed flag should be false");
            verify(mockPaintAll, never()).run();
        }
    }
}
