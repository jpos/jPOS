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

package org.jpos.q2;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import javax.management.ObjectName;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOFieldValidator;
import org.jpos.iso.IVA_ALPHANUM;
import org.jpos.q2.iso.ChannelAdaptor;
import org.jpos.transaction.participant.BSHTransactionParticipant;
import org.jpos.transaction.participant.Join;
import org.junit.jupiter.api.Test;

public class QFactory2Test {
    @Test
    public void testConstructor() throws Throwable {
        ObjectName loaderName = new ObjectName("");
        String[] args = new String[0];
        Q2 q2 = mock(Q2.class);
        QFactory qFactory = new QFactory(loaderName, q2);
        assertTrue(qFactory.classMapping.getKeys().hasMoreElements(),
                "qFactory.classMapping.getKeys().hasMoreElements()");
        assertSame(loaderName, qFactory.loaderName, "qFactory.loaderName");
        assertSame(q2, qFactory.q2, "qFactory.q2");
    }

    @Test
    public void testCreateQBeanThrowsNullPointerException() throws Throwable {
        Element e = new Element("testQFactoryName", "testQFactoryPrefix", "testQFactoryUri");
        try {
            new QFactory(new ObjectName(""), null).createQBean(null, e, new Join());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.Q2.getMBeanServer()\" because \"server\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals("testQFactoryName", e.getName(), "e.getName()");
        }
    }

    @Test
    public void testCreateQBeanThrowsNullPointerException2() throws Throwable {
        String[] args = new String[0];
        String[] args2 = new String[3];
        args2[0] = "Q2-ShutdownHook";
        args2[1] = "Q2:type=dystem,service=loader";
        args2[2] = ".";
        Q2 q2 = new Q2(args);
        Q2 q2_2 = new Q2(args2);
        try {
            new QFactory(null, q2).createQBean(q2_2, null, new Join());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getAttributeValue(String)\" because \"e\" is null", ex.getMessage(), "ex.getMessage()");
            }
        } finally {
            q2.stop();
            q2_2.stop();
        }
    }

    @Test
    public void testDestroyQBeanThrowsNullPointerException1() throws Throwable {
        String[] args = new String[0];
        String keyParam = "testString";
        String valueParam = "testString";
        Q2 q2 = new Q2(args);
        try {
            new QFactory(ObjectName.getInstance("testQFactoryParam1", keyParam, valueParam), null).destroyQBean(q2, new ObjectName(
                    "testQFactoryParam1", "testQFactoryParam2", "testQFactoryParam3"), new ChannelAdaptor());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"javax.management.MBeanServer.invoke(javax.management.ObjectName, String, Object[], String[])\" because \"mserver\" is null", ex.getMessage(), "ex.getMessage()");
            }
        } finally {
            q2.stop();
        }
    }

    @Test
    public void testGetAttributeName() throws Throwable {
        String result = new QFactory(new ObjectName(""), null).getAttributeName("testQFactoryName");
        assertEquals("TestQFactoryName", result, "result");
    }

    @Test
    public void testGetAttributeNameThrowsNullPointerException() throws Throwable {
        try {
            new QFactory(new ObjectName(""), null).getAttributeName(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("attribute name can not be null", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testGetObject() throws Throwable {
        QFactory qFactory = new QFactory(new ObjectName(""), null);
        String result = (String) qFactory.getObject(new Element("testQFactoryName", "testQFactoryPrefix", "testQFactoryUri"));
        assertEquals("", result, "result");
    }

    @Test
    public void testGetObjectThrowsNullPointerException() throws Throwable {
        QFactory qFactory = new QFactory(new ObjectName(""), null);
        try {
            qFactory.getObject(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getAttributeValue(String, String)\" because \"childElement\" is null", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.Q2.getMBeanServer()\" because \"server\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertTrue(qFactory.classMapping.getKeys().hasMoreElements(),
                    "qFactory.classMapping.getKeys().hasMoreElements()");
            assertSame(q2, qFactory.q2, "qFactory.q2");
            assertEquals("testQFactoryName", e.getName(), "e.getName()");
        } finally {
            q2.stop();
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
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"javax.management.MBeanServer.instantiate(String, javax.management.ObjectName)\" because \"mserver\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertTrue(qFactory.classMapping.getKeys().hasMoreElements(),
                    "qFactory.classMapping.getKeys().hasMoreElements()");
            assertSame(q2, qFactory.q2, "qFactory.q2");
            assertEquals("testQFactoryName", e.getName(), "e.getName()");
            assertSame(args2, server.getCommandLineArgs(), "server.getCommandLineArgs()");
        }
        q2.stop();
        server.stop();
    }

    @Test
    public void testInvoke() throws Throwable {
        QFactory.invoke("", "testQFactorym", "testString", Integer.class);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testInvoke1() throws Throwable {
        QFactory.invoke(null, "testQFactorym", "testString", Integer.class);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testInvoke2() throws Throwable {
        QFactory.invoke("", "testQFactorym", null);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testInvoke3() throws Throwable {
        QFactory.invoke("", "testQFactorym", "");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        String[] args = new String[0];
        Element e = new Element("testQFactoryName");
        Q2 q2 = new Q2(args);
        new QFactory(new ObjectName(""), q2).setConfiguration("", e);
        assertEquals("testQFactoryName", e.getName(), "e.getName()");
        q2.stop();
    }

    @Test
    public void testSetConfiguration1() throws Throwable {
        String[] args = new String[0];
        ISOFieldValidator obj = new ISOFieldValidator(true);
        Q2 q2 = new Q2(args);
        new QFactory(new ObjectName(""), q2).setConfiguration(obj, new Element("testQFactoryName"));
        assertFalse(obj.breakOnError(), "obj.breakOnError()");
        q2.stop();
    }

    @Test
    public void testSetConfiguration3() throws Throwable {
        String[] args = new String[0];
        Element e = new Element("testQFactoryName", "testQFactoryPrefix", "testQFactoryUri");
        Q2 q2 = new Q2(args);
        new QFactory(null, q2).setConfiguration(new BSHTransactionParticipant(), e);
        assertEquals("testQFactoryName", e.getName(), "e.getName()");
        q2.stop();
    }

    @Test
    public void testSetConfigurationThrowsConfigurationException() throws Throwable {
        String[] args = new String[0];
        Q2 q2 = new Q2(args);
        try {
            new QFactory(null, q2).setConfiguration(new BSHTransactionParticipant(), null);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertEquals("org.jpos.core.ConfigurationException (java.lang.NullPointerException)", ex.getMessage(), "ex.getMessage()");
                assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            } else {
                assertEquals("org.jpos.core.ConfigurationException: Cannot invoke \"org.jdom2.Element.getChild(String)\" because \"e\" is null (java.lang.NullPointerException: Cannot invoke \"org.jdom2.Element.getChild(String)\" because \"e\" is null)", ex.getMessage(), "ex.getMessage()");
                assertEquals("Cannot invoke \"org.jdom2.Element.getChild(String)\" because \"e\" is null", ex.getNested().getMessage(), "ex.getNested().getMessage()");
            }
        } finally {
            q2.stop();
        }
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        String[] args = new String[0];
        Q2 q2 = new Q2(args);
        try {
            new QFactory(new ObjectName(""), q2).setConfiguration(new IVA_ALPHANUM(true, 100, 1000,
                    "testQFactoryDescription"), null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getAttributeValue(String)\" because \"e\" is null", ex.getMessage(), "ex.getMessage()");
            }
        } finally {
            q2.stop();
        }
    }

    @Test
    public void testSetLogger() throws Throwable {
        String[] args = new String[0];
        Element e = new Element("testQFactoryName");
        Q2 q2 = new Q2(args);
        new QFactory(new ObjectName(""), q2).setLogger(new Object(), e);
        assertEquals("testQFactoryName", e.getName(), "e.getName()");
        q2.stop();
    }

    @Test
    public void testStartQBeanThrowsNullPointerException() throws Throwable {
        String[] args = new String[0];
        Q2 q2 = new Q2(args);
        try {
            new QFactory(null, q2).startQBean(q2, new ObjectName(""));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"javax.management.MBeanServer.invoke(javax.management.ObjectName, String, Object[], String[])\" because \"mserver\" is null", ex.getMessage(), "ex.getMessage()");
            }
        } finally {
            q2.stop();
        }
    }
}
