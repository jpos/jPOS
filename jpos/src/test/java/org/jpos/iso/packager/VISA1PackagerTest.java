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

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.jpos.iso.ISOBitMap;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.VISA1ResponseFilter;
import org.junit.jupiter.api.Test;

public class VISA1PackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        int[] sequence = new int[0];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        assertNull(vISA1Packager.getRealm(), "vISA1Packager.getRealm()");
        assertSame(vISA1Packager, vISA1Packager.filter, "vISA1Packager.filter");
        assertNull(vISA1Packager.getLogger(), "vISA1Packager.getLogger()");
        assertEquals("testVISA1PackagerBadResultCode", vISA1Packager.badResultCode, "vISA1Packager.badResultCode");
        assertSame(sequence, vISA1Packager.sequence, "vISA1Packager.sequence");
        assertEquals("testVISA1PackagerOkPattern", vISA1Packager.okPattern, "vISA1Packager.okPattern");
        assertEquals(100, vISA1Packager.respField, "vISA1Packager.respField");
    }

    @Test
    public void testCreateISOMsg() throws Throwable {
        int[] sequence = new int[0];
        ISOMsg result = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern")
                .createISOMsg();
        assertEquals(0, result.getDirection(), "result.getDirection()");
    }

    @Test
    public void testGetFieldDescription() throws Throwable {
        int[] sequence = new int[0];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        String result = vISA1Packager.getFieldDescription(new ISOMsg(100), 100);
        assertEquals("VISA 1 fld 100", result, "result");
    }

    @Test
    public void testGuessAutNumber() throws Throwable {
        int[] sequence = new int[0];
        String result = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern")
                .guessAutNumber("testVISA1Packagers");
        assertEquals("000001", result, "result");
    }

    @Test
    public void testGuessAutNumber1() throws Throwable {
        int[] sequence = new int[2];
        String result = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern")
                .guessAutNumber(" ");
        assertNull(result, "result");
    }

    @Test
    public void testGuessAutNumber2() throws Throwable {
        int[] sequence = new int[2];
        String result = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern")
                .guessAutNumber("");
        assertNull(result, "result");
    }

    @Test
    public void testGuessAutNumber3() throws Throwable {
        int[] sequence = new int[1];
        String result = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern")
                .guessAutNumber("1");
        assertEquals("000001", result, "result");
    }

    @Test
    public void testGuessAutNumberThrowsNullPointerException() throws Throwable {
        int[] sequence = new int[2];
        try {
            new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern").guessAutNumber(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"s\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleSpecialField351() throws Throwable {
        int[] sequence = new int[0];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);

        int result = vISA1Packager.handleSpecialField35(new ISOMsg(100), bout);
        assertEquals(0, result, "result");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleSpecialField35ThrowsNullPointerException() throws Throwable {
        int[] sequence = new int[0];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);

        try {
            vISA1Packager.handleSpecialField35(null, bout);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.iso.ISOMsg.hasField(int)\" because \"m\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, bout.toByteArray().length);
        }
    }

    public void testPackThrowsISOException() throws Throwable {
        int[] sequence = new int[0];
        try {
            new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "testVISA1PackagerOkPattern").pack(new ISOField());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("Can't call VISA1 packager on non ISOMsg", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
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
        assertEquals(100, ((VISA1Packager) vISA1Packager.filter).respField, "vISA1Packager.filter.respField");
        assertEquals("testVISA1PackagerBadResultCode", ((VISA1Packager) vISA1Packager.filter).badResultCode,
                "vISA1Packager.filter.badResultCode");
        assertEquals("testVISA1PackagerOkPattern", ((VISA1Packager) vISA1Packager.filter).okPattern,
                "vISA1Packager.filter.okPattern");
        assertSame(filter, vISA1Packager.filter, "vISA1Packager.filter");
    }

    @Test
    public void testUnpack() throws Throwable {
        int[] sequence = new int[2];
        ISOComponent m = new ISOMsg(100);
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode", "");
        byte[] b = new byte[1];
        int result = vISA1Packager.unpack(m, b);
        assertEquals(100, m.getMaxField(), "(ISOMsg) m.getMaxField()");
        assertEquals(1, result, "result");
        assertSame(vISA1Packager, vISA1Packager.filter, "vISA1Packager.filter");
    }

    @Test
    public void testUnpack1() throws Throwable {
        int[] sequence = new int[0];
        VISA1Packager vISA1Packager = new VISA1Packager(sequence, 100, "testVISA1PackagerBadResultCode",
                "testVISA1PackagerOkPattern");
        ISOComponent m = new ISOMsg(100);
        byte[] b = new byte[0];
        int result = vISA1Packager.unpack(m, b);
        assertEquals(100, m.getMaxField(), "(ISOVMsg) m.getMaxField()");
        assertEquals(0, result, "result");
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
            assertEquals("not implemented", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
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
            assertEquals("Can't add to Leaf", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
            assertSame(vISA1Packager, vISA1Packager.filter, "vISA1Packager.filter");
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
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.iso.ISOComponent.set(org.jpos.iso.ISOComponent)\" because \"m\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertSame(vISA1Packager, vISA1Packager.filter, "vISA1Packager.filter");
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
            assertEquals(100, m.getMaxField(), "(ISOMsg) m.getMaxField()");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"prefix\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertSame(vISA1Packager, vISA1Packager.filter, "vISA1Packager.filter");
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
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read the array length because \"bytes\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertSame(vISA1Packager, vISA1Packager.filter, "vISA1Packager.filter");
        }
    }
}
