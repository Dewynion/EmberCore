package dev.blufantasyonline.embercore.math.geometry;

import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public final class Transform {
    private Transform parent;
    private Transform root;

    private Set<Transform> children = new HashSet<>();
    private Vector translation;
    private Quaternion rotation;

    public Transform() {
        this(Vectors.zero(), Quaternion.zero());
    }

    public Transform(Vector translation, Quaternion rotation) {
        this(translation, rotation, null);
    }

    public Transform(Transform parent) {
        this(Vectors.zero(), Quaternion.zero(), parent);
    }

    public Transform(Vector translation, Quaternion rotation, Transform parent) {
        this.translation = translation;
        this.rotation = rotation;
        setParent(parent);
    }

    public boolean isRoot() {
        return parent == null;
    }

    public Transform getRoot() {
        if (root == null) {
            root = this;
            while (!root.isRoot())
                root = root.parent;
        }
        return root;
    }

    public Transform setParent(Transform other) {
        parent = other;
        if (parent == null)
            root = this;
        else
            root = parent.root;
        return this;
    }

    public Transform translate(Vector translate) {
        translation.add(translate);
        return this;
    }

    public Transform rotate(Quaternion rotate) {
        rotation.add(rotate);
        return this;
    }
}
