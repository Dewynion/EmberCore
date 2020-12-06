package com.github.Dewynion.embercore.reflection;

import com.github.Dewynion.embercore.EmberCore;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class Versioning {
    public static final String NMS_DOMAIN = "net.minecraft.server";
    public static final String CRAFTBUKKIT_DOMAIN = "org.bukkit.craftbukkit";
    public static final String CRAFTBUKKIT = "craftbukkit.";

    private static String version;
    private static String nmsPackage;

    private static String craftbukkitPackage;

    /*
     * https://gist.github.com/ReflxctionDev/84a5753a69f426f48ffe75f7ab3adcbb
     * pointed me in the right direction when it came to actually getting the version string.
     */
    static {
        version = Bukkit.getServer().getClass().getName();
        version = version.substring(version.indexOf(CRAFTBUKKIT)
                + CRAFTBUKKIT.length());
        version = version.substring(0, version.indexOf("."));
        nmsPackage = NMS_DOMAIN + "." + version;
        craftbukkitPackage = CRAFTBUKKIT_DOMAIN + "." + version;
        EmberCore.log(Level.INFO, "You're running NMS version " + version + ".");
        EmberCore.log(Level.INFO, "  NMS package: " + nmsPackage);
        EmberCore.log(Level.INFO, "  CB package: " + craftbukkitPackage);
    }

    /**
     * Attempts to retrieve an NMS class by name.
     */
    public static Class<?> getNMSClass(String className) throws NullPointerException {
        String fullName = nmsPackage + "." + className;
        EmberCore.log(Level.INFO, "Attempting to retrieve class: " + fullName);
        try {
            Class<?> clz = Class.forName(fullName);
            EmberCore.log(Level.INFO, "Success!");
            return clz;
        } catch (Exception e) {
            EmberCore.log(Level.SEVERE, "Versioning::forName(): Unable to retrieve class " +
                    fullName + ".");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Attempts to retrieve a Craftbukkit class by name.
     */
    public static Class<?> getCraftbukkitClass(String className) throws NullPointerException {
        String fullName = craftbukkitPackage + "." + className;
        EmberCore.log(Level.INFO, "Attempting to retrieve class: " + fullName);
        try {
            Class<?> clz = Class.forName(fullName);
            EmberCore.log(Level.INFO, "Success!");
            return clz;
        } catch (Exception e) {
            EmberCore.log(Level.SEVERE, "Versioning::forName(): Unable to retrieve class " +
                    fullName + ".");
            e.printStackTrace();
            return null;
        }
    }
}
