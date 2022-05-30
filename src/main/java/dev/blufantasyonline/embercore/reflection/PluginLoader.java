package dev.blufantasyonline.embercore.reflection;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.blufantasyonline.embercore.CoreSettings;
import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.command.CommandHandler;
import dev.blufantasyonline.embercore.command.CommandRunner;
import dev.blufantasyonline.embercore.config.serialization.jackson.SimpleModuleBuilder;
import dev.blufantasyonline.embercore.config.serialization.jackson.TypedKeyDeserializer;
import dev.blufantasyonline.embercore.config.serialization.jackson.TypedKeySerializer;
import dev.blufantasyonline.embercore.reflection.annotations.*;
import dev.blufantasyonline.embercore.util.ErrorUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PluginLoader {
    public final static String CLASS_EXTENSION = ".class";
    public final static String PLUGIN_FILE_METHOD = "getFile";
    private static final int COMMAND_TREE_SPACE_DEPTH = 2;
    private final static HashMap<JavaPlugin, List<Class<? extends Object>>> assemblies;
    private final static HashMap<JavaPlugin, LinkedHashMap<Class<?>, Object>> singletons;
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
     *
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
     *
     * @param plugin         The plugin that registered the class. If the plugin is not loaded,
     *                       it will be loaded first.
     * @param singletonClass The class to search for an instance of.
     * @param <T>            The type of the requested class.
     * @return Either the registered single instance of the class, or null if not found.
     */
    public static <T> T getSingleton(JavaPlugin plugin, Class<T> singletonClass) {
        if (!singletons.containsKey(plugin))
            load(plugin);
        return (T) singletons.get(plugin).get(singletonClass);
    }

    public static <T> T getSingleton(Class<T> singletonClass) {
        for (Map<Class<?>, Object> singletonMap : singletons.values()) {
            if (singletonMap.containsKey(singletonClass))
                return (T) singletonMap.get(singletonClass);
        }
        return null;
    }

    //===========================================================================================================
    // Private helper methods.
    //===========================================================================================================

    private static List<Class<?>> filterAnnotations(List<Class<?>> list, Class<? extends Annotation> annotation,
                                                    Comparator<? super Class<?>> sortCondition) {
        Stream<Class<?>> stream = list.stream().filter(clz -> clz.isAnnotationPresent(annotation));
        if (sortCondition != null)
            return stream.sorted(sortCondition).collect(Collectors.toList());
        return stream.collect(Collectors.toList());
    }

    private static Map<Class<?>, Method> filterMethods(List<Class<?>> list, Class<? extends Annotation> annotation,
                                                       Comparator<? super Method> comparator) {
        LinkedHashMap<Class<?>, Method> map = new LinkedHashMap<>();
        list.forEach(clz -> {
                    try {
                        Stream<Method> methods = Arrays.stream(clz.getDeclaredMethods())
                                .filter(method -> method.isAnnotationPresent(annotation));
                        if (comparator != null)
                                methods = methods.sorted(comparator);
                        methods.forEach(method -> {
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

            // handle preloads first if anyone really wants to shove these in here
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

            if (plugin == EmberCore.getInstance()) {
                Level level = CoreSettings.coreLogSettings.logLevel;
                EmberCore.info("Your log level is set to %s.", level.toString());
                // Level is not an enum, so it's time to pull a YandereDev
                if (level.equals(Level.OFF))
                    EmberCore.info("This will disable logging entirely, but will not stop critical errors from printing to console.");
                else if (level.equals(Level.INFO))
                    EmberCore.info("This will produce a significant quantity of messages - set your log level to OFF, WARNING or SEVERE to disable this.");
                EmberCore.getInstance().getLogger().setLevel(level);
            }

            // first we figure out which classes are loaded and cached
            List<Class<?>> onEnable = filterAnnotations(assembly, OnEnable.class, null);

            EmberCore.info("Loading plugin %s...", plugin.getName());
            EmberCore.info("Instantiating singletons...");
            instantiateAll(plugin, onEnable);

            EmberCore.info("Calculating dependencies...");
            onEnable = byDependencies(plugin, onEnable);

            // then any methods they run
            Map<Class<?>, Method> onEnableMethods = filterMethods(onEnable, OnEnable.class, null);

            EmberCore.info("Running on-enable methods...");
            runAll(plugin, onEnableMethods);

            // inject configuration-related values.
            EmberCore.info("Injecting configured values...");
            singletons.get(plugin).values().forEach(obj -> {
                ConfigInjector.injectIntoObject(plugin, obj);
            });

            Map<Class<?>, Method> afterEnableMethods = filterMethods(onEnable, AfterEnable.class, null);

            EmberCore.info("Running after-enable methods...");
            runAll(plugin, afterEnableMethods);

            // do this last just in case
            EmberCore.info("Assembling command tree...");
            setupCommands(plugin);

            EmberCore.info("Plugin %s loaded.", plugin.getName());
        } catch (IOException ex) {
            ErrorUtil.severe("Encountered an I/O error while processing annotations for %s.", plugin.getName());
            ex.printStackTrace();
        }
    }

    private static ArrayList<Class<?>> byDependencies(JavaPlugin plugin, List<Class<?>> input) {
        ArrayList<Class<?>> byDependencies = new ArrayList<>();
        Map<Class<?>, Set<Class<?>>> dependencyMap = new HashMap<>();
        // not sure how necessary this is but reverse index mapping go brr
        Map<Class<?>, Integer> indexMap = new HashMap<>();
        AtomicInteger index = new AtomicInteger(0);

        // build dependency map
        input.forEach(clz -> {
            byDependencies.add(clz);
            indexMap.put(clz, index.getAndIncrement());
            dependencyMap.put(clz, new HashSet<>());
            Set<Class<?>> dependencies = dependencyMap.get(clz);
            if (clz.isAnnotationPresent(Depend.class))
                dependencies.addAll(Arrays.asList(clz.getAnnotation(Depend.class).value()));

            Object instance = getSingleton(plugin, clz);
            for (Field field : clz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Inject.class))
                    continue;
                field.setAccessible(true);
                Class<?> fieldType = field.getType();

                // add dependency
                dependencies.add(fieldType);

                // actually inject
                Object targetInstance = getSingleton(fieldType);
                if (instance == null && !Modifier.isStatic(field.getModifiers()))
                    EmberCore.warn("No singleton of type %s exists to inject into non-static field %s.",
                            clz.getName(), field.getName());
                else if (targetInstance == null)
                    EmberCore.warn("Field %s in class %s is of type %s, but no such singleton exists across all plugins. Did you forget a dependency?",
                            field.getName(), clz.getName(), fieldType.getName());
                else {
                    EmberCore.logInjection("  Injecting singleton of type %s into field %s (class %s)...",
                            fieldType.getName(), field.getName(), clz.getName());
                    try {
                        field.set(instance, targetInstance);
                        EmberCore.logInjection("    ...Success!");
                    } catch (IllegalAccessException e) {
                        EmberCore.warn("    Illegal access exception encountered during injection: %s", e.getMessage());
                    }
                }
            }
        });

        // flag to determine if the list is sorted
        boolean sorted;
        do {
            // assume sorted at start
            sorted = true;
            // go through the list
            for (int i = 0; i < byDependencies.size(); i++) {
                Class<?> clz = byDependencies.get(i);
                int selfIndex = i;
                // for all dependencies of this element
                for (Class<?> dep : dependencyMap.get(clz)) {
                    // ignore circular dependencies so we don't end up stuck in an infinite loop
                    if (dependencyMap.get(dep).contains(clz))
                        continue;
                    // grab the cached index of this dependency
                    int otherIndex = indexMap.get(dep);
                    // if it's set to load after this element,
                    if (otherIndex > selfIndex) {
                        // swap them
                        byDependencies.set(selfIndex, dep);
                        byDependencies.set(otherIndex, clz);
                        selfIndex = otherIndex;
                        // and make sure to update the index cache to reflect the new indices
                        indexMap.put(dep, selfIndex);
                        indexMap.put(clz, otherIndex);
                        sorted = false;
                    }
                }
            }
        } while (!sorted);

        EmberCore.info("Dynamically determined load order:");
        byDependencies.forEach(clz -> EmberCore.info("  %s", clz.getName()));

        return byDependencies;
    }

    private static void setupCommands(JavaPlugin plugin) {
        Set<CommandRunner> commands = getCommandStream(plugin).collect(Collectors.toSet());
        if (commands.size() == 0) {
            EmberCore.info("  Plugin %s has no recognized commands. Skipping.", plugin.getName());
            return;
        }

        commands.forEach(cmd -> {
                    if (cmd.isRoot()) {
                        EmberCore.info("  Found root command: %s", cmd.getIdentifier());
                        CommandHandler.addRootCommand(plugin, cmd);
                    } else {
                        cmd.setParent(getSingleton(plugin, cmd.getParentClass()));
                        EmberCore.info("    Found leaf command: %s (parent %s)", cmd.getIdentifier(),
                                cmd.getParentClass().getSimpleName());
                    }
                }
        );

        // because stream consume
        EmberCore.info("Command tree:");
        commands.stream()
                .filter(CommandRunner::isRoot)
                .forEach(cmd -> printChildren(cmd, COMMAND_TREE_SPACE_DEPTH));
    }

    private static Stream<CommandRunner> getCommandStream(JavaPlugin plugin) {
        return singletons.get(plugin).values().stream()
                .filter(obj -> obj instanceof CommandRunner)
                .map(obj -> (CommandRunner) obj);
    }

    private static void printChildren(CommandRunner cmd, int depth) {
        EmberCore.info("%s%s", new String(new char[depth]).replace("\0", " "),
                cmd.getIdentifier());
        for (CommandRunner child : cmd.getChildren())
            printChildren(child, depth + COMMAND_TREE_SPACE_DEPTH);
    }

    private static void instantiateAll(JavaPlugin plugin, List<Class<?>> classes) {
        // map of all class to instance relations for this plugin
        if (!singletons.containsKey(plugin))
            singletons.put(plugin, new LinkedHashMap<>());
        Map<Class<?>, Object> singletonsForPlugin = singletons.get(plugin);
        classes.forEach(clz -> {
            try {
                // ========================================================================
                // Register serializers. The typed key serializer check exists because Jackson
                // is a little weird on how they decided to separate key (de)serializers.
                // ========================================================================
                if (!TypedKeySerializer.class.isAssignableFrom(clz) && JsonSerializer.class.isAssignableFrom(clz)) {
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
                    // Register key serializers.
                    // ========================================================================
                } else if (TypedKeySerializer.class.isAssignableFrom(clz)) {
                    SimpleModule keyModule = SimpleModuleBuilder.keySerializerModule((Class<? extends TypedKeySerializer>) clz);
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
