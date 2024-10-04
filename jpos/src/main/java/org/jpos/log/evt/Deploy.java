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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.jpos.log.AuditLogEvent;

/**
 * Represents a deployment action
 *
 * <p>This record is used as part of the audit log events in the system to track deployment actions.</p>
 *
 * @param path the path where the deployment is located
 * @param enabled a boolean flag indicating whether the deployment is enabled
 * @param eager a boolean flag indicating whether the QBean requires eager-start
 */

public record Deploy(
  String path,
  @JacksonXmlProperty(isAttribute = true) boolean enabled,
  @JacksonXmlProperty(isAttribute = true) @JsonInclude(JsonInclude.Include.NON_DEFAULT) boolean eager
) implements AuditLogEvent {
    @Override
    public String toString() {
        return "Deploy{" +
          "path='" + path + '\'' +
          ", enabled=" + enabled +
          (eager ? ", eager=" + eager : "")+
          '}';
    }
}
