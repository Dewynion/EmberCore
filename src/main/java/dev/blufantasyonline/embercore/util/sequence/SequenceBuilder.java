package dev.blufantasyonline.embercore.util.sequence;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public final class SequenceBuilder {
    private JavaPlugin plugin;
    private Stack<Sequence> sequences = new Stack<>();
    private Sequence root;
    private Sequence marked;
    private Sequence current;

    public SequenceBuilder(JavaPlugin plugin, Runnable runnable) {
        this(plugin, new Sequence(plugin, runnable));
    }

    public SequenceBuilder(JavaPlugin plugin, Sequence sequence) {
        this.plugin = plugin;
        root = sequence;
        current = root;
        marked = current;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  On-condition sequencers.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder onCondition(AtomicBoolean conditionReference, Runnable runnable) {
        return onCondition((seq) -> conditionReference.get(), runnable);
    }

    public SequenceBuilder onCondition(AtomicBoolean conditionReference, Sequence sequence) {
        return onCondition((seq) -> conditionReference.get(), sequence);
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

    public SequenceBuilder onConditionThen(AtomicBoolean conditionReference, Runnable runnable) {
        return onConditionThen((seq) -> conditionReference.get(), runnable);
    }

    public SequenceBuilder onConditionThen(AtomicBoolean conditionReference, Sequence sequence) {
        return onConditionThen((seq) -> conditionReference.get(), sequence);
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
        return _then(runnable, Sequence::suspended);
    }

    public SequenceBuilder thenImmediate(Runnable runnable) {
        return _then(runnable, (seq) -> true);
    }

    public SequenceBuilder then(Sequence sequence) {
        return _then(sequence, Sequence::suspended);
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

    public SequenceBuilder withConditional(Sequence sequence, AtomicBoolean condition) {
        return withConditional(sequence, (seq) -> condition.get());
    }

    public SequenceBuilder withConditional(Runnable runnable, AtomicBoolean condition) {
        return withConditional(runnable, (seq) -> condition.get());
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
    //  Conditional suspend sequencers for the current sequence.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    @Deprecated(forRemoval = true)
    public SequenceBuilder endIf(AtomicBoolean endCondition) {
        return suspendIf((seq) -> endCondition.get());
    }

    public SequenceBuilder suspendIf(AtomicBoolean endCondition) {
        return suspendIf((seq) -> endCondition.get());
    }

    @Deprecated(forRemoval = true)
    public SequenceBuilder endIf(Predicate<Sequence> endTaskCondition) {
        return suspendIf(endTaskCondition);
    }

    public SequenceBuilder suspendIf(Predicate<Sequence> endTaskCondition) {
        current.suspendCondition = endTaskCondition;
        return this;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Conditional terminate sequencers for the current sequence.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public SequenceBuilder terminateIf(AtomicBoolean terminateCondition) {
        return terminateIf((seq) -> terminateCondition.get());
    }

    public SequenceBuilder terminateIf(Predicate<Sequence> terminateCondition) {
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

    public SequenceBuilder mark() {
        marked = current;
        return this;
    }

    public SequenceBuilder recall() {
        // Also used as a temp variable in a moment.
        Sequence seq = current;
        // Drag the current sequence back to the mark.
        current = marked;
        // Backtrack through the stack as long as it's got something in it and the currently-processing
        // sequence doesn't reference the marked sequence.
        // Since the current sequence shouldn't be on the stack, this works just fine, even if one uses
        // mark() and then recall() (since seq == current == marked in that case).
        while (!sequences.isEmpty() && seq != marked)
            seq = sequences.pop();
        return this;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Literally just the execute method, here by its lonesome.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

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
        return iterations(1);
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
