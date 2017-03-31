/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2017 jPOS Software SRL
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
