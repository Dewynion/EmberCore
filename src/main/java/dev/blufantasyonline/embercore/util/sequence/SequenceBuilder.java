package dev.blufantasyonline.embercore.util.sequence;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.ref.Reference;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class SequenceBuilder {
    private JavaPlugin plugin;
    private Stack<Sequence> sequences = new Stack<>();
    private Sequence root;
    private Sequence current;

    public SequenceBuilder(JavaPlugin plugin, Runnable runnable) {
        this(plugin, new Sequence(plugin, runnable));
    }

    public SequenceBuilder(JavaPlugin plugin, Sequence sequence) {
        this.plugin = plugin;
        root = sequence;
        current = root;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  On-condition sequencers.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder onCondition(AtomicReference<Boolean> conditionReference, Runnable runnable) {
        return onCondition((seq) -> conditionReference.get(), runnable);
    }

    public SequenceBuilder onCondition(Predicate<Sequence> condition, Runnable runnable) {
        return onCondition(condition, new Sequence(plugin, runnable));
    }

    public SequenceBuilder onCondition(Predicate<Sequence> condition, Sequence sequence) {
        current.next.put(sequence, condition);
        return this;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  On-condition sequencers that proceed to the next sequence.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder onConditionThen(AtomicReference<Boolean> conditionReference, Runnable runnable) {
        return onConditionThen((seq) -> conditionReference.get(), runnable);
    }

    public SequenceBuilder onConditionThen(Predicate<Sequence> condition, Runnable runnable) {
        return onConditionThen(condition, new Sequence(plugin, runnable));
    }

    public SequenceBuilder onConditionThen(Predicate<Sequence> condition, Sequence sequence) {
        return _then(sequence, condition);
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  "Next" sequencers, unconditional.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder then(Runnable runnable) {
        return _then(runnable, (seq) -> seq.endTask.test(seq));
    }

    public SequenceBuilder thenImmediate(Runnable runnable) {
        return _then(runnable, (seq) -> true);
    }

    public SequenceBuilder then(Sequence sequence) {
        return _then(sequence, (seq) -> seq.endTask.test(seq));
    }

    public SequenceBuilder thenImmediate(Sequence sequence) {
        return _then(sequence, (seq) -> true);
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Methods to add additional sequences that run in tandem with the current sequence when conditions are met.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder with(Sequence sequence) {
        return withConditional(sequence, (seq) -> true);
    }

    public SequenceBuilder with(Runnable runnable) {
        return with(new Sequence(plugin, runnable));
    }

    public SequenceBuilder withConditional(Sequence sequence, Predicate<Sequence> condition) {
        current.concurrent.put(sequence, condition);
        sequences.push(current);
        current = sequence;
        return this;
    }

    public SequenceBuilder withConditional(Runnable runnable, Predicate<Sequence> condition) {
        return withConditional(new Sequence(plugin, runnable), condition);
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Conditional end sequencers for the current sequence.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder endIf(AtomicReference<Boolean> endCondition) {
        return endIf((seq) -> endCondition.get());
    }

    public SequenceBuilder endIf(Predicate<Sequence> endTaskCondition) {
        current.endTask = endTaskCondition;
        return this;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Conditional terminate sequencers for the current sequence.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder terminateIf(AtomicReference<Boolean> terminateCondition) {
        return terminateIf((seq) -> terminateCondition.get());
    }

    public SequenceBuilder terminateIf(Predicate<Sequence> terminateCondition) {
        current.terminate = terminateCondition;
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
        current.durationMs = System.currentTimeMillis() + durationMs;
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

    public SequenceBuilder once() {
        current.maxIterations = 1;
        return this;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Private helper methods.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    private SequenceBuilder _then(Sequence sequence, Predicate<Sequence> condition) {
        current.next.put(sequence, condition);
        sequences.push(current);
        current = sequence;
        return this;
    }

    private SequenceBuilder _then(Runnable runnable, Predicate<Sequence> condition) {
        return _then(new Sequence(plugin, runnable), condition);
    }
}
