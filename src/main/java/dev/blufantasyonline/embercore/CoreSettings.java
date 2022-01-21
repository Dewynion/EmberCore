package dev.blufantasyonline.embercore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.blufantasyonline.embercore.config.serialization.jackson.builtins.LevelDeserializer;
import dev.blufantasyonline.embercore.reflection.annotations.Preload;
import dev.blufantasyonline.embercore.config.serialization.jackson.builtins.LevelSerializer;

import java.util.logging.Level;

@Preload
public final class CoreSettings {
    public static CoreLogSettings coreLogSettings = new CoreLogSettings();

    public static final class CoreLogSettings {
        @JsonProperty
        @JsonSerialize(using = LevelSerializer.class)
        @JsonDeserialize(using = LevelDeserializer.class)
        public Level logLevel = Level.INFO;
        @JsonProperty
        public boolean listeners = true;
        @JsonProperty
        public boolean injection = false;
        @JsonProperty
        public boolean serialization = false;
    }
}
