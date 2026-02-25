package ui;

import app.AppContext;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.sim.Simulation;
import core.utils.DefaultStuffMaker;
import core.utils.MyLogger;
import core.utils.RoadXml;
import core.utils.RunDetails;
import core.utils.loading.RoadLoader;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/*****************************
 * Unit tests for NewMapDialogMaker, covering various scenarios of creating a new map, handling user interactions,
 * and verifying that the correct methods are called for XML writing, map loading, and logging.
 *
 * @author Michael Hladky
 * @version 1.0
 *****************************/
@ExtendWith(MockitoExtension.class)
public class NewMapDialogMakerTest {

    /** mock Stage for dialog ownership **/
    @Mock
    private Stage mockStage;

    /** mock Runnable for paintAll function to verify that it is called when expected **/
    @Mock
    private Runnable mockPaintAll;

    /**
     * initialize JavaFX toolkit before all tests to allow creation of UI components without exceptions
     **/
    @BeforeAll
    static void initJavaFX() {
        // initialize JavaFX toolkit to prevent IllegalStateException when creating UI components
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // toolkit is already initialized, we can ignore this
        }
    }

    /**
     * setup method to initialize the global application context before each test, ensuring that the AppContext is in a
     * known state and that dependencies like Simulation are mocked to prevent NullPointerExceptions during testing
     **/
    @BeforeEach
    void setUp() {
        // setting up the global application context
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.SIMULATION = mock(Simulation.class);
    }

    /**
     * test to verify that if the user clicks 'Create', the map is successfully written to XML,
     * the second dialog pops up, the user clicks 'Yes', the map is loaded, and paintAll is called.
     **/
    @Test
    void newMapDialog_CreateClicked_WriteSuccess_LoadSuccess_ShouldPaint() {
        // Arrange
        try (MockedConstruction<Dialog> dialogMock = mockConstruction(Dialog.class, (mock, context) -> {
            DialogPane pane = new DialogPane();
            when(mock.getDialogPane()).thenReturn(pane);
            when(mock.showAndWait()).thenAnswer(inv -> {
                // Return 'Create' for the first dialog, or 'Yes' for the second one
                Optional<ButtonType> btn = pane.getButtonTypes().stream()
                        .filter(b -> b.getText().equals("Create") || b.getText().equals("Yes"))
                        .findFirst();
                return btn.isPresent() ? btn : Optional.of(ButtonType.CANCEL);
            });
        });
             MockedStatic<RoadXml> xmlMock = mockStatic(RoadXml.class);
             MockedStatic<RoadLoader> loaderMock = mockStatic(RoadLoader.class);
             MockedStatic<DefaultStuffMaker> defaultMakerMock = mockStatic(DefaultStuffMaker.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {


            LinkedList<CarGenerator> mockGenerators = new LinkedList<>();
            mockGenerators.add(mock(CarGenerator.class));
            LinkedList<LightPlan> mockLightPlans = new LinkedList<>();
            mockLightPlans.add(mock(LightPlan.class));

            defaultMakerMock.when(() -> DefaultStuffMaker.createDefaultGenerator(anyInt())).thenReturn(mockGenerators);
            defaultMakerMock.when(() -> DefaultStuffMaker.createDefaultLightPlan(anyInt())).thenReturn(mockLightPlans);


            // mock successful XML write and successful map load
            xmlMock.when(() -> RoadXml.writeMapToXml(any(), anyInt(), anyString())).thenReturn(true);
            loaderMock.when(() -> RoadLoader.loadMap(anyString())).thenReturn(true);

            // Act
            NewMapDialogMaker.newMapDialog(mockStage, mockPaintAll);

            // Assert
            xmlMock.verify(() -> RoadXml.writeMapToXml(any(), anyInt(), eq("map.xml")), times(1));
            loaderMock.verify(() -> RoadLoader.loadMap("map.xml"), times(1));
            verify(mockPaintAll, times(1)).run();
            loggerMock.verify(() -> MyLogger.log(contains("New map created successfully"), anyString()), atLeastOnce());
        }
    }

    /**
     * test to verify that if writing the map to XML fails after clicking 'Create',
     * it logs an error and does NOT open the second dialog for map loading.
     **/
    @Test
    void newMapDialog_CreateClicked_WriteFail_ShouldNotAskToOpen() {
        // Arrange
        try (MockedConstruction<Dialog> dialogMock = mockConstruction(Dialog.class, (mock, context) -> {
            DialogPane pane = new DialogPane();
            when(mock.getDialogPane()).thenReturn(pane);
            when(mock.showAndWait()).thenAnswer(inv -> pane.getButtonTypes().stream()
                    .filter(b -> b.getText().equals("Create"))
                    .findFirst());
        });
             MockedStatic<RoadXml> xmlMock = mockStatic(RoadXml.class);
             MockedStatic<DefaultStuffMaker> defaultMakerMock = mockStatic(DefaultStuffMaker.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            LinkedList<CarGenerator> mockGenerators = new LinkedList<>();
            mockGenerators.add(mock(CarGenerator.class));

            LinkedList<LightPlan> mockLightPlans = new LinkedList<>();
            mockLightPlans.add(mock(LightPlan.class));

            defaultMakerMock.when(() -> DefaultStuffMaker.createDefaultGenerator(anyInt())).thenReturn(mockGenerators);
            defaultMakerMock.when(() -> DefaultStuffMaker.createDefaultLightPlan(anyInt())).thenReturn(mockLightPlans);

            // simulate failure when writing XML
            xmlMock.when(() -> RoadXml.writeMapToXml(any(), anyInt(), anyString())).thenReturn(false);

            // Act
            NewMapDialogMaker.newMapDialog(mockStage, mockPaintAll);

            // Assert
            xmlMock.verify(() -> RoadXml.writeMapToXml(any(), anyInt(), eq("map.xml")), times(1));
            loggerMock.verify(() -> MyLogger.log(contains("Failed to create new map"), anyString()), atLeastOnce());
            // Because it failed, it shouldn't try to open the new road dialog (paintAll won't be called)
            verify(mockPaintAll, never()).run();
        }
    }

    /**
     * test to verify that UI listeners (spinner change, button clicks) work without throwing exceptions,
     * triggering internal list modifications and inherited dialog openers.
     **/
    @Test
    @SuppressWarnings("unchecked")
    void newMapDialog_UIInteractions_ShouldTriggerListenersAndButtons() {
        // Arrange
        try (MockedConstruction<Dialog> dialogMock = mockConstruction(Dialog.class, (mock, context) -> {
            DialogPane pane = new DialogPane();
            when(mock.getDialogPane()).thenReturn(pane);
            when(mock.showAndWait()).thenAnswer(inv -> {
                Node content = pane.getContent();
                if (content instanceof GridPane grid) {

                    // 1. Find and interact with Spinner to trigger the valueProperty listener
                    grid.getChildren().stream()
                            .filter(n -> n instanceof Spinner)
                            .map(n -> (Spinner<Integer>) n)
                            .findFirst()
                            .ifPresent(spinner -> {
                                spinner.getValueFactory().setValue(3); // increase (adds roads)
                                spinner.getValueFactory().setValue(1); // decrease (removes roads)
                            });

                }
                // cancel out of whatever dialog is currently open
                return pane.getButtonTypes().stream()
                        .filter(b -> b.getButtonData() == javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE)
                        .findFirst();
            });
        });
             MockedStatic<DefaultStuffMaker> defaultMakerMock = mockStatic(DefaultStuffMaker.class)) {

            // Use real lists containing mocks to avoid NPEs on .get()
            LinkedList<CarGenerator> mockGenerators = new LinkedList<>();
            mockGenerators.add(mock(CarGenerator.class));

            LinkedList<LightPlan> mockLightPlans = new LinkedList<>();
            mockLightPlans.add(mock(LightPlan.class));

            defaultMakerMock.when(() -> DefaultStuffMaker.createDefaultGenerator(anyInt())).thenReturn(mockGenerators);
            defaultMakerMock.when(() -> DefaultStuffMaker.createDefaultLightPlan(anyInt())).thenReturn(mockLightPlans);

            // Act
            NewMapDialogMaker.newMapDialog(mockStage, mockPaintAll);

            // Assert
            // verify that the default maker was called more times because the spinner increased the road count.
            defaultMakerMock.verify(() -> DefaultStuffMaker.createDefaultGenerator(anyInt()), atLeast(3));
        }
    }

    /**
     * test to verify that if the user clicks 'Yes' on the private openNewRoadDialog but the loading fails,
     * it logs an error and does not repaint the map.
     **/
    @Test
    void openNewRoadDialog_YesClicked_LoadFail_ShouldNotPaint() throws Exception {
        // Arrange
        try (MockedConstruction<Dialog> dialogMock = mockConstruction(Dialog.class, (mock, context) -> {
            DialogPane pane = new DialogPane();
            when(mock.getDialogPane()).thenReturn(pane);
            when(mock.showAndWait()).thenAnswer(inv -> pane.getButtonTypes().stream()
                    .filter(b -> b.getText().equals("Yes"))
                    .findFirst());
        });
             MockedStatic<RoadLoader> loaderMock = mockStatic(RoadLoader.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            // simulate map loader returning false
            loaderMock.when(() -> RoadLoader.loadMap(anyString())).thenReturn(false);

            // Act
            Method openDialogMethod = NewMapDialogMaker.class.getDeclaredMethod(
                    "openNewRoadDialog", Stage.class, String.class, Runnable.class);
            openDialogMethod.setAccessible(true);
            openDialogMethod.invoke(null, mockStage, "test_map.xml", mockPaintAll);

            // Assert
            loaderMock.verify(() -> RoadLoader.loadMap("test_map.xml"), times(1));
            verify(mockPaintAll, never()).run();
            loggerMock.verify(() -> MyLogger.log(contains("Failed to open new map"), anyString()), atLeastOnce());
        }
    }

    /**
     * test to verify that if the user clicks 'No' on the private openNewRoadDialog,
     * it does nothing (does not try to load the map).
     **/
    @Test
    void openNewRoadDialog_NoClicked_ShouldNotLoadMap() throws Exception {
        // Arrange
        try (MockedConstruction<Dialog> dialogMock = mockConstruction(Dialog.class, (mock, context) -> {
            DialogPane pane = new DialogPane();
            when(mock.getDialogPane()).thenReturn(pane);
            when(mock.showAndWait()).thenAnswer(inv -> pane.getButtonTypes().stream()
                    .filter(b -> b.getText().equals("No"))
                    .findFirst());
        });
             MockedStatic<RoadLoader> loaderMock = mockStatic(RoadLoader.class)) {

            // Act
            Method openDialogMethod = NewMapDialogMaker.class.getDeclaredMethod(
                    "openNewRoadDialog", Stage.class, String.class, Runnable.class);
            openDialogMethod.setAccessible(true);
            openDialogMethod.invoke(null, mockStage, "test_map.xml", mockPaintAll);

            // Assert
            // loader should never be triggered if 'No' is clicked
            loaderMock.verify(() -> RoadLoader.loadMap(anyString()), never());
        }
    }
}