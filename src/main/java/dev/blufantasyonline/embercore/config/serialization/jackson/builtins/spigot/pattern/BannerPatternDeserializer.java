package dev.blufantasyonline.embercore.config.serialization.jackson.builtins.spigot.pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import dev.blufantasyonline.embercore.reflection.annotations.Preload;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.io.IOException;

@Preload
public class BannerPatternDeserializer extends StdDeserializer<Pattern> {
    public BannerPatternDeserializer() {
        super(Pattern.class);
    }

    @Override
    public Pattern deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode root = codec.readTree(jsonParser);

        DyeColor dyeColor = codec.readValue(root.get("color").traverse(), DyeColor.class);
        PatternType patternType = codec.readValue(root.get("pattern").traverse(), PatternType.class);

        return new Pattern(dyeColor, patternType);
    }
}
