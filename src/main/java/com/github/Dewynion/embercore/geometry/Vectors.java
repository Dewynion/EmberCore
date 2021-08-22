package com.github.Dewynion.embercore.geometry;

import org.bukkit.util.Vector;

public final class Vectors {
    private static final Vector ZERO = new Vector(0, 0, 0);
    private static final Vector UP = new Vector(0, 1, 0);
    private static final Vector DOWN = new Vector(0, -1, 0);
    private static final Vector LEFT = new Vector(-1, 0, 0);
    private static final Vector RIGHT = new Vector(1, 0, 0);
    private static final Vector FORWARD = new Vector(0, 0, 1);
    private static final Vector BACKWARD = new Vector(0, 0, -1);

    public static Vector zero() {
        return ZERO.clone();
    }

    public static Vector up() {
        return UP.clone();
    }

    public static Vector down() {
        return DOWN.clone();
    }

    public static Vector left() {
        return LEFT.clone();
    }

    public static Vector right() {
        return RIGHT.clone();
    }

    public static Vector forward() {
        return FORWARD.clone();
    }

    public static Vector backward() {
        return BACKWARD.clone();
    }
}
