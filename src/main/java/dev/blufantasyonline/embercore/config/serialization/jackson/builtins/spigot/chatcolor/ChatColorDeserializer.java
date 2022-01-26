package dev.blufantasyonline.embercore.config.serialization.jackson.builtins.spigot.chatcolor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import dev.blufantasyonline.embercore.reflection.annotations.OnEnable;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.io.IOException;

@OnEnable
public class ChatColorDeserializer extends StdDeserializer<ChatColor> {
    protected ChatColorDeserializer() {
        super(ChatColor.class);
    }

    @Override
    public ChatColor deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode root = codec.readTree(jsonParser);

        try {
            return ChatColor.of(root.get("name").asText());
        } catch (IllegalArgumentException ex) {
            Color color = ctx.readValue(root.get("color").traverse(), Color.class);
            /*
            JsonNode color = root.get("color");
            int red = color.get("red").asInt();
            int green = color.get("green").asInt();
            int blue = color.get("blue").asInt();
            int alpha = color.get("alpha").asInt();
            */
            return ChatColor.of(color);
        }
    }
}
