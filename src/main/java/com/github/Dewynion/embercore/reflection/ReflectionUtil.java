package com.github.Dewynion.embercore.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public final class ReflectionUtil {

    public static Type getGenericSuperclassType(Object object) {
        return getGenericSuperclassType(object, 0);
    }

    public static Type getGenericSuperclassType(Object object, int index) {
        return getGenericSuperclassType(object.getClass(), index);
    }

    public static Type getGenericSuperclassType(Class<?> clz) {
        return getGenericSuperclassType(clz, 0);
    }

    public static Type getGenericSuperclassType(Class<?> clz, int index) {
        return ((ParameterizedType) clz.getGenericSuperclass()).getActualTypeArguments()[index];
    }

    public static Type[] getMapTypes(Map<?, ?> map) {
        return ((ParameterizedType) map.getClass().getGenericSuperclass()).getActualTypeArguments();
    }

    public static Type getCollectionType(Collection<?> collection) {
        return ((ParameterizedType) collection.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
