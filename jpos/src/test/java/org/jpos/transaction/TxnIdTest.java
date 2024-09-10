/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
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

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TxnIdTest {
    @Test
    public void test() {
        TxnId txnId = TxnId.create(Instant.now(), 0, 1L);
        TxnId txnId1 = TxnId.parse(txnId.toString());
        TxnId txnId2 = TxnId.parse(txnId.id());
        TxnId txnId3 = TxnId.fromRrn(txnId.toRrn());
        assertEquals(txnId1, txnId2);
        assertEquals(txnId1.toString(), txnId2.toString());
        assertEquals(txnId2, txnId3);
        assertEquals(txnId2.toString(), txnId3.toString());
        assertEquals(12, txnId.toRrn().length());
    }

    @Test
    public void testBigId() {
        try {
            TxnId.parse(Long.MAX_VALUE);
            fail ("Should raise exception");
        } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void testNegativeId() {
        try {
            TxnId.parse(-Long.MAX_VALUE);
            fail ("Should raise exception");
        } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void testInvalidRrn() {
        try {
            TxnId id = TxnId.fromRrn(Long.toString(Long.MAX_VALUE,36));
            fail ("Should raise exception - TxnId=" + id);
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid rrn 1y2p0ij32e8e7", e.getMessage());
        }
    }

    @Test
    public void testMaxValue() {
        TxnId txnId = TxnId.parse(4738381338321616895L);
        TxnId txnId1 = TxnId.parse(txnId.toString());
        TxnId txnId2 = TxnId.parse(txnId.id());
        TxnId txnId3 = TxnId.fromRrn(txnId.toRrn());
        assertEquals(txnId1, txnId2);
        assertEquals(txnId1.toString(), txnId2.toString());
        assertEquals(txnId2, txnId3);
        assertEquals(txnId2.toString(), txnId3.toString());
        assertEquals(12, txnId.toRrn().length());
    }

    @Test
    public void testCreateCompatibility() {
        Instant now = Instant.now();
        TxnId txnId1 = TxnId.create(now, 0, 1L);
        TxnId txnId2 = TxnId.create(now.atZone(UTC), 0, 1L);
        assertEquals(txnId1, txnId2);
    }
}
