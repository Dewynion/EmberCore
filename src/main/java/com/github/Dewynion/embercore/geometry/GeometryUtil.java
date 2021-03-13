package com.github.Dewynion.embercore.util.geometry;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GeometryUtil {
    public static double MAX_ANGLE_DEGREES = 360.0;

    public static double clampAngle(double angle) {
        while (angle > MAX_ANGLE_DEGREES)
            angle -= MAX_ANGLE_DEGREES;
        while (angle < 0.0)
            angle += MAX_ANGLE_DEGREES;
        return angle;
    }

    /**
     * Converts the given rotation to radians and calls {@link #rotateAround(Location, Location, EulerAngles)}.
     */
    public static Location rotateAroundDegrees(Location point, Location origin, EulerAngles rotation) {
        return rotateAround(point, origin, rotation.toRadians());
    }

    /**
     * Rotates one point in space around another by the given rotation vector.
     * Rotations are executed counterclockwise around all axes.
     * @param point - The point to rotate from. Original is not affected.
     * @param origin - The point to rotate around.
     * @param rotation - Rotation vector, in radians. Use {@link #rotateAroundDegrees(Location, Location, EulerAngles)}
     *                for convenience if using degrees.
     * @return A clone of point rotated around origin by rotation.
     */
    public static Location rotateAround(Location point, Location origin, EulerAngles rotation) {
        return rotateY(
                rotateZ(
                        rotateX(point, origin, rotation.getX()),
                        origin, rotation.getZ()),
                origin, rotation.getY());
    }

    public static Location rotateX(Location point, Location origin, double rotation) {
        double rX = point.getX() - origin.getX(),
                rY = point.getY() - origin.getY(),
                rZ = point.getZ() - origin.getZ();
        double sinTheta = Math.sin(rotation),
                cosTheta = Math.cos(rotation);
        double y = rY * cosTheta - rZ * sinTheta,
                z = rZ * cosTheta + rY * sinTheta;
        return origin.clone().add(rX, y, z);
    }

    public static Location rotateY(Location point, Location origin, double rotation) {
        double rX = point.getX() - origin.getX(),
                rY = point.getY() - origin.getY(),
                rZ = point.getZ() - origin.getZ();
        double sinTheta = Math.sin(rotation),
                cosTheta = Math.cos(rotation);
        double x = rX * cosTheta + rZ * sinTheta,
                z = rZ * cosTheta - rX * sinTheta;
        return origin.clone().add(x, rY, z);
    }

    public static Location rotateZ(Location point, Location origin, double rotation) {
        double rX = point.getX() - origin.getX(),
                rY = point.getY() - origin.getY(),
                rZ = point.getZ() - origin.getZ();
        double sinTheta = Math.sin(rotation),
                cosTheta = Math.cos(rotation);
        double y = rY * cosTheta + rX * sinTheta,
                x = rX * cosTheta - rY * sinTheta;
        return origin.clone().add(x, y, rZ);
    }

    public static Vector axisRotation(Vector point, Vector ax, Vector ay, Vector az) {
        // formula for component c with point (x, y, z) and axes ax, ay, az:
        // c = (x * ax.c) + (y * ay.c) + (z * az.c)
        double x = (point.getX() * ax.getX()) + (point.getY() * ay.getX()) + (point.getZ() * az.getX());
        double y = (point.getX() * ax.getY()) + (point.getY() * ay.getY()) + (point.getZ() * az.getY());
        double z = (point.getX() * ax.getZ()) + (point.getY() * ay.getZ()) + (point.getZ() * az.getZ());
        return new Vector(x, y, z);
    }
}
