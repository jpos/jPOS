/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

/**
 * @author joconnor
 */
public class IFB_BINARYTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x12, 0x34});
        IFB_BINARY packager = new IFB_BINARY(2, "Should be 1234");
        TestUtils.assertEquals(new byte[] {0x12, 0x34}, packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[] {0x12, 0x34};
        IFB_BINARY packager = new IFB_BINARY(2, "Should be 1234");
        ISOBinaryField field = new ISOBinaryField(12);
        packager.unpack(field, raw, 0);
        TestUtils.assertEquals(new byte[] {0x12, 0x34}, (byte[])field.getValue());
    }

    public void testReversability() throws Exception
    {
        byte[] origin = new byte[] {0x12, 0x34, 0x56, 0x78};
        ISOBinaryField f = new ISOBinaryField(12, origin);
        IFB_BINARY packager = new IFB_BINARY(4, "Should be 12345678");

        ISOBinaryField unpack = new ISOBinaryField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        TestUtils.assertEquals(origin, (byte[])unpack.getValue());
    }
}
