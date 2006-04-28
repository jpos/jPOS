/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.IF_UNUSED;
import org.jpos.iso.ISOField;

/**
 * @author joconnor
 */
public class IF_UNUSEDTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCD");
        IF_UNUSED packager = new IF_UNUSED();
        try
        {
            packager.pack(field);
            fail("Should have thrown an exception");
        } catch (Exception expected)
        {
        }
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{};
        IF_UNUSED packager = new IF_UNUSED();
        ISOField field = new ISOField(12);
        try
        {
            packager.unpack(field, raw, 0);
            fail("Should have thrown an exception");
        } catch (Exception expected)
        {
        }
    }
}
