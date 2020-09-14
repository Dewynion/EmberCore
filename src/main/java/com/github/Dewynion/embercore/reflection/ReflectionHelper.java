package com.github.Dewynion.embercore.reflection;

import com.github.Dewynion.embercore.EmberCore;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ReflectionHelper {
    public final static String CLASS_EXTENSION = ".class";
    public final static String PLUGIN_FILE_METHOD = "getFile";
    private static HashMap<JavaPlugin, List<Class<? extends Object>>> assemblies;
    private static HashMap<JavaPlugin, Set<Object>> singletons;

    static {
        assemblies = new HashMap<>();
        singletons = new HashMap<>();
    }

    public static List<Class<? extends Object>> getAllClasses(JavaPlugin plugin)
            throws IOException {
        return getAllClasses(plugin, false);
    }

    public static List<Class<? extends Object>> getAllClasses(JavaPlugin plugin, boolean reload)
            throws IOException {
        if (reload || !assemblies.containsKey(plugin)) {
            EmberCore core = EmberCore.getInstance();
            File pluginFile = getPluginFile(plugin);
            if (pluginFile == null)
                throw new IOException("EmberCore::ReflectionHelper: unable to load plugin assembly for " +
                        plugin.getName() + ".");
            core.getLogger().info("ReflectionHelper: Retrieving assembly for " + plugin.getName() + ".");
            List<Class<?>> classes = new ArrayList<>();
            ZipFile zip = new ZipFile(pluginFile);
            String packagee = plugin.getClass().getPackage().getName()
                    .replaceAll("\\.", "/");
            assemblies.put(plugin, classes);
            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                // if it's a directory
                if (!name.startsWith(packagee))
                    continue;
                name = name.substring(packagee.length() + 1);
                if (name.endsWith(CLASS_EXTENSION)) {
                    String className = (packagee + "."
                            + name.substring(0, name.length() - CLASS_EXTENSION.length()))
                            .replaceAll("/", ".");
                    core.getLogger().info("  Found class     " + className);
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException ex) {
                        core.getLogger().warning("  class "
                                + className + " not found. Skipping.");
                    }
                }
            }
            zip.close();
        }

        return assemblies.get(plugin);
    }

    public static Set<Object> getSingletons(JavaPlugin plugin) {
        return getSingletons(plugin, false);
    }

    /**
     * Retrieves the assembly for the given {@link JavaPlugin} via {@link #getAllClasses(JavaPlugin)},
     * then instantiates any classes tagged with it.
     * <p>
     * Classes with a lower {@link Singleton#priority()} are
     * loaded first - priority 1 is loaded before priority 2, etc. This allows developers to ensure that one class
     * (e.g. a class for managing custom entities) consistently loads before another (e.g. a listener that creates
     * custom entities in place of vanilla spawns).
     *
     * @param plugin - The plugin to load for.
     * @param reload - If false, will return the precached list of Singletons or load them if they have not
     *               yet been cached; if true, will always attempt to load. Will not instantiate any classes already
     *               instantiated.
     * @return
     */
    public static Set<Object> getSingletons(JavaPlugin plugin, boolean reload) {
        try {
            EmberCore core = EmberCore.getInstance();
            Set<Object> pluginSingletons;
            if (!singletons.containsKey(plugin))
                pluginSingletons = new HashSet<>();
            else {
                if (!reload)
                    return singletons.get(plugin);
                else
                    pluginSingletons = singletons.get(plugin);
            }
            List<Class<? extends Object>> assembly = ReflectionHelper.getAllClasses(plugin);
            assembly.stream().filter(clz -> clz.isAnnotationPresent(Singleton.class))
                    .sorted(Comparator.comparingInt(clz -> clz.getAnnotation(Singleton.class).priority()))
                    .forEach(clz -> {
                        try {
                            // don't duplicate if this singleton has been instantiated already
                            if (!pluginSingletons.stream().anyMatch(obj -> obj.getClass().equals(clz))) {
                                Object inst = clz.getConstructor().newInstance();
                                core.getLogger().info("  Loaded singleton " + clz.getSimpleName() + " with priority " +
                                        clz.getAnnotation(Singleton.class).priority());
                                pluginSingletons.add(inst);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            singletons.put(plugin, pluginSingletons);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        return singletons.get(plugin);
    }

    /**
     * Calls {@link Bukkit#getServer()}'s {@link org.bukkit.plugin.PluginManager#registerEvents(Listener, Plugin)}
     * on all {@link Singleton}s that implement {@link Listener}. Singletons are retrieved via
     * {@link #getSingletons(JavaPlugin)}, so this method can be called by itself on plugin enable to
     * load the plugin's assembly, instantiate all singletons, and register Listeners.
     *
     * @param plugin - The plugin to register events for.
     */
    public static void registerEvents(JavaPlugin plugin) {
        EmberCore core = EmberCore.getInstance();
        getSingletons(plugin).forEach(inst -> {
            if (inst instanceof Listener) {
                Bukkit.getServer().getPluginManager().registerEvents((Listener) inst, plugin);
                core.getLogger().info("    Registered listener for " + inst.getClass().getSimpleName() + ".");
            }
        });
    }

    /**
     * Reflectively retrieves the plugin's .jar file from the plugins folder.
     */
    public static File getPluginFile(JavaPlugin plugin) {
        // to be blunt there's no way this should ever fail
        try {
            Method fileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
            fileMethod.setAccessible(true);
            return (File) fileMethod.invoke(plugin);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Calls all methods tagged with {@link PostSetup} in {@link Singleton}s
     * for the given plugin.
     */
    public static void postSetup(JavaPlugin plugin) {
        Map<Method, Object> postSetupMethods = new HashMap<>();
        getSingletons(plugin).forEach(inst -> {
            Method[] methodArr = inst.getClass().getMethods();
            for (Method method : methodArr) {
                if (method.isAnnotationPresent(PostSetup.class) &&
                        method.getParameters().length == 0) {
                    postSetupMethods.put(method, inst);
                }
            }
        });
        EmberCore.log(Level.INFO, "Calling post-setup for " + plugin.getName() + ".");
        postSetupMethods.keySet().stream().sorted(Comparator.comparingInt(m
                -> m.getAnnotation(PostSetup.class).priority())).forEach(m
                -> {
            try {
                m.invoke(postSetupMethods.get(m));
            } catch (Exception e) {
                EmberCore.log(Level.SEVERE, "Something went wrong during post-setup:");
                e.printStackTrace();
            }
        });
    }
}
