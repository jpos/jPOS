/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2016 Alejandro P. Revilla
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

import java.nio.BufferUnderflowException;
import java.util.Enumeration;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TLVListTest {

    static final String ISO_EXCEPT_EXCEEDS_AVAL = "BAD TLV FORMAT - tag (%x) length (%d) exceeds available data.";

    static final String ISO_EXCEPT_WITHOUT_LEN  = "BAD TLV FORMAT - tag (%x) without length or value";

    @Rule public ExpectedException exception = ExpectedException.none();

    TLVList tLVList;

    @Before
    public void beforeTest() {
      tLVList = new TLVList();
    }

    @Test
    public void testAppend() throws Throwable {
        tLVList.append(100, new byte[3]);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testAppend1() throws Throwable {
        tLVList.append(new TLVMsg(100, new byte[0]));
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testConstructor() throws Throwable {
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testDeleteByIndex() throws Throwable {
        tLVList.append(100, new byte[0]);
        tLVList.deleteByIndex(0);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testDeleteByIndexThrowsIndexOutOfBoundsException() throws Throwable {
        tLVList.deleteByIndex(100);
    }

    @Test
    public void testDeleteByTag() throws Throwable {
        tLVList.append(100, new byte[1]);
        tLVList.append(0, new byte[0]);
        tLVList.deleteByTag(100);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testDeleteByTag1() throws Throwable {
        tLVList.append(100, new byte[0]);
        tLVList.deleteByTag(1000);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testDeleteByTag2() throws Throwable {
        tLVList.deleteByTag(100);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testDeleteByTag3() throws Throwable {
        tLVList.append(0, new byte[0]);
        tLVList.deleteByTag(0);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testDeleteByTagThrowsNullPointerException() throws Throwable {
        tLVList.append(100, new byte[1]);
        tLVList.append(null);
        exception.expect(NullPointerException.class);
        try {
            tLVList.deleteByTag(1000);
        } catch (RuntimeException ex) {
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testDeleteByTagThrowsNullPointerException2() throws Throwable {
        tLVList.append(new TLVMsg());
        tLVList.append(100, new byte[2]);
        tLVList.append(null);
        exception.expect(NullPointerException.class);
        try {
            tLVList.deleteByTag(100);
        } catch (RuntimeException ex) {
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testDeleteByTagThrowsNullPointerException3() throws Throwable {
        tLVList.append(100, new byte[1]);
        tLVList.append(null);
        exception.expect(NullPointerException.class);
        try {
            tLVList.deleteByTag(100);
        } catch (RuntimeException ex) {
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testElements() throws Throwable {
        Enumeration result = tLVList.elements();
        assertFalse("result.hasMoreElements()", result.hasMoreElements());
    }

    @Test
    public void testFind() throws Throwable {
        tLVList.append(100, new byte[1]);
        TLVMsg tlvToAppend = new TLVMsg(0x07, null);
        tLVList.append(tlvToAppend);
        TLVMsg result = tLVList.find(0x07);
        assertSame("result", tlvToAppend, result);
    }

    @Test
    public void testFind1() throws Throwable {
        TLVMsg tlvToAppend = new TLVMsg();
        tLVList.append(tlvToAppend);
        TLVMsg result = tLVList.find(0);
        assertSame("result", tlvToAppend, result);
    }

    @Test
    public void testFind2() throws Throwable {
        tLVList.append(1000, new byte[0]);
        TLVMsg result = tLVList.find(100);
        assertNull("result", result);
    }

    @Test
    public void testFind3() throws Throwable {
        TLVMsg result = tLVList.find(100);
        assertNull("result", result);
    }

    @Test
    public void testFindIndex() throws Throwable {
        tLVList.append(100, new byte[2]);
        tLVList.append(0, new byte[2]);
        int result = tLVList.findIndex(0);
        assertEquals("result", 1, result);
    }

    @Test
    public void testFindIndex1() throws Throwable {
        tLVList.append(1000, new byte[3]);
        int result = tLVList.findIndex(100);
        assertEquals("result", -1, result);
    }

    @Test
    public void testFindIndex2() throws Throwable {
        tLVList.append(new TLVMsg());
        int result = tLVList.findIndex(0);
        assertEquals("result", 0, result);
    }

    @Test(expected = NullPointerException.class)
    public void testFindIndexThrowsNullPointerException() throws Throwable {
        tLVList.append(new TLVMsg());
        tLVList.append(null);
        tLVList.findIndex(100);
    }

    @Test(expected = NullPointerException.class)
    public void testFindIndexThrowsNullPointerException1() throws Throwable {
        tLVList.append(null);
        tLVList.findIndex(100);
    }

    @Test
    public void testFindNextTLV() throws Throwable {
        tLVList.findIndex(100);
        tLVList.append(1000, new byte[0]);
        TLVMsg result = tLVList.findNextTLV();
        assertNull("result", result);
    }

    @Test
    public void testFindNextTLV1() throws Throwable {
        tLVList.append(100,  new byte[2]);
        TLVMsg tlvToAppend = new TLVMsg();
        tLVList.append(tlvToAppend);
        TLVMsg result = tLVList.findNextTLV();
        assertSame("result", tlvToAppend, result);
    }

    @Test
    public void testFindNextTLV2() throws Throwable {
        tLVList.append(0, new byte[3]);
        TLVMsg result = tLVList.findNextTLV();
        assertEquals("result.getTag()", 0, result.getTag());
    }

    @Test
    public void testFindNextTLV3() throws Throwable {
        TLVMsg result = tLVList.findNextTLV();
        assertNull("result", result);
    }

    @Test(expected = NullPointerException.class)
    public void testFindNextTLVThrowsNullPointerException() throws Throwable {
        tLVList.append(null);
        tLVList.findNextTLV();
    }

    @Test(expected = NullPointerException.class)
    public void testFindNextTLVThrowsNullPointerException1() throws Throwable {
        tLVList.findIndex(100);
        tLVList.append(new TLVMsg());
        tLVList.append(null);
        tLVList.findNextTLV();
    }

    @Test(expected = NullPointerException.class)
    public void testFindThrowsNullPointerException() throws Throwable {
        tLVList.append(null);
        tLVList.find(100);
    }

    @Test(expected = NullPointerException.class)
    public void testFindThrowsNullPointerException1() throws Throwable {
        tLVList.append(new TLVMsg());
        tLVList.append(null);
        tLVList.find(100);
    }

    @Test
    public void testIndex() throws Throwable {
        TLVMsg tlvToAppend = new TLVMsg(100, null);
        tLVList.append(tlvToAppend);
        TLVMsg result = tLVList.index(0);
        assertSame("result", tlvToAppend, result);
    }

    @Test
    public void testIndex1() throws Throwable {
        tLVList.append(new TLVMsg(100, "testString".getBytes()));
        tLVList.append(100, new byte[1]);
        tLVList.append(new TLVMsg());
        tLVList.append(1000, new byte[3]);
        tLVList.append(0, new byte[1]);
        tLVList.append(-1, new byte[0]);
        tLVList.deleteByIndex(0);
        tLVList.append(new TLVMsg());
        tLVList.append(1, new byte[3]);
        tLVList.append(null);
        tLVList.append(10, new byte[0]);
        tLVList.append(new TLVMsg());
        tLVList.append(10000, new byte[2]);
        tLVList.append(100000, new byte[1]);
        tLVList.append(null);
        TLVMsg result = tLVList.index(12);
        assertNull("result", result);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testIndexThrowsIndexOutOfBoundsException() throws Throwable {
        tLVList.index(100);
    }

    @Test
    public void testPack() throws Throwable {
        byte[] result = tLVList.pack();
        assertEquals("result.length", 0, result.length);
    }

    @Test(expected = NullPointerException.class)
    public void testPackThrowsNullPointerException() throws Throwable {
        tLVList.append(null);
        tLVList.pack();
    }

    @Test
    public void testUnpack() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("030100");
        tLVList.unpack(buf, 0);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        TLVMsg tm = tLVList.index(0);
        assertEquals("tm.getTag()", 0x03, tm.getTag());
        assertArrayEquals("tm.getValue()", ISOUtil.hex2byte("00"), tm.getValue());
    }

    @Test
    public void testUnpack1() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("000100");
        tLVList.unpack(buf, 0);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        TLVMsg tm = tLVList.index(0);
        assertEquals("tm.getTag()", 0x01, tm.getTag());
        assertArrayEquals("tm.getValue()", new byte[0], tm.getValue());
    }

    @Test
    public void testUnpack10() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("2080");
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        TLVMsg tm = tLVList.index(0);
        assertEquals("tm.getTag()", 0x20, tm.getTag());
        assertArrayEquals("tm.getValue()", new byte[0], tm.getValue());
    }

    @Test
    public void testUnpack11() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("030100");
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        TLVMsg tm = tLVList.index(0);
        assertEquals("tm.getTag()", 0x03, tm.getTag());
        assertArrayEquals("tm.getValue()", ISOUtil.hex2byte("00"), tm.getValue());
    }

    @Test
    public void testUnpack12() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("ff00");
        tLVList.unpack(buf);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack2() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("000100");
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        TLVMsg tm = tLVList.index(0);
        assertEquals("tm.getTag()", 0x01, tm.getTag());
        assertArrayEquals("tm.getValue()", new byte[0], tm.getValue());
    }

    @Test
    public void testUnpack3() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("0100");
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
        TLVMsg tm = tLVList.index(0);
        assertEquals("tm.getTag()", 0x01, tm.getTag());
        assertArrayEquals("tm.getValue()", new byte[0], tm.getValue());
    }

    @Test
    public void testUnpack4() throws Throwable {
        byte[] buf = new byte[0];
        tLVList.unpack(buf);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack5() throws Throwable {
        byte[] buf = new byte[0];
        tLVList.unpack(buf, 0);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack6() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("6000");
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack7() throws Throwable {
        byte[] buf = new byte[2];
        tLVList.unpack(buf);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack8() throws Throwable {
        byte[] buf = new byte[3];
        tLVList.unpack(buf);
        assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpack9() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("1e00");
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpackWith0x00Padding() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("fe0000");
        tLVList.unpack(buf, 0);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpackWith0x00Padding1() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("878000");
        // buf[0] = (byte) -121;
        // buf[1] = (byte) -128;
        tLVList.unpack(buf);
        assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
    }

    @Test
    public void testUnpackThrowsBufferUnderflowException3() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("007f");
        exception.expect(BufferUnderflowException.class);
        try {
            tLVList.unpack(buf);
        } catch (BufferUnderflowException ex) {
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsIndexOutOfBoundsException() throws Throwable {
        byte[] buf = new byte[3];
        exception.expect(IndexOutOfBoundsException.class);
        try {
            tLVList.unpack(buf, 100);
        } catch (IndexOutOfBoundsException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("00ff80");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x80));
        try {
            tLVList.unpack(buf, 0);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException1() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("001e");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x1e));
        try {
            tLVList.unpack(buf, 0);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException10() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("7f0007");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_EXCEEDS_AVAL, 0x7f00, 0x07));
        try {
            tLVList.unpack(buf);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException11() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("ff1e");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x1e));
        try {
            tLVList.unpack(buf, 0);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException12() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("fe2000");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_EXCEEDS_AVAL, 0xfe, 0x20));
        try {
            tLVList.unpack(buf, 0);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException13() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("000008");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x08));
        try {
            tLVList.unpack(buf, 0);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException14() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("7f00");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x7f00));
        try {
            tLVList.unpack(buf, 0);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException15() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("f8");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0xf8));
        try {
            tLVList.unpack(buf, 0);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException16() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("fe81ed");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_EXCEEDS_AVAL, 0xfe, 0xed));
        try {
            tLVList.unpack(buf);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException17() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("000001");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x01));
        try {
            tLVList.unpack(buf);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException18() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("878009");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x09));
        try {
            tLVList.unpack(buf);
        } catch (ISOException ex) {
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            assertNull("ex.getNested()", ex.getNested());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException2() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("0001");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x01));
        try {
            tLVList.unpack(buf);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException3() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("fe00ed");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0xed));
        try {
            tLVList.unpack(buf);
        } catch (ISOException ex) {
            assertTrue("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            assertNull("ex.getNested()", ex.getNested());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException4() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("00fe");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0xfe));
        try {
            tLVList.unpack(buf);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException5() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("fe7600");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_EXCEEDS_AVAL, 0xfe, 0x76));
        try {
            tLVList.unpack(buf);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    } 

    @Test
    public void testUnpackThrowsISOException6() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("ff1e");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x1e));
        try {
            tLVList.unpack(buf);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException7() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("01");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x01));
        try {
            tLVList.unpack(buf);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException8() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("7f00");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x7f00));
        try {
            tLVList.unpack(buf);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test
    public void testUnpackThrowsISOException9() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("00f801");
        exception.expect(ISOException.class);
        exception.expectMessage(String.format(ISO_EXCEPT_EXCEEDS_AVAL, 0xf8, 0x01));
        try {
            tLVList.unpack(buf);
        } catch (ISOException ex) {
            assertNull("ex.getNested()", ex.getNested());
            assertFalse("tLVList.elements().hasMoreElements()", tLVList.elements().hasMoreElements());
            throw ex;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testUnpackThrowsNullPointerException() throws Throwable {
        tLVList.unpack(null, 100);
    }

    @Test(expected = NullPointerException.class)
    public void testUnpackThrowsNullPointerException1() throws Throwable {
        tLVList.unpack(null);
    }
}
