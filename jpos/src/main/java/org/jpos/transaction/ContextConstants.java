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

/**
 * Well-known keys for entries placed into a transaction {@link Context}.
 *
 * <p>Each constant's {@link #toString()} is the canonical context key used
 * across jPOS participants and adaptors. Refer to the source for a concise
 * description of each individual constant.
 */
public enum ContextConstants {
    /** Per-transaction profiler instance. */
    PROFILER,
    /** Wall-clock timestamp captured when the transaction started. */
    TIMESTAMP,
    /** Source channel/peer from which the request originated. */
    SOURCE,
    /** Inbound request message. */
    REQUEST,
    /** Outbound response message. */
    RESPONSE,
    /** Aggregated log event for the transaction. */
    LOGEVT,
    /** Database connection or session reserved for this transaction. */
    DB,
    /** Database transaction handle, when applicable. */
    TX,
    /** International response code resolved during processing. */
    IRC,
    /** Logical transaction name. */
    TXNNAME,
    /** Final {@link org.jpos.rc.Result} of the transaction. */
    RESULT,
    /** Merchant identifier. */
    MID,
    /** Terminal identifier. */
    TID,
    /** Processing code. */
    PCODE,
    /** Cardholder/card data captured by the transaction. */
    CARD,
    /** Transmission timestamp from the inbound message. */
    TRANSMISSION_TIMESTAMP,
    /** Transaction timestamp from the inbound message. */
    TRANSACTION_TIMESTAMP,
    /** Capture date for clearing/settlement. */
    CAPTURE_DATE,
    /** POS data code describing the entry environment. */
    POS_DATA_CODE,
    /** Settlement/transaction amount. */
    AMOUNT,
    /** Cardholder-billing amount in the cardholder's local currency. */
    LOCAL_AMOUNT,
    /** MTI of the original message, when echoing or reversing. */
    ORIGINAL_MTI,
    /** STAN of the original message, when echoing or reversing. */
    ORIGINAL_STAN,
    /** Transmission timestamp of the original message. */
    ORIGINAL_TIMESTAMP,
    /** Data elements echoed from the original message. */
    ORIGINAL_DATA_ELEMENTS,
    /** Routing destination chosen for the transaction. */
    DESTINATION,
    /** Panic flag indicating the transaction must abort immediately. */
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
