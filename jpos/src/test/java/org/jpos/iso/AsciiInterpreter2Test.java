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

public class AsciiInterpreter2Test {

    @Test
    public void testConstructor() throws Throwable {
        new AsciiInterpreter();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = new AsciiInterpreter().getPackedLength(0);
        assertEquals(0, result, "result");
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = new AsciiInterpreter().getPackedLength(100);
        assertEquals(100, result, "result");
    }

    @Test
    public void testInterpret() throws Throwable {
        byte[] b = new byte[10];
        new AsciiInterpreter().interpret("10Characte", b, 0);
        assertEquals((byte) 49, b[0], "b[0]");
    }

    @Test
    public void testInterpretThrowsNullPointerException() throws Throwable {
        try {
            new AsciiInterpreter().interpret("testAsciiInterpreterData", null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testInterpretThrowsNullPointerException1() throws Throwable {
        byte[] b = new byte[2];
        try {
            new AsciiInterpreter().interpret(null, b, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.getBytes(java.nio.charset.Charset)\" because \"data\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(2, b.length, "b.length");
        }
    }

    @Test
    public void testUninterpret() throws Throwable {
        byte[] rawData = new byte[2];
        String result = new AsciiInterpreter().uninterpret(rawData, 0, 1);
        assertEquals("\u0000", result, "result");
    }

    @Test
    public void testUninterpretThrowsNegativeArraySizeException() throws Throwable {
        byte[] rawData = new byte[0];
        try {
            new AsciiInterpreter().uninterpret(rawData, 100, -1);
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
            new AsciiInterpreter().uninterpret(null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }
}
