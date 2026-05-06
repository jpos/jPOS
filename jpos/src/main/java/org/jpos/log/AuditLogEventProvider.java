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

import java.util.Collection;

/**
 * SPI for contributing additional {@link AuditLogEvent} implementations.
 *
 * <p>External modules register their event classes by declaring an implementation in
 * {@code META-INF/services/org.jpos.log.AuditLogEventProvider}; the
 * {@link AuditLogEventRegistry} discovers and merges them with the built-in mappings
 * via {@link java.util.ServiceLoader}.</p>
 *
 * <p>Type ids must be unique. Providers must not reuse any of the built-in ids
 * ({@code warn}, {@code start}, {@code stop}, {@code deploy}, {@code undeploy},
 * {@code msg}, {@code shutdown}, {@code deploy-activity}, {@code throwable},
 * {@code license}, {@code sysinfo}, {@code connect}, {@code disconnect},
 * {@code listen}, {@code session-start}, {@code session-end}, {@code txn}).</p>
 *
 * @since 3.0.0
 */
public interface AuditLogEventProvider {
    /**
     * @return the type mappings contributed by this provider; must not be {@code null}.
     */
    Collection<AuditLogEventType> types();
}
