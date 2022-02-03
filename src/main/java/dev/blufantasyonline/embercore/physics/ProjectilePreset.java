package dev.blufantasyonline.embercore.physics;

import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class ProjectilePreset {
    public String name = "Projectile";
    public Vector size = new Vector(VectorProjectile.DEFAULT_SIZE, VectorProjectile.DEFAULT_SIZE, VectorProjectile.DEFAULT_SIZE);
    public Particle particle = Particle.CRIT;
    public long lifetime = 0;
    public double range = 0.0;
}
