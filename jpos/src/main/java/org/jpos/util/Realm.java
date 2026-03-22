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

public final class Realm {
    public static final String COMM = "comm";
    public static final String COMM_CHANNEL = "comm/channel";
    public static final String COMM_SERVER = "comm/server";
    public static final String COMM_MUX = "comm/mux";
    public static final String COMM_CLIENT = "comm/client";
    public static final String TXN = "txn";
    public static final String Q2 = "q2";
    public static final String Q2_DEPLOY = "q2/deploy";
    public static final String Q2_LIFECYCLE = "q2/lifecycle";
    public static final String SECURITY = "security";
    public static final String SECURITY_AUTHN = "security/authn";
    public static final String SECURITY_AUTHZ = "security/authz";
    public static final String SECURITY_AUDIT = "security/audit";
    public static final String SYSTEM = "system";

    private Realm() {
    }
}
