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

package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.Vector;

import org.jpos.iso.ISOBitMap;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.VISA1ResponseFilter;
import org.junit.Test;

public class VISA1PackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        int[] sequence = new int[0];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        assertNull("vISA1Packager.getRealm()", vISA1Packager.getRealm());
        assertSame("vISA1Packager.filter", vISA1Packager, vISA1Packager.filter);
        assertNull("vISA1Packager.getLogger()", vISA1Packager.getLogger());
        assertEquals("vISA1Packager.badResultCode", "testVISA1PackagerBadResultCode", vISA1Packager.badResultCode);
        assertSame("vISA1Packager.sequence", sequence, vISA1Packager.sequence);
        assertEquals("vISA1Packager.okPattern", "testVISA1PackagerOkPattern", vISA1Packager.okPattern);
        assertEquals("vISA1Packager.respField", 100, vISA1Packager.respField);
    }

    @Test
    public void testCreateISOMsg() throws Throwable {
        int[] sequence = new int[0];
        ISOMsg result = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern")
                .createISOMsg();
        assertEquals("result.getDirection()", 0, result.getDirection());
    }

    @Test
    public void testGetFieldDescription() throws Throwable {
        int[] sequence = new int[0];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        String result = vISA1Packager.getFieldDescription(new ISOMsg(100), 100);
        assertEquals("result", "VISA 1 fld 100", result);
    }

    @Test
    public void testGuessAutNumber() throws Throwable {
        int[] sequence = new int[0];
        String result = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern")
                .guessAutNumber("testVISA1Packagers");
        assertEquals("result", "000001", result);
    }

    @Test
    public void testGuessAutNumber1() throws Throwable {
        int[] sequence = new int[2];
        String result = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern")
                .guessAutNumber(" ");
        assertNull("result", result);
    }

    @Test
    public void testGuessAutNumber2() throws Throwable {
        int[] sequence = new int[2];
        String result = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern")
                .guessAutNumber("");
        assertNull("result", result);
    }

    @Test
    public void testGuessAutNumber3() throws Throwable {
        int[] sequence = new int[1];
        String result = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern")
                .guessAutNumber("1");
        assertEquals("result", "000001", result);
    }

    @Test
    public void testGuessAutNumberThrowsNullPointerException() throws Throwable {
        int[] sequence = new int[2];
        try {
            new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern").guessAutNumber(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleSpecialField351() throws Throwable {
        int[] sequence = new int[0];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        Vector v = new Vector(100, 1000);
        int result = vISA1Packager.handleSpecialField35(new ISOMsg(100), v);
        assertEquals("result", 0, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleSpecialField35ThrowsNullPointerException() throws Throwable {
        int[] sequence = new int[0];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        Vector v = new Vector(100);
        try {
            vISA1Packager.handleSpecialField35(null, v);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("v.size()", 0, v.size());
        }
    }

    public void testPackThrowsISOException() throws Throwable {
        int[] sequence = new int[0];
        try {
            new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern").pack(new ISOField());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "Can't call VISA1 packager on non ISOMsg", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testSetVISA1ResponseFilter() throws Throwable {
        int[] sequence = new int[0];
        VISA1ResponseFilter filter = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern");
        int[] sequence2 = new int[0];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence2, 1000, "testVISA1PackagerBadResultCode1",
                "testVISA1PackagerOkPattern1");
        vISA1Packager.setVISA1ResponseFilter(filter);
        assertEquals("vISA1Packager.filter.respField", 100, ((VISA1Packager) vISA1Packager.filter).respField);
        assertEquals("vISA1Packager.filter.badResultCode", "testVISA1PackagerBadResultCode",
                ((VISA1Packager) vISA1Packager.filter).badResultCode);
        assertEquals("vISA1Packager.filter.okPattern", "testVISA1PackagerOkPattern",
                ((VISA1Packager) vISA1Packager.filter).okPattern);
        assertSame("vISA1Packager.filter", filter, vISA1Packager.filter);
    }

    @Test
    public void testUnpack() throws Throwable {
        int[] sequence = new int[2];
        ISOComponent m = new ISOMsg(100);
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "");
        byte[] b = new byte[1];
        int result = vISA1Packager.unpack(m, b);
        assertEquals("(ISOMsg) m.getMaxField()", 100, ((ISOMsg) m).getMaxField());
        assertEquals("result", 1, result);
        assertSame("vISA1Packager.filter", vISA1Packager, vISA1Packager.filter);
    }

    @Test
    public void testUnpack1() throws Throwable {
        int[] sequence = new int[0];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        ISOComponent m = new ISOMsg(100);
        byte[] b = new byte[0];
        int result = vISA1Packager.unpack(m, b);
        assertEquals("(ISOVMsg) m.getMaxField()", 100, ((ISOMsg) m).getMaxField());
        assertEquals("result", 0, result);
    }

    @Test
    public void testUnpackThrowsISOException() throws Throwable {
        int[] sequence = new int[2];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        byte[] bytes = new byte[3];
        try {
            vISA1Packager.unpack(new ISOMsg(100), new ByteArrayInputStream(bytes));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "not implemented", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testUnpackThrowsISOException1() throws Throwable {
        int[] sequence = new int[3];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        byte[] b = new byte[3];
        try {
            vISA1Packager.unpack(new ISOBitMap(100), b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "Can't add to Leaf", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertSame("vISA1Packager.filter", vISA1Packager, vISA1Packager.filter);
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException1() throws Throwable {
        int[] sequence = new int[3];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        byte[] b = new byte[3];
        try {
            vISA1Packager.unpack(null, b);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("vISA1Packager.filter", vISA1Packager, vISA1Packager.filter);
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException2() throws Throwable {
        int[] sequence = new int[2];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", null);
        byte[] b = new byte[3];
        ISOComponent m = new ISOMsg();
        try {
            vISA1Packager.unpack(m, b);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("(ISOMsg) m.getMaxField()", 100, ((ISOMsg) m).getMaxField());
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("vISA1Packager.filter", vISA1Packager, vISA1Packager.filter);
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException3() throws Throwable {
        int[] sequence = new int[0];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        try {
            vISA1Packager.unpack(new ISOBitMap(100), (byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("vISA1Packager.filter", vISA1Packager, vISA1Packager.filter);
        }
    }
}
