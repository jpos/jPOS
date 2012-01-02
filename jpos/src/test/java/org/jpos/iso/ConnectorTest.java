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

package org.jpos.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.ServerSocket;

import org.jpos.core.Configuration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.channel.NACChannel;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.EuroPackager;
import org.jpos.util.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectorTest {
    @Mock
    Logger logger;

    @Test
    public void testConstructor() throws Throwable {
        Connector connector = new Connector();
        assertEquals("connector.timeout", 0, connector.timeout);
    }

    @Test
    public void testGetLogger() throws Throwable {
        Connector connector = new Connector();
        Logger logger = new Logger();
        connector.setLogger(logger, "testConnectorRealm");
        Logger result = connector.getLogger();
        assertSame("result", logger, result);
    }

    @Test
    public void testGetRealm() throws Throwable {
        String result = new Connector().getRealm();
        assertNull("result", result);
    }

    @Test
    public void testGetRealm1() throws Throwable {
        Connector connector = new Connector();

        connector.setLogger(logger, "testConnectorRealm");
        String result = connector.getRealm();
        assertEquals("result", "testConnectorRealm", result);
    }

    @Test
    public void testProcess() throws Throwable {
        Connector connector = new Connector();
        boolean result = connector.process(new XMLChannel(), new ISOMsg(100));
        assertTrue("result", result);
    }

    @Test
    public void testProcessConstructor() throws Throwable {
        ISOSource source = new XMLChannel(new EuroPackager(), new ServerSocket());
        ISOMsg m = new ISOMsg(100);
        Connector.Process process = new Connector().new Process(source, m);
        assertSame("process.m", m, process.m);
        assertSame("process.source", source, process.source);
    }

    @Test
    public void testProcessRunThrowsNullPointerException() throws Throwable {
        ISOSource source = new NACChannel();
        Connector.Process process = new Connector().new Process(source, null);
        try {
            process.run();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("process.m", process.m);
            assertSame("process.source", source, process.source);
        }
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        Connector connector = new Connector();
        Configuration cfg = new SubConfiguration();
        try {
            connector.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("connector.timeout", 0, connector.timeout);
            assertNull("connector.channelName", connector.channelName);
            assertNull("connector.muxName", connector.muxName);
        }
    }

    @Test
    public void testSetLogger() throws Throwable {
        Connector connector = new Connector();
        Logger logger = new Logger();
        connector.setLogger(logger, "testConnectorRealm");
        assertSame("connector.getLogger()", logger, connector.getLogger());
        assertEquals("connector.getRealm()", "testConnectorRealm", connector.getRealm());
    }
}
