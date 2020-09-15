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

package org.jpos.bsh;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.CSChannel;
import org.jpos.iso.channel.LogChannel;
import org.jpos.iso.channel.PostChannel;
import org.junit.jupiter.api.Test;

public class BSHRequestListenerTest {

    @Test
    public void testConstructor() throws Throwable {
        new BSHRequestListener();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testProcess() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        bSHRequestListener.setConfiguration(new SimpleConfiguration());
        boolean result = bSHRequestListener.process(new PostChannel(), new ISOMsg("testBSHRequestListenerMti"));
        assertFalse(result, "result should be false"); //there is no source to process
        assertEquals(1, bSHRequestListener.whitelist.size(), "bSHRequestListener.whitelist.size()");
    }

    @Test
    public void testProcess1() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        boolean result = bSHRequestListener.process(new CSChannel(), new ISOMsg());
        assertFalse(result, "result");
        assertNull(bSHRequestListener.whitelist, "bSHRequestListener.whitelist");
    }

    @Test
    public void testProcess2() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        ISOMsg m = new ISOMsg();
        m.setMTI("testBSHRequestListenerMti");
        boolean result = bSHRequestListener.process(new LogChannel(), m);
        assertFalse(result, "result");
        assertNull(bSHRequestListener.whitelist, "bSHRequestListener.whitelist");
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        Configuration cfg = new SimpleConfiguration();
        bSHRequestListener.setConfiguration(cfg);
        assertEquals(1, bSHRequestListener.whitelist.size(), "bSHRequestListener.whitelist.size()");
        assertEquals(0, bSHRequestListener.bshSource.length, "bSHRequestListener.bshSource.length");
        assertSame(cfg, bSHRequestListener.cfg, "bSHRequestListener.cfg");
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        Configuration cfg = new SubConfiguration();
        try {
            bSHRequestListener.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertSame(cfg, bSHRequestListener.cfg, "bSHRequestListener.cfg");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.getAll(String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(bSHRequestListener.whitelist, "bSHRequestListener.whitelist");
            assertNull(bSHRequestListener.bshSource, "bSHRequestListener.bshSource");
        }
    }
}
