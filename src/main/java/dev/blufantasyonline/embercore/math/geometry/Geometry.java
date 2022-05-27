package dev.blufantasyonline.embercore.math.geometry;

import dev.blufantasyonline.embercore.physics.raycast.Intersection;
import dev.blufantasyonline.embercore.physics.raycast.Ray;

public abstract class Geometry {
    public abstract Intersection<Ray, Geometry> intersection(Ray ray);
}
