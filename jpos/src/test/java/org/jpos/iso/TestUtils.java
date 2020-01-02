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

import org.junit.jupiter.api.Assertions;

/**
 * @author joconnor
 */
public class TestUtils {
    /** Tests for equality of array elements */
    public static void assertEquals(Object[] expected, Object[] was) {
        Assertions.assertEquals(expected.length, was.length, "Wrong size array");
        for (int i= 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], was[i], "Non-equal objects at index " + i);
        }
    }

    /** Tests for equality of array elements */
    public static void assertEquals(byte[] expected, byte[] was) {
        if (expected.length != was.length) {
            for (int i = 0; i < expected.length && i < was.length; i++) {
                Assertions.assertEquals(expected[i], was[i], "Arrays different lengths. Non-equal objects at index " + i);
            }
            // Following always fails, but gives a nice error message
            Assertions.assertEquals(expected.length, was.length, "Wrong size array");
        } else {
            for (int i= 0; i < expected.length; i++) {
                Assertions.assertEquals(expected[i], was[i], "Non-equal objects at index " + i);
            }
        }
    }
}
