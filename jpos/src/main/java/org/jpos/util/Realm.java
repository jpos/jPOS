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

package org.jpos.util;

/**
 * Canonical realm strings used by jPOS log sources to group related events.
 *
 * <p>Constants in this class are passed to {@link Log}/{@link Logger}
 * so events can be filtered or routed by component family.
 */
public final class Realm {
    /** Generic communications realm. */
    public static final String COMM = "comm";
    /** Channel-level communications realm. */
    public static final String COMM_CHANNEL = "comm/channel";
    /** Server-side communications realm. */
    public static final String COMM_SERVER = "comm/server";
    /** MUX communications realm. */
    public static final String COMM_MUX = "comm/mux";
    /** Client-side communications realm. */
    public static final String COMM_CLIENT = "comm/client";
    /** Transaction-manager realm. */
    public static final String TXN = "txn";
    /** Q2 container realm. */
    public static final String Q2 = "q2";
    /** Q2 deploy/undeploy realm. */
    public static final String Q2_DEPLOY = "q2/deploy";
    /** Q2 startup/shutdown lifecycle realm. */
    public static final String Q2_LIFECYCLE = "q2/lifecycle";
    /** Q2 system monitor (SysInfo) realm. */
    public static final String Q2_SYSMON = "q2/sysinfo";
    /** Security/SM realm. */
    public static final String SECURITY = "security";
    /** Generic system realm. */
    public static final String SYSTEM = "system";

    private Realm() {
    }
}
