/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.IFA_LLNUM;
import org.jpos.iso.ISOField;

/**
 * @author joconnor
 */
public class IFA_LLNUMTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "1234");
        IFA_LLNUM packager = new IFA_LLNUM(10, "Should be 041234");
        TestUtils.assertEquals("041234".getBytes(), packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = "041234".getBytes();
        IFA_LLNUM packager = new IFA_LLNUM(10, "Should be 041234");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("1234", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "1234567890";
        ISOField f = new ISOField(12, origin);
        IFA_LLNUM packager = new IFA_LLNUM(10, "Should be 1234567890");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
