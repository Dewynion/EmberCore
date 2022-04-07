package dev.blufantasyonline.embercore.physics.raycast;

import dev.blufantasyonline.embercore.math.MathUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class Plane extends Geometry {
    public final Location origin;
    public final Vector normal;

    public Plane(Location point, Vector normal) {
        this.origin = point;
        this.normal = normal.normalize();
    }

    @Override
    public Intersection<Ray, Geometry> intersection(Ray ray) {
        Vector n = normal;
        Vector d = ray.direction.clone();
        Vector e = ray.origin.toVector();
        Vector p0 = origin.toVector();

        double dotNormal = d.dot(n);
        // ensure the denominator isn't zero or arbitrarily small
        if (MathUtil.zero(dotNormal))
            return new Intersection<>(ray, this, null);

        // t = (plane origin - ray origin) . (plane normal) / (ray direction . plane normal)
        double t = (p0.subtract(e)).dot(n) / dotNormal;

        // There's no intersection if t is negative or beyond the ray's length.
        if (t < 0.0 || t > ray.length)
            return new Intersection<>(ray, this, null);

        return new Intersection<>(ray, this, ray.origin.clone().add(d.multiply(t)));
    }
}
