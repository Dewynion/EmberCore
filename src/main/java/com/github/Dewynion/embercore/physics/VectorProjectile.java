package com.github.Dewynion.embercore.physics;

import com.github.Dewynion.embercore.EmberCore;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class VectorProjectile {
    public static double DEFAULT_SIZE = 0.5;
    private LivingEntity owner;
    private Location location;
    private BoundingBox hitbox;
    private Vector velocity;
    private Vector acceleration;
    /**
     * How many sections to divide each location update into.
     * If this is 5 and the projectile's velocity is 10 meters per tick
     * in the positive X direction, all on-tick functions will be called
     * every 10/5 = 2 meters.
     */
    private int interpolationScale = 1;

    public VectorProjectile(Location location) {
        this.location = location;
        hitbox = BoundingBox.of(location, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE);
    }

    public void setSize(double width, double height, double length) {
        double x =  location.getX(),
                y = location.getY(),
                z = location.getZ(),
                wR = width / 2,
                hR = height / 2,
                lR = length / 2;
        hitbox.resize(x - wR, y - hR, z - lR,
                x + wR, y + hR, z + lR);
    }

    public void onTick() {}

    public void onImpact() {}

    public void onHitEntity(LivingEntity entity) {}

    public boolean shouldHitEntity(LivingEntity entity) {
        return true;
    }

    public boolean shouldHitBlock(Block block) {
        return block.getType().isSolid();
    }

    private final void init() {
        new BukkitRunnable() {
            public void run() {
                Vector interpolatedVelocity = velocity.clone().divide(new Vector(interpolationScale,
                        interpolationScale, interpolationScale));
                for (int i = 0; i < interpolationScale; i++) {
                    location.add(interpolatedVelocity);
                    onTick();
                }
                velocity.add(acceleration);
            }
        }.runTaskTimer(EmberCore.getInstance(), 0, 1);
    }
}
