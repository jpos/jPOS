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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.jpos.log.evt.Connect;
import org.jpos.log.evt.ContextEvt;
import org.jpos.log.evt.Deploy;
import org.jpos.log.evt.DeployActivity;
import org.jpos.log.evt.Disconnect;
import org.jpos.log.evt.License;
import org.jpos.log.evt.Listen;
import org.jpos.log.evt.LogEventEvt;
import org.jpos.log.evt.LogMessage;
import org.jpos.log.evt.ProfilerEvt;
import org.jpos.log.evt.SessionEnd;
import org.jpos.log.evt.SessionStart;
import org.jpos.log.evt.Shutdown;
import org.jpos.log.evt.Start;
import org.jpos.log.evt.Stop;
import org.jpos.log.evt.SysInfo;
import org.jpos.log.evt.ThrowableAuditLogEvent;
import org.jpos.log.evt.TraceEvt;
import org.jpos.log.evt.Txn;
import org.jpos.log.evt.UnDeploy;
import org.jpos.log.evt.Warning;
import org.jpos.util.Kind;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Registry of {@link AuditLogEvent} type mappings.
 *
 * <p>Holds the built-in type ids and discovers additional ones contributed by
 * {@link AuditLogEventProvider} implementations through
 * {@link ServiceLoader}. The result is a single source of truth used by every
 * Jackson-based renderer in jPOS to register subtypes on its {@code ObjectMapper}.</p>
 *
 * <p>The registry is initialized lazily on first use. Built-in entries are
 * always registered first so external providers cannot shadow them: any
 * provider entry that re-uses a built-in id, or that collides with another
 * provider entry, is rejected with {@link IllegalStateException}.</p>
 *
 * @since 3.0.0
 */
public final class AuditLogEventRegistry {
    private static final List<AuditLogEventType> BUILTINS = List.of(
      new AuditLogEventType("warn", Warning.class, Kind.WARN),
      new AuditLogEventType("start", Start.class, Kind.LIFECYCLE),
      new AuditLogEventType("stop", Stop.class, Kind.LIFECYCLE),
      new AuditLogEventType("deploy", Deploy.class, Kind.DEPLOY),
      new AuditLogEventType("undeploy", UnDeploy.class, Kind.DEPLOY),
      new AuditLogEventType("msg", LogMessage.class, Kind.INFO),
      new AuditLogEventType("shutdown", Shutdown.class, Kind.LIFECYCLE),
      new AuditLogEventType("deploy-activity", DeployActivity.class, Kind.DEPLOY),
      new AuditLogEventType("throwable", ThrowableAuditLogEvent.class, Kind.ERROR),
      new AuditLogEventType("license", License.class, Kind.LIFECYCLE),
      new AuditLogEventType("sysinfo", SysInfo.class, Kind.STATUS),
      new AuditLogEventType("connect", Connect.class, Kind.ISO_SESSION),
      new AuditLogEventType("disconnect", Disconnect.class, Kind.ISO_SESSION),
      new AuditLogEventType("listen", Listen.class, Kind.ISO_SESSION),
      new AuditLogEventType("session-start", SessionStart.class, Kind.ISO_SESSION),
      new AuditLogEventType("session-end", SessionEnd.class, Kind.ISO_SESSION),
      new AuditLogEventType("txn", Txn.class, Kind.TXN),
      // secondary payloads: ride along inside another event, imply no kind
      new AuditLogEventType("profiler", ProfilerEvt.class),
      new AuditLogEventType("context", ContextEvt.class),
      new AuditLogEventType("logevt", LogEventEvt.class, Kind.INFO),
      new AuditLogEventType("trace", TraceEvt.class)
    );

    private record Snapshot(Map<String, AuditLogEventType> byName, Map<Class<?>, AuditLogEventType> byClass) { }

    private static volatile Snapshot snapshot;

    private AuditLogEventRegistry() { }

    /**
     * Returns every registered type mapping.
     *
     * @return all known type mappings, built-ins first, then ServiceLoader-discovered.
     */
    public static Collection<AuditLogEventType> types() {
        return load().byName().values();
    }

    /**
     * Looks up a registered type by its discriminator id.
     *
     * @param name the {@code t} value
     * @return the type, or {@code null} when unknown
     */
    public static AuditLogEventType typeOf(String name) {
        return name == null ? null : load().byName().get(name);
    }

    /**
     * Looks up a registered type by its implementation class.
     *
     * @param clazz the {@link AuditLogEvent} implementation
     * @return the type, or {@code null} when unknown
     */
    public static AuditLogEventType typeOf(Class<?> clazz) {
        return clazz == null ? null : load().byClass().get(clazz);
    }

    /**
     * Registers every known {@link AuditLogEvent} subtype on the given
     * {@code ObjectMapper}. Renderers should call this once after constructing
     * their mapper.
     *
     * @param <M>    concrete mapper type, allowing fluent chaining on subclasses
     *               such as {@code XmlMapper}
     * @param mapper the Jackson {@link ObjectMapper} (or subclass, e.g. {@code XmlMapper})
     * @return the same mapper for chaining
     */
    public static <M extends ObjectMapper> M register(M mapper) {
        for (AuditLogEventType t : types()) {
            mapper.registerSubtypes(new NamedType(t.clazz(), t.name()));
        }
        return mapper;
    }

    /**
     * Reloads the registry. Visible for testing — callers shouldn't need this in
     * production, since mappings discovered through {@link ServiceLoader} are
     * fixed for the lifetime of the JVM.
     */
    static synchronized void reload() {
        snapshot = null;
        load();
    }

    private static Snapshot load() {
        Snapshot s = snapshot;
        if (s != null)
            return s;
        synchronized (AuditLogEventRegistry.class) {
            if (snapshot != null)
                return snapshot;
            Map<String, AuditLogEventType> map = new LinkedHashMap<>();
            for (AuditLogEventType t : BUILTINS)
                map.put(t.name(), t);
            for (AuditLogEventProvider provider : ServiceLoader.load(AuditLogEventProvider.class)) {
                Collection<AuditLogEventType> contributed = provider.types();
                if (contributed == null)
                    continue;
                for (AuditLogEventType t : contributed) {
                    AuditLogEventType existing = map.get(t.name());
                    if (existing != null && !existing.equals(t)) {
                        throw new IllegalStateException(
                          "AuditLogEventProvider " + provider.getClass().getName()
                            + " attempted to register conflicting type id '" + t.name()
                            + "' (already mapped to " + existing.clazz().getName() + ")");
                    }
                    map.put(t.name(), t);
                }
            }
            Map<Class<?>, AuditLogEventType> byClass = new LinkedHashMap<>();
            for (AuditLogEventType t : map.values())
                byClass.put(t.clazz(), t);
            snapshot = new Snapshot(Collections.unmodifiableMap(map), Collections.unmodifiableMap(byClass));
            return snapshot;
        }
    }
}
