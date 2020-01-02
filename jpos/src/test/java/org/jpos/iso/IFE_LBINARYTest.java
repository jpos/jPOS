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

import org.junit.jupiter.api.Test;

/**
 * @author mouslih.abdelhakim@gmail.com
 */
public class IFE_LBINARYTest {

    @Test
    public void testPackAnEmptyArray() throws Exception
    {
        ISOBinaryField field = new ISOBinaryField(9, new byte[]{});
        IFE_LBINARY packager = new IFE_LBINARY(9, "testing packing an empty Array");
        TestUtils.assertEquals(new byte[] {(byte)0xF0},
                packager.pack(field));
    }

    @Test
    public void testPackANonEmptyArray() throws Exception
    {
        ISOBinaryField field = new ISOBinaryField(9, new byte[] {0x30, 0x31});
        IFE_LBINARY packager = new IFE_LBINARY(9, "testing packing a non empty Array");
        TestUtils.assertEquals(new byte[] {(byte) 0xF2, 0x30, 0x31},
                packager.pack(field));
    }

    @Test
    public void testPackAFullArray() throws Exception
    {
        ISOBinaryField field = new ISOBinaryField(9, new byte[] {0x30, 0x31, 0X32, 0X33, 0X34, 0X35, 0X36, 0X37, 0X38});
        IFE_LBINARY packager = new IFE_LBINARY(9, "testing packing a full Array");
        TestUtils.assertEquals(new byte[] {
                        (byte) 0xF9,
                        0x30, 0x31, 0X32,
                        0X33, 0X34, 0X35,
                        0X36, 0X37, 0X38},
                packager.pack(field));
    }

    @Test
    public void testUnpackZeroLengthArray() throws Exception
    {
        byte[] raw = new byte[] {(byte) 0xF0};
        IFE_LBINARY packager = new IFE_LBINARY(9, "testing unpacking an empty Array");
        ISOBinaryField field = new ISOBinaryField(9);
        packager.unpack(field, raw, 0);
        TestUtils.assertEquals(new byte[]{}, (byte[])field.getValue());
    }

    @Test
    public void testUnpackATwoLengthArray() throws Exception
    {
        byte[] raw = new byte[] {(byte) 0xF2, 0x30, 0x31};
        IFE_LBINARY packager = new IFE_LBINARY(9, "testing unpacking a non empty Array");
        ISOBinaryField field = new ISOBinaryField(9);
        packager.unpack(field, raw, 0);
        TestUtils.assertEquals(new byte[] {0x30, 0x31}, (byte[])field.getValue());
    }

    @Test
    public void testUnpackAMaxLengthArray() throws Exception
    {
        byte[] raw = new byte[] {
                (byte) 0xF9,
                0x30, 0x31, 0X32,
                0X33, 0X34, 0X35,
                0X36, 0X37, 0X38};
        IFE_LBINARY packager = new IFE_LBINARY(9, "testing unpacking a max length Array");
        ISOBinaryField field = new ISOBinaryField(9);
        packager.unpack(field, raw, 0);
        TestUtils.assertEquals(new byte[] {0x30, 0x31, 0X32, 0X33, 0X34, 0X35, 0X36, 0X37, 0X38}, (byte[])field.getValue());
    }

    @Test
    public void testReversability() throws Exception
    {
        IFE_LBINARY packager = new IFE_LBINARY(9, "testing packing and unpacking cycle");

        byte[] originalData = new byte[] {0x12, 0x34, 0x56, 0x78};
        ISOBinaryField originalDataField = new ISOBinaryField(9, originalData);

        byte[] packedData = packager.pack(originalDataField);

        ISOBinaryField unpackedDataField = new ISOBinaryField(9);
        packager.unpack(unpackedDataField, packedData, 0);


        TestUtils.assertEquals(originalData, (byte[]) unpackedDataField.getValue());
    }

    @Test
    public void testUnpackOffsetArray() throws Exception
    {
        byte[] raw = new byte[] {0x30, 0x31, (byte) 0xF2, 0x30, 0x31};
        IFE_LBINARY packager = new IFE_LBINARY(9, "testing unpacking a non empty Array with an offset");
        ISOBinaryField field = new ISOBinaryField(9);
        packager.unpack(field, raw, 2);
        TestUtils.assertEquals(new byte[] {0x30, 0x31}, (byte[])field.getValue());
    }
}
