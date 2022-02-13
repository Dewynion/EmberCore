package dev.blufantasyonline.embercore.config.serialization.jackson.builtins.spigot.chatcolor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import dev.blufantasyonline.embercore.reflection.annotations.Preload;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.io.IOException;

@Preload
public class ChatColorSerializer extends StdSerializer<ChatColor> {
    public ChatColorSerializer() {
        super(ChatColor.class);
    }

    @Override
    public void serialize(ChatColor chatColor, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("name", chatColor.getName());
        jsonGenerator.writeObjectFieldStart("color");
        Color color = chatColor.getColor();
        jsonGenerator.writeNumberField("red", color.getRed());
        jsonGenerator.writeNumberField("green", color.getGreen());
        jsonGenerator.writeNumberField("blue", color.getBlue());
        jsonGenerator.writeNumberField("alpha", color.getAlpha());
        jsonGenerator.writeEndObject();

        jsonGenerator.writeEndObject();
    }
}
