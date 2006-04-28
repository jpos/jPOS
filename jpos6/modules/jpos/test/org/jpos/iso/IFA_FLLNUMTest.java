/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.IFA_FLLNUM;
import org.jpos.iso.ISOField;

/**
 * @author joconnor
 */
public class IFA_FLLNUMTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "1234");
        IFA_FLLNUM packager = new IFA_FLLNUM(10, "Should be 04ABCD");
        TestUtils.assertEquals("041234      ".getBytes(), packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = "041234      ".getBytes();
        IFA_FLLNUM packager = new IFA_FLLNUM(10, "Should be 04ABCD");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("1234", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "1234056789";
        ISOField f = new ISOField(12, origin);
        IFA_FLLNUM packager = new IFA_FLLNUM(10, "Should be Abc123:.-");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
