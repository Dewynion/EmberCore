package com.github.Dewynion.embercore.physics;

import com.github.Dewynion.embercore.CoreLoadPriority;
import com.github.Dewynion.embercore.config.YamlSerialized;
import com.github.Dewynion.embercore.reflection.Singleton;

import java.util.*;

@Singleton(priority = CoreLoadPriority.PROJECTILE_REGISTRY_PRIORITY)
public final class ProjectileRegistry {
    private static ProjectileRegistry instance;
    @YamlSerialized
    private int maxProjectiles = 1000;
    private LinkedHashSet<VectorProjectile> projectiles;

    public ProjectileRegistry() {
        instance = this;
        projectiles = new LinkedHashSet<>();
    }

    public static ProjectileRegistry getInstance() {
        return instance;
    }

    public void registerProjectile(VectorProjectile proj) {
        projectiles.add(proj);
        if (projectiles.size() >= maxProjectiles) {
            VectorProjectile oldest = projectiles.iterator().next();
            oldest.destroy();
        }
    }

    public boolean removeProjectile(VectorProjectile proj) {
        return projectiles.remove(proj);
    }

    public void destroyAll() {
        VectorProjectile[] arr = new VectorProjectile[projectiles.size()];
        projectiles.toArray(arr);
        for (VectorProjectile v : arr)
            v.destroy();
    }
}
