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

package org.jpos.iso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static org.apache.commons.lang3.JavaVersion.JAVA_1_8;
import static org.apache.commons.lang3.JavaVersion.JAVA_10;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.jpos.iso.packager.Base1_BITMAP126;
import org.junit.jupiter.api.Test;

public class ISOFieldPackagerTest {

    @Test
    public void testCreateComponent() throws Throwable {
        ISOField result = (ISOField) new IFB_AMOUNT().createComponent(100);
        assertNotNull(result);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testPack() throws Throwable {
        ObjectOutput out = new ObjectOutputStream(new ByteArrayOutputStream());
        new IF_NOP().pack(new ISOVField((ISOField) new IFA_LCHAR().createComponent(100), new ISOVError(
                "testISOFieldPackagerDescription")), out);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testPackThrowsNullPointerException() throws Throwable {
        ObjectOutput out = new ObjectOutputStream(new ByteArrayOutputStream());
        try {
            new IF_ECHAR(100, "testISOFieldPackagerDescription").pack(new ISOField(), out);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"str\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testReadBytes() throws Throwable {
        ISOFieldPackager iFB_LLHFBINARY = new IFB_LLHFBINARY(100, "testISOFieldPackagerDescription");
        byte[] bytes = new byte[1];
        byte[] result = iFB_LLHFBINARY.readBytes(new ByteArrayInputStream(bytes), 1);
        assertEquals(1, result.length, "result.length");
        assertEquals((byte) 0, result[0], "result[0]");
    }

    @Test
    public void testReadBytes1() throws Throwable {
        ISOFieldPackager iFB_AMOUNT = new IFB_AMOUNT();
        byte[] result = iFB_AMOUNT.readBytes(new ByteArrayInputStream("testString".getBytes()), 0);
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testReadBytesThrowsEOFException() throws Throwable {
        ISOFieldPackager iFA_BINARY = new IFA_BINARY(100, "testISOFieldPackagerDescription");
        byte[] bytes = new byte[1];
        InputStream in = new ByteArrayInputStream(bytes);
        try {
            iFA_BINARY.readBytes(in, 100);
            fail("Expected EOFException to be thrown");
        } catch (EOFException ex) {
            assertEquals(EOFException.class, ex.getClass(), "ex.getClass()");
            assertEquals(0, in.available(), "(ByteArrayInputStream) in.available()");
        }
    }

    @Test
    public void testReadBytesThrowsNegativeArraySizeException() throws Throwable {
        InputStream in = new ByteArrayInputStream("testString".getBytes());
        ISOFieldPackager iFEB_LLLNUM = new IFEB_LLLNUM();
        try {
            iFEB_LLLNUM.readBytes(in, -1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("-1", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(10, in.available(), "(ByteArrayInputStream) in.available()");
        }
    }

    @Test
    public void testReadBytesThrowsNullPointerException() throws Throwable {
        ISOFieldPackager iFA_FLLCHAR = new IFA_FLLCHAR(0, "testISOFieldPackagerDescription");
        try {
            iFA_FLLCHAR.readBytes(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.InputStream.read(byte[], int, int)\" because \"in\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSetDescription() throws Throwable {
        ISOFieldPackager iFA_LCHAR = new IFA_LCHAR();
        iFA_LCHAR.setDescription("testISOFieldPackagerDescription");
        assertEquals("testISOFieldPackagerDescription", iFA_LCHAR.getDescription(), "(IFA_LCHAR) iFA_LCHAR.getDescription()");
    }

    @Test
    public void testSetLength() throws Throwable {
        ISOFieldPackager iFB_AMOUNT = new IFB_AMOUNT();
        iFB_AMOUNT.setLength(100);
        assertEquals(100, iFB_AMOUNT.getLength(), "(IFB_AMOUNT) iFB_AMOUNT.getLength()");
    }

    @Test
    public void testSetPad() throws Throwable {
        ISOFieldPackager iFA_BINARY = new IFA_BINARY(100, "testISOFieldPackagerDescription");
        iFA_BINARY.setPad(true);
        assertTrue(((IFA_BINARY) iFA_BINARY).pad, "(IFA_BINARY) iFA_BINARY.pad");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testUnpack() throws Throwable {
        ISOComponent c = new ISOField();
        ISOFieldPackager iF_ECHAR = new IF_ECHAR(100, "testISOFieldPackagerDescription");
        byte[] bytes = new byte[0];
        iF_ECHAR.setLength(0);
        iF_ECHAR.unpack(c, new ByteArrayInputStream(bytes));
        assertEquals("", ((ISOField) c).value, "(ISOVField) c.value");
    }

    @Test
    public void testUnpackThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] bytes = new byte[2];
        InputStream in = new ByteArrayInputStream(bytes);
        try {
            new IFEB_LLLNUM().unpack(new ISOMsg("testISOFieldPackagerMti"), in);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("2", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 2 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, in.available(), "(ByteArrayInputStream) in.available()");
        }
    }

    @Test
    public void testUnpackThrowsISOException() throws Throwable {
        ISOFieldPackager iFB_AMOUNT = new IFB_AMOUNT();
        iFB_AMOUNT.setLength(1);
        byte[] bytes = new byte[3];
        InputStream in = new ByteArrayInputStream(bytes);
        try {
            iFB_AMOUNT.unpack(new ISOVMsg(new ISOMsg("testISOFieldPackagerMti"), new ISOVError("testISOFieldPackagerDescription",
                    "testISOFieldPackagerRejectCode")), in);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("setValue N/A in ISOMsg", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertEquals(2, in.available(), "(ByteArrayInputStream) in.available()");
        }
    }

    @Test
    public void testUnpackThrowsNegativeArraySizeException() throws Throwable {
        byte[] bytes = new byte[0];
        InputStream in = new ByteArrayInputStream(bytes);
        try {
            new Base1_BITMAP126(-1, "testISOFieldPackagerDescription").unpack(new IFB_AMOUNT().createComponent(100), in);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("-1", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, in.available(), "(ByteArrayInputStream) in.available()");
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException() throws Throwable {
        ISOFieldPackager iFB_AMOUNT = new IFB_AMOUNT();
        iFB_AMOUNT.setLength(100);
        try {
            iFB_AMOUNT.unpack(new ISOBinaryField(), null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.InputStream.read(byte[], int, int)\" because \"in\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUnpackThrowsStringIndexOutOfBoundsException() throws Throwable {
        ISOFieldPackager iFB_AMOUNT = new IFB_AMOUNT();
        InputStream in = new ByteArrayInputStream("testString".getBytes());
        iFB_AMOUNT.setLength(-1);
        try {
            iFB_AMOUNT.unpack(new ISOMsg(), in);
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_1_8)) {
                assertEquals("String index out of range: 1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("offset 0, count 1, length 0", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(10, in.available(), "(ByteArrayInputStream) in.available()");
        }
    }
}
