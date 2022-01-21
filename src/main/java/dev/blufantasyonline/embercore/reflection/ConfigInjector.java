package dev.blufantasyonline.embercore.reflection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        EmberCore.logInjection("Injecting configured values into object of type %s.", o.getClass().getName());
        EmberCore.logInjection("Filename for this object: %s", typeDefaultConfigFilename);

        for (Field field : objectClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if (field.isAnnotationPresent(ExcludeFromSerialization.class))
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

                // Optional.ofNullable was returning null here, so back to the classic method we go
                if (!configs.containsKey(configFileName)) {
                    EmberCore.logInjection("Reading configuration file: %s", configFileName);
                    configs.put(configFileName, PluginConfiguration.create(configFile, plugin));
                }
                PluginConfiguration configuration = configs.get(configFileName);

                if (configuration == null) {
                    EmberCore.warn(plugin, "Unable to create a plugin configuration object from file %s. Field %s will retain its default value.",
                            configFileName, field.getName());
                    continue;
                }

                injectIntoField(field, o, configuration);

                configuration.saveConfiguration();
            } catch (IllegalAccessException ex) {
                EmberCore.warn("Unable to access field %s in class %s while injecting config.",
                        field.getName(), objectClass.getName());
            }
        }
    }

    public static <T> void injectIntoField(Field field, Object o, PluginConfiguration pc) throws IllegalAccessException {
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
            yamlMapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);

            jsonMapper = new ObjectMapper();
            jsonMapper.addMixIn(Vector.class, VectorMixIn.class);
            jsonMapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);

            xmlMapper = new XmlMapper();
            xmlMapper.addMixIn(Vector.class, VectorMixIn.class);
            xmlMapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
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
