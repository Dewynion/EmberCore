package dev.blufantasyonline.embercore.math.geometry;

import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public final class Transform {
    private Transform parent;
    private Set<Transform> children = new HashSet<>();
    private Vector translation;
    private Quaternion rotation;

    public Transform() {

    }

    public boolean isRoot() {
        return parent == null;
    }

    public Transform getRoot() {
        Transform current = this;
        while (current.parent != null)
            current = current.parent;
        return current;
    }


}
