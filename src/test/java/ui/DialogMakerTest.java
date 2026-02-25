package ui;

import app.AppContext;
import core.engine.CoreEngine;
import core.model.CarGenerator;
import core.model.LightPlan;
import core.sim.Simulation;
import core.utils.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Spinner;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/*************************
 * Unit tests for DialogMaker class, testing its static methods related to input validation, dialog creation, and list updates
 *
 * @author Michael Hladky
 * @version 1.0.0
 *************************/
@ExtendWith(MockitoExtension.class)
public class DialogMakerTest {

    /** Mocking Stage and CoreEngine to prevent actual UI interactions and engine calls during tests, as DialogMaker
     * methods often require these as parameters **/
    @Mock
    private Stage mockStage;

    /** Mocking CoreEngine to prevent actual engine interactions during tests, as some DialogMaker methods may call
     * engine methods **/
    @Mock
    private CoreEngine mockEngine;

    /** Before all tests, initialize JavaFX toolkit to prevent IllegalStateException when creating UI components in
     * tests **/
    @BeforeAll
    static void initJavaFX() {
        // initialize JavaFX toolkit to prevent IllegalStateException when creating UI components
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX toolkit already initialized, ignore
        }
    }

    /** Before each test, set up the application context with default values to prevent NullPointerExceptions and ensure
     * consistent state across tests **/
    @BeforeEach
    void setUp() {
        // AppContext initialization left uncommented as requested
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.SIMULATION = mock(Simulation.class);
    }

    /**
     * test to verify that the checkRoadInputs method returns false when the number of lanes is zero or negative
     **/
    @Test
    void checkRoadInputs_InvalidLanes_ShouldReturnFalse() {
        // Arrange
        LinkedList<CarGenerator> generators = new LinkedList<>();
        LinkedList<LightPlan> lightPlans = new LinkedList<>();

        // Act
        boolean resultZero = DialogMaker.checkRoadInputs(0, "50", "100", generators, lightPlans);
        boolean resultNegative = DialogMaker.checkRoadInputs(-1, "50", "100", generators, lightPlans);

        // Assert
        assertFalse(resultZero, "Should return false for 0 lanes");
        assertFalse(resultNegative, "Should return false for negative lanes");
    }

    /**
     * test to verify that the checkRoadInputs method returns false when speed or length are not valid numbers
     **/
    @Test
    void checkRoadInputs_InvalidSpeedOrLength_ShouldReturnFalse() {
        // Arrange
        LinkedList<CarGenerator> generators = new LinkedList<>();
        LinkedList<LightPlan> lightPlans = new LinkedList<>();

        // Act & Assert
        assertFalse(DialogMaker.checkRoadInputs(2, "invalid", "100", generators, lightPlans));
        assertFalse(DialogMaker.checkRoadInputs(2, "-10", "100", generators, lightPlans));
        assertFalse(DialogMaker.checkRoadInputs(2, "50", "invalid", generators, lightPlans));
        assertFalse(DialogMaker.checkRoadInputs(2, "50", "-50", generators, lightPlans));
    }

    /**
     * test to verify that addRoadParameters correctly creates a new RoadParameters object and adds it to the list
     **/
    @Test
    void addRoadParameters_ValidInputs_ShouldAddToList() {
        // Arrange
        ArrayList<RoadParameters> roadParamsList = new ArrayList<>();
        LinkedList<LightPlan> lightPlans = new LinkedList<>();
        LinkedList<CarGenerator> generators = new LinkedList<>();

        // Act
        DialogMaker.addRoadParameters(2, 50.0, 100.0, lightPlans, generators, roadParamsList);

        // Assert
        assertEquals(1, roadParamsList.size());
        RoadParameters added = roadParamsList.get(0);
        assertEquals(2, added.lanes);
        assertEquals(50.0, added.maxSpeed);
        assertEquals(100.0, added.length);
        assertEquals(lightPlans, added.lightPlan);
        assertEquals(generators, added.carGenerators);
    }

    /**
     * test to verify that warningDialog successfully creates an Alert and shows it
     **/
    @Test
    void warningDialog_ShouldCreateAndShowAlert() {
        // Arrange
        try (MockedConstruction<Alert> mockedAlert = mockConstruction(Alert.class,
                (mock, context) -> {
                    // mocking showAndWait to prevent blocking the test
                    when(mock.showAndWait()).thenReturn(Optional.of(ButtonType.OK));
                })) {

            // Act
            DialogMaker.warningDialog(mockStage, "Test Warning");

            // Assert
            assertEquals(1, mockedAlert.constructed().size(), "One Alert should be constructed");
            Alert constructedAlert = mockedAlert.constructed().get(0);
            verify(constructedAlert, times(1)).showAndWait();
        }
    }

    /**
     * test to verify that onCloseUnsavedChangesDialog returns true when user chooses to save
     * and calls the RoadXml save method
     **/
    @Test
    void onCloseUnsavedChangesDialog_UserClicksSave_ShouldSaveAndReturnTrue() {
        // Arrange
        AppContext.RUN_DETAILS.mapFile = "dummy.xml";

        try (MockedConstruction<Alert> mockedAlert = mockConstruction(Alert.class,
                (mock, context) -> {
                    // 1. Provide a real observable list so the production code can call .setAll(...) without NPE
                    ObservableList<ButtonType> buttonTypes = FXCollections.observableArrayList();
                    when(mock.getButtonTypes()).thenReturn(buttonTypes);

                    // 2. Dynamically return the exact "Save" button instance the production code created
                    when(mock.showAndWait()).thenAnswer(invocation -> {
                        ButtonType addedSaveButton = mock.getButtonTypes().stream()
                                .filter(b -> b.getText().equals("Save"))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("Save button not found in Alert!"));
                        return Optional.of(addedSaveButton);
                    });
                });
             MockedStatic<RoadXml> xmlMock = mockStatic(RoadXml.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            xmlMock.when(() -> RoadXml.saveAs(anyString())).thenReturn(true);

            // Act
            boolean result = DialogMaker.onCloseUnsavedChangesDialog(mockStage);

            // Assert
            assertTrue(result, "Should return true indicating application can proceed to close");
            xmlMock.verify(() -> RoadXml.saveAs("dummy.xml"), times(1));
            loggerMock.verify(() -> MyLogger.log(anyString(), anyString()), atLeastOnce());
        }
    }

    /**
     * test to verify that saveAsDialog opens a FileChooser and if a file is selected, it saves the map
     **/
    @Test
    void saveAsDialog_FileSelected_ShouldCallRoadXmlSaveAs() {
        // Arrange
        File mockFile = mock(File.class);
        when(mockFile.getAbsolutePath()).thenReturn("C:/test_map.xml");

        try (MockedConstruction<FileChooser> mockedFileChooser = mockConstruction(FileChooser.class,
                (mock, context) -> {
                    // simulate user selecting a file in the dialog
                    when(mock.showSaveDialog(mockStage)).thenReturn(mockFile);
                    when(mock.getExtensionFilters()).thenReturn(mock(javafx.collections.ObservableList.class));
                });
             MockedStatic<RoadXml> xmlMock = mockStatic(RoadXml.class);
             MockedStatic<MyLogger> loggerMock = mockStatic(MyLogger.class)) {

            xmlMock.when(() -> RoadXml.saveAs(anyString())).thenReturn(true);

            // Act
            DialogMaker.saveAsDialog(mockStage);

            // Assert
            xmlMock.verify(() -> RoadXml.saveAs("C:/test_map.xml"), times(1));
            loggerMock.verify(() -> MyLogger.log(contains("User saved map as"), anyString()), times(1));
        }
    }

    /**
     * test to verify that updateNumberOfLanes correctly expands the lists when number of lanes increases
     **/
    @Test
    void updateNumberOfLanes_IncreaseLanes_ShouldAddDefaultPlansAndGenerators() {
        // Arrange
        LinkedList<LightPlan> lightPlans = new LinkedList<>();
        LinkedList<CarGenerator> generators = new LinkedList<>();
        Spinner<Integer> mockSpinner = new Spinner<>();

        // initially 1 lane
        lightPlans.add(mock(LightPlan.class));
        generators.add(mock(CarGenerator.class));

        // mock DefaultStuffMaker to prevent it from accessing AppContext.CAR_FOLLOWING_MODEL
        try (MockedStatic<DefaultStuffMaker> stuffMock = mockStatic(DefaultStuffMaker.class)) {

            // tell the mock what to return when these methods are called
            stuffMock.when(DefaultStuffMaker::createDefaultLightPlan).thenReturn(mock(LightPlan.class));
            stuffMock.when(DefaultStuffMaker::createDefaultGenerator).thenReturn(mock(CarGenerator.class));

            // Act
            // increase to 3 lanes
            DialogMaker.updateNumberOfLanes(lightPlans, generators, 3, mockSpinner);

            // Assert
            assertEquals(3, lightPlans.size(), "Light plans list should have 3 items");
            assertEquals(3, generators.size(), "Generators list should have 3 items");

            // verify that the default maker methods were called exactly twice (for lane 2 and lane 3)
            stuffMock.verify(DefaultStuffMaker::createDefaultLightPlan, times(2));
            stuffMock.verify(DefaultStuffMaker::createDefaultGenerator, times(2));
        }
    }
}