package dev.blufantasyonline.embercore.reflection;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.blufantasyonline.embercore.CoreSettings;
import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.config.serialization.jackson.SimpleModuleBuilder;
import dev.blufantasyonline.embercore.config.serialization.jackson.TypedKeyDeserializer;
import dev.blufantasyonline.embercore.reflection.annotations.AfterEnable;
import dev.blufantasyonline.embercore.reflection.annotations.OnEnable;
import dev.blufantasyonline.embercore.reflection.annotations.Preload;
import dev.blufantasyonline.embercore.util.ErrorUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PluginLoader {
    public final static String CLASS_EXTENSION = ".class";
    public final static String PLUGIN_FILE_METHOD = "getFile";
    private final static HashMap<JavaPlugin, List<Class<? extends Object>>> assemblies;
    private final static HashMap<JavaPlugin, Map<Class<?>, Object>> singletons;
    // I don't know why the above are "final static", I always write static final lmao
    private static final Set<SimpleModule> defaultModules;

    static {
        assemblies = new HashMap<>();
        singletons = new HashMap<>();
        defaultModules = new HashSet<>();
    }

    //===========================================================================================================
    // Publicly accessible methods.
    //===========================================================================================================

    public static void register(JavaPlugin plugin) {
        load(plugin);
    }

    public static boolean registered(JavaPlugin plugin) {
        return assemblies.containsKey(plugin);
    }

    public static void remove(JavaPlugin plugin) {
        assemblies.remove(plugin);
        singletons.remove(plugin);
    }

    public static List<Class<? extends Object>> getAllClasses(JavaPlugin plugin)
            throws IOException {
        return getAllClasses(plugin, false);
    }

    public static List<Class<? extends Object>> getAllClasses(JavaPlugin plugin, boolean reload)
            throws IOException {
        if (reload || !assemblies.containsKey(plugin)) {
            File pluginFile = getPluginFile(plugin);
            if (pluginFile == null)
                throw new IOException("EmberCore::ReflectionHelper: unable to load plugin assembly for " +
                        plugin.getName() + ".");
            EmberCore.info("Retrieving assembly for %s.", plugin.getName());
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
                    EmberCore.info("  Found class     %s", className);
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException ex) {
                        EmberCore.warn("  class %s not found. Skipping.", className);
                    }
                }
            }
            zip.close();
        }

        return assemblies.get(plugin);
    }

    /**
     * Reflectively retrieves a plugin's .jar file from the plugins folder.
     * @param plugin An instance of a Spigot plugin.
     * @return The provided plugin's packaged .jar file.
     */
    public static File getPluginFile(JavaPlugin plugin) {
        // to be blunt there's no way this should ever fail
        try {
            Method fileMethod = JavaPlugin.class.getDeclaredMethod(PLUGIN_FILE_METHOD);
            fileMethod.setAccessible(true);
            return (File) fileMethod.invoke(plugin);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * See {@link #getSingletons(JavaPlugin, boolean)}.
     */
    public static Collection<Object> getSingletons(JavaPlugin plugin) {
        return getSingletons(plugin, false);
    }

    /**
     * Retrieves the assembly for the given {@link JavaPlugin} via {@link #getAllClasses(JavaPlugin)},
     * then instantiates any classes tagged with it.
     * <p>
     * Classes with a lower {@link OnEnable#priority()} are
     * loaded first - priority 1 is loaded before priority 2, etc. This allows developers to ensure that one class
     * (e.g. a class for managing custom entities) consistently loads before another (e.g. a listener that creates
     * custom entities in place of vanilla spawns).
     *
     * @param plugin The plugin to load for.
     * @param reload If false, will return the precached list of Singletons or load them if they have not
     *               yet been cached; if true, will always attempt to load. Will not instantiate any classes already
     *               instantiated.
     * @return All single-instance classes registered by the provided plugin.
     */
    public static Collection<Object> getSingletons(JavaPlugin plugin, boolean reload) {
        if (reload || !singletons.containsKey(plugin))
            load(plugin);
        return singletons.get(plugin).values();
    }

    /**
     * Attempts to retrieve a single-instance class of the specified Java type.
     * @param plugin The plugin that registered the class. If the plugin is not loaded,
     *        it will be loaded first.
     * @param singletonClass The class to search for an instance of.
     * @param <T> The type of the requested class.
     * @return Either the registered single instance of the class, or null if not found.
     */
    public static <T> T getSingleton(JavaPlugin plugin, Class<T> singletonClass) {
        if (!singletons.containsKey(plugin))
            load(plugin);
        return (T) singletons.get(plugin).get(singletonClass);
    }

    //===========================================================================================================
    // Private helper methods.
    //===========================================================================================================

    private static List<Class<?>> filterAnnotations(List<Class<?>> list, Class<? extends Annotation> annotation,
                                                    Comparator<? super Class<?>> sortCondition) {
        Stream<Class<?>> stream = list.stream().filter(clz -> clz.isAnnotationPresent(annotation));
        if (sortCondition != null)
                stream = stream.sorted(sortCondition);
        return stream.collect(Collectors.toList());
    }

    private static Map<Class<?>, Method> filterMethods(List<Class<?>> list, Class<? extends Annotation> annotation,
                                                       Comparator<? super Method> comparator) {
        Map<Class<?>, Method> map = new HashMap<>();
        list.forEach(clz -> {
                    try {
                        Arrays.stream(clz.getDeclaredMethods())
                                .filter(method -> method.isAnnotationPresent(annotation))
                                .sorted(comparator)
                                .forEach(method -> {
                                    method.setAccessible(true);
                                    map.put(clz, method);
                                });
                    } catch (Exception ex) {
                        EmberCore.warn("Error occurred while loading class %s: %s", clz.getName(),
                                ex.getMessage());
                        ex.printStackTrace();
                    }
                }
        );
        return map;
    }

    private static void load(JavaPlugin plugin) {
        try {
            // grab all plugin classes
            List<Class<?>> assembly = getAllClasses(plugin);

            // handle preloads (essential configs, etc.) first.
            List<Class<?>> preload = filterAnnotations(assembly, Preload.class, null);
            EmberCore.info("Preloading plugin %s...", plugin.getName());

            // add default modules during preload phase
            if (!plugin.equals(EmberCore.getInstance())) {
                EmberCore.info("Applying builtin serialization overrides. These are for compatibility purposes.");
                EmberCore.info("These modules make use of Jackson's BeanSerializerModifier functionality in order to " +
                        "modify how built-in Spigot classes serialize. Please only override them if you are very sure " +
                        "of what you are doing.");
                for (SimpleModule module : defaultModules) {
                    EmberCore.info("  Module: %s", module.getModuleName());
                    ConfigInjector.registerModule(plugin, module);
                }
            }

            instantiateAll(plugin, preload);
            // TODO: please make this less lazy, it injects into preloads AGAIN after the fact lmao
            singletons.get(plugin).values().forEach(obj -> {
                ConfigInjector.injectIntoObject(plugin, obj);
            });

            Level level = CoreSettings.coreLogSettings.logLevel;
            EmberCore.info("Your log level is set to %s.", level.toString());
            // Level is not an enum, so it's time to pull a YandereDev
            if (level.equals(Level.OFF))
                EmberCore.info("This will disable logging entirely, but will not stop critical errors from printing to console.");
            else if (level.equals(Level.INFO))
                EmberCore.info("This will produce a significant quantity of messages - set your log level to OFF, WARNING or SEVERE to disable this.");
            EmberCore.getInstance().getLogger().setLevel(level);

            // first we figure out which classes are loaded and cached
            List<Class<?>> onEnable = filterAnnotations(assembly, OnEnable.class,
                    Comparator.comparingInt(clz -> clz.getAnnotation(OnEnable.class).priority()));
            // then any methods they run
            Map<Class<?>, Method> onEnableMethods = filterMethods(onEnable, OnEnable.class,
                    Comparator.comparingInt(method -> method.getAnnotation(OnEnable.class).priority()));

            EmberCore.info("Loading plugin %s...", plugin.getName());
            EmberCore.info("Instantiating singletons...");
            instantiateAll(plugin, onEnable);
            EmberCore.info("Running on-enable methods...");
            runAll(plugin, onEnableMethods);

            // inject configuration-related values.
            EmberCore.info("Injecting values...");
            singletons.get(plugin).values().forEach(obj -> {
                ConfigInjector.injectIntoObject(plugin, obj);
            });

            Map<Class<?>, Method> afterEnableMethods = filterMethods(onEnable, AfterEnable.class,
                    Comparator.comparingInt(method -> method.getAnnotation(AfterEnable.class).priority()));

            EmberCore.info("Running after-enable methods...");
            runAll(plugin, afterEnableMethods);

            EmberCore.info("Plugin %s loaded.", plugin.getName());
        } catch (IOException ex) {
            ErrorUtil.severe("Encountered an I/O error while processing annotations for %s.", plugin.getName());
            ex.printStackTrace();
        }
    }

    private static void instantiateAll(JavaPlugin plugin, List<Class<?>> classes) {
        // map of all class to instance relations for this plugin
        if (!singletons.containsKey(plugin))
            singletons.put(plugin, new HashMap<>());
        Map<Class<?>, Object> singletonsForPlugin = singletons.get(plugin);
        classes.forEach(clz -> {
            try {
                // ========================================================================
                // Register serializers.
                // ========================================================================
                if (JsonSerializer.class.isAssignableFrom(clz)) {
                    SimpleModule serializerModule = SimpleModuleBuilder.serializerModule((Class<? extends JsonSerializer>) clz);
                    ConfigInjector.registerModule(plugin, serializerModule);
                    if (plugin.equals(EmberCore.getInstance()))
                        defaultModules.add(serializerModule);

                // ========================================================================
                // Or deserializers. Can't be both, so else is fine.
                // TODO: A more "friendly" serialization system that uses Spigot stuff (MemorySections, etc.) so that
                //       folks don't have to learn Jackson in order to use EmberCore.
                // ========================================================================
                } else if (JsonDeserializer.class.isAssignableFrom(clz)) {
                    SimpleModule deserializerModule = SimpleModuleBuilder.deserializerModule((Class<? extends JsonDeserializer>) clz);
                    ConfigInjector.registerModule(plugin, deserializerModule);
                    if (plugin.equals(EmberCore.getInstance()))
                        defaultModules.add(deserializerModule);

                // ========================================================================
                // Register key deserializers.
                // ========================================================================
                } else if (TypedKeyDeserializer.class.isAssignableFrom(clz)) {
                    SimpleModule keyModule = SimpleModuleBuilder.keyDeserializerModule((Class<? extends TypedKeyDeserializer>) clz);
                    ConfigInjector.registerModule(plugin, keyModule);
                    if (plugin.equals(EmberCore.getInstance()))
                        defaultModules.add(keyModule);
                // ========================================================================
                // Register totally normal classes, including listeners.
                // ========================================================================
                } else {
                    EmberCore.info("Attempting to create singleton class %s...",
                            clz.getName());
                    Constructor<?> constructor = clz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    // otherwise if it's a totally normal class
                    constructor = clz.getDeclaredConstructor();
                    Object instance = constructor.newInstance();

                    // Register this if it's a listener.
                    // No, you cannot have serializers also be listeners. Single-purpose, my dude.
                    if (Listener.class.isAssignableFrom(clz)) {
                        EmberCore.info("    Registering event listener...");
                        Bukkit.getServer().getPluginManager().registerEvents((Listener) instance, plugin);
                        EmberCore.info("    Registered listener %s.", clz.getSimpleName());
                    }

                    singletonsForPlugin.put(clz, instance);
                }

                // 1.0.3 update: pog, kinda
                EmberCore.info("  ...Success!");
            } catch (NoSuchMethodException e) {
                ErrorUtil.severe("  No parameterless constructor was found for singleton class %s. Could not instantiate singleton.",
                        clz.getName());
            } catch (IllegalAccessException e) {
                ErrorUtil.severe("  Unable to access constructor for singleton class %s.",
                        clz.getName());
            } catch (InstantiationException e) {
                ErrorUtil.severe("  Class %s is marked as a singleton, but cannot be instantiated. Is it abstract or an interface?",
                        clz.getName());
            } catch (InvocationTargetException e) {
                ErrorUtil.severe("  Invocation target exception when creating singleton class %s.",
                        clz.getName());
                // more difficult exception to pinpoint, leave the stacktrace in.
                e.printStackTrace();
            } catch (IllegalPluginAccessException e) {
                EmberCore.severe("  Plugin access exception occurred when creating singleton class %s: %s",
                        clz.getName(), e.getMessage());
            }
        });
    }

    private static void runAll(JavaPlugin plugin, Map<Class<?>, Method> methods) {
        methods.forEach((clz, method) -> {
            method.setAccessible(true);
            try {
                method.invoke(getSingleton(plugin, clz));
            } catch (IllegalAccessException e) {
                ErrorUtil.severe("Unable to access method %s for singleton class %s.",
                        method.getName(), clz.getName());
            } catch (InvocationTargetException e) {
                ErrorUtil.severe("Invocation target exception when executing method %s for singleton class %s.",
                        method.getName(), clz.getName());
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                ErrorUtil.warn("Method %s in singleton class %s could not be invoked due to an argument mismatch.",
                        method.getName(), clz.getName());
                ErrorUtil.warn("Please ensure all methods annotated with @OnEnable or @AfterEnable have no parameters");
            }
        });
    }
}
