package dev.blufantasyonline.embercore.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.Random;

public final class MovingParticle {
    private static final Random rand = new Random();

    public static void spawnMovingParticles(Location location, Particle particle, int count, double offsetX,
                                            double offsetY,
                                            double offsetZ,
                                            double minVelocityX,
                                            double minVelocityY,
                                            double minVelocityZ,
                                            double maxVelocityX, double maxVelocityY,
                                            double maxVelocityZ) {
        for (int i = 0; i < count; i++) {
            Location l = location.clone().add(randOffset(offsetX), randOffset(offsetY), randOffset(offsetZ));
            l.getWorld().spawnParticle(particle, l, 0, randVelocity(minVelocityX, maxVelocityX),
                    randVelocity(minVelocityY, maxVelocityY), randVelocity(minVelocityZ, maxVelocityZ));
        }
    }

    public static void spawnMovingParticles(Location location, Particle particle, int count, double offsetX,
                                            double offsetY, double offsetZ, Vector minVelocity, Vector maxVelocity) {
        spawnMovingParticles(location, particle, count, offsetX, offsetY, offsetZ, minVelocity.getX(),
                minVelocity.getY(), minVelocity.getZ(), maxVelocity.getX(), maxVelocity.getY(), maxVelocity.getZ());
    }

    public static void spawnMovingParticles(Location location, Particle particle, int count, double offsetX,
                                            double offsetY, double offsetZ, double velocityX, double velocityY,
                                            double velocityZ) {
        spawnMovingParticles(location, particle, count, offsetX, offsetY, offsetZ, velocityX, velocityY,
                velocityZ, velocityX, velocityY, velocityZ);
    }

    public static void spawnMovingParticles(Location location, Particle particle, int count, double offsetX,
                                            double offsetY, double offsetZ, Vector velocity) {
        spawnMovingParticles(location, particle, count, offsetX, offsetY, offsetZ, velocity.getX(),
                velocity.getY(), velocity.getZ());
    }

    private static double randOffset(double maxOffset) {
        return rand.nextDouble() * maxOffset * (rand.nextBoolean() ? 1 : -1);
    }

    private static double randVelocity(double minVelocity, double maxVelocity) {
        return (rand.nextDouble() * (maxVelocity - minVelocity)) + minVelocity;
    }
}
