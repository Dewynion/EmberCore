package dev.blufantasyonline.embercore.physics.raycast;

import org.bukkit.Location;

public final class Intersection<T, U> {
    public final T first;
    public final U second;
    private final Location point;

    public Intersection(T first, U second, Location point) {
        this.first = first;
        this.second = second;
        this.point = point;
    }

    public Location getHitPoint() {
        return point.clone();
    }

    public boolean hit() {
        return point != null;
    }
}
