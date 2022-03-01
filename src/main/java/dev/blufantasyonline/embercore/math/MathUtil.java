package dev.blufantasyonline.embercore.math;

import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.util.TimeUnits;
import dev.blufantasyonline.embercore.util.ErrorUtil;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.util.Vector;

import java.util.*;

public final class MathUtil {
    /**
     * An arbitrarily small number against which any double value subject to inaccuracy may be safely compared.
     * Use {@link #zero(double)} or {@link Math#abs(double)} <= {@link #ARBITRARILY_SMALL_NUMBER}.
      */
    public static final double ARBITRARILY_SMALL_NUMBER = 1e-6;

    private static final HashMap<String, Expression> expressionCache = new HashMap<>();

    /**
     * Compares the provided value with {@link #ARBITRARILY_SMALL_NUMBER} rather than 0.0 in order to
     * mitigate slight inaccuracy, such as with velocities.
     * @param value The value to compare.
     * @return Whether the provided value is effectively 0.0.
     */
    public static boolean zero(double value) {
        return Math.abs(value) <= ARBITRARILY_SMALL_NUMBER;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Time conversion methods, with shortcuts for common conversions.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    /**
     * Converts the provided amount of time into a different unit of time.
     * @param time The amount of time to convert.
     * @param from The {@link TimeUnits} that the provided time is in.
     * @param to The {@link TimeUnits} to which to convert the provided time.
     * @return An amount of time converted from one designated {@link TimeUnits} to the other.
     */
    public static double convertTime(double time, TimeUnits from, TimeUnits to) {
        return (time * from.seconds()) / to.seconds();
    }

    /**
     * Shorthand for {@link #convertTime(double, TimeUnits, TimeUnits)} from
     * {@link TimeUnits#TICKS} to {@link TimeUnits#MILLISECONDS}.
     */
    public static long ticksToMs(int ticks) {
        return Math.round(convertTime(ticks, TimeUnits.TICKS, TimeUnits.MILLISECONDS));
    }

    /**
     * Shorthand for {@link #convertTime(double, TimeUnits, TimeUnits)} from
     * {@link TimeUnits#MILLISECONDS} to {@link TimeUnits#TICKS}.
     */
    public static int msToTicks(long ms) {
        return (int) Math.round(convertTime(ms, TimeUnits.MILLISECONDS, TimeUnits.TICKS));
    }

    /**
     * Shorthand for {@link #convertTime(double, TimeUnits, TimeUnits)} to {@link TimeUnits#SECONDS}.
     */
    public static long toSeconds(double time, TimeUnits from) {
        return Math.round(convertTime(time, from, TimeUnits.SECONDS));
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Clamping methods.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public static <T extends Number & Comparable> T clamp(T val, T min, T max) {
        try {
            return val.compareTo(min) > 0 ? (val.compareTo(max) < 0 ? val : max) : min;
        } catch (ClassCastException e) {
            EmberCore.getInstance().getLogger().warning("MathUtil::clamp(): attempted clamp " +
                    "involving a non-number.");
            return val;
        }
    }

    /**
     * Clamps the provided value within [0.0, 1.0].
     * @param val The value to clamp.
     * @return A double in the range [0.0, 1.0] based off of the provided value.
     */
    public static double clampOne(double val) {
        return clamp(val, 0.0, 1.0);
    }

    // this sucks and could be much better
    public static Collection<Double> normalized(Collection<Double> numbers) {
        ArrayList<Double> normalizedNumbers = new ArrayList<>(numbers.size());
        double max = -Double.MAX_VALUE;
        for (double n : numbers)
            if (n > max)
                max = n;
        int index = 0;
        for (double n : numbers)
            normalizedNumbers.set(index++, n / max);
        return normalizedNumbers;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Comparison
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public static <T extends Comparable> int compareTo(T val1, T val2) {
        try {
            return val1.compareTo(val2);
        } catch (ClassCastException e) {
            EmberCore.getInstance().getLogger().warning("MathUtil::compareTo(): " +
                    "attempted comparison of non-comparable types.");
            return 0;
        }
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Linear interpolation methods.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public static double lerp(double from, double to, double ratio) {
        ratio = MathUtil.clampOne(ratio);
        return from + (to - from) * ratio;
    }

    public static double lerp(double from, double to, double delta, double totalTime) {
        return lerp(from, to, delta / totalTime);
    }

    public static Vector lerp(Vector from, Vector to, double ratio) {
        double x = lerp(from.getX(), to.getX(), ratio);
        double y = lerp(from.getY(), to.getY(), ratio);
        double z = lerp(from.getZ(), to.getZ(), ratio);
        return new Vector(x, y, z);
    }

    public static Vector lerp(Vector from, Vector to, double delta, double totalTime) {
        return lerp(from, to, delta / totalTime);
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Experimental smooth-damping methods.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public static DampingValues smoothDamp(DampingValues current, double target, double maxVelocity, double delta, double totalTime) {
        // this is literally just Unity's adaptation, which is in turn adapted from
        totalTime = MathUtil.clamp(Math.abs(totalTime), TimeUnits.MILLISECONDS.seconds(), Double.MAX_VALUE);
        delta = Math.abs(delta);
        double omega = 2.0 / totalTime;

        double x = omega * delta;
        double exp = 1.0 / (1.0 + x + 0.48 * Math.pow(x, 2) + 0.235 * Math.pow(x, 3));
        double change = current.position - target;
        double originalTarget = target;

        double maxChange = maxVelocity * totalTime;
        change = MathUtil.clamp(change, -maxChange, maxChange);
        target = current.position - change;

        double tmp = (current.velocity + omega * change) * delta;
        current.velocity = (current.velocity - omega * tmp) * exp;
        double out = target + (change + tmp) * exp;

        if (originalTarget - current.position > 0.0 == out > originalTarget) {
            out = originalTarget;
            current.velocity = (out - originalTarget) / delta;
        }

        current.position = out;
        return current;
    }

    public static DampingValues smoothDamp(double current, double target, double velocity, double maxVelocity, double delta, double totalTime) {
        return smoothDamp(new DampingValues(current, velocity), target, maxVelocity, delta, totalTime);
    }

    public static DampingValues smoothDamp(double current, double target, double velocity, double delta, double totalTime) {
        return smoothDamp(current, target, velocity, Double.MAX_VALUE, delta, totalTime);
    }

    public static DampingValues smoothDamp(DampingValues current, double target, double delta, double totalTime) {
        return smoothDamp(current, target, Double.MAX_VALUE, delta, totalTime);
    }

    public static Vector smoothDamp(Vector current, Vector target, Vector velocity, double delta, double totalTime) {
        DampingValues x = smoothDamp(current.getX(), target.getX(), velocity.getX(), delta, totalTime);
        DampingValues y = smoothDamp(current.getY(), target.getY(), velocity.getY(), delta, totalTime);
        DampingValues z = smoothDamp(current.getZ(), target.getZ(), velocity.getZ(), delta, totalTime);

        current.setX(x.position).setY(y.position).setZ(z.position);
        velocity.setX(x.velocity).setY(y.velocity).setZ(z.velocity);
        return current;
    }

    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //  Expression-related methods, using Exp4J parsing.
    //  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public static double evaluate(String expression, Object... variables) {
        return evaluate(expression, 0.0, variables);
    }

    public static double evaluate(String expression, Map<String, Double> variables) {
        return evaluate(expression, 0.0, variables);
    }

    public static double evaluate(String expression, double defaultValue, Object... variables) {
        if (variables.length % 2 != 0) {
            ErrorUtil.warn("Varargs object array must contain an even number of elements.");
            return defaultValue;
        }
        Map<String, Double> varMap = new HashMap<>();
        for (int i = 0; i < variables.length; i += 2) {
            try {
                String varName = variables[i].toString();
                double varValue = Double.parseDouble(variables[i + 1].toString());
                varMap.put(varName, varValue);
            } catch (NumberFormatException ex) {
                ErrorUtil.warn("Unable to parse double value for '%s'.", variables[i + 1]);
                return defaultValue;
            }
        }
        return evaluate(expression, defaultValue, varMap);
    }

    public static double evaluate(String expression, double defaultValue, Map<String, Double> variables) {
        try {
            Expression expr;
            if (expressionCache.containsKey(expression))
                expr = expressionCache.get(expression);
            else {
                expr = new ExpressionBuilder(expression)
                        .variables(variables.keySet())
                        .build();
                // TODO: ensure this actually works as intended.
                expressionCache.put(expression, expr);
            }
            return expr.setVariables(variables).evaluate();
        } catch (IllegalArgumentException ex) {
            ErrorUtil.warn("Couldn't evaluate expression! Reason: %s", ex.getMessage());
            return defaultValue;
        }
    }

    public static class DampingValues {
        public double position;
        public double velocity;

        public DampingValues(double position, double velocity) {
            this.position = position;
            this.velocity = velocity;
        }
    }
}
