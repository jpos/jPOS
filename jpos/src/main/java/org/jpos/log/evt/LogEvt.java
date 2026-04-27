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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.jpos.log.AuditLogEvent;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Top-level structured log envelope serialised as the {@code <log>} element.
 *
 * @param ts       event timestamp
 * @param kind     event kind/category
 * @param lifespan lifespan in milliseconds, or {@code null} when not measured
 * @param tags     optional tag map (omitted from output when empty)
 * @param events   payload of structured audit events
 */
@JacksonXmlRootElement(localName = "log")
public record LogEvt(
  @JacksonXmlProperty(isAttribute = true) Instant ts,
  @JacksonXmlProperty(isAttribute = true) String kind,
  @JacksonXmlProperty(isAttribute = true) Long lifespan,
  @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String,String> tags,
  @JsonProperty("payload") @JacksonXmlElementWrapper(useWrapping = false) List<AuditLogEvent> events) { }