package dev.blufantasyonline.embercore.math.curves;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public abstract class Curve {
    /**
     * Returns the {@link Vector} that this curve evaluates to given a
     * parameter t, which indicates the interpolation amount.
     * @param t A double-precision floating point value in the range [0.0, 1.0].
     * @return A point in 3D space along the curve.
     */
    public abstract Vector evaluate(double t);

    /**
     * Creates an ordered list of in-world {@link Location}s based off of
     * this curve.
     * @param world The world in which to create the locations.
     * @param numPoints The number of locations to create.
     * @return An ordered list containing the provided number of {@link Location}s in
     * the given {@link World}.
     */
    public final ArrayList<Location> create(World world, int numPoints) {
        ArrayList<Location> locations = new ArrayList<>();
        double increment = 1.0 / numPoints;

        for (double t = 0.0; t <= 1.0; t += increment)
            locations.add(evaluate(t).toLocation(world));

        return locations;
    }
}
