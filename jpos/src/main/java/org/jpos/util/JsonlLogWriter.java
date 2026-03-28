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

package org.jpos.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.log.AuditLogEvent;
import org.jpos.log.evt.LogEvt;
import org.jpos.log.evt.LogMessage;
import org.jpos.log.evt.ThrowableAuditLogEvent;
import org.jpos.log.render.ThrowableSerializer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.time.Duration;
import java.util.*;

/**
 * JSONL (one JSON object per line) LogEventWriter with built-in PCI protection.
 *
 * <p>When serializing ISOMsg objects in LogEvent payloads, sensitive fields are
 * masked or wiped inline — no upstream {@link ProtectedLogListener} required.</p>
 *
 * <p>Configuration properties (same convention as {@link ProtectedLogListener}):</p>
 * <ul>
 *   <li>{@code protect} — space-separated field numbers to mask via {@link ISOUtil#protect(String)} (default: {@code "2"})</li>
 *   <li>{@code wipe} — space-separated field numbers to replace with [WIPED] (default: {@code "35 45 48 52 55"})</li>
 * </ul>
 *
 * <p>Output is suitable for {@code jq}, Filebeat, and Elasticsearch ingestion.</p>
 *
 * @since 3.0.0
 */
public class JsonlLogWriter extends BaseLogEventWriter implements Configurable {
    private static final String WIPED = "[WIPED]";
    private static final Set<Integer> DEFAULT_SAFE_FIELDS = Set.of(
        3, 4, 7, 11, 12, 13, 18, 22, 24, 25, 32, 37, 38, 39, 41, 42, 49, 90
    );

    private ObjectMapper mapper;
    private String host;
    private Set<Integer> protectFields = Set.of(2);
    private Set<Integer> wipeFields = Set.of(35, 45, 48, 52, 55);

    /** Default constructor. */
    public JsonlLogWriter() {
        initMapper();
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            host = "unknown";
        }
    }

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        String protect = cfg.get("protect", null);
        if (protect != null) {
            protectFields = toIntSet(protect);
        }
        String wipe = cfg.get("wipe", null);
        if (wipe != null) {
            wipeFields = toIntSet(wipe);
        }
        initMapper();
    }

    @Override
    public void write(LogEvent ev) {
        if (p == null || ev == null)
            return;
        try {
            List<AuditLogEvent> events;
            synchronized (ev.getPayLoad()) {
                events = ev.getPayLoad()
                    .stream()
                    .map(this::toAuditLogEvent)
                    .toList();
            }
            long elapsed = Duration.between(ev.getCreatedAt(), ev.getDumpedAt()).toMillis();
            LogEvt logEvt = new LogEvt(
                ev.getDumpedAt(),
                ev.getTag(),
                elapsed == 0L ? null : elapsed,
                buildTags(ev),
                events
            );
            p.println(mapper.writeValueAsString(logEvt));
            p.flush();
        } catch (JsonProcessingException e) {
            p.println("{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            p.flush();
        }
    }

    private AuditLogEvent toAuditLogEvent(Object obj) {
        return switch (obj) {
            case AuditLogEvent ale -> ale;
            case ISOMsg m -> new LogMessage(protectAndDump(m));
            case Throwable t -> new ThrowableAuditLogEvent(t);
            default -> new LogMessage(dump(obj));
        };
    }

    private Map<String,String> buildTags(LogEvent ev) {
        ev.getTraceId(); // ensure trace-id is generated
        Map<String,String> tags = new LinkedHashMap<>();
        String realm = ev.getRealm();
        if (realm != null && !realm.isEmpty())
            tags.put("realm", realm);
        if (host != null)
            tags.put("host", host);
        tags.putAll(ev.getTags());
        return tags;
    }

    private String protectAndDump(ISOMsg original) {
        ISOMsg m = (ISOMsg) original.clone();
        for (int field : protectFields) {
            String v = m.getString(field);
            if (v != null) {
                m.set(field, ISOUtil.protect(v));
            }
        }
        for (int field : wipeFields) {
            if (m.hasField(field)) {
                m.set(field, WIPED);
            }
        }
        return dump(m);
    }

    private String dump(Object obj) {
        if (obj instanceof Loggeable loggeable) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            loggeable.dump(ps, "");
            return baos.toString().trim();
        }
        return obj.toString();
    }

    private void initMapper() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule module = new SimpleModule();
        module.addSerializer(Throwable.class, new ThrowableSerializer());
        mapper.registerModule(module);
    }

    private static Set<Integer> toIntSet(String spaceSeparated) {
        if (spaceSeparated == null || spaceSeparated.isBlank())
            return Set.of();
        Set<Integer> result = new HashSet<>();
        for (String token : spaceSeparated.trim().split("\\s+")) {
            result.add(Integer.parseInt(token));
        }
        return Collections.unmodifiableSet(result);
    }
}
