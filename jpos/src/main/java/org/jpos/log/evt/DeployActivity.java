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

package org.jpos.log.evt;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.jpos.log.AuditLogEvent;

/**
 * Represents activity in the deployment directory
 *
 * <p>This record is used as part of the audit log events in the system to track deployment actions.</p>
 *
 * @param action deploy action
 * @param info additional info (such as the path) related to the deployment descriptor
 */

public record DeployActivity(@JacksonXmlProperty(isAttribute = true) Action action, String info) implements AuditLogEvent {
    public enum Action {
        CREATE, DELETE, MODIFY, RENAME, RENAME_ERROR
    }
}
