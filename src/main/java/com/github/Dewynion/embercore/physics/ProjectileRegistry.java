package com.github.Dewynion.embercore.physics;

import com.github.Dewynion.embercore.CoreLoadPriority;
import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.config.serialization.ExcludeFromSerialization;
import com.github.Dewynion.embercore.config.serialization.SerializationInfo;
import com.github.Dewynion.embercore.reflection.annotations.OnEnable;
import com.github.Dewynion.embercore.reflection.annotations.AfterEnable;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;


@OnEnable(priority = CoreLoadPriority.PROJECTILE_REGISTRY_PRIORITY)
public final class ProjectileRegistry {

    private static int maxProjectiles = 1000;

    @SerializationInfo(filename = "projectile-presets.yml")
    private static HashMap<String, ProjectilePreset> projectilePresets = new HashMap<>();

    @ExcludeFromSerialization
    private static LinkedHashSet<VectorProjectile> projectiles = new LinkedHashSet<>();

    public static VectorProjectile fromPreset(String presetName, Location origin) {
        ProjectilePreset preset = projectilePresets.get(presetName);
        if (preset != null)
            return new VectorProjectile(origin) {
                @Override
                protected void init() {
                    setLifetime(preset.lifetime);
                    setRange(preset.range);
                    super.init();
                }
            };
        return null;
    }

    public static ProjectilePreset fromPresetName(String presetName) {
        return projectilePresets.get(presetName);
    }

    public static void registerProjectile(VectorProjectile proj) {
        projectiles.add(proj);
        if (projectiles.size() >= maxProjectiles) {
            VectorProjectile oldest = projectiles.iterator().next();
            oldest.destroy();
        }
    }

    public static boolean removeProjectile(VectorProjectile proj) {
        return projectiles.remove(proj);
    }

    public static void destroyAll() {
        VectorProjectile[] arr = new VectorProjectile[projectiles.size()];
        projectiles.toArray(arr);
        for (VectorProjectile v : arr)
            v.destroy();
    }
}
