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

package org.jpos.q2.iso;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.jdom2.Element;
import org.junit.jupiter.api.Test;

public class QServerTest {

    @Test
    public void testConstructor() throws Throwable {
        QServer qServer = new QServer();
        assertEquals("org.jpos.q2.iso.QServer", qServer.getLog().getRealm(), "qServer.getLog().getRealm()");
        assertEquals(-1, qServer.getState(), "qServer.getState()");
        assertEquals(100, qServer.getMaxSessions(), "qServer.getMaxSessions()");
        assertEquals(1, qServer.getMinSessions(), "qServer.getMinSessions()");
        assertTrue(qServer.isModified(), "qServer.isModified()");
        assertEquals(0, qServer.getPort(), "qServer.getPort()");
    }

    @Test
    public void testSetChannel() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setChannel("testQServerChannel");
        assertEquals("testQServerChannel", qServer.getChannel(), "qServer.getChannel()");
        assertTrue(qServer.isModified(), "qServer.isModified()");
    }

    @Test
    public void testSetChannelThrowsNullPointerException() throws Throwable {
        QServer qServer = new QServer();
        try {
            qServer.setChannel("testQServerChannel");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("testQServerChannel", qServer.getChannel(), "qServer.getChannel()");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildren(String)\" because the return value of \"org.jpos.q2.QBeanSupport.getPersist()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(qServer.isModified(), "qServer.isModified()");
        }
    }

    @Test
    public void testSetMaxSessions() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setMaxSessions(1000);
        assertEquals(1000, qServer.getMaxSessions(), "qServer.getMaxSessions()");
        assertTrue(qServer.isModified(), "qServer.isModified()");
    }

    @Test
    public void testSetMaxSessionsThrowsNullPointerException() throws Throwable {
        QServer qServer = new QServer();
        try {
            qServer.setMaxSessions(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildren(String)\" because the return value of \"org.jpos.q2.QBeanSupport.getPersist()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(100, qServer.getMaxSessions(), "qServer.getMaxSessions()");
            assertFalse(qServer.isModified(), "qServer.isModified()");
        }
    }

    @Test
    public void testSetMinSessions() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setMinSessions(100);
        assertEquals(100, qServer.getMinSessions(), "qServer.getMinSessions()");
        assertTrue(qServer.isModified(), "qServer.isModified()");
    }

    @Test
    public void testSetMinSessionsThrowsNullPointerException() throws Throwable {
        QServer qServer = new QServer();
        try {
            qServer.setMinSessions(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals(100, qServer.getMinSessions(), "qServer.getMinSessions()");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildren(String)\" because the return value of \"org.jpos.q2.QBeanSupport.getPersist()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(qServer.isModified(), "qServer.isModified()");
        }
    }

    @Test
    public void testSetPackager() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setPackager("testQServerPackager");
        assertEquals("testQServerPackager", qServer.getPackager(), "qServer.getPackager()");
        assertTrue(qServer.isModified(), "qServer.isModified()");
    }

    @Test
    public void testSetPackagerThrowsNullPointerException() throws Throwable {
        QServer qServer = new QServer();
        try {
            qServer.setPackager("testQServerPackager");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("testQServerPackager", qServer.getPackager(), "qServer.getPackager()");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildren(String)\" because the return value of \"org.jpos.q2.QBeanSupport.getPersist()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(qServer.isModified(), "qServer.isModified()");
        }
    }

    @Test
    public void testSetPort() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setPort(100);
        assertEquals(100, qServer.getPort(), "qServer.getPort()");
        assertTrue(qServer.isModified(), "qServer.isModified()");
    }

    @Test
    public void testSetPortThrowsNullPointerException() throws Throwable {
        QServer qServer = new QServer();
        try {
            qServer.setPort(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals(100, qServer.getPort(), "qServer.getPort()");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildren(String)\" because the return value of \"org.jpos.q2.QBeanSupport.getPersist()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(qServer.isModified(), "qServer.isModified()");
        }
    }

    @Test
    public void testSetSocketFactory() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setSocketFactory("testQServerSFactory");
        assertEquals("testQServerSFactory", qServer.getSocketFactory(), "qServer.getSocketFactory()");
        assertTrue(qServer.isModified(), "qServer.isModified()");
    }

    @Test
    public void testSetSocketFactoryThrowsNullPointerException() throws Throwable {
        QServer qServer = new QServer();
        try {
            qServer.setSocketFactory("testQServerSFactory");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("testQServerSFactory", qServer.getSocketFactory(), "qServer.getSocketFactory()");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildren(String)\" because the return value of \"org.jpos.q2.QBeanSupport.getPersist()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(qServer.isModified(), "qServer.isModified()");
        }
    }

    @Test
    public void testStartService() throws Throwable {
        QServer qServer = new QServer();
        qServer.startService();
        assertTrue(qServer.isModified(), "qServer.isModified()");
    }

    @Test
    public void testStartService1() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setPort(100);
        qServer.startService();
        assertFalse(qServer.isModified(), "qServer.isModified()");
    }

    @Test
    public void testStopService() throws Throwable {
        QServer qServer = new QServer();
        qServer.stopService();
    }
}
