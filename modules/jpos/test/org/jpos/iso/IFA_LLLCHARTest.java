/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.IFA_LLLCHAR;
import org.jpos.iso.ISOField;

/**
 * @author joconnor
 */
public class IFA_LLLCHARTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCD");
        IFA_LLLCHAR packager = new IFA_LLLCHAR(10, "Should be 004ABCD");
        TestUtils.assertEquals("004ABCD".getBytes(), packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = "004ABCD".getBytes();
        IFA_LLLCHAR packager = new IFA_LLLCHAR(10, "Should be 004ABCD");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("ABCD", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "Abc123:.-";
        ISOField f = new ISOField(12, origin);
        IFA_LLLCHAR packager = new IFA_LLLCHAR(10, "Should be Abc123:.-");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
