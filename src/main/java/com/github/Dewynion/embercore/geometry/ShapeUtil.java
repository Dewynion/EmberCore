package com.github.Dewynion.embercore.geometry;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public final class ShapeUtil {
    /**
     * Generates a list of Locations that form a line from start to end.
     *
     * @param start  Start point of the line.
     * @param end    Endpoint of the line.
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
     * Returns an arc facing east (positive x).
     */
    public static List<Location> arc(Location center, Location target, double angle, double radius, int points) {
        List<Location> locations = new ArrayList<>();
        double angleIncrement = angle / points;
        for (int i = -points / 2; i < points / 2; i++)
            locations.add(center.clone().add(radius * Math.cos(angleIncrement * i), 0f,
                    radius * Math.sin(angleIncrement * i)));
        return locations;
    }

    /**
     * Generates a horizontal circle of Locations with the given radius.
     *
     * @param center The center of the circle.
     * @param radius The radius of the circle, in blocks/meters.
     * @param points The number of points in the circle.
     * @return A list of Locations.
     */
    public static List<Location> circle(Location center, double radius, int points) {
        List<Location> locations = new ArrayList<>();
        double angle = 360.0 / points;
        for (int i = 0; i < points; i++) {
            locations.add(center.clone().add(radius * Math.cos(i * angle),
                    0, radius * Math.sin(i * angle)));
        }
        return locations;
    }

    /**
     * Generates a list of locations that form a horizontal quadrilateral.
     *
     * @param center       Center of the quadrilateral.
     * @param pointsWidth  Number of points along the "width" side.
     * @param pointsLength Number of points along the "length" side.
     */
    public static List<Location> quad(Location center, double width, double length,
                                      int pointsWidth, int pointsLength) {
        List<Location> locations = new ArrayList<>();
        Location offset = center.clone().subtract(width / 2, 0, length / 2);
        double widthIncrement = width / pointsWidth,
                lengthIncrement = length / pointsLength;
        for (int x = 0; x < pointsWidth; x++) {
            for (int z = 0; z < pointsLength; z++) {
                locations.add(offset.clone().add(x * widthIncrement, 0, z * lengthIncrement));
            }
        }
        return locations;
    }

    /**
     * Generates a cube of Locations. See {@link #quad(Location, double, double, int, int)} for arguments.
     */
    public static List<Location> cube(Location center, double width, double length, double height, int pointsWidth,
                                      int pointsLength, int pointsHeight) {
        List<Location> locations = new ArrayList<>();
        double heightIncrement = height / pointsHeight;
        for (int y = 0; y < pointsHeight; y++) {
            locations.addAll(quad(center, width, length, pointsWidth, pointsLength));
            center.add(0, heightIncrement, 0);
        }
        return locations;
    }

    /**
     * Generates a Fibonacci sphere around the provided location with the given radius.
     *
     * @param points Number of points in the sphere.
     */
    public static List<Location> sphere(Location center, double radius, int points) {
        List<Location> locations = new ArrayList<>();
        // phi = the golden angle, in radians
        // yes i had to look this up
        double phi = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int i = 0; i < points; i++) {
            // constrain y to [-1.0, 1.0]
            double y = 1.0 - ((double) i / ((double) points - 1.0)) * 2.0;
            // angle at i
            double theta = phi * i;
            double r = radius * Math.sqrt(1.0 - y * y);
            double x = r * Math.cos(theta);
            double z = r * Math.sin(theta);
            locations.add(center.clone().add(x, y, z));
        }
        return locations;
    }
}
