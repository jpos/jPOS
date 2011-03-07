package org.jpos.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.junit.Test;

public class ProtectedLogListenerTest {

    @Test
    public void testConstructor() throws Throwable {
        ProtectedLogListener protectedLogListener = new ProtectedLogListener();
        assertNull("protectedLogListener.wipeFields", protectedLogListener.wipeFields);
        assertNull("protectedLogListener.protectFields", protectedLogListener.protectFields);
        assertNull("protectedLogListener.cfg", protectedLogListener.cfg);
    }

    @Test
    public void testLogThrowsNullPointerException() throws Throwable {
        try {
            new ProtectedLogListener().log(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        ProtectedLogListener protectedLogListener = new ProtectedLogListener();
        Configuration cfg = new SimpleConfiguration();
        protectedLogListener.setConfiguration(cfg);
        assertEquals("protectedLogListener.protectFields.length", 0, protectedLogListener.protectFields.length);
        assertEquals("protectedLogListener.wipeFields.length", 0, protectedLogListener.wipeFields.length);
        assertSame("protectedLogListener.cfg", cfg, protectedLogListener.cfg);
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        ProtectedLogListener protectedLogListener = new ProtectedLogListener();
        Configuration cfg = new SubConfiguration();
        try {
            protectedLogListener.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertSame("protectedLogListener.cfg", cfg, protectedLogListener.cfg);
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("protectedLogListener.wipeFields", protectedLogListener.wipeFields);
            assertNull("protectedLogListener.protectFields", protectedLogListener.protectFields);
        }
    }
}
