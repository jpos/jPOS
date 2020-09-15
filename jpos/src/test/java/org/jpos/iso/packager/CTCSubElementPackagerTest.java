/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso.packager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import static org.apache.commons.lang3.JavaVersion.JAVA_10;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

import org.jpos.iso.IFA_AMOUNT;
import org.jpos.iso.IFA_LCHAR;
import org.jpos.iso.IFA_LLBNUM;
import org.jpos.iso.IFE_CHAR;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOVField;
import org.jpos.util.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CTCSubElementPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        CTCSubElementPackager cTCSubElementPackager = new CTCSubElementPackager();
        assertNull(cTCSubElementPackager.getLogger(), "cTCSubElementPackager.getLogger()");
        assertNull(cTCSubElementPackager.getRealm(), "cTCSubElementPackager.getRealm()");
    }

    @Test
    public void testEmitBitMap() throws Throwable {
        CTCSubElementPackager cTCSubElementPackager = new CTCSubElementPackager();
        boolean result = cTCSubElementPackager.emitBitMap();
        assertFalse(result, "result");
    }

    @Test
    public void testPack() throws Throwable {
        CTCSubElementPackager cTCSubElementPackager = new CTCSubElementPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[2];
        cTCSubElementPackager.setFieldPackager(fld);
        byte[] result = cTCSubElementPackager.pack(new ISOBinaryField());
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testPack1() throws Throwable {
        CTCSubElementPackager cTCSubElementPackager = new CTCSubElementPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[0];
        cTCSubElementPackager.setFieldPackager(fld);
        byte[] result = cTCSubElementPackager.pack(new ISOMsg());
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testPackThrowsISOException() throws Throwable {
        CTCSubElementPackager cTCSubElementPackager = new CTCSubElementPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[2];
        cTCSubElementPackager.setFieldPackager(fld);
        try {
            cTCSubElementPackager.pack(new ISOMsg("testCTCSubElementPackagerMti"));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals(ClassCastException.class, ex.getNested().getClass(), "ex.getNested().getClass()");
        }
    }

    @Test
    public void testPackThrowsISOException1() throws Throwable {
        try {
            new CTCSubElementPackager().pack(null);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertEquals("null:null", ex.getMessage(), "ex.getMessage()");
                assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            } else {
                assertEquals("null:Cannot invoke \"org.jpos.iso.ISOComponent.getChildren()\" because \"c\" is null", ex.getMessage(), "ex.getMessage()");
                assertEquals("Cannot invoke \"org.jpos.iso.ISOComponent.getChildren()\" because \"c\" is null", ex.getNested().getMessage(), "ex.getNested().getMessage()");
            }
        }
    }

    @Test
    public void testPackThrowsISOException2() throws Throwable {
        try {
            new CTCSubElementPackager().pack(new ISOBinaryField(100));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertEquals("null:null", ex.getMessage(), "ex.getMessage()");
                assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            } else {
                assertEquals("null:Cannot read the array length because \"this.fld\" is null", ex.getMessage(), "ex.getMessage()");
                assertEquals("Cannot read the array length because \"this.fld\" is null", ex.getNested().getMessage(), "ex.getNested().getMessage()");
            }
        }
    }

    @Test
    public void testUnpackThrowsArrayIndexOutOfBoundsException() throws Throwable {
        CTCSubElementPackager cTCSubElementPackager = new CTCSubElementPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[0];
        cTCSubElementPackager.setFieldPackager(fld);
        byte[] b = new byte[1];
        try {
            cTCSubElementPackager.unpack(new ISOMsg(), b);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("0", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 0 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUnpackThrowsISOException() throws Throwable {
        CTCSubElementPackager cTCSubElementPackager = new CTCSubElementPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[2];
        cTCSubElementPackager.setFieldPackager(fld);
        cTCSubElementPackager.setLogger(new Logger(), "testCTCSubElementPackagerRealm");
        cTCSubElementPackager.setFieldPackager(0, new IFE_CHAR());
        byte[] b = new byte[2];
        try {
            cTCSubElementPackager.unpack(new ISOVField(new ISOField(100, "testCTCSubElementPackagerv"), null), b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("Can't add to Leaf", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testUnpackThrowsISOException1() throws Throwable {
        CTCSubElementPackager cTCSubElementPackager = new CTCSubElementPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[2];
        cTCSubElementPackager.setFieldPackager(fld);
        cTCSubElementPackager.setFieldPackager(0, new IFA_LCHAR());
        byte[] b = new byte[3];
        try {
            cTCSubElementPackager.unpack(new ISOMsg(), b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("org.jpos.iso.IFA_LCHAR: Problem unpacking field 0", ex.getMessage(), "ex.getMessage()");
            assertEquals("Invalid character found. Expected digit.", ex.getNested().getMessage(), "ex.getNested().getMessage()");
        }
    }

    @Test
    public void testUnpackThrowsISOExceptionOnBadInputData() throws Throwable {
        byte[] b = new byte[3];
        ISOFieldPackager[] fld = new ISOFieldPackager[3];
        fld[0] = new IFA_LLBNUM();
        CTCSubElementPackager cTCSubElementPackager = new CTCSubElementPackager();
        cTCSubElementPackager.setFieldPackager(fld);
        try {
            cTCSubElementPackager.unpack(new ISOBinaryField(), b);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (ISOException ex) {
            assertEquals("Invalid character found. Expected digit.", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException() throws Throwable {
        byte[] b = new byte[3];
        try {
            new CTCSubElementPackager().unpack(new ISOField(100), b);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot load from object array because \"this.fld\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException1() throws Throwable {
        try {
            new CTCSubElementPackager().unpack(new ISOMsg(), (byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read the array length because \"b\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException2() throws Throwable {
        CTCSubElementPackager cTCSubElementPackager = new CTCSubElementPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[2];
        fld[0] = new IFA_AMOUNT();
        cTCSubElementPackager.setFieldPackager(fld);
        byte[] b = new byte[3];
        ISOComponent m = new ISOMsg("testCTCSubElementPackagerMti");
        try {
            cTCSubElementPackager.unpack(m, b);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.iso.ISOFieldPackager.createComponent(int)\" because \"this.fld[i]\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

}
