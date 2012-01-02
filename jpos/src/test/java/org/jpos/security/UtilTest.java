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

package org.jpos.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class UtilTest {

    @Test
    public void testAdjustDESParity() throws Throwable {
        byte[] bytes = new byte[1];
        Util.adjustDESParity(bytes);
        assertEquals("bytes[0]", (byte) 1, bytes[0]);
    }

    @Test
    public void testAdjustDESParity1() throws Throwable {
        byte[] bytes = new byte[0];
        Util.adjustDESParity(bytes);
        assertEquals("bytes.length", 0, bytes.length);
    }

    @Test
    public void testAdjustDESParityThrowsNullPointerException() throws Throwable {
        try {
            Util.adjustDESParity((byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        new Util();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testIsDESParityAdjusted() throws Throwable {
        byte[] bytes = new byte[2];
        boolean result = Util.isDESParityAdjusted(bytes);
        assertFalse("result", result);
    }

    @Test
    public void testIsDESParityAdjusted1() throws Throwable {
        byte[] bytes = new byte[0];
        boolean result = Util.isDESParityAdjusted(bytes);
        assertTrue("result", result);
    }

    @Test
    public void testIsDESParityAdjustedThrowsNullPointerException() throws Throwable {
        try {
            Util.isDESParityAdjusted((byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
