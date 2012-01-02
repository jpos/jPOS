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

package org.jpos.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InvalidCardExceptionTest {

    @Test
    public void testConstructor() throws Throwable {
        new InvalidCardException();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testConstructor1() throws Throwable {
        InvalidCardException invalidCardException = new InvalidCardException("testInvalidCardExceptions");
        assertEquals("invalidCardException.getMessage()", "testInvalidCardExceptions", invalidCardException.getMessage());
    }
}
