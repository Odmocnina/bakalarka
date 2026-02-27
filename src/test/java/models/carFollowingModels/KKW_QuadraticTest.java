package models.carFollowingModels;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**********************************
 * Unit tests for the KKW_Quadratic car-following model, focusing on verifying
 * that the synchronization gap is calculated using the correct quadratic formula,
 * and that basic properties like name and ID are correct.
 *
 * @author Michael Hladky
 * @version 1.0
 **********************************/
class KKW_QuadraticTest {

    /**
     * Helper method: read parent's protected "acceleration" field using reflection,
     * because KKW_Quadratic depends on it in the formula:
     *
     * gap = d + v*t + v + beta * v / (2 * acceleration)
     */
    private double getAcceleration(KKW_Quadratic model) throws Exception {
        Field f = KKW_Linear.class.getDeclaredField("acceleration");
        f.setAccessible(true);
        return (double) f.get(model);
    }

    /**
     *  getSynchronizationGap() must follow the quadratic formula:
     *
     * gap = (int)( d + v * dt + v + beta * v / (2 * acceleration) )
     *
     * We test on a clean quadratic model with default values.
     */
    @Test
    void synchronizationGap_usesQuadraticFormula() throws Exception {
        KKW_Quadratic model = new KKW_Quadratic();

        double currentSpeed = 10.0;
        double d = 2.0;
        double k = 1.0;
        double dt = 1.5;
        double beta = 0.5;
        double a = getAcceleration(model);

        double expectedDouble =
                d + currentSpeed * dt + currentSpeed +
                        beta * currentSpeed / (2 * a);

        int expected = (int) expectedDouble;

        int result = model.getSynchronizationGap(currentSpeed, d, k, dt);

        assertEquals(expected, result,
                "KKW_Quadratic must compute synchronization gap using the quadratic formula.");
    }

    /**
     * verify behavior with lower speeds.
     */
    @Test
    void synchronizationGap_smallSpeed() throws Exception {
        KKW_Quadratic model = new KKW_Quadratic();

        double v = 2.0;
        double d = 2.0;
        double dt = 1.0;
        double k = 1.0;
        double a = getAcceleration(model);
        double beta = 0.5;

        double expectedDouble =
                d + v * dt + v + beta * v / (2 * a);

        int expected = (int) expectedDouble;

        int result = model.getSynchronizationGap(v, d, k, dt);

        assertEquals(expected, result,
                "Synchronization gap must work correctly even for low speeds.");
    }

    /**
     * name must be correct.
     */
    @Test
    void getName_returnsQuadraticName() {
        assertEquals("Kerner-Klenov-Wolf (quadratic)", new KKW_Quadratic().getName());
    }

    /**
     * ID must be correct.
     */
    @Test
    void getID_returnsQuadraticID() {
        assertEquals("kkw-quadratic", new KKW_Quadratic().getID());
    }

    /**
     *  inheritance sanity check — ensure that quadratic model still behaves
     * like linear model for parts not overridden.
     */
    @Test
    void inheritsLinearBehavior_notOverriddenMethodsStillWork() throws Exception {
        KKW_Quadratic model = new KKW_Quadratic();

        double cellSize = model.getCellSize();

        assertEquals(1.5, cellSize, 1e-9,
                "KKW_Quadratic should inherit cell size from KKW_Linear.");

        assertNotNull(model.getType(),
                "KKW_Quadratic should inherit a valid type from KKW_Linear.");
    }
}
