package com.github.Dewynion.embercore.physics;

import com.github.Dewynion.embercore.util.StringUtil;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class ProjectilePreset {
    public String name = "Projectile";
    public Vector size = new Vector(VectorProjectile.DEFAULT_SIZE, VectorProjectile.DEFAULT_SIZE, VectorProjectile.DEFAULT_SIZE);
    public Particle particle = Particle.CRIT;
    public long lifetime = 0;
    public double range = 0.0;

    @Override
    public String toString() {
        return StringUtil.fString("Projectile Preset:\n" +
                        "  Name: {name}\n" +
                        "  Size: {size}\n" +
                        "  Particle: {particle}\n" +
                        "  Lifetime: {lifetime}\n" +
                        "  Range: {range}\n",
                this);
    }
}
