package com.github.Dewynion.embercore.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.reflection.ConfigInjector;
import com.github.Dewynion.embercore.reflection.ReflectionUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public abstract class PluginConfiguration {
    protected final File configurationFile;
    protected final JavaPlugin plugin;
    protected final ConfigurationFormat configurationFormat;
    protected JsonNode configRoot;

    public PluginConfiguration(File configurationFile, JavaPlugin plugin, ConfigurationFormat configurationFormat) {
        this.configurationFile = configurationFile;
        this.plugin = plugin;
        this.configurationFormat = configurationFormat;
        try {
            configRoot = ConfigInjector.getObjectMapper(plugin, configurationFormat).readTree(configurationFile);
            if (configRoot == null || configRoot instanceof NullNode || configRoot instanceof MissingNode)
                configRoot = JsonNodeFactory.instance.objectNode();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveConfiguration() {
        try {
            ConfigInjector.getObjectMapper(plugin, configurationFormat).writeValue(configurationFile, configRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final <T> T get(String path, Class<T> tClass, Object defaultValue) {
        return get(path, tClass, defaultValue, false);
    }

    public final <T> T get(String path, Field field, Object defaultValue, boolean setIfNotFound) {
        TypeFactory typeFactory = TypeFactory.defaultInstance();
        JavaType type = null;
        EmberCore.logSerialization("Reading field: %s", field.getName());
        if (Map.class.isAssignableFrom(field.getType())) {
            Type[] mapTypes = ReflectionUtil.getMapTypes(field);
            assert mapTypes != null;
            EmberCore.logSerialization("  Retrieving map of %s, %s", mapTypes[0].getTypeName(), mapTypes[1].getTypeName());
            JavaType keyType = typeFactory.constructType(mapTypes[0]);
            JavaType valueType = typeFactory.constructType(mapTypes[1]);
            EmberCore.logSerialization("  Jackson type factory produced key and value pairs of %s, %s",
                    keyType.getTypeName(), valueType.getTypeName());
            type = typeFactory.constructMapType((Class<? extends Map>) field.getType(), keyType, valueType);
        } else if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            Type collectionType = ReflectionUtil.getGenericType(field);
            assert collectionType != null;
            EmberCore.logSerialization("  Retrieving collection of %s", collectionType.getTypeName());
            JavaType colType = typeFactory.constructType(collectionType);
            EmberCore.logSerialization("  Jackson type factory produced collection type %s", colType.getTypeName());
            type = typeFactory.constructCollectionType((Class<? extends Collection>) field.getType(), colType);
        } else {
            type = typeFactory.constructType(field.getType());
        }

        EmberCore.logSerialization("  Field type: %s", type.getTypeName());

        return get(path, type, defaultValue, setIfNotFound);
    }

    public final <T> T get(String path, Class<T> tClass, Object defaultValue, boolean setIfNotFound) {
        TypeFactory typeFactory = TypeFactory.defaultInstance();
        return get(path, typeFactory.constructType(tClass), defaultValue, setIfNotFound);
    }

    public final <T> T get(String path, JavaType type, Object defaultValue, boolean setIfNotFound) {
        try {
            JsonNode node = traverseToParent(path);
            String[] tmp = path.split("\\.");
            String fieldName = path;
            if (tmp.length > 1)
                fieldName = tmp[tmp.length - 1];
            if (node.path(fieldName) == null || node.path(fieldName) instanceof MissingNode)
                // tl;dr - if the path doesn't exist, create it
                throw new NullPointerException("This exception is functionally a break statement and will be caught immediately.");

            ObjectMapper mapper = ConfigInjector.getObjectMapper(plugin, configurationFormat);
            return mapper.convertValue(node.path(fieldName), type);
        } catch (NullPointerException | ClassCastException ex) {
            if (setIfNotFound) {
                EmberCore.logSerialization("  Setting config path %s in file %s to its default value.",
                        path, configurationFile.getAbsolutePath());
                set(path, defaultValue);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return (T) defaultValue;
    }

    public final void set(String path, Object object) {
        try {
            String[] tmp = path.split("\\.");
            String fieldName = path;
            if (tmp.length > 1)
                fieldName = tmp[tmp.length - 1];
            JsonNode node = traverseToParent(path, true);
            ((ObjectNode) node).putPOJO(fieldName, object);
        } catch (NullPointerException ex) {
            EmberCore.warn(path);
            ex.printStackTrace();
        }
    }

    public final JavaPlugin getPlugin() {
        return plugin;
    }

    public final File getConfigurationFile() {
        return configurationFile;
    }

    public final ConfigurationFormat getConfigurationFormat() {
        return configurationFormat;
    }

    public static PluginConfiguration create(File configurationFile, JavaPlugin plugin) throws NullPointerException {
        if (!configurationFile.exists()) {
            EmberCore.warn("Configuration file %s doesn't exist and will be created.", configurationFile.getAbsolutePath());
            try {
                String dir = configurationFile.getParent();
                File directory = new File(dir);
                if (!directory.mkdirs())
                    EmberCore.info("Directory %s exists, skipping creation.", dir);
                else
                    EmberCore.info("Directory %s created.", dir);
                if (!configurationFile.createNewFile())
                    EmberCore.info("Couldn't create file %s.", configurationFile.getAbsolutePath());
                else
                    EmberCore.info("Successfully created file: %s", configurationFile.getAbsolutePath());
            } catch (IOException ex) {
                EmberCore.warn("I/O exception: %s", ex.getMessage());
                EmberCore.warn("No configuration was created and the file was not saved.");
                return null;
            }
        }

        String filePath = configurationFile.getAbsolutePath();
        String fileExtension = filePath.substring(filePath.lastIndexOf('.'));

        ConfigurationFormat format = ConfigurationFormat.TEXT;
        for (ConfigurationFormat fmt : ConfigurationFormat.values()) {
            if (fmt.fileExtension.equalsIgnoreCase(fileExtension)) {
                format = fmt;
                break;
            }
        }

        if (format.configClass == null) {
            createError(plugin, format, filePath, "Provided file format is not supported.");
            return null;
        }

        try {
            Constructor<? extends PluginConfiguration> c = format.configClass.getDeclaredConstructor(File.class, JavaPlugin.class);
            c.setAccessible(true);
            return c.newInstance(configurationFile, plugin);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            createError(plugin, format, filePath, "No constructor accepting arguments of type { File, JavaPlugin } was found.");
            e.printStackTrace();
            return null;
        }
    }

    protected void ioError() {
        EmberCore.warn(plugin, "I/O exception: no read/write access for file %s.", configurationFile.getAbsolutePath());
    }

    private static void createError(JavaPlugin plugin, ConfigurationFormat format, String filePath, String errorMessage) {
        EmberCore.warn(plugin, "Unable to create file configuration class %s for file %s: %s.",
                format.configClass.getName(), filePath, errorMessage);
    }

    private JsonNode traverseToParent(String path) throws NullPointerException {
        return traverseToParent(path, false);
    }

    private JsonNode traverseToParent(String path, boolean createNewNodes) throws NullPointerException {
        String[] splitPath = path.split("\\.");
        JsonNode target = configRoot;
        if (splitPath.length > 1) {
            splitPath = Arrays.copyOf(splitPath, splitPath.length - 1);
            int i = 0;
            while (target != null && i < splitPath.length) {
                String currentPath = splitPath[i++];
                JsonNode child = target.get(currentPath);
                if (createNewNodes && !(child instanceof ObjectNode)) {
                    //((ObjectNode) target).remove(currentPath);
                    child = ((ObjectNode) target).putObject(currentPath);
                }
                target = child;
            }
        }
        return target;
    }
}
