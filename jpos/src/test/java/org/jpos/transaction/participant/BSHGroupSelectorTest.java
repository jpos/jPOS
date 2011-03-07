package org.jpos.transaction.participant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.StreamCorruptedException;

import org.jdom.Element;
import org.jpos.core.ConfigurationException;
import org.junit.Test;

public class BSHGroupSelectorTest {

    @Test
    public void testConstructor() throws Throwable {
        BSHGroupSelector bSHGroupSelector = new BSHGroupSelector();
        assertNull("bSHGroupSelector.getRealm()", bSHGroupSelector.getRealm());
        assertNull("bSHGroupSelector.getLogger()", bSHGroupSelector.getLogger());
    }

    @Test
    public void testDefaultSelect() throws Throwable {
        String result = new BSHGroupSelector().defaultSelect(100L, new StreamCorruptedException());
        assertEquals("result", "", result);
    }

    @Test
    public void testSelect() throws Throwable {
        String result = new BSHGroupSelector().select(100L, new EOFException());
        assertEquals("result", "", result);
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        BSHGroupSelector bSHGroupSelector = new BSHGroupSelector();
        bSHGroupSelector.setConfiguration(new Element("testBSHGroupSelectorName", "testBSHGroupSelectorUri"));
        assertNull("bSHGroupSelector.prepareForAbortMethod", bSHGroupSelector.prepareForAbortMethod);
        assertNull("bSHGroupSelector.selectMethod", bSHGroupSelector.selectMethod);
        assertNull("bSHGroupSelector.commitMethod", bSHGroupSelector.commitMethod);
        assertNull("bSHGroupSelector.abortMethod", bSHGroupSelector.abortMethod);
        assertNull("bSHGroupSelector.prepareMethod", bSHGroupSelector.prepareMethod);
        assertFalse("bSHGroupSelector.trace", bSHGroupSelector.trace);
    }

    @Test
    public void testSetConfigurationThrowsConfigurationException() throws Throwable {
        BSHGroupSelector bSHGroupSelector = new BSHGroupSelector();
        try {
            bSHGroupSelector.setConfiguration(null);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
            assertNull("bSHGroupSelector.prepareForAbortMethod", bSHGroupSelector.prepareForAbortMethod);
            assertNull("bSHGroupSelector.selectMethod", bSHGroupSelector.selectMethod);
            assertNull("bSHGroupSelector.commitMethod", bSHGroupSelector.commitMethod);
            assertNull("bSHGroupSelector.abortMethod", bSHGroupSelector.abortMethod);
            assertNull("bSHGroupSelector.prepareMethod", bSHGroupSelector.prepareMethod);
            assertFalse("bSHGroupSelector.trace", bSHGroupSelector.trace);
        }
    }
}
