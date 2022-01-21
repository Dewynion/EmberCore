package com.github.Dewynion.embercore.config.serialization.jackson.builtins;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.Dewynion.embercore.reflection.annotations.OnEnable;

import java.io.IOException;
import java.util.logging.Level;

@OnEnable
public class LevelSerializer extends StdSerializer<Level> {
    public LevelSerializer() {
        super(Level.class);
    }

    @Override
    public void serialize(Level level, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(level.getName());
    }
}
