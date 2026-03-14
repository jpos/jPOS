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

package org.jpos.transaction;

/** Standard key constants for the jPOS {@link Context} transient/persistent maps. */
public enum ContextConstants {
    /** Profiler instance key. */
    PROFILER, 
    /** Transaction start timestamp key. */
    TIMESTAMP,
    /** Originating ISOSource key. */
    SOURCE,
    /** Incoming request ISOMsg key. */
    REQUEST,
    /** Outgoing response ISOMsg key. */
    RESPONSE,
    /** LogEvent for the current transaction. */
    LOGEVT,
    /** Database connection key. */
    DB,
    /** Database transaction key. */
    TX,
    /** Internal result code (IRC) key. */
    IRC,
    /** Transaction name key. */
    TXNNAME,
    /** Transaction result key. */
    RESULT,
    /** Merchant ID key. */
    MID,
    /** Terminal ID key. */
    TID,
    /** Processing code key. */
    PCODE,
    /** Card object key. */
    CARD,
    /** Transmission timestamp key. */
    TRANSMISSION_TIMESTAMP,
    /** Transaction timestamp key. */
    TRANSACTION_TIMESTAMP,
    /** Capture date key. */
    CAPTURE_DATE,
    /** POS data code key. */
    POS_DATA_CODE,
    /** Transaction amount key. */
    AMOUNT,
    /** Local amount key. */
    LOCAL_AMOUNT,
    /** Original MTI for reversals. */
    ORIGINAL_MTI,
    /** Original STAN for reversals. */
    ORIGINAL_STAN,
    /** Original timestamp for reversals. */
    ORIGINAL_TIMESTAMP,
    /** Original data elements (field 90) key. */
    ORIGINAL_DATA_ELEMENTS,
    /** Routing destination key. */
    DESTINATION,
    /** Panic flag key — set to abort the current transaction node. */
    PANIC;

    private final String name;

    ContextConstants() {
        this.name = name();
    }
    ContextConstants(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
