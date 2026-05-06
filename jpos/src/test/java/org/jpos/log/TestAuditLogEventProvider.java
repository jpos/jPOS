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

import java.util.List;

/**
 * Provider used by the {@code AuditLogEventRegistry} tests to verify
 * {@link java.util.ServiceLoader} discovery of external types.
 */
public class TestAuditLogEventProvider implements AuditLogEventProvider {

    public record CustomEvent(String value) implements AuditLogEvent { }

    @Override
    public List<AuditLogEventType> types() {
        return List.of(new AuditLogEventType("custom-test", CustomEvent.class));
    }
}
