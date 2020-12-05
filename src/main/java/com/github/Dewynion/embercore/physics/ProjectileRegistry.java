package com.github.Dewynion.embercore.physics;

import com.github.Dewynion.embercore.CoreLoadPriority;
import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.config.ConfigConstants;
import com.github.Dewynion.embercore.reflection.PostSetup;
import com.github.Dewynion.embercore.reflection.Singleton;

import java.util.*;

@Singleton(priority = CoreLoadPriority.PROJECTILE_REGISTRY_PRIORITY)
public class ProjectileRegistry {
    private static ProjectileRegistry instance;
    private int maxProjectiles;
    private LinkedHashSet<VectorProjectile> projectiles;

    public ProjectileRegistry() {
        instance = this;
        projectiles = new LinkedHashSet<>();
    }

    @PostSetup(priority = CoreLoadPriority.PROJECTILE_REGISTRY_PRIORITY)
    public void config() {
        maxProjectiles = EmberCore.getConfigReader().getInt(EmberCore.getInstance(),
                EmberCore.CONFIG_KEY,
                ConfigConstants.MAX_PROJECTILES.getPath(),
                (int) ConfigConstants.MAX_PROJECTILES.getDefaultValue());
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
