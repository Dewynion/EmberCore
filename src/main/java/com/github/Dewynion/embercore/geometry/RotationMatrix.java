package com.github.Dewynion.embercore.geometry;

import org.bukkit.util.Vector;

@Deprecated
public class RotationMatrix {
    // column 1 - x axis
    private Vector u;
    // column 2 - y axis
    private Vector v;
    // column 3 - z axis
    private Vector w;

    public RotationMatrix() {
        u = Vectors.zero();
        v = Vectors.zero();
        w = Vectors.zero();
    }

    public static RotationMatrix fromEulerAngles(EulerAngles rotation) {
        Vector zAxis = rotation.toDirectionVector();
        Vector xAxis = new Vector(-zAxis.getZ(), zAxis.getY(), zAxis.getX());
        Vector yAxis = zAxis.clone().crossProduct(xAxis).normalize();
        xAxis = yAxis.clone().crossProduct(zAxis).normalize();

        return new RotationMatrix().setU(xAxis).setY(yAxis).setZ(zAxis);
    }

    public Vector u() {
        return u.clone();
    }

    public Vector v() {
        return v.clone();
    }

    public Vector w() {
        return w.clone();
    }

    public RotationMatrix setU(Vector u) {
        this.u = u.clone();
        return this;
    }

    public RotationMatrix setV(Vector v) {
        this.v = v.clone();
        return this;
    }

    public RotationMatrix setW(Vector w) {
        this.w = w.clone();
        return this;
    }

    public Vector x() {
        return new Vector(u.getX(), v.getX(), w.getX());
    }

    public Vector y() {
        return new Vector(u.getY(), v.getY(), w.getY());
    }

    public Vector z() {
        return new Vector(u.getZ(), v.getZ(), w.getZ());
    }

    public RotationMatrix setX(Vector x) {
        u.setX(x.getX());
        v.setX(x.getY());
        w.setX(x.getZ());
        return this;
    }

    public RotationMatrix setY(Vector y) {
        u.setY(y.getX());
        v.setY(y.getY());
        w.setY(y.getZ());
        return this;
    }

    public RotationMatrix setZ(Vector z) {
        u.setZ(z.getX());
        v.setZ(z.getY());
        w.setZ(z.getZ());
        return this;
    }

    public RotationMatrix multiply(RotationMatrix other) {
        Vector x = x();
        Vector y = y();
        Vector z = z();
        Vector a = other.u();
        Vector b = other.v();
        Vector c = other.w();
        return setX(new Vector(x.clone().dot(a), x.clone().dot(b), x.clone().dot(c)))
                .setY(new Vector(y.clone().dot(a), y.clone().dot(b), y.clone().dot(c)))
                .setZ(new Vector(z.clone().dot(a), z.clone().dot(b), z.clone().dot(c)));
    }

    public RotationMatrix multiply(double mult) {
        u.multiply(mult);
        v.multiply(mult);
        w.multiply(mult);
        return this;
    }

    public Vector applyTo(Vector point) {
        Vector xAxis = x();
        Vector yAxis = y();
        Vector zAxis = z();
        double x = point.clone().dot(xAxis);
        double y = point.clone().dot(yAxis);
        double z = point.clone().dot(zAxis);
        return new Vector(x, y, z);
    }

    public RotationMatrix clone() {
        return new RotationMatrix()
                .setU(u)
                .setV(v)
                .setW(w);
    }

    public static RotationMatrix angleAxis(Vector axis, double angle) {
        double u = axis.getX();
        double v = axis.getY();
        double w = axis.getZ();
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        // u^2 + (1 - u^2)cos, uv(1-cos) - wsin, uw(1-cos) + vsin
        Vector x = new Vector(u * u + (1 - u * u) * cos, u * v * (1 - cos) - w * sin, u * w * (1 - cos) + v * sin);
        // uv(1-cos) + wsin, v^2 + (1-v^2)cos, vw(1-cos) - usin
        Vector y = new Vector(u * v * (1 - cos) + w * sin, v * v + (1 - v * v) * cos, v * w * (1 - cos) - u * sin);
        // uw(1-cos) - vsin, vw(1-cos) + usin, w^2 + (1-w^2)cos
        Vector z = new Vector(u * w * (1 - cos) - v * sin, v * w * (1 - cos) + u * sin, w * w + (1 - w * w) * cos);
        return new RotationMatrix().setX(x)
                .setY(y)
                .setZ(z);
    }
}
