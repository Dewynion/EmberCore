package dev.blufantasyonline.embercore.physics.raycast;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public final class BoundingVolume {
    private Set<BoundingVolume> children = new HashSet<>();
    private Set<BoundingBox> contents = new HashSet<>();
    private Vector min, max;

    public BoundingVolume(Vector min, Vector max) {
        this.min = min.clone();
        this.max = max.clone();
    }

    public Vector getMin() {
        return min.clone();
    }

    public Vector getMax() {
        return max.clone();
    }
}
