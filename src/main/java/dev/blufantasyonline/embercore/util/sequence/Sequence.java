package dev.blufantasyonline.embercore.util.sequence;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Predicate;

public class Sequence {
    protected LinkedHashMap<Sequence, Predicate<Sequence>> next = new LinkedHashMap<>();
    protected LinkedHashMap<Sequence, Predicate<Sequence>> concurrent = new LinkedHashMap<>();
    protected Predicate<Sequence> suspendCondition = (seq) -> false;
    protected Predicate<Sequence> terminateCondition = (seq) -> false;
    protected Runnable runnable;
    protected int period;
    protected int duration = -1;
    protected int maxIterations = -1;
    protected long durationMs = -1;
    protected SequenceState state = SequenceState.RUNNING;
    private JavaPlugin plugin;

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
                    if (!suspended() && shouldTick()) {
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

            private boolean suspended() {
                boolean suspended = suspendCondition.test(Sequence.this)
                        || (durationMs > 0 && System.currentTimeMillis() >= endTimeMs)
                        || (duration > 0 && ticksElapsed >= duration)
                        || (maxIterations > 0 && iterations >= maxIterations);
                // don't shift a terminated sequence back to suspension, in the event that it could happen
                if (state == SequenceState.RUNNING && suspended)
                    state = SequenceState.SUSPENDED;
                return suspended;
            }

            private boolean shouldTerminate() {
                boolean terminate = !plugin.isEnabled()
                        || terminateCondition.test(Sequence.this)
                        || (suspended() && next.isEmpty() && concurrent.isEmpty());
                if (terminate)
                    state = SequenceState.TERMINATED;
                return terminate;
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

    public boolean running() {
        return state == SequenceState.RUNNING;
    }

    public boolean suspended() {
        return state == SequenceState.SUSPENDED;
    }

    public boolean terminated() {
        return state == SequenceState.TERMINATED;
    }

    protected enum SequenceState {
        RUNNING,
        SUSPENDED,
        TERMINATED
    }
}
