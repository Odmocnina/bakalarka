package scenario;

import app.AppContext;
import core.engine.CoreEngine;
import core.model.Road;
import core.sim.Simulation;
import core.utils.OutputDetails;
import core.utils.RunDetails;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import ui.render.IRoadRenderer;
import ui.Window;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*********************************************
 * Base class for integration tests of the Window class.
 * Sets up a mocked environment for the Window, including a mocked Simulation and CoreEngine,
 * and provides helper methods for interacting with the UI components.
 *
 * @author Michael Hladky
 * @version 1.0
 *********************************************/
@ExtendWith(ApplicationExtension.class)
public abstract class BaseWindowIT extends ApplicationTest {

    /** window instance, that is going to be tested **/
    protected Window window;

    /** mocked dependencies of the Window class, that we can control in our tests **/
    protected Simulation mockSimulation;
    protected CoreEngine mockEngine;
    protected Stage primaryStage;

    /*
     * The @Start method is called by TestFX to set up the JavaFX environment before each test.
     * Here we create mocks for the Simulation and CoreEngine, as well as other dependencies like the car following model,
     * lane changing model, and renderer. We then set up the AppContext with these mocks and start the Window.
     * Finally, we inject the mocked CoreEngine into the Window instance using reflection, so that we can verify interactions with it in our tests.
     */
    @Start
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;

        mockSimulation = mock(Simulation.class);
        mockEngine = mock(CoreEngine.class);

        org.mockito.stubbing.Answer<Object> safeStringAnswer = invocation -> {
            if (invocation.getMethod().getReturnType() == String.class) {
                return "";
            }
            return org.mockito.Mockito.RETURNS_DEFAULTS.answer(invocation);
        };

        ICarFollowingModel mockCarFollowingModel = mock(ICarFollowingModel.class, safeStringAnswer);
        ILaneChangingModel mockLaneChangingModel = mock(ILaneChangingModel.class, safeStringAnswer);

        IRoadRenderer mockRenderer = mock(IRoadRenderer.class);
        OutputDetails mockOutputDetails = mock(OutputDetails.class);

//        ICarFollowingModel mockCarFollowingModel = mock(ICarFollowingModel.class);
//        ILaneChangingModel mockLaneChangingModel = mock(ILaneChangingModel.class);
//        IRoadRenderer mockRenderer = mock(IRoadRenderer.class);
//        OutputDetails mockOutputDetails = mock(OutputDetails.class);

        // set up of appContext
        AppContext.RUN_DETAILS = new RunDetails();
        AppContext.RUN_DETAILS.mapChanged = false;
        AppContext.RUN_DETAILS.mapLoaded = true;
        AppContext.RUN_DETAILS.log = new boolean[6];
        AppContext.RUN_DETAILS.timeBetweenSteps = 100;

        when(mockOutputDetails.getWhatToOutput()).thenReturn(new HashMap<>());
        AppContext.RUN_DETAILS.outputDetails = mockOutputDetails;

        AppContext.SIMULATION = mockSimulation;
        AppContext.CAR_FOLLOWING_MODEL = mockCarFollowingModel;
        AppContext.LANE_CHANGING_MODEL = mockLaneChangingModel;
        AppContext.RENDERER = mockRenderer;

        when(mockCarFollowingModel.getName()).thenReturn("Mocked Car Model");
        when(mockLaneChangingModel.getName()).thenReturn("Mocked Lane Model");
        when(mockSimulation.getRoads()).thenReturn(new Road[]{ mock(Road.class) });

        when(mockCarFollowingModel.getName()).thenReturn("Mocked Car Model");
        when(mockLaneChangingModel.getName()).thenReturn("Mocked Lane Model");
        when(mockCarFollowingModel.requestParameters()).thenReturn("");
        when(mockLaneChangingModel.requestParameters()).thenReturn("");
        when(mockSimulation.getRoads()).thenReturn(new Road[]{ mock(Road.class) });

        window = new Window();
        window.start(stage);

        // inject the mocked CoreEngine into the Window instance using reflection, so we can verify interactions with it in our tests
        Field engineField = Window.class.getDeclaredField("engine");
        engineField.setAccessible(true);
        engineField.set(window, mockEngine);
    }
    // --- BaseWindowIT.java ---

    // --- HELP METHODS FOR TESTING ---

    /**
     * find button in toolbar by its tooltip text, and assert that it is not null (exists in the UI)
     */
    protected ButtonBase getButtonByTooltip(String tooltipText) {
        ButtonBase btn = lookup((Node node) -> {
            if (node instanceof ButtonBase) {
                Tooltip tooltip = ((ButtonBase) node).getTooltip();
                return tooltip != null && tooltipText.equals(tooltip.getText());
            }
            return false;
        }).query();

        assertNotNull(btn, "Button with tooltip '" + tooltipText + "' was not found!");
        return btn;
    }

    /**
     * helper method to set the map loaded state in AppContext and update the mocked Simulation's roads accordingly.
     */
    protected void setMapLoadedState(boolean isLoaded) {
        AppContext.RUN_DETAILS.mapLoaded = isLoaded;
        if (!isLoaded) {
            when(mockSimulation.getRoads()).thenReturn(new Road[0]);
        } else {
            when(mockSimulation.getRoads()).thenReturn(new Road[]{ mock(Road.class) });
        }

        // force the UI to update by changing the stage width slightly, which will trigger a layout pass and update the state of the buttons
        Platform.runLater(() -> primaryStage.setWidth(primaryStage.getWidth() + 1));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Recursively searches for a MenuItem by its exact text across the entire MenuBar.
     * This bypasses the need to physically click and open menus in TestFX, making tests 100% stable.
     *
     * @param itemText exact text of the menu item
     * @return found MenuItem
     */
    protected MenuItem getMenuItemByText(String itemText) {
        MenuBar menuBar = lookup((Node node) -> node instanceof javafx.scene.control.MenuBar).query();

        for (Menu menu : menuBar.getMenus()) {
            MenuItem found = searchMenuItem(menu, itemText);
            if (found != null) {
                return found;
            }
        }

        Assertions.fail("MenuItem with text '" + itemText + "' was not found!");
        return null;
    }

    /**
     * Recursive helper for getMenuItemByText
     */
    private MenuItem searchMenuItem(MenuItem currentItem, String targetText) {
        // If the current item matches the text, return it
        if (targetText.equals(currentItem.getText())) {
            return currentItem;
        }
        // If the current item is a Menu (has children), search its children
        if (currentItem instanceof Menu) {
            for (MenuItem subItem : ((Menu) currentItem).getItems()) {
                MenuItem found = searchMenuItem(subItem, targetText);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}