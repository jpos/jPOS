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
 * @author joconnor
 */
public class IFB_LLLBINARYTest {
    @Test
    public void testPack() throws Exception
    {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x12, 0x34});
        IFB_LLLBINARY packager = new IFB_LLLBINARY(100, "Should be 1234");
        TestUtils.assertEquals(new byte[] {0x00, 0x02, 0x12, 0x34}, packager.pack(field));
    }

    @Test
    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[] {0x00, 0x02, 0x12, 0x34};
        IFB_LLLBINARY packager = new IFB_LLLBINARY(100, "Should be 1234");
        ISOBinaryField field = new ISOBinaryField(12);
        packager.unpack(field, raw, 0);
        TestUtils.assertEquals(new byte[] {0x12, 0x34}, (byte[])field.getValue());
    }

    @Test
    public void testReversability() throws Exception
    {
        byte[] origin = new byte[] {0x12, 0x34, 0x56, 0x78};
        ISOBinaryField f = new ISOBinaryField(12, origin);
        IFB_LLLBINARY packager = new IFB_LLLBINARY(100, "Should be 12345678");

        ISOBinaryField unpack = new ISOBinaryField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        TestUtils.assertEquals(origin, (byte[])unpack.getValue());
    }
    
    @Test
    public void testPackGreaterThan16() throws Exception
    {
        ISOBinaryField field = new ISOBinaryField(1, new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 ,0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11});
        IFB_LLLBINARY packager = new IFB_LLLBINARY(1, "Should be 1234");
        TestUtils.assertEquals(new byte[] {0x00, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 ,0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11}, packager.pack(field));
    }

    @Test
    public void testUnpackGreaterThan16() throws Exception
    {
        byte[] raw = new byte[] {0x00, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 ,0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11};
        IFB_LLLBINARY packager = new IFB_LLLBINARY(999, "Should be 17 bytes 01 through 11");
        ISOBinaryField field = new ISOBinaryField(1);
        packager.unpack(field, raw, 0);
        TestUtils.assertEquals(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 ,0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11}, (byte[])field.getValue());
    }

    @Test
    public void testReversabilityGreaterThan16() throws Exception
    {
        byte[] origin = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 ,0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11};
        ISOBinaryField f = new ISOBinaryField(1, origin);
        IFB_LLLBINARY packager = new IFB_LLLBINARY(999, "Should be x'0102030405060708090a0b0c0d0e0f1011'");

        ISOBinaryField unpack = new ISOBinaryField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        TestUtils.assertEquals(origin, (byte[])unpack.getValue());
    }
}
