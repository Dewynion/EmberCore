package dev.blufantasyonline.embercore.util;

import dev.blufantasyonline.embercore.EmberCore;
import org.bukkit.plugin.java.JavaPlugin;

public final class ErrorUtil {
    public static StackTraceElement[] getStackTrace() {
        return Thread.currentThread().getStackTrace();
    }

    public static StackTraceElement getCurrentMethod() {
        return getCurrentMethod(1);
    }

    public static StackTraceElement getCurrentMethod(int depth) throws NullPointerException {
        try {
            return getStackTrace()[depth];
        } catch (ArrayIndexOutOfBoundsException ex) {
            EmberCore.warn("Unable to access stack frame at depth %s.", depth);
            return null;
        }
    }

    //===========================================================================================================
    // Error logging methods begin here.
    //===========================================================================================================

    public static void warn(String message, Object... format) {
        // This is identical to warn(JavaPlugin, String, Object[]) rather than
        // simply calling it due to how the stack works.
        StackTraceElement stackFrame = getCurrentMethodInternal();
        message = String.format(message, format);
        EmberCore.warn("%s::%s : %s", stackFrame.getClassName(), stackFrame.getMethodName(),
                message);
    }

    public static void warn(JavaPlugin plugin, String message, Object... format) {
        StackTraceElement stackFrame = getCurrentMethodInternal();
        message = String.format(message, format);
        EmberCore.warn(plugin, "%s::%s : %s", stackFrame.getClassName(), stackFrame.getMethodName(),
                message);
    }

    public static void severe(String message, Object... format) {
        StackTraceElement stackFrame = getCurrentMethodInternal();
        message = String.format(message, format);
        EmberCore.severe("%s::%s : %s", stackFrame.getClassName(), stackFrame.getMethodName(),
                message);
    }

    public static void severe(JavaPlugin plugin, String message, Object... format) {
        StackTraceElement stackFrame = getCurrentMethodInternal();
        message = String.format(message, format);
        EmberCore.severe(plugin, "%s::%s : %s", stackFrame.getClassName(), stackFrame.getMethodName(),
                message);
    }

    private static StackTraceElement getCurrentMethodInternal() {
        return getCurrentMethod(2);
    }
}
