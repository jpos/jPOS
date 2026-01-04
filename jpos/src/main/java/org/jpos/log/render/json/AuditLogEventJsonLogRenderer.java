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

package org.jpos.log.render.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
