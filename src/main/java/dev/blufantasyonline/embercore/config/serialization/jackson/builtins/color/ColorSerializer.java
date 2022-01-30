package dev.blufantasyonline.embercore.config.serialization.jackson.builtins.color;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import dev.blufantasyonline.embercore.reflection.annotations.OnEnable;

import java.awt.*;
import java.io.IOException;

@OnEnable
public class ColorSerializer extends StdSerializer<Color> {
    public ColorSerializer() {
        super(Color.class);
    }

    @Override
    public void serialize(Color color, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeNumberField("red", color.getRed());
        jsonGenerator.writeNumberField("green", color.getGreen());
        jsonGenerator.writeNumberField("blue", color.getBlue());
        jsonGenerator.writeNumberField("alpha", color.getAlpha());

        jsonGenerator.writeEndObject();
    }
}
