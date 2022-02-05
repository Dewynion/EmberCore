package dev.blufantasyonline.embercore.reflection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.config.ConfigurationFormat;
import dev.blufantasyonline.embercore.config.PluginConfiguration;
import dev.blufantasyonline.embercore.config.serialization.ExcludeFromSerialization;
import dev.blufantasyonline.embercore.config.serialization.SerializationInfo;
import dev.blufantasyonline.embercore.reflection.annotations.Inject;
import dev.blufantasyonline.embercore.storage.sql.DbConnection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public final class ConfigInjector {
    private final static String DEFAULT_CONFIG_FILE = "config.yml";

    private final static String REGEX_CAMEL_TO_SNAKE = "(?<!^)(?=[A-Z])";
    private final static String SNAKE_CASE_JOINER = "-";

    private static HashMap<JavaPlugin, HashMap<String, PluginConfiguration>> pluginConfigurations = new HashMap<>();
    private static HashMap<JavaPlugin, MultiFormatObjectMapperWrapper> pluginObjectMappers = new HashMap<>();

    public static ObjectMapper getObjectMapper(JavaPlugin plugin) {
        return getObjectMapper(plugin, ConfigurationFormat.YAML);
    }

    public static ObjectMapper getObjectMapper(JavaPlugin plugin, ConfigurationFormat configurationFormat) {
        if (!pluginObjectMappers.containsKey(plugin)) {
            MultiFormatObjectMapperWrapper mapper = new MultiFormatObjectMapperWrapper();
            pluginObjectMappers.put(plugin, mapper);
        }
        return pluginObjectMappers.get(plugin).getMapperFor(configurationFormat);
    }

    public static PluginConfiguration getPluginConfiguration(JavaPlugin plugin, String name) {
        pluginConfigurations.putIfAbsent(plugin, new HashMap<>());
        if (!Paths.get(name).isAbsolute())
            name = new File(plugin.getDataFolder(), name).getAbsolutePath();
        return pluginConfigurations.get(plugin).get(name);
    }

    public static void injectIntoObject(JavaPlugin plugin, Object o) {
        injectIntoObject(plugin, o, null);
    }

    public static void injectIntoObject(JavaPlugin plugin, Object o, File file) {
        injectIntoObject(plugin, o, file, "");
    }

    public static void injectIntoObject(JavaPlugin plugin, Object o, DbConnection connection, String parentPath) {
        // TODO work something out with this
        EmberCore.warn("DbConnection injection is not yet implemented.");
    }

    public static void injectIntoObject(JavaPlugin plugin, Object o, File file, String parentPath) {
        File dataFolder = plugin.getDataFolder();
        if (!pluginConfigurations.containsKey(plugin))
            pluginConfigurations.put(plugin, new HashMap<>());

        HashMap<String, PluginConfiguration> configs = pluginConfigurations.get(plugin);
        if (o == null)
            return;

        Class<?> objectClass = o.getClass();

        // literally the type's default config filename.
        // this is so you can define a config file in an annotation for a class itself ("commands.yml" for instance)
        // and not have to add @SerializationInfo(path = "your file here") to every field.

        // first: cache fields to inject into.
        List<Field> fields = new ArrayList<>();

        String typeDefaultConfigFilename = "";

        // the superclass of a base level class will be Object, so when this loop calls the second time for object,
        // it will just cancel instead because the superclass of Object/enum/etc. is null.
        while (objectClass != null && objectClass.getSuperclass() != null) {
            SerializationInfo serializationInfo = objectClass.getAnnotation(SerializationInfo.class);
            // This will always be the highest-level default filename.
            if (serializationInfo != null && typeDefaultConfigFilename.isBlank()) {
                // TODO: Phase out filename.
                if (serializationInfo.location().isBlank())
                    typeDefaultConfigFilename = serializationInfo.filename();
                else
                    typeDefaultConfigFilename = serializationInfo.location();
            }

            for (Field field : objectClass.getDeclaredFields()) {
                try {
                    if (!ignore(field)) {
                        field.setAccessible(true);
                        fields.add(field);
                    }
                } catch (RuntimeException ex) {
                    EmberCore.warn("Unable to access field %s in class %s while injecting config.",
                            field.getName(), objectClass.getName());
                    ex.printStackTrace();
                }
            }
            // Don't use superclasses by default, and don't use them if the serialization info says no.
            if (serializationInfo == null || !serializationInfo.useSuperclasses())
                break;
            objectClass = objectClass.getSuperclass();
        }

        // Set this to the default config file if it's still blank.
        if (typeDefaultConfigFilename.isBlank())
            typeDefaultConfigFilename = DEFAULT_CONFIG_FILE;

        String parentNodePath = parentConfigPath(o, parentPath);

        EmberCore.logInjection("Injecting configured values into object of type %s.", o.getClass().getName());
        EmberCore.logInjection("Filename for this object: %s", typeDefaultConfigFilename);

        for (Field field : fields) {
            try {
                // get the config file
                String filename = typeDefaultConfigFilename;
                if (field.isAnnotationPresent(SerializationInfo.class)) {
                    String fieldFilename = field.getAnnotation(SerializationInfo.class).filename();
                    if (fieldFilename.length() > 0) {
                        EmberCore.logInjection("Filename override present on field %s: %s",
                                field.getName(), fieldFilename);
                        filename = fieldFilename;
                    }
                }
                File configFile = file != null ? file : new File(dataFolder, filename);
                String configFileName = configFile.getAbsolutePath();

                if (!configs.containsKey(configFileName)) {
                    EmberCore.info("Attempting to create plugin configuration from file %s", configFileName);
                    configs.put(configFileName, PluginConfiguration.create(configFile, plugin));
                }
                PluginConfiguration configuration = configs.get(configFileName);

                if (configuration == null) {
                    EmberCore.warn(plugin, "Unable to create a plugin configuration object from file %s. Field %s will retain its default value.",
                            configFileName, field.getName());
                    continue;
                }

                injectIntoField(field, o, configuration, parentNodePath);

                configuration.saveConfiguration();
            } catch (IllegalAccessException ex) {
                EmberCore.warn("Unable to access field %s in class %s while injecting config.",
                        field.getName(), objectClass.getName());
            }
        }
    }

    public static <T> void injectIntoField(Field field, Object o, PluginConfiguration pc) throws IllegalAccessException {
        injectIntoField(field, o, pc, "");
    }

    public static <T> void injectIntoField(Field field, Object o, PluginConfiguration pc, String parentNodePath) throws IllegalAccessException {
        // path to follow through config - use the path in SerializationInfo or just start from the root element
        String configPath = configPathName(field, parentNodePath);

        EmberCore.logInjection("Injecting configured value into field %s (config path %s)...", field.getName(), configPath);

        Object codedValue = field.get(o);
        Object configuredObject = pc.get(configPath, field, codedValue, true);
        field.set(o, configuredObject);

        EmberCore.logInjection("Field %s has a default value of %s.", field.getName(), codedValue == null ? "null" :
                codedValue.toString());
        EmberCore.logInjection("  Configured value: %s %s", configuredObject == null ? "null" : configuredObject.toString(),
                configuredObject == codedValue ? "(using default)" : "");
    }

    public static void readFromObject(PluginConfiguration pc, Object o) {
        readFromObject(pc, o, "");
    }

    public static void readFromObject(PluginConfiguration pc, Object o, String parentPath) {
        Class<?> objectClass = o.getClass();
        String parentNodePath = parentConfigPath(o, parentPath);
        for (Field field : objectClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if (ignore(field))
                    continue;
                readFromField(pc, field, o, parentNodePath);
            } catch (IllegalAccessException ex) {
                EmberCore.warn("Unable to access field %s in class %s while reading values into config.",
                        field.getName(), objectClass.getName());
            }
        }
    }

    public static void readFromField(PluginConfiguration pc, Field field, Object o, String parentNodePath) throws IllegalAccessException {
        String configPath = configPathName(field, parentNodePath);
        EmberCore.logInjection("  Setting value %s to the value of field %s.", configPath, field.getName());
        pc.set(configPath, field.get(o));
    }

    public static void registerModule(JavaPlugin plugin, SimpleModule module) {
        // laughed way too hard at this, i could have given it a better var name but like...this one's funny to me
        if (!pluginObjectMappers.containsKey(plugin))
            pluginObjectMappers.put(plugin, new MultiFormatObjectMapperWrapper());
        MultiFormatObjectMapperWrapper mfomw = pluginObjectMappers.get(plugin);
        mfomw.jsonMapper.registerModule(module);
        mfomw.yamlMapper.registerModule(module);
        mfomw.xmlMapper.registerModule(module);
    }

    private static String configPathName(Field field) {
        return configPathName(field, "");
    }

    private static String configPathName(Field field, String parentPath) {
        String configPath = Optional.ofNullable(field.getAnnotation(SerializationInfo.class))
                .map(SerializationInfo::path)
                .orElse("");

        if (configPath.isBlank())
            configPath = String.join(SNAKE_CASE_JOINER, field.getName().split(REGEX_CAMEL_TO_SNAKE));

        // slap that bad boy on there
        if (!parentPath.isBlank())
            configPath = parentPath + "." + configPath;
        return configPath.toLowerCase();
    }

    private static String parentConfigPath(Object object) {
        return parentConfigPath(object, "");
    }

    private static String parentConfigPath(Object object, String parentPath) {
        Class<?> objectClass = object.getClass();
        StringBuilder parentNodePathSb = new StringBuilder(parentPath);
        // loop through object + superclasses (if set to use superclasses), construct parent node
        // path based on SerializationInfo#path() if applicable
        while (objectClass != null && objectClass.getSuperclass() != null) {
            SerializationInfo serializationInfo = objectClass.getAnnotation(SerializationInfo.class);
            Optional.ofNullable(serializationInfo)
                    .filter(info -> !info.path().isBlank())
                    .stream().findFirst()
                    .ifPresent(info ->
                            // Insert the path at the *back* of the string builder.
                            parentNodePathSb.insert(0, info.path() + "."));
            if (serializationInfo == null || !serializationInfo.useSuperclasses())
                break;
            objectClass = objectClass.getSuperclass();
        }
        return parentNodePathSb.toString();
    }

    private static boolean ignore(Field field) {
        return field.isAnnotationPresent(JsonIgnore.class)
                || field.isAnnotationPresent(ExcludeFromSerialization.class)
                || field.isAnnotationPresent(Inject.class);
    }

    /**
     * Contains {@link ObjectMapper}s configured for different file formats.
     */
    private static class MultiFormatObjectMapperWrapper {
        private ObjectMapper yamlMapper, jsonMapper, xmlMapper;

        public MultiFormatObjectMapperWrapper() {
            yamlMapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            yamlMapper.addMixIn(Vector.class, VectorMixIn.class);
            yamlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
            yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            jsonMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .enable(SerializationFeature.INDENT_OUTPUT);
            jsonMapper.addMixIn(Vector.class, VectorMixIn.class);
            jsonMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
            jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            xmlMapper = new XmlMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            xmlMapper.addMixIn(Vector.class, VectorMixIn.class);
            xmlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
            xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }

        public ObjectMapper getMapperFor(ConfigurationFormat configurationFormat) {
            return switch (configurationFormat) {
                default -> yamlMapper;
                case JSON -> jsonMapper;
                case XML -> xmlMapper;
            };
        }
    }

    private interface VectorMixIn {
        @JsonIgnore
        void setX(float x);

        @JsonIgnore
        void setY(float y);

        @JsonIgnore
        void setZ(float z);

        @JsonIgnore
        void setX(int x);

        @JsonIgnore
        void setY(int y);

        @JsonIgnore
        void setZ(int z);
    }
}
