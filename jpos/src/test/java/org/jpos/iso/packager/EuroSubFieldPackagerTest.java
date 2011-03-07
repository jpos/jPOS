package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jpos.iso.IFB_LLLCHAR;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOMsg;
import org.junit.Test;

public class EuroSubFieldPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        EuroSubFieldPackager euroSubFieldPackager = new EuroSubFieldPackager();
        assertNull("euroSubFieldPackager.getLogger()", euroSubFieldPackager.getLogger());
        assertNull("euroSubFieldPackager.getRealm()", euroSubFieldPackager.getRealm());
    }

    @Test
    public void testEmitBitMap() throws Throwable {
        EuroSubFieldPackager euroSubFieldPackager = new EuroSubFieldPackager();
        boolean result = euroSubFieldPackager.emitBitMap();
        assertFalse("result", result);
    }

    @Test
    public void testPack() throws Throwable {
        EuroSubFieldPackager euroSubFieldPackager = new EuroSubFieldPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[4];
        euroSubFieldPackager.setFieldPackager(fld);
        byte[] result = euroSubFieldPackager.pack(new ISOMsg(100));
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testPack3() throws Throwable {
        EuroSubFieldPackager euroSubFieldPackager = new EuroSubFieldPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[1];
        euroSubFieldPackager.setFieldPackager(fld);
        byte[] result = euroSubFieldPackager.pack(new ISOField());
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testPackThrowsISOException1() throws Throwable {
        try {
            new EuroSubFieldPackager().pack(new ISOMsg("testEuroSubFieldPackagerMti"));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testPackThrowsISOException2() throws Throwable {
        try {
            new EuroSubFieldPackager().pack(null);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException() throws Throwable {
        byte[] b = new byte[3];
        try {
            new EuroSubFieldPackager().unpack(new ISOBinaryField(100), b);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException1() throws Throwable {
        EuroSubFieldPackager euroSubFieldPackager = new EuroSubFieldPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[4];
        fld[0] = new IFB_LLLCHAR();
        euroSubFieldPackager.setFieldPackager(fld);
        byte[] b = new byte[3];
        try {
            euroSubFieldPackager.unpack(null, b);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
