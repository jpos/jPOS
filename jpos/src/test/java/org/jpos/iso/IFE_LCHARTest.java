/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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
import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * @author mouslih.abdelhakim@gmail.com
 */
public class IFE_LCHARTest {
    @Test
    public void testPackAnEmptyArray() throws Exception
    {
        ISOField field = new ISOField(9, "");
        IFE_LCHAR packager = new IFE_LCHAR(9, "testing packing an empty Array");
        TestUtils.assertEquals(new byte[] {(byte)0xF0},
                packager.pack(field));
    }

    @Test
    public void testPackANonEmptyArray() throws Exception
    {
        ISOField field = new ISOField(9, "AB");
        IFE_LCHAR packager = new IFE_LCHAR(9, "testing packing a non empty Array");
        TestUtils.assertEquals(new byte[] {(byte) 0xF2, (byte) 0xC1, (byte) 0xC2},
                packager.pack(field));
    }

    @Test
    public void testPackAFullArray() throws Exception
    {
        ISOField field = new ISOField(9, "ABCDEFGHI");
        IFE_LCHAR packager = new IFE_LCHAR(9, "testing packing a full Array");
        TestUtils.assertEquals(new byte[] {
                        (byte) 0xF9,
                        (byte) 0xC1, (byte) 0xC2, (byte) 0xC3,
                        (byte) 0xC4, (byte) 0xC5, (byte) 0xC6,
                        (byte) 0xC7, (byte) 0xC8, (byte) 0xC9},
                packager.pack(field));
    }

    @Test
    public void testUnpackZeroLengthArray() throws Exception
    {
        byte[] raw = new byte[] {(byte) 0xF0};
        IFE_LCHAR packager = new IFE_LCHAR(9, "testing unpacking an empty Array");
        ISOField field = new ISOField(9);
        packager.unpack(field, raw, 0);
        assertEquals("",  field.getValue());
    }

    @Test
    public void testUnpackATwoLengthArray() throws Exception
    {
        byte[] raw = new byte[] {(byte) 0xF2, (byte) 0xC1, (byte) 0xC2};
        IFE_LCHAR packager = new IFE_LCHAR(9, "testing unpacking a non empty Array");
        ISOField field = new ISOField(9);
        packager.unpack(field, raw, 0);
        assertEquals("AB",  field.getValue());
    }

    @Test
    public void testUnpackAMaxLengthArray() throws Exception
    {
        byte[] raw = new byte[] {
                (byte) 0xF9,
                (byte) 0xC1, (byte) 0xC2, (byte) 0xC3,
                (byte) 0xC4, (byte) 0xC5, (byte) 0xC6,
                (byte) 0xC7, (byte) 0xC8, (byte) 0xC9};
        IFE_LCHAR packager = new IFE_LCHAR(9, "testing unpacking a max length Array");
        ISOField field = new ISOField(9);
        packager.unpack(field, raw, 0);
        assertEquals( "ABCDEFGHI" , field.getValue());
    }

    @Test
    public void testReversability() throws Exception
    {
        IFE_LCHAR packager = new IFE_LCHAR(9, "testing packing and unpacking cycle");

        String originalData = "ABCD";
        ISOField originalDataField = new ISOField(9, originalData);

        byte[] packedData = packager.pack(originalDataField);

        ISOField unpackedDataField = new ISOField(9);
        packager.unpack(unpackedDataField, packedData, 0);


        assertEquals(originalData, unpackedDataField.getValue());
    }

    @Test
    public void testUnpackOffsetArray() throws Exception
    {
        byte[] raw = new byte[] {0x30, 0x31, (byte) 0xF2, (byte) 0xC1, (byte) 0xC1};
        IFE_LCHAR packager = new IFE_LCHAR(9, "testing unpacking a non empty Array with an offset");
        ISOField field = new ISOField(9);
        packager.unpack(field, raw, 2);
        assertEquals("AA", field.getValue());
    }
}
