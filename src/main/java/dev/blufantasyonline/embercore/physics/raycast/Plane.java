package dev.blufantasyonline.embercore.physics.raycast;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class Plane {
    public final Location origin;
    public final Vector normal;

    public Plane(Location point, Vector normal) {
        this.origin = point;
        this.normal = normal.normalize();
    }
}
