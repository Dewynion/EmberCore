package com.github.Dewynion.embercore.util;

import com.github.Dewynion.embercore.EmberCore;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

public class KeyWithDefault<T> {
    private Class<T> persistentClass;
    private String key;
    private T defaultValue;
    private Map<String, Object> parameters;

    public KeyWithDefault(String key, T defaultValue, Object... params) {
        persistentClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.key = key;
        this.defaultValue = defaultValue;

        parameters = new HashMap<>();
        for (int i = 0; i < params.length - 1; i += 2) {
            try {
                parameters.put(params[i].toString(), params[i + 1]);
            } catch (Exception ex) {
                EmberCore.warn("Error reading parameter for KeyWithDefault, skipping.");
            }
        }
    }

    public Class<T> getPersistentClass() {
        return persistentClass;
    }

    public String getKey() {
        return key;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}