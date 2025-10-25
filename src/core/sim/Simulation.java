package core.sim;

import core.model.Road;

public class Simulation {

    private final Road[] roads;

    public Simulation(Road[] roads) {
        this.roads = roads;
    }

    public void step() {
        for (Road r : roads) {
            if (r != null) {
                r.upadateRoad();
            }
        }
    }

    public Road[] getRoads() {
        return roads;
    }
}
