/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.IFA_NUMERIC;
import org.jpos.iso.ISOField;

/**
 * @author joconnor
 */
public class IFA_NUMERICTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "1234");
        IFA_NUMERIC packager = new IFA_NUMERIC(10, "Should be 0000001234");
        TestUtils.assertEquals("0000001234".getBytes(), packager.pack(field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = "0000001234".getBytes();
        IFA_NUMERIC packager = new IFA_NUMERIC(10, "Should be 0000001234");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("0000001234", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "1234567890";
        ISOField f = new ISOField(12, origin);
        IFA_NUMERIC packager = new IFA_NUMERIC(10, "Should be 1234567890");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
