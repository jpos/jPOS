/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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
import static org.junit.Assert.fail;

import org.jdom2.Element;
import org.jpos.iso.ISOMsg;
import org.junit.Test;

public class MUXPoolTest {
    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        MUXPool mUXPool = new MUXPool();
        try {
            mUXPool.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("mUXPool.isModified()", mUXPool.isModified());
            assertNull("mUXPool.muxName", mUXPool.muxName);
            assertNull("mUXPool.mux", mUXPool.mux);
            assertEquals("mUXPool.strategy", 0, mUXPool.strategy);
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
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("mUXPool.mux", mUXPool.mux);
            assertFalse("mUXPool.isModified()", mUXPool.isModified());
            assertEquals("mUXPool.strategy", 0, mUXPool.strategy);
            assertNull("mUXPool.muxName", mUXPool.muxName);
        }
    }

    @Test
    public void testIsConnectedThrowsNullPointerException() throws Throwable {
        try {
            new MUXPool().isConnected();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testRequestThrowsNullPointerException() throws Throwable {
        MUXPool mUXPool = new MUXPool();
        try {
            mUXPool.request(new ISOMsg("testMUXPoolMti"), 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStopService() throws Throwable {
        MUXPool mUXPool = new MUXPool();
        mUXPool.stopService();
        assertNull("mUXPool.getName()", mUXPool.getName());
    }
}
