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

package org.jpos.iso;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ISOFormattableBinaryFieldPackagerTest {

    @Test
    public void packWithTagFirst() throws ISOException {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x30, 0x31});
        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(999, "Should be 1234",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LL, IsoFieldHeaderFormatter.TAG_FIRST);

        TestUtils.assertEquals(new byte[] {(byte)0xF1, (byte)0xF2,(byte)0xF0, (byte)0xF2, (byte)0x30, (byte)0x31}, packager.pack(field));
    }

    @Test
    public void unpackWithTagFirst() throws ISOException {
        byte[] raw = new byte[] {(byte)0xF1, (byte)0xF2,(byte)0xF0, (byte)0xF2, (byte)0x30, (byte)0x31};

        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(999, "Should be 1234",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LL, IsoFieldHeaderFormatter.TAG_FIRST);

        ISOBinaryField isoField = new ISOBinaryField();
        assertEquals(raw.length, packager.unpack(isoField, raw, 0));
        TestUtils.assertEquals (new byte[] {(byte)0x30, (byte)0x31}, (byte[]) isoField.getValue());
        assertEquals(12, isoField.fieldNumber);
    }

    @Test
    public void packWithLengthFirst() throws ISOException {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x30, 0x31});
        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(999, "Should be F0F0F4F1F23031",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLL, IsoFieldHeaderFormatter.LENGTH_FIRST);

        TestUtils.assertEquals(new byte[] {(byte)0xF0, (byte)0xF0, (byte)0xF4, (byte)0xF1, (byte)0xF2, (byte)0x30, (byte)0x31}, packager.pack(field));
    }

    @Test
    public void unpackWithLengthFirst() throws ISOException {
        byte[] raw = new byte[] {(byte)0xF0, (byte)0xF0, (byte)0xF4, (byte)0xF1, (byte)0xF2, (byte)0x30, (byte)0x31};

        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(999, "Should be 1234",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLL, IsoFieldHeaderFormatter.LENGTH_FIRST);

        ISOBinaryField isoField = new ISOBinaryField();
        assertEquals(raw.length, packager.unpack(isoField, raw, 0));
        TestUtils.assertEquals(new byte[] {(byte)0x30, (byte)0x31}, (byte[])isoField.getValue());
        assertEquals(12, isoField.fieldNumber);
    }

    @Test
    public void packWithTagFirstZeroLenFixedLenField() throws ISOException {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x30, 0x31});
        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(2, "Should be F1F23031",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE, NullPrefixer.INSTANCE, IsoFieldHeaderFormatter.TAG_FIRST);

        TestUtils.assertEquals(new byte[] {(byte)0xF1, (byte)0xF2, (byte)0x30, (byte)0x31}, packager.pack(field));
    }

    @Test
    public void unpackWithTagFirstZeroLenFixedLenField() throws ISOException {
        byte[] raw = new byte[] {(byte)0xF1, (byte)0xF2, (byte)0x30, (byte)0x31};

        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(2, "Should be F1F23031",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE, NullPrefixer.INSTANCE, IsoFieldHeaderFormatter.TAG_FIRST);

        ISOBinaryField isoField = new ISOBinaryField();
        assertEquals(raw.length, packager.unpack(isoField, raw, 0));
        TestUtils.assertEquals(new byte[] {(byte)0x30, (byte)0x31}, (byte[]) isoField.getValue());
        assertEquals(12, isoField.fieldNumber);
    }

    @Test
    public void packWithLengthFirstLeftZeroPadder() throws ISOException {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x30, 0x31});
        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(10, "Should be F0F0F7F1F20000003031",
                EbcdicPrefixer.LL, LeftPadder.ZERO_PADDER, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLL, IsoFieldHeaderFormatter.LENGTH_FIRST);

        TestUtils.assertEquals(new byte[] {(byte)0xF0, (byte)0xF0, (byte)0xF7, (byte)0xF1, (byte)0xF2, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x30, (byte)0x31}, packager.pack(field));
    }

    @Test
    public void unpackWithLengthFirstLeftZeroPadder() throws ISOException {
        byte[] raw = new byte[] {(byte)0xF0, (byte)0xF0, (byte)0xF7, (byte)0xF1, (byte)0xF2, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x30, (byte)0x31};

        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(10, "Should be F0F0F7F1F20000003031",
                EbcdicPrefixer.LL, LeftPadder.ZERO_PADDER, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLL, IsoFieldHeaderFormatter.LENGTH_FIRST);

        ISOBinaryField isoField = new ISOBinaryField();
        assertEquals(raw.length, packager.unpack(isoField, raw, 0));
        TestUtils.assertEquals(new byte[]{(byte)0x30, (byte)0x31}, (byte[]) isoField.getValue());
        assertEquals(12, isoField.fieldNumber);
    }

}
