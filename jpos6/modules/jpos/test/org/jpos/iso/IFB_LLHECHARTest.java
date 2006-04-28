/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.IFB_LLHECHAR;
import org.jpos.iso.ISOField;

/**
 * @author joconnor
 */
public class IFB_LLHECHARTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCD");
        IFB_LLHECHAR packager = new IFB_LLHECHAR(10, "Should be 04ABCD");
        TestUtils.assertEquals(new byte[] {(byte)0x04, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4},
                            packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[] {(byte)0x04, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4};
        IFB_LLHECHAR packager = new IFB_LLHECHAR(10, "Should be 04ABCD");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("ABCD", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "Abc123:.-";
        ISOField f = new ISOField(12, origin);
        IFB_LLHECHAR packager = new IFB_LLHECHAR(10, "Should be Abc123:.-");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
