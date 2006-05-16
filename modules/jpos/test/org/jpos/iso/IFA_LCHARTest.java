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
public class IFA_LCHARTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCD");
        IFA_LCHAR packager = new IFA_LCHAR(8, "Should be 4ABCD");
        TestUtils.assertEquals("4ABCD".getBytes(), packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = "4ABCD".getBytes();
        IFA_LCHAR packager = new IFA_LCHAR(8, "Should be 4ABCD");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("ABCD", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "Abc123";
        ISOField f = new ISOField(12, origin);
        IFA_LCHAR packager = new IFA_LCHAR(8, "Should be Abc123");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
