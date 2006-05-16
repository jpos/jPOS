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
public class IFA_BINARYTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x12, 0x34});
        IFA_BINARY packager = new IFA_BINARY(2, "Should be 1234");
        TestUtils.assertEquals("1234".getBytes(), packager.pack(field));
    }

    public void testPackWrongLength() throws Exception
    {
        try
        {
            ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x12, 0x34});
            IFA_BINARY packager = new IFA_BINARY(3, "Should be 1234");
            packager.pack(field);
            fail("Packing 2 bytes into 3 should throw an exception");
        }
        catch (Exception ignored)
        {
            // Exception expected - correct behaviour
        }
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = "1234".getBytes();
        IFA_BINARY packager = new IFA_BINARY(2, "Should be 1234");
        ISOBinaryField field = new ISOBinaryField(12);
        packager.unpack(field, raw, 0);
        TestUtils.assertEquals(new byte[] {0x12, 0x34}, (byte[])field.getValue());
    }

    public void testReversability() throws Exception
    {
        byte[] origin = new byte[] {0x12, 0x34, 0x56, 0x78};
        ISOBinaryField f = new ISOBinaryField(12, origin);
        IFA_BINARY packager = new IFA_BINARY(4, "Should be 12345678");

        ISOBinaryField unpack = new ISOBinaryField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        TestUtils.assertEquals(origin, (byte[])unpack.getValue());
    }
}
