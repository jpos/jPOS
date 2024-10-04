/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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

import org.jpos.log.AuditLogEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents the stopping log entry, marking the completion of a log instance (end of file/run).
 *
 * @param id      The unique identifier of the Q2 instance, corresponding to the {@link UUID} initialized at the
 *                start of the process. This ID links the stop event directly with its corresponding start event.
 * @param uptime The duration between the {@link Start} event and this {@link Stop} event for the specific log instance,
 *                given as a {@link Duration}. This measures the total time taken for the event, providing insights into
 *                performance and operational efficiency.
 */
public record Stop(UUID id, Duration uptime) implements AuditLogEvent { }
