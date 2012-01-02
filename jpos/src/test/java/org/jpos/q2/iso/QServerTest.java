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

package org.jpos.q2.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jdom.Element;
import org.junit.Test;

public class QServerTest {

    @Test
    public void testConstructor() throws Throwable {
        QServer qServer = new QServer();
        assertEquals("qServer.getLog().getRealm()", "org.jpos.q2.iso.QServer", qServer.getLog().getRealm());
        assertEquals("qServer.getState()", -1, qServer.getState());
        assertEquals("qServer.getMaxSessions()", 100, qServer.getMaxSessions());
        assertEquals("qServer.getMinSessions()", 1, qServer.getMinSessions());
        assertTrue("qServer.isModified()", qServer.isModified());
        assertEquals("qServer.getPort()", 0, qServer.getPort());
    }

    @Test
    public void testSetChannel() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setChannel("testQServerChannel");
        assertEquals("qServer.getChannel()", "testQServerChannel", qServer.getChannel());
        assertTrue("qServer.isModified()", qServer.isModified());
    }

    @Test
    public void testSetChannelThrowsNullPointerException() throws Throwable {
        QServer qServer = new QServer();
        try {
            qServer.setChannel("testQServerChannel");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("qServer.getChannel()", "testQServerChannel", qServer.getChannel());
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("qServer.isModified()", qServer.isModified());
        }
    }

    @Test
    public void testSetMaxSessions() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setMaxSessions(1000);
        assertEquals("qServer.getMaxSessions()", 1000, qServer.getMaxSessions());
        assertTrue("qServer.isModified()", qServer.isModified());
    }

    @Test
    public void testSetMaxSessionsThrowsNullPointerException() throws Throwable {
        QServer qServer = new QServer();
        try {
            qServer.setMaxSessions(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("qServer.getMaxSessions()", 100, qServer.getMaxSessions());
            assertFalse("qServer.isModified()", qServer.isModified());
        }
    }

    @Test
    public void testSetMinSessions() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setMinSessions(100);
        assertEquals("qServer.getMinSessions()", 100, qServer.getMinSessions());
        assertTrue("qServer.isModified()", qServer.isModified());
    }

    @Test
    public void testSetMinSessionsThrowsNullPointerException() throws Throwable {
        QServer qServer = new QServer();
        try {
            qServer.setMinSessions(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("qServer.getMinSessions()", 100, qServer.getMinSessions());
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("qServer.isModified()", qServer.isModified());
        }
    }

    @Test
    public void testSetPackager() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setPackager("testQServerPackager");
        assertEquals("qServer.getPackager()", "testQServerPackager", qServer.getPackager());
        assertTrue("qServer.isModified()", qServer.isModified());
    }

    @Test
    public void testSetPackagerThrowsNullPointerException() throws Throwable {
        QServer qServer = new QServer();
        try {
            qServer.setPackager("testQServerPackager");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("qServer.getPackager()", "testQServerPackager", qServer.getPackager());
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("qServer.isModified()", qServer.isModified());
        }
    }

    @Test
    public void testSetPort() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setPort(100);
        assertEquals("qServer.getPort()", 100, qServer.getPort());
        assertTrue("qServer.isModified()", qServer.isModified());
    }

    @Test
    public void testSetPortThrowsNullPointerException() throws Throwable {
        QServer qServer = new QServer();
        try {
            qServer.setPort(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("qServer.getPort()", 100, qServer.getPort());
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("qServer.isModified()", qServer.isModified());
        }
    }

    @Test
    public void testSetSocketFactory() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setSocketFactory("testQServerSFactory");
        assertEquals("qServer.getSocketFactory()", "testQServerSFactory", qServer.getSocketFactory());
        assertTrue("qServer.isModified()", qServer.isModified());
    }

    @Test
    public void testSetSocketFactoryThrowsNullPointerException() throws Throwable {
        QServer qServer = new QServer();
        try {
            qServer.setSocketFactory("testQServerSFactory");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("qServer.getSocketFactory()", "testQServerSFactory", qServer.getSocketFactory());
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("qServer.isModified()", qServer.isModified());
        }
    }

    @Test
    public void testStartService() throws Throwable {
        QServer qServer = new QServer();
        qServer.startService();
        assertTrue("qServer.isModified()", qServer.isModified());
    }

    @Test
    public void testStartService1() throws Throwable {
        QServer qServer = new QServer();
        qServer.setPersist(new Element("testQServerName", "testQServerUri"));
        qServer.setPort(100);
        qServer.startService();
        assertFalse("qServer.isModified()", qServer.isModified());
    }

    @Test
    public void testStopService() throws Throwable {
        QServer qServer = new QServer();
        qServer.stopService();
    }
}
