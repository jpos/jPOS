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
public class IFB_LLLHECHARTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCD");
        IFB_LLLHECHAR packager = new IFB_LLLHECHAR(10, "Should be 04ABCD");
        TestUtils.assertEquals(new byte[] {0x00, 0x04, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4},
                            packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[] {0x00, 0x04, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4};
        IFB_LLLHECHAR packager = new IFB_LLLHECHAR(10, "Should be 04ABCD");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("ABCD", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "Abc123:.-";
        ISOField f = new ISOField(12, origin);
        IFB_LLLHECHAR packager = new IFB_LLLHECHAR(10, "Should be Abc123:.-");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
