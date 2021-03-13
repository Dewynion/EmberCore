package com.github.Dewynion.embercore.config;

import com.github.Dewynion.embercore.EmberCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigInjector {

    private static final String SERIALIZE_METHOD = "toConfigurationSection";
    private static final String DESERIALIZE_METHOD = "fromConfigurationSection";

    public static void autoInjectFields(JavaPlugin plugin, Object o, boolean writeDefaults) {
        Map<String, FileConfiguration> tmpFiles = new HashMap<>();
        for (Field field : o.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(YamlSerialized.class))
                continue;
            String filename = field.getAnnotation(YamlSerialized.class).filename();
            File file = new File(plugin.getDataFolder(), filename);
            FileConfiguration fc;
            if (tmpFiles.containsKey(filename))
                fc = tmpFiles.get(filename);
            else {
                fc = YamlConfiguration.loadConfiguration(file);
                tmpFiles.put(filename, fc);
            }
            injectIntoField(field, o, fc);
            if (writeDefaults) {
                try {
                    fc.save(file);
                } catch (IOException ex) {
                    EmberCore.warn("Unable to write to file %s. Config will not be saved.",
                            file.getAbsolutePath());
                }
            }
        }
    }

    public static void injectIntoField(Field field, Object o, FileConfiguration fc) {
        if (!field.isAnnotationPresent(YamlSerialized.class))
            return;
        try {
            field.setAccessible(true);
            String path = field.getAnnotation(YamlSerialized.class).path();
            if (path.equals("")) {
                // convert the field name into yml case by splitting CamelCase at uppercase letters
                String[] pathRegex = field.getName().split("(?=\\p{Upper})");
                StringBuilder ymlPath = new StringBuilder();
                // then stitch components together with "-"
                for (String s : pathRegex)
                    ymlPath.append(s.toLowerCase()).append("-");
                // there'll be an unnecessary "-" at the end so cut it off
                path = ymlPath.substring(0, ymlPath.length() - 1);
            }

            Object fieldValue = field.get(o);
            Class<? extends Object> type = field.getType();

            if (fc.contains(path)) {
                Object configuredValue = fc.get(path, fieldValue);
                if (configuredValue instanceof MemorySection) {
                    try {
                        Method fromConfigMethod = type.getDeclaredMethod(DESERIALIZE_METHOD, ConfigurationSection.class);
                        configuredValue = fromConfigMethod.invoke(null, fc.getConfigurationSection(path));
                    } catch (NoSuchMethodException nsme) {
                        try {
                            if (Map.class.isAssignableFrom(type)) {
                                Map<String, ?> map = fromConfigurationSection(fc.getConfigurationSection(path));
                                if (field.getAnnotation(YamlSerialized.class).overwriteCollections())
                                    configuredValue = map;
                                else {
                                    for (Map.Entry<String, ?> entry : map.entrySet()) {
                                        try {
                                            ((Map) fieldValue).put(entry.getKey(), entry.getValue());
                                        } catch (ClassCastException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                    configuredValue = fieldValue;
                                }
                            }
                        } catch (ClassCastException cce) {
                            EmberCore.warn("Cast exception when attempting to inject map into field %s. Possible " +
                                    "generic mismatch?", field.getName());
                        }
                    }
                } else {
                    try {
                        // For Enums, ChatColor, etc.
                        Method valueOf = type.getMethod("valueOf", String.class);
                        configuredValue = valueOf.invoke(null, String.valueOf(configuredValue).toUpperCase());
                    } catch (NoSuchMethodException nsme) {
                    } catch (Exception e) {
                        EmberCore.warn("Unable to inject value for field %s.", field.getName());
                        e.printStackTrace();
                        return;
                    }
                }

                field.set(o, configuredValue);
            } else {
                if (Map.class.isAssignableFrom(type)) {
                    fieldValue = fromMap((Map) fieldValue);
                } else {
                    try {
                        Method toConfigMethod = type.getDeclaredMethod(SERIALIZE_METHOD);
                        fieldValue = toConfigMethod.invoke(fieldValue);
                    } catch (NoSuchMethodException nsme) {
                        try {
                            // If the method doesn't serialize to anything in particular,
                            // try for a name() method. This ensures that enums and ChatColors
                            // serialize in a human-readable form.
                            //
                            // ChatColor's toString() returns Thai characters and I could not tell you why.
                            Method nameMethod = type.getMethod("name");
                            fieldValue = nameMethod.invoke(fieldValue);
                        } catch (NoSuchMethodException nsme2) {
                        }
                    }
                }
                fc.set(path, fieldValue);
            }
        } catch (IllegalAccessException ex) {
            EmberCore.warn("Unable to inject value for field %s: illegal access.", field.getName());
        } catch (ClassCastException e) {
            EmberCore.warn("Unable to set field to its configured value: type mismatch.");
            e.printStackTrace();
        } catch (Exception e) {
            EmberCore.warn("Error encountered while injecting into field %s.", field.getName());
            e.printStackTrace();
        }
    }

    public static void injectIntoStaticField(Field field, FileConfiguration fc) {
        try {
            injectIntoField(field, null, fc);
        } catch (NullPointerException npe) {
            EmberCore.warn("Attempted static field injection but encountered a null value. Maybe this isn't a static " +
                    "field?");
        }
    }

    public static void injectIntoObject(Object o, File configFile, boolean writeDefaults) {
        if (!configFile.exists()) {
            EmberCore.log(Level.WARNING, String.format("File %s doesn't exist. Injection aborted.",
                    configFile.getAbsolutePath()));
            return;
        }
        FileConfiguration fc = YamlConfiguration.loadConfiguration(configFile);
        injectIntoObject(o, fc);
        if (writeDefaults) {
            try {
                fc.save(configFile);
            } catch (IOException ex) {
                EmberCore.log(Level.WARNING, String.format("Unable to write to file %s. Config will not be saved.",
                        configFile.getAbsolutePath()));
            }
        }
    }

    public static void injectIntoObject(Object o, FileConfiguration fc) {
        for (Field field : o.getClass().getDeclaredFields()) {
            injectIntoField(field, o, fc);
        }
    }

    private static ConfigurationSection fromMap(Map<?, ?> map) {
        ConfigurationSection config = new MemoryConfiguration();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String s = entry.getKey().toString();
            config.set(s, entry.getValue());
        }
        return config;
    }

    private static <T> Map<String, T> fromConfigurationSection(ConfigurationSection section) {
        Map<String, T> map = new HashMap<>();
        for (String s : section.getKeys(true)) {
            try {
                map.put(s, (T) section.get(s));
            } catch (ClassCastException cce) {
                EmberCore.warn("Couldn't cast configured value of type %s to provided type.",
                        section.get(s).getClass().getSimpleName());
            }
        }
        return map;
    }
}
