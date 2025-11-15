package core.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CoreEngineTest {

    private CoreEngine engineForCleanup; // pro jistotu stop po každém testu

    @AfterEach
    void tearDown() {
        if (engineForCleanup != null && engineForCleanup.getRunning()) {
            engineForCleanup.stop();
        }
    }

    @Test
    void constructor_nullTick_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new CoreEngine(null, 100)
        );
        assertEquals("tick must not be null", ex.getMessage());
    }

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

    @Test
    void start_setsRunningAndExecutesTick() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        CoreEngine engine = new CoreEngine(counter::incrementAndGet, 20);
        engineForCleanup = engine;

        assertFalse(engine.getRunning(), "Engine should not be running before start");

        engine.start();

        // necháme to chvíli běžet
        TimeUnit.MILLISECONDS.sleep(80);

        assertTrue(engine.getRunning(), "Engine should be running after start");
        assertTrue(counter.get() > 0, "Tick should have been executed at least once");

        engine.stop();
        assertFalse(engine.getRunning(), "Engine should not be running after stop");
    }

    @Test
    void start_isIdempotent_executorNotRecreated() throws Exception {
        CoreEngine engine = new CoreEngine(() -> {}, 50);
        engineForCleanup = engine;

        // první start
        engine.start();

        // získáme referenci na exec přes reflexi
        Field execField = CoreEngine.class.getDeclaredField("exec");
        execField.setAccessible(true);
        Object firstExec = execField.get(engine);

        // druhý start by neměl vytvořit nový executor
        engine.start();
        Object secondExec = execField.get(engine);

        assertSame(firstExec, secondExec, "Executor should not be recreated when start() is called again");
    }

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

    @Test
    void setPeriodMs_updatesPeriod_whenStopped() {
        CoreEngine engine = new CoreEngine(() -> {}, 100);
        engineForCleanup = engine;

        engine.setPeriodMs(50);
        assertEquals(50, engine.getPeriodMs(), "Period should be updated when newPeriod > 0 and engine is stopped");
    }

    @Test
    void setPeriodMs_updatesPeriodAndRestartsExecutor_whenRunning() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        CoreEngine engine = new CoreEngine(counter::incrementAndGet, 50);
        engineForCleanup = engine;

        engine.start();

        // reflexí získáme původní executor
        Field execField = CoreEngine.class.getDeclaredField("exec");
        execField.setAccessible(true);
        Object oldExec = execField.get(engine);

        engine.setPeriodMs(30);

        assertEquals(30, engine.getPeriodMs(), "Period should be updated to new value");
        assertTrue(engine.getRunning(), "Engine should still be running after setPeriodMs when it was running");

        Object newExec = execField.get(engine);
        assertNotSame(oldExec, newExec, "Executor should be recreated when period is changed while running");

        // ověříme, že tick dál běží
        int before = counter.get();
        TimeUnit.MILLISECONDS.sleep(70);
        int after = counter.get();
        assertTrue(after > before, "Tick should still be executed after period change");
    }
}

