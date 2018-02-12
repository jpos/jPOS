/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jpos.util.NameRegistrar;
import org.junit.Test;

public class ISOServerTest {

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new ISOServer(100, null, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetServerThrowsNotFoundException() throws Throwable {
        try {
            ISOServer.getServer("testISOServerName");
            fail("Expected NotFoundException to be thrown");
        } catch (NameRegistrar.NotFoundException ex) {
            assertEquals("ex.getMessage()", "server.testISOServerName", ex.getMessage());
        }
    }
}
