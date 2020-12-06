package com.github.Dewynion.embercore.physics;

import com.github.Dewynion.embercore.EmberCore;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VectorProjectile {
    public static double DEFAULT_SIZE = 0.5;
    private boolean active;
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
    private long expireTime = 0;
    private double range = 0.0;

    public VectorProjectile(Location location) {
        this.location = location;
        active = true;
        hitbox = BoundingBox.of(location, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE);
        ProjectileRegistry.getInstance().registerProjectile(this);
    }

    public void destroy() {
        active = false;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    public boolean hasOwner() {
        return owner != null;
    }

    public void setOwner(@Nullable LivingEntity newOwner) {
        owner = newOwner;
    }

    /**
     * Sets the current lifetime of the projectile in milliseconds.
     * Use a value of 0 to set an infinite lifetime. This is not recommended.
     */
    public void setLifetime(long lifetime) {
        if (lifetime == 0)
            expireTime = 0;
        else
            expireTime = System.currentTimeMillis() + lifetime;
    }

    /**
     * Sets the maximum range of the projectile before it expires.
     * Set to 0 for infinite range. This is not recommended.
     */
    public void setRange(double newRange) {
        range = Math.abs(newRange);
    }

    /**
     * Sets the size of the projectile's hitbox.
     */
    public void setSize(double width, double height, double length) {
        double x = location.getX(),
                y = location.getY(),
                z = location.getZ(),
                wR = width / 2,
                hR = height / 2,
                lR = length / 2;
        hitbox.resize(x - wR, y - hR, z - lR,
                x + wR, y + hR, z + lR);
    }

    public int getInterpolationScale() {
        return interpolationScale;
    }

    /**
     * Sets how many pieces location updates are divided into. Useful for
     * improving precision collision detection.
     *
     * @param newScale
     */
    public void setInterpolationScale(int newScale) {
        if (newScale < 1)
            newScale = 1;
        interpolationScale = newScale;
    }

    /**
     * Runs each update tick, independent of interpolation.
     */
    public void onTick() {
    }

    /**
     * Runs each update and each interpolated update (i.e. the "extra ticks"
     * that occur when {@link #interpolationScale} > 1).
     */
    public void interpolatedTick() {
    }

    /**
     * Called when the projectile is destroyed, regardless of means.
     */
    public void onDeath() {
    }

    /**
     * The code to run if {@link #shouldHitEntity(LivingEntity)} returns true.
     * By default, does nothing.
     *
     * @return True if the projectile should be destroyed after this code is run,
     * false otherwise.
     */
    public boolean onHitEntity(@Nonnull LivingEntity entity) {
        return true;
    }

    /**
     * Determines whether or not to hit
     */
    public boolean shouldHitEntity(@Nonnull LivingEntity entity) {
        return !entity.equals(owner);
    }

    public boolean shouldHitBlock(@Nonnull Block block) {
        return block.getType().isSolid();
    }

    private final void init() {
        new BukkitRunnable() {
            private double distSquared;

            public void run() {
                if (!active || System.currentTimeMillis() >= expireTime)
                    cancel();
                Vector interpolatedVelocity = velocity.clone().divide(new Vector(interpolationScale,
                        interpolationScale, interpolationScale));
                onTick();
                for (int i = 0; i < interpolationScale; i++) {
                    // if it connects with a block it perceives as solid,
                    // reaches its maximum lifetime, travels its maximum range,
                    // or has been destroyed externally, cancel the thread
                    if (shouldHitBlock(location.getBlock()) ||
                            distSquared >= Math.pow(range, 2))
                        cancel();

                    Location prev = location.clone();
                    location.add(interpolatedVelocity);
                    distSquared += prev.distanceSquared(location);
                    hitbox.shift(location.clone().subtract(prev));

                    for (LivingEntity entity : location.getWorld().getLivingEntities()) {
                        if (hitbox.contains(entity.getBoundingBox()) &&
                                shouldHitEntity(entity) &&
                                onHitEntity(entity))
                            cancel();
                    }
                }
                velocity.add(acceleration);
            }

            public void cancel() {
                active = false;
                onDeath();
                ProjectileRegistry.getInstance().removeProjectile(VectorProjectile.this);
                super.cancel();
            }
        }.runTaskTimer(EmberCore.getInstance(), 0, 1);
    }
}
