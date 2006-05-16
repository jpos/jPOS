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
public class IFB_LLNUMTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "1234");
        IFB_LLNUM packager = new IFB_LLNUM(10, "Should be 041234", true);
        TestUtils.assertEquals(new byte[]{0x04, 0x12, 0x34}, packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{0x04, 0x12, 0x34};
        IFB_LLNUM packager = new IFB_LLNUM(10, "Should be 041234", true);
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("1234", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "1234567890";
        ISOField f = new ISOField(12, origin);
        IFB_LLNUM packager = new IFB_LLNUM(10, "Should be 1234567890", true);

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
