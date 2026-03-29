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
 * along with this program, if not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PosCapability} (DE-027).
 */
class PosCapabilityTest {

    // -------------------------------------------------------------------------
    // Wire length
    // -------------------------------------------------------------------------

    @Test
    void wireLength_is27() {
        PosCapability cap = PosCapability.builder().build();
        assertEquals(27, cap.pack().length);
    }

    // -------------------------------------------------------------------------
    // Sub-field 27-1: card reading capability
    // -------------------------------------------------------------------------

    @Test
    void readingCapability_icc() {
        PosCapability cap = PosCapability.builder()
            .readingCapability(PosDataCode.ReadingMethod.ICC)
            .build();
        assertTrue(cap.canReadICC());
        assertFalse(cap.canReadMagstripe());
        assertFalse(cap.canReadContactless());
    }

    @Test
    void readingCapability_magstripe() {
        PosCapability cap = PosCapability.builder()
            .readingCapability(PosDataCode.ReadingMethod.MAGNETIC_STRIPE)
            .build();
        assertTrue(cap.canReadMagstripe());
        assertFalse(cap.canReadICC());
    }

    @Test
    void readingCapability_contactless() {
        PosCapability cap = PosCapability.builder()
            .readingCapability(PosDataCode.ReadingMethod.CONTACTLESS)
            .build();
        assertTrue(cap.canReadContactless());
    }

    @Test
    void readingCapability_multiple() {
        PosCapability cap = PosCapability.builder()
            .readingCapability(PosDataCode.ReadingMethod.ICC)
            .readingCapability(PosDataCode.ReadingMethod.MAGNETIC_STRIPE)
            .readingCapability(PosDataCode.ReadingMethod.CONTACTLESS)
            .build();
        assertTrue(cap.canReadICC());
        assertTrue(cap.canReadMagstripe());
        assertTrue(cap.canReadContactless());
    }

    @Test
    void readingCapability_fallback() {
        PosCapability cap = PosCapability.builder()
            .readingCapability(PosDataCode.ReadingMethod.FALLBACK)
            .build();
        assertTrue(cap.canRead(PosDataCode.ReadingMethod.FALLBACK));
    }

    // -------------------------------------------------------------------------
    // Sub-field 27-2: cardholder verification capability
    // -------------------------------------------------------------------------

    @Test
    void verificationCapability_onlinePin() {
        PosCapability cap = PosCapability.builder()
            .verificationCapability(PosDataCode.VerificationMethod.ONLINE_PIN)
            .build();
        assertTrue(cap.canVerifyOnlinePin());
        assertFalse(cap.canVerifySignature());
        assertFalse(cap.canVerifyOfflinePinInClear());
    }

    @Test
    void verificationCapability_signature() {
        PosCapability cap = PosCapability.builder()
            .verificationCapability(PosDataCode.VerificationMethod.MANUAL_SIGNATURE)
            .build();
        assertTrue(cap.canVerifySignature());
        assertFalse(cap.canVerifyOnlinePin());
    }

    @Test
    void verificationCapability_offlinePin() {
        PosCapability cap = PosCapability.builder()
            .verificationCapability(PosDataCode.VerificationMethod.OFFLINE_PIN_IN_CLEAR)
            .verificationCapability(PosDataCode.VerificationMethod.OFFLINE_PIN_ENCRYPTED)
            .build();
        assertTrue(cap.canVerifyOfflinePinInClear());
        assertTrue(cap.canVerifyOfflinePinEncrypted());
        assertFalse(cap.canVerifyOnlinePin());
    }

    @Test
    void verificationCapability_multiple() {
        PosCapability cap = PosCapability.builder()
            .verificationCapability(PosDataCode.VerificationMethod.ONLINE_PIN)
            .verificationCapability(PosDataCode.VerificationMethod.MANUAL_SIGNATURE)
            .build();
        assertTrue(cap.canVerifyOnlinePin());
        assertTrue(cap.canVerifySignature());
    }

    // -------------------------------------------------------------------------
    // Sub-field 27-3: approval code length
    // -------------------------------------------------------------------------

    @Test
    void approvalCodeLength_default_zero() {
        assertEquals(0, PosCapability.builder().build().getApprovalCodeLength());
    }

    @Test
    void approvalCodeLength_set() {
        PosCapability cap = PosCapability.builder().approvalCodeLength(6).build();
        assertEquals(6, cap.getApprovalCodeLength());
    }

    @Test
    void approvalCodeLength_boundary() {
        assertEquals(0, PosCapability.builder().approvalCodeLength(0).build().getApprovalCodeLength());
        assertEquals(9, PosCapability.builder().approvalCodeLength(9).build().getApprovalCodeLength());
    }

    @Test
    void approvalCodeLength_outOfRange_throws() {
        assertThrows(IllegalArgumentException.class, () ->
            PosCapability.builder().approvalCodeLength(10));
        assertThrows(IllegalArgumentException.class, () ->
            PosCapability.builder().approvalCodeLength(-1));
    }

    // -------------------------------------------------------------------------
    // Sub-fields 27-4 through 27-8: N3 capacity fields
    // -------------------------------------------------------------------------

    @Test
    void cardholderReceiptLength_default_zero() {
        assertEquals(0, PosCapability.builder().build().getCardholderReceiptLength());
    }

    @Test
    void cardholderReceiptLength_set() {
        assertEquals(40, PosCapability.builder().cardholderReceiptLength(40).build().getCardholderReceiptLength());
    }

    @Test
    void cardholderReceiptLength_max() {
        assertEquals(999, PosCapability.builder().cardholderReceiptLength(999).build().getCardholderReceiptLength());
    }

    @Test
    void cardAcceptorReceiptLength_set() {
        assertEquals(80, PosCapability.builder().cardAcceptorReceiptLength(80).build().getCardAcceptorReceiptLength());
    }

    @Test
    void cardholderDisplayLength_set() {
        assertEquals(16, PosCapability.builder().cardholderDisplayLength(16).build().getCardholderDisplayLength());
    }

    @Test
    void cardAcceptorDisplayLength_set() {
        assertEquals(24, PosCapability.builder().cardAcceptorDisplayLength(24).build().getCardAcceptorDisplayLength());
    }

    @Test
    void iccScriptDataLength_set() {
        assertEquals(128, PosCapability.builder().iccScriptDataLength(128).build().getIccScriptDataLength());
    }

    @Test
    void n3_outOfRange_throws() {
        assertThrows(IllegalArgumentException.class, () ->
            PosCapability.builder().cardholderReceiptLength(1000));
        assertThrows(IllegalArgumentException.class, () ->
            PosCapability.builder().cardholderReceiptLength(-1));
    }

    // -------------------------------------------------------------------------
    // Sub-field 27-9: track 3 rewrite
    // -------------------------------------------------------------------------

    @Test
    void track3Rewrite_default_false() {
        assertFalse(PosCapability.builder().build().canRewriteTrack3());
    }

    @Test
    void track3Rewrite_true() {
        assertTrue(PosCapability.builder().track3RewriteCapable(true).build().canRewriteTrack3());
    }

    @Test
    void track3Rewrite_false() {
        assertFalse(PosCapability.builder().track3RewriteCapable(false).build().canRewriteTrack3());
    }

    // -------------------------------------------------------------------------
    // Sub-field 27-10: card capture
    // -------------------------------------------------------------------------

    @Test
    void cardCapture_default_false() {
        assertFalse(PosCapability.builder().build().canCaptureCard());
    }

    @Test
    void cardCapture_true() {
        assertTrue(PosCapability.builder().cardCaptureCapable(true).build().canCaptureCard());
    }

    // -------------------------------------------------------------------------
    // Sub-field 27-11: PIN input length
    // -------------------------------------------------------------------------

    @Test
    void pinInputLength_default_zero() {
        assertEquals(0, PosCapability.builder().build().getPinInputLength());
    }

    @Test
    void pinInputLength_set() {
        assertEquals(12, PosCapability.builder().pinInputLength(12).build().getPinInputLength());
    }

    @Test
    void pinInputLength_maxByte() {
        assertEquals(255, PosCapability.builder().pinInputLength(255).build().getPinInputLength());
    }

    @Test
    void pinInputLength_outOfRange_throws() {
        assertThrows(IllegalArgumentException.class, () ->
            PosCapability.builder().pinInputLength(256));
        assertThrows(IllegalArgumentException.class, () ->
            PosCapability.builder().pinInputLength(-1));
    }

    // -------------------------------------------------------------------------
    // Pack / unpack round-trip
    // -------------------------------------------------------------------------

    @Test
    void packUnpack_roundTrip() {
        PosCapability original = PosCapability.builder()
            .readingCapability(PosDataCode.ReadingMethod.ICC)
            .readingCapability(PosDataCode.ReadingMethod.MAGNETIC_STRIPE)
            .verificationCapability(PosDataCode.VerificationMethod.ONLINE_PIN)
            .verificationCapability(PosDataCode.VerificationMethod.MANUAL_SIGNATURE)
            .approvalCodeLength(6)
            .cardholderReceiptLength(40)
            .cardAcceptorReceiptLength(40)
            .cardholderDisplayLength(16)
            .cardAcceptorDisplayLength(16)
            .iccScriptDataLength(128)
            .track3RewriteCapable(false)
            .cardCaptureCapable(true)
            .pinInputLength(12)
            .build();

        PosCapability decoded = PosCapability.unpack(original.pack());

        assertTrue(decoded.canReadICC());
        assertTrue(decoded.canReadMagstripe());
        assertFalse(decoded.canReadContactless());
        assertTrue(decoded.canVerifyOnlinePin());
        assertTrue(decoded.canVerifySignature());
        assertEquals(6,   decoded.getApprovalCodeLength());
        assertEquals(40,  decoded.getCardholderReceiptLength());
        assertEquals(40,  decoded.getCardAcceptorReceiptLength());
        assertEquals(16,  decoded.getCardholderDisplayLength());
        assertEquals(16,  decoded.getCardAcceptorDisplayLength());
        assertEquals(128, decoded.getIccScriptDataLength());
        assertFalse(decoded.canRewriteTrack3());
        assertTrue(decoded.canCaptureCard());
        assertEquals(12,  decoded.getPinInputLength());
    }

    @Test
    void unpack_wrongLength_throws() {
        assertThrows(IllegalArgumentException.class, () -> PosCapability.unpack(new byte[26]));
        assertThrows(IllegalArgumentException.class, () -> PosCapability.unpack(new byte[28]));
        assertThrows(IllegalArgumentException.class, () -> PosCapability.unpack(null));
    }

    // -------------------------------------------------------------------------
    // toBuilder independence
    // -------------------------------------------------------------------------

    @Test
    void toBuilder_doesNotMutateOriginal() {
        PosCapability original = PosCapability.builder()
            .readingCapability(PosDataCode.ReadingMethod.ICC)
            .approvalCodeLength(6)
            .build();

        PosCapability modified = original.toBuilder()
            .readingCapability(PosDataCode.ReadingMethod.MAGNETIC_STRIPE)
            .approvalCodeLength(4)
            .build();

        assertTrue(original.canReadICC());
        assertFalse(original.canReadMagstripe());
        assertEquals(6, original.getApprovalCodeLength());

        assertTrue(modified.canReadICC());
        assertTrue(modified.canReadMagstripe());
        assertEquals(4, modified.getApprovalCodeLength());
    }

    // -------------------------------------------------------------------------
    // ISOMsg integration
    // -------------------------------------------------------------------------

    @Test
    void isomsg_setAndGet_roundTrip() throws Exception {
        PosCapability cap = PosCapability.builder()
            .readingCapability(PosDataCode.ReadingMethod.ICC)
            .verificationCapability(PosDataCode.VerificationMethod.ONLINE_PIN)
            .approvalCodeLength(6)
            .pinInputLength(12)
            .build();

        ISOMsg msg = new ISOMsg();
        msg.set(new ISOBinaryField(27, cap.pack()));

        PosCapability decoded = PosCapability.unpack(msg.getBytes(27));
        assertTrue(decoded.canReadICC());
        assertTrue(decoded.canVerifyOnlinePin());
        assertEquals(6,  decoded.getApprovalCodeLength());
        assertEquals(12, decoded.getPinInputLength());
    }
}
