/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

public enum ContextConstants {
    PROFILER, TIMESTAMP,
    SOURCE, REQUEST, RESPONSE,
    LOGEVT,
    DB, TX,
    IRC,
    TXNNAME,
    RESULT,
    MID,
    TID,
    PCODE,
    CARD,
    TRANSMISSION_TIMESTAMP,
    TRANSACTION_TIMESTAMP,
    CAPTURE_DATE,
    POS_DATA_CODE,
    AMOUNT,
    LOCAL_AMOUNT,
    ORIGINAL_MTI,
    ORIGINAL_STAN,
    ORIGINAL_TIMESTAMP,
    ORIGINAL_DATA_ELEMENTS,
    DESTINATION,
    PANIC,
    PAUSED_TRANSACTION(":paused_transaction");

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
