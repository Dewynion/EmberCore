package dev.blufantasyonline.embercore.physics.raycast;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class Plane {
    public final Location point;
    public final Vector normal;

    public Plane(Location point, Vector normal) {
        this.point = point;
        this.normal = normal.normalize();
    }

    public Plane(Location a, Location b, Location c) {

    }

    public Plane(World world, Vector a, Vector b, Vector c) {

    }
}
