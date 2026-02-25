package app;

import core.sim.Simulation;
import core.utils.RunDetails;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.render.IRoadRenderer;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/****************************
 * Unit tests for AppContext class
 *
 * @author Michael Hladky
 * @version 1.0
 ****************************/
class AppContextTest {

    /**
     * clear and set up before and after each test to ensure that static fields in AppContext do not interfere between
     * tests, maintaining test isolation
     **/
    @BeforeEach
    @AfterEach
    void resetAppContext() {
        // clear all static fields in AppContext before and after each test to ensure test isolation
        AppContext.RENDERER = null;
        AppContext.CAR_FOLLOWING_MODEL = null;
        AppContext.LANE_CHANGING_MODEL = null;
        AppContext.SIMULATION = null;
        AppContext.RUN_DETAILS = null;
    }

    /**
     * test to verify that the private constructor can be invoked via reflection
     * to achieve 100% code coverage, ensuring no exception is thrown during instantiation
     **/
    @Test
    void privateConstructor_ShouldBeInstantiableViaReflection() throws Exception {
        // Act
        // get the private constructor of AppContext and make it accessible, then create an instance
        Constructor<AppContext> constructor = AppContext.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        AppContext instance = constructor.newInstance();

        // Assert
        // verify that the instance was created successfully and is not null
        assertNotNull(instance, "Instance should be created via reflection");
    }

    /**
     * test to verify that all static fields in AppContext can be set and retrieved correctly
     **/
    @Test
    void staticFields_CanBeSetAndRetrieved() {
        // Arrange
        // create mock objects for each of the static fields in AppContext
        IRoadRenderer mockRenderer = mock(IRoadRenderer.class);
        ICarFollowingModel mockCarModel = mock(ICarFollowingModel.class);
        ILaneChangingModel mockLaneModel = mock(ILaneChangingModel.class);
        Simulation mockSimulation = mock(Simulation.class);
        RunDetails mockRunDetails = new RunDetails();

        // Act
        // set each static field in AppContext to the corresponding mock object
        AppContext.RENDERER = mockRenderer;
        AppContext.CAR_FOLLOWING_MODEL = mockCarModel;
        AppContext.LANE_CHANGING_MODEL = mockLaneModel;
        AppContext.SIMULATION = mockSimulation;
        AppContext.RUN_DETAILS = mockRunDetails;

        // Assert
        // zkontrolujeme, zda globální proměnné skutečně drží ty objekty, které jsme do nich vložili
        assertEquals(mockRenderer, AppContext.RENDERER, "Renderer should be set correctly");
        assertEquals(mockCarModel, AppContext.CAR_FOLLOWING_MODEL, "Car following model should be set correctly");
        assertEquals(mockLaneModel, AppContext.LANE_CHANGING_MODEL, "Lane changing model should be set correctly");
        assertEquals(mockSimulation, AppContext.SIMULATION, "Simulation should be set correctly");
        assertEquals(mockRunDetails, AppContext.RUN_DETAILS, "Run details should be set correctly");
    }
}