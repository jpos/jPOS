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

import java.util.Map;
import org.junit.jupiter.api.Test;

public class LogEventTagsTest {
    @Test
    void tagsEmptyByDefault() {
        LogEvent ev = new LogEvent("test");
        assertTrue(ev.getTags().isEmpty());
    }

    @Test
    void withTagAddsEntry() {
        LogEvent ev = new LogEvent("test")
            .withTag("tid", "T001")
            .withTag("mid", "M001");
        assertEquals(Map.of("tid", "T001", "mid", "M001"), ev.getTags());
    }

    @Test
    void withTagOverwritesExisting() {
        LogEvent ev = new LogEvent("test")
            .withTag("tid", "T001")
            .withTag("tid", "T002");
        assertEquals("T002", ev.getTags().get("tid"));
        assertEquals(1, ev.getTags().size());
    }

    @Test
    void withTagsBulkAdd() {
        LogEvent ev = new LogEvent("test")
            .withTags(Map.of("tid", "T001", "mid", "M001"));
        assertEquals(2, ev.getTags().size());
        assertEquals("T001", ev.getTags().get("tid"));
    }

    @Test
    void withTagsNullIsNoOp() {
        LogEvent ev = new LogEvent("test").withTags(null);
        assertTrue(ev.getTags().isEmpty());
    }

    @Test
    void withTagsEmptyMapIsNoOp() {
        LogEvent ev = new LogEvent("test").withTags(Map.of());
        assertTrue(ev.getTags().isEmpty());
    }

    @Test
    void getTagsReturnsUnmodifiableView() {
        LogEvent ev = new LogEvent("test").withTag("k", "v");
        assertThrows(UnsupportedOperationException.class, () -> ev.getTags().put("x", "y"));
    }

    @Test
    void fluentChaining() {
        LogEvent ev = new LogEvent("test")
            .withTraceId()
            .withTag("tid", "T001")
            .add("payload");
        assertEquals("T001", ev.getTags().get("tid"));
        assertNotNull(ev.getTraceId());
        assertEquals(1, ev.getPayLoad().size());
    }
}
