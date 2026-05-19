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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.jpos.log.AuditLogEvent;

/**
 * Structured participant-trace line emitted during a transaction's
 * lifecycle by {@link org.jpos.transaction.TransactionManager}.
 *
 * <p>Mirrors {@link org.jpos.transaction.TransactionManager.Trace}
 * but as a typed {@link AuditLogEvent} so downstream serializers
 * don't have to parse the formatted text dump.</p>
 *
 * @param phase lifecycle phase tag (e.g. {@code prepare}, {@code commit},
 *              {@code abort}, {@code selector})
 * @param name  participant short name, or selector group name
 * @param info  trailing info — typically the participant's returned
 *              state flags ({@code PREPARED}, {@code READONLY},
 *              {@code NO_JOIN}, …). Empty string when there are none.
 *
 * @since 3.0.2
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "trace")
public record TraceEvt(
    @JsonProperty("phase") String phase,
    @JsonProperty("name")  String name,
    @JsonProperty("info")  String info
) implements AuditLogEvent { }
