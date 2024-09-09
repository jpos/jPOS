/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jpos.log.evt.*;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "t"
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = Warning.class, name = "warn"),
  @JsonSubTypes.Type(value = Start.class, name = "start"),
  @JsonSubTypes.Type(value = Stop.class, name = "stop"),
  @JsonSubTypes.Type(value = Deploy.class, name = "deploy"),
  @JsonSubTypes.Type(value = UnDeploy.class, name = "undeploy"),
  @JsonSubTypes.Type(value = LogMessage.class, name = "msg"),
  @JsonSubTypes.Type(value = Shutdown.class, name = "shutdown"),
  @JsonSubTypes.Type(value = DeployActivity.class, name = "deploy-activity"),
  @JsonSubTypes.Type(value = ThrowableAuditLogEvent.class, name = "throwable"),
  @JsonSubTypes.Type(value = License.class, name = "license"),
  @JsonSubTypes.Type(value = SysInfo.class, name = "sysinfo"),
  @JsonSubTypes.Type(value = Connect.class, name = "connect"),
  @JsonSubTypes.Type(value = Disconnect.class, name = "disconnect"),
  @JsonSubTypes.Type(value = Listen.class, name = "listen"),
  @JsonSubTypes.Type(value = SessionStart.class, name = "session-start"),
  @JsonSubTypes.Type(value = SessionEnd.class, name = "session-end"),
  @JsonSubTypes.Type(value = Txn.class, name = "txn")
})

public sealed interface AuditLogEvent permits Connect, Deploy, DeployActivity, Disconnect, License, Listen, LogMessage, SessionEnd, SessionStart, Shutdown, Start, Stop, SysInfo, ThrowableAuditLogEvent, Txn, UnDeploy, Warning { }
