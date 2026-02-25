package core.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**********************************
 * Unit tests for CoreEngine class
 *
 * @author Michael Hladky
 * @version 1.0
 ****************************/
class CoreEngineTest {

    /** reference to engine for cleanup after tests that start it **/
    private CoreEngine engineForCleanup;

    /**
     * cleanup method to stop any engine that was started during a test
     **/
    @AfterEach
    void tearDown() {
        if (engineForCleanup != null && engineForCleanup.getRunning()) {
            engineForCleanup.stop();
        }
    }

    /**
     * test to verify that passing a null tick runnable to the constructor throws an IllegalArgumentException
     **/
    @Test
    void constructor_nullTick_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new CoreEngine(null, 100)
        );
        assertEquals("tick must not be null", ex.getMessage());
    }

    /**
     * test to verify that passing a zero or negative period to the constructor throws an IllegalArgumentException
     **/
    @Test
    void constructor_nonPositivePeriod_throwsException() {
        IllegalArgumentException exZero = assertThrows(
                IllegalArgumentException.class,
                () -> new CoreEngine(() -> {}, 0)
        );
        assertEquals("periodMs must be > 0", exZero.getMessage());

        IllegalArgumentException exNegative = assertThrows(
                IllegalArgumentException.class,
                () -> new CoreEngine(() -> {}, -10)
        );
        assertEquals("periodMs must be > 0", exNegative.getMessage());
    }

    /**
     * test to verify that the start method sets the engine's running state to true and successfully
     * executes the tick runnable multiple times
     **/
    @Test
    void start_setsRunningAndExecutesTick() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        CoreEngine engine = new CoreEngine(counter::incrementAndGet, 20);
        engineForCleanup = engine;

        assertFalse(engine.getRunning(), "Engine should not be running before start");

        engine.start();

        // let it go for a while
        TimeUnit.MILLISECONDS.sleep(80);

        assertTrue(engine.getRunning(), "Engine should be running after start");
        assertTrue(counter.get() > 0, "Tick should have been executed at least once");

        engine.stop();
        assertFalse(engine.getRunning(), "Engine should not be running after stop");
    }

    /**
     * test to verify that calling the start method multiple times on an already running engine is
     * idempotent and does not recreate the internal executor
     **/
    @Test
    void start_isIdempotent_executorNotRecreated() throws Exception {
        CoreEngine engine = new CoreEngine(() -> {}, 50);
        engineForCleanup = engine;

        // first start
        engine.start();

        // get reference to exec before second start via reflexion
        Field execField = CoreEngine.class.getDeclaredField("exec");
        execField.setAccessible(true);
        Object firstExec = execField.get(engine);

        // second start should not make a new executor
        engine.start();
        Object secondExec = execField.get(engine);

        assertSame(firstExec, secondExec, "Executor should not be recreated when start() is called again");
    }

    /**
     * test to verify that the stop method halts the engine, sets the running state to false,
     * and prevents further tick executions
     **/
    @Test
    void stop_stopsRunningAndNoMoreTicks() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        CoreEngine engine = new CoreEngine(counter::incrementAndGet, 20);
        engineForCleanup = engine;

        engine.start();
        TimeUnit.MILLISECONDS.sleep(80);
        int beforeStop = counter.get();

        engine.stop();
        assertFalse(engine.getRunning(), "Engine should not be running after stop");

        // wait for stop
        TimeUnit.MILLISECONDS.sleep(80);
        int afterWait = counter.get();

        // allow max one tick after stop
        assertTrue(afterWait - beforeStop <= 1,
                "Tick count should not increase by more than 1 after engine is stopped");
    }

    /**
     * test to verify that calling setPeriodMs with zero or a negative value is ignored and
     * does not change the period when the engine is stopped
     **/
    @Test
    void setPeriodMs_ignoresNonPositive_whenStopped() {
        CoreEngine engine = new CoreEngine(() -> {}, 100);
        engineForCleanup = engine;

        assertEquals(100, engine.getPeriodMs());

        engine.setPeriodMs(0);
        assertEquals(100, engine.getPeriodMs(), "Period should not change when newPeriod is 0");

        engine.setPeriodMs(-5);
        assertEquals(100, engine.getPeriodMs(), "Period should not change when newPeriod is negative");
    }

    /**
     * test to verify that calling setPeriodMs with a valid positive value successfully updates
     * the period when the engine is stopped
     **/
    @Test
    void setPeriodMs_updatesPeriod_whenStopped() {
        CoreEngine engine = new CoreEngine(() -> {}, 100);
        engineForCleanup = engine;

        engine.setPeriodMs(50);
        assertEquals(50, engine.getPeriodMs(), "Period should be updated when newPeriod > 0 and engine is stopped");
    }

    /**
     * test to verify that calling setPeriodMs with a valid value while the engine is running successfully
     * updates the period, restarts the internal executor, and continues executing the tick runnable
     **/
    @Test
    void setPeriodMs_updatesPeriodAndRestartsExecutor_whenRunning() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        CoreEngine engine = new CoreEngine(counter::incrementAndGet, 50);
        engineForCleanup = engine;

        engine.start();

        // get reference to exec before changing period via reflexion
        Field execField = CoreEngine.class.getDeclaredField("exec");
        execField.setAccessible(true);
        Object oldExec = execField.get(engine);

        engine.setPeriodMs(30);

        assertEquals(30, engine.getPeriodMs(), "Period should be updated to new value");
        assertTrue(engine.getRunning(), "Engine should still be running after setPeriodMs when it was running");

        Object newExec = execField.get(engine);
        assertNotSame(oldExec, newExec, "Executor should be recreated when period is changed while running");

        // check that tick is still executed with new period
        int before = counter.get();
        TimeUnit.MILLISECONDS.sleep(70);
        int after = counter.get();
        assertTrue(after > before, "Tick should still be executed after period change");
    }
}