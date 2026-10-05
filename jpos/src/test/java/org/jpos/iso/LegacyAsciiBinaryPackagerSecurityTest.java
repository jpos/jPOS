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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LegacyAsciiBinaryPackagerSecurityTest {
    @Test
    void asciiHexBinaryPackagersValidateByteArrayLengths() throws Exception {
        verifyAsciiHexByteArray(new IFA_LLABINARY(4, "LL"), "02ABCD", 6);
        verifyAsciiHexByteArray(new IFA_LLLABINARY(4, "LLL"), "002ABCD", 7);
    }

    @Test
    void asciiHexBinaryPackagersValidateStreamLengths() throws Exception {
        verifyAsciiHexStream(new IFA_LLABINARY(4, "LL"), "02ABCD");
        verifyAsciiHexStream(new IFA_LLLABINARY(4, "LLL"), "002ABCD");
    }

    @Test
    void asciiHexBinaryPackagersAcceptZeroAndExactMaximum() throws Exception {
        byte[] value = new byte[] { 1, 2, 3, 4 };
        verifyAsciiHexBoundaries(new IFA_LLABINARY(4, "LL"), "00", value);
        verifyAsciiHexBoundaries(new IFA_LLLABINARY(4, "LLL"), "000", value);
    }

    @Test
    void asciiBcdNumericPackagerValidatesByteArrayLengths() throws Exception {
        IFA_LLBNUM packager = new IFA_LLBNUM(4, "LL BCD", true);
        ISOField field = new ISOField(2);

        assertEquals(4, packager.unpack(field, new byte[] { '0', '4', 0x12, 0x34 }, 0));
        assertEquals("1234", field.getValue());
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("0"), 0));
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("0X"), 0));
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("05123"), 0));
        assertThrows(ISOException.class,
          () -> packager.unpack(field, new byte[] { '0', '4', 0x12 }, 0));
        assertEquals(2, packager.unpack(field, bytes("00"), 0));
        assertEquals("", field.getValue());
    }

    @Test
    void asciiBcdNumericPackagerValidatesStreamLengths() throws Exception {
        IFA_LLBNUM packager = new IFA_LLBNUM(4, "LL BCD", true);
        ISOField field = new ISOField(2);

        packager.unpack(field, new ByteArrayInputStream(new byte[] { '0', '4', 0x12, 0x34 }));
        assertEquals("1234", field.getValue());
        assertEquals(4, packager.getMaxPackedLength());
        assertThrows(ISOException.class, () -> packager.unpack(field, stream("05")));
        assertThrows(java.io.EOFException.class,
          () -> packager.unpack(field, new ByteArrayInputStream(new byte[] { '0', '4', 0x12 })));
    }

    @Test
    void asciiBcdNumericPackagerAcceptsItsPaddedOddMaximum() throws Exception {
        IFA_LLBNUM packager = new IFA_LLBNUM(9, "odd maximum", false);
        ISOField original = new ISOField(2, "123456789");
        byte[] packed = packager.pack(original);
        ISOField unpacked = new ISOField(2);

        assertEquals(7, packager.unpack(unpacked, packed, 0));
        assertEquals("1234567890", unpacked.getValue());
    }

    @Test
    void fixedBinaryPackagerRequiresTheWholeFixedField() throws Exception {
        IFB_LLHFBINARY packager = new IFB_LLHFBINARY(4, "fixed binary");
        ISOBinaryField field = new ISOBinaryField(2);

        assertEquals(5, packager.unpack(field, new byte[] { 2, 1, 2, 0, 0 }, 0));
        TestUtils.assertEquals(new byte[] { 1, 2 }, field.getBytes());
        assertEquals(5, packager.unpack(field, new byte[] { 0, 0, 0, 0, 0 }, 0));
        TestUtils.assertEquals(new byte[0], field.getBytes());
        assertThrows(ISOException.class,
          () -> packager.unpack(field, new byte[] { 2, 1, 2 }, 0));
        assertThrows(ISOException.class,
          () -> packager.unpack(field, new byte[] { 5, 1, 2, 3, 4, 5 }, 0));
    }

    @Test
    void fixedBinaryStreamConsumesTheWholeFixedField() throws Exception {
        IFB_LLHFBINARY packager = new IFB_LLHFBINARY(4, "fixed binary");
        ISOBinaryField field = new ISOBinaryField(2);
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[] { 2, 1, 2, 0, 0, 9 });

        packager.unpack(field, in);

        TestUtils.assertEquals(new byte[] { 1, 2 }, field.getBytes());
        assertEquals(9, in.read());
        assertThrows(ISOException.class,
          () -> packager.unpack(field, new ByteArrayInputStream(new byte[] { 5 })));
        assertThrows(java.io.EOFException.class,
          () -> packager.unpack(field, new ByteArrayInputStream(new byte[] { 2, 1, 2 })));
    }

    private static void verifyAsciiHexByteArray(ISOFieldPackager packager, String valid, int consumed)
      throws Exception {
        ISOBinaryField field = new ISOBinaryField(2);
        int prefixLength = valid.length() - 4;

        assertEquals(consumed, packager.unpack(field, bytes(valid), 0));
        TestUtils.assertEquals(new byte[] { (byte) 0xAB, (byte) 0xCD }, field.getBytes());
        assertThrows(ISOException.class,
          () -> packager.unpack(field, bytes(valid.substring(0, prefixLength - 1)), 0));
        assertThrows(ISOException.class,
          () -> packager.unpack(field, bytes("X" + valid.substring(1, prefixLength)), 0));
        String tooLongPrefix = prefixLength == 2 ? "05" : "005";
        assertThrows(ISOException.class,
          () -> packager.unpack(field, bytes(tooLongPrefix + "0011223344"), 0));
        assertThrows(ISOException.class,
          () -> packager.unpack(field, bytes(valid.substring(0, prefixLength) + "ABC"), 0));
    }

    private static void verifyAsciiHexStream(ISOFieldPackager packager, String valid) throws Exception {
        ISOBinaryField field = new ISOBinaryField(2);
        int prefixLength = valid.length() - 4;

        packager.unpack(field, stream(valid));
        TestUtils.assertEquals(new byte[] { (byte) 0xAB, (byte) 0xCD }, field.getBytes());
        String tooLongPrefix = prefixLength == 2 ? "05" : "005";
        assertThrows(ISOException.class, () -> packager.unpack(field, stream(tooLongPrefix)));
        assertThrows(java.io.EOFException.class,
          () -> packager.unpack(field, stream(valid.substring(0, valid.length() - 1))));
    }

    private static void verifyAsciiHexBoundaries(ISOFieldPackager packager, String empty,
                                                   byte[] maximum) throws Exception {
        ISOBinaryField field = new ISOBinaryField(2);

        assertEquals(empty.length(), packager.unpack(field, bytes(empty), 0));
        TestUtils.assertEquals(new byte[0], field.getBytes());

        byte[] packed = packager.pack(new ISOBinaryField(2, maximum));
        assertEquals(packed.length, packager.unpack(field, packed, 0));
        TestUtils.assertEquals(maximum, field.getBytes());

        packager.unpack(field, new ByteArrayInputStream(packed));
        TestUtils.assertEquals(maximum, field.getBytes());
    }

    private static byte[] bytes(String value) {
        return value.getBytes(ISOUtil.CHARSET);
    }

    private static ByteArrayInputStream stream(String value) {
        return new ByteArrayInputStream(bytes(value));
    }
}
