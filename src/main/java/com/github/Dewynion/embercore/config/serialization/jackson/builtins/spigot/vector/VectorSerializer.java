package com.github.Dewynion.embercore.config.serialization.jackson.builtins.spigot.vector;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.Dewynion.embercore.reflection.annotations.OnEnable;
import org.bukkit.util.Vector;

import java.io.IOException;

@OnEnable
public class VectorSerializer extends StdSerializer<Vector> {
    public VectorSerializer() {
        super(Vector.class);
    }

    @Override
    public void serialize(Vector vector, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeNumberField("x", vector.getX());
        jsonGenerator.writeNumberField("y", vector.getY());
        jsonGenerator.writeNumberField("z", vector.getZ());

        jsonGenerator.writeEndObject();
    }
}

