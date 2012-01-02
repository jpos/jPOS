/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

package org.jpos.tlv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.BufferUnderflowException;
import java.util.Enumeration;

import org.jpos.iso.ISOException;
import org.junit.Test;

public class TLVListTest {

    @Test
    public void testAppend() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] value = new byte[3];
        tLVList.append(100, value);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testAppend1() throws Throwable {
        TLVList tLVList = new TLVList();
        TLVMsg tlvToAppend = new TLVMsg(100, "".getBytes());
        tLVList.append(tlvToAppend);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testConstructor() throws Throwable {
        TLVList tLVList = new TLVList();
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testDeleteByIndex() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] value = new byte[0];
        tLVList.append(100, value);
        tLVList.deleteByIndex(0);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testDeleteByIndexThrowsArrayIndexOutOfBoundsException() throws Throwable {
        TLVList tLVList = new TLVList();
        try {
            tLVList.deleteByIndex(100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "Array index out of range: 100", ex.getMessage());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testDeleteByTag() throws Throwable {
        byte[] value = new byte[1];
        TLVList tLVList = new TLVList();
        tLVList.append(100, value);
        byte[] value2 = new byte[0];
        tLVList.append(0, value2);
        tLVList.deleteByTag(100);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testDeleteByTag1() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] value = new byte[0];
        tLVList.append(100, value);
        tLVList.deleteByTag(1000);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testDeleteByTag2() throws Throwable {
        TLVList tLVList = new TLVList();
        tLVList.deleteByTag(100);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testDeleteByTag3() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] value = new byte[0];
        tLVList.append(0, value);
        tLVList.deleteByTag(0);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testDeleteByTagThrowsNullPointerException() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] value = new byte[1];
        tLVList.append(100, value);
        tLVList.append(null);
        try {
            tLVList.deleteByTag(1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testDeleteByTagThrowsNullPointerException2() throws Throwable {
        TLVList tLVList = new TLVList();
        TLVMsg tlvToAppend = new TLVMsg();
        tLVList.append(tlvToAppend);
        byte[] value = new byte[2];
        tLVList.append(100, value);
        tLVList.append(null);
        try {
            tLVList.deleteByTag(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testDeleteByTagThrowsNullPointerException3() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] value = new byte[1];
        tLVList.append(100, value);
        tLVList.append(null);
        try {
            tLVList.deleteByTag(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testElements() throws Throwable {
        Enumeration result = new TLVList().elements();
        assertFalse("result.hasMoreElements()", result.hasMoreElements());
    }

    @Test
    public void testFind() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] value = new byte[1];
        tLVList.append(100, value);
        TLVMsg tlvToAppend = new TLVMsg();
        tLVList.append(tlvToAppend);
        TLVMsg result = tLVList.find(0);
        assertSame("result", tlvToAppend, result);
    }

    @Test
    public void testFind1() throws Throwable {
        TLVMsg tlvToAppend = new TLVMsg();
        TLVList tLVList = new TLVList();
        tLVList.append(tlvToAppend);
        TLVMsg result = tLVList.find(0);
        assertSame("result", tlvToAppend, result);
    }

    @Test
    public void testFind2() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] value = new byte[0];
        tLVList.append(1000, value);
        TLVMsg result = tLVList.find(100);
        assertNull("result", result);
    }

    @Test
    public void testFind3() throws Throwable {
        TLVList tLVList = new TLVList();
        TLVMsg result = tLVList.find(100);
        assertNull("result", result);
    }

    @Test
    public void testFindIndex() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] value = new byte[2];
        tLVList.append(100, value);
        byte[] value2 = new byte[2];
        tLVList.append(0, value2);
        int result = tLVList.findIndex(0);
        assertEquals("result", 1, result);
    }

    @Test
    public void testFindIndex1() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] value = new byte[3];
        tLVList.append(1000, value);
        int result = tLVList.findIndex(100);
        assertEquals("result", -1, result);
    }

    @Test
    public void testFindIndex2() throws Throwable {
        TLVList tLVList = new TLVList();
        tLVList.append(new TLVMsg());
        int result = tLVList.findIndex(0);
        assertEquals("result", 0, result);
    }

    @Test
    public void testFindIndexThrowsNullPointerException() throws Throwable {
        TLVList tLVList = new TLVList();
        tLVList.append(new TLVMsg());
        tLVList.append(null);
        try {
            tLVList.findIndex(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testFindIndexThrowsNullPointerException1() throws Throwable {
        TLVList tLVList = new TLVList();
        tLVList.append(null);
        try {
            tLVList.findIndex(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testFindNextTLV() throws Throwable {
        TLVList tLVList = new TLVList();
        tLVList.findIndex(100);
        byte[] value = new byte[0];
        tLVList.append(1000, value);
        TLVMsg result = tLVList.findNextTLV();
        assertNull("result", result);
    }

    @Test
    public void testFindNextTLV1() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] value = new byte[2];
        tLVList.append(100, value);
        TLVMsg tlvToAppend = new TLVMsg();
        tLVList.append(tlvToAppend);
        TLVMsg result = tLVList.findNextTLV();
        assertSame("result", tlvToAppend, result);
    }

    @Test
    public void testFindNextTLV2() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] value = new byte[3];
        tLVList.append(0, value);
        TLVMsg result = tLVList.findNextTLV();
        assertEquals("result.getTag()", 0, result.getTag());
    }

    @Test
    public void testFindNextTLV3() throws Throwable {
        TLVMsg result = new TLVList().findNextTLV();
        assertNull("result", result);
    }

    @Test
    public void testFindNextTLVThrowsNullPointerException() throws Throwable {
        TLVList tLVList = new TLVList();
        tLVList.append(null);
        try {
            tLVList.findNextTLV();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testFindNextTLVThrowsNullPointerException1() throws Throwable {
        TLVList tLVList = new TLVList();
        tLVList.findIndex(100);
        tLVList.append(new TLVMsg());
        tLVList.append(null);
        try {
            tLVList.findNextTLV();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testFindThrowsNullPointerException() throws Throwable {
        TLVList tLVList = new TLVList();
        tLVList.append(null);
        try {
            tLVList.find(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testFindThrowsNullPointerException1() throws Throwable {
        TLVList tLVList = new TLVList();
        tLVList.append(new TLVMsg());
        tLVList.append(null);
        try {
            tLVList.find(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testIndex() throws Throwable {
        TLVList tLVList = new TLVList();
        TLVMsg tlvToAppend = new TLVMsg(100, null);
        tLVList.append(tlvToAppend);
        TLVMsg result = tLVList.index(0);
        assertSame("result", tlvToAppend, result);
    }

    @Test
    public void testIndex1() throws Throwable {
        byte[] value = new byte[0];
        TLVList tLVList = new TLVList();
        tLVList.append(new TLVMsg(100, "testString".getBytes()));
        byte[] value2 = new byte[1];
        tLVList.append(100, value2);
        tLVList.append(new TLVMsg());
        byte[] value3 = new byte[3];
        tLVList.append(1000, value3);
        byte[] value4 = new byte[1];
        tLVList.append(0, value4);
        tLVList.append(-1, value);
        tLVList.deleteByIndex(0);
        tLVList.append(new TLVMsg());
        byte[] value5 = new byte[3];
        tLVList.append(1, value5);
        tLVList.append(null);
        byte[] value6 = new byte[0];
        tLVList.append(10, value6);
        tLVList.append(new TLVMsg());
        byte[] value7 = new byte[2];
        tLVList.append(10000, value7);
        byte[] value8 = new byte[1];
        tLVList.append(100000, value8);
        tLVList.append(null);
        TLVMsg result = tLVList.index(12);
        assertNull("result", result);
    }

    @Test
    public void testIndexThrowsArrayIndexOutOfBoundsException() throws Throwable {
        try {
            new TLVList().index(100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "Array index out of range: 100", ex.getMessage());
        }
    }

    @Test
    public void testPack() throws Throwable {
        byte[] result = new TLVList().pack();
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testPackThrowsNullPointerException() throws Throwable {
        TLVList tLVList = new TLVList();
        tLVList.append(null);
        try {
            tLVList.pack();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnpack() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[0] = (byte) 3;
        buf[1] = (byte) 1;
        tLVList.unpack(buf, 0);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack1() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[1] = (byte) 1;
        tLVList.unpack(buf, 0);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack10() throws Throwable {
        byte[] buf = new byte[2];
        buf[0] = (byte) 32;
        buf[1] = (byte) -128;
        TLVList tLVList = new TLVList();
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack11() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[0] = (byte) 3;
        buf[1] = (byte) 1;
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack12() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        buf[0] = (byte) -1;
        tLVList.unpack(buf);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack2() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[1] = (byte) 1;
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack3() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        buf[0] = (byte) 1;
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack4() throws Throwable {
        byte[] buf = new byte[0];
        TLVList tLVList = new TLVList();
        tLVList.unpack(buf);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack5() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[0];
        tLVList.unpack(buf, 0);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack6() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        buf[0] = (byte) 96;
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack7() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        tLVList.unpack(buf);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack8() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        tLVList.unpack(buf);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack9() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        buf[0] = (byte) 30;
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpackThrowsBufferUnderflowException() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[0] = (byte) -2;
        try {
            tLVList.unpack(buf, 0);
            fail("Expected BufferUnderflowException to be thrown");
        } catch (BufferUnderflowException ex) {
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnpackThrowsBufferUnderflowException1() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[0] = (byte) -121;
        buf[1] = (byte) -128;
        try {
            tLVList.unpack(buf);
            fail("Expected BufferUnderflowException to be thrown");
        } catch (BufferUnderflowException ex) {
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnpackThrowsBufferUnderflowException2() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[0] = (byte) -2;
        try {
            tLVList.unpack(buf);
            fail("Expected BufferUnderflowException to be thrown");
        } catch (BufferUnderflowException ex) {
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnpackThrowsBufferUnderflowException3() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        buf[1] = (byte) 127;
        try {
            tLVList.unpack(buf);
            fail("Expected BufferUnderflowException to be thrown");
        } catch (BufferUnderflowException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsIndexOutOfBoundsException() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        try {
            tLVList.unpack(buf, 100);
            fail("Expected IndexOutOfBoundsException to be thrown");
        } catch (IndexOutOfBoundsException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[1] = (byte) -1;
        buf[2] = (byte) -128;
        try {
            tLVList.unpack(buf, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (80) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException1() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        buf[1] = (byte) 30;
        try {
            tLVList.unpack(buf, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (1e) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException10() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[0] = (byte) 127;
        buf[2] = (byte) 7;
        try {
            tLVList.unpack(buf);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (7f00) length (7) exceeds available data.", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException11() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        buf[0] = (byte) -1;
        buf[1] = (byte) 30;
        try {
            tLVList.unpack(buf, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (1e) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException12() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[0] = (byte) -2;
        buf[1] = (byte) 32;
        try {
            tLVList.unpack(buf, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (fe) length (32) exceeds available data.", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException13() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[2] = (byte) 8;
        try {
            tLVList.unpack(buf, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (8) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException14() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        buf[0] = (byte) 127;
        try {
            tLVList.unpack(buf, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (7f00) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException15() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[1];
        buf[0] = (byte) -8;
        try {
            tLVList.unpack(buf, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (f8) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException16() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[0] = (byte) -2;
        buf[1] = (byte) -127;
        buf[2] = (byte) -19;
        try {
            tLVList.unpack(buf);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (fe) length (237) exceeds available data.", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException17() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[2] = (byte) 1;
        try {
            tLVList.unpack(buf);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (1) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException18() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[0] = (byte) -121;
        buf[1] = (byte) -128;
        buf[2] = (byte) 9;
        try {
            tLVList.unpack(buf);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (9) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testUnpackThrowsISOException2() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        buf[1] = (byte) 1;
        try {
            tLVList.unpack(buf);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (1) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException3() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[0] = (byte) -2;
        buf[2] = (byte) -19;
        try {
            tLVList.unpack(buf);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (ed) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testUnpackThrowsISOException4() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        buf[1] = (byte) -2;
        try {
            tLVList.unpack(buf);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (fe) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException5() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[0] = (byte) -2;
        buf[1] = (byte) 118;
        try {
            tLVList.unpack(buf);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (fe) length (118) exceeds available data.", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException6() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        buf[0] = (byte) -1;
        buf[1] = (byte) 30;
        try {
            tLVList.unpack(buf);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (1e) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException7() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[1];
        buf[0] = (byte) 1;
        try {
            tLVList.unpack(buf);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (1) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException8() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[2];
        buf[0] = (byte) 127;
        try {
            tLVList.unpack(buf);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (7f00) without length or value", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsISOException9() throws Throwable {
        TLVList tLVList = new TLVList();
        byte[] buf = new byte[3];
        buf[1] = (byte) -8;
        buf[2] = (byte) 1;
        try {
            tLVList.unpack(buf);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "BAD TLV FORMAT - tag (f8) length (1) exceeds available data.", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException() throws Throwable {
        TLVList tLVList = new TLVList();
        try {
            tLVList.unpack((byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException1() throws Throwable {
        TLVList tLVList = new TLVList();
        try {
            tLVList.unpack((byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        }
    }
}
