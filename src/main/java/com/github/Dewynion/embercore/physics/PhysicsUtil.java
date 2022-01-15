package com.github.Dewynion.embercore.physics;

public final class PhysicsUtil {
    private static long msToSeconds(long ms) {
        return ms / 1000L;
    }

    public static double acceleration(double distance, double initialVelocity, long timeMs) {
        double time = msToSeconds(timeMs);
        return 2.0 * ((distance / Math.pow(time, 2)) - (initialVelocity / time));
    }

    /**
     * Calculates the distance something has traveled over the given time in milliseconds, given a starting
     * and final velocity.
     */
    public static double distanceFromVelocities(double initialVelocity, double finalVelocity, long timeMs) {
        return ((initialVelocity + finalVelocity) / 2.0) * (msToSeconds(timeMs));
    }

    /**
     * Calculates the distance something has traveled over the given time in milliseconds.
     */
    public static double distance(double initialVelocity, double acceleration, long timeMs) {
        return (initialVelocity * timeMs) + (0.5 * acceleration * Math.pow(msToSeconds(timeMs), 2));
    }
}
