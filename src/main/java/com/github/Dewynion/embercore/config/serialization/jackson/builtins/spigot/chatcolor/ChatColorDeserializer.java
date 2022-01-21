package com.github.Dewynion.embercore.config.serialization.jackson.builtins.spigot.chatcolor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.github.Dewynion.embercore.reflection.annotations.OnEnable;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.io.IOException;

@OnEnable
public class ChatColorDeserializer extends StdDeserializer<ChatColor> {
    protected ChatColorDeserializer() {
        super(ChatColor.class);
    }

    @Override
    public ChatColor deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode root = codec.readTree(jsonParser);

        try {
            return ChatColor.of(root.get("name").asText());
        } catch (IllegalArgumentException ex) {
            JsonNode color = root.get("color");
            int red = root.get("red").asInt();
            int green = root.get("green").asInt();
            int blue = root.get("blue").asInt();
            int alpha = root.get("alpha").asInt();
            return ChatColor.of(new Color(red, green, blue, alpha));
        }
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return super.deserializeWithType(p, ctxt, typeDeserializer);
    }
}
