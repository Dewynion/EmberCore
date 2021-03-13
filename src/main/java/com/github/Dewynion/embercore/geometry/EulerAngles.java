package com.github.Dewynion.embercore.geometry;

import org.bukkit.util.Vector;

public class EulerAngles {
    public static final EulerAngles ZERO = new EulerAngles(0f, 0f, 0f);

    // Or: pitch, yaw, roll
    protected float x, y, z;

    /**
     * Stores a 3-angle representation of a rotation. Uses degrees.
     */
    public EulerAngles(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    /**
     * Pitch is another way of referring to rotation about the x axis, such as a plane's nose tilting up and down.
     * Returns the same thing as {@link #getX()}.
     */
    public float getPitch() {
        return x;
    }

    public float getY() {
        return y;
    }

    /**
     * Yaw is another way of referring to rotation about the y axis, such as the spin of a compass needle.
     * Returns the same thing as {@link #getY()}.
     */
    public float getYaw() {
        return y;
    }

    public float getZ() {
        return z;
    }

    /**
     * Roll is another way of referring to rotation about the z axis, such as a plane rolling side to side.
     * Returns the same thing as {@link #getZ()}.
     */
    public float getRoll() {
        return z;
    }

    public EulerAngles setAngle(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public EulerAngles add(EulerAngles angles) {
        return setAngle(this.x + angles.x, this.y + angles.y, this.z + angles.z);
    }

    public EulerAngles subtract(EulerAngles angles) {
        return setAngle(this.x - angles.x, this.y - angles.y, this.z - angles.z);
    }

    public EulerAngles multiply(Vector vec) {
        return setAngle(this.x * (float) vec.getX(), this.y * (float) vec.getY(), this.z * (float) vec.getZ());
    }

    /**
     * Constructs a three-dimensional rotation, in degrees of rotation about the X, Y and Z axes,
     * from a direction vector. Optional roll parameter for rotation about the Z axis.
     */
    public static EulerAngles fromDirectionVector(Vector direction, float roll) {
        // important
        direction = direction.normalize();
        // minecraft uses the Y axis in a Vector to determine vertical orientation
        float pitch = (float) Math.toDegrees(Math.asin(direction.getY()));
        // heading or yaw is calculated from the two horizontal components
        float yaw = (float) Math.toDegrees(Math.atan2(direction.getZ(), direction.getX()));
        return new EulerAngles(pitch, yaw, roll);
    }

    /**
     * Shortcut for {@link #fromDirectionVector(Vector, float)} where roll = 0f.
     */
    public static EulerAngles fromDirectionVector(Vector direction) {
        return fromDirectionVector(direction, 0f);
    }

    /**
     * Returns a (normalized) direction vector based on this rotation.
     */
    public Vector toDirectionVector() {
        // pitch = angle up and down
        double y = Math.sin(Math.toRadians(getPitch()));
        // yaw = angle on the horizontal plane
        // if z is the vertical axis on this plane and x is the horizontal,
        double x = Math.cos(Math.toRadians(getYaw()));
        double z = Math.sin(Math.toRadians(getYaw()));
        Vector vec = new Vector(x, y, z).normalize();
        return vec;
    }

    public EulerAngles toRadians() {
        return new EulerAngles((float) Math.toRadians(x), (float) Math.toRadians(y), (float) Math.toRadians(z));
    }

    public EulerAngles clone() {
        return new EulerAngles(x, y, z);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof EulerAngles))
            return false;
        EulerAngles o = (EulerAngles) other;
        return this.x == o.x && this.y == o.y && this.z == o.z;
    }
}
