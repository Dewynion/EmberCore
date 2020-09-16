package com.github.Dewynion.embercore.reflection;

import com.github.Dewynion.embercore.EmberCore;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.logging.Level;

public class Versioning {
    public static final String NMS_DOMAIN = "net.minecraft.server";
    public static final String CRAFTBUKKIT_PACKAGE = "craftbukkit";

    private static HashMap<String, Class> classCache = new HashMap<>();
    private static String nmsVersion;
    private static String nmsPackage;

    /*
     * https://gist.github.com/ReflxctionDev/84a5753a69f426f48ffe75f7ab3adcbb
     * pointed me in the right direction when it came to actually getting the version string.
     */
    static {
        nmsVersion = Bukkit.getServer().getClass().getName();
        nmsVersion = nmsVersion.substring(nmsVersion.indexOf(CRAFTBUKKIT_PACKAGE + ".")
                        + (CRAFTBUKKIT_PACKAGE + ".").length());
        nmsVersion = nmsVersion.substring(0, nmsVersion.indexOf("."));
        nmsPackage = NMS_DOMAIN + "." + nmsVersion;
        EmberCore.log(Level.INFO, "You're running NMS version " + nmsVersion + ".");
    }

    /** Attempts to retrieve an NMS class by name. */
    public static Class<?> getNMSClass(String className) throws NullPointerException {
        try {
            if (!classCache.containsKey(className)) {
                Class<?> clz = Class.forName(nmsPackage + "." + className);
                classCache.put(className, clz);
                return clz;
            } else
                return classCache.get(className);
        } catch (Exception e) {
            EmberCore.log(Level.SEVERE, "Versioning::forName(): Unable to retrieve class " +
                    nmsPackage + "." + className + ".");
            e.printStackTrace();
            return null;
        }
    }
}
