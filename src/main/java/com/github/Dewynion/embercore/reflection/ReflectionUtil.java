package com.github.Dewynion.embercore.reflection;

import com.github.Dewynion.embercore.EmberCore;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    public static Type[] getMapTypes(Field mapField) {
        try {
            ParameterizedType pt = (ParameterizedType) mapField.getGenericType();
            return pt.getActualTypeArguments();
        } catch (Exception ex) {
            EmberCore.warn("ReflectionUtil::Attempted to retrieve map types from a non-map field. Returning null.");
            return null;
        }
    }

    public static Type getGenericType(Field genericField) {
        try {
            ParameterizedType pt = (ParameterizedType) genericField.getGenericType();
            return pt.getActualTypeArguments()[0];
        } catch (Exception ex) {
            EmberCore.warn("ReflectionUtil::Attempted to retrieve generic type from a non-generic field. Returning null.");
            return null;
        }
    }

    public static List<Class<?>> scanClassPath(File jar, String packagee, boolean external) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        if (jar == null)
            return classes;
        String ee = packagee.replaceAll("\\.", "/");
        ZipFile zip = new ZipFile(jar);
        URLClassLoader child = new URLClassLoader(new URL[] {jar.toURL()}, EmberCore.class.getClassLoader());
        for (Enumeration<? extends ZipEntry> entries = zip
                .entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!name.startsWith(ee)) // directory
                continue;
            name = name.substring(ee.length() + 1);
            if (name.endsWith(".class")) {
                String className = packagee + "." + name.substring(0, name.length() - 6).replaceAll("/", ".");
                try {
                    if (external)
                        classes.add(Class.forName(className, true, child));
                    else classes.add(Class.forName(className));
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
        return classes;
    }
}
