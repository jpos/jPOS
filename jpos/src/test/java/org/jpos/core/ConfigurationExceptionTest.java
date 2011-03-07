package org.jpos.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ConfigurationExceptionTest {

    @Test
    public void testConstructor() throws Throwable {
        Throwable nested = new ConfigurationException();
        ConfigurationException configurationException = new ConfigurationException("testConfigurationExceptionDetail", nested);
        assertEquals("configurationException.getMessage()", "testConfigurationExceptionDetail", configurationException.getMessage());
        assertSame("configurationException.getNested()", nested, configurationException.getNested());
    }

    @Test
    public void testConstructor1() throws Throwable {
        Throwable nested = new ConfigurationException();
        ConfigurationException configurationException = new ConfigurationException(nested);
        assertEquals("configurationException.getMessage()", "org.jpos.core.ConfigurationException",
                configurationException.getMessage());
        assertSame("configurationException.getNested()", nested, configurationException.getNested());
    }

    @Test
    public void testConstructor2() throws Throwable {
        ConfigurationException configurationException = new ConfigurationException("testConfigurationExceptionDetail");
        assertEquals("configurationException.getMessage()", "testConfigurationExceptionDetail", configurationException.getMessage());
        assertNull("configurationException.getNested()", configurationException.getNested());
    }

    @Test
    public void testConstructor3() throws Throwable {
        ConfigurationException configurationException = new ConfigurationException();
        assertNull("configurationException.getNested()", configurationException.getNested());
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new ConfigurationException((Throwable) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
