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
public class IFE_CHARTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCD");
        IFE_CHAR packager = new IFE_CHAR(10, "Should be ABCD      ");
        TestUtils.assertEquals(new byte[]{(byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40}, packager.pack(
                field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{(byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40};
        IFE_CHAR packager = new IFE_CHAR(10, "Should be ABCD      ");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("ABCD      ", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "Abc123:.-";
        ISOField f = new ISOField(12, origin);
        IFE_CHAR packager = new IFE_CHAR(10, "Should be ABCD      ");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin + " ", (String) unpack.getValue());
    }
}
