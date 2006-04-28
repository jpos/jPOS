/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.IFE_LLNUM;
import org.jpos.iso.ISOField;

/**
 * @author joconnor
 */
public class IFE_LLNUMTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "1234");
        IFE_LLNUM packager = new IFE_LLNUM(10, "Should be 041234");
        TestUtils.assertEquals(new byte[] {(byte)0xF0, (byte)0xF4, (byte)0xF1, (byte)0xF2, (byte)0xF3, (byte)0xF4},
                                packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[] {(byte)0xF0, (byte)0xF4, (byte)0xF1, (byte)0xF2, (byte)0xF3, (byte)0xF4};
        IFE_LLNUM packager = new IFE_LLNUM(10, "Should be 041234");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("1234", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "1234567890";
        ISOField f = new ISOField(12, origin);
        IFE_LLNUM packager = new IFE_LLNUM(10, "Should be 1234567890");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
