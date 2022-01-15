package com.github.Dewynion.embercore.config.serialization.builtins;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.reflection.annotations.OnEnable;
import org.bukkit.util.Vector;

import java.io.IOException;

@OnEnable
public class VectorDeserializer extends StdDeserializer<Vector> {
    protected VectorDeserializer(JsonDeserializer<Object> deserializer) {
        super(Vector.class);
    }

    @Override
    public Vector deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode root = codec.readTree(jsonParser);

        double x = root.get("x").asDouble();
        double y = root.get("y").asDouble();
        double z = root.get("z").asDouble();

        return new Vector(x, y, z);
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return super.deserializeWithType(p, ctxt, typeDeserializer);
    }
}
