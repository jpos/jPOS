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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static org.apache.commons.lang3.JavaVersion.JAVA_10;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

import org.junit.jupiter.api.Test;

public class SignedEbcdicNumberInterpreter2Test {

    @Test
    public void testConstructor() throws Throwable {
        new SignedEbcdicNumberInterpreter();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = new SignedEbcdicNumberInterpreter().getPackedLength(100);
        assertEquals(100, result, "result");
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = new SignedEbcdicNumberInterpreter().getPackedLength(0);
        assertEquals(0, result, "result");
    }

    @Test
    public void testInterpret() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            new SignedEbcdicNumberInterpreter().interpret("", new byte[1], 100);
        });
    }

    @Test
    public void testInterpret1() throws Throwable {
        byte[] targetArray = new byte[3];
        new SignedEbcdicNumberInterpreter().interpret("-\u5483\uD0AF", targetArray, 0);
        assertEquals((byte) 63, targetArray[0], "targetArray[0]");
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            new SignedEbcdicNumberInterpreter().interpret("testSignedEbcdicNumberInterpreterData", new byte[2], 100);
        });
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            new SignedEbcdicNumberInterpreter().interpret("-\u5483\uD0AF", new byte[1], 100);
        });
    }

    @Test
    public void testInterpretThrowsNullPointerException() throws Throwable {
        try {
            new SignedEbcdicNumberInterpreter().interpret("-", null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testInterpretThrowsNullPointerException1() throws Throwable {
        byte[] targetArray = new byte[1];
        try {
            new SignedEbcdicNumberInterpreter().interpret(null, targetArray, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.startsWith(String)\" because \"data\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(1, targetArray.length, "targetArray.length");
        }
    }

    @Test
    public void testUninterpret() throws Throwable {
        byte[] rawData = new byte[3];
        String result = new SignedEbcdicNumberInterpreter().uninterpret(rawData, 0, 1);
        assertEquals((byte) -16, rawData[0], "rawData[0]");
        assertEquals("0", result, "result");
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            byte[] rawData = new byte[2];
            rawData[0] = (byte) -48;
            new SignedEbcdicNumberInterpreter().uninterpret(rawData, -48, 49);
        });
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] rawData = new byte[3];
        try {
            new SignedEbcdicNumberInterpreter().uninterpret(rawData, 100, 1000);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1099", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1099 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(3, rawData.length, "rawData.length");
        }
    }

    @Test
    public void testUninterpretThrowsNegativeArraySizeException() throws Throwable {
        byte[] rawData = new byte[3];
        rawData[0] = (byte) -39;
        try {
            new SignedEbcdicNumberInterpreter().uninterpret(rawData, 240, -239);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals((byte) -7, rawData[0], "rawData[0]");
            assertNull(ex.getMessage(), "ex.getMessage()");
            assertEquals(3, rawData.length, "rawData.length");
        }
    }

    @Test
    public void testUninterpretThrowsNullPointerException() throws Throwable {
        try {
            new SignedEbcdicNumberInterpreter().uninterpret(null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot load from byte/boolean array because \"rawData\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
