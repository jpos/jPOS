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

import org.jpos.log.AuditLogEventRegistry;
import org.jpos.log.AuditLogEventType;
import org.jpos.log.TestAuditLogEventProvider;
import org.jpos.log.evt.ProfilerEvt;
import org.jpos.log.evt.SessionStart;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KindTest {

    @Test
    void everyRegisteredKindIsValidAndHasAFamily() {
        Set<String> kinds = Kind.registered();
        assertTrue(kinds.size() >= 35, "core registers its emitted kinds");
        for (String k : kinds) {
            assertTrue(Kind.isValid(k), k);
            assertTrue(Kind.isRegistered(k), k);
            assertNotNull(Kind.family(k), k);
        }
    }

    @Test
    void everyTypeThatImpliesAKindImpliesARegisteredOne() {
        for (AuditLogEventType t : AuditLogEventRegistry.types()) {
            if (t.kind() != null)
                assertTrue(Kind.isRegistered(t.kind()), t.name() + " -> " + t.kind());
        }
    }

    @Test
    void familyNeverReturnsNull() {
        assertEquals(Kind.Family.NEUTRAL, Kind.family(null));
        assertEquals(Kind.Family.NEUTRAL, Kind.family(""));
        assertEquals(Kind.Family.NEUTRAL, Kind.family("   "));
        assertEquals(Kind.Family.NEUTRAL, Kind.family("no-such-kind"));
        assertEquals(Kind.Family.NEUTRAL, Kind.family("jcard.pin-change"));
    }

    @Test
    void familyIsCaseInsensitiveAndHonoursLegacyAliases() {
        assertEquals(Kind.Family.WARN, Kind.family("WARN"));
        assertEquals(Kind.Family.WARN, Kind.family("warning"));
        assertEquals(Kind.Family.WARN, Kind.family(" Warning "));
        assertEquals(Kind.Family.FAIL, Kind.family("fatal"));
        assertEquals(Kind.Family.FAIL, Kind.family("Fatal"));
        assertEquals(Kind.Family.TRANSPORT, Kind.family(Kind.ISO_SESSION));
        assertEquals(Kind.Family.SECURITY, Kind.family(Kind.S_M_OPERATION));
    }

    @Test
    void kindOfTypeFollowsTheUmbrella() {
        assertEquals(Kind.ISO_SESSION, Kind.kindOf("listen"));
        assertEquals(Kind.ISO_SESSION, Kind.kindOf("session-start"));
        assertEquals(Kind.LIFECYCLE, Kind.kindOf("start"));
        assertEquals(Kind.LIFECYCLE, Kind.kindOf("license"));
        assertEquals(Kind.DEPLOY, Kind.kindOf("undeploy"));
        assertEquals(Kind.ERROR, Kind.kindOf("throwable"));
        assertEquals(Kind.ISO_SESSION, Kind.kindOf(new SessionStart(1, 10, "127.0.0.1", 4321, 8000)));
    }

    @Test
    void kindOfFallsBackToInfo() {
        assertEquals(Kind.INFO, Kind.kindOf("profiler"), "secondary payload implies no kind");
        assertEquals(Kind.INFO, Kind.kindOf(new ProfilerEvt(0L, List.of())));
        assertEquals(Kind.INFO, Kind.kindOf("no-such-type"));
        assertEquals(Kind.INFO, Kind.kindOf((String) null));
        assertEquals(Kind.INFO, Kind.kindOf((org.jpos.log.AuditLogEvent) null));
    }

    @Test
    void isValidEnforcesTheFormat() {
        assertTrue(Kind.isValid("send"));
        assertTrue(Kind.isValid("session-start"));
        assertTrue(Kind.isValid("jcard.pin-change"));
        assertTrue(Kind.isValid("a".repeat(Kind.MAX_LENGTH)));
        assertFalse(Kind.isValid(null));
        assertFalse(Kind.isValid(""));
        assertFalse(Kind.isValid("Send"));
        assertFalse(Kind.isValid("a b"));
        assertFalse(Kind.isValid("comm/channel"));
        assertFalse(Kind.isValid("1abc"));
        assertFalse(Kind.isValid("-abc"));
        assertFalse(Kind.isValid("abc."));
        assertFalse(Kind.isValid("a".repeat(Kind.MAX_LENGTH + 1)));
    }

    @Test
    void registeredIsImmutable() {
        assertThrows(UnsupportedOperationException.class, () -> Kind.registered().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> Kind.registered().remove(Kind.INFO));
    }

    @Test
    void providersRegisterKindsAndImpliedKinds() {
        assertTrue(Kind.isRegistered(TestAuditLogEventProvider.CUSTOM_KIND));
        assertEquals(Kind.Family.TELEMETRY, Kind.family(TestAuditLogEventProvider.CUSTOM_KIND));
        assertEquals(TestAuditLogEventProvider.CUSTOM_KIND, Kind.kindOf("custom-test"));
        assertEquals(TestAuditLogEventProvider.CUSTOM_KIND,
          Kind.kindOf(new TestAuditLogEventProvider.CustomEvent("v")));
        assertEquals("custom-family", Kind.family(TestAuditLogEventProvider.UNTYPED_KIND),
          "families are extensible");
    }

    @Test
    void conflictingRegistrationThrows() {
        Map<String, String> map = new HashMap<>();
        Kind.register(map, new Kind.Def("dup", Kind.Family.WARN), "a");
        Kind.register(map, new Kind.Def("dup", Kind.Family.WARN), "b"); // same family: fine
        assertThrows(IllegalStateException.class,
          () -> Kind.register(map, new Kind.Def("dup", Kind.Family.FAIL), "c"));
    }

    @Test
    void defRejectsMalformedValues() {
        assertThrows(IllegalArgumentException.class, () -> new Kind.Def("Bad", Kind.Family.WARN));
        assertThrows(IllegalArgumentException.class, () -> new Kind.Def("ok", null));
        assertThrows(IllegalArgumentException.class, () -> new Kind.Def("ok", "Bad Family"));
        assertThrows(IllegalArgumentException.class, () -> new AuditLogEventType("t", SessionStart.class, "Bad"));
    }

    @Test
    void logLevelConstantsAreUnchangedAndReferenceKind() {
        assertEquals("trace", Log.TRACE);
        assertEquals("debug", Log.DEBUG);
        assertEquals("info",  Log.INFO);
        assertEquals("warn",  Log.WARN);
        assertEquals("error", Log.ERROR);
        assertEquals("fatal", Log.FATAL);
        assertEquals(Kind.FATAL, Log.FATAL);
        assertTrue(Kind.isRegistered(Log.FATAL));
    }

    @Test
    void createEventUsesTheImpliedKindAndDefaultStaysInfo() {
        Log log = new Log(new Logger(), "test");
        LogEvent evt = log.createEvent(new SessionStart(1, 10, "127.0.0.1", 4321, 8000));
        assertEquals(Kind.ISO_SESSION, evt.getTag());
        assertEquals(1, evt.getPayLoad().size());
        assertEquals(Kind.INFO, log.createEvent(new ProfilerEvt(0L, List.of())).getTag());
        assertEquals(Kind.INFO, new LogEvent().getTag());
        assertEquals(Kind.ISO_SESSION, new LogEvent(log, new SessionStart(1, 10, "127.0.0.1", 4321, 8000)).getTag());
    }
}
