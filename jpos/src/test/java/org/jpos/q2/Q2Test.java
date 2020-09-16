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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jpos.q2.qbean.SystemMonitor;
import org.jpos.util.Log;
import org.junit.jupiter.api.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Q2Test {
    static String[] m_args = new String[0];
    static Q2 m_q2;

    @BeforeAll
    public static void setUp() {
        m_q2 = new Q2(m_args);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        m_q2.shutdown(true);
    }

    @Test
    public void testAccept() throws Throwable {
        String[] args = new String[2];
        args[0] = "";
        args[1] = "";
        Q2 q2 = new Q2(args);
        boolean result = q2.accept(new File("testQ2Param1"));
        assertFalse(result, "result");
        q2.stop();
    }

    @Test
    public void testAcceptThrowsNullPointerException() throws Throwable {
        String[] args = new String[1];
        args[0] = "";
        Q2 q2 = new Q2(args);
        try {
            q2.accept(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.File.canRead()\" because \"f\" is null", ex.getMessage(), "ex.getMessage()");
            }
        } finally {
            q2.stop();
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        assertEquals("deploy", m_q2.getDeployDir().getName(), "m_q2.getDeployDir().getName()");
        assertSame(m_args, m_q2.getCommandLineArgs(), "m_q2.getCommandLineArgs()");
    }

    @Test
    public void testDecrypt() throws Throwable {
        String[] args = new String[2];
        args[0] = "testString";
        args[1] = "testString";
        Q2 q2 = new Q2(args);
        Document doc = mock(Document.class);
        Element element = mock(Element.class);
        given(doc.getRootElement()).willReturn(element);
        given(element.getName()).willReturn("testString");
        Document result = q2.decrypt(doc);
        assertSame(doc, result, "result");
        q2.stop();
    }

    @Test
    public void testDecryptThrowsIllegalStateException() throws Throwable {
        try {
            m_q2.decrypt(new Document());
            fail("Expected IllegalStateException to be thrown");
        } catch (IllegalStateException ex) {
            assertEquals("Root element not set", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testDecryptThrowsNullPointerException() throws Throwable {
        String[] args = new String[1];
        args[0] = "testString";
        Q2 q2 = new Q2(args);
        try {
            q2.decrypt(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Document.getRootElement()\" because \"doc\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
        q2.stop();
    }

    @Disabled
    @Test
    public void testEncryptThrowsIllegalStateException() throws Throwable {
        try {
            m_q2.encrypt(new Document());
            fail("Expected IllegalStateException to be thrown");
        } catch (IllegalStateException ex) {
            assertEquals("Root element not set", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testGetCommandLineArgs() throws Throwable {
        String[] args = new String[1];
        args[0] = "testString";
        Q2 q2 = new Q2(args);
        String[] result = q2.getCommandLineArgs();
        assertSame(args, result, "result");
        assertEquals("testString", args[0], "m_args[0]");
        q2.stop();
    }

    @Test
    public void testGetDeployDir() throws Throwable {
        String[] args = new String[0];
        Q2 q2 = new Q2(args);
        File result = q2.getDeployDir();
        assertEquals("deploy", result.getName(), "result.getName()");
        q2.stop();
    }

    @Test
    public void testGetFactory() throws Throwable {
        String[] args = new String[2];
        args[0] = "testString";
        args[1] = "testString";
        Q2 q2 = new Q2(args);
        QFactory result = q2.getFactory();
        assertNull(result, "result");
        q2.stop();
    }

    @Test
    public void testGetKey() throws Throwable {
        String[] args = new String[2];
        args[0] = "testString";
        args[1] = "testString";
        Q2 q2 = new Q2(args);
        byte[] result = q2.getKey();
        assertEquals(8, result.length, "result.length");
        assertEquals((byte) 67, result[0], "result[0]");
    }

    @Test
    public void testGetLoader() throws Throwable {
        QClassLoader result = m_q2.getLoader();
        assertNull(result, "result");
    }

    @Test
    public void testGetLog() throws Throwable {
        Log result = m_q2.getLog();
        assertEquals("Q2.system", result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testGetLog2() throws Throwable {
        String[] args = new String[2];
        args[0] = "testString";
        args[1] = "testString";
        Q2 q2 = new Q2(args);
        Log log = q2.getLog();
        Log result = q2.getLog();
        assertSame(log, result, "result");
        q2.stop();
    }

    @Test
    public void testGetMBeanServer() throws Throwable {
        String[] args = new String[0];
        Q2 q2 = new Q2(args);
        MBeanServer result = q2.getMBeanServer();
        assertNull(result, "result");
        q2.stop();
    }

    @Test
    public void testQEntryConstructor() throws Throwable {
        ObjectInstance instance = new ObjectInstance(new ObjectName("testQEntryParam1", "testQEntryParam2", "testQEntryParam3"),
                "testQEntryParam2");
        Q2.QEntry qEntry = new Q2.QEntry(100L, instance);
        assertEquals(100L, qEntry.deployed, "qEntry.deployed");
        assertSame(instance, qEntry.instance, "qEntry.instance");
    }


    @Test
    public void testQEntryGetDeployed() throws Throwable {
        long result = new Q2.QEntry(0L, new ObjectInstance(
                new ObjectName("testQEntryParam1", "testQEntryParam2", "testQEntryParam3"), "testQEntryParam2")).getDeployed();
        assertEquals(0L, result, "result");
    }

    @Test
    public void testQEntryGetDeployed1() throws Throwable {
        long result = new Q2.QEntry(100L, new ObjectInstance(new ObjectName("testQEntryParam1", "testQEntryParam2",
                "testQEntryParam3"), "testQEntryParam2")).getDeployed();
        assertEquals(100L, result, "result");
    }

    @Test
    public void testQEntryGetInstance() throws Throwable {
        ObjectInstance instance = new ObjectInstance(new ObjectName("testQEntryParam1", "testQEntryParam2", "testQEntryParam3"),
                "testQEntryParam2");
        ObjectInstance result = new Q2.QEntry(100L, instance).getInstance();
        assertSame(instance, result, "result");
    }

    @Test
    public void testQEntryGetObject() throws Throwable {
        Object result = new Q2.QEntry(100L, null).getObject();
        assertNull(result, "result");
    }

    @Test
    public void testQEntryGetObject1() throws Throwable {
        Q2.QEntry qEntry = new Q2.QEntry(100L, new ObjectInstance(new ObjectName("testQEntryParam1", "testQEntryParam2",
                "testQEntryParam3"), "testQEntryParam2"));
        Object obj = new Object();
        qEntry.setObject(obj);
        Object result = qEntry.getObject();
        assertSame(obj, result, "result");
    }

    @Test
    public void testQEntryGetObjectName() throws Throwable {
        ObjectName result = new Q2.QEntry(100L, null).getObjectName();
        assertNull(result, "result");
    }

    @Test
    public void testQEntryGetObjectName1() throws Throwable {
        ObjectName objectName = new ObjectName("testQEntryParam1", "testQEntryParam2", "testQEntryParam3");
        ObjectName result = new Q2.QEntry(100L, new ObjectInstance(objectName, "testQEntryParam2")).getObjectName();
        assertSame(objectName, result, "result");
    }

    @Test
    public void testQEntryIsQBean() throws Throwable {
        boolean result = new Q2.QEntry(100L, new ObjectInstance(new ObjectName("testQEntryParam1", "testQEntryParam2",
                "testQEntryParam3"), "testQEntryParam2")).isQBean();
        assertFalse(result, "result");
    }

    @Test
    public void testQEntryIsQBean1() throws Throwable {
        Q2.QEntry qEntry = new Q2.QEntry(100L, new ObjectInstance(new ObjectName("testQEntryParam1", "testQEntryParam2",
                "testQEntryParam3"), "testQEntryParam2"));
        qEntry.setObject(new SystemMonitor());
        boolean result = qEntry.isQBean();
        assertTrue(result, "result");
    }

    @Test
    public void testQEntryIsQPersist1() throws Throwable {
        boolean result = new Q2.QEntry(100L, new ObjectInstance(new ObjectName("testQEntryParam1", "testQEntryParam2",
                "testQEntryParam3"), "testQEntryParam2")).isQPersist();
        assertFalse(result, "result");
    }

    @Test
    public void testQEntrySetInstance() throws Throwable {
        ObjectInstance instance = new ObjectInstance(new ObjectName("testQEntryParam1", "testQEntryParam2", "testQEntryParam3"),
                "testQEntryParam2");
        Q2.QEntry qEntry = new Q2.QEntry(100L, instance);
        qEntry.setInstance(instance);
        assertSame(instance, qEntry.instance, "qEntry.instance");
    }

    @Test
    public void testQEntrySetObject() throws Throwable {
        Q2.QEntry qEntry = new Q2.QEntry(100L, new ObjectInstance(new ObjectName("testQEntryParam1", "testQEntryParam2",
                "testQEntryParam3"), "testQEntryParam2"));
        qEntry.setObject("");
        assertEquals("", qEntry.obj, "qEntry.obj");
    }

    @Test
    public void testRelax() throws Throwable {
        m_q2.relax(0L);
        assertSame(m_args, m_q2.getCommandLineArgs(), "m_q2.getCommandLineArgs()");
    }

    @Disabled
    @Test
    public void testRelaxThrowsIllegalArgumentException() throws Throwable {
        String[] args = new String[0];
        try {
            new Q2(args).relax(-100L);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("timeout value is negative", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testShutdown() throws Throwable {
        String[] args = new String[1];
        args[0] = "testString";
        Q2 q2 = new Q2(args);
        q2.shutdown(true);
    }

    @Test
    public void testShutdown1() throws Throwable {
        String[] args = new String[1];
        args[0] = "undeploed:";
        Q2 q2 = new Q2(args);
        q2.shutdown();
    }

    @Test
    public void testAdditionalInstance() {
        m_q2.start();
        m_q2.ready(5000L);
        Q2 q2 = new Q2();
        q2.start();
        q2.ready(5000L);
        m_q2.shutdown();
        q2.shutdown();
    }
}
