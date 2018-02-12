/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jpos.iso.IFA_LCHAR;
import org.jpos.iso.IFA_LLLLCHAR;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Logger;
import org.junit.Test;

public class GenericSubFieldPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        GenericSubFieldPackager genericSubFieldPackager = new GenericSubFieldPackager();
        assertNull("genericSubFieldPackager.getLogger()", genericSubFieldPackager.getLogger());
        assertNull("genericSubFieldPackager.getRealm()", genericSubFieldPackager.getRealm());
    }

    @Test
    public void testPackThrowsISOException() throws Throwable {
        try {
            new GenericSubFieldPackager().pack(new ISOMsg());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testPackThrowsISOException1() throws Throwable {
        try {
            new GenericSubFieldPackager().pack(null);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testPackThrowsISOException2() throws Throwable {
        ISOFieldPackager[] fld = new ISOFieldPackager[3];
        fld[1] = new IFA_LCHAR(0, "testGenericSubFieldPackagerDescription");
        GenericSubFieldPackager genericSubFieldPackager = new GenericSubFieldPackager();
        genericSubFieldPackager.setFieldPackager(fld);
        try {
            genericSubFieldPackager.pack(new ISOBinaryField(100));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "org.jpos.iso.IFA_LCHAR: Problem packing field unknown", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testUnpackThrowsISOException() throws Throwable {
        ISOFieldPackager[] fld = new ISOFieldPackager[0];
        GenericSubFieldPackager genericSubFieldPackager = new GenericSubFieldPackager();
        genericSubFieldPackager.setFieldPackager(fld);
        byte[] b = new byte[2];
        try {
            genericSubFieldPackager.unpack(new ISOMsg(), b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
//            assertEquals("ex.getMessage()", "java.lang.ArrayIndexOutOfBoundsException: 1", ex.getMessage());
//            assertEquals("ex.getNested().getMessage()", "1", ex.getNested().getMessage());
        }
    }

    @Test
    public void testUnpackThrowsISOException1() throws Throwable {
        byte[] b = new byte[2];
        try {
            new GenericSubFieldPackager().unpack(new ISOMsg(), b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testUnpackThrowsISOException2() throws Throwable {
        ISOFieldPackager[] fld = new ISOFieldPackager[2];
        fld[1] = new IFA_LCHAR(0, "testGenericSubFieldPackagerDescription");
        GenericSubFieldPackager genericSubFieldPackager = new GenericSubFieldPackager();
        genericSubFieldPackager.setFieldPackager(fld);
        byte[] b = new byte[3];
        try {
            genericSubFieldPackager.unpack(new ISOMsg(100), b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "org.jpos.iso.IFA_LCHAR: Problem unpacking field -1", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testUnpackThrowsISOException3() throws Throwable {
        GenericSubFieldPackager genericSubFieldPackager = new GenericSubFieldPackager();
        genericSubFieldPackager.setLogger(new Logger(), "testGenericSubFieldPackagerRealm");
        try {
            genericSubFieldPackager.unpack(new ISOMsg(), (byte[]) null);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testUnpackThrowsISOException4() throws Throwable {
        byte[] b = new byte[0];
        try {
            new GenericSubFieldPackager().unpack(null, b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testUnpackThrowsISOException5() throws Throwable {
        byte[] b = new byte[2];
        try {
            new GenericSubFieldPackager().unpack(new ISOBinaryField(), b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "Can't call packager on non Composite", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testReturnsZero() throws Throwable {
        byte[] b = new byte[0];
        GenericSubFieldPackager genericSubFieldPackager = new GenericSubFieldPackager();
        genericSubFieldPackager.setLogger(new Logger(), "testGenericSubFieldPackagerRealm");
        int l = genericSubFieldPackager.unpack(new ISOMsg("testGenericSubFieldPackagerMti"), b);
        assertEquals (0L, (long) l);
    }

    @Test
    public void testUnpackReturnsZero1() throws Throwable {
        byte[] b = new byte[0];
        ISOFieldPackager[] fld = new ISOFieldPackager[0];
        GenericSubFieldPackager genericSubFieldPackager = new GenericSubFieldPackager();
        genericSubFieldPackager.setFieldPackager(fld);
        genericSubFieldPackager.setLogger(new Logger(), "testGenericSubFieldPackagerRealm");
        int l = genericSubFieldPackager.unpack(new ISOMsg("testGenericSubFieldPackagerMti"), b);
        assertEquals (0L, (long) l);
    }

    @Test
    public void testUnpackThrowsISOException8() throws Throwable {
        ISOFieldPackager[] fld = new ISOFieldPackager[2];
        fld[1] = new IFA_LLLLCHAR();
        GenericSubFieldPackager genericSubFieldPackager = new GenericSubFieldPackager();
        genericSubFieldPackager.setFieldPackager(fld);
        genericSubFieldPackager.setLogger(new Logger(), "testGenericSubFieldPackagerRealm");
        byte[] b = new byte[5];
        try {
            genericSubFieldPackager.unpack(new ISOMsg(), b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "org.jpos.iso.IFA_LLLLCHAR: Problem unpacking field -1", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

}
