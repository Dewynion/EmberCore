package com.github.Dewynion.embercore.reflection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.config.ConfigurationFormat;
import com.github.Dewynion.embercore.config.PluginConfiguration;
import com.github.Dewynion.embercore.config.serialization.ExcludeFromSerialization;
import com.github.Dewynion.embercore.config.serialization.SerializationInfo;
import com.sun.org.apache.xpath.internal.operations.Mult;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.HashMap;
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
                .map(SerializationInfo::filename).orElse(DEFAULT_CONFIG_FILE);

        EmberCore.info("Injecting configured values into object of type %s.", o.getClass().getName());

        for (Field field : objectClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                if (field.isAnnotationPresent(ExcludeFromSerialization.class) || (field.getModifiers() & Modifier.FINAL) != 0)
                    continue;
                // get the config file
                File configFile = new File(dataFolder,
                        Optional.ofNullable(field.getAnnotation(SerializationInfo.class))
                                .map(SerializationInfo::filename).orElse(typeDefaultConfigFilename));
                String configFileName = configFile.getAbsolutePath();

                // Optional.ofNullable was returning null here, so back to the classic method we go
                if (!configs.containsKey(configFileName)) {
                    EmberCore.info("Reading configuration file: %s", configFileName);
                    configs.put(configFileName, PluginConfiguration.create(configFile, plugin));
                }
                PluginConfiguration configuration = configs.get(configFileName);

                if (configuration == null) {
                    EmberCore.warn(plugin, "Unable to create a plugin configuration object from file %s. Field %s will retain its default value.",
                            configFileName, field.getName());
                    continue;
                }

                injectIntoField(plugin, field, o, configuration);

                configuration.saveConfiguration();
            } catch (IllegalAccessException ex) {
                EmberCore.warn("Unable to access field %s in class %s while injecting config.",
                        field.getName(), objectClass.getName());
            }
        }
    }

    public static void injectIntoField(JavaPlugin plugin, Field field, Object o, PluginConfiguration pc) throws IllegalAccessException {
        // path to follow through config - use the path in SerializationInfo or just start from the root element
        String configPath = Optional.ofNullable(field.getAnnotation(SerializationInfo.class))
                .map(SerializationInfo::path)
                .orElse("").toLowerCase();

        // default for SerializationInfo is "", so if it's unset or the annotation just isn't present,
        // configPath will be an empty string
        // this is a lot easier to comprehend than trying to chain together a bunch of stream statements
        // to achieve the same result
        if (configPath.equalsIgnoreCase(""))
            configPath = String.join(SNAKE_CASE_JOINER, field.getName().split(REGEX_CAMEL_TO_SNAKE)).toLowerCase();
        EmberCore.info("Injecting configured value into field %s (config path %s)...", field.getName(), configPath);

        Object codedValue = field.get(o);
        Object configuredObject = pc.get(configPath, field.getType(), codedValue, true);
        field.set(o, configuredObject);

        boolean overwriteCollections = Optional.ofNullable(field.getAnnotation(SerializationInfo.class))
                .map(SerializationInfo::overwriteCollections)
                .orElse(false);

        EmberCore.info("Field %s has a default value of %s.", field.getName(), codedValue == null ? "null" :
                codedValue.toString());
        EmberCore.info("  Configured value: %s %s", configuredObject == null ? "null" : configuredObject.toString(),
                configuredObject == codedValue ? "(using default)" : "");
        EmberCore.info("  Final value: %s", field.get(o));
    }

    public static void registerModule(JavaPlugin plugin, SimpleModule module) {
        // laughed way too hard at this, i could have given it a better var name but like...this one's funny to me
        if (!pluginObjectMappers.containsKey(plugin))
            pluginObjectMappers.put(plugin, new MultiFormatObjectMapperWrapper());
        MultiFormatObjectMapperWrapper mfomw = pluginObjectMappers.get(plugin);
        mfomw.jsonMapper.registerModule(module);
        mfomw.yamlMapper.registerModule(module);
    }

    /**
     * Contains {@link ObjectMapper}s configured for different file formats.
     */
    private static class MultiFormatObjectMapperWrapper {
        private ObjectMapper yamlMapper, jsonMapper;

        public MultiFormatObjectMapperWrapper() {
            yamlMapper = new ObjectMapper(new YAMLFactory());
            yamlMapper.addMixIn(Vector.class, VectorMixIn.class);
            jsonMapper = new ObjectMapper();
            jsonMapper.addMixIn(Vector.class, VectorMixIn.class);
        }

        public ObjectMapper getMapperFor(ConfigurationFormat configurationFormat) {
            switch (configurationFormat) {
                case YAML:
                default:
                    return yamlMapper;
                case JSON:
                    return jsonMapper;
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
