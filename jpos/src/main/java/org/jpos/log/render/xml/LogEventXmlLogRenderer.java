package org.jpos.log.render.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jpos.log.AuditLogEvent;

import org.jpos.log.LogRenderer;
import org.jpos.log.evt.LogEvt;
import org.jpos.log.evt.LogMessage;
import org.jpos.util.LogEvent;
import org.jpos.util.Loggeable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public final class LogEventXmlLogRenderer implements LogRenderer<LogEvent> {
    private final XmlMapper mapper = new XmlMapper();

    public LogEventXmlLogRenderer() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void render(LogEvent evt, PrintStream ps, String indent) {
        List<AuditLogEvent> events = evt.getPayLoad()
          .stream()
          .map (obj -> obj instanceof AuditLogEvent ? (AuditLogEvent) obj : new LogMessage(dump(obj)))
          .toList();
        long elapsed = Duration.between(evt.getCreatedAt(), evt.getDumpedAt()).toMillis();
        LogEvt ev = new LogEvt (
          evt.getDumpedAt(),
          UUID.randomUUID(),
          evt.getRealm(),
          evt.getTag(),
          elapsed == 0L ? null : elapsed,
          events
        );
        try {
            ps.println (mapper.writeValueAsString(ev));
        } catch (JsonProcessingException e) {
            ps.print (kv("exception", e.toString()));
        }
    }
    public Class<?> clazz() {
        return LogEvent.class;
    }
    public Type type() {
        return Type.XML;
    }

    private String kv (String k, String v) {
        return "{\"%s\":\"%s\"}".formatted(k,v);
    }

    private String dump (Object obj) {
        if (obj instanceof Loggeable loggeable) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            loggeable.dump(ps, "");
            return baos.toString();
        }
        return obj.toString();
    }
}
