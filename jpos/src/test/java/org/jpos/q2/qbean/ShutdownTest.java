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

package org.jpos.q2.qbean;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.jpos.q2.Q2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ShutdownTest {
    @Mock
    Q2 server;

    @Test
    public void testConstructor() throws Throwable {
        Shutdown shutdown = new Shutdown();
        assertEquals("org.jpos.q2.qbean.Shutdown", shutdown.getLog().getRealm(), "shutdown.getLog().getRealm()");
        assertEquals(-1, shutdown.getState(), "shutdown.getState()");
        assertTrue(shutdown.isModified(), "shutdown.isModified()");
    }

    @Test
    public void testStartService() throws Throwable {
        Shutdown shutdown = new Shutdown();
        String[] args = new String[2];
        args[0] = "";
        args[1] = "testString";
        shutdown.setServer(server);
        shutdown.startService();
        assertSame(server, shutdown.getServer(), "shutdown.getServer()");
    }

    @Test
    public void testStartServiceThrowsNullPointerException() throws Throwable {
        try {
            new Shutdown().startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.Q2.shutdown()\" because the return value of \"org.jpos.q2.qbean.Shutdown.getServer()\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
