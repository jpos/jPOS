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

package org.jpos.metrics;
public enum MeterInfo {
    TM_ACTIVE_SESSIONS("jpos.tm.active.sessions", "TransactionManager activeSessions"),
    TM_OPERATION("jpos.tm.op", "TransactionManager operation"),
    TM_COUNTER("jpos.tm.cnt", "TransactionManager counter"),
    ISOSERVER_CONNECTION_COUNT("jpos.server.connections", "Incoming active connections"),
    ISOCHANNEL_CONNECTION_COUNT("jpos.channel.connections", "Outgoing active connections"),
    ISOMSG_OUT ("jpos.isomsg.out", "Transmitted messages"),
    ISOMSG_IN ("jpos.isomsg.in", "Received messages"),
    CHANNEL_ACTIVE_CONNECTIONS("jpos.channel.connections", "Active outgoing connections"),
    MUX_STATUS("jpos.mux.status", "MUX Status"),
    MUX_TX("jpos.mux.tx", "MUX tx counter"),
    MUX_RX("jpos.mux.rx", "MUX rx counter"),
    MUX_EXPIRED("jpos.mux.expired.cnt", "MUX expired counter"),
    MUX_RESPONSE_TIME("jpos.mux.timer", "MUX response timer"),
    CHANNEL_STATUS("jpos.channel.status", "Channel status");

    final String id;
    final String description;

    MeterInfo(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String id() {
        return id;
    }

    public String description() {
        return description;
    };
}
