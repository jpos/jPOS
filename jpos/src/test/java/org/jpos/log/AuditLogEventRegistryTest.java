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

package org.jpos.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jpos.log.evt.LogEvt;
import org.jpos.log.evt.LogMessage;
import org.jpos.log.evt.Warning;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuditLogEventRegistryTest {

    private static ObjectMapper newMapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        m.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return AuditLogEventRegistry.register(m);
    }

    @Test
    void builtInTypeIdsArePreserved() {
        Map<String, Class<? extends AuditLogEvent>> byName = AuditLogEventRegistry.types().stream()
          .collect(Collectors.toMap(AuditLogEventType::name, AuditLogEventType::clazz, (a, b) -> a));

        assertEquals(org.jpos.log.evt.Warning.class, byName.get("warn"));
        assertEquals(org.jpos.log.evt.Start.class, byName.get("start"));
        assertEquals(org.jpos.log.evt.Stop.class, byName.get("stop"));
        assertEquals(org.jpos.log.evt.Deploy.class, byName.get("deploy"));
        assertEquals(org.jpos.log.evt.UnDeploy.class, byName.get("undeploy"));
        assertEquals(org.jpos.log.evt.LogMessage.class, byName.get("msg"));
        assertEquals(org.jpos.log.evt.Shutdown.class, byName.get("shutdown"));
        assertEquals(org.jpos.log.evt.DeployActivity.class, byName.get("deploy-activity"));
        assertEquals(org.jpos.log.evt.ThrowableAuditLogEvent.class, byName.get("throwable"));
        assertEquals(org.jpos.log.evt.License.class, byName.get("license"));
        assertEquals(org.jpos.log.evt.SysInfo.class, byName.get("sysinfo"));
        assertEquals(org.jpos.log.evt.Connect.class, byName.get("connect"));
        assertEquals(org.jpos.log.evt.Disconnect.class, byName.get("disconnect"));
        assertEquals(org.jpos.log.evt.Listen.class, byName.get("listen"));
        assertEquals(org.jpos.log.evt.SessionStart.class, byName.get("session-start"));
        assertEquals(org.jpos.log.evt.SessionEnd.class, byName.get("session-end"));
        assertEquals(org.jpos.log.evt.Txn.class, byName.get("txn"));

        Set<String> required = Set.of(
          "warn","start","stop","deploy","undeploy","msg","shutdown","deploy-activity",
          "throwable","license","sysinfo","connect","disconnect","listen",
          "session-start","session-end","txn"
        );
        assertTrue(byName.keySet().containsAll(required), "all built-in ids must be present");
    }

    @Test
    void builtInEventSerializationIsBackwardCompatible() throws Exception {
        ObjectMapper mapper = newMapper();
        // Warning was previously serialized as {"t":"warn","warn":"oops"} — confirm that's still the shape.
        String json = mapper.writeValueAsString(new Warning("oops"));
        JsonNode node = mapper.readTree(json);
        assertEquals("warn", node.get("t").asText());
        assertEquals("oops", node.get("warn").asText());

        String msgJson = mapper.writeValueAsString(new LogMessage("hi"));
        JsonNode msg = mapper.readTree(msgJson);
        assertEquals("msg", msg.get("t").asText());
    }

    @Test
    void externalProviderEventSerializesWithConfiguredId() throws Exception {
        ObjectMapper mapper = newMapper();
        AuditLogEvent event = new TestAuditLogEventProvider.CustomEvent("xyz");
        String json = mapper.writeValueAsString(event);
        JsonNode node = mapper.readTree(json);
        assertEquals("custom-test", node.get("t").asText(),
          "ServiceLoader-discovered provider must drive the discriminator");
        assertEquals("xyz", node.get("value").asText());
    }

    @Test
    void logEvtPayloadCanMixBuiltInAndExternalEvents() throws Exception {
        ObjectMapper mapper = newMapper();
        List<AuditLogEvent> payload = List.of(
          new Warning("oops"),
          new TestAuditLogEventProvider.CustomEvent("abc"),
          new LogMessage("done")
        );
        LogEvt evt = new LogEvt(Instant.parse("2026-05-06T00:00:00Z"), "test", null, Map.of(), payload);

        String json = mapper.writeValueAsString(evt);
        JsonNode root = mapper.readTree(json);
        JsonNode list = root.get("payload");
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("warn",        list.get(0).get("t").asText());
        assertEquals("custom-test", list.get(1).get("t").asText());
        assertEquals("abc",         list.get(1).get("value").asText());
        assertEquals("msg",         list.get(2).get("t").asText());
    }
}
