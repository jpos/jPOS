/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jdom2.Element;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.MUX;
import org.jpos.space.Space;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MUXPoolTest {
    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        MUXPool mUXPool = new MUXPool();
        try {
            mUXPool.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildTextTrim(String)\" because \"e\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(mUXPool.isModified(), "mUXPool.isModified()");
            assertNull(mUXPool.muxName, "mUXPool.muxName");
            assertNull(mUXPool.mux, "mUXPool.mux");
            assertEquals(0, mUXPool.strategy, "mUXPool.strategy");
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException1() throws Throwable {
        MUXPool mUXPool = new MUXPool();
        mUXPool.setPersist(new Element("testMUXPoolName", "testMUXPoolUri"));
        try {
            mUXPool.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.getBoolean(String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(mUXPool.mux, "mUXPool.mux");
            assertFalse(mUXPool.isModified(), "mUXPool.isModified()");
            assertEquals(0, mUXPool.strategy, "mUXPool.strategy");
            assertNull(mUXPool.muxName, "mUXPool.muxName");
        }
    }

    @Test
    public void testIsConnectedThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new MUXPool().isConnected();
        });
    }

    @Test
    public void testRequestThrowsNullPointerException() throws Throwable {
        MUXPool mUXPool = new MUXPool();
        assertThrows(NullPointerException.class, () -> {
            mUXPool.request(new ISOMsg("testMUXPoolMti"), 100L);
        });
    }

    @Test
    public void testStopService() throws Throwable {
        MUXPool mUXPool = new MUXPool();
        mUXPool.stopService();
        assertNull(mUXPool.getName(), "mUXPool.getName()");
    }

    /**
     * Test isConnected(timeout) where at least one MUX in the pool connects
     * before the timeout expires.
     */
    @Test
    public void testIsConnectedWithTimeout() throws Exception {
        MUXPool pool = new MUXPool();
        MUX m1 = mock(MUX.class);
        MUX m2 = mock(MUX.class);
        pool.mux = new MUX[] { m1, m2 };

        when(m1.isConnected(anyLong())).thenAnswer(invocation -> {
            Thread.sleep(200);
            return true;
        });
        when(m1.isConnected()).thenReturn(true);
        when(m2.isConnected(anyLong())).thenReturn(false);

        assertTrue(pool.isConnected(1000), "Pool should be connected if at least one MUX is connected");
    }

    /**
     * Test isConnected(timeout) where all MUXes in the pool fail to connect
     * within the given timeout.
     */
    @Test
    public void testIsConnectedWithTimeoutFail() throws Exception {
        MUXPool pool = new MUXPool();
        MUX m1 = mock(MUX.class);
        MUX m2 = mock(MUX.class);
        pool.mux = new MUX[] { m1, m2 };

        when(m1.isConnected(anyLong())).thenReturn(false);
        when(m1.isConnected()).thenReturn(false);
        when(m2.isConnected(anyLong())).thenReturn(false);
        when(m2.isConnected()).thenReturn(false);

        assertFalse(pool.isConnected(500), "Pool should not be connected if all MUXes timeout or return false");
    }

    @Test
    public void testIsConnectedWithZeroTimeoutWithDisconnectedUnderlyingMux() throws Exception {
        MUXPool pool = new MUXPool();
        MUX m1 = mock(MUX.class);
        pool.mux = new MUX[] { m1 };
        when(m1.isConnected()).thenReturn(false);
        assertFalse(pool.isConnected(0), "Should return false for zero timeout, because underlying mux is not connected");
    }

    @Test
    public void testIsConnectedWithZeroTimeoutWithConnectedUnderlyingMux() throws Exception {
        MUXPool pool = new MUXPool();
        MUX m1 = mock(MUX.class);
        pool.mux = new MUX[] { m1 };
        when(m1.isConnected()).thenReturn(true);
        assertTrue(pool.isConnected(0), "Should return true for zero timeout, because underlying mux is connected");
    }

    /**
     * Test isConnected(timeout) with checkEnabled=true where the QMUX is 
     * connected, and the enabled indicator in space matches the ready indicator.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testIsConnectedWithTimeoutAndCheckEnabled() throws Exception {
        MUXPool pool = new MUXPool();
        pool.checkEnabled = true;
        Space sp = mock(Space.class);
        pool.sp = sp;

        QMUX qmux = mock(QMUX.class);
        when(qmux.getName()).thenReturn("qmux1");
        when(qmux.isConnected(anyLong())).thenReturn(true);
        when(qmux.isConnected()).thenReturn(true);
        when(qmux.getReadyIndicatorNames()).thenReturn(new String[]{"ready1"});

        pool.mux = new MUX[] { qmux };

        // Mock Space behavior: qmux1.enabled matches ready1 value (non-blocking)
        Object value = new Object();
        when(sp.rdp("qmux1.enabled")).thenReturn(value);
        when(sp.rdp("ready1")).thenReturn(value);

        assertTrue(pool.isConnected(1000), "Pool should be connected if QMUX is connected and enabled indicator matches ready indicator");
    }

    /**
     * Test isConnected(timeout) with checkEnabled=true where the QMUX is
     * connected but the enabled indicator in space does NOT match the ready indicator.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testIsConnectedWithTimeoutAndCheckEnabledFail() throws Exception {
        MUXPool pool = new MUXPool();
        pool.checkEnabled = true;
        Space sp = mock(Space.class);
        pool.sp = sp;

        QMUX qmux = mock(QMUX.class);
        when(qmux.getName()).thenReturn("qmux1");
        when(qmux.isConnected(anyLong())).thenReturn(true);
        when(qmux.isConnected()).thenReturn(true);
        when(qmux.getReadyIndicatorNames()).thenReturn(new String[]{"ready1"});

        pool.mux = new MUX[] { qmux };

        // Mock Space behavior: ready1 is available, but qmux1.enabled does NOT match ready1 value
        Object value1 = new Object();
        Object value2 = new Object();
        when(sp.rdp("ready1")).thenReturn(value1);
        when(sp.rdp("qmux1.enabled")).thenReturn(value2);

        assertFalse(pool.isConnected(1000), "Pool should NOT be connected if enabled indicator does NOT match ready indicator");
    }

    /**
     * Test isConnected(timeout) with checkEnabled=true where the MUX is initially
     * connected but not enabled. It waits for both conditions to be met simultaneously
     * using the parallel implementation.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testIsConnectedWithTimeoutAndParallelCondition() throws Exception {
        MUXPool pool = new MUXPool();
        pool.checkEnabled = true;
        Space sp = mock(Space.class);
        pool.sp = sp;

        QMUX qmux = mock(QMUX.class);
        when(qmux.getName()).thenReturn("qmux1");
        when(qmux.getReadyIndicatorNames()).thenReturn(new String[]{"ready1"});

        pool.mux = new MUX[] { qmux };

        Object enabled = new Object();

        // Simulate: initially connected but NOT enabled
        when(qmux.isConnected(anyLong())).thenReturn(true);
        when(qmux.isConnected()).thenReturn(true);

        // Blocking rd call: simulate it takes some time to become enabled
        when(sp.rd(eq("qmux1.enabled"), anyLong())).thenReturn(enabled);

        // isUsable(mux) checks rdp
        when(sp.rdp("qmux1.enabled")).thenReturn(null, enabled); // first call null, second call value
        when(sp.rdp("ready1")).thenReturn(enabled);

        assertTrue(pool.isConnected(1000), "Pool should eventually be connected when both conditions match");
    }

    /**
     * Test isConnected(timeout) with checkEnabled=true where the MUX is enabled
     * but never connects.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testIsConnectedWithTimeoutEnabledButNotConnected() throws Exception {
        MUXPool pool = new MUXPool();
        pool.checkEnabled = true;
        Space sp = mock(Space.class);
        pool.sp = sp;

        QMUX qmux = mock(QMUX.class);
        when(qmux.getName()).thenReturn("qmux1");
        when(qmux.getReadyIndicatorNames()).thenReturn(new String[]{"ready1"});
        pool.mux = new MUX[] { qmux };

        Object enabled = new Object();
        when(sp.rd("qmux1.enabled", 500L)).thenReturn(enabled);
        when(sp.rdp("qmux1.enabled")).thenReturn(enabled);
        
        // Never connects
        when(qmux.isConnected(anyLong())).thenReturn(false);
        when(qmux.isConnected()).thenReturn(false);

        assertFalse(pool.isConnected(500), "Should return false if enabled but never connected");
    }

    /**
     * Test isConnected(timeout) with checkEnabled=true where the MUX connects
     * but is never enabled.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testIsConnectedWithTimeoutConnectedButNotEnabled() throws Exception {
        MUXPool pool = new MUXPool();
        pool.checkEnabled = true;
        Space sp = mock(Space.class);
        pool.sp = sp;

        QMUX qmux = mock(QMUX.class);
        when(qmux.getName()).thenReturn("qmux1");
        when(qmux.getReadyIndicatorNames()).thenReturn(new String[]{"ready1"});
        pool.mux = new MUX[] { qmux };

        // Connects
        when(qmux.isConnected(anyLong())).thenReturn(true);
        when(qmux.isConnected()).thenReturn(true);
        when(sp.rdp("ready1")).thenReturn(new Object());

        // Never enabled
        when(sp.rd(eq("qmux1.enabled"), anyLong())).thenReturn(null);
        when(sp.rdp("qmux1.enabled")).thenReturn(null);

        assertFalse(pool.isConnected(500), "Should return false if connected but never enabled");
    }

    /**
     * Test isConnected(timeout) where it becomes enabled first, then connected.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testIsConnectedWithTimeoutEnabledThenConnected() throws Exception {
        MUXPool pool = new MUXPool();
        pool.checkEnabled = true;
        Space sp = mock(Space.class);
        pool.sp = sp;

        QMUX qmux = mock(QMUX.class);
        when(qmux.getName()).thenReturn("qmux1");
        when(qmux.getReadyIndicatorNames()).thenReturn(new String[]{"ready1"});
        pool.mux = new MUX[] { qmux };

        Object val = new Object();
        // Becomes enabled quickly
        when(sp.rd(eq("qmux1.enabled"), anyLong())).thenReturn(val);
        when(sp.rdp("qmux1.enabled")).thenReturn(val);

        // Becomes connected later
        when(qmux.isConnected(anyLong())).thenAnswer(i -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // ignore
            }
            return true;
        });
        when(qmux.isConnected()).thenReturn(false, false, true);
        when(sp.rdp("ready1")).thenReturn(val);

        assertTrue(pool.isConnected(1000), "Should return true if it becomes enabled then connected");
    }
}
