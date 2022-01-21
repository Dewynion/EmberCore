package dev.blufantasyonline.embercore.util.armorstand;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ArmorStandAnimation {
    private CosmeticArmorStand armorStand;
    private Consumer<CosmeticArmorStand> exitConsumer;
    ArmorStandAnimation(CosmeticArmorStand armorStand, JavaPlugin plugin, int delayTicks, int periodTicks,
                               Consumer<CosmeticArmorStand> consumer, Predicate<CosmeticArmorStand> exitCondition) {
        this.armorStand = armorStand;
        new BukkitRunnable() {
            public void run() {
                if (exitCondition.test(armorStand) || armorStand.scheduledRemove())
                    cancel();
                else
                    consumer.accept(armorStand);
            }

            @Override
            public synchronized void cancel() throws IllegalStateException {
                super.cancel();
                if (exitConsumer != null)
                    exitConsumer.accept(armorStand);
                if (!armorStand.scheduledRemove())
                    armorStand.remove();
            }
        }.runTaskTimer(plugin, delayTicks, periodTicks);
    }

    public CosmeticArmorStand onExit(Consumer<CosmeticArmorStand> consumer) {
        this.exitConsumer = consumer;
        return armorStand;
    }
}
