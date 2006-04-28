/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.IF_CHAR;
import org.jpos.iso.ISOField;

/**
 * @author joconnor
 */
public class IF_CHARTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCD");
        IF_CHAR packager = new IF_CHAR(10, "Should be ABCD      ");
        TestUtils.assertEquals(new byte[]{65, 66, 67, 68, 32, 32, 32, 32, 32, 32}, packager.pack(
                field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{65, 66, 67, 68, 32, 32, 32, 32, 32, 32};
        IF_CHAR packager = new IF_CHAR(10, "Should be ABCD      ");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("ABCD      ", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "Abc123:.-";
        ISOField f = new ISOField(12, origin);
        IF_CHAR packager = new IF_CHAR(10, "Should be ABCD      ");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin + " ", (String) unpack.getValue());
    }
}
