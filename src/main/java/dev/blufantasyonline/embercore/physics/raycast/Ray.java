package dev.blufantasyonline.embercore.physics.raycast;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class Ray {
    public final Location origin;
    public final Vector direction;
    public final double length;

    public Ray(Location origin, Vector direction, double length) {
        this.origin = origin;
        this.direction = direction.normalize();
        this.length = Math.abs(length);
    }

    public static Ray normal(Location origin, Vector direction) {
        return new Ray(origin, direction, 1.0);
    }
}
