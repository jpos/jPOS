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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static org.apache.commons.lang3.JavaVersion.JAVA_10;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

import org.junit.jupiter.api.Test;

public class AsciiHexInterpreter2Test {

    @Test
    public void testConstructor() throws Throwable {
        new AsciiHexInterpreter();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = AsciiHexInterpreter.INSTANCE.getPackedLength(0);
        assertEquals(0, result, "result");
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = new AsciiHexInterpreter().getPackedLength(100);
        assertEquals(200, result, "result");
    }

    @Test
    public void testInterpret() throws Throwable {
        byte[] b = new byte[1];
        byte[] data = new byte[0];
        new AsciiHexInterpreter().interpret(data, b, 100);
        assertEquals(1, b.length, "b.length");
    }

    @Test
    public void testInterpret1() throws Throwable {
        byte[] data = new byte[1];
        byte[] b = new byte[5];
        AsciiHexInterpreter.INSTANCE.interpret(data, b, 0);
        assertEquals((byte) 48, b[0], "b[0]");
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] data = new byte[4];
        byte[] b = new byte[1];
        try {
            AsciiHexInterpreter.INSTANCE.interpret(data, b, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals((byte) 48, b[0], "b[0]");
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(1, b.length, "b.length");
        }
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] data = new byte[1];
        byte[] b = new byte[0];
        try {
            AsciiHexInterpreter.INSTANCE.interpret(data, b, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("100", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 100 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, b.length, "b.length");
        }
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] b = new byte[3];
        byte[] data = new byte[2];
        try {
            AsciiHexInterpreter.INSTANCE.interpret(data, b, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals((byte) 48, b[0], "b[0]");
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("3", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 3 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(3, b.length, "b.length");
        }
    }

    @Test
    public void testInterpretThrowsNullPointerException() throws Throwable {
        byte[] b = new byte[5];
        try {
            new AsciiHexInterpreter().interpret(null, b, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read the array length because \"data\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(5, b.length, "b.length");
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] rawData = new byte[68];
        rawData[2] = (byte) 65;
        rawData[3] = (byte) 64;
        rawData[5] = (byte) 65;
        rawData[12] = (byte) 64;
        rawData[65] = (byte) 63;
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("68", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 68 out of bounds for length 68", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] rawData = new byte[68];
        rawData[3] = (byte) 64;
        rawData[5] = (byte) 65;
        rawData[12] = (byte) 64;
        rawData[65] = (byte) 63;
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("68", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 68 out of bounds for length 68", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] rawData = new byte[68];
        rawData[2] = (byte) 65;
        rawData[3] = (byte) 64;
        rawData[12] = (byte) 64;
        rawData[65] = (byte) 63;
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("68", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 68 out of bounds for length 68", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException3() throws Throwable {
        byte[] rawData = new byte[67];
        rawData[65] = (byte) 65;
        rawData[66] = (byte) 65;
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 65, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("67", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 67 out of bounds for length 67", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException4() throws Throwable {
        byte[] rawData = new byte[2];
        rawData[0] = (byte) 63;
        rawData[1] = (byte) 65;
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("2", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 2 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException5() throws Throwable {
        byte[] rawData = new byte[5];
        rawData[3] = (byte) 64;
        rawData[4] = (byte) 64;
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 1, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("5", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 5 out of bounds for length 5", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException6() throws Throwable {
        byte[] rawData = new byte[1];
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(rawData, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException7() throws Throwable {
        byte[] rawData = new byte[3];
        rawData[0] = (byte) 66;
        try {
            new AsciiHexInterpreter().uninterpret(rawData, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("3", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 3 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUninterpretThrowsNegativeArraySizeException() throws Throwable {
        byte[] rawData = new byte[1];
        try {
            new AsciiHexInterpreter().uninterpret(rawData, 100, -1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("-1", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUninterpretThrowsNullPointerException() throws Throwable {
        try {
            AsciiHexInterpreter.INSTANCE.uninterpret(null, 100, 1000);
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
