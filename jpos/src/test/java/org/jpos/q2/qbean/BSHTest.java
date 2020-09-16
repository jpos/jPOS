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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class BSHTest {

    @Test
    public void testConstructor() throws Throwable {
        BSH bSH = new BSH();
        assertEquals("org.jpos.q2.qbean.BSH", bSH.getLog().getRealm(), "bSH.getLog().getRealm()");
        assertEquals(-1, bSH.getState(), "bSH.getState()");
        assertTrue(bSH.isModified(), "bSH.isModified()");
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        BSH bSH = new BSH();
        try {
            bSH.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.Q2.getLoader()\" because the return value of \"org.jpos.q2.qbean.BSH.getServer()\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testRun() throws Throwable {
        BSH bSH = new BSH();
        bSH.run();
        assertNull(bSH.bsh, "bSH.bsh");
        assertFalse(bSH.isModified(), "bSH.isModified()");
    }

    @Test
    public void testStartService() throws Throwable {
        BSH bSH = new BSH();
        bSH.startService();
        assertNull(bSH.getName(), "bSH.getName()");
    }
}
