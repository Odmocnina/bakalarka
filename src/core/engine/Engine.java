package core.engine;

import app.AppContext;
import core.model.Road;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Engine {

    private Timeline loop;
    private Duration interval;
    private final Runnable tick;

    public Engine(Runnable tick, Duration interval) {
        if (tick == null) {
            throw new IllegalArgumentException("tick must not be null");
        }
        if (interval == null || interval.lessThanOrEqualTo(Duration.ZERO)) {
            throw new IllegalArgumentException("interval must be > 0");
        }
        this.tick = tick;
        this.interval = interval;
    }

    public void start() {
        if (isRunning()) {
            return;
        }
        loop = new Timeline(new KeyFrame(interval, e -> tick.run()));
        loop.setCycleCount(Animation.INDEFINITE);
        loop.play();
    }

    public void stop() {
        if (loop != null) {
            loop.stop();
            loop = null;
        }
    }

    public boolean isRunning() {
        return loop != null && loop.getStatus() == Animation.Status.RUNNING;
    }

    /** změní interval; pokud běží, přestaví smyčku za běhu */
    public void setInterval(Duration newInterval) {
        if (newInterval == null || newInterval.lessThanOrEqualTo(Duration.ZERO)) return;
        this.interval = newInterval;
        if (isRunning()) {
            stop();
            start();
        }
    }

    public Duration getInterval() {
        return interval;
    }

}
