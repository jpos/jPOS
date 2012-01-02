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
import static java.lang.String.format;
import static org.mockito.BDDMockito.*;
import static org.junit.Assume.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.mockito.*;
import org.mockito.runners.*;
import org.junit.*;
import org.junit.runner.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

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
    public void testGetLThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] value = new byte[0];
        try {
            new TLVMsg(100, value).getL();
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0", ex.getMessage());
        }
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
    }

    @Test
    public void testGetTLVThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] value = new byte[0];
        try {
            new TLVMsg(100, value).getTLV();
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0", ex.getMessage());
        }
    }

    @Test
    public void testGetTLVEmptyValueThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] value = new byte[0];
        TLVMsg tLVMsg = new TLVMsg(1000, value);
        try {
            tLVMsg.getTLV();
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0", ex.getMessage());
        }
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
}
