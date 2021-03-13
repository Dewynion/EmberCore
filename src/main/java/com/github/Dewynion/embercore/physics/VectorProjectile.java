package com.github.Dewynion.embercore.physics;

import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.geometry.Vectors;
import org.bukkit.Bukkit;
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
    protected LivingEntity owner;
    protected Location location;
    protected BoundingBox hitbox;
    protected Vector velocity = Vectors.ZERO;
    protected Vector acceleration = Vectors.ZERO;
    /**
     * How many sections to divide each location update into.
     * If this is 5 and the projectile's velocity is 10 meters per tick
     * in the positive X direction, all on-tick functions will be called
     * every 10/5 = 2 meters.
     */
    protected int interpolationScale = 1;
    protected long expireTime = 0;
    protected double range = 0.0;
    private boolean recalculateInterpolationScale = false;

    public VectorProjectile(Location location) {
        this.location = location;
        active = true;
        hitbox = BoundingBox.of(location, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE);
        ProjectileRegistry.getInstance().registerProjectile(this);
        init();
    }

    public final void destroy() {
        active = false;
    }

    public final LivingEntity getOwner() {
        return owner;
    }

    public final boolean hasOwner() {
        return owner != null;
    }

    public final void setOwner(@Nullable LivingEntity newOwner) {
        owner = newOwner;
    }

    /**
     * Sets the current lifetime of the projectile in milliseconds.
     * Use a value of 0 to set an infinite lifetime. This is not recommended.
     */
    public final void setLifetime(long lifetime) {
        if (lifetime == 0)
            expireTime = 0;
        else
            expireTime = System.currentTimeMillis() + lifetime;
    }

    /**
     * Sets the maximum range of the projectile before it expires.
     * Set to 0 for infinite range. This is not recommended.
     */
    public final void setRange(double newRange) {
        range = Math.abs(newRange);
    }

    /**
     * Sets the size of the projectile's hitbox.
     */
    public final void setSize(double width, double height, double length) {
        double x = location.getX(),
                y = location.getY(),
                z = location.getZ(),
                wR = width / 2,
                hR = height / 2,
                lR = length / 2;
        hitbox.resize(x - wR, y - hR, z - lR,
                x + wR, y + hR, z + lR);
        recalculateInterpolationScale = true;
    }

    public final int getInterpolationScale() {
        return interpolationScale;
    }

    /**
     * Sets how many pieces location updates are divided into. Useful for
     * improving precision collision detection.
     *
     * @param newScale
     */
    public final void setInterpolationScale(int newScale) {
        interpolationScale = Math.max(1, newScale);
        recalculateInterpolationScale = false;
    }

    /**
     * Lazy implementation that tries to ensure proper collision for projectiles that move more than
     * their width in a single tick.
     */
    public final int autoInterpolationScale() {
        double length = velocity.length();
        double smallest = Math.min(hitbox.getWidthX(), Math.min(hitbox.getWidthZ(), hitbox.getHeight()));
        return (int) Math.floor(length / smallest);
    }

    public final void setVelocity(Vector newVelocity) {
        velocity = newVelocity;
        recalculateInterpolationScale = true;
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

    private void init() {
        new BukkitRunnable() {
            private double distSquared;
            private Vector interpolatedVelocity;

            public void run() {
                if (!active || (expireTime != 0 && System.currentTimeMillis() >= expireTime))
                    cancel();
                // This will be flagged if the hitbox size or velocity change.
                if (recalculateInterpolationScale || interpolatedVelocity == null) {
                    setInterpolationScale(autoInterpolationScale());
                    interpolatedVelocity = velocity.clone().divide(new Vector(interpolationScale,
                            interpolationScale, interpolationScale));
                }

                // standard tick
                onTick();
                for (int i = 0; i < interpolationScale; i++) {
                    // if it connects with a block it perceives as solid,
                    // reaches its maximum lifetime, travels its maximum range,
                    // or has been destroyed externally, cancel the thread
                    if (shouldHitBlock(location.getBlock()) ||
                            (range != 0 && distSquared >= Math.pow(range, 2)))
                        cancel();

                    // interpolated tick
                    interpolatedTick();
                    for (LivingEntity entity : location.getWorld().getLivingEntities()) {
                        if (!hitbox.overlaps(entity.getBoundingBox()))
                            continue;
                        if (shouldHitEntity(entity))
                            if (onHitEntity(entity))
                                cancel();
                    }
                    Location prev = location.clone();
                    location.add(interpolatedVelocity);
                    distSquared += prev.distanceSquared(location);
                    hitbox.shift(interpolatedVelocity);
                }
                if (!acceleration.equals(Vectors.ZERO))
                    setVelocity(velocity.add(acceleration));
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
