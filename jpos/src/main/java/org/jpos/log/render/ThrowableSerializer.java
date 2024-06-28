package org.jpos.log.render;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ThrowableSerializer extends JsonSerializer<Throwable> {
    @Override
    public void serialize(Throwable value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("message", value.getMessage());
        gen.writeArrayFieldStart("stackTrace");
        StackTraceElement[] stackTrace = value.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            gen.writeString(stackTraceElement.toString());
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }
}
