package dev.blufantasyonline.embercore.config.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializationInfo {
    /**
     * Specifies the file to read from, starting from the plugin's data folder.
     * If applied to a type, sets this as the default filename for all serialized fields.
     */
    String filename() default "";

    /**
     * Specifies the path through the config tree taken to reach the value for a field.
     * When applied to a type, this value will be prepended to all paths in the file.
     */
    String path() default "";

    /**
     * If this is true, any Collection or Map object will be replaced entirely from config.
     * If this is false, values in config will be appended to the existing values.
     *
     * Means nothing when applied to a class.
     */
    boolean overwriteCollections() default true;
}
