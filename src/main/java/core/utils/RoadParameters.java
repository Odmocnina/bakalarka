package core.utils;

import core.model.CarGenerator;
import core.model.LightPlan;
import javafx.scene.effect.Light;

import java.util.LinkedList;

public class RoadParameters {
    public double maxSpeed;
    public double length;
    public int lanes;
    public LinkedList<LightPlan> lightPlan;
    public LinkedList<CarGenerator> carGenerators;
}
