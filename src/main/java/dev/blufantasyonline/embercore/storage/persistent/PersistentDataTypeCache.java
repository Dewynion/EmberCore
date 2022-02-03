package dev.blufantasyonline.embercore.storage.persistent;

import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.reflection.ReflectionUtil;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.util.HashMap;

public final class PersistentDataTypeCache {
    private static HashMap<Class, PersistentDataType> dataCache;

    static {
        dataCache = new HashMap<>();
        dataCache.put(Byte.class, PersistentDataType.BYTE);
        dataCache.put(Short.class, PersistentDataType.SHORT);
        dataCache.put(Integer.class, PersistentDataType.INTEGER);
        dataCache.put(Long.class, PersistentDataType.LONG);
        dataCache.put(Float.class, PersistentDataType.FLOAT);
        for (Field f : PersistentDataType.class.getDeclaredFields()) {
            if (!f.getType().isAssignableFrom(PersistentDataType.class))
                continue;
            try {
                Class<?> dataClass = (Class<?>) ReflectionUtil.getMapTypes(f)[1];
                PersistentDataType<?, ?> pdt = (PersistentDataType<?, ?>) f.get(null);
                dataCache.put(dataClass, pdt);
                EmberCore.warn("Cached mapping of type %s to PersistentDataType.%s",
                        dataClass.getName(), f.getName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
