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

/**
 * Audit log event recording a channel disconnection.
 * @param host remote host name or address
 * @param remotePort remote port number
 * @param localPort local port number
 * @param exception exception class name, or {@code null} if the disconnect was clean
 * @param message exception message, or {@code null} if the disconnect was clean
 */
public record Disconnect(
  String host,
  int remotePort,
  int localPort,
  @JsonInclude(JsonInclude.Include.NON_NULL) String exception,
  @JsonInclude(JsonInclude.Include.NON_NULL) String message) implements AuditLogEvent { }