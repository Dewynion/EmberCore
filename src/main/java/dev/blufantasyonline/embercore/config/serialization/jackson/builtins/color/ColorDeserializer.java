package dev.blufantasyonline.embercore.config.serialization.jackson.builtins.color;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import dev.blufantasyonline.embercore.reflection.annotations.OnEnable;

import java.awt.*;
import java.io.IOException;

@OnEnable
public class ColorDeserializer extends StdDeserializer<Color> {
    public ColorDeserializer() {
        super(Color.class);
    }

    @Override
    public Color deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode root = codec.readTree(jsonParser);
        int red, green, blue, alpha;
        red = green = blue = 0;
        alpha = 255;
        try {
            red = root.get("red").asInt();
            green = root.get("green").asInt();
            blue = root.get("blue").asInt();
            alpha = root.get("alpha").asInt();
        } catch (Exception ignored) { }
        return new Color(red, green, blue, alpha);
    }
}

