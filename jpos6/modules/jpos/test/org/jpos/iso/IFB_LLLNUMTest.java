/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.IFB_LLLNUM;
import org.jpos.iso.ISOField;

/**
 * @author joconnor
 */
public class IFB_LLLNUMTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "1234");
        IFB_LLLNUM packager = new IFB_LLLNUM(10, "Should be 00041234", true);
        TestUtils.assertEquals(new byte[]{0x00, 0x04, 0x12, 0x34}, packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{0x00, 0x04, 0x12, 0x34};
        IFB_LLLNUM packager = new IFB_LLLNUM(10, "Should be 00041234", true);
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("1234", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "1234567890";
        ISOField f = new ISOField(12, origin);
        IFB_LLLNUM packager = new IFB_LLLNUM(10, "Should be 1234567890", true);

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
