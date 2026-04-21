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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jdom2.Element;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.MUX;
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
        when(m2.isConnected(anyLong())).thenReturn(false);

        assertTrue(pool.isConnected(1000), "Pool should be connected if at least one MUX is connected");
    }

    @Test
    public void testIsConnectedWithTimeoutFail() throws Exception {
        MUXPool pool = new MUXPool();
        MUX m1 = mock(MUX.class);
        MUX m2 = mock(MUX.class);
        pool.mux = new MUX[] { m1, m2 };

        when(m1.isConnected(anyLong())).thenReturn(false);
        when(m2.isConnected(anyLong())).thenReturn(false);

        assertFalse(pool.isConnected(500), "Pool should not be connected if all MUXes timeout or return false");
    }
}
