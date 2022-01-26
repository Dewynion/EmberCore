package dev.blufantasyonline.embercore.util;

import dev.blufantasyonline.embercore.EmberCore;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashMap;
import java.util.Map;

public final class MathUtil {
    public static long MS_TO_TICKS_CONVERSION = 50L;

    public static int msToTicks(long ms) {
        return (int) (ms / MS_TO_TICKS_CONVERSION);
    }

    public static <T extends Number & Comparable> T clamp(T val, T min, T max) {
        try {
            return val.compareTo(min) > 0 ? (val.compareTo(max) < 0 ? val : max) : min;
        } catch (ClassCastException e) {
            EmberCore.getInstance().getLogger().warning("MathUtil::clamp(): attempted clamp " +
                    "involving a non-number.");
            return val;
        }
    }

    public static <T extends Comparable> int compareTo(T val1, T val2) {
        try {
            return val1.compareTo(val2);
        } catch (ClassCastException e) {
            EmberCore.getInstance().getLogger().warning("MathUtil::compareTo(): " +
                    "attempted comparison of non-comparable types.");
            return 0;
        }
    }

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
            return new ExpressionBuilder(expression)
                    .variables(variables.keySet())
                    .build()
                    .setVariables(variables)
                    .evaluate();
        } catch (IllegalArgumentException ex) {
            ErrorUtil.warn("Couldn't evaluate expression! Reason: %s", ex.getMessage());
            return defaultValue;
        }
    }
}
