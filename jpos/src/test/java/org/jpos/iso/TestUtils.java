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

package org.jpos.iso;

import junit.framework.Assert;

/**
 * @author joconnor
 */
public class TestUtils {
    /** Tests for equality of array elements */
    public static void assertEquals(Object[] expected, Object[] was) {
        Assert.assertEquals("Wrong size array", expected.length, was.length);
        for (int i= 0; i < expected.length; i++) {
            Assert.assertEquals("Non-equal objects at index " + i, expected[i], was[i]);
        }
    }

    /** Tests for equality of array elements */
    public static void assertEquals(byte[] expected, byte[] was) {
        if (expected.length != was.length) {
            for (int i = 0; i < expected.length && i < was.length; i++) {
                Assert.assertEquals("Arrays different lengths. Non-equal objects at index " + i, expected[i], was[i]);
            }
            // Following always fails, but gives a nice error message
            Assert.assertEquals("Wrong size array", expected.length, was.length);
        } else {
            for (int i= 0; i < expected.length; i++) {
                Assert.assertEquals("Non-equal objects at index " + i, expected[i], was[i]);
            }
        }
    }
}
