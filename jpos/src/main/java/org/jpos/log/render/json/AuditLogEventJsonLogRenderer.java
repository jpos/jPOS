package org.jpos.log.render.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jpos.log.AuditLogEvent;
import org.jpos.log.LogRenderer;

import java.io.PrintStream;

public final class AuditLogEventJsonLogRenderer implements LogRenderer<AuditLogEvent> {
    private final ObjectMapper mapper = new ObjectMapper();

    public AuditLogEventJsonLogRenderer () {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void render(AuditLogEvent evt, PrintStream ps, String indent) {
        try {
            ps.print (mapper.writeValueAsString(evt));
        } catch (JsonProcessingException e) {
            ps.print (kv("exception", e.toString()));
        }
    }
    public Class<?> clazz() {
        return AuditLogEvent.class;
    }
    public Type type() {
        return Type.JSON;
    }

    private String kv (String k, String v) {
        return "{\"%s\":\"%s\"}".formatted(k,v);
    }
}
