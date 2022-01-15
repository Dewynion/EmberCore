package com.github.Dewynion.embercore.util.armorstand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class ArmorStandUtil {
    private static Map<String, Set<CosmeticArmorStand>> cosmeticStands = new HashMap<>();

    public static CosmeticArmorStand createArmorStand(JavaPlugin plugin, Location location) {
        String key = plugin.getName();
        if (!cosmeticStands.containsKey(key))
            cosmeticStands.put(key, new HashSet<>());
        Set<CosmeticArmorStand> pluginArmorStands = cosmeticStands.get(key);
        CosmeticArmorStand cosmeticArmorStand = new CosmeticArmorStand(plugin, location);
        pluginArmorStands.add(cosmeticArmorStand);
        return cosmeticArmorStand;
    }

    public static void removeArmorStand(CosmeticArmorStand cosmeticArmorStand) {
        String key = cosmeticArmorStand.getOwningPlugin().getName();
        if (cosmeticStands.containsKey(key)) {
            cosmeticStands.get(key).remove(cosmeticArmorStand);
        }
    }

    public static Set<CosmeticArmorStand> standsFor(JavaPlugin plugin) {
        return cosmeticStands.containsKey(plugin.getName()) ?
                Collections.unmodifiableSet(cosmeticStands.get(plugin.getName())) :
                Collections.emptySet();
    }

    public static void removeAll(JavaPlugin plugin) {
        String key = plugin.getName();
        if (cosmeticStands.containsKey(key)) {
            Set<CosmeticArmorStand> armorStands = cosmeticStands.get(key);
            if (armorStands != null)
                // schedule removal the next tick
                armorStands.forEach(c -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                           c.remove();
                        }
                    }.runTaskLater(plugin, 1);
                });
        }
    }

    public static boolean registered(JavaPlugin plugin) {
        return cosmeticStands.containsKey(plugin.getName());
    }
}
