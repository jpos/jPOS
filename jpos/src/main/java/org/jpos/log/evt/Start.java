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

import org.jpos.log.AuditLogEvent;

import java.util.UUID;

/**
 * Represents the starting log entry for an auditing process in the system. This record encapsulates
 * all the essential details needed for initializing audit logs in a structured and consistent format.
 *
 * @param q2        The identifier of the Q2 system instance from which the log is originating.
 * @param version   The version of the Q2 system, detailing the specific build or release version.
 * @param appVersion The version of the application that is running within the Q2 system,
 *                   providing context about the application's release state.
 * @param deploy     Absolute path to Q2's deploy directory.
 * @param env        The name of the environment in which the application is running.
 */
public record Start(UUID q2, String version, String appVersion, String deploy, String env) implements AuditLogEvent { }
