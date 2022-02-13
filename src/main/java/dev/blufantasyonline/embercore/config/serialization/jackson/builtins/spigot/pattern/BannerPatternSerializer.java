package dev.blufantasyonline.embercore.config.serialization.jackson.builtins.spigot.pattern;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import dev.blufantasyonline.embercore.reflection.annotations.Preload;
import org.bukkit.block.banner.Pattern;

import java.io.IOException;

@Preload
public class BannerPatternSerializer extends StdSerializer<Pattern> {
    public BannerPatternSerializer() {
        super(Pattern.class);
    }

    @Override
    public void serialize(Pattern pattern, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeObjectField("color", pattern.getColor());
        jsonGenerator.writeObjectField("pattern", pattern.getPattern());

        jsonGenerator.writeEndObject();
    }
}
