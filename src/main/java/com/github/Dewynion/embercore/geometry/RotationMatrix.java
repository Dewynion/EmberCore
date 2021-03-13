package com.github.Dewynion.embercore.geometry;

import com.github.Dewynion.embercore.EmberCore;
import org.bukkit.Rotation;
import org.bukkit.util.Vector;

import java.util.logging.Level;

public class Matrix4x4 {
    // Row-major

    // First row
    private double m11;
    private double m21;
    private double m31;
    private double m41;

    // Second row
    private double m12;
    private double m22;
    private double m32;
    private double m42;

    // Third row
    private double m13;
    private double m23;
    private double m33;
    private double m43;

    // Fourth row
    private double m14;
    private double m24;
    private double m34;
    private double m44;

    public static Matrix4x4 identity() {
        Matrix4x4 matrix = transformationMatrix();
        matrix.setU(Vectors.RIGHT);
        matrix.setV(Vectors.UP);
        matrix.setW(Vectors.FORWARD);
        matrix.setT(Vectors.ZERO);
        return matrix;
    }

    public static Matrix4x4 transformationMatrix() {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.m41 = 0;
        matrix.m42 = 0;
        matrix.m43 = 0;
        matrix.m44 = 1;
    }

    public static Matrix4x4 aboutAxisDegrees(Vector axis, double angle) {
        Matrix4x4 matrix = transformationMatrix();

        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        double nx = axis.getX();
        double ny = axis.getY();
        double nz = axis.getZ();


    }

    public void setU(Vector u) {
        setU(u.getX(), u.getY(), u.getZ(), 0.0);
    }

    public void setU(double x, double y, double z, double q) {
        m11 = x;
        m12 = y;
        m13 = z;
        m14 = q;
    }

    public Vector u() {
        return new Vector(m11, m12, m13);
    }

    public void setV(Vector v) {
        setV(v.getX(), v.getY(), v.getZ(), 0.0);
    }

    public void setV(double x, double y, double z, double q) {
        m21 = x;
        m22 = y;
        m23 = z;
        m24 = q;
    }

    public Vector v() {
        return new Vector(m21, m22, m23);
    }

    public void setW(Vector w) {
        setU(w.getX(), w.getY(), w.getZ(), 0.0);
    }

    public void setW(double x, double y, double z, double q) {
        m31 = x;
        m32 = y;
        m33 = z;
        m34 = q;
    }

    public Vector w() {
        return new Vector(31, 32, 33);
    }

    public void setT(Vector t) {
        m41 = t.getX();
        m42 = t.getY();
        m43 = t.getZ();
    }

    public Vector t() {
        return new Vector(m41, m42, m43);
    }

    public Matrix4x4 multiply(Matrix4x4 other) {
        Vector u = u();
        Vector v = v();
        Vector w = w();
        Vector t = t();
        Vector ou = other.u();
        Vector ov = other.v();
        Vector ow = other.w();
        Vector ot = other.t();
        double m11 =
    }
}
