package dev.blufantasyonline.embercore.util.sequence;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Sequence {
    public LinkedHashMap<Sequence, Predicate<Sequence>> next = new LinkedHashMap<>();
    public LinkedHashMap<Sequence, Predicate<Sequence>> concurrent = new LinkedHashMap<>();
    public Predicate<Sequence> endTask = (seq) -> false;
    public Predicate<Sequence> terminate = (seq) -> false;
    public Runnable runnable;
    private JavaPlugin plugin;
    public int period;
    public int duration = -1;
    public int maxIterations = -1;
    public long durationMs = -1;

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
            long nextTickTime = 0;
            long endTimeMs = System.currentTimeMillis() + durationMs;
            Set<Sequence> concurrentCache = new HashSet<>();

            public void run() {
                if (shouldTerminate())
                    cancel();
                else {
                    concurrent.entrySet().stream().filter(entry -> entry.getValue().test(Sequence.this))
                            .forEach(entry -> concurrentCache.add(entry.getKey()));
                    concurrentCache.forEach(seq -> {
                        concurrent.remove(seq);
                        seq.execute();
                    });
                    concurrentCache.clear();
                    if (!hasEnded() && shouldTick()) {
                        runnable.run();
                        steps = 0;
                        if (shouldIterate())
                            iterations++;
                    }
                    steps++;
                    ticksElapsed++;

                    next.entrySet().stream()
                            .filter(entry -> entry.getValue().test(Sequence.this))
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
                return endTask.test(Sequence.this)
                        || (durationMs > 0 && System.currentTimeMillis() >= endTimeMs)
                        || (duration > 0 && ticksElapsed >= duration)
                        || (maxIterations > 0 && iterations >= maxIterations);
            }

            private boolean shouldTerminate() {
                return terminate.test(Sequence.this) || (hasEnded() && next.isEmpty());
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Determines if the Sequence should increment its iteration counter when the runnable executes.
     * By default, this simply returns true, but it can be modified to allow for "iterations" to count as
     * repetitions of a series of actions that occur in sequence. For example, a Sequence could be used to display
     * a line of particles one at a time and run each tick - but the iteration criteria could consist of whether the
     * display has reached the end of the line rather than whether there's been a step in the runnable.
     **/
    protected boolean shouldIterate() {
        return true;
    }
}
