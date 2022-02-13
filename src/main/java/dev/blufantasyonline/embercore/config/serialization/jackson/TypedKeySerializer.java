package dev.blufantasyonline.embercore.config.serialization.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public abstract class TypedKeySerializer<T> extends StdSerializer<T> {
    protected TypedKeySerializer(Class<T> tClass) {
        super(tClass);
    }

    public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
        g.writeFieldName(stringValue(value));
    }

    protected abstract String stringValue(Object value);
}
