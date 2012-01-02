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

package org.jpos.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.custommonkey.xmlunit.XMLUnit;
import org.jpos.iso.packager.ISO87APackagerBBitmap;
import org.jpos.iso.packager.ISO93APackager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.junit.Before;
import org.junit.Test;

public class LogEventTest {

    @Before
    public void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void testAddMessage() throws Throwable {
        LogEvent logEvent = new LogEvent(new ISO87APackagerBBitmap(), "testLogEventTag", Integer.valueOf(-2));
        logEvent.addMessage("false");
        assertEquals("logEvent.payLoad.size()", 2, logEvent.getPayLoad().size());
        assertEquals("logEvent.payLoad.get(1)", "false", logEvent.getPayLoad().get(1));
    }

    @Test
    public void testAddMessage1() throws Throwable {
        LogEvent logEvent = new LogEvent(new Log(), "testString");
        logEvent.addMessage("#>", "testString");
        assertEquals("logEvent.payLoad.size()", 1, logEvent.getPayLoad().size());
        assertEquals("logEvent.payLoad.get(0)", "<#>>testString</#>>", logEvent.getPayLoad().get(0));
    }

    @Test
    public void testConstructor() throws Throwable {
        LogEvent logEvent = new LogEvent("testLogEventTag");
        assertEquals("logEvent.payLoad.size()", 0, logEvent.getPayLoad().size());
        assertEquals("logEvent.tag", "testLogEventTag", logEvent.getTag());
    }

    @Test
    public void testConstructor1() throws Throwable {
        LogEvent logEvent = new LogEvent();
        assertEquals("logEvent.payLoad.size()", 0, logEvent.getPayLoad().size());
        assertEquals("logEvent.tag", "info", logEvent.getTag());
    }

    @Test
    public void testConstructor3() throws Throwable {
        LogEvent logEvent = new LogEvent("testLogEventTag", "");
        assertEquals("logEvent.payLoad.size()", 1, logEvent.getPayLoad().size());
        assertEquals("logEvent.tag", "testLogEventTag", logEvent.getTag());
    }

    @Test
    public void testConstructor4() throws Throwable {
        LogSource source = new ISO93APackager();
        LogEvent logEvent = new LogEvent(source, "testString");
        assertSame("logEvent.source", source, logEvent.getSource());
        assertEquals("logEvent.payLoad.size()", 0, logEvent.getPayLoad().size());
        assertEquals("logEvent.tag", "testString", logEvent.getTag());
    }

    @Test
    public void testDump() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        new LogEvent("testLogEventTag").dump(p, "\r\n");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump1() throws Throwable {
        LogEvent logEvent = new LogEvent("testLogEventTag");
        logEvent.addMessage("testString", "testString");
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        logEvent.dump(p, "testLogEventIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump2() throws Throwable {
        LogEvent logEvent = new LogEvent("testLogEventTag", (Object) null);
        logEvent.addMessage("testString", "1s");
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        logEvent.dump(p, "testLogEventIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump3() throws Throwable {
        new LogEvent(new ThreadPool(0, 0), "testString", null).dump(new PrintStream(new ByteArrayOutputStream(), true), "a42");
        assertEquals("\"a42\"", "a42", "a42");
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            new LogEvent("testLogEventTag").dump(null, "testLogEventIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetRealm() throws Throwable {
        LogSource source = new ISO87APackagerBBitmap();
        LogEvent logEvent = new LogEvent(source, "testLogEventTag", Integer.valueOf(-2));
        String result = logEvent.getRealm();
        assertNull("result", result);
        assertSame("logEvent.source", source, logEvent.getSource());
    }

    @Test
    public void testGetRealm1() throws Throwable {
        LogSource source = new SystemMonitor(-100, new Logger(), "testLogEventRealm");
        LogEvent logEvent = new LogEvent(source, "testLogEventTag", "");
        String result = logEvent.getRealm();
        assertEquals("result", "testLogEventRealm", result);
        assertSame("logEvent.source", source, logEvent.getSource());
    }

    @Test
    public void testGetSource() throws Throwable {
        LogSource source = new ISO93APackager();
        LogSource result = new LogEvent(source, "x", Integer.valueOf(64)).getSource();
        assertSame("result", source, result);
    }

    @Test
    public void testSetSource() throws Throwable {
        LogEvent logEvent = new LogEvent(new Log(), "testLogEventTag");
        LogSource source = new ISOBaseValidatingPackager();
        logEvent.setSource(source);
        assertSame("logEvent.source", source, logEvent.getSource());
    }

}
