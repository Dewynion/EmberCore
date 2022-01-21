package dev.blufantasyonline.embercore.config.serialization.jackson.builtins;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.reflection.annotations.OnEnable;

import java.io.IOException;
import java.util.logging.Level;

@OnEnable
public class LevelDeserializer extends StdDeserializer<Level> {
    private static final Level DEFAULT_LEVEL = Level.INFO;

    public LevelDeserializer() {
        super(Level.class);
    }

    @Override
    public Level deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode root = codec.readTree(jsonParser);
        String name = root.asText();

        try {
            return Level.parse(name);
        } catch (IllegalArgumentException ex) {
            EmberCore.warn("Couldn't parse a log level named '%s'. Using '%s'.",
                    name, DEFAULT_LEVEL.getName());
            return DEFAULT_LEVEL;
        }
    }
}
