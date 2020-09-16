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

package org.jpos.iso;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.ServerSocket;

import org.jpos.core.Configuration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.channel.NACChannel;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.EuroPackager;
import org.jpos.util.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConnectorTest {
    @Mock
    Logger logger;

    @Test
    public void testConstructor() throws Throwable {
        Connector connector = new Connector();
        assertEquals(0, connector.timeout, "connector.timeout");
    }

    @Test
    public void testGetLogger() throws Throwable {
        Connector connector = new Connector();
        Logger logger = new Logger();
        connector.setLogger(logger, "testConnectorRealm");
        Logger result = connector.getLogger();
        assertSame(logger, result, "result");
    }

    @Test
    public void testGetRealm() throws Throwable {
        String result = new Connector().getRealm();
        assertNull(result, "result");
    }

    @Test
    public void testGetRealm1() throws Throwable {
        Connector connector = new Connector();

        connector.setLogger(logger, "testConnectorRealm");
        String result = connector.getRealm();
        assertEquals("testConnectorRealm", result, "result");
    }

    @Test
    public void testProcess() throws Throwable {
        Connector connector = new Connector();
        boolean result = connector.process(new XMLChannel(), new ISOMsg(100));
        assertTrue(result, "result");
    }

    @Test
    public void testProcessConstructor() throws Throwable {
        ISOSource source = new XMLChannel(new EuroPackager(), new ServerSocket());
        ISOMsg m = new ISOMsg(100);
        Connector.Process process = new Connector().new Process(source, m);
        assertSame(m, process.m, "process.m");
        assertSame(source, process.source, "process.source");
    }

    @Test
    public void testProcessRunThrowsNullPointerException() throws Throwable {
        ISOSource source = new NACChannel();
        Connector.Process process = new Connector().new Process(source, null);
        try {
            process.run();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.iso.ISOMsg.clone()\" because \"this.m\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(process.m, "process.m");
            assertSame(source, process.source, "process.source");
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
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.getInt(String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, connector.timeout, "connector.timeout");
            assertNull(connector.channelName, "connector.channelName");
            assertNull(connector.muxName, "connector.muxName");
        }
    }

    @Test
    public void testSetLogger() throws Throwable {
        Connector connector = new Connector();
        Logger logger = new Logger();
        connector.setLogger(logger, "testConnectorRealm");
        assertSame(logger, connector.getLogger(), "connector.getLogger()");
        assertEquals("testConnectorRealm", connector.getRealm(), "connector.getRealm()");
    }
}
