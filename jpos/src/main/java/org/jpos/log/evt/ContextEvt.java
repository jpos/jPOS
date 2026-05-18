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

package org.jpos.log.evt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.jpos.log.AuditLogEvent;

import java.util.Map;

/**
 * Structured representation of an {@code org.jpos.transaction.Context}.
 *
 * <p>Values that implement {@link org.jpos.log.AuditLogEventConvertible}
 * (e.g. {@link org.jpos.util.Profiler}) are expanded into nested
 * {@link AuditLogEvent} records — so a profiler embedded in the context
 * surfaces as a {@code ProfilerEvt} rather than a text blob.</p>
 *
 * @param entries the context's transient entries, structured where possible
 *                and rendered as text otherwise
 *
 * @since 3.0.2
 */
@JacksonXmlRootElement(localName = "context")
public record ContextEvt(
    @JsonProperty("entries") Map<String, Object> entries
) implements AuditLogEvent { }
