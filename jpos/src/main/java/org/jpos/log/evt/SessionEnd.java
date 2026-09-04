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
import org.jpos.log.AuditLogEvent;

import java.time.Duration;

/**
 * Audit event recorded when a server session terminates.
 *
 * <p>Field names mirror {@link Connect} and {@link Disconnect} so viewers can
 * render channel and server sessions with one template.</p>
 *
 * @param connections active connection count after this session ended
 * @param permits     remaining session permits
 * @param host        remote address, or {@code null} when the channel exposes no socket
 * @param remotePort  remote port
 * @param localPort   local port the session was accepted on
 * @param duration    session length, from accept to close
 */
public record SessionEnd(
  int connections,
  int permits,
  @JsonInclude(JsonInclude.Include.NON_NULL) String host,
  int remotePort,
  int localPort,
  Duration duration
) implements AuditLogEvent { }
