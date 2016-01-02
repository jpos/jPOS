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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class TLVMsgTest {

    @Test
    public void testConstructor() throws Throwable {
        byte[] value = new byte[2];
        TLVMsg tLVMsg = new TLVMsg(100, value);
        assertSame("tLVMsg.getValue()", value, tLVMsg.getValue());
        assertEquals("tLVMsg.getTag()", 100, tLVMsg.getTag());
    }

    @Test
    public void testConstructor1() throws Throwable {
        TLVMsg tLVMsg = new TLVMsg();
        assertEquals("tLVMsg.getTag()", 0, tLVMsg.getTag());
    }

    @Test
    public void testGetL() throws Throwable {
        byte[] value = new byte[3];
        byte[] result = new TLVMsg(100, value).getL();
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 3, result[0]);
    }

    @Test
    public void testGetL1() throws Throwable {
        byte[] value = new byte[1];
        byte[] result = new TLVMsg(100, value).getL();
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 1, result[0]);
    }

    @Test
    public void testGetL2() throws Throwable {
        byte[] result = new TLVMsg(100, null).getL();
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 0, result[0]);
    }

    @Test
    public void testGetL3() throws Throwable {
        byte[] value = new byte[200];
        byte[] result = new TLVMsg(100, value).getL();
        assertEquals("result.length", 2, result.length);
        assertEquals("result[0]", (byte) 0x81, result[0]);
        assertEquals("result[1]", (byte) 0xc8, result[1]);
    }

    @Test
    public void testGetL4() throws Throwable {
        byte[] value = new byte[0x7ff7];
        byte[] result = new TLVMsg(100, value).getL();
        assertEquals("result.length", 3, result.length);
        assertEquals("result[0]", (byte) 0x82, result[0]);
        assertEquals("result[1]", (byte) 0x7f, result[1]);
        assertEquals("result[2]", (byte) 0xf7, result[2]);
    }

    @Test
    public void testGetL5() throws Throwable {
        byte[] value = new byte[0x8ff8];
        byte[] result = new TLVMsg(100, value).getL();
        assertEquals("result.length", 3, result.length);
        assertEquals("result[0]", (byte) 0x82, result[0]);
        assertEquals("result[1]", (byte) 0x8f, result[1]);
        assertEquals("result[2]", (byte) 0xf8, result[2]);
    }

    @Test
    public void testGetL6() throws Throwable {
        byte[] value = new byte[0];
        byte[] result = new TLVMsg(100, value).getL();
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 0x00, result[0]);
    }

    @Test
    public void testGetTLV() throws Throwable {
        byte[] value = new byte[1];
        byte[] result = new TLVMsg(100, value).getTLV();
        assertEquals("result.length", 3, result.length);
        assertEquals("result[0]", (byte) 100, result[0]);
    }

    @Test
    public void testGetTLV1() throws Throwable {
        byte[] result = new TLVMsg(100, null).getTLV();
        assertEquals("result.length", 2, result.length);
        assertEquals("result[0]", (byte) 100, result[0]);
        assertEquals("result[1]", (byte) 0,   result[1]);
    }

    @Test
    public void testGetTLVEmptyValue1() throws Throwable {
        byte[] value = new byte[0];
        byte[] result = new TLVMsg(100, value).getTLV();
        assertEquals("result.length", 2, result.length);
        assertEquals("result[0]", (byte) 100, result[0]);
        assertEquals("result[1]", (byte) 0,   result[1]);
    }

    @Test
    public void testGetTLVEmptyValue2() throws Throwable {
        byte[] value = new byte[0];
        byte[] result = new TLVMsg(1000, value).getTLV();
        assertEquals("result.length", 3, result.length);
        assertEquals("result[0]", (byte) 0x03, result[0]);
        assertEquals("result[1]", (byte) 0xe8, result[1]);
        assertEquals("result[2]", (byte) 0,    result[2]);
    }

    @Test
    public void testSetTag() throws Throwable {
        byte[] value = new byte[0];
        TLVMsg tLVMsg = new TLVMsg(100, value);
        tLVMsg.setTag(1000);
        assertEquals("tLVMsg.getTag()", 1000, tLVMsg.getTag());
    }

    @Test
    public void testSetValue() throws Throwable {
        TLVMsg tLVMsg = new TLVMsg();
        byte[] newValue = new byte[1];
        tLVMsg.setValue(newValue);
        assertSame("tLVMsg.getValue()", newValue, tLVMsg.getValue());
    }
    
    @Test
    public void testGetStringValue() {
        TLVMsg tLVMsg = new TLVMsg(23, "987612".getBytes());
        String result = tLVMsg.getStringValue();
        assertThat(result,is("393837363132"));
    }
    @Test
    public void testLowTagID() {
        TLVMsg tlvMsg = new TLVMsg(8, "987612".getBytes());
        String result = tlvMsg.getStringValue();
        assertThat(result,is("393837363132"));
        byte[] b = tlvMsg.getTLV();
        assertEquals("b.length", 8, b.length);
        assertEquals("b[0]", (byte) 8, b[0]);
    }
}
