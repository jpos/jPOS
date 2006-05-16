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
public class IFA_AMOUNTTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "D123");
        IFA_AMOUNT packager = new IFA_AMOUNT(6, "Should be D00123");
        TestUtils.assertEquals(new byte[]{68, 48, 48, 49, 50, 51}, packager.pack(
                field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{68, 48, 48, 49, 50, 51};
        IFA_AMOUNT packager = new IFA_AMOUNT(6, "Should be D00123");
        ISOField field = new ISOField();
        packager.unpack(field, raw, 0);
        assertEquals("D00123", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "E0123456";
        ISOField f = new ISOField(12, origin);
        IFA_AMOUNT packager = new IFA_AMOUNT(8, "Should be E0123456");

        ISOField unpack = new ISOField();
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
