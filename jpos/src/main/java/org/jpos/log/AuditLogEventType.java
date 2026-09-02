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

import java.util.Objects;

/**
 * Pairs a stable type id with the {@link AuditLogEvent} implementation it identifies,
 * and optionally with the {@link Kind} the type implies.
 *
 * <p>Used by {@link AuditLogEventProvider} implementations and by
 * {@link AuditLogEventRegistry} to register Jackson subtype mappings.</p>
 *
 * <p>When {@code kind} is set, events created through
 * {@link org.jpos.util.Log#createEvent(AuditLogEvent)} carry it as their tag.
 * The kind must be registered (by core or by a provider's
 * {@link AuditLogEventProvider#kinds()}). Leave it {@code null} for secondary
 * payloads that ride along inside another event.</p>
 *
 * @param name  stable type id used as the JSON/XML discriminator value (e.g. {@code "warn"})
 * @param clazz the {@link AuditLogEvent} implementation
 * @param kind  the kind this type implies, or {@code null}
 *
 * @since 3.0.0
 */
public record AuditLogEventType(String name, Class<? extends AuditLogEvent> clazz, String kind) {
    /**
     * Validates the record components: {@code name} must be non-null and
     * non-blank, {@code clazz} must be non-null, and {@code kind}, when
     * present, must be well-formed.
     *
     * @throws NullPointerException     if {@code name} or {@code clazz} is {@code null}
     * @throws IllegalArgumentException if {@code name} is blank or {@code kind} is malformed
     */
    public AuditLogEventType {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(clazz, "clazz");
        if (name.isBlank())
            throw new IllegalArgumentException("name must not be blank");
        if (kind != null && !Kind.isValid(kind))
            throw new IllegalArgumentException("invalid kind '" + kind + "' for type '" + name + "'");
    }

    /**
     * Creates a type that implies no kind.
     *
     * @param name  stable type id
     * @param clazz the {@link AuditLogEvent} implementation
     */
    public AuditLogEventType(String name, Class<? extends AuditLogEvent> clazz) {
        this(name, clazz, null);
    }
}
