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
public class IFA_LLLLLCHARTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCD");
        IFA_LLLLLCHAR packager = new IFA_LLLLLCHAR(10, "Should be 00004ABCD");
        TestUtils.assertEquals("00004ABCD".getBytes(), packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = "00004ABCD".getBytes();
        IFA_LLLLLCHAR packager = new IFA_LLLLLCHAR(10, "Should be 00004ABCD");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("ABCD", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "Abc123:.-";
        ISOField f = new ISOField(12, origin);
        IFA_LLLLLCHAR packager = new IFA_LLLLLCHAR(10, "Should be Abc123:.-");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
