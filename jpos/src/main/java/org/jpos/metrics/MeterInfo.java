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

package org.jpos.metrics;

import io.micrometer.core.instrument.Tags;

public enum MeterInfo {
    TM_ACTIVE ("jpos.tm.active", "TransactionManager activeSessions"),
    TM_OPERATION("jpos.tm.op", "TransactionManager operation"),
    TM_COUNTER("jpos.tm.cnt", "TransactionManager counter"),

    ISOSERVER_CONNECTION_COUNT("jpos.server.connections", "Incoming active connections"),
    ISOCHANNEL_CONNECTION_COUNT("jpos.channel.connections", "Outgoing active connections"),

    ISOMSG_OUT ("jpos.isomsg", "Transmitted messages", Tags.of ("direction", "out")),
    ISOMSG_IN ("jpos.isomsg", "Received messages", Tags.of ("direction", "in")),
    CHANNEL_ACTIVE_CONNECTIONS("jpos.channel.connections", "Active outgoing connections"),
    CHANNEL_STATUS("jpos.channel.status", "Channel status"),

    MUX_STATUS("jpos.mux.status", "MUX Status"),
    MUX_RX_PENDING("jpos.mux.pending", "MUX rx pending"),
    MUX_RESPONSE_TIMER("jpos.mux.timer", "MUX response"),
    MUX_TX("jpos.mux", "MUX tx", Tags.of("type", "tx")),
    MUX_RX ("jpos.mux", "MUX rx", Tags.of("type", "rx")),
    MUX_MATCH ("jpos.mux", "MUX rx unhandled", Tags.of("type", "match")),
    MUX_UNHANDLED ("jpos.mux", "MUX rx unhandled", Tags.of("type", "unhandled"));

    final String id;
    final String description;
    final Tags tags;

    MeterInfo(String id, String description) {
        this (id, description, null);
    }
    MeterInfo(String id, String description, Tags tags) {
        this.id = id;
        this.description = description;
        this.tags = tags;
    }

    public String id() {
        return id;
    }

    public String description() {
        return description;
    };

    public Tags add (Tags tags) {
        return this.tags != null ? tags.and(this.tags) : tags;
    }
}
