package org.jpos.log.render.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jpos.log.AuditLogEvent;

import org.jpos.log.LogRenderer;
import org.jpos.log.evt.LogEvt;
import org.jpos.log.evt.LogMessage;
import org.jpos.log.evt.ThrowableAuditLogEvent;
import org.jpos.log.render.ThrowableSerializer;
import org.jpos.util.LogEvent;
import org.jpos.util.Loggeable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;

public final class LogEventJsonLogRenderer implements LogRenderer<LogEvent> {
    private final ObjectMapper mapper = new ObjectMapper();

    public LogEventJsonLogRenderer() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule module = new SimpleModule();
        module.addSerializer(Throwable.class, new ThrowableSerializer());
        mapper.registerModule(module);
    }

    @Override
    public void render(LogEvent evt, PrintStream ps, String indent) {
        List<AuditLogEvent> events = evt.getPayLoad()
          .stream()
          .map (obj -> switch (obj) {
              case AuditLogEvent ale -> ale;
              case Throwable t -> new ThrowableAuditLogEvent(t);
              default -> new LogMessage(dump(obj));
          }).toList();

        long elapsed = Duration.between(evt.getCreatedAt(), evt.getDumpedAt()).toMillis();
        LogEvt ev = new LogEvt (
          evt.getDumpedAt(),
          evt.getTraceId(),
          evt.getRealm(),
          evt.getTag(),
          elapsed == 0L ? null : elapsed,
          events
        );
        try {
             ps.println (mapper.writeValueAsString(ev));
        } catch (JsonProcessingException e) {
            throw new RuntimeException (e);
        }
    }
    public Class<?> clazz() {
        return LogEvent.class;
    }
    public Type type() {
        return Type.JSON;
    }

    private String dump (Object obj) {
        if (obj instanceof Loggeable loggeable) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            loggeable.dump(ps, "");
            return baos.toString();
        }
        return obj.getClass() + ":" + obj;
    }
}
