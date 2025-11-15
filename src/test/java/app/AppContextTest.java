package app;

import core.sim.Simulation;
import core.utils.RunDetails;
import models.ICarFollowingModel;
import models.ILaneChangingModel;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ui.render.IRoadRenderer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit testy pro {@link AppContext}.
 */
public class AppContextTest {

    /**
     * Ověří, že AppContext má privátní konstruktor
     * (utility třída, nelze normálně instanciovat).
     */
    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<AppContext> constructor = AppContext.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()),
                "Constructor of AppContext should be private");

        // umožníme volání, jen abychom ověřili, že tam není nějaká chyba
        constructor.setAccessible(true);
        AppContext instance = constructor.newInstance();
        assertNotNull(instance, "Even private constructor should be invokable via reflection");
    }

    /**
     * Ověří, že statická pole jsou po startu JVM defaultně null.
     * (Pozn.: pokud někde jinde v testech AppContext používáš,
     * bude potřeba tenhle test pouštět izolovaně, nebo před ním
     * AppContext zresetovat.)
     */
    @Test
    void testStaticFieldsAreInitiallyNull() {
        // POZOR: tento test předpokládá, že před jeho spuštěním
        // nikdo na AppContext sahal. Pokud ano, je potřeba
        // hodnoty před testem vynulovat.
        assertNull(AppContext.RENDERER, "RENDERER should be null by default");
        assertNull(AppContext.CAR_FOLLOWING_MODEL, "CAR_FOLLOWING_MODEL should be null by default");
        assertNull(AppContext.LANE_CHANGING_MODEL, "LANE_CHANGING_MODEL should be null by default");
        assertNull(AppContext.SIMULATION, "SIMULATION should be null by default");
        assertNull(AppContext.RUN_DETAILS, "RUN_DETAILS should be null by default");
    }

    /**
     * Ověří, že do statických polí lze přiřadit hodnoty a že se pak vrací stejné instance.
     */
    @Test
    void testStaticFieldsAssignment() {
        IRoadRenderer renderer = mock(IRoadRenderer.class);
        ICarFollowingModel cfModel = mock(ICarFollowingModel.class);
        ILaneChangingModel lcModel = mock(ILaneChangingModel.class);
        Simulation simulation = mock(Simulation.class);
        RunDetails runDetails = new RunDetails();
        runDetails.timeBetweenSteps = 123;
        runDetails.showGui = true;
        runDetails.outputFile = null;

        AppContext.RENDERER = renderer;
        AppContext.CAR_FOLLOWING_MODEL = cfModel;
        AppContext.LANE_CHANGING_MODEL = lcModel;
        AppContext.SIMULATION = simulation;
        AppContext.RUN_DETAILS = runDetails;

        assertSame(renderer, AppContext.RENDERER, "RENDERER should return assigned instance");
        assertSame(cfModel, AppContext.CAR_FOLLOWING_MODEL, "CAR_FOLLOWING_MODEL should return assigned instance");
        assertSame(lcModel, AppContext.LANE_CHANGING_MODEL, "LANE_CHANGING_MODEL should return assigned instance");
        assertSame(simulation, AppContext.SIMULATION, "SIMULATION should return assigned instance");
        assertSame(runDetails, AppContext.RUN_DETAILS, "RUN_DETAILS should return assigned instance");
    }
}

