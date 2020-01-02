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

public class ISOFormattableStringFieldPackagerTest {

    @Test
    public void packWithTagFirst() throws ISOException {
        ISOField field = new ISOField(60, "123");
        ISOFormattableStringFieldPackager ttllPackager = new ISOFormattableStringFieldPackager(999, "Should be 6003123",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LL, IsoFieldHeaderFormatter.TAG_FIRST);

        TestUtils.assertEquals(new byte[] {(byte)0xF6, (byte)0xF0,(byte)0xF0, (byte)0xF3,  (byte)0xF1, (byte)0xF2, (byte)0xF3}, ttllPackager.pack(field));
    }

    @Test
    public void unpackWithTagFirst() throws ISOException {
        byte[] raw = new byte[] {(byte)0xF6, (byte)0xF0,(byte)0xF0, (byte)0xF3,  (byte)0xF1, (byte)0xF2, (byte)0xF3};

        ISOFormattableStringFieldPackager packager = new ISOFormattableStringFieldPackager(999, "Should be 6003123",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LL, IsoFieldHeaderFormatter.TAG_FIRST);

        ISOField isoField = new ISOField();
        assertEquals(raw.length, packager.unpack(isoField, raw, 0));
        assertEquals("123", (String) isoField.getValue());
        assertEquals(60, isoField.fieldNumber);
    }

    @Test
    public void packWithLengthFirst() throws ISOException {
        ISOField field = new ISOField(60, "123");
        ISOFormattableStringFieldPackager ttllPackager = new ISOFormattableStringFieldPackager(999, "Should be 00560123",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LLL, IsoFieldHeaderFormatter.LENGTH_FIRST);

        TestUtils.assertEquals(new byte[] {(byte)0xF0, (byte)0xF0, (byte)0xF5, (byte)0xF6, (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3}, ttllPackager.pack(field));
    }

    @Test
    public void unpackWithLengthFirst() throws ISOException {
        byte[] raw = new byte[] {(byte)0xF0, (byte)0xF0, (byte)0xF5, (byte)0xF6, (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3};

        ISOFormattableStringFieldPackager packager = new ISOFormattableStringFieldPackager(999, "Should be 00560123",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LLL, IsoFieldHeaderFormatter.LENGTH_FIRST);

        ISOField isoField = new ISOField();
        assertEquals(raw.length, packager.unpack(isoField, raw, 0));
        assertEquals("123", (String) isoField.getValue());
        assertEquals(60, isoField.fieldNumber);
    }

    @Test
    public void packWithTagFirstZeroLenFixedLenField() throws ISOException {
        ISOField field = new ISOField(60, "123");
        ISOFormattableStringFieldPackager ttllPackager = new ISOFormattableStringFieldPackager(3, "Should be 60123",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, NullPrefixer.INSTANCE, IsoFieldHeaderFormatter.TAG_FIRST);

        TestUtils.assertEquals(new byte[] {(byte)0xF6, (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3}, ttllPackager.pack(field));
    }

    @Test
    public void unpackWithTagFirstZeroLenFixedLenField() throws ISOException {
        byte[] raw = new byte[] {(byte)0xF6, (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3};

        ISOFormattableStringFieldPackager packager = new ISOFormattableStringFieldPackager(3, "Should be 60123",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, NullPrefixer.INSTANCE, IsoFieldHeaderFormatter.TAG_FIRST);

        ISOField isoField = new ISOField();
        assertEquals(raw.length, packager.unpack(isoField, raw, 0));
        assertEquals("123", (String) isoField.getValue());
        assertEquals(60, isoField.fieldNumber);
    }

    @Test
    public void packWithLengthFirstLeftZeroPadder() throws ISOException {
        ISOField field = new ISOField(60, "123");
        ISOFormattableStringFieldPackager ttllPackager = new ISOFormattableStringFieldPackager(10, "Should be F0F1F2F6F0F0F0F0F0F0F0F0F1F2F3",
                EbcdicPrefixer.LL, LeftPadder.ZERO_PADDER, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LLL, IsoFieldHeaderFormatter.LENGTH_FIRST);

        TestUtils.assertEquals(new byte[] {(byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF6, (byte)0xF0, (byte)0xF0, (byte)0xF0, (byte)0xF0, (byte)0xF0,
                (byte)0xF0, (byte)0xF0, (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3}, ttllPackager.pack(field));
    }

    @Test
    public void unpackWithLengthFirstLeftZeroPadder() throws ISOException {
        byte[] raw = new byte[] {(byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF6, (byte)0xF0, (byte)0xF0, (byte)0xF0, (byte)0xF0, (byte)0xF0,
                (byte)0xF0, (byte)0xF0, (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3};

        ISOFormattableStringFieldPackager packager = new ISOFormattableStringFieldPackager(10, "Should be F0F1F2F6F0F0F0F0F0F0F0F0F1F2F3",
                EbcdicPrefixer.LL, LeftPadder.ZERO_PADDER, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LLL, IsoFieldHeaderFormatter.LENGTH_FIRST);

        ISOField isoField = new ISOField();
        assertEquals(raw.length, packager.unpack(isoField, raw, 0));
        assertEquals("123", (String) isoField.getValue());
        assertEquals(60, isoField.fieldNumber);
    }

}
