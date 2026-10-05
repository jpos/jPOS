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

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LegacyEmbeddedFieldPackagerSecurityTest {
    @Test
    void ebcdicBcdPackagersHandleEmptyAndOddValues() throws Exception {
        verifyNumericRoundTrip(new IFEB_LLNUM(5, "test"), "");
        verifyNumericRoundTrip(new IFEB_LLNUM(5, "test"), "12345");
        verifyNumericRoundTrip(new IFEB_LLLNUM(5, "test"), "");
        verifyNumericRoundTrip(new IFEB_LLLNUM(5, "test"), "12345");
    }

    @Test
    void ebcdicBcdPackagersRejectMalformedOrTruncatedInput() {
        IFEB_LLNUM ll = new IFEB_LLNUM(5, "test");
        assertThrows(ISOException.class,
          () -> ll.unpack(new ISOField(2), new byte[] {(byte) 0xF0}, 0));
        assertThrows(ISOException.class,
          () -> ll.unpack(new ISOField(2), new byte[] {0, (byte) 0xF1, 0}, 0));
        assertThrows(ISOException.class,
          () -> ll.unpack(new ISOField(2), ISOUtil.asciiToEbcdic("03"), 0));

        IFEB_LLLNUM lll = new IFEB_LLLNUM(5, "test");
        assertThrows(ISOException.class,
          () -> lll.unpack(new ISOField(2), ISOUtil.asciiToEbcdic("00"), 0));
        assertThrows(ISOException.class,
          () -> lll.unpack(new ISOField(2), new byte[] {(byte) 0xF0, 0, (byte) 0xF1}, 0));
        assertThrows(ISOException.class,
          () -> lll.unpack(new ISOField(2), ISOUtil.asciiToEbcdic("003"), 0));
    }

    @Test
    void ebcdicBcdPackagersRejectInvalidValueNibbles() {
        IFEB_LLNUM ll = new IFEB_LLNUM(6, "test");
        assertThrows(ISOException.class,
          () -> ll.unpack(new ISOField(2), concat(ISOUtil.asciiToEbcdic("01"), new byte[] {0x1A}), 0));
        assertThrows(ISOException.class,
          () -> ll.unpack(new ISOField(2), concat(ISOUtil.asciiToEbcdic("02"), new byte[] {0x1F, 0x23}), 0));

        IFEB_LLLNUM lll = new IFEB_LLLNUM(6, "test");
        assertThrows(ISOException.class,
          () -> lll.unpack(new ISOField(2), concat(ISOUtil.asciiToEbcdic("001"), new byte[] {(byte) 0xA1}), 0));
        assertThrows(ISOException.class,
          () -> lll.unpack(new ISOField(2), concat(ISOUtil.asciiToEbcdic("002"), new byte[] {0x1F, 0x23}), 0));
    }

    @Test
    void ebcdicBcdStreamPackagersRejectLengthBeforeReadingValue() {
        byte[] llInput = concat(ISOUtil.asciiToEbcdic("02"), new byte[] {0x12, 0x34});
        ByteArrayInputStream llStream = new ByteArrayInputStream(llInput);
        assertThrows(ISOException.class,
          () -> new IFEB_LLNUM(2, "test").unpack(new ISOField(2), llStream));
        assertEquals(2, llStream.available());

        byte[] lllInput = concat(ISOUtil.asciiToEbcdic("002"), new byte[] {0x12, 0x34});
        ByteArrayInputStream lllStream = new ByteArrayInputStream(lllInput);
        assertThrows(ISOException.class,
          () -> new IFEB_LLLNUM(2, "test").unpack(new ISOField(2), lllStream));
        assertEquals(2, lllStream.available());
    }

    @Test
    void embeddedCharacterPackagersRoundTripAtMaximumLength() throws Exception {
        verifyCharacterRoundTrip(new IFEP_LLCHAR(3, "test"), "ABC");
        verifyCharacterRoundTrip(new IFEP_LLLCHAR(3, "test"), "ABC");
        assertEquals(7, new IFEP_LLCHAR(3, "test").getMaxPackedLength());
        assertEquals(8, new IFEP_LLLCHAR(3, "test").getMaxPackedLength());
    }

    @Test
    void embeddedCharacterPackagersRejectTagUnderflowAndOversizedValues() {
        IFEP_LLCHAR ll = new IFEP_LLCHAR(3, "test");
        assertThrows(ISOException.class,
          () -> ll.unpack(new ISOField(), "0112".getBytes(ISOUtil.CHARSET), 0));
        assertThrows(ISOException.class,
          () -> ll.unpack(new ISOField(), "0612ABCD".getBytes(ISOUtil.CHARSET), 0));

        IFEP_LLLCHAR lll = new IFEP_LLLCHAR(3, "test");
        assertThrows(ISOException.class,
          () -> lll.unpack(new ISOField(), ISOUtil.asciiToEbcdic("00112"), 0));
        byte[] oversized = concat(ISOUtil.asciiToEbcdic("00612"), "ABCD".getBytes(ISOUtil.CHARSET));
        assertThrows(ISOException.class,
          () -> lll.unpack(new ISOField(), oversized, 0));
    }

    @Test
    void embeddedCharacterPackagersRejectMalformedAndTruncatedHeaders() {
        IFEP_LLCHAR ll = new IFEP_LLCHAR(3, "test");
        assertThrows(ISOException.class,
          () -> ll.unpack(new ISOField(), "05X2ABC".getBytes(ISOUtil.CHARSET), 0));
        assertThrows(ISOException.class,
          () -> ll.unpack(new ISOField(), "05".getBytes(ISOUtil.CHARSET), 0));
        assertThrows(ISOException.class,
          () -> ll.unpack(new ISOField(), "0512AB".getBytes(ISOUtil.CHARSET), 0));

        IFEP_LLLCHAR lll = new IFEP_LLLCHAR(3, "test");
        byte[] malformedLength = concat(new byte[] {(byte) 0xF0, 0, (byte) 0xF5, (byte) 0xF1, (byte) 0xF2},
          "ABC".getBytes(ISOUtil.CHARSET));
        assertThrows(ISOException.class,
          () -> lll.unpack(new ISOField(), malformedLength, 0));
        byte[] malformedTag = concat(ISOUtil.asciiToEbcdic("0051X"), "ABC".getBytes(ISOUtil.CHARSET));
        assertThrows(ISOException.class,
          () -> lll.unpack(new ISOField(), malformedTag, 0));
        assertThrows(ISOException.class,
          () -> lll.unpack(new ISOField(), ISOUtil.asciiToEbcdic("005"), 0));
        byte[] truncated = concat(ISOUtil.asciiToEbcdic("00512"), "AB".getBytes(ISOUtil.CHARSET));
        assertThrows(ISOException.class,
          () -> lll.unpack(new ISOField(), truncated, 0));
    }

    @Test
    void embeddedPackagerStreamsRejectLengthBeforeReadingTagOrValue() {
        ByteArrayInputStream llStream = new ByteArrayInputStream("9912ABCD".getBytes(ISOUtil.CHARSET));
        assertThrows(ISOException.class,
          () -> new IFEP_LLCHAR(3, "test").unpack(new ISOField(), llStream));
        assertEquals(6, llStream.available());

        byte[] lllInput = concat(ISOUtil.asciiToEbcdic("99912"), "ABCD".getBytes(ISOUtil.CHARSET));
        ByteArrayInputStream lllStream = new ByteArrayInputStream(lllInput);
        assertThrows(ISOException.class,
          () -> new IFEP_LLLCHAR(3, "test").unpack(new ISOField(), lllStream));
        assertEquals(6, lllStream.available());

        ByteArrayInputStream binaryStream = new ByteArrayInputStream(lllInput);
        assertThrows(ISOException.class,
          () -> new IFEP_LLLBINARY(3, "test").unpack(new ISOBinaryField(), binaryStream));
        assertEquals(6, binaryStream.available());
    }

    @Test
    void embeddedBinaryPackagerValidatesBeforeAllocation() throws Exception {
        IFEP_LLLBINARY packager = new IFEP_LLLBINARY(3, "test");
        byte[] packed = packager.pack(new ISOBinaryField(12, new byte[] {1, 2, 3}));

        ISOBinaryField arrayField = new ISOBinaryField();
        assertEquals(packed.length, packager.unpack(arrayField, packed, 0));
        assertEquals(12, arrayField.getKey());
        TestUtils.assertEquals(new byte[] {1, 2, 3}, arrayField.getBytes());

        ISOBinaryField streamField = new ISOBinaryField();
        packager.unpack(streamField, new ByteArrayInputStream(packed));
        assertEquals(12, streamField.getKey());
        TestUtils.assertEquals(new byte[] {1, 2, 3}, streamField.getBytes());

        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOBinaryField(), ISOUtil.asciiToEbcdic("00112"), 0));
        byte[] malformedLength = concat(new byte[] {(byte) 0xF0, 0, (byte) 0xF5, (byte) 0xF1, (byte) 0xF2},
          new byte[] {1, 2, 3});
        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOBinaryField(), malformedLength, 0));
        byte[] oversized = concat(ISOUtil.asciiToEbcdic("00612"), new byte[] {1, 2, 3, 4});
        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOBinaryField(), oversized, 0));
        byte[] malformedTag = concat(ISOUtil.asciiToEbcdic("0051X"), new byte[] {1, 2, 3});
        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOBinaryField(), malformedTag, 0));
        byte[] truncated = concat(ISOUtil.asciiToEbcdic("00512"), new byte[] {1, 2});
        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOBinaryField(), truncated, 0));
    }

    private static void verifyNumericRoundTrip(ISOFieldPackager packager, String value) throws Exception {
        byte[] packed = packager.pack(new ISOField(2, value));

        ISOField arrayField = new ISOField(2);
        assertEquals(packed.length, packager.unpack(arrayField, packed, 0));
        assertEquals(value, arrayField.getValue());

        ISOField streamField = new ISOField(2);
        packager.unpack(streamField, new ByteArrayInputStream(packed));
        assertEquals(value, streamField.getValue());
    }

    private static void verifyCharacterRoundTrip(ISOFieldPackager packager, String value) throws Exception {
        byte[] packed = packager.pack(new ISOField(12, value));

        ISOField arrayField = new ISOField();
        assertEquals(packed.length, packager.unpack(arrayField, packed, 0));
        assertEquals(12, arrayField.getKey());
        assertEquals(value, arrayField.getValue());

        ISOField streamField = new ISOField();
        packager.unpack(streamField, new ByteArrayInputStream(packed));
        assertEquals(12, streamField.getKey());
        assertEquals(value, streamField.getValue());
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
