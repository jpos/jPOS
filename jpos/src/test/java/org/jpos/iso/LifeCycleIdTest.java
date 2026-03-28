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

package org.jpos.iso;

import org.jpos.transaction.TxnId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class LifeCycleIdTest {

    // ── Wire length ───────────────────────────────────────────────────────────

    @Test
    void packProduces19Bytes() {
        byte[] wire = LifeCycleId.builder()
                .supportIndicator('1')
                .traceId("ABCDE1234567890")
                .build()
                .pack();
        assertEquals(19, wire.length);
    }

    // ── Support indicator ─────────────────────────────────────────────────────

    @Test
    void supportIndicatorLiteralCharRoundTrip() throws ISOException {
        LifeCycleId lc = LifeCycleId.builder().supportIndicator('2').build();
        LifeCycleId parsed = LifeCycleId.unpack(lc.pack());
        assertEquals('2', parsed.supportIndicator());
    }

    @Test
    void supportIndicatorFromFourDigitMTI() {
        assertEquals('1', LifeCycleId.supportIndicatorForMTI("0100"));
        assertEquals('2', LifeCycleId.supportIndicatorForMTI("0200"));
        assertEquals('4', LifeCycleId.supportIndicatorForMTI("0420"));
        assertEquals('8', LifeCycleId.supportIndicatorForMTI("0804"));
    }

    @Test
    void supportIndicatorFromThreeDigitCMFMTI() {
        assertEquals('1', LifeCycleId.supportIndicatorForMTI("100"));
        assertEquals('2', LifeCycleId.supportIndicatorForMTI("200"));
    }

    @Test
    void supportIndicatorBuilderFromMTIString() throws ISOException {
        LifeCycleId lc = LifeCycleId.builder()
                .supportIndicator("0100")
                .build();
        assertEquals('1', lc.supportIndicator());
    }

    // ── Trace identifier ──────────────────────────────────────────────────────

    @Test
    void rawTraceRoundTrip() throws ISOException {
        String trace = "ABCDE1234567890";
        LifeCycleId lc = LifeCycleId.builder().traceId(trace).build();
        LifeCycleId parsed = LifeCycleId.unpack(lc.pack());
        assertEquals(trace, parsed.traceIdentifier().trim());
    }

    @Test
    void shortTraceIsPadded() throws ISOException {
        LifeCycleId lc = LifeCycleId.builder().traceId("SHORT").build();
        assertEquals(15, lc.traceIdentifier().length());
        assertTrue(lc.traceIdentifier().startsWith("SHORT"));
    }

    @Test
    void traceExceeding15CharsThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                LifeCycleId.builder().traceId("1234567890123456")); // 16 chars
    }

    // ── TxnId integration ─────────────────────────────────────────────────────

    @Test
    void txnIdRoundTripViaTruncatedRrn() throws ISOException {
        TxnId txnId = TxnId.create(Instant.parse("2026-03-28T15:00:00Z"), 1, 42L);
        LifeCycleId lc = LifeCycleId.builder()
                .supportIndicator('1')
                .traceId(txnId)
                .build();

        assertEquals(15, lc.traceIdentifier().length());

        LifeCycleId parsed = LifeCycleId.unpack(lc.pack());
        assertTrue(parsed.txnId().isPresent());
        assertEquals(txnId.id(), parsed.txnId().get().id());
    }

    @Test
    void txnIdReturnsEmptyForExternalTrace() throws ISOException {
        LifeCycleId lc = LifeCycleId.builder()
                .traceId("EXTERNALID00000")
                .build();
        LifeCycleId parsed = LifeCycleId.unpack(lc.pack());
        assertFalse(parsed.txnId().isPresent());
    }

    // ── Sequence number ───────────────────────────────────────────────────────

    @Test
    void sequenceNumberZeroByDefault() throws ISOException {
        LifeCycleId lc = LifeCycleId.builder().build();
        assertEquals(0, LifeCycleId.unpack(lc.pack()).sequenceNumber());
    }

    @Test
    void sequenceNumberRoundTrip() throws ISOException {
        LifeCycleId lc = LifeCycleId.builder().sequenceNumber(7).build();
        assertEquals(7, LifeCycleId.unpack(lc.pack()).sequenceNumber());
    }

    // ── Auth token ────────────────────────────────────────────────────────────

    @Test
    void authTokenZeroByDefault() throws ISOException {
        LifeCycleId lc = LifeCycleId.builder().build();
        assertEquals(0, LifeCycleId.unpack(lc.pack()).authToken());
    }

    @Test
    void authTokenRoundTrip() throws ISOException {
        LifeCycleId lc = LifeCycleId.builder().authToken(9999).build();
        assertEquals(9999, LifeCycleId.unpack(lc.pack()).authToken());
    }

    // ── toBuilder (financial presentment echo pattern) ────────────────────────

    @Test
    void toBuilderProducesFullCopyByDefault() throws ISOException {
        TxnId txnId = TxnId.create(Instant.parse("2026-03-28T15:00:00Z"), 1, 99L);
        LifeCycleId auth = LifeCycleId.builder()
                .supportIndicator("0100")
                .traceId(txnId)
                .sequenceNumber(0)
                .authToken(0)
                .build();

        // Simulate financial presentment echo with updated seq and token
        LifeCycleId financial = auth.toBuilder()
                .sequenceNumber(1)
                .authToken(5678)
                .build();

        // Support indicator and trace are preserved
        assertEquals(auth.supportIndicator(), financial.supportIndicator());
        assertEquals(auth.traceIdentifier(), financial.traceIdentifier());

        // Sequence and token are updated
        assertEquals(1, financial.sequenceNumber());
        assertEquals(5678, financial.authToken());

        // Original is unchanged
        assertEquals(0, auth.sequenceNumber());
        assertEquals(0, auth.authToken());
    }

    @Test
    void toBuilderProducesIndependentInstance() throws ISOException {
        LifeCycleId original = LifeCycleId.builder()
                .supportIndicator('1')
                .traceId("ORIGINAL000000A")
                .sequenceNumber(1)
                .build();

        LifeCycleId modified = original.toBuilder()
                .traceId("MODIFIED00000AB")
                .build();

        assertEquals("ORIGINAL000000A", original.traceIdentifier());
        assertEquals("MODIFIED00000AB", modified.traceIdentifier());
    }

    // ── Unpack validation ─────────────────────────────────────────────────────

    @Test
    void unpackNullThrows() {
        assertThrows(ISOException.class, () -> LifeCycleId.unpack(null));
    }

    @Test
    void unpackTooShortThrows() {
        assertThrows(ISOException.class, () -> LifeCycleId.unpack(new byte[10]));
    }

    // ── Integration with ISOMsg ───────────────────────────────────────────────

    @Test
    void roundTripViaISOBinaryField() throws ISOException {
        TxnId txnId = TxnId.create(Instant.parse("2026-03-28T15:00:00Z"), 7, 12345L);
        LifeCycleId lc = LifeCycleId.builder()
                .supportIndicator("0200")
                .traceId(txnId)
                .sequenceNumber(3)
                .authToken(5678)
                .build();

        ISOMsg msg = new ISOMsg("0200");
        msg.set(new ISOBinaryField(21, lc.pack()));

        LifeCycleId parsed = LifeCycleId.unpack(msg.getBytes(21));

        assertEquals('2', parsed.supportIndicator());
        assertEquals(3, parsed.sequenceNumber());
        assertEquals(5678, parsed.authToken());
        assertTrue(parsed.txnId().isPresent());
        assertEquals(txnId.id(), parsed.txnId().get().id());
    }
}
