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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.jdom2.Element;
import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.Connector;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.channel.PADChannel;
import org.jpos.iso.packager.EuroSubFieldPackager;
import org.jpos.util.NameRegistrar;
import org.junit.jupiter.api.Test;

public class QMUXTest {

    @Test
    public void testConstructor() throws Throwable {
        QMUX qMUX = new QMUX();
        assertEquals("org.jpos.q2.iso.QMUX", qMUX.getLog().getRealm(), "qMUX.getLog().getRealm()");
        assertEquals(-1, qMUX.getState(), "qMUX.getState()");
        assertTrue(qMUX.isModified(), "qMUX.isModified()");
        assertEquals(0, qMUX.listeners.size(), "qMUX.listeners.size()");
    }

    @Test
    public void testGetInQueue() throws Throwable {
        String result = new QMUX().getInQueue();
        assertNull(result, "result");
    }

    @Test
    public void testGetKeyThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        ISOMsg m = new ISOMsg();
        try {
            qMUX.getKey(m);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("Misconfigured QMUX. Please verify out queue is not null.", ex.getMessage(), "ex.getMessage()");
            assertEquals(0, m.getDirection(), "m.getDirection()");
        }
    }

    @Test
    public void testGetMUXThrowsNotFoundException() throws Throwable {
        try {
            QMUX.getMUX("testQMUXName");
            fail("Expected NotFoundException to be thrown");
        } catch (NameRegistrar.NotFoundException ex) {
            assertEquals("mux.testQMUXName", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testGetOutQueue() throws Throwable {
        String result = new QMUX().getOutQueue();
        assertNull(result, "result");
    }

    @Test
    public void testGetUnhandledQueue() throws Throwable {
        String result = new QMUX().getUnhandledQueue();
        assertNull(result, "result");
    }

    @Test
    public void testInitServiceWithoutListeners() throws Throwable {
        QMUX mux = new QMUX();
        Configuration cfg = new SimpleConfiguration();
        mux.setConfiguration(cfg);
        Element persist = new Element("testQMUXName");
        persist.addContent(new Element("in").addContent("queue-in"));
        persist.addContent(new Element("out").addContent("queue-out"));
        persist.addContent(new Element("unhandled").addContent("queue-unhandled"));
        persist.addContent(new Element("ready").addContent("test-ready"));
        persist.addContent(new Element("unhandled").addContent("queue-unhandled"));
        mux.setPersist(persist);
        mux.initService();

        assertEquals("queue-unhandled", mux.unhandled);
        assertEquals("queue-out", mux.out);
        assertEquals("queue-in", mux.in);
        assertNull(mux.ignorerc);
        assertNotNull(mux.sp);
        assertFalse(mux.isModified());
        assertArrayEquals(new String[]{"test-ready"}, mux.ready);
        assertArrayEquals(new String[]{"41", "11"}, mux.key);
        assertEquals(0, mux.listeners.size());
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            QMUX qMUX = new QMUX();
            qMUX.setPersist(new Element("testQMUXName", "testQMUXUri"));
            qMUX.initService();
        });
    }

    @Test
    public void testInitServiceThrowsNullPointerException2() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChild(String)\" because \"e\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(qMUX.unhandled, "qMUX.unhandled");
            assertNull(qMUX.out, "qMUX.out");
            assertNull(qMUX.in, "qMUX.in");
            assertNull(qMUX.ignorerc, "qMUX.ignorerc");
            assertNull(qMUX.sp, "qMUX.sp");
            assertFalse(qMUX.isModified(), "qMUX.isModified()");
            assertNull(qMUX.ready, "qMUX.ready");
            assertNull(qMUX.key, "qMUX.key");
            assertEquals(0, qMUX.listeners.size(), "qMUX.listeners.size()");
        }
    }

    @Test
    public void testIsConnected() throws Throwable {
        assertFalse(new QMUX().isConnected(), "result"); // MUX was not started
    }

    @Test
    public void testNotifyThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.notify("", Integer.valueOf(2));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.space.LocalSpace.inp(Object)\" because \"this.sp\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(qMUX.sp, "qMUX.sp");
            assertEquals(0, qMUX.listeners.size(), "qMUX.listeners.size()");
        }
    }

    @Test
    public void testProcessUnhandled() throws Throwable {
        ISOMsg m = new ISOMsg("testQMUXMti");
        m.setSource(new PADChannel(new EuroSubFieldPackager()));
        QMUX qMUX = new QMUX();
        qMUX.processUnhandled(m);
        assertEquals(0, qMUX.listeners.size(), "qMUX.listeners.size()");
    }

    @Test
    public void testProcessUnhandled1() throws Throwable {
        QMUX qMUX = new QMUX();
        ISOMsg m = new ISOMsg("testQMUXMti");
        qMUX.processUnhandled(m);
        assertNull(qMUX.sp, "qMUX.sp");
        assertEquals(0, qMUX.listeners.size(), "qMUX.listeners.size()");
        assertEquals(0, m.getDirection(), "m.getDirection()");
    }

    @Test
    public void testProcessUnhandledThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.processUnhandled(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.iso.ISOMsg.getSource()\" because \"m\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(qMUX.sp, "qMUX.sp");
            assertEquals(0, qMUX.listeners.size(), "qMUX.listeners.size()");
        }
    }

    @Test
    public void testRemoveISORequestListener() throws Throwable {
        QMUX qMUX = new QMUX();
        boolean result = qMUX.removeISORequestListener(new Connector());
        assertFalse(result, "result");
        assertEquals(0, qMUX.listeners.size(), "qMUX.listeners.size()");
    }

    @Test
    public void testRemoveISORequestListener1() throws Throwable {
        QMUX qMUX = new QMUX();
        ISORequestListener l = new Connector();
        qMUX.addISORequestListener(l);
        boolean result = qMUX.removeISORequestListener(l);
        assertEquals(0, qMUX.listeners.size(), "qMUX.listeners.size()");
        assertFalse(qMUX.listeners.contains(l), "qMUX.listeners.contains(l)");
        assertTrue(result, "result");
    }

    @Test
    public void testRequestThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        ISOMsg m = new ISOMsg("testQMUXMti");
        try {
            qMUX.request(m, 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("Misconfigured QMUX. Please verify out queue is not null.", ex.getMessage(), "ex.getMessage()");
            assertNull(qMUX.sp, "qMUX.sp");
            assertEquals(0, m.getDirection(), "m.getDirection()");
        }
    }

    @Test
    public void testSetInQueueThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.setInQueue("testQMUXIn");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("testQMUXIn", qMUX.in, "qMUX.in");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChild(String)\" because the return value of \"org.jpos.q2.iso.QMUX.getPersist()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(qMUX.isModified(), "qMUX.isModified()");
        }
    }

    @Test
    public void testSetOutQueueThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.setOutQueue("testQMUXOut");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("testQMUXOut", qMUX.out, "qMUX.out");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChild(String)\" because the return value of \"org.jpos.q2.iso.QMUX.getPersist()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(qMUX.isModified(), "qMUX.isModified()");
        }
    }

    @Test
    public void testSetUnhandledQueueThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.setUnhandledQueue("testQMUXUnhandled");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("testQMUXUnhandled", qMUX.unhandled, "qMUX.unhandled");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChild(String)\" because the return value of \"org.jpos.q2.iso.QMUX.getPersist()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(qMUX.isModified(), "qMUX.isModified()");
        }
    }

    @Test
    public void testStartServiceThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        qMUX.setState(0);
        try {
            qMUX.startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot enter synchronized block because \"this.sp\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(qMUX.sp, "qMUX.sp");
        }
    }

    @Test
    public void testStopServiceThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.stopService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.space.LocalSpace.removeListener(Object, org.jpos.space.SpaceListener)\" because \"this.sp\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(qMUX.sp, "qMUX.sp");
        }
    }
}
