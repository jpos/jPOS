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

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.*;

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
        // MAX_VALUE is a numeric ceiling for DE-037 length compliance (12 base-36 chars).
        // It is not guaranteed to decode into semantically valid YYY/DDD/SSSSS components.
        TxnId txnId = TxnId.parse(4738381338321616895L);

        // Numeric round-trip must work.
        TxnId txnId2 = TxnId.parse(txnId.id());
        assertEquals(txnId, txnId2);

        // RRN round-trip must work.
        TxnId txnId3 = TxnId.fromRrn(txnId.toRrn());
        assertEquals(txnId, txnId3);

        // Must fit DE-037.
        assertTrue(txnId.toRrn().length() <= 12);
    }


    @Test
    public void testCreateCompatibility() {
        Instant now = Instant.now();
        TxnId txnId1 = TxnId.create(now, 0, 1L);
        TxnId txnId2 = TxnId.create(now.atZone(UTC), 0, 1L);
        assertEquals(txnId1, txnId2);
    }

    /*
     * Additional tests.
     */

    @Test
    public void testZzzRrnDecodesToExpectedToString() {
        TxnId id = TxnId.fromRrn("zzzzzzzzzzzz");
        assertEquals("473-838-13383-216-16895", id.toString());
        assertEquals(12, id.toRrn().length());
        assertEquals("zzzzzzzzzzzz", id.toRrn());
    }

    @Test
    public void testFromRrnRejectsNonBase36() {
        try {
            TxnId.fromRrn("not-a-base36");
            fail("Should raise exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid rrn not-a-base36", e.getMessage());
        }
    }

    @Test
    public void testParseStringRejectsBadFormat() {
        try {
            TxnId.parse("01-002-00001-000-00001"); // wrong widths
            fail("Should raise exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid idString '01-002-00001-000-00001'", e.getMessage());
        }
    }

    @Test
    public void testParseStringRejectsNonMatchingCharacters() {
        try {
            TxnId.parse("abc-002-00001-000-00001");
            fail("Should raise exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid idString 'abc-002-00001-000-00001'", e.getMessage());
        }
    }

    @Test
    public void testRrnLengthIsAlwaysAtMost12ForCreate() {
        // Use post-2000 instants; the encoding is years-since-2000.
        TxnId a = TxnId.create(ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, UTC), 0, 0L);
        TxnId b = TxnId.create(Instant.now(), 999, 99999L);

        // DE-037 constraint: base-36 string must fit within 12 chars.
        if (a.toRrn().length() > 12) fail("RRN too long: " + a.toRrn());
        if (b.toRrn().length() > 12) fail("RRN too long: " + b.toRrn());
    }


    @Test
    public void testMaxSemanticallyValidCreateAt2473() {
        // The Javadoc statement we want to support:
        // a semantically valid TxnId created for 2473-12-31 23:59:59Z should still satisfy the 12-char DE-037 constraint.
        ZonedDateTime zdt = ZonedDateTime.of(2473, 12, 31, 23, 59, 59, 0, UTC);

        TxnId id = TxnId.create(zdt, 999, 99999L);

        // Ensure it round-trips through all formats.
        TxnId id1 = TxnId.parse(id.toString());
        TxnId id2 = TxnId.parse(id.id());
        TxnId id3 = TxnId.fromRrn(id.toRrn());

        assertEquals(id1, id2);
        assertEquals(id2, id3);
        assertEquals(id.toString(), id3.toString());

        // Must fit DE-037.
        if (id.toRrn().length() > 12) fail("RRN too long: " + id.toRrn());
    }

    @Test
    public void testCreateAfter2473IsRejectedByMaxValue() {
        ZonedDateTime zdt = ZonedDateTime.of(2474, 1, 1, 0, 0, 0, 0, UTC);
        try {
            TxnId.create(zdt, 0, 0L);
            fail("Should raise exception");
        } catch (IllegalArgumentException e) {
            assertEquals("TxnId exceeds maximum RRN value 4740010000000000000", e.getMessage());
        }
    }

}
