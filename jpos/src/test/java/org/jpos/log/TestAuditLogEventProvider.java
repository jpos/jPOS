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

import org.jpos.util.Kind;

import java.util.List;

/**
 * Provider used by the {@code AuditLogEventRegistry} and {@code Kind} tests
 * to verify {@link java.util.ServiceLoader} discovery of external types and kinds.
 */
public class TestAuditLogEventProvider implements AuditLogEventProvider {

    public static final String CUSTOM_KIND = "custom-kind";
    public static final String UNTYPED_KIND = "custom-untyped";

    public record CustomEvent(String value) implements AuditLogEvent { }

    @Override
    public List<AuditLogEventType> types() {
        return List.of(new AuditLogEventType("custom-test", CustomEvent.class, CUSTOM_KIND));
    }

    @Override
    public List<Kind.Def> kinds() {
        return List.of(
          new Kind.Def(CUSTOM_KIND, Kind.Family.TELEMETRY),
          new Kind.Def(UNTYPED_KIND, "custom-family")
        );
    }
}
