package dev.blufantasyonline.embercore.physics;

import dev.blufantasyonline.embercore.math.MathUtil;
import dev.blufantasyonline.embercore.math.geometry.Vectors;
import dev.blufantasyonline.embercore.physics.raycast.Intersection;
import dev.blufantasyonline.embercore.physics.raycast.Ray;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public final class Physics {
    private static final double CHUNK_SIZE = 16.0;

    public static boolean intersectsPlane(Location pointOnPlane, Vector planeNormal, Ray ray) {

    }

    public static boolean intersectsPlane(Ray plane, Ray ray) {

    }

    public static Set<Intersection<Ray, LivingEntity>> raycastEntities(Ray ray) {
        Set<Intersection<Ray, LivingEntity>> potentialTargets = new HashSet<>();
        final Location origin = ray.origin;
        final Vector direction = ray.direction;
        final double distance = ray.length;
        // Get this out of the way to save us pain and CPU
        if (direction.equals(Vectors.zero()) || MathUtil.zero(distance))
            return potentialTargets;

        World world = origin.getWorld();
        // break early if world doesn't exist
        if (world == null)
            return potentialTargets;

        // restrict our search to only chunks that will be hit by this raycast
        Set<Chunk> hitChunks = new HashSet<>();

        // check if the raycast will only ever hit this chunk.
        // this is the case if someone looks straight up or down.
        Location sameChunkCheck = origin.clone().add(direction.clone().multiply(distance));
        if (world.getChunkAt(origin).equals(world.getChunkAt(sameChunkCheck))) {
            hitChunks.add(world.getChunkAt(origin));
        } else {
            // set up a location to do this
            Location chunkCheck = origin.clone();
            // then precalculate this so we're not creating a new vector every iteration
            Vector chunkCastVector = direction.clone().multiply(CHUNK_SIZE);
            double distTraveled = 0.0;

            // We add the CHUNK_SIZE here because a raycast of length <= 16.0 would only check the origin
            // and the loop would break after one iteration. This means we cast for an extra chunk.
            while (distTraveled < distance + CHUNK_SIZE) {
                hitChunks.add(world.getChunkAt(chunkCheck));
                distTraveled += CHUNK_SIZE;
                chunkCheck.add(chunkCastVector);
            }
        }

        // we now have a set of all chunks the raycast could hit

        for (Chunk chunk : hitChunks) {
            for (Entity e : chunk.getEntities()) {
                if (!(e instanceof LivingEntity))
                    continue;
                LivingEntity entity = (LivingEntity) e;
                BoundingBox hitbox = entity.getBoundingBox();

            }
        }

        return potentialTargets;
    }

    public static Set<Intersection<Ray, LivingEntity>> raycastEntities(Location origin, Vector direction, double distance) {
        return raycastEntities(new Ray(origin, direction, distance));
    }
}