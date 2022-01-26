package dev.blufantasyonline.embercore.util.sequence;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class Sequence {
    public LinkedHashMap<Sequence, Supplier<Boolean>> next = new LinkedHashMap<>();
    public Supplier<Boolean> endTaskCondition = () -> false;
    public Supplier<Boolean> terminateCondition = () -> false;
    public Runnable runnable;
    private JavaPlugin plugin;
    public int period;
    public int duration = -1;
    public int maxIterations = -1;
    public long endTimeMs;

    protected Sequence(JavaPlugin plugin) {
        this(plugin, () -> {
        });
    }

    protected Sequence(JavaPlugin plugin, Runnable runnable) {
        this(plugin, runnable, 1);
    }

    protected Sequence(JavaPlugin plugin, Runnable runnable, int period) {
        this.plugin = plugin;
        this.runnable = runnable;
        this.period = period;
    }

    void execute() {
        new BukkitRunnable() {
            int iterations = 0;
            int ticksElapsed = 0;
            int steps = period;
            int nextTickTime = 0;

            public void run() {
                if (shouldTerminate())
                    cancel();
                else {
                    if (!hasEnded() && shouldTick()) {
                        runnable.run();
                        steps = 0;
                        iterations++;
                    }
                    steps++;
                    ticksElapsed++;

                    next.entrySet().stream().filter(entry -> entry.getValue().get())
                            .findFirst().ifPresent(entry -> {
                        entry.getKey().execute();
                        cancel();
                    });
                }
            }

            private boolean shouldTick() {
                return steps == period;
            }

            private boolean hasEnded() {
                return endTaskCondition.get()
                        || (endTimeMs > 0 && System.currentTimeMillis() >= endTimeMs)
                        || (duration > 0 && ticksElapsed >= duration)
                        || (maxIterations > 0 && iterations >= maxIterations);
            }

            private boolean shouldTerminate() {
                return terminateCondition.get() || (hasEnded() && next.isEmpty());
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}
