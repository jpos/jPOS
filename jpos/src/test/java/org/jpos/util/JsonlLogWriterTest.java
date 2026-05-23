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

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.TransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class JsonlLogWriterTest {
    private JsonlLogWriter writer;
    private ByteArrayOutputStream baos;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        writer = new JsonlLogWriter();
        baos = new ByteArrayOutputStream();
        writer.setPrintStream(new PrintStream(baos));
        objectMapper = new ObjectMapper();
    }

    @Test
    void writesValidJsonLine() throws Exception {
        LogEvent ev = new LogEvent("test");
        ev.addMessage("hello");
        writer.write(ev);

        String line = baos.toString().trim();
        assertFalse(line.contains("\n"), "should be a single line");
        JsonNode node = objectMapper.readTree(line);
        assertEquals("test", node.get("kind").asText());
        assertNotNull(node.get("ts"));
        assertNotNull(node.get("tags").get("host"), "host should be in tags");
    }

    @Test
    void includesTagsInOutput() throws Exception {
        LogEvent ev = new LogEvent("send")
            .withTag("tid", "TERM001")
            .withTag("mid", "MERCH001");
        ev.addMessage("payload");
        writer.write(ev);

        JsonNode node = objectMapper.readTree(baos.toString().trim());
        JsonNode tags = node.get("tags");
        assertNotNull(tags, "tags should be present");
        assertEquals("TERM001", tags.get("tid").asText());
        assertEquals("MERCH001", tags.get("mid").asText());
    }

    @Test
    void alwaysIncludesTraceIdAndHost() throws Exception {
        LogEvent ev = new LogEvent("test");
        ev.addMessage("hello");
        writer.write(ev);

        JsonNode node = objectMapper.readTree(baos.toString().trim());
        JsonNode tags = node.get("tags");
        assertNotNull(tags, "tags should always be present");
        assertNotNull(tags.get("trace-id"), "trace-id should always be in tags");
        assertNotNull(tags.get("host"), "host should always be in tags");
    }

    @Test
    void protectsPanField() throws Exception {
        ISOMsg m = new ISOMsg();
        m.setMTI("0200");
        m.set(2, "4111111111111111");
        m.set(11, "123456");

        LogEvent ev = new LogEvent("send");
        ev.addMessage(m);
        writer.write(ev);

        String output = baos.toString().trim();
        assertFalse(output.contains("4111111111111111"), "PAN must not appear in cleartext");
        assertTrue(output.contains("411111"), "BIN should be preserved");
        assertTrue(output.contains("1111"), "last 4 should be preserved");
        assertTrue(output.contains("123456"), "safe field 11 should pass through");
    }

    @Test
    void wipesTrackData() throws Exception {
        ISOMsg m = new ISOMsg();
        m.setMTI("0200");
        m.set(35, "4111111111111111=2512");
        m.set(52, "AABBCCDD");

        LogEvent ev = new LogEvent("send");
        ev.addMessage(m);
        writer.write(ev);

        String output = baos.toString().trim();
        assertFalse(output.contains("4111111111111111=2512"), "track2 must be wiped");
        assertFalse(output.contains("AABBCCDD"), "PIN block must be wiped");
        assertTrue(output.contains("[WIPED]"), "wiped fields should show [WIPED]");
    }

    @Test
    void customProtectAndWipeConfig() throws Exception {
        Properties props = new Properties();
        props.setProperty("protect", "");
        props.setProperty("wipe", "2");
        writer.setConfiguration(new SimpleConfiguration(props));

        ISOMsg m = new ISOMsg();
        m.setMTI("0200");
        m.set(2, "4111111111111111");

        LogEvent ev = new LogEvent("send");
        ev.addMessage(m);
        writer.write(ev);

        String output = baos.toString().trim();
        assertTrue(output.contains("[WIPED]"), "field 2 should be wiped");
        assertFalse(output.contains("4111111111111111"), "PAN must not appear");
    }

    @Test
    void protectsNestedFieldPath() throws Exception {
        Properties props = new Properties();
        props.setProperty("protect", "123.1");
        props.setProperty("wipe", "");
        writer.setConfiguration(new SimpleConfiguration(props));

        ISOMsg m = new ISOMsg();
        m.setMTI("0200");
        m.set("123.1", "4111111111111111");

        LogEvent ev = new LogEvent("send");
        ev.addMessage(m);
        writer.write(ev);

        String output = baos.toString().trim();
        assertFalse(output.contains("4111111111111111"), "nested PAN must not appear in cleartext");
        assertTrue(output.contains("411111"), "nested BIN should be preserved");
        assertTrue(output.contains("1111"), "nested last 4 should be preserved");
    }

    @Test
    void wipesNestedFieldPath() throws Exception {
        Properties props = new Properties();
        props.setProperty("protect", "");
        props.setProperty("wipe", "112.3.1");
        writer.setConfiguration(new SimpleConfiguration(props));

        ISOMsg m = new ISOMsg();
        m.setMTI("0200");
        m.set("112.3.1", "secret nested value");

        LogEvent ev = new LogEvent("send");
        ev.addMessage(m);
        writer.write(ev);

        String output = baos.toString().trim();
        assertFalse(output.contains("secret nested value"), "nested field must be wiped");
        assertTrue(output.contains("[WIPED]"), "nested wiped fields should show [WIPED]");
    }

    @Test
    void includesRealmInTags() throws Exception {
        Log source = new Log();
        source.setRealm("channel/send");
        LogEvent ev = new LogEvent(source, "send");
        ev.addMessage("hello");
        writer.write(ev);

        JsonNode node = objectMapper.readTree(baos.toString().trim());
        assertEquals("channel/send", node.get("tags").get("realm").asText());
    }

    @Test
    void usesPayloadFieldName() throws Exception {
        LogEvent ev = new LogEvent("test");
        ev.addMessage("hello");
        writer.write(ev);

        JsonNode node = objectMapper.readTree(baos.toString().trim());
        assertNotNull(node.get("payload"), "events should be serialized as 'payload'");
        assertNull(node.get("evt"), "old 'evt' field name should not appear");
    }

    @Test
    void nestedLogEventRendersAsStructuredBlock() throws Exception {
        LogEvent inner = new LogEvent("info");
        inner.addMessage("alpha");
        inner.addMessage("beta");

        LogEvent outer = new LogEvent("commit");
        outer.addMessage(inner);
        writer.write(outer);

        JsonNode entry = objectMapper.readTree(baos.toString().trim()).get("payload").get(0);
        assertEquals("logevt", entry.get("t").asText());
        assertEquals("info",   entry.get("tag").asText());
        JsonNode messages = entry.get("messages");
        assertEquals(2, messages.size());
        assertEquals("alpha", messages.get(0).asText());
        assertEquals("beta",  messages.get(1).asText());
    }

    @Test
    void profilerRendersAsStructuredEvent() throws Exception {
        Profiler prof = new Profiler();
        prof.checkPoint("step-a");
        prof.checkPoint("step-b");

        LogEvent ev = new LogEvent("commit");
        ev.addMessage(prof);
        writer.write(ev);

        JsonNode node = objectMapper.readTree(baos.toString().trim());
        JsonNode entry = node.get("payload").get(0);
        assertEquals("profiler", entry.get("t").asText(), "profiler entries should use the 'profiler' type id");
        assertTrue(entry.has("elapsed_ns"), "profiler should expose elapsed_ns");
        JsonNode checkpoints = entry.get("checkpoints");
        assertNotNull(checkpoints);
        // step-a, step-b, end (auto-added)
        assertEquals(3, checkpoints.size());
        // each checkpoint is [name, duration_ns, total_ns]
        JsonNode first = checkpoints.get(0);
        assertEquals(3, first.size(), "checkpoint should be a 3-tuple");
        assertEquals("step-a", first.get(0).asText());
        assertTrue(first.get(1).isNumber(), "duration_ns should be numeric");
        assertTrue(first.get(2).isNumber(), "total_ns should be numeric");
        assertEquals("end", checkpoints.get(2).get(0).asText());
    }

    @Test
    void participantTracesRenderAsStructuredEvents() throws Exception {
        LogEvent ev = new LogEvent("commit");
        ev.addMessage(TransactionManager.Trace.of("prepare", "PartA"));
        ev.addMessage(TransactionManager.Trace.of("prepare", "PartB", "READONLY NO_JOIN"));
        ev.addMessage(TransactionManager.Trace.of("commit", "PartA"));
        writer.write(ev);

        JsonNode payload = objectMapper.readTree(baos.toString().trim()).get("payload");
        assertEquals(3, payload.size());
        for (JsonNode entry : payload) {
            assertEquals("trace", entry.get("t").asText(),
                "every Trace.of(...) message should serialise as a TraceEvt, got " + entry);
        }
        assertEquals("prepare", payload.get(0).get("phase").asText());
        assertEquals("PartA",   payload.get(0).get("name").asText());
        // info is empty — JsonInclude.NON_EMPTY should drop it
        assertNull(payload.get(0).get("info"));

        assertEquals("prepare", payload.get(1).get("phase").asText());
        assertEquals("PartB",   payload.get(1).get("name").asText());
        assertEquals("READONLY NO_JOIN", payload.get(1).get("info").asText());

        assertEquals("commit", payload.get(2).get("phase").asText());
        assertEquals("PartA",  payload.get(2).get("name").asText());
    }

    @Test
    void contextWithOpaqueValueDoesNotFailSerialization() throws Exception {
        org.jpos.transaction.Context ctx = new org.jpos.transaction.Context();
        ctx.put("STR", "hello");
        // anonymous Object: Jackson cannot serialize this without
        // FAIL_ON_EMPTY_BEANS disabled — toString() fallback in
        // Context.convertEntry is what makes this safe.
        ctx.put("OPAQUE", new Object() {
            @Override public String toString() { return "opaque-value"; }
        });

        LogEvent ev = new LogEvent("commit");
        ev.addMessage(ctx);
        writer.write(ev);

        String line = baos.toString().trim();
        assertFalse(line.startsWith("{\"error\":"), "should not have fallen into error branch: " + line);
        JsonNode entry = objectMapper.readTree(line).get("payload").get(0);
        assertEquals("context", entry.get("t").asText());
        assertEquals("hello", entry.get("entries").get("STR").asText());
        assertEquals("opaque-value", entry.get("entries").get("OPAQUE").asText());
    }

    @Test
    void nullEventIsNoOp() {
        writer.write(null);
        assertEquals(0, baos.size());
    }

    @Test
    void nullPrintStreamIsNoOp() {
        writer.setPrintStream(null);
        LogEvent ev = new LogEvent("test");
        ev.addMessage("hello");
        writer.write(ev);
        // should not throw
    }
}
