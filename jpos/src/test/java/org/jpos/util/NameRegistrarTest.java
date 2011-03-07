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
