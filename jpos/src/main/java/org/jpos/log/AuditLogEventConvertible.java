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

/**
 * Implemented by domain objects (e.g. {@link org.jpos.util.Profiler},
 * {@link org.jpos.transaction.Context}) that can describe themselves as a
 * structured {@link AuditLogEvent}.
 *
 * <p>Structured writers such as {@code JsonlLogWriter} discover this interface
 * to render payload entries as typed objects instead of opaque
 * {@code dump()} strings.</p>
 *
 * @since 3.0.2
 */
public interface AuditLogEventConvertible {
    /**
     * Builds a structured snapshot of this object suitable for typed log writers.
     *
     * @return a structured representation of this object as an {@link AuditLogEvent}.
     */
    AuditLogEvent toAuditEvent();
}
