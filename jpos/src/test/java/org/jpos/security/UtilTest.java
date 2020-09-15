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

package org.jpos.security;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class UtilTest {

    @Test
    public void testAdjustDESParity() throws Throwable {
        byte[] bytes = new byte[1];
        Util.adjustDESParity(bytes);
        assertEquals((byte) 1, bytes[0], "bytes[0]");
    }

    @Test
    public void testAdjustDESParity1() throws Throwable {
        byte[] bytes = new byte[0];
        Util.adjustDESParity(bytes);
        assertEquals(0, bytes.length, "bytes.length");
    }

    @Test
    public void testAdjustDESParityThrowsNullPointerException() throws Throwable {
        try {
            Util.adjustDESParity(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read the array length because \"bytes\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        new Util();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testIsDESParityAdjusted() throws Throwable {
        byte[] bytes = new byte[2];
        boolean result = Util.isDESParityAdjusted(bytes);
        assertFalse(result, "result");
    }

    @Test
    public void testIsDESParityAdjusted1() throws Throwable {
        byte[] bytes = new byte[0];
        boolean result = Util.isDESParityAdjusted(bytes);
        assertTrue(result, "result");
    }

    @Test
    public void testIsDESParityAdjustedThrowsNullPointerException() throws Throwable {
        try {
            Util.isDESParityAdjusted(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"[B.clone()\" because \"bytes\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
