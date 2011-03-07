package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

import org.jpos.iso.ISOException;
import org.jpos.iso.X92_BITMAP;
import org.junit.Test;

public class X92GenericPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        X92GenericPackager x92GenericPackager = new X92GenericPackager();
        assertNull("x92GenericPackager.getLogger()", x92GenericPackager.getLogger());
        assertNull("x92GenericPackager.getRealm()", x92GenericPackager.getRealm());
    }

    @Test
    public void testConstructorThrowsISOException() throws Throwable {
        byte[] bytes = new byte[1];
        try {
            new X92GenericPackager(new ByteArrayInputStream(bytes));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "org.xml.sax.SAXParseException: Content is not allowed in prolog.", ex.getMessage());
            assertEquals("ex.getNested().getMessage()", "Content is not allowed in prolog.", ex.getNested().getMessage());
        }
    }

    @Test
    public void testConstructorThrowsISOException1() throws Throwable {
        try {
            new X92GenericPackager("testX92GenericPackagerFilename");
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getNested().getClass()", FileNotFoundException.class, ex.getNested().getClass());
        }
    }

    @Test
    public void testEmitBitMap() throws Throwable {
        X92GenericPackager x92GenericPackager = new X92GenericPackager();
        boolean result = x92GenericPackager.emitBitMap();
        assertTrue("result", result);
    }

    @Test
    public void testGetBitMapfieldPackager() throws Throwable {
        X92GenericPackager x92GenericPackager = new X92GenericPackager();
        X92_BITMAP result = (X92_BITMAP) x92GenericPackager.getBitMapfieldPackager();
        assertEquals("result.getMaxPackedLength()", 4, result.getMaxPackedLength());
    }

    @Test
    public void testGetMaxValidField() throws Throwable {
        X92GenericPackager x92GenericPackager = new X92GenericPackager();
        int result = x92GenericPackager.getMaxValidField();
        assertEquals("result", 64, result);
    }
}
