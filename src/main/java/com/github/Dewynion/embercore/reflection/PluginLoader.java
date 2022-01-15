package com.github.Dewynion.embercore.reflection;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.reflection.annotations.AfterEnable;
import com.github.Dewynion.embercore.reflection.annotations.OnEnable;
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
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PluginLoader {
    public final static String CLASS_EXTENSION = ".class";
    public final static String PLUGIN_FILE_METHOD = "getFile";
    private final static HashMap<JavaPlugin, List<Class<? extends Object>>> assemblies;
    private final static HashMap<JavaPlugin, Map<Class<?>, Object>> singletons;

    static {
        assemblies = new HashMap<>();
        singletons = new HashMap<>();
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
            EmberCore core = EmberCore.getInstance();
            File pluginFile = getPluginFile(plugin);
            if (pluginFile == null)
                throw new IOException("EmberCore::ReflectionHelper: unable to load plugin assembly for " +
                        plugin.getName() + ".");
            EmberCore.info("ReflectionHelper: Retrieving assembly for %s.", plugin.getName());
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

    /**
     * Reflectively retrieves the plugin's .jar file from the plugins folder.
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
     * @param plugin - The plugin to load for.
     * @param reload - If false, will return the precached list of Singletons or load them if they have not
     *               yet been cached; if true, will always attempt to load. Will not instantiate any classes already
     *               instantiated.
     * @return
     */
    public static Collection<Object> getSingletons(JavaPlugin plugin, boolean reload) {
        if (reload || !singletons.containsKey(plugin))
            load(plugin);
        return singletons.get(plugin).values();
    }

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
        return list.stream().filter(clz -> clz.isAnnotationPresent(annotation))
                .sorted(sortCondition)
                .collect(Collectors.toList());
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
            singletons.get(plugin).values().forEach(obj -> ConfigInjector.injectIntoObject(plugin, obj));

            Map<Class<?>, Method> afterEnableMethods = filterMethods(onEnable, AfterEnable.class,
                    Comparator.comparingInt(method -> method.getAnnotation(AfterEnable.class).priority()));

            EmberCore.info("Running after-enable methods...");
            runAll(plugin, afterEnableMethods);

            EmberCore.info("Plugin %s loaded.", plugin.getName());
        } catch (IOException ex) {
            EmberCore.severe("Encountered an I/O error while processing annotations for %s.", plugin.getName());
            ex.printStackTrace();
        }
    }

    private static void instantiateAll(JavaPlugin plugin, List<Class<?>> classes) {
        // map of all class to instance relations for this plugin
        Map<Class<?>, Object> singletonsForPlugin = new HashMap<>();
        classes.forEach(clz -> {
            try {
                EmberCore.info("Attempting to create singleton class %s...",
                        clz.getName());
                Constructor<?> constructor;
                final Object[] instance = new Object[1];

                // ========================================================================
                // Register serializers.
                // ========================================================================
                if (StdSerializer.class.isAssignableFrom(clz)) {
                    EmberCore.info("    Registering standard serializer...");
                    // set up new default constructor
                    constructor = clz.getDeclaredConstructor(JsonSerializer.class);
                    constructor.setAccessible(true);
                    // todo: put the generic class name back for logging purposes

                    SimpleModule serializerModule = new SimpleModule();
                    serializerModule.setSerializerModifier(new BeanSerializerModifier() {
                        @Override
                        public JsonSerializer<?> modifySerializer(SerializationConfig config,
                                                                  BeanDescription beanDesc,
                                                                  JsonSerializer<?> serializer) {

                            if (beanDesc.getBeanClass().equals(ReflectionUtil.getGenericSuperclassType(clz)))
                                try {
                                    instance[0] = constructor.newInstance(serializer);
                                    return (JsonSerializer<?>) instance[0];
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            return serializer;
                        }
                    });
                    ConfigInjector.registerModule(plugin, serializerModule);

                // ========================================================================
                // Or deserializers. Can't be both, so else is fine.
                // TODO: A more "friendly" serialization system that uses Spigot stuff (MemorySections, etc.) so that
                //       folks don't have to learn Jackson in order to use EmberCore.
                // ========================================================================
                } else if (StdDeserializer.class.isAssignableFrom(clz)) {
                    String typeName = ReflectionUtil.getGenericSuperclassType(clz).getTypeName();
                    EmberCore.info("    Registering standard deserializer for type %s.", typeName);
                    // set up new default constructor
                    constructor = clz.getDeclaredConstructor(JsonDeserializer.class);
                    constructor.setAccessible(true);



                    // TODO: use typedeserializer?
                    SimpleModule deserializerModule = new SimpleModule();
                    deserializerModule.setDeserializerModifier(new BeanDeserializerModifier() {
                        @Override
                        public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                                      BeanDescription beanDesc,
                                                                      JsonDeserializer<?> deserializer) {

                            if (beanDesc.getBeanClass().equals(ReflectionUtil.getGenericSuperclassType(clz))) {
                                try {
                                    instance[0] = constructor.newInstance(deserializer);
                                    return (JsonDeserializer<?>) instance[0];
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            return deserializer;
                        }
                    });
                    ConfigInjector.registerModule(plugin, deserializerModule);
                } else {
                    // otherwise if it's a totally normal class
                    constructor = clz.getDeclaredConstructor();
                    instance[0] = constructor.newInstance();
                }

                // Register this if it's a listener.
                if (Listener.class.isAssignableFrom(clz)) {
                    EmberCore.info("    Registering event listener...");
                    Bukkit.getServer().getPluginManager().registerEvents((Listener) instance[0], plugin);
                    EmberCore.info("    Registered listener %s.", clz.getSimpleName());
                }

                // pog?
                EmberCore.info("  ...Success!");

                singletonsForPlugin.put(clz, instance[0]);
            } catch (NoSuchMethodException e) {
                EmberCore.severe("  No parameterless constructor was found for singleton class %s. Could not instantiate singleton.",
                        clz.getName());
            } catch (IllegalAccessException e) {
                EmberCore.severe("  Unable to access constructor for singleton class %s.",
                        clz.getName());
            } catch (InstantiationException e) {
                EmberCore.severe("  Class %s is marked as a singleton, but cannot be instantiated. Is it abstract or an interface?",
                        clz.getName());
            } catch (InvocationTargetException e) {
                EmberCore.severe("  Invocation target exception when creating singleton class %s.",
                        clz.getName());
                // genuinely don't know when this would pop up, so I'm leaving this here.
                e.printStackTrace();
            } catch (IllegalPluginAccessException e) {
                EmberCore.severe("  Plugin access exception occurred when creating singleton class %s: %s",
                        clz.getName(), e.getMessage());
            }
        });
        singletons.put(plugin, singletonsForPlugin);
    }

    private static void runAll(JavaPlugin plugin, Map<Class<?>, Method> methods) {
        methods.forEach((clz, method) -> {
            method.setAccessible(true);
            try {
                method.invoke(getSingleton(plugin, clz));
            } catch (IllegalAccessException e) {
                EmberCore.severe("Unable to access method %s for singleton class %s.",
                        method.getName(), clz.getName());
            } catch (InvocationTargetException e) {
                EmberCore.severe("Invocation target exception when executing method %s for singleton class %s.",
                        method.getName(), clz.getName());
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                EmberCore.warn("Method %s in singleton class %s could not be invoked due to an argument mismatch.",
                        method.getName(), clz.getName());
                EmberCore.warn("Please ensure all methods annotated with @OnEnable or @AfterEnable have no parameters");
            }
        });
    }
}
