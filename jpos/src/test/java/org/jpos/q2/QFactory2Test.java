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

package org.jpos.q2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.Hashtable;
import java.util.PropertyResourceBundle;

import javax.management.ObjectName;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOFieldValidator;
import org.jpos.iso.IVA_ALPHANUM;
import org.jpos.q2.iso.ChannelAdaptor;
import org.jpos.transaction.participant.BSHTransactionParticipant;
import org.jpos.transaction.participant.Join;
import org.junit.Test;

public class QFactory2Test {
    @Test
    public void testConstructor() throws Throwable {
        ObjectName loaderName = new ObjectName("");
        String[] args = new String[0];
        Q2 q2 = mock(Q2.class);
        QFactory qFactory = new QFactory(loaderName, q2);
        assertTrue("qFactory.classMapping.getKeys().hasMoreElements()", ((PropertyResourceBundle) qFactory.classMapping).getKeys()
                .hasMoreElements());
        assertSame("qFactory.loaderName", loaderName, qFactory.loaderName);
        assertSame("qFactory.q2", q2, qFactory.q2);
    }

    @Test
    public void testCreateQBeanThrowsNullPointerException() throws Throwable {
        Element e = new Element("testQFactoryName", "testQFactoryPrefix", "testQFactoryUri");
        try {
            new QFactory(new ObjectName(""), null).createQBean(null, e, new Join());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("e.getName()", "testQFactoryName", e.getName());
        }
    }

    @Test
    public void testCreateQBeanThrowsNullPointerException2() throws Throwable {
        String[] args = new String[0];
        String[] args2 = new String[3];
        args2[0] = "Q2-ShutdownHook";
        args2[1] = "Q2:type=dystem,service=loader";
        args2[2] = ".";
        try {
            new QFactory(null, new Q2(args)).createQBean(new Q2(args2), null, new Join());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testDestroyQBeanThrowsNullPointerException1() throws Throwable {
        String[] args = new String[0];
        Hashtable<String, String> hashtable = new Hashtable<String, String>(100, 100.0F);
        hashtable.put("testString", "testString");
        try {
            new QFactory(ObjectName.getInstance("testQFactoryParam1", hashtable), null).destroyQBean(new Q2(args), new ObjectName(
                    "testQFactoryParam1", "testQFactoryParam2", "testQFactoryParam3"), new ChannelAdaptor());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetAttributeName() throws Throwable {
        String result = new QFactory(new ObjectName(""), null).getAttributeName("testQFactoryName");
        assertEquals("result", "TestQFactoryName", result);
    }

    @Test
    public void testGetAttributeNameThrowsNullPointerException() throws Throwable {
        try {
            new QFactory(new ObjectName(""), null).getAttributeName(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetAttributeNameThrowsStringIndexOutOfBoundsException() throws Throwable {
        String[] args = new String[0];
        try {
            new QFactory(new ObjectName("testQFactoryParam1", "testQFactoryParam2", "testQFactoryParam3"), new Q2(args))
                    .getAttributeName("");
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "String index out of range: 0", ex.getMessage());
        }
    }

    @Test
    public void testGetObject() throws Throwable {
        QFactory qFactory = new QFactory(new ObjectName(""), null);
        String result = (String) qFactory.getObject(new Element("testQFactoryName", "testQFactoryPrefix", "testQFactoryUri"));
        assertEquals("result", "", result);
    }

    @Test
    public void testGetObjectThrowsNullPointerException() throws Throwable {
        QFactory qFactory = new QFactory(new ObjectName(""), null);
        try {
            qFactory.getObject(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testInstantiateThrowsNullPointerException1() throws Throwable {
        String[] args = new String[0];
        Q2 q2 = new Q2(args);
        QFactory qFactory = new QFactory(null, q2);
        Element e = new Element("testQFactoryName", "testQFactoryUri");
        try {
            qFactory.instantiate(null, e);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertTrue("qFactory.classMapping.getKeys().hasMoreElements()", ((PropertyResourceBundle) qFactory.classMapping)
                    .getKeys().hasMoreElements());
            assertSame("qFactory.q2", q2, qFactory.q2);
            assertEquals("e.getName()", "testQFactoryName", e.getName());
        }
    }

    @Test
    public void testInstantiateThrowsNullPointerException3() throws Throwable {
        String[] args = new String[0];
        Q2 q2 = new Q2(args);
        Element element = new Element("testQFactoryName", "testQFactoryUri");
        element.setAttribute(new Attribute("testQFactoryName", "testQFactoryValue", 0));
        Element e = (Element) element.clone();
        QFactory qFactory = new QFactory(new ObjectName("testQFactoryParam1", "testQFactoryParam2", "testQFactoryParam3"), q2);
        e.setName("testQFactoryName");
        String[] args2 = new String[2];
        args2[0] = "stat";
        args2[1] = "undeplying:";
        Q2 server = new Q2(args2);
        try {
            qFactory.instantiate(server, e);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertTrue("qFactory.classMapping.getKeys().hasMoreElements()", ((PropertyResourceBundle) qFactory.classMapping)
                    .getKeys().hasMoreElements());
            assertSame("qFactory.q2", q2, qFactory.q2);
            assertEquals("e.getName()", "testQFactoryName", e.getName());
            assertSame("server.getCommandLineArgs()", args2, server.getCommandLineArgs());
        }
    }

    @Test
    public void testInvoke() throws Throwable {
        QFactory.invoke("", "testQFactorym", "testString", Integer.class);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testInvoke1() throws Throwable {
        QFactory.invoke(null, "testQFactorym", "testString", Integer.class);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testInvoke2() throws Throwable {
        QFactory.invoke("", "testQFactorym", null);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testInvoke3() throws Throwable {
        QFactory.invoke("", "testQFactorym", "");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        String[] args = new String[0];
        Element e = new Element("testQFactoryName");
        new QFactory(new ObjectName(""), new Q2(args)).setConfiguration("", e);
        assertEquals("e.getName()", "testQFactoryName", e.getName());
    }

    @Test
    public void testSetConfiguration1() throws Throwable {
        String[] args = new String[0];
        ISOFieldValidator obj = new ISOFieldValidator(true);
        new QFactory(new ObjectName(""), new Q2(args)).setConfiguration(obj, new Element("testQFactoryName"));
        assertFalse("obj.breakOnError()", obj.breakOnError());
    }

    @Test
    public void testSetConfiguration3() throws Throwable {
        String[] args = new String[0];
        Element e = new Element("testQFactoryName", "testQFactoryPrefix", "testQFactoryUri");
        new QFactory(null, new Q2(args)).setConfiguration(new BSHTransactionParticipant(), e);
        assertEquals("e.getName()", "testQFactoryName", e.getName());
    }

    @Test
    public void testSetConfigurationThrowsConfigurationException() throws Throwable {
        String[] args = new String[0];
        try {
            new QFactory(null, new Q2(args)).setConfiguration(new BSHTransactionParticipant(), null);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertEquals("ex.getMessage()", "org.jpos.core.ConfigurationException (java.lang.NullPointerException)", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        String[] args = new String[0];
        try {
            new QFactory(new ObjectName(""), new Q2(args)).setConfiguration(new IVA_ALPHANUM(true, 100, 1000,
                    "testQFactoryDescription"), null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetLogger() throws Throwable {
        String[] args = new String[0];
        Element e = new Element("testQFactoryName");
        new QFactory(new ObjectName(""), new Q2(args)).setLogger(new Object(), e);
        assertEquals("e.getName()", "testQFactoryName", e.getName());
    }

    @Test
    public void testStartQBeanThrowsNullPointerException() throws Throwable {
        String[] args = new String[0];
        Q2 q2 = new Q2(args);
        try {
            new QFactory(null, q2).startQBean(q2, new ObjectName(""));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
