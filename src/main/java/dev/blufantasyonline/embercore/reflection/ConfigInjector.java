package dev.blufantasyonline.embercore.reflection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.config.ConfigurationFormat;
import dev.blufantasyonline.embercore.config.PluginConfiguration;
import dev.blufantasyonline.embercore.config.serialization.ExcludeFromSerialization;
import dev.blufantasyonline.embercore.config.serialization.SerializationInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

public final class ConfigInjector {
    private final static String DEFAULT_CONFIG_FILE = "config.yml";

    private final static String REGEX_CAMEL_TO_SNAKE = "(?<!^)(?=[A-Z])";
    private final static String SNAKE_CASE_JOINER = "-";

    private static HashMap<JavaPlugin, HashMap<String, PluginConfiguration>> pluginConfigurations = new HashMap<>();
    private static HashMap<JavaPlugin, MultiFormatObjectMapperWrapper> pluginObjectMappers = new HashMap<>();

    public static ObjectMapper getObjectMapper(JavaPlugin plugin, ConfigurationFormat configurationFormat) {
        if (!pluginObjectMappers.containsKey(plugin)) {
            MultiFormatObjectMapperWrapper mapper = new MultiFormatObjectMapperWrapper();
            pluginObjectMappers.put(plugin, mapper);
        }
        return pluginObjectMappers.get(plugin).getMapperFor(configurationFormat);
    }

    public static void injectIntoObject(JavaPlugin plugin, Object o) {
        injectIntoObject(plugin, o, null);
    }

    public static void injectIntoObject(JavaPlugin plugin, Object o, File file) {
        injectIntoObject(plugin, o, file, "");
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
        String typeDefaultConfigFilename = Optional.ofNullable(objectClass.getAnnotation(SerializationInfo.class))
                .filter(info -> info.filename().length() > 0)
                .map(SerializationInfo::filename)
                .orElse(DEFAULT_CONFIG_FILE);

        StringBuilder parentNodePathSb = new StringBuilder(parentPath);
        Optional.ofNullable(objectClass.getAnnotation(SerializationInfo.class))
                .filter(info -> info.path().length() > 0)
                .stream().findFirst()
                .ifPresent(info -> parentNodePathSb.append(info.path()).append("."));
        String parentNodePath = parentNodePathSb.toString();

        EmberCore.logInjection("Injecting configured values into object of type %s.", o.getClass().getName());
        EmberCore.logInjection("Filename for this object: %s", typeDefaultConfigFilename);

        for (Field field : objectClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                // TODO: remove ExcludeFromSerialization in future
                if (field.isAnnotationPresent(JsonIgnore.class) || field.isAnnotationPresent(ExcludeFromSerialization.class))
                    continue;
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
        String configPath = Optional.ofNullable(field.getAnnotation(SerializationInfo.class))
                .map(SerializationInfo::path)
                .orElse("");

        if (configPath.isEmpty())
            configPath = String.join(SNAKE_CASE_JOINER, field.getName().split(REGEX_CAMEL_TO_SNAKE));

        // slap that bad boy on there
        if (!parentNodePath.isEmpty())
            configPath = parentNodePath + "." + configPath;
        configPath = configPath.toLowerCase();

        EmberCore.logInjection("Injecting configured value into field %s (config path %s)...", field.getName(), configPath);

        Object codedValue = field.get(o);
        Object configuredObject = pc.get(configPath, field, codedValue, true);
        field.set(o, configuredObject);

        EmberCore.logInjection("Field %s has a default value of %s.", field.getName(), codedValue == null ? "null" :
                codedValue.toString());
        EmberCore.logInjection("  Configured value: %s %s", configuredObject == null ? "null" : configuredObject.toString(),
                configuredObject == codedValue ? "(using default)" : "");
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

    /**
     * Contains {@link ObjectMapper}s configured for different file formats.
     */
    private static class MultiFormatObjectMapperWrapper {
        private ObjectMapper yamlMapper, jsonMapper, xmlMapper;

        public MultiFormatObjectMapperWrapper() {
            yamlMapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
            yamlMapper.addMixIn(Vector.class, VectorMixIn.class);
            yamlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
            yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            jsonMapper = new ObjectMapper();
            jsonMapper.addMixIn(Vector.class, VectorMixIn.class);
            jsonMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
            jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            xmlMapper = new XmlMapper();
            xmlMapper.addMixIn(Vector.class, VectorMixIn.class);
            xmlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
            xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }

        public ObjectMapper getMapperFor(ConfigurationFormat configurationFormat) {
            switch (configurationFormat) {
                case YAML:
                default:
                    return yamlMapper;
                case JSON:
                    return jsonMapper;
                case XML:
                    return xmlMapper;
            }
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
