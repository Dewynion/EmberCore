package com.github.Dewynion.embercore.util;

import com.github.Dewynion.embercore.EmberCore;

public class MathUtil {
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
                    "attempted comparison of noncomparables.");
            return 0;
        }
    }
}
