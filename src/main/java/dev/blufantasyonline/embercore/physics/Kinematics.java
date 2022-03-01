package dev.blufantasyonline.embercore.physics;

import dev.blufantasyonline.embercore.util.TimeUnits;

import static dev.blufantasyonline.embercore.math.MathUtil.toSeconds;

public final class Kinematics {
    public static double acceleration(double distance, double initialVelocity, long timeMs) {
        double time = toSeconds(timeMs, TimeUnits.MILLISECONDS);
        return 2.0 * ((distance / Math.pow(time, 2)) - (initialVelocity / time));
    }

    /**
     * Calculates the distance something has traveled over the given time in milliseconds, given a starting
     * and final velocity.
     */
    public static double distanceFromVelocities(double initialVelocity, double finalVelocity, long timeMs) {
        return ((initialVelocity + finalVelocity) / 2.0) * (toSeconds(timeMs, TimeUnits.MILLISECONDS));
    }

    /**
     * Calculates the distance something has traveled over the given time in milliseconds.
     */
    public static double distance(double initialVelocity, double acceleration, long timeMs) {
        return (initialVelocity * timeMs) + (0.5 * acceleration * Math.pow(toSeconds(timeMs, TimeUnits.MILLISECONDS), 2));
    }
}
