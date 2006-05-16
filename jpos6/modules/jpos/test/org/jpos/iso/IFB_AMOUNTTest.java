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
public class IFB_AMOUNTTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "D123");
        IFB_AMOUNT packager = new IFB_AMOUNT(6, "Should be D00123", true);
        TestUtils.assertEquals(new byte[]{68, 0x00, 0x01, 0x23}, packager.pack(field));
    }

    public void testPackOddDigits() throws Exception
    {
        ISOField field = new ISOField(12, "D123");
        IFB_AMOUNT packager = new IFB_AMOUNT(5, "Should be D0123", true);
        TestUtils.assertEquals(new byte[]{68, 0x01, 0x23}, packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{68, 0x00, 0x01, 0x23};
        IFB_AMOUNT packager = new IFB_AMOUNT(6, "Should be D00123", true);
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("D00123", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "E0123456";
        ISOField f = new ISOField(12, origin);
        IFB_AMOUNT packager = new IFB_AMOUNT(8, "Should be E0123456", true);

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
