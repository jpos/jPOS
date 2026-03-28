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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageErrorIndicatorTest {

    // ── ErrorCode enum ─────────────────────────────────────────────────────────

    @Test
    void errorCodeResolvesByNumericValue() {
        assertEquals(MessageErrorIndicator.ErrorCode.REQUIRED_MISSING,
                MessageErrorIndicator.ErrorCode.of(1));
        assertEquals(MessageErrorIndicator.ErrorCode.CUSTOMER_VENDOR_FORMAT,
                MessageErrorIndicator.ErrorCode.of(13));
    }

    @Test
    void errorCodeReturnsNullForPrivateUseRange() {
        assertNull(MessageErrorIndicator.ErrorCode.of(6000));
        assertNull(MessageErrorIndicator.ErrorCode.of(9999));
    }

    @Test
    void errorCodeStringIsFourDigitPadded() {
        assertEquals("0001", MessageErrorIndicator.ErrorCode.REQUIRED_MISSING.codeString());
        assertEquals("0013", MessageErrorIndicator.ErrorCode.CUSTOMER_VENDOR_FORMAT.codeString());
    }

    // ── FieldError — primitive DE ──────────────────────────────────────────────

    @Test
    void primitiveErrorPacksCorrectly() {
        MessageErrorIndicator.FieldError fe =
                MessageErrorIndicator.FieldError.primitiveError(
                        MessageErrorIndicator.ErrorCode.REQUIRED_MISSING, 37);

        byte[] packed = fe.pack();
        assertEquals(14, packed.length);

        // Severity "00" (ASCII)
        assertEquals('0', packed[0]);
        assertEquals('0', packed[1]);
        // Error code "0001" (ASCII)
        assertEquals('0', packed[2]);
        assertEquals('0', packed[3]);
        assertEquals('0', packed[4]);
        assertEquals('1', packed[5]);
        // DE number "037" (ASCII)
        assertEquals('0', packed[6]);
        assertEquals('3', packed[7]);
        assertEquals('7', packed[8]);
        // Sub-element "00" (ASCII)
        assertEquals('0', packed[9]);
        assertEquals('0', packed[10]);
        // Dataset identifier 0x00 (binary)
        assertEquals(0x00, packed[11] & 0xFF);
        // Dataset bit/tag 0x0000 (binary)
        assertEquals(0x00, packed[12] & 0xFF);
        assertEquals(0x00, packed[13] & 0xFF);
    }

    @Test
    void primitiveErrorRoundTrip() throws ISOException {
        MessageErrorIndicator.FieldError original =
                MessageErrorIndicator.FieldError.primitiveError(
                        MessageErrorIndicator.ErrorCode.REQUIRED_MISSING, 37);

        byte[] packed = original.pack();
        MessageErrorIndicator.FieldError parsed =
                MessageErrorIndicator.FieldError.unpack(packed, 0);

        assertEquals(MessageErrorIndicator.Severity.REJECTED, parsed.severity());
        assertEquals(1, parsed.errorCode());
        assertEquals(37, parsed.deNumber());
        assertEquals(0, parsed.subElement());
        assertEquals(0, parsed.datasetIdentifier());
        assertEquals(0, parsed.datasetBitOrTag());
    }

    // ── FieldError — constructed DE ────────────────────────────────────────────

    @Test
    void constructedErrorCarriesSubElement() throws ISOException {
        MessageErrorIndicator.FieldError fe =
                MessageErrorIndicator.FieldError.constructedError(
                        MessageErrorIndicator.ErrorCode.INVALID_VALUE, 30, 2);

        byte[] packed = fe.pack();
        // Sub-element "02" at positions 9-10
        assertEquals('0', packed[9]);
        assertEquals('2', packed[10]);
        // Dataset identifier and bit/tag zero
        assertEquals(0x00, packed[11] & 0xFF);
        assertEquals(0x00, packed[12] & 0xFF);
        assertEquals(0x00, packed[13] & 0xFF);

        MessageErrorIndicator.FieldError parsed =
                MessageErrorIndicator.FieldError.unpack(packed, 0);
        assertEquals(30, parsed.deNumber());
        assertEquals(2, parsed.subElement());
        assertEquals(0, parsed.datasetIdentifier());
    }

    // ── FieldError — composite DE ──────────────────────────────────────────────

    @Test
    void compositeErrorCarriesDatasetIdAndTLVTag() throws ISOException {
        // ICC data error: DE-055, dataset 0x37, tag 0x9F26
        MessageErrorIndicator.FieldError fe =
                MessageErrorIndicator.FieldError.compositeError(
                        MessageErrorIndicator.ErrorCode.INVALID_VALUE, 55, 0x37, 0x9F26);

        byte[] packed = fe.pack();
        // Dataset identifier 0x37 at position 11
        assertEquals(0x37, packed[11] & 0xFF);
        // Tag 0x9F26 at positions 12-13 (big-endian)
        assertEquals(0x9F, packed[12] & 0xFF);
        assertEquals(0x26, packed[13] & 0xFF);

        MessageErrorIndicator.FieldError parsed =
                MessageErrorIndicator.FieldError.unpack(packed, 0);
        assertEquals(55, parsed.deNumber());
        assertEquals(0, parsed.subElement());
        assertEquals(0x37, parsed.datasetIdentifier());
        assertEquals(0x9F26, parsed.datasetBitOrTag());
        assertEquals(MessageErrorIndicator.ErrorCode.INVALID_VALUE, parsed.errorCodeEnum());
    }

    @Test
    void compositeErrorWithDBMBitNumber() throws ISOException {
        // Verification data: DE-049, dataset 0x71, DBM bit 3
        MessageErrorIndicator.FieldError fe =
                MessageErrorIndicator.FieldError.compositeError(
                        MessageErrorIndicator.ErrorCode.INVALID_VALUE, 49, 0x71, 3);

        byte[] packed = fe.pack();
        assertEquals(0x71, packed[11] & 0xFF);
        assertEquals(0x00, packed[12] & 0xFF);
        assertEquals(0x03, packed[13] & 0xFF);

        MessageErrorIndicator.FieldError parsed =
                MessageErrorIndicator.FieldError.unpack(packed, 0);
        assertEquals(49, parsed.deNumber());
        assertEquals(0x71, parsed.datasetIdentifier());
        assertEquals(3, parsed.datasetBitOrTag());
    }

    // ── MessageErrorIndicator — multiple error sets ────────────────────────────

    @Test
    void packsTwoErrorSetsIntoTwentyEightBytes() {
        MessageErrorIndicator mei = new MessageErrorIndicator()
                .add(MessageErrorIndicator.FieldError.primitiveError(
                        MessageErrorIndicator.ErrorCode.REQUIRED_MISSING, 37))
                .add(MessageErrorIndicator.FieldError.compositeError(
                        MessageErrorIndicator.ErrorCode.INVALID_VALUE, 55, 0x37, 0x9F26));

        byte[] packed = mei.pack();
        assertEquals(28, packed.length);
        assertEquals(2, mei.size());
    }

    @Test
    void unpacksMultipleErrorSets() throws ISOException {
        MessageErrorIndicator original = new MessageErrorIndicator()
                .add(MessageErrorIndicator.FieldError.primitiveError(
                        MessageErrorIndicator.ErrorCode.REQUIRED_MISSING, 37))
                .add(MessageErrorIndicator.FieldError.constructedError(
                        MessageErrorIndicator.ErrorCode.AMOUNT_FORMAT, 30, 1))
                .add(MessageErrorIndicator.FieldError.compositeError(
                        MessageErrorIndicator.ErrorCode.INVALID_VALUE, 55, 0x37, 0x9F26));

        byte[] packed = original.pack();
        assertEquals(42, packed.length);

        MessageErrorIndicator parsed = MessageErrorIndicator.unpack(packed);
        assertEquals(3, parsed.size());

        assertEquals(37,   parsed.errors().get(0).deNumber());
        assertEquals(1,    parsed.errors().get(0).errorCode());

        assertEquals(30,   parsed.errors().get(1).deNumber());
        assertEquals(4,    parsed.errors().get(1).errorCode());
        assertEquals(1,    parsed.errors().get(1).subElement());

        assertEquals(55,   parsed.errors().get(2).deNumber());
        assertEquals(0x37, parsed.errors().get(2).datasetIdentifier());
        assertEquals(0x9F26, parsed.errors().get(2).datasetBitOrTag());
    }

    @Test
    void unpackEmptyReturnsEmptyIndicator() throws ISOException {
        MessageErrorIndicator mei = MessageErrorIndicator.unpack(new byte[0]);
        assertTrue(mei.isEmpty());
    }

    @Test
    void unpackNullReturnsEmptyIndicator() throws ISOException {
        MessageErrorIndicator mei = MessageErrorIndicator.unpack(null);
        assertTrue(mei.isEmpty());
    }

    @Test
    void unpackThrowsOnNonMultipleOfFourteen() {
        assertThrows(ISOException.class, () ->
                MessageErrorIndicator.unpack(new byte[15]));
    }

    @Test
    void addThrowsWhenExceedingTenErrorSets() {
        MessageErrorIndicator mei = new MessageErrorIndicator();
        for (int i = 0; i < 10; i++) {
            mei.add(MessageErrorIndicator.FieldError.primitiveError(
                    MessageErrorIndicator.ErrorCode.REQUIRED_MISSING, i + 1));
        }
        assertThrows(IllegalStateException.class, () ->
                mei.add(MessageErrorIndicator.FieldError.primitiveError(
                        MessageErrorIndicator.ErrorCode.REQUIRED_MISSING, 11)));
    }

    // ── Integration with ISOMsg ────────────────────────────────────────────────

    @Test
    void setsAndGetsViaISOMsg() throws ISOException {
        ISOMsg msg = new ISOMsg("0110");
        msg.set(39, "9100");

        MessageErrorIndicator mei = new MessageErrorIndicator()
                .add(MessageErrorIndicator.FieldError.primitiveError(
                        MessageErrorIndicator.ErrorCode.REQUIRED_MISSING, 37));

        msg.set(new ISOBinaryField(18, mei.pack()));

        byte[] field18 = msg.getBytes(18);
        MessageErrorIndicator parsed = MessageErrorIndicator.unpack(field18);

        assertEquals(1, parsed.size());
        assertEquals(37, parsed.errors().get(0).deNumber());
        assertEquals(MessageErrorIndicator.ErrorCode.REQUIRED_MISSING,
                parsed.errors().get(0).errorCodeEnum());
    }

    // ── Warning severity ───────────────────────────────────────────────────────

    @Test
    void warningSeverityRoundTrip() throws ISOException {
        MessageErrorIndicator.FieldError fe =
                MessageErrorIndicator.FieldError.primitiveError(
                        MessageErrorIndicator.Severity.WARNING,
                        MessageErrorIndicator.ErrorCode.NAME_FORMAT, 43);

        byte[] packed = fe.pack();
        // Severity "01"
        assertEquals('0', packed[0]);
        assertEquals('1', packed[1]);

        MessageErrorIndicator.FieldError parsed =
                MessageErrorIndicator.FieldError.unpack(packed, 0);
        assertEquals(MessageErrorIndicator.Severity.WARNING, parsed.severity());
    }

    // ── Raw private-use error code ─────────────────────────────────────────────

    @Test
    void rawPrivateUseCodeRoundTrip() throws ISOException {
        MessageErrorIndicator.FieldError fe =
                MessageErrorIndicator.FieldError.withRawCode(
                        MessageErrorIndicator.Severity.REJECTED, 6001, 63);

        byte[] packed = fe.pack();
        MessageErrorIndicator.FieldError parsed =
                MessageErrorIndicator.FieldError.unpack(packed, 0);

        assertEquals(6001, parsed.errorCode());
        assertNull(parsed.errorCodeEnum()); // outside ISO-defined range
        assertEquals(63, parsed.deNumber());
    }
}
