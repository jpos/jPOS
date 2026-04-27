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

package org.jpos.metrics;

import io.micrometer.core.instrument.Tags;

/**
 * Catalog of jPOS-defined Micrometer meters: a stable identifier, human-readable
 * description, and optional default tags for each instrument.
 */
public enum MeterInfo {
    /** TransactionManager active session count. */
    TM_ACTIVE ("jpos.tm.active", "TransactionManager activeSessions"),
    /** TransactionManager operation timer. */
    TM_OPERATION("jpos.tm.op", "TransactionManager operation"),
    /** TransactionManager arbitrary counter. */
    TM_COUNTER("jpos.tm.cnt", "TransactionManager counter"),

    /** Active inbound connections accepted by ISOServer. */
    ISOSERVER_CONNECTION_COUNT("jpos.server.connections", "Incoming active connections"),
    /** Active outbound connections opened by ISOChannel. */
    ISOCHANNEL_CONNECTION_COUNT("jpos.channel.connections", "Outgoing active connections"),

    /** Outbound ISO message counter, tagged {@code direction=out}. */
    ISOMSG_OUT("jpos.isomsg", "Transmitted messages", Tags.of ("direction", "out")),
    /** Inbound ISO message counter, tagged {@code direction=in}. */
    ISOMSG_IN ("jpos.isomsg", "Received messages",    Tags.of ("direction", "in")),

    /** Active outbound connections; alias kept for backward compatibility. */
    CHANNEL_ACTIVE_CONNECTIONS("jpos.channel.connections", "Active outgoing connections"),
    /** Per-channel up/down status gauge. */
    CHANNEL_STATUS("jpos.channel.status", "Channel status"),

    /** Per-MUX up/down status gauge. */
    MUX_STATUS("jpos.mux.status", "MUX Status"),
    /** Number of in-flight requests awaiting a response. */
    MUX_RX_PENDING("jpos.mux.pending", "MUX rx pending"),
    /** Response time timer for MUX request/response pairs. */
    MUX_RESPONSE_TIMER("jpos.mux.timer", "MUX response"),
    /** MUX transmit counter, tagged {@code type=tx}. */
    MUX_TX("jpos.mux", "MUX tx", Tags.of("type", "tx")),
    /** MUX receive counter, tagged {@code type=rx}. */
    MUX_RX ("jpos.mux", "MUX rx", Tags.of("type", "rx")),
    /** MUX matched-response counter, tagged {@code type=match}. */
    MUX_MATCH ("jpos.mux", "MUX rx unhandled", Tags.of("type", "match")),
    /** MUX unhandled-response counter, tagged {@code type=unhandled}. */
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

    /**
     * Returns the meter identifier.
     *
     * @return the meter id (e.g. {@code jpos.mux.status})
     */
    public String id() {
        return id;
    }

    /**
     * Returns the human-readable meter description.
     *
     * @return the meter description
     */
    public String description() {
        return description;
    };

    /**
     * Combines the supplied tags with this meter's default tags.
     *
     * @param tags caller-supplied tags
     * @return the combined tag set, or {@code tags} unchanged when this meter has no defaults
     */
    public Tags add (Tags tags) {
        return this.tags != null ? tags.and(this.tags) : tags;
    }
}
