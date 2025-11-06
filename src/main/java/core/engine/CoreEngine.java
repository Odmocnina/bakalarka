package core.engine;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoreEngine {
    private final Runnable TICK;
    private final AtomicBoolean RUNNING = new AtomicBoolean(false);
    private ScheduledExecutorService exec;
    private long periodMs;

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

    public synchronized void stop() {
        if (exec != null) {
            exec.shutdownNow();
            exec = null;
        }
        RUNNING.set(false);
    }

    public boolean getRunning() {
        return RUNNING.get();
    }

    public synchronized void setPeriodMs(long newPeriod) {
        if (newPeriod <= 0) return;
        this.periodMs = newPeriod;
        if (getRunning()) {
            stop();
            start();
        }
    }

    public long getPeriodMs() {
        return periodMs;
    }
}
