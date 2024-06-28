package org.jpos.log.render;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class ThrowableSerializer extends JsonSerializer<Throwable> {
    @Override
    public void serialize(Throwable t, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("message", t.getMessage());
        gen.writeStringField("stacktrace", toString(t));
        gen.writeEndObject();
    }

    private String toString (Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        t.printStackTrace(ps);
        return baos.toString();
    }
}
