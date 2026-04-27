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

import java.util.Objects;

/**
 * Pairs a stable type id with the {@link AuditLogEvent} implementation it identifies.
 *
 * <p>Used by {@link AuditLogEventProvider} implementations and by
 * {@link AuditLogEventRegistry} to register Jackson subtype mappings.</p>
 *
 * @param name  stable type id used as the JSON/XML discriminator value (e.g. {@code "warn"})
 * @param clazz the {@link AuditLogEvent} implementation
 *
 * @since 3.0.0
 */
public record AuditLogEventType(String name, Class<? extends AuditLogEvent> clazz) {
    /**
     * Validates the record components: {@code name} must be non-null and
     * non-blank, and {@code clazz} must be non-null.
     *
     * @throws NullPointerException     if {@code name} or {@code clazz} is {@code null}
     * @throws IllegalArgumentException if {@code name} is blank
     */
    public AuditLogEventType {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(clazz, "clazz");
        if (name.isBlank())
            throw new IllegalArgumentException("name must not be blank");
    }
}
