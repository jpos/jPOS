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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jpos.bsh.BSHRequestListener;
import org.jpos.transaction.participant.Debug;
import org.junit.jupiter.api.Test;

public class LogTest {

    @Test
    public void testConstructor() throws Throwable {
        Logger logger = Logger.getLogger("testLogName");
        Log log = new Log(logger, "testLogRealm");
        assertEquals("testLogRealm", log.realm, "m_log.realm");
        assertSame(logger, log.logger, "m_log.logger");
    }

    @Test
    public void testConstructor1() throws Throwable {
        Log log = new Log();
        assertNull(log.getRealm(), "m_log.getRealm()");
    }

    @Test
    public void testCreateDebug() throws Throwable {
        LogEvent result = new BSHRequestListener().createDebug("testString");
        assertNull(result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateDebug1() throws Throwable {
        LogEvent result = new Debug().createDebug();
        assertNull(result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateError() throws Throwable {
        LogEvent result = new Debug().createError();
        assertNull(result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateError1() throws Throwable {
        LogEvent result = new BSHRequestListener().createError("");
        assertNull(result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateFatal() throws Throwable {
        LogEvent result = new Debug().createFatal("t7r");
        assertNull(result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateFatal1() throws Throwable {
        LogEvent result = new Debug().createFatal();
        assertNull(result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateInfo() throws Throwable {
        LogEvent result = Log.getLog("testLogLogName", "testLogRealm").createInfo("testString");
        assertEquals("testLogRealm", result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateInfo1() throws Throwable {
        LogEvent result = new Log().createInfo();
        assertNull(result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateLogEvent() throws Throwable {
        LogEvent result = new BSHRequestListener().createLogEvent("testLogLevel", "");
        assertNull(result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateLogEvent1() throws Throwable {
        LogEvent result = new Debug().createLogEvent("testLogLevel");
        assertNull(result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateTrace() throws Throwable {
        LogEvent result = Log.getLog("testLogLogName", "testLogRealm").createTrace();
        assertEquals("testLogRealm", result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateTrace1() throws Throwable {
        LogEvent result = new BSHRequestListener().createTrace("");
        assertNull(result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateWarn() throws Throwable {
        LogEvent result = new Log().createWarn("testString");
        assertNull(result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testCreateWarn1() throws Throwable {
        LogEvent result = new Debug().createWarn();
        assertNull(result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testDebug() throws Throwable {
        new BSHRequestListener().debug("testString", Integer.valueOf(0));
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDebug1() throws Throwable {
        new BSHRequestListener().debug("");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testError() throws Throwable {
        new Debug().error(new Object());
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testError1() throws Throwable {
        new BSHRequestListener().error("", Integer.valueOf(18));
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testFatal1() throws Throwable {
        Log log = new Log();
        log.fatal("", "q  ");
        assertNull(log.getRealm(), "m_log.getRealm()");
    }

    @Test
    public void testGetLog() throws Throwable {
        Log result = Log.getLog("testLogLogName", "testLogRealm");
        assertEquals("testLogRealm", result.getRealm(), "result.getRealm()");
    }

    @Test
    public void testGetLogger() throws Throwable {
        Logger logger = new Logger();
        Log bSHRequestListener = new BSHRequestListener();
        bSHRequestListener.setLogger(logger, "testLogRealm");
        Logger result = bSHRequestListener.getLogger();
        assertSame(logger, result, "result");
    }

    @Test
    public void testGetRealm() throws Throwable {
        String result = Log.getLog("testLogLogName", "testLogRealm").getRealm();
        assertEquals("testLogRealm", result, "result");
    }

    @Test
    public void testInfo1() throws Throwable {
        new BSHRequestListener().info("", "");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testSetLogger() throws Throwable {
        Logger logger = new Logger();
        Log bSHRequestListener = new BSHRequestListener();
        bSHRequestListener.setLogger(logger, "testLogRealm");
        assertEquals("testLogRealm", ((BSHRequestListener) bSHRequestListener).realm,
                "(BSHRequestListener) bSHRequestListener.realm");
        assertSame(logger, ((BSHRequestListener) bSHRequestListener).logger, "(BSHRequestListener) bSHRequestListener.logger");
    }

    @Test
    public void testSetLogger1() throws Throwable {
        Log bSHRequestListener = new BSHRequestListener();
        Logger logger = new Logger();
        bSHRequestListener.setLogger(logger);
        assertSame(logger, ((BSHRequestListener) bSHRequestListener).logger, "(BSHRequestListener) bSHRequestListener.logger");
    }

    @Test
    public void testSetRealm() throws Throwable {
        Log bSHRequestListener = new BSHRequestListener();
        bSHRequestListener.setRealm("testLogRealm");
        assertEquals("testLogRealm", ((BSHRequestListener) bSHRequestListener).realm,
                "(BSHRequestListener) bSHRequestListener.realm");
    }

    @Test
    public void testTrace() throws Throwable {
        new BSHRequestListener().trace("x", "x");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testTrace1() throws Throwable {
        new BSHRequestListener().trace("testString");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testWarn() throws Throwable {
        new BSHRequestListener().warn("", "");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testWarn1() throws Throwable {
        new BSHRequestListener().warn(new Object());
        assertTrue(true, "Test completed without Exception");
    }

}
