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

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class EbcdicInterpreter2Test {

    @Test
    public void testConstructor() throws Throwable {
        new EbcdicInterpreter();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = new EbcdicInterpreter().getPackedLength(100);
        assertEquals(100, result, "result");
    }

    @Test
    public void testInterpret() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            new EbcdicInterpreter().interpret("", new byte[2], 100);
        });
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            new EbcdicInterpreter().interpret("testEbcdicInterpreterData", new byte[1], 100);
        });
    }

    @Test
    public void testInterpretThrowsNullPointerException() throws Throwable {
        try {
            EbcdicInterpreter.INSTANCE.interpret("testEbcdicInterpreterData", null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testUninterpret() throws Throwable {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            byte[] rawData = new byte[0];
            String result = EbcdicInterpreter.INSTANCE.uninterpret(rawData, 100, 0);
            assertEquals("", result, "result");
        });
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            new EbcdicInterpreter().uninterpret(new byte[0], 100, 1000);
        });
    }

    @Test
    public void testUninterpretThrowsNegativeArraySizeException() throws Throwable {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            new EbcdicInterpreter().uninterpret(new byte[2], 100, -1);
        });
    }

    @Test
    public void testUninterpretThrowsNullPointerException() throws Throwable {
        try {
            new EbcdicInterpreter().uninterpret(null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read the array length because \"buf\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
