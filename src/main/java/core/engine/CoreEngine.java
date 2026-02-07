package core.engine;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/********************************************
 * Core engine class to manage the simulation loop
 *
 * @author Michael Hladky
 * @version 1.0
 ********************************************/
public class CoreEngine {

    /** tick runnable to be executed each period **/
    private final Runnable TICK;

    /** atomic boolean indicating if engine is running **/
    private final AtomicBoolean RUNNING = new AtomicBoolean(false);

    /** scheduled executor service for running the tick periodically **/
    private ScheduledExecutorService exec;

    /** period in milliseconds between ticks **/
    private long periodMs;

    /**
     * constructor for CoreEngine
     *
     * @param tick runnable to be executed each period
     * @param periodMs period in milliseconds between ticks
     **/
    public CoreEngine(Runnable tick, long periodMs) {
        if (tick == null) {
            throw new IllegalArgumentException("tick must not be null");
        }
        if (periodMs <= 0) {
            throw new IllegalArgumentException("periodMs must be > 0");
        }
        this.TICK = tick;
        this.periodMs = periodMs;
    }

    /**
     * start the core engine
     **/
    public synchronized void start() {
        if (RUNNING.get()) {
            return;
        }

        exec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "simulation-loop");
            t.setDaemon(true);
            return t;
        });
        exec.scheduleAtFixedRate(TICK, 0, periodMs, TimeUnit.MILLISECONDS);
        RUNNING.set(true);
    }

    /**
     * stop the core engine
     **/
    public synchronized void stop() {
        if (exec != null) {
            exec.shutdownNow();
            exec = null;
        }
        RUNNING.set(false);
    }

    /**
     * get if the core engine is running
     *
     * @return boolean indicating if the engine is running
     **/
    public boolean getRunning() {
        return RUNNING.get();
    }

    /**
     * set the period in milliseconds between ticks
     *
     * @param newPeriod new period in milliseconds
     **/
    public synchronized void setPeriodMs(long newPeriod) {
        if (newPeriod <= 0) {
            return;
        }
        this.periodMs = newPeriod;
        if (getRunning()) {
            stop();
            start();
        }
    }

    /**
     * get the period in milliseconds between ticks
     *
     * @return period in milliseconds
     **/
    public long getPeriodMs() {
        return periodMs;
    }
}
