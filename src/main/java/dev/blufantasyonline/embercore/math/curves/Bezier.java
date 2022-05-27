package dev.blufantasyonline.embercore.math.curves;

import dev.blufantasyonline.embercore.math.MathUtil;
import org.bukkit.util.Vector;

public final class Bezier extends Curve {
    /**
     * Bézier curve basis matrix. Stored in [row][column] format.
     * May change to a proper matrix implementation later. We just don't know.
     **/
    public static final double[][] BASIS_MATRIX = {
            {-1,  3, -3,  1},
            { 3, -6,  3,  0},
            {-3,  3,  0,  0},
            { 1,  0,  0,  0},
    };

    /**
     *     The points that comprise this curve.
     *     Index 0 is the start point, index 3 is the end point,
     *     index 1 is the first control point, and index 2 is the second.
     */
    private Vector[] points = new Vector[4];

    /**
     * The coefficients for the equations corresponding to the XYZ components of
     * any point on this curve.
     */
    private double[] x, y, z = new double[4];

    public Bezier(Vector p0, Vector p1, Vector p2, Vector p3) {
        points[0] = p0.clone();
        points[1] = p1.clone();
        points[2] = p2.clone();
        points[3] = p3.clone();

        // for each point,
        for (int i = 0; i < 4; i++) {
            // initialize the coefficient
            x[i] = 0.0;
            y[i] = 0.0;
            z[i] = 0.0;
            // then add this
            for (int k = 0; k < 4; k++) {
                x[i] += BASIS_MATRIX[k][i] * points[i].getX();
                y[i] += BASIS_MATRIX[k][i] * points[i].getY();
                z[i] += BASIS_MATRIX[k][i] * points[i].getZ();
            }
        }
    }

    public Vector evaluate(double t) {
        t = MathUtil.clampOne(t);
        // XYZ components of the resulting point
        double pX = 0.0, pY = 0.0, pZ = 0.0;

        // x[0] * t^3 + x[1] * t^2 + x[2] * t + x[3] * 1 for each component
        for (int i = 0; i < 4; i++) {
            double exp = 4 - (i + 1);
            pX += x[i] * Math.pow(t, exp);
            pY += y[i] * Math.pow(t, exp);
            pZ += z[i] * Math.pow(t, exp);
        }

        return new Vector(pX, pY, pZ);
    }
}
