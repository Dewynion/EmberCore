package dev.blufantasyonline.embercore.math.geometry;

import org.bukkit.util.Vector;

public final class Quaternion {
    private double scalar;
    private Vector vector;

    public Quaternion(double scalar, double x, double y, double z) {
        this.scalar = scalar;
        vector = new Vector(x, y, z);
    }
}
