package dev.blufantasyonline.embercore.math.geometry;

import org.bukkit.util.Vector;


public final class Quaternion implements Cloneable {
    public static Quaternion zero() {
        return new Quaternion(0, 0, 0, 0);
    }

    private double scalar;
    private Vector vector;

    public Quaternion(double scalar, Vector vector) {
        this(scalar, vector.getX(), vector.getY(), vector.getZ());
    }

    public Quaternion(double w, double x, double y, double z) {
        this.scalar = w;
        vector = new Vector(x, y, z);
    }

    public double w() {
        return scalar;
    }

    public double x() {
        return vector.getX();
    }

    public double y() {
        return vector.getY();
    }

    public double z() {
        return vector.getZ();
    }

    public Quaternion add(Quaternion other) {
        this.scalar += other.scalar;
        this.vector.add(other.vector);
        return this;
    }

    public Quaternion subtract(Quaternion other) {
        this.scalar -= other.scalar;
        this.vector.subtract(other.vector);
        return this;
    }

    public Quaternion multiply(Quaternion other) {
        double dot = vector.dot(other.vector);
        Vector v1 = other.vector.clone().multiply(scalar);
        Vector v2 = vector.clone().multiply(other.scalar);

        scalar = scalar * other.scalar - dot;
        vector = v1.add(v2).add(vector.crossProduct(other.vector));

        return this;
    }

    public Quaternion multiply(float scalar) {
        return multiply((double) scalar);
    }

    public Quaternion multiply(double scalar) {
        this.scalar *= scalar;
        vector.multiply(scalar);
        return this;
    }

    public Quaternion conjugate() {
        vector.multiply(-1);
        return this;
    }

    public double magnitude() {
        return Math.sqrt(Math.pow(w(), 2) + Math.pow(x(), 2) + Math.pow(y(), 2) + Math.pow(z(), 2));
    }

    public double norm() {
        return magnitude();
    }

    public double dot(Quaternion other) {
        return scalar * other.scalar + vector.dot(other.vector);
    }

    public Quaternion normalize() {
        return multiply(1 / magnitude());
    }

    public Quaternion invert() {
        double n = Math.pow(norm(), 2);
        if (n == 1.0)
            // for unit quaternions we don't have to worry about division
            return conjugate();
        else
            return conjugate().multiply(1 / n);
    }

    public Quaternion clone() {
        try {
            return (Quaternion) super.clone();
        } catch (CloneNotSupportedException ex) {
            return new Quaternion(scalar, vector.getX(), vector.getY(), vector.getZ());
        }
    }
}
