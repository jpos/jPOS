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
 * @author apr
 */
public class IFE_AMOUNTTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "D123");
        IFE_AMOUNT packager = new IFE_AMOUNT(6, "Should be C4F0F0F1F2F3");
        TestUtils.assertEquals(
            ISOUtil.hex2byte ("C4F0F0F1F2F3"),
            packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{ 68, 48, 48, 49, 50, 51};
        IFE_AMOUNT packager = new IFE_AMOUNT(6, "Should be C4F0F0F1F2F3");
        ISOField field = new ISOField();
        packager.unpack(field, ISOUtil.hex2byte ("C4F0F0F1F2F3"), 0);
        assertEquals("D00123", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "D0000123";
        ISOField f = new ISOField(12, origin);
        IFE_AMOUNT packager = new IFE_AMOUNT(8, "Should be C4F0F0F1F2F3");

        ISOField unpack = new ISOField();
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}

