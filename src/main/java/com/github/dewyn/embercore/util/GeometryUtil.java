package com.github.dewyn.embercore.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GeometryUtil {
    public static double MAX_ANGLE_DEGREES = 360.0;

    /** Generates a list of Locations that form a line from start to end.
     * @param start Start point of the line.
     * @param end Endpoint of the line.
     * @param points Number of Locations in the resulting line.
     * @return A list of Locations.
     **/
    public static List<Location> line(Location start, Location end, int points) {
        List<Location> locations = new ArrayList<>();
        // subtracting 1 ensures that one location will always be at the endpoint
        // since the final iteration of the for loop below will multiply increment by points - 1.
        Vector increment = end.toVector().subtract(start.toVector()).divide(new Vector(points - 1,
                points - 1, points - 1));
        for (int i = 0; i < points; i++)
            locations.add(start.clone().add(increment.clone().multiply(i)));
        return locations;
    }

    /**
     * Generates a horizontal circle of Locations with the given radius.
     * @param center The center of the circle.
     * @param radius The radius of the circle, in blocks/meters.
     * @param points The number of points in the circle.
     * @param type The {@link ShapeType} to use. Please note that {@link ShapeType#WIREFRAME},
     *             {@link ShapeType#HOLLOW}, and {@link ShapeType#CORNERS} will all return the
     *             same thing for a 2D circle.
     * @return A list of Locations.
     */
    public static List<Location> circle(Location center, double radius, int points, ShapeType type) {
        List<Location> locations = new ArrayList<>();
        double angle = MAX_ANGLE_DEGREES / points;
        double fillDist = radius / points;
        for (int i = 0; i < points; i++) {
            if (type == ShapeType.WIREFRAME || type == ShapeType.CORNERS) {
                for (double j = 0; j < radius; j += fillDist) {
                    // only add the very center of the circle once, the first run through.
                    // otherwise a clone of the center will be added multiple times and that's
                    // just not cool
                    if (i > 0 && j == 0)
                        continue;
                    locations.add(center.clone().add(j * Math.cos(i * angle),
                            0, j * Math.sin(i * angle)));
                }
            }
            locations.add(center.clone().add(radius * Math.cos(i * angle),
                    0, radius * Math.sin(i * angle)));
        }
        return locations;
    }

    /**
     * Generates a list of locations that form a horizontal quadrilateral.
     * @param center Center of the quadrilateral.
     * @param pointsWidth Number of points along the "width" side.
     * @param pointsLength Number of points along the "length" side.
     * @param type The {@link ShapeType} to use. "Hollow" produces a solid for 2D shapes.
     */
    public static List<Location> quad(Location center, double width, double length, int pointsWidth, int pointsLength,
                                      ShapeType type) {
        List<Location> locations = new ArrayList<>();
        Location offset = center.clone().subtract(width / 2, 0, length / 2);
        double widthIncrement = width / pointsWidth,
                lengthIncrement = length / pointsLength;
        for (int x = 0; x < pointsWidth; x++) {
            for (int z = 0; z < pointsLength; z++) {
                switch (type) {
                    case SOLID:
                    case HOLLOW:
                    default:
                        locations.add(offset.clone().add(x * widthIncrement, 0, z * lengthIncrement));
                        break;
                    case WIREFRAME:
                        if (x == 0 || x == pointsWidth - 1 || z == 0 || z == pointsLength - 1)
                            locations.add(offset.clone().add(x * widthIncrement, 0, z * lengthIncrement));
                        break;
                    case CORNERS:
                        if ((x == 0 || x == pointsWidth - 1) && (z == 0 || z == pointsLength - 1))
                            locations.add(offset.clone().add(x * widthIncrement, 0, z * lengthIncrement));
                        break;
                }
            }
        }
        return locations;
    }

    /**
     * Generates a cube of Locations. See {@link #quad(Location, double, double, int, int, ShapeType)} for arguments.
     */
    public static List<Location> cube(Location center, double width, double length, double height, int pointsWidth,
                                      int pointsLength, int pointsHeight, ShapeType type) {
        List<Location> locations = new ArrayList<>();
        double heightIncrement = height / pointsHeight;
        for (int y = 0; y < pointsHeight; y++) {
            locations.addAll(quad(center, width, length, pointsWidth, pointsLength, y == 0 || y == pointsHeight - 1 ?
                    type : ShapeType.CORNERS));
            center.add(0, heightIncrement, 0);
        }
        return locations;
    }

    /**
     * Generates a sphere around the provided location with the given radius.
     * @param points Refers to the number of points around the edges of the sphere both horizontally
     *               and vertically. The number of points in the sphere is thus the square of the given number.
     */
    public static List<Location> sphere(Location center, double radius, int points, ShapeType type) {
        List<Location> locations = new ArrayList<>();
        double increment = MAX_ANGLE_DEGREES / points;
        double verticalIncrement = increment / 2;
        for (int i = 0; i < points; i++) {
            for (int j = -points / 2; j < points / 2; j++) {
                if (type == ShapeType.SOLID) {
                    double radialIncrement = radius / points;
                    for (int k = 1; k < points; k++) {
                        Location l = center.clone().add(new Vector(k * radialIncrement, 0, 0));
                        l = rotateAroundDegrees(l, center, new Vector(0, i * increment, j * verticalIncrement));
                        locations.add(l);
                    }
                }
                Location loc = center.clone().add(new Vector(radius, 0, 0));
                loc = rotateAroundDegrees(loc, center, new Vector(0,  i * increment, j * verticalIncrement));
                locations.add(loc);
            }
        }
        return locations;
    }

    /**
     * Converts the given rotation to radians and calls {@link #rotateAround(Location, Location, Vector)}.
     */
    public static Location rotateAroundDegrees(Location point, Location origin, Vector rotation) {
        return rotateAround(point, origin, new Vector(Math.toRadians(rotation.getX()),
                Math.toRadians(rotation.getY()),
                Math.toRadians(rotation.getZ())));
    }

    /**
     * Rotates one point in space around another by the given rotation vector.
     * Rotations are executed counterclockwise around all axes.
     * @param point - The point to rotate from. Original is not affected.
     * @param origin - The point to rotate around.
     * @param rotation - Rotation vector, in radians. Use {@link #rotateAroundDegrees(Location, Location, Vector)}
     *                for convenience if using degrees.
     * @return A clone of point rotated around origin by rotation.
     */
    public static Location rotateAround(Location point, Location origin, Vector rotation) {
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

    public enum ShapeType {
        SOLID,
        HOLLOW,
        WIREFRAME,
        CORNERS
    }
}
