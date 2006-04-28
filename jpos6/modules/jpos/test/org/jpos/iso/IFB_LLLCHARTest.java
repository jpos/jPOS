/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.IFB_LLLCHAR;
import org.jpos.iso.ISOField;

/**
 * @author joconnor
 */
public class IFB_LLLCHARTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCD");
        IFB_LLLCHAR packager = new IFB_LLLCHAR(10, "Should be 004ABCD");
        TestUtils.assertEquals(new byte[] {0x00, 0x04, 0x41, 0x42, 0x43, 0x44}, packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[] {0x00, 0x04, 0x41, 0x42, 0x43, 0x44};
        IFB_LLLCHAR packager = new IFB_LLLCHAR(10, "Should be 04ABCD");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("ABCD", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "Abc123:.-";
        ISOField f = new ISOField(12, origin);
        IFB_LLLCHAR packager = new IFB_LLLCHAR(10, "Should be Abc123:.-");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
