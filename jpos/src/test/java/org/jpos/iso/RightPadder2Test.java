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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class RightPadder2Test {

    @Test
    public void testPad() throws Throwable {
        String result = new RightPadder(' ').pad("", 100);
        assertEquals("                                                                                                    ",
                result, "result");
    }

    @Test
    public void testPad1() throws Throwable {
        String result = new RightPadder(' ').pad("", 0);
        assertEquals("", result, "result");
    }

    @Test
    public void testPadThrowsISOException() throws Throwable {
        try {
            new RightPadder(' ').pad("testRightPadderData", 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("Data is too long. Max = 0", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
        }
    }

    @Test
    public void testPadThrowsNullPointerException() throws Throwable {
        try {
            new RightPadder(' ').pad(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"data\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testUnpad() throws Throwable {
        String result = RightPadder.SPACE_PADDER.unpad(" ");
        assertEquals("", result, "result");
    }

    @Test
    public void testUnpad1() throws Throwable {
        String result = RightTPadder.SPACE_PADDER.unpad("X ");
        assertEquals("X", result, "result");
    }

    @Test
    public void testUnpad2() throws Throwable {
        String result = new RightPadder(' ').unpad("1");
        assertEquals("1", result, "result");
    }

    @Test
    public void testUnpad3() throws Throwable {
        String result = new RightPadder(' ').unpad("");
        assertEquals("", result, "result");
    }

    @Test
    public void testUnpadThrowsNullPointerException() throws Throwable {
        try {
            new RightPadder(' ').unpad(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"paddedData\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
