/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.IFE_LLLBINARY;
import org.jpos.iso.ISOBinaryField;

/**
 * @author joconnor
 */
public class IFE_LLLBINARYTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x30, 0x31});
        IFE_LLLBINARY packager = new IFE_LLLBINARY(100, "Should be 1234");
        TestUtils.assertEquals(new byte[] {(byte)0xF0, (byte)0xF0, (byte)0xF2, (byte)0x30, (byte)0x31},
                                packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[] {(byte)0xF0, (byte)0xF0, (byte)0xF2, (byte)0x30, (byte)0x31};
        IFE_LLLBINARY packager = new IFE_LLLBINARY(100, "Should be 1234");
        ISOBinaryField field = new ISOBinaryField(12);
        packager.unpack(field, raw, 0);
        TestUtils.assertEquals(new byte[] {0x30, 0x31}, (byte[])field.getValue());
    }

    public void testReversability() throws Exception
    {
        byte[] origin = new byte[] {0x12, 0x34, 0x56, 0x78};
        ISOBinaryField f = new ISOBinaryField(12, origin);
        IFE_LLLBINARY packager = new IFE_LLLBINARY(100, "Should be 12345678");

        ISOBinaryField unpack = new ISOBinaryField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        TestUtils.assertEquals(origin, (byte[])unpack.getValue());
    }
}
