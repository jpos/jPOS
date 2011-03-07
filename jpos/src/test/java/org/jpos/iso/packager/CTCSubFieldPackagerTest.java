package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jpos.iso.IFA_LLLLCHAR;
import org.jpos.iso.IFE_CHAR;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOValidator;
import org.jpos.util.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CTCSubFieldPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
        assertNull("cTCSubFieldPackager.getLogger()", cTCSubFieldPackager.getLogger());
        assertNull("cTCSubFieldPackager.getRealm()", cTCSubFieldPackager.getRealm());
    }

    @Test
    public void testEmitBitMap() throws Throwable {
        CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
        boolean result = cTCSubFieldPackager.emitBitMap();
        assertFalse("result", result);
    }

    @Test
    public void testPack1() throws Throwable {
        CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[1];
        cTCSubFieldPackager.setFieldPackager(fld);
        byte[] result = cTCSubFieldPackager.pack(new ISOField());
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testPackThrowsISOException() throws Throwable {
        try {
            new CTCSubFieldPackager().pack(null);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "null: null", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testPackThrowsISOException1() throws Throwable {
        try {
            new CTCSubFieldPackager().pack(new ISOBinaryField(100));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "null: null", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testPackThrowsISOException2() throws Throwable {
        CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[1];
        cTCSubFieldPackager.setFieldPackager(fld);
        try {
            cTCSubFieldPackager.pack(new ISOMsg("testCTCSubFieldPackagerMti"));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "null: null", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testUnpack() throws Throwable {
        byte[] b = new byte[0];
        int result = new CTCSubFieldPackager().unpack(new ISOField(), b);
        assertEquals("result", 0, result);
    }

    @Test
    public void testUnpackThrowsISOException() throws Throwable {
        CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[6];
        fld[0] = new IFA_LLLLCHAR(100, "testCTCSubFieldPackagerDescription");
        cTCSubFieldPackager.setFieldPackager(fld);
        byte[] b = new byte[4];
        try {
            cTCSubFieldPackager.unpack(new ISOField(), b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "org.jpos.iso.IFA_LLLLCHAR: Problem unpacking field 0", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testUnpackThrowsNullPointerException() throws Throwable {
        ISOFieldPackager[] fld = new ISOFieldPackager[4];
        fld[0] = new IFE_CHAR();
        CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
        cTCSubFieldPackager.setFieldPackager(fld);
        byte[] b = new byte[2];
        cTCSubFieldPackager.unpack(null, b);
    }

    @Test(expected = NullPointerException.class)
    public void testUnpackThrowsNullPointerException2() throws Throwable {
        new CTCSubFieldPackager().unpack(new ISOBinaryField(100), (byte[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testValidateThrowsNullPointerException1() throws Throwable {
        new CTCSubFieldPackager().validate(null);
    }

    @Test(expected = NullPointerException.class)
    public void testValidateThrowsNullPointerException2() throws Throwable {
        new CTCSubFieldPackager().validate(new ISOMsg());
    }

    @Test(expected = NullPointerException.class)
    public void testValidateThrowsNullPointerException3() throws Throwable {
        CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
        cTCSubFieldPackager.setLogger(Logger.getLogger(""), "testCTCSubFieldPackagerRealm");
        ISOValidator[] fvlds = new ISOValidator[2];
        cTCSubFieldPackager.setFieldValidator(fvlds);
        cTCSubFieldPackager.validate(new ISOMsg("testCTCSubFieldPackagerMti"));
    }
}
