package com.github.Dewynion.embercore.geometry;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorUtil {
    public static boolean isInRange(Location source, Location target, float visionRadius, double visionRange) {
        // if the source location is blind
        if (visionRadius == 0f || visionRange == 0f)
            return false;

        // source's direction
        Vector dir = source.getDirection().normalize();
        // difference between source and target locations
        Vector diff = target.toVector().subtract(source.toVector());
        double angle = angleBetweenDegrees(dir, diff);

        // Since we're only checking the angle in one direction rather than two, halve the vision radius
        if (angle <= visionRadius / 2)
            // true if within radius and within vision range
            // square roots are demanding and this might be used many times
            // so I just compare squares
            return source.distanceSquared(target) <= Math.pow(visionRange, 2);
        else
            // false if not in vision radius
            return false;
    }

    public static double angleBetweenDegrees(Vector v1, Vector v2) {
        // dot ∈ [-1.0, 1.0]
        // dot = 0 means the target is at a 90 degree angle either left or right
        // dot = 1.0 means the target is directly ahead (0 degrees)
        // dot = -1.0 means the target is directly behind (180 degrees)
        // after some basic algebra:
        return (-90 * v1.dot(v2)) + 90;
    }
}
