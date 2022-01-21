package dev.blufantasyonline.embercore.config.serialization.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.reflection.ReflectionUtil;
import dev.blufantasyonline.embercore.util.ErrorUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class SimpleModuleBuilder {
    public static SimpleModule keyDeserializerModule(Class<? extends TypedKeyDeserializer> clz) {
        String typeName = getTypeName(clz);
        SimpleModule module = new SimpleModule(String.format("%s_key-deserializer", typeName));
        try {
            Type keyType = ReflectionUtil.getGenericSuperclassType(clz);
            if (keyType instanceof Class)
                module.addKeyDeserializer((Class<?>) keyType,
                        (KeyDeserializer) retrieveConstructor(clz).newInstance());
        } catch (Exception ex) {
            // TODO: more specific error
            ex.printStackTrace();
        }
        EmberCore.info("Constructed module %s.", module.getModuleName());
        return module;
    }

    public static SimpleModule serializerModule(Class<? extends JsonSerializer> clz) {
        String typeName = getTypeName(clz);
        SimpleModule module = new SimpleModule(String.format("%s_serializer", typeName));
        module.setSerializerModifier(constructSerializerModifier(clz));
        EmberCore.info("Constructed module %s.", module.getModuleName());
        return module;
    }

    public static SimpleModule deserializerModule(Class<? extends JsonDeserializer> clz) {
        String typeName = getTypeName(clz);
        SimpleModule module = new SimpleModule(String.format("%s_deserializer", typeName));
        module.setDeserializerModifier(constructDeserializerModifier(clz));
        EmberCore.info("Constructed module %s.", module.getModuleName());
        return module;
    }

    private static BeanSerializerModifier constructSerializerModifier(Class<? extends JsonSerializer> clz) {
        Constructor<?> constructor = retrieveConstructor(clz);
        if (constructor == null) {
            ErrorUtil.warn("Couldn't construct BeanSerializerModifier from %s due to lack of a valid constructor.",
                    clz.getName());
            return null;
        }

        constructor.setAccessible(true);
        return new BeanSerializerModifier() {
            private JsonSerializer<?> serializerInstance;
            private boolean attempted = false;

            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config,
                                                      BeanDescription beanDesc,
                                                      JsonSerializer<?> serializer) {
                if (serializerInstance == null && !attempted) {
                    try {
                        serializerInstance = (JsonSerializer<?>) constructor.newInstance(serializer);
                    } catch (Exception ex) {
                        try {
                            serializerInstance = (JsonSerializer<?>) constructor.newInstance();
                        } catch (Exception e2) {
                            ErrorUtil.warn(e2.getMessage());
                            EmberCore.warn("Further errors from this context will be suppressed until EmberCore is reloaded.");
                            attempted = true;
                        }
                    }
                }

                if (beanDesc.getBeanClass().equals(ReflectionUtil.getGenericSuperclassType(clz)))
                    try {
                        return serializerInstance;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                return serializer;
            }
        };
    }

    private static BeanDeserializerModifier constructDeserializerModifier(Class<? extends JsonDeserializer> clz) {
        Constructor<?> constructor = retrieveConstructor(clz);
        if (constructor == null) {
            ErrorUtil.warn("Couldn't construct BeanSerializerModifier from %s due to lack of a valid constructor.",
                    clz.getName());
            return null;
        }

        constructor.setAccessible(true);
        return new BeanDeserializerModifier() {
            private JsonDeserializer<?> deserializerInstance;
            private boolean attempted = false;

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                          BeanDescription beanDesc,
                                                          JsonDeserializer<?> deserializer) {
                if (deserializerInstance == null && !attempted) {
                    try {
                        deserializerInstance = (JsonDeserializer<?>) constructor.newInstance(deserializer);
                    } catch (Exception ex) {
                        try {
                            deserializerInstance = (JsonDeserializer<?>) constructor.newInstance();
                        } catch (Exception e2) {
                            ErrorUtil.warn(e2.getMessage());
                            EmberCore.warn("Further errors from this context will be suppressed until EmberCore is reloaded.");
                            attempted = true;
                        }
                    }
                }

                if (beanDesc.getBeanClass().equals(ReflectionUtil.getGenericSuperclassType(clz)))
                    try {
                        return deserializerInstance;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                return deserializer;
            }
        };
    }

    private static String getTypeName(Class<?> clz) {
        return ParameterizedType.class.isAssignableFrom(clz) ?
                ReflectionUtil.getGenericSuperclassType(clz).getTypeName() :
                clz.getTypeName();
    }

    private static Constructor<?> retrieveConstructor(Class<?> clz) throws NullPointerException {
        try {
            return clz.getDeclaredConstructor(JsonSerializer.class);
        } catch (NoSuchMethodException ex1) {
            try {
                return clz.getDeclaredConstructor(JsonDeserializer.class);
            } catch (NoSuchMethodException ex2) {
                try {
                    return clz.getDeclaredConstructor();
                } catch (NoSuchMethodException ex3) {
                    ErrorUtil.severe("Couldn't find a constructor for %s that accepts either JsonSerializer or JsonDeserializer as a parameter, and no default constructor exists.");
                }
            }
        }

        return null;
    }
}
