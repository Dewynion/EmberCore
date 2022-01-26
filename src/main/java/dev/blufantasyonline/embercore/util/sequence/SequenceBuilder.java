package dev.blufantasyonline.embercore.util.sequence;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.ref.Reference;
import java.util.Stack;
import java.util.function.Supplier;

public final class SequenceBuilder {
    private JavaPlugin plugin;
    private Stack<Sequence> sequences = new Stack<>();
    private Sequence root;
    private Sequence current;

    public SequenceBuilder(JavaPlugin plugin, Runnable runnable) {
        this.plugin = plugin;
        root = new Sequence(plugin, runnable);
        current = root;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  On-condition sequencers.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder onCondition(Reference<Boolean> conditionReference, Runnable runnable) {
        return onCondition(conditionReference::get, runnable);
    }

    public SequenceBuilder onCondition(Supplier<Boolean> condition, Runnable runnable) {
        return onCondition(condition, new Sequence(plugin, runnable));
    }

    public SequenceBuilder onCondition(Supplier<Boolean> condition, Sequence sequence) {
        current.next.put(sequence, condition);
        return this;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  On-condition sequencers that proceed to the next sequence.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder onConditionThen(Reference<Boolean> conditionReference, Runnable runnable) {
        return onConditionThen(conditionReference::get, runnable);
    }

    public SequenceBuilder onConditionThen(Supplier<Boolean> condition, Runnable runnable) {
        return onConditionThen(condition, new Sequence(plugin, runnable));
    }

    public SequenceBuilder onConditionThen(Supplier<Boolean> condition, Sequence sequence) {
        current.next.put(sequence, condition);
        sequences.push(current);
        current = sequence;
        return this;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  "Next" sequencers, unconditional.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder then(Runnable runnable) {
        return _then(runnable, () -> current.endTaskCondition.get());
    }

    public SequenceBuilder thenImmediate(Runnable runnable) {
        return _then(runnable, () -> true);
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Conditional end sequencers for the current sequence.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder endIf(Reference<Boolean> endCondition) {
        return endIf(endCondition::get);
    }

    public SequenceBuilder endIf(Supplier<Boolean> endTaskCondition) {
        current.endTaskCondition = endTaskCondition;
        return this;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Conditional terminate sequencers for the current sequence.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder terminateIf(Reference<Boolean> terminateCondition) {
        return terminateIf(terminateCondition::get);
    }

    public SequenceBuilder terminateIf(Supplier<Boolean> terminateCondition) {
        current.terminateCondition = terminateCondition;
        return this;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Backtracking methods.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder back() {
        if (!sequences.isEmpty())
            current = sequences.pop();
        return this;
    }

    public SequenceBuilder root() {
        current = root;
        sequences.clear();
        return this;
    }

    // execute go brr

    public void execute() {
        root.execute();
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Adjustment of duration/period/iterations.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder duration(int durationTicks) {
        current.duration = durationTicks;
        return this;
    }

    public SequenceBuilder durationMs(long durationMs) {
        current.endTimeMs = System.currentTimeMillis() + durationMs;
        return this;
    }

    public SequenceBuilder period(int period) {
        current.period = period;
        return this;
    }

    public SequenceBuilder iterations(int iterations) {
        current.maxIterations = iterations;
        return this;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Private helper methods.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    private SequenceBuilder _then(Runnable runnable, Supplier<Boolean> condition) {
        Sequence next = new Sequence(plugin, runnable);
        current.next.put(next, condition);
        sequences.push(current);
        current = next;
        return this;
    }
}
