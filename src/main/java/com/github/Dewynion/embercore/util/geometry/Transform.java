package com.github.Dewynion.embercore.util.geometry;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

public class Transform {
    // Center or pivot of the Transform.
    private Location center;
    // Non-rotated positions relative to the center.
    private Location[] localPositions;
    // Rotated positions in world space.
    private Location[] worldPositions;
    // Three-dimensional rotation of the Transform.
    private EulerAngles localRotation = EulerAngles.zero;
    // currently does nothing.
    private float scale = 1.0f;
    private int numPoints;

    public Transform(Location center, List<Location> points) {
        this(center, EulerAngles.zero, points);
    }

    public Transform(Location center, EulerAngles rotation, List<Location> points) {
        this(center, rotation, 1.0f, points);
    }

    public Transform(Location center, EulerAngles rotation, float scale, List<Location> points) {
        this.center = center;
        this.scale = scale;
        this.numPoints = points.size();
        this.localPositions = new Location[points.size()];
        this.worldPositions = new Location[points.size()];
        points.toArray(worldPositions);
        for (int i = 0; i < points.size(); i++)
            localPositions[i] = worldPositions[i].clone().subtract(center);
        setRotation(rotation);
    }

    public void translate(Vector vec) {
        setPosition(center.add(vec));
    }

    public void setPosition(Location location) {
        // don't do anything if it's moved to the same location
        if (location.equals(center))
            return;
        center = location;
        for (int i = 0; i < numPoints; i++) {
            worldPositions[i] = localPositions[i].clone().add(center);
        }
    }

    public void rotate(EulerAngles angles) {
        setRotation(localRotation.add(angles));
    }

    public void setRotation(EulerAngles angles) {
        // don't do anything if the rotation is the same as it currently is
        if (angles.equals(localRotation))
            return;
        for (int i = 0; i < numPoints; i++)
            worldPositions[i] = GeometryUtil.rotateAroundDegrees(worldPositions[i], center, localRotation);
    }

    /**
     * Returns a <i><b>copy</b></i> of this Transform's world-position points.
     * No changes made to either this array or its components will affect the source Transform.
     */
    public Location[] points() {
        Location[] points = new Location[numPoints];
        for (int i = 0; i < numPoints; i++)
            points[i] = worldPositions[i].clone();
        return points;
    }
}
