package com.github.Dewynion.embercore.geometry;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

@Deprecated
public class Transform {
    private RotationMatrix rotationMatrix;
    private EulerAngles localRotation;
    private Vector translation;

    private Location origin;
    private Location[] worldPoints;
    private Location[] localPoints;

    private int numPoints;

    public Transform(Location origin, EulerAngles startingRotation, List<Location> points) {
        numPoints = points.size();
        worldPoints = new Location[numPoints];
        localPoints = new Location[numPoints];

        this.origin = origin;
        translation = origin.toVector();

        localRotation = startingRotation;
        rotationMatrix = RotationMatrix.fromEulerAngles(startingRotation);
        RotationMatrix reverseRotation = rotationMatrix.clone().multiply(-1);
        points.toArray(worldPoints);
        for (int i = 0; i < numPoints; i++) {
            Location l = worldPoints[i].clone();
            l.subtract(translation);
            l = reverseRotation.applyTo(l.toVector()).toLocation(origin.getWorld());
            localPoints[i] = l;
        }
    }

    public void rotate(EulerAngles angles) {
        setRotation(localRotation.add(angles));
    }

    public void setRotation(EulerAngles angles) {
        localRotation = angles;
        rotationMatrix = RotationMatrix.fromEulerAngles(angles);

        Vector x = rotationMatrix.u();
        Vector y = rotationMatrix.v();
        Vector z = rotationMatrix.w();

        RotationMatrix rX = RotationMatrix.angleAxis(x, angles.x);
        RotationMatrix rY = RotationMatrix.angleAxis(y, angles.y);
        RotationMatrix rZ = RotationMatrix.angleAxis(z, angles.z);

        for (Location l : ShapeUtil.line(origin, origin.clone().add(x.multiply(5)), 10)) {
            l.getWorld().spawnParticle(Particle.CRIT, l, 1, 0, 0, 0, 0);
        }
        for (Location l : ShapeUtil.line(origin, origin.clone().add(y.multiply(5)), 10)) {
            l.getWorld().spawnParticle(Particle.CRIT, l, 1, 0, 0, 0, 0);
        }
        for (Location l : ShapeUtil.line(origin, origin.clone().add(z.multiply(5)), 10)) {
            l.getWorld().spawnParticle(Particle.CRIT, l, 1, 0, 0, 0, 0);
        }
        for (int i = 0; i < numPoints; i++) {
            Vector v = localPoints[i].toVector();
            v = rX.applyTo(rY.applyTo(rZ.applyTo(v)));
            v.add(translation);
            worldPoints[i] = v.toLocation(origin.getWorld());
        }
    }

    public Location[] points() {
        return Arrays.copyOf(worldPoints, numPoints);
    }

    public RotationMatrix getTransformationMatrix() {
        return rotationMatrix;
    }
}
