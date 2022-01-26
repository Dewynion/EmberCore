package dev.blufantasyonline.embercore.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class ParticleUtil {
    private static final Random rand = new Random();
    private static final Set<Particle> colorable = new HashSet<>();
    private static final String NO_WORLD_MSG = "Can't spawn particles in a nonexistent world.";

    static {
        colorable.addAll(Arrays.asList(
                Particle.EXPLOSION_HUGE,
                Particle.EXPLOSION_LARGE,
                Particle.NOTE,
                Particle.REDSTONE,
                Particle.SPELL_MOB,
                Particle.SPELL_MOB_AMBIENT
        ));
    }

    public static boolean colorable(Particle particle) {
        return colorable.contains(particle);
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Spawn colored particles for a single player.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public static void spawnColoredParticles(Player player, Location location, Particle particle, int count,
                                             Vector offset, double r, double g, double b) {
        spawnColoredParticles(player, location, particle, count, offset.getX(), offset.getY(), offset.getZ(), r, g, b);
    }

    public static void spawnColoredParticles(Player player, Location location, Particle particle, int count,
                                             Vector offset, Color color) {
        spawnColoredParticles(player, location, particle, count, offset.getX(), offset.getY(), offset.getZ(),
                color.getRed(), color.getGreen(), color.getBlue());
    }

    public static void spawnColoredParticles(Player player, Location location, Particle particle, int count,
                                             double offsetX, double offsetY, double offsetZ,
                                             Color color) {
        spawnColoredParticles(player, location, particle, count, offsetX, offsetY, offsetZ,
                color.getRed(), color.getGreen(), color.getBlue());
    }

    public static void spawnColoredParticles(Player player, Location location, Particle particle, int count,
                                             double offsetX, double offsetY, double offsetZ,
                                             double r, double g, double b) {
        if (location.getWorld() == null)
            throw new NullPointerException("Can't spawn particles at a location without a world!");

        for (int i = 0; i < count; i++) {
            Location l = location.clone().add(randOffset(offsetX), randOffset(offsetY), randOffset(offsetZ));
            player.spawnParticle(particle, l, 0, r, g, b);
        }
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Spawn colored particles for all players within range.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public static void spawnColoredParticles(Location location, Particle particle, int count, double range,
                                             Vector offset, double r, double g, double b) {
        spawnColoredParticles(location, particle, count, range, offset.getX(), offset.getY(), offset.getZ(),
                r, g, b);
    }

    public static void spawnColoredParticles(Location location, Particle particle, int count, double range,
                                             Vector offset, Color color) {
        spawnColoredParticles(location, particle, count, range, offset.getX(), offset.getY(), offset.getZ(),
                color);
    }

    public static void spawnColoredParticles(Location location, Particle particle, int count, double range,
                                             double offsetX, double offsetY, double offsetZ,
                                             Color color) {
        spawnColoredParticles(location, particle, count, range, offsetX, offsetY, offsetZ,
                color.getRed(), color.getGreen(), color.getBlue());
    }

    public static void spawnColoredParticles(Location location, Particle particle, int count, double range,
                                             double offsetX, double offsetY, double offsetZ,
                                             double r, double g, double b) {
        if (location.getWorld() == null)
            throw new NullPointerException(NO_WORLD_MSG);

        location.getWorld().getPlayers().forEach(p -> spawnColoredParticles(p, location, particle, count,
                offsetX, offsetY, offsetZ, r, g, b));
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Spawn moving particles for a single player.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public static void spawnMovingParticles(Player player, Location location, Particle particle, int count,
                                            Vector offset, Vector velocity) {
        spawnMovingParticles(player, location, particle, count, offset, velocity, velocity);
    }

    public static void spawnMovingParticles(Player player, Location location, Particle particle, int count,
                                            Vector offset, Vector minVelocity, Vector maxVelocity) {
        spawnMovingParticles(player, location, particle, count,
                offset.getY(), offset.getZ(), offset.getX(),
                minVelocity, maxVelocity);
    }

    public static void spawnMovingParticles(Player player, Location location, Particle particle, int count, double offsetX,
                                            double offsetY, double offsetZ, Vector velocity) {
        spawnMovingParticles(player, location, particle, count,
                offsetX, offsetY, offsetZ,
                velocity, velocity);
    }

    public static void spawnMovingParticles(Player player, Location location, Particle particle, int count, double offsetX,
                                            double offsetY, double offsetZ, Vector minVelocity, Vector maxVelocity) {
        spawnMovingParticles(player, location, particle, count,
                offsetX, offsetY, offsetZ,
                minVelocity.getX(), minVelocity.getY(), minVelocity.getZ(),
                maxVelocity.getX(), maxVelocity.getY(), maxVelocity.getZ());
    }

    public static void spawnMovingParticles(Player player, Location location, Particle particle, int count, double offsetX,
                                            double offsetY, double offsetZ, double velocityX, double velocityY,
                                            double velocityZ) {
        spawnMovingParticles(player, location, particle, count,
                offsetX, offsetY, offsetZ,
                velocityX, velocityY, velocityZ,
                velocityX, velocityY, velocityZ);
    }

    public static void spawnMovingParticles(Player player, Location location, Particle particle, int count,
                                            double offsetX, double offsetY, double offsetZ,
                                            double minVelocityX, double minVelocityY, double minVelocityZ,
                                            double maxVelocityX, double maxVelocityY, double maxVelocityZ) {
        if (location.getWorld() == null)
            throw new NullPointerException(NO_WORLD_MSG);

        for (int i = 0; i < count; i++) {
            Location l = location.clone().add(randOffset(offsetX), randOffset(offsetY), randOffset(offsetZ));
            player.spawnParticle(particle, l, 0, randInRange(minVelocityX, maxVelocityX),
                    randInRange(minVelocityY, maxVelocityY), randInRange(minVelocityZ, maxVelocityZ));
        }
    }


    private static double randOffset(double maxOffset) {
        return rand.nextDouble() * maxOffset * (rand.nextBoolean() ? 1 : -1);
    }

    private static double randInRange(double min, double max) {
        return (rand.nextDouble() * (max - min)) + min;
    }
}
