package dev.blufantasyonline.embercore.model;

import dev.blufantasyonline.embercore.math.geometry.Transform;

public class ModelInstance {
    protected Transform transform;

    public ModelInstance(Model model) {
        this(model, null);
    }

    public ModelInstance(Model model, Transform parent) {

    }
}
