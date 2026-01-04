/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.log.render.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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

public final class LogEventXmlLogRenderer implements LogRenderer<LogEvent> {
    private final XmlMapper mapper = new XmlMapper();

    public LogEventXmlLogRenderer() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

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
