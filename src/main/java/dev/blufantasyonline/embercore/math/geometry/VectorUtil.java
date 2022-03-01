package dev.blufantasyonline.embercore.math.geometry;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public final class VectorUtil {
    /**
     * Determines if the source location can "see" the target location. Does not take line of sight into account.
     * <br><br>
     * An example use case would be having a "guard" NPC detect players up to 10 meters and 60 degrees in front of them,
     * allowing players to sneak behind the NPC without attracting its attention.
     * @param visionRadius The angle, in degrees, that the source's vision covers. A vision radius of 90 degrees
     *                     can see up to 45 degrees left or right, covering a total of 90 degrees of vision.
     * @param visionRange The maximum distance, in meters (blocks), that the source can "see".
     */
    public static boolean canSee(Location source, Location target, float visionRadius, double visionRange) {
        // if the source location is blind
        if (visionRadius == 0f || visionRange == 0f)
            return false;

        // source's direction
        Vector dir = source.getDirection().normalize();
        // difference between source and target locations
        Vector diff = target.toVector().subtract(source.toVector());
        double angle = Math.toDegrees(dir.angle(diff));

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

    public static Vector direction(Location from, Entity to) {
        return direction(from, to.getLocation());
    }

    public static Vector direction(Entity from, Location to) {
        return direction(from.getLocation(), to);
    }

    public static Vector direction(Entity from, Entity to) {
        return direction(from.getLocation(), to.getLocation());
    }

    public static Vector direction(Location from, Location to) {
        return direction(from.toVector(), to.toVector());
    }

    public static Vector direction(Vector from, Vector to) {
        return from.clone().subtract(to).normalize();
    }
}
