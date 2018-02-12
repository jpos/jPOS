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

package org.jpos.iso.header;

import org.jpos.iso.ISOUtil;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class BASE1HeaderTest {
    @Test
    public void testConstructor() throws Throwable {
        byte[] header = new byte[2];
        BASE1Header bASE1Header = new BASE1Header(header);
        assertTrue("bASE1Header.header", Arrays.equals(header, bASE1Header.header)
        );
    }

    @Test
    public void testConstructor1() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header("testBASE1HeaderSource", "testBASE1HeaderDestination");
        assertEquals("bASE1Header.header.length", 22, bASE1Header.header.length);
    }

    @Test
    public void testConstructor2() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        assertEquals("bASE1Header.header.length", 22, bASE1Header.header.length);
    }

    @Test
    public void testConstructorThrowsArrayIndexOutOfBoundsException() throws Throwable {
        try {
            new BASE1Header("testBASE1HeaderSource", "");
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        try {
            new BASE1Header("", "testBASE1HeaderDestination");
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructorAcceptsNullValue() throws Throwable {
        BASE1Header h = new BASE1Header(null);
        assertNotNull("BASE1Header is null", h);
    }

    @Test
    public void testConstructorThrowsNullPointerException1() throws Throwable {
        try {
            new BASE1Header("testBASE1HeaderSource", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetDestinationThrowsArrayIndexOutOfBoundsException() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[3];
        bASE1Header.unpack(header);
        try {
            bASE1Header.getDestination();
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            // assertEquals("ex.getMessage()", "5", ex.getMessage());
        }
    }

    @Test
    public void testGetFormat() throws Throwable {
        int result = new BASE1Header().getFormat();
        assertEquals("result", 2, result);
    }

    @Test
    public void testGetFormatThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] header = new byte[2];
        BASE1Header bASE1Header = new BASE1Header("testBASE1HeaderSource", "testBASE1HeaderDestination");
        bASE1Header.unpack(header);
        try {
            bASE1Header.getFormat();
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void testGetHLen() throws Throwable {
        int result = new BASE1Header().getHLen();
        assertEquals("result", 22, result);
    }

    @Test
    public void testGetHLenThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] header = new byte[0];
        BASE1Header bASE1Header = new BASE1Header();
        bASE1Header.unpack(header);
        try {
            bASE1Header.getHLen();
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0", ex.getMessage());
        }
    }

    @Test
    public void testGetRejectCode() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header("testBASE1HeaderSource", "testBASE1HeaderDestination");
        byte[] header = new byte[29];
        header[22] = (byte) -118;
        bASE1Header.unpack(header);
        String result = bASE1Header.getRejectCode();
        assertEquals("result", "0000", result);
    }

    @Test
    public void testGetRejectCode1() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[25];
        bASE1Header.unpack(header);
        String result = bASE1Header.getRejectCode();
        assertEquals("result", "", result);
    }

    @Test
    public void testGetRejectCode2() throws Throwable {
        byte[] header = new byte[27];
        BASE1Header bASE1Header = new BASE1Header();
        bASE1Header.unpack(header);
        String result = bASE1Header.getRejectCode();
        assertEquals("result", "", result);
    }

    @Test
    public void testGetSource() throws Throwable {
        String result = new BASE1Header().getSource();
        assertEquals("result", "000000", result);
    }

    @Test
    public void testGetSourceThrowsArrayIndexOutOfBoundsException() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[3];
        bASE1Header.unpack(header);
        try {
            bASE1Header.getSource();
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            // assertEquals("ex.getMessage()", "8", ex.getMessage());
        }
    }

    @Test
    public void testIsRejected() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[29];
        header[22] = (byte) -55;
        bASE1Header.unpack(header);
        boolean result = bASE1Header.isRejected();
        assertTrue("result", result);
    }

    @Test
    public void testIsRejected1() throws Throwable {
        byte[] header = new byte[26];
        header[22] = (byte) -128;
        BASE1Header bASE1Header = new BASE1Header();
        bASE1Header.unpack(header);
        boolean result = bASE1Header.isRejected();
        assertTrue("result", result);
    }

    @Test
    public void testIsRejected2() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[25];
        bASE1Header.unpack(header);
        boolean result = bASE1Header.isRejected();
        assertFalse("result", result);
    }

    @Test
    public void testSetBatchNumber() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[18];
        bASE1Header.unpack(header);
        bASE1Header.setBatchNumber(100);
        assertFalse("bASE1Header.header", Arrays.equals(header, bASE1Header.header));
    }

    @Test
    public void testSetBatchNumberThrowsArrayIndexOutOfBoundsException() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[3];
        bASE1Header.unpack(header);
        try {
            bASE1Header.setBatchNumber(100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "17", ex.getMessage());
            assertTrue("bASE1Header.header", Arrays.equals(header, bASE1Header.header));
        }
    }

    @Test
    public void testSetDestination() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[8];
        bASE1Header.unpack(header);
        bASE1Header.setDestination("testBASE1HeaderDest");
        assertFalse("bASE1Header.header", Arrays.equals(header, bASE1Header.header));
    }

    @Test
    public void testSetDestinationThrowsArrayIndexOutOfBoundsException() throws Throwable {
        try {
            new BASE1Header().setDestination("");
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetDestinationThrowsNullPointerException() throws Throwable {
        try {
            new BASE1Header().setDestination(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetFlags() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        bASE1Header.setFlags(100);
        assertEquals("bASE1Header.header.length", 22, bASE1Header.header.length);
    }

    @Test
    public void testSetFlagsThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] header = new byte[9];
        BASE1Header bASE1Header = new BASE1Header();
        bASE1Header.unpack(header);
        try {
            bASE1Header.setFlags(100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "12", ex.getMessage());
            assertTrue("bASE1Header.header", Arrays.equals(header, bASE1Header.header));
        }
    }

    @Test
    public void testSetFlagsThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] header = new byte[13];
        BASE1Header bASE1Header = new BASE1Header();
        bASE1Header.unpack(header);
        try {
            bASE1Header.setFlags(100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "13", ex.getMessage());
            assertNotEquals("bASE1Header.header", header, bASE1Header.header);
        }
    }

    @Test
    public void testSetFormat() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        bASE1Header.setFormat(100);
        assertEquals("bASE1Header.header.length", 22, bASE1Header.header.length);
    }

    @Test
    public void testSetFormatThrowsArrayIndexOutOfBoundsException() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header("testBASE1HeaderSource", "testBASE1HeaderDestination");
        byte[] header = new byte[2];
        bASE1Header.unpack(header);
        try {
            bASE1Header.setFormat(100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "2", ex.getMessage());
            assertNotEquals("bASE1Header.header", header, bASE1Header.header);
        }
    }

    @Test
    public void testSetHFormat() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        bASE1Header.setHFormat(100);
        assertEquals("bASE1Header.header.length", 22, bASE1Header.header.length);
    }

    @Test
    public void testSetLen() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        bASE1Header.setLen(100);
        assertEquals("bASE1Header.header.length", 22, bASE1Header.header.length);
    }

    @Test
    public void testSetLenThrowsArrayIndexOutOfBoundsException() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header("testBASE1HeaderSource", "testBASE1HeaderDestination");
        byte[] header = new byte[4];
        bASE1Header.unpack(header);
        try {
            bASE1Header.setLen(100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "4", ex.getMessage());
            assertTrue("bASE1Header.header", Arrays.equals(header, bASE1Header.header));
        }
    }

    @Test
    public void testSetLenThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        BASE1Header clone = (BASE1Header) new BASE1Header("testBASE1HeaderSource", "testBASE1HeaderDestination").clone();
        byte[] header = new byte[3];
        clone.unpack(header);
        try {
            clone.setLen(100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
            assertTrue("clone.header", Arrays.equals(header, clone.header));
        }
    }

    @Test
    public void testSetRtCtl() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[22];
        bASE1Header.unpack(header);
        bASE1Header.setRtCtl(100);
        assertFalse("bASE1Header.header", Arrays.equals(header, bASE1Header.header));
    }

    @Test
    public void testSetRtCtlThrowsArrayIndexOutOfBoundsException() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[2];
        bASE1Header.unpack(header);
        try {
            bASE1Header.setRtCtl(100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "11", ex.getMessage());
            assertTrue("bASE1Header.header", Arrays.equals(header, bASE1Header.header));
        }
    }

    @Test
    public void testSetSource() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[22];
        bASE1Header.unpack(header);
        bASE1Header.setSource("testBASE1HeaderSrc");
        assertFalse("bASE1Header.header", Arrays.equals(header, bASE1Header.header));
    }

    @Test
    public void testSetSourceThrowsArrayIndexOutOfBoundsException() throws Throwable {
        try {
            new BASE1Header().setSource("");
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetSourceThrowsNullPointerException() throws Throwable {
        try {
            new BASE1Header().setSource(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetStatus() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[17];
        bASE1Header.unpack(header);
        bASE1Header.setStatus(100);
        assertFalse("BASE1Header.header", Arrays.equals(header, bASE1Header.header));
    }

    @Test
    public void testSetStatusThrowsArrayIndexOutOfBoundsException() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[15];
        bASE1Header.unpack(header);
        try {
            bASE1Header.setStatus(100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "15", ex.getMessage());
        }
    }

    @Test
    public void testSetStatusThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[3];
        bASE1Header.unpack(header);
        try {
            bASE1Header.setStatus(100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "14", ex.getMessage());
        }
    }

    @Test
    public void testUnpack() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[0];
        int result = bASE1Header.unpack(header);
        assertEquals("result", 0, result);
        assertNotNull("bASE1Header.header", bASE1Header.header);
    }

    @Test
    public void testUnpack1() throws Throwable {
        BASE1Header bASE1Header = new BASE1Header();
        byte[] header = new byte[3];
        int result = bASE1Header.unpack(header);
        assertEquals("result", 3, result);
    }

    @Test
    public void testCloneAndSwap() throws Throwable {
        BASE1Header h = new BASE1Header(ISOUtil.hex2byte("16010201020000001234560000000000000000000000"));
        BASE1Header ha = (BASE1Header) h.clone();
        BASE1Header hb = new BASE1Header(h.pack());
        assertEquals ("source should be '123456'", h.getSource(), "123456");
        assertEquals ("destination should be '000000'", h.getDestination(), "000000");
        h.swapDirection();
        assertEquals ("source should be '123456'", h.getSource(), "000000");
        assertEquals ("destination should be '000000'", h.getDestination(), "123456");
        assertEquals ("cloned source should be '123456'", ha.getSource(), "123456");
        assertEquals ("cloned destination should be '000000'", ha.getDestination(), "000000");
        assertEquals ("packed source should be '123456'", hb.getSource(), "123456");
        assertEquals ("packed destination should be '000000'", hb.getDestination(), "000000");
    }
}
