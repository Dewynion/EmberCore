package dev.blufantasyonline.embercore.physics;

import dev.blufantasyonline.embercore.math.MathUtil;
import dev.blufantasyonline.embercore.math.geometry.Vectors;
import dev.blufantasyonline.embercore.physics.raycast.Intersection;
import dev.blufantasyonline.embercore.physics.raycast.Plane;
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

    public static Intersection<Ray, BoundingBox> intersection(Ray ray, BoundingBox box) {
        World world = ray.origin.getWorld();
        if (box.contains(ray.origin.toVector()) || box.contains(ray.end.toVector()))
            return new Intersection<>(ray, box, box.getCenter().toLocation(world));
        Vector tMin = new Vector();
        Vector tMax = new Vector();
        Vector s = ray.origin.toVector();
        Vector dir = new Vector(1, 1, 1).divide(ray.direction);
        int[] sign = new int[] { dir.getX() < 0 ? 1 : 0, dir.getY() < 0 ? 1 : 0, dir.getZ() < 0 ? 1 : 0};
        Vector[] bounds = { box.getMin().clone(), box.getMax().clone() };
        tMin.setX((bounds[sign[0]].getX() - s.getX()) * dir.getX());
        tMax.setX((bounds[1 - sign[0]].getX() - s.getX()) * dir.getX());
        tMin.setY((bounds[sign[1]].getY() - s.getY()) * dir.getY());
        tMax.setY((bounds[1 - sign[1]].getY() - s.getY()) * dir.getY());

        // ensure these contain the smallest and largest values in this interaction

        if (tMin.getX() > tMax.getY() || tMin.getY() > tMax.getX())
            return null;

        if (tMin.getY() > tMin.getX())
            tMin.setX(tMin.getY());

        if (tMax.getY() < tMax.getX())
            tMax.setX(tMax.getY());

        tMin.setZ((bounds[sign[2]].getZ() - s.getZ()) * dir.getZ());
        tMax.setZ((bounds[1 - sign[2]].getZ() - s.getZ()) * dir.getZ());

        if (tMin.getX() > tMax.getZ() || tMin.getZ() > tMax.getX())
            return null;

        if (tMin.getZ() > tMin.getX())
            tMin.setX(tMin.getZ());

        if (tMax.getZ() < tMax.getX())
            tMax.setX(tMax.getZ());

        return new Intersection<>(ray, box, box.getCenter().toLocation(world));
    }

    public static boolean intersectsPlane(Location pointOnPlane, Vector planeNormal, Ray ray) {
        return intersectsPlane(new Plane(pointOnPlane, planeNormal), ray);
    }

    public static boolean intersectsPlane(Ray plane, Ray ray) {
        return intersectsPlane(new Plane(plane.origin, plane.direction), ray);
    }

    public static boolean intersectsPlane(Plane plane, Ray ray) {
        // TODO finish this method
        return false;
    }

    public static Set<LivingEntity> raycastEntities(Ray ray) {
        Set<LivingEntity> potentialTargets = new HashSet<>();
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
        // TODO: actually, we don't, because if a ray passes the corner or edge of another chunk, it'll do this really
        //       funny thing where it won't bother checking the entities in that chunk. need to balance polling entities
        //       as well since an entity with a large hitbox standing on a chunk border won't be hit

        for (Chunk chunk : hitChunks) {
            for (Entity e : chunk.getEntities()) {
                if (!(e instanceof LivingEntity))
                    continue;
                LivingEntity entity = (LivingEntity) e;
                BoundingBox hitbox = entity.getBoundingBox();
                Intersection<Ray, BoundingBox> intersection = intersection(ray, hitbox);
                if (intersection != null)
                    potentialTargets.add(entity);
            }
        }

        return potentialTargets;
    }

    public static Set<LivingEntity> raycastEntities(Location origin, Vector direction, double distance) {
        return raycastEntities(new Ray(origin, direction, distance));
    }
}