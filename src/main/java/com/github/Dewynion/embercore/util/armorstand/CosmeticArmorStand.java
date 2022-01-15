package com.github.Dewynion.embercore.util.armorstand;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class CosmeticArmorStand {
    private JavaPlugin plugin;
    private ArmorStand armorStand;
    private boolean remove = false;

    CosmeticArmorStand(JavaPlugin plugin, Location location) {
        this.plugin = plugin;
        armorStand = location.getWorld().spawn(location, ArmorStand.class);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
    }

    public JavaPlugin getOwningPlugin() {
        return plugin;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public Location getLocation() {
        return armorStand.getLocation();
    }

    public CosmeticArmorStand setLocation(Location newLocation) {
        armorStand.teleport(newLocation);
        return this;
    }

    public ArmorStandAnimation animate(Consumer<CosmeticArmorStand> animation, Predicate<CosmeticArmorStand> exitCondition,
                                       int delayTicks, int periodTicks) {
        return new ArmorStandAnimation(this, plugin, delayTicks, periodTicks, animation, exitCondition);
    }

    public boolean scheduledRemove() {
        return remove;
    }

    public void remove() {
        armorStand.remove();
        remove = true;
        ArmorStandUtil.removeArmorStand(this);
    }
}
