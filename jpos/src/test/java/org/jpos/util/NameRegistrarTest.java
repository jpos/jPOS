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

package org.jpos.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class NameRegistrarTest {

    @Test
    public void testDump() throws Throwable {
        NameRegistrar.getInstance().dump(new PrintStream(new ByteArrayOutputStream()), "testNameRegistrarIndent");
        assertTrue("Test completed without Exception", true);
        // dependencies on static and environment state led to removal of 1
        // assertion
    }

    @Test
    public void testDump1() throws Throwable {
        NameRegistrar.getInstance().dump(new PrintStream(new ByteArrayOutputStream()), "testNameRegistrarIndent", false);
        assertTrue("Test completed without Exception", true);
        // dependencies on static and environment state led to removal of 1
        // assertion
    }

    @Test
    public void testDump2() throws Throwable {
        NameRegistrar.getInstance().dump(new PrintStream(new ByteArrayOutputStream()), "testNameRegistrarIndent", true);
        assertTrue("Test completed without Exception", true);
        // dependencies on static and environment state led to removal of 1
        // assertion
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            NameRegistrar.getInstance().dump(null, "testNameRegistrarIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertTrue("Test completed without Exception", true);
            // dependencies on static and environment state led to removal of 3
            // assertions
        }
    }

    @Test
    public void testDumpThrowsNullPointerException1() throws Throwable {
        try {
            NameRegistrar.getInstance().dump(null, "testNameRegistrarIndent", true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertTrue("Test completed without Exception", true);
            // dependencies on static and environment state led to removal of 3
            // assertions
        }
    }

    @Test
    public void testGetInstance() throws Throwable {
        NameRegistrar result = NameRegistrar.getInstance();
        assertNotNull("result", result);
    }

    @Test
    public void testGetMap() throws Throwable {
        NameRegistrar.getMap();
        assertTrue("Test completed without Exception", true);
        // dependencies on static and environment state led to removal of 3
        // assertions
    }

    @Test
    public void testGetThrowsNotFoundException() throws Throwable {
        try {
            NameRegistrar.get("2C");
            fail("Expected NotFoundException to be thrown");
        } catch (NameRegistrar.NotFoundException ex) {
            assertEquals("ex.getMessage()", "2C", ex.getMessage());
        }
    }

    @Test
    public void testGetThrowsNullPointerException() throws Throwable {
        try {
            NameRegistrar.get(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testNotFoundExceptionConstructor() throws Throwable {
        new NameRegistrar.NotFoundException();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testNotFoundExceptionConstructor1() throws Throwable {
        NameRegistrar.NotFoundException notFoundException = new NameRegistrar.NotFoundException("testNotFoundExceptionDetail");
        assertEquals("notFoundException.getMessage()", "testNotFoundExceptionDetail", notFoundException.getMessage());
    }

    @Test
    public void testRegister() throws Throwable {
        NameRegistrar.register("testNameRegistrarKey", "");
        assertTrue("Test completed without Exception", true);
        // dependencies on static and environment state led to removal of 1
        // assertion
    }

    @Test
    public void testRegisterThrowsNullPointerException() throws Throwable {
        try {
            NameRegistrar.register(null, Integer.valueOf(1));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnregister() throws Throwable {
        NameRegistrar.unregister("testNameRegistrarKey");
        assertTrue("Test completed without Exception", true);
        // dependencies on static and environment state led to removal of 1
        // assertion
    }

    @Test
    public void testUnregisterThrowsNullPointerException() throws Throwable {
        try {
            NameRegistrar.unregister(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
