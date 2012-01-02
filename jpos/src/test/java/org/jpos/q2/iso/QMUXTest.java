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
import org.jpos.iso.Connector;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.channel.PADChannel;
import org.jpos.iso.packager.EuroSubFieldPackager;
import org.jpos.util.NameRegistrar;
import org.junit.Test;

public class QMUXTest {

    @Test
    public void testConstructor() throws Throwable {
        QMUX qMUX = new QMUX();
        assertEquals("qMUX.getLog().getRealm()", "org.jpos.q2.iso.QMUX", qMUX.getLog().getRealm());
        assertEquals("qMUX.getState()", -1, qMUX.getState());
        assertTrue("qMUX.isModified()", qMUX.isModified());
        assertEquals("qMUX.listeners.size()", 0, qMUX.listeners.size());
    }

    @Test
    public void testGetInQueue() throws Throwable {
        String result = new QMUX().getInQueue();
        assertNull("result", result);
    }

    @Test
    public void testGetKeyThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        ISOMsg m = new ISOMsg();
        try {
            qMUX.getKey(m);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("m.getDirection()", 0, m.getDirection());
        }
    }

    @Test
    public void testGetMUXThrowsNotFoundException() throws Throwable {
        try {
            QMUX.getMUX("testQMUXName");
            fail("Expected NotFoundException to be thrown");
        } catch (NameRegistrar.NotFoundException ex) {
            assertEquals("ex.getMessage()", "mux.testQMUXName", ex.getMessage());
        }
    }

    @Test
    public void testGetOutQueue() throws Throwable {
        String result = new QMUX().getOutQueue();
        assertNull("result", result);
    }

    @Test
    public void testGetUnhandledQueue() throws Throwable {
        String result = new QMUX().getUnhandledQueue();
        assertNull("result", result);
    }

    @Test(expected = NullPointerException.class)
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        qMUX.setPersist(new Element("testQMUXName", "testQMUXUri"));
        qMUX.initService();
    }

    @Test
    public void testInitServiceThrowsNullPointerException2() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("qMUX.unhandled", qMUX.unhandled);
            assertNull("qMUX.out", qMUX.out);
            assertNull("qMUX.in", qMUX.in);
            assertNull("qMUX.ignorerc", qMUX.ignorerc);
            assertNull("qMUX.sp", qMUX.sp);
            assertFalse("qMUX.isModified()", qMUX.isModified());
            assertNull("qMUX.ready", qMUX.ready);
            assertNull("qMUX.key", qMUX.key);
            assertEquals("qMUX.listeners.size()", 0, qMUX.listeners.size());
        }
    }

    @Test
    public void testIsConnected() throws Throwable {
        boolean result = new QMUX().isConnected();
        assertTrue("result", result);
    }

    @Test
    public void testNotifyThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.notify("", Integer.valueOf(2));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("qMUX.sp", qMUX.sp);
            assertEquals("qMUX.listeners.size()", 0, qMUX.listeners.size());
        }
    }

    @Test
    public void testProcessUnhandled() throws Throwable {
        ISOMsg m = new ISOMsg("testQMUXMti");
        m.setSource(new PADChannel(new EuroSubFieldPackager()));
        QMUX qMUX = new QMUX();
        qMUX.processUnhandled(m);
        assertEquals("qMUX.listeners.size()", 0, qMUX.listeners.size());
    }

    @Test
    public void testProcessUnhandled1() throws Throwable {
        QMUX qMUX = new QMUX();
        ISOMsg m = new ISOMsg("testQMUXMti");
        qMUX.processUnhandled(m);
        assertNull("qMUX.sp", qMUX.sp);
        assertEquals("qMUX.listeners.size()", 0, qMUX.listeners.size());
        assertEquals("m.getDirection()", 0, m.getDirection());
    }

    @Test
    public void testProcessUnhandledThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.processUnhandled(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("qMUX.sp", qMUX.sp);
            assertEquals("qMUX.listeners.size()", 0, qMUX.listeners.size());
        }
    }

    @Test
    public void testRemoveISORequestListener() throws Throwable {
        QMUX qMUX = new QMUX();
        boolean result = qMUX.removeISORequestListener(new Connector());
        assertFalse("result", result);
        assertEquals("qMUX.listeners.size()", 0, qMUX.listeners.size());
    }

    @Test
    public void testRemoveISORequestListener1() throws Throwable {
        QMUX qMUX = new QMUX();
        ISORequestListener l = new Connector();
        qMUX.addISORequestListener(l);
        boolean result = qMUX.removeISORequestListener(l);
        assertEquals("qMUX.listeners.size()", 0, qMUX.listeners.size());
        assertFalse("qMUX.listeners.contains(l)", qMUX.listeners.contains(l));
        assertTrue("result", result);
    }

    @Test
    public void testRequestThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        ISOMsg m = new ISOMsg("testQMUXMti");
        try {
            qMUX.request(m, 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("qMUX.sp", qMUX.sp);
            assertEquals("m.getDirection()", 0, m.getDirection());
        }
    }

    @Test
    public void testSetInQueueThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.setInQueue("testQMUXIn");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("qMUX.in", "testQMUXIn", qMUX.in);
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("qMUX.isModified()", qMUX.isModified());
        }
    }

    @Test
    public void testSetOutQueueThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.setOutQueue("testQMUXOut");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("qMUX.out", "testQMUXOut", qMUX.out);
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("qMUX.isModified()", qMUX.isModified());
        }
    }

    @Test
    public void testSetUnhandledQueueThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.setUnhandledQueue("testQMUXUnhandled");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("qMUX.unhandled", "testQMUXUnhandled", qMUX.unhandled);
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("qMUX.isModified()", qMUX.isModified());
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
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("qMUX.sp", qMUX.sp);
        }
    }

    @Test
    public void testStopServiceThrowsNullPointerException() throws Throwable {
        QMUX qMUX = new QMUX();
        try {
            qMUX.stopService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("qMUX.sp", qMUX.sp);
        }
    }
}
