package dev.blufantasyonline.embercore.config.serialization.jackson.builtins.spigot.chatcolor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import dev.blufantasyonline.embercore.EmberCore;
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
            String name = root.get("name").asText();
            try {
                return ChatColor.of(name);
            } catch (IllegalArgumentException ex) {
                try {
                    EmberCore.info("Couldn't parse chat color with name '%s', attempting creation from RGBA.", name);
                    Color color = ctx.readValue(root.get("color").traverse(), Color.class);
                    return ChatColor.of(color);
                } catch (IllegalArgumentException ex2) {
                    EmberCore.warn("RGBA color couldn't be read. Using default.");
                    return ChatColor.GRAY;
                }
            }
        } catch (NullPointerException ex) {
            EmberCore.warn("ChatColor not found. Using default.");
            return ChatColor.GRAY;
        }
    }
}
