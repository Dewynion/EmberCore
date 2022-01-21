package dev.blufantasyonline.embercore.util;

import java.util.Map;

public final class MapUtil {
    public static <T, U> U getOrCreate(Map<T, U> source, T key, U defaultValue) {
        if (!source.containsKey(key))
            source.put(key, defaultValue);
        return source.get(key);
    }
}
