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

package org.jpos.util;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.junit.jupiter.api.Test;

public class ProtectedLogListenerTest {

    @Test
    public void testConstructor() throws Throwable {
        ProtectedLogListener protectedLogListener = new ProtectedLogListener();
        assertNull(protectedLogListener.wipeFields, "protectedLogListener.wipeFields");
        assertNull(protectedLogListener.protectFields, "protectedLogListener.protectFields");
        assertNull(protectedLogListener.cfg, "protectedLogListener.cfg");
    }

    @Test
    public void testLogThrowsNullPointerException() throws Throwable {
        try {
            new ProtectedLogListener().log(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.util.LogEvent.getPayLoad()\" because \"ev\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        ProtectedLogListener protectedLogListener = new ProtectedLogListener();
        Configuration cfg = new SimpleConfiguration();
        protectedLogListener.setConfiguration(cfg);
        assertEquals(0, protectedLogListener.protectFields.length, "protectedLogListener.protectFields.length");
        assertEquals(0, protectedLogListener.wipeFields.length, "protectedLogListener.wipeFields.length");
        assertSame(cfg, protectedLogListener.cfg, "protectedLogListener.cfg");
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        ProtectedLogListener protectedLogListener = new ProtectedLogListener();
        Configuration cfg = new SubConfiguration();
        try {
            protectedLogListener.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertSame(cfg, protectedLogListener.cfg, "protectedLogListener.cfg");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.get(String, String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(protectedLogListener.wipeFields, "protectedLogListener.wipeFields");
            assertNull(protectedLogListener.protectFields, "protectedLogListener.protectFields");
        }
    }
}
