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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
            fail("Should raise exception");
        } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void testNegativeId() {
        try {
            TxnId.parse(-Long.MAX_VALUE);
            fail("Should raise exception");
        } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void testInvalidRrn() {
        try {
            TxnId id = TxnId.fromRrn(Long.toString(Long.MAX_VALUE, 36));
            fail("Should raise exception - TxnId=" + id);
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

    @Test
    public void testCreateFoldsLargeTransactionIdToSuffix() {
        // Regression: create() must remain drop-in compatible with callers passing a full TM counter.
        ZonedDateTime zdt = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, UTC);
        TxnId id = TxnId.create(zdt, 0, 1525201791L);
        // 1525201791 % 100000 = 1791 => formatted as 01791.
        assertTrue(id.toString().endsWith("-01791"), "Expected suffix -01791, got " + id);
    }

    @Test
    public void testCreateFoldsNegativeTransactionIdToSuffix() {
        // If create() uses Math.floorMod(transactionId, 100000), negatives fold into [0..99999].
        ZonedDateTime zdt = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, UTC);
        TxnId id = TxnId.create(zdt, 0, -1L);
        // floorMod(-1, 100000) = 99999.
        assertTrue(id.toString().endsWith("-99999"), "Expected suffix -99999, got " + id);
    }

    /*
     * Range/bounds tests for the new functionality.
     */

    @Test
    public void testLowerUpperBoundInstantEncloseAllIdsInSecond() {
        Instant t = Instant.parse("2026-01-01T12:34:56Z");

        long lo = TxnId.lowerBoundId(t);
        long hi = TxnId.upperBoundId(t);

        assertTrue(lo <= hi);

        TxnId a = TxnId.create(t, 0, 0L);
        TxnId b = TxnId.create(t, 999, 99999L);
        TxnId c = TxnId.create(t, 123, 45678L);

        assertTrue(a.id() >= lo && a.id() <= hi, "a not in range");
        assertTrue(b.id() >= lo && b.id() <= hi, "b not in range");
        assertTrue(c.id() >= lo && c.id() <= hi, "c not in range");

        assertEquals(TxnId.create(t, 0, 0L).id(), lo);
        assertEquals(TxnId.create(t, 999, 99999L).id(), hi);
    }

    @Test
    public void testIdRangeInstantInclusiveEndpoints() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-01T00:00:00Z"); // same second

        TxnId.TxnIdRange r = TxnId.idRange(from, to);
        assertFalse(r.isEmpty());

        assertEquals(TxnId.lowerBoundId(from), r.fromInclusive());
        assertEquals(TxnId.upperBoundId(to), r.toInclusive());

        TxnId x = TxnId.create(from, 7, 42L);
        assertTrue(x.id() >= r.fromInclusive() && x.id() <= r.toInclusive());
    }

    @Test
    public void testIdRangeInstantEmptyWhenFromAfterTo() {
        Instant from = Instant.parse("2026-01-01T00:00:01Z");
        Instant to = Instant.parse("2026-01-01T00:00:00Z");

        TxnId.TxnIdRange r = TxnId.idRange(from, to);
        assertTrue(r.isEmpty());
        assertTrue(r.fromInclusive() > r.toInclusive());
    }

    @Test
    public void testIdRangeLocalDateUtcMatchesExpectedInclusiveSeconds() {
        ZoneId zone = ZoneOffset.UTC;
        LocalDate d = LocalDate.of(2026, 1, 1);

        TxnId.TxnIdRange r = TxnId.idRange(d, d, zone);
        assertFalse(r.isEmpty());

        Instant dayStart = d.atStartOfDay(zone).toInstant();
        Instant nextStart = d.plusDays(1).atStartOfDay(zone).toInstant();
        Instant dayEndInclusive = nextStart.minusSeconds(1);

        assertEquals(TxnId.lowerBoundId(dayStart), r.fromInclusive());
        assertEquals(TxnId.upperBoundId(dayEndInclusive), r.toInclusive());
    }

    @Test
    public void testLocalDateLowerUpperHelpersConsistentWithRange() {
        ZoneId zone = ZoneId.of("UTC");
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 3);

        TxnId.TxnIdRange r = TxnId.idRange(from, to, zone);

        assertEquals(TxnId.lowerBoundId(from, zone), r.fromInclusive());
        assertEquals(TxnId.upperBoundId(to, zone), r.toInclusive());
    }

    @Test
    public void testIdRangeLocalDateOnDstGapDayIsNonEmptyAndConsistent() {
        // DST start (gap) in US typically around early March.
        ZoneId zone = ZoneId.of("America/New_York");
        LocalDate d = LocalDate.of(2026, 3, 8);

        TxnId.TxnIdRange r = TxnId.idRange(d, d, zone);
        assertFalse(r.isEmpty());

        assertEquals(TxnId.lowerBoundId(d, zone), r.fromInclusive());
        assertEquals(TxnId.upperBoundId(d, zone), r.toInclusive());

        Instant startUtc = d.atStartOfDay(zone).toInstant();
        TxnId x = TxnId.create(startUtc, 0, 0L);
        assertTrue(x.id() >= r.fromInclusive() && x.id() <= r.toInclusive());
    }

    @Test
    public void testIdRangeLocalDateOnDstOverlapDayIsNonEmptyAndConsistent() {
        // DST end (overlap) in US typically around early November.
        ZoneId zone = ZoneId.of("America/New_York");
        LocalDate d = LocalDate.of(2026, 11, 1);

        TxnId.TxnIdRange r = TxnId.idRange(d, d, zone);
        assertFalse(r.isEmpty());

        assertEquals(TxnId.lowerBoundId(d, zone), r.fromInclusive());
        assertEquals(TxnId.upperBoundId(d, zone), r.toInclusive());

        Instant startUtc = d.atStartOfDay(zone).toInstant();
        TxnId x = TxnId.create(startUtc, 999, 99999L);
        assertTrue(x.id() >= r.fromInclusive() && x.id() <= r.toInclusive());
    }

    @Test
    public void testIdRangeLocalDateTimeUtcInclusive() {
        ZoneId zone = ZoneOffset.UTC;

        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 1, 0, 0, 1);

        TxnId.TxnIdRange r = TxnId.idRange(from, to, zone);
        assertFalse(r.isEmpty());

        Instant i0 = from.atZone(zone).toInstant();
        Instant i1 = to.atZone(zone).toInstant();

        assertEquals(TxnId.lowerBoundId(i0), r.fromInclusive());
        assertEquals(TxnId.upperBoundId(i1), r.toInclusive());

        TxnId a = TxnId.create(i0, 0, 0L);
        TxnId b = TxnId.create(i1, 999, 99999L);

        assertTrue(a.id() >= r.fromInclusive() && a.id() <= r.toInclusive());
        assertTrue(b.id() >= r.fromInclusive() && b.id() <= r.toInclusive());
    }

    @Test
    public void testIdRangeZonedDateTimeDelegatesToInstantRange() {
        ZonedDateTime from = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime to = ZonedDateTime.of(2026, 1, 1, 0, 0, 10, 0, ZoneOffset.UTC);

        TxnId.TxnIdRange r1 = TxnId.idRange(from, to);
        TxnId.TxnIdRange r2 = TxnId.idRange(from.toInstant(), to.toInstant());

        assertEquals(r2, r1);
    }

    @Test
    public void testUpperBoundLocalDateUsesNextStartMinusOneSecond() {
        ZoneId zone = ZoneOffset.UTC;
        LocalDate d = LocalDate.of(2026, 1, 1);

        Instant nextStart = d.plusDays(1).atStartOfDay(zone).toInstant();
        Instant expectedEnd = nextStart.minusSeconds(1);

        long upper = TxnId.upperBoundId(d, zone);
        assertEquals(TxnId.upperBoundId(expectedEnd), upper);
    }
}
