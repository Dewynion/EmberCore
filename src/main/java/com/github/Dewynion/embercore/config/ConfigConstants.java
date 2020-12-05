package com.github.Dewynion.embercore.config;

public enum ConfigConstants {
    MAX_PROJECTILES("max-projectiles", 1000);

    String path;
    Object defaultValue;

    ConfigConstants(String s, Object o) {
        path = s;
        defaultValue = o;
    }

    public String getPath() {
        return path;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
