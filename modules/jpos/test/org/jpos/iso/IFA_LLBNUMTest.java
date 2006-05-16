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
public class IFA_LLBNUMTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "123");
        IFA_LLBNUM packager = new IFA_LLBNUM(10, "Should be 0x30340123", true);
        TestUtils.assertEquals(new byte[]{0x30, 0x34, 0x01, 0x23}, packager.pack(
                field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{0x30, 0x34, 0x01, 0x23};
        IFA_LLBNUM packager = new IFA_LLBNUM(10, "Should be 0x30330123", true);
        ISOField field = new ISOField();
        packager.unpack(field, raw, 0);
        assertEquals("0123", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "123456789";
        ISOField f = new ISOField(12, origin);
        IFA_LLBNUM packager = new IFA_LLBNUM(10, "Should be 0x31301234567890", false);

        ISOField unpack = new ISOField();
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin + "0", (String) unpack.getValue());
    }
}
