/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package iso;

import junit.framework.TestCase;

import org.jpos.iso.ISOField;
import org.jpos.iso.IFB_LLLHCHAR;

/**
 * @author joconnor
 */
public class IFB_LLLHCHARTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(10, "ABCDEFGHIJ");
        IFB_LLLHCHAR packager = new IFB_LLLHCHAR(10, "Should be ABCDEFGHIJ");
        TestUtils.assertEquals(new byte[] {0x00, 0x0A, 0x41, 0x42, 0x43, 0x44,
                                            0x45, 0x46, 0x47, 0x48, 0x49, 0x4A},
            packager.pack(field));
    }

    public void testPackagerTooLong() throws Exception
    {
        try
        {
            new IFB_LLLHCHAR(1000, "Too long for this");
            fail("1000 is too long and should have thrown an exception");
        } catch (Exception ignored)
        {
        }
    }

    public void testPackTooMuch() throws Exception
    {
        ISOField field = new ISOField(10, "ABCDEFGHIJ");
        IFB_LLLHCHAR packager = new IFB_LLLHCHAR(5, "Should be ABCDEFGHIJ");
        try
        {
            packager.pack(field);
            fail("field is too long and should have thrown an exception");
        } catch (Exception ignored)
        {
        }
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[] {0x00, 0x0A, 0x41, 0x42, 0x43, 0x44,
        0x45, 0x46, 0x47, 0x48, 0x49, 0x4A};
        IFB_LLLHCHAR packager = new IFB_LLLHCHAR(10, "Should be ABCDEFGHIJ");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("ABCDEFGHIJ", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "Abc123:.-";
        ISOField f = new ISOField(12, origin);
        IFB_LLLHCHAR packager = new IFB_LLLHCHAR(10, "Should be Abc123:.-");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}

