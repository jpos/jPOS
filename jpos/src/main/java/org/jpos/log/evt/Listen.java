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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jpos.log.AuditLogEvent;

import java.net.InetAddress;

public record Listen(
  int port,
  @JsonInclude(JsonInclude.Include.NON_NULL) InetAddress bindAddr,
  int permits,
  int backlog,
  @JsonInclude(JsonInclude.Include.NON_NULL) String error
) implements AuditLogEvent {

    public Listen(int port,
                  @JsonInclude(JsonInclude.Include.NON_NULL) InetAddress bindAddr,
                  int permits,
                  int backlog) {
        this(port, bindAddr, permits, backlog, null);
    }

    @Override
    public String toString() {
        return "Listen[" +
         "port=" + port +
          ", bindAddr=" + bindAddr +
          ", permits=" + permits +
          ", backlog=" + backlog +
          (error != null ? ", error='" + error + '\'' : "") +
          ']';
    }
}
