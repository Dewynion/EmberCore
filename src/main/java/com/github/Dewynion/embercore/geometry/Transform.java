package com.github.Dewynion.embercore.util.geometry;

import com.github.Dewynion.embercore.EmberCore;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.logging.Level;

public class Transform {
    // Center or pivot of the Transform.
    private Location center;
    // Non-rotated positions relative to the center.
    private Location[] localPositions;
    // Rotated positions in world space.
    private Location[] worldPositions;
    // Three-dimensional rotation of the Transform.
    private EulerAngles localRotation = EulerAngles.ZERO;
    // currently does nothing.
    private float scale = 1.0f;
    private int numPoints;

    // the x axis
    private Vector forward;
    // the y axis
    private Vector up;
    // the z axis
    private Vector right;

    public Transform(Location center, List<Location> points) {
        this(center, EulerAngles.ZERO, points);
    }

    public Transform(Location center, EulerAngles rotation, List<Location> points) {
        this(center, rotation, 1.0f, points);
    }

    public Transform(Location center, Vector forward, List<Location> points) {
        this(center, forward, 1.0f, points);
    }

    public Transform(Location center, EulerAngles rotation, float scale, List<Location> points) {
        this(center, rotation.toDirectionVector(), scale, points);
    }

    public Transform(Location center, Vector forward, float scale, List<Location> points) {
        this.center = center;
        this.scale = scale;
        this.numPoints = points.size();
        this.localPositions = new Location[points.size()];
        this.worldPositions = new Location[points.size()];
        points.toArray(worldPositions);

        this.localRotation = EulerAngles.fromDirectionVector(forward);
        updateAxes();

        EmberCore.log(Level.INFO, String.format("Forward: %s %s %s", forward.getX(), forward.getY(), forward.getZ()));
        EmberCore.log(Level.INFO, String.format("Right: %s %s %s", right.getX(), right.getY(), right.getZ()));
        EmberCore.log(Level.INFO, String.format("Up: %s %s %s", up.getX(), up.getY(), up.getZ()));

        // reverse rotation axes
        Vector backward = this.forward.clone().multiply(-1);
        Vector down = this.up.clone().multiply(-1);
        Vector left = this.right.clone().multiply(-1);

        // apply those to localpoints
        for (int i = 0; i < numPoints; i++) {
            Location l = worldPositions[i].clone();
            l.subtract(center);
            l = GeometryUtil.axisRotation(l.toVector(), backward, down, left)
                    .toLocation(l.getWorld());
            localPositions[i] = l;
            EmberCore.log(Level.INFO, String.format("%s %s %s", l.getX(), l.getY(), l.getZ()));
        }
    }

    public void translate(Vector vec) {
        setPosition(center.add(vec));
    }

    public void setPosition(Location location) {
        center = location;
        for (int i = 0; i < numPoints; i++) {
            worldPositions[i].add(center);
        }
    }

    public void rotate(EulerAngles angles) {
        setRotation(localRotation.add(angles));
    }

    public void setRotation(EulerAngles angles) {
        localRotation = angles;
        updateAxes();
        for (int i = 0; i < numPoints; i++) {
            Location l = localPositions[i].clone();
            worldPositions[i] = GeometryUtil.axisRotation(l.toVector(), forward, up, right)
                    .toLocation(l.getWorld())
                    .add(center);
        }
    }

    private void updateAxes() {
        Vector forward = localRotation.toDirectionVector();
        this.forward = forward.normalize();
        this.right = Vectors.right;
        // for some reason this keeps projecting the up vector in the negative y direction
        this.up = forward.clone().crossProduct(right).normalize();
        this.right = up.clone().crossProduct(forward).normalize();
    }

    public Vector xAxis() {
        return forward;
    }

    public Vector yAxis() {
        return up;
    }

    public Vector zAxis() {
        return right;
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
