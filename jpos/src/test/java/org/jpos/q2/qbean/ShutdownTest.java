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

package org.jpos.q2.qbean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jpos.q2.Q2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ShutdownTest {
    @Mock
    Q2 server;

    @Test
    public void testConstructor() throws Throwable {
        Shutdown shutdown = new Shutdown();
        assertEquals("shutdown.getLog().getRealm()", "org.jpos.q2.qbean.Shutdown", shutdown.getLog().getRealm());
        assertEquals("shutdown.getState()", -1, shutdown.getState());
        assertTrue("shutdown.isModified()", shutdown.isModified());
    }

    @Test
    public void testStartService() throws Throwable {
        Shutdown shutdown = new Shutdown();
        String[] args = new String[2];
        args[0] = "";
        args[1] = "testString";
        shutdown.setServer(server);
        shutdown.startService();
        assertSame("shutdown.getServer()", server, shutdown.getServer());
    }

    @Test
    public void testStartServiceThrowsNullPointerException() throws Throwable {
        try {
            new Shutdown().startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
