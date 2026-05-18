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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.jpos.log.AuditLogEvent;

import java.util.List;

/**
 * Structured representation of a nested {@code LogEvent} (e.g. the
 * {@code LOGEVT} entry inside a transaction {@code Context}).
 *
 * <p>Top-level events are rendered by {@link LogEvt}; this lighter shape
 * carries only the tag and a flat list of rendered messages — much easier to
 * consume from JSONL than the legacy {@code <tag>...</tag>} text blob.</p>
 *
 * @param tag      the event tag (e.g. {@code "info"}, {@code "warn"})
 * @param messages payload entries rendered to text
 *
 * @since 3.0.2
 */
@JacksonXmlRootElement(localName = "logevt")
public record LogEventEvt(
    @JacksonXmlProperty(isAttribute = true) String tag,
    @JsonProperty("messages") @JacksonXmlElementWrapper(useWrapping = false) List<String> messages
) implements AuditLogEvent { }
