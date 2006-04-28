/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.IF_NOP;
import org.jpos.iso.ISOField;

/**
 * @author joconnor
 */
public class IF_NOPTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCD");
        IF_NOP packager = new IF_NOP();
        assertTrue(packager.pack(field).length == 0);
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{};
        IF_NOP packager = new IF_NOP();
        ISOField field = new ISOField(12);
        assertEquals(0, packager.unpack(field, raw, 0));
        assertNull(field.getValue());
    }
}
