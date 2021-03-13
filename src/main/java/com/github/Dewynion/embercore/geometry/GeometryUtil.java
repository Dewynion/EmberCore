package com.github.Dewynion.embercore.geometry;

import org.bukkit.Location;
import org.bukkit.util.Vector;

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
     *
     * @param point    - The point to rotate from. Original is not affected.
     * @param origin   - The point to rotate around.
     * @param rotation - Rotation vector, in radians. Use {@link #rotateAroundDegrees(Location, Location, EulerAngles)}
     *                 for convenience if using degrees.
     * @return A clone of point rotated around origin by rotation.
     */
    public static Location rotateAround(Location point, Location origin, EulerAngles rotation) {
        return rotateZ(
                rotateY(
                        rotateX(point, origin, rotation.getX()),
                        origin, rotation.getY()),
                origin, rotation.getZ());
    }

    public static Location rotateX(Location point, Location origin, double rotation) {
        double sinTheta = Math.sin(rotation),
                cosTheta = Math.cos(rotation);
        return rotateX(point, origin, sinTheta, cosTheta);
    }

    private static Location rotateX(Location point, Location origin, double sinTheta, double cosTheta) {
        double rX = point.getX() - origin.getX(),
                rY = point.getY() - origin.getY(),
                rZ = point.getZ() - origin.getZ();
        double y = rY * cosTheta - rZ * sinTheta,
                z = rZ * cosTheta + rY * sinTheta;
        return origin.clone().add(rX, y, z);
    }

    public static Location rotateY(Location point, Location origin, double rotation) {
        double sinTheta = Math.sin(rotation),
                cosTheta = Math.cos(rotation);
        return rotateY(point, origin, sinTheta, cosTheta);
    }

    private static Location rotateY(Location point, Location origin, double sinTheta, double cosTheta) {
        double rX = point.getX() - origin.getX(),
                rY = point.getY() - origin.getY(),
                rZ = point.getZ() - origin.getZ();
        double x = rX * cosTheta + rZ * sinTheta,
                z = rZ * cosTheta - rX * sinTheta;
        return origin.clone().add(x, rY, z);
    }

    public static Location rotateZ(Location point, Location origin, double rotation) {
        double sinTheta = Math.sin(rotation),
                cosTheta = Math.cos(rotation);
        return rotateZ(point, origin, sinTheta, cosTheta);
    }

    private static Location rotateZ(Location point, Location origin, double sinTheta, double cosTheta) {
        double rX = point.getX() - origin.getX(),
                rY = point.getY() - origin.getY(),
                rZ = point.getZ() - origin.getZ();
        double y = rY * cosTheta + rX * sinTheta,
                x = rX * cosTheta - rY * sinTheta;
        return origin.clone().add(x, y, rZ);
    }

    public static List<Location> rotateAllDegrees(List<Location> points, Location origin, EulerAngles rotation) {
        return rotateAll(points, origin, rotation.toRadians());
    }

    public static List<Location> rotateAll(List<Location> points, Location origin, EulerAngles rotation) {
        return rotateZ(
                rotateY(
                        rotateX(points, origin, rotation.getX()),
                        origin, rotation.getY()),
                origin, rotation.getZ());
    }

    public static List<Location> rotateX(List<Location> points, Location origin, double rotation) {
        double sinTheta = Math.sin(rotation),
                cosTheta = Math.cos(rotation);
        for (int i = 0; i < points.size(); i++) {
            Location l = points.get(i);
            l = rotateX(l, origin, sinTheta, cosTheta);
            points.set(i, l);
        }
        return points;
    }

    public static List<Location> rotateY(List<Location> points, Location origin, double rotation) {
        double sinTheta = Math.sin(rotation),
                cosTheta = Math.cos(rotation);
        for (int i = 0; i < points.size(); i++) {
            Location l = points.get(i);
            l = rotateY(l, origin, sinTheta, cosTheta);
            points.set(i, l);
        }
        return points;
    }

    public static List<Location> rotateZ(List<Location> points, Location origin, double rotation) {
        double sinTheta = Math.sin(rotation),
                cosTheta = Math.cos(rotation);
        for (int i = 0; i < points.size(); i++) {
            Location l = points.get(i);
            l = rotateZ(l, origin, sinTheta, cosTheta);
            points.set(i, l);
        }
        return points;
    }
}
