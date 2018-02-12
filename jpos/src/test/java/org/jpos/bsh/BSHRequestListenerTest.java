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

package org.jpos.bsh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.CSChannel;
import org.jpos.iso.channel.LogChannel;
import org.jpos.iso.channel.PostChannel;
import org.junit.Test;

public class BSHRequestListenerTest {

    @Test
    public void testConstructor() throws Throwable {
        new BSHRequestListener();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testProcess() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        bSHRequestListener.setConfiguration(new SimpleConfiguration());
        boolean result = bSHRequestListener.process(new PostChannel(), new ISOMsg("testBSHRequestListenerMti"));
        assertFalse("result should be false", result); //there is no source to process
        assertEquals("bSHRequestListener.whitelist.size()", 1, bSHRequestListener.whitelist.size());
    }

    @Test
    public void testProcess1() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        boolean result = bSHRequestListener.process(new CSChannel(), new ISOMsg());
        assertFalse("result", result);
        assertNull("bSHRequestListener.whitelist", bSHRequestListener.whitelist);
    }

    @Test
    public void testProcess2() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        ISOMsg m = new ISOMsg();
        m.setMTI("testBSHRequestListenerMti");
        boolean result = bSHRequestListener.process(new LogChannel(), m);
        assertFalse("result", result);
        assertNull("bSHRequestListener.whitelist", bSHRequestListener.whitelist);
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        Configuration cfg = new SimpleConfiguration();
        bSHRequestListener.setConfiguration(cfg);
        assertEquals("bSHRequestListener.whitelist.size()", 1, bSHRequestListener.whitelist.size());
        assertEquals("bSHRequestListener.bshSource.length", 0, bSHRequestListener.bshSource.length);
        assertSame("bSHRequestListener.cfg", cfg, bSHRequestListener.cfg);
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        Configuration cfg = new SubConfiguration();
        try {
            bSHRequestListener.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertSame("bSHRequestListener.cfg", cfg, bSHRequestListener.cfg);
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("bSHRequestListener.whitelist", bSHRequestListener.whitelist);
            assertNull("bSHRequestListener.bshSource", bSHRequestListener.bshSource);
        }
    }
}
