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
