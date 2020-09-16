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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.custommonkey.xmlunit.XMLUnit;
import org.jpos.iso.packager.ISO87APackagerBBitmap;
import org.jpos.iso.packager.ISO93APackager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LogEventTest {

    @BeforeEach
    public void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void testAddMessage() throws Throwable {
        LogEvent logEvent = new LogEvent(new ISO87APackagerBBitmap(), "testLogEventTag", Integer.valueOf(-2));
        logEvent.addMessage("false");
        assertEquals(2, logEvent.getPayLoad().size(), "logEvent.payLoad.size()");
        assertEquals("false", logEvent.getPayLoad().get(1), "logEvent.payLoad.get(1)");
    }

    @Test
    public void testAddMessage1() throws Throwable {
        LogEvent logEvent = new LogEvent(new Log(), "testString");
        logEvent.addMessage("#>", "testString");
        assertEquals(1, logEvent.getPayLoad().size(), "logEvent.payLoad.size()");
        assertEquals("<#>>testString</#>>", logEvent.getPayLoad().get(0), "logEvent.payLoad.get(0)");
    }

    @Test
    public void testConstructor() throws Throwable {
        LogEvent logEvent = new LogEvent("testLogEventTag");
        assertEquals(0, logEvent.getPayLoad().size(), "logEvent.payLoad.size()");
        assertEquals("testLogEventTag", logEvent.getTag(), "logEvent.tag");
    }

    @Test
    public void testConstructor1() throws Throwable {
        LogEvent logEvent = new LogEvent();
        assertEquals(0, logEvent.getPayLoad().size(), "logEvent.payLoad.size()");
        assertEquals("info", logEvent.getTag(), "logEvent.tag");
    }

    @Test
    public void testConstructor3() throws Throwable {
        LogEvent logEvent = new LogEvent("testLogEventTag", "");
        assertEquals(1, logEvent.getPayLoad().size(), "logEvent.payLoad.size()");
        assertEquals("testLogEventTag", logEvent.getTag(), "logEvent.tag");
    }

    @Test
    public void testConstructor4() throws Throwable {
        LogSource source = new ISO93APackager();
        LogEvent logEvent = new LogEvent(source, "testString");
        assertSame(source, logEvent.getSource(), "logEvent.source");
        assertEquals(0, logEvent.getPayLoad().size(), "logEvent.payLoad.size()");
        assertEquals("testString", logEvent.getTag(), "logEvent.tag");
    }

    @Test
    public void testDump() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        new LogEvent("testLogEventTag").dump(p, "\r\n");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDump1() throws Throwable {
        LogEvent logEvent = new LogEvent("testLogEventTag");
        logEvent.addMessage("testString", "testString");
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        logEvent.dump(p, "testLogEventIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDump2() throws Throwable {
        LogEvent logEvent = new LogEvent("testLogEventTag", null);
        logEvent.addMessage("testString", "1s");
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        logEvent.dump(p, "testLogEventIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDump3() throws Throwable {
        new LogEvent(new ThreadPool(0, 0), "testString", null).dump(new PrintStream(new ByteArrayOutputStream(), true), "a42");
        assertEquals("a42", "a42", "\"a42\"");
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            new LogEvent("testLogEventTag").dump(null, "testLogEventIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.PrintStream.println(String)\" because \"p\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetRealm() throws Throwable {
        LogSource source = new ISO87APackagerBBitmap();
        LogEvent logEvent = new LogEvent(source, "testLogEventTag", Integer.valueOf(-2));
        String result = logEvent.getRealm();
        assertNull(result, "result");
        assertSame(source, logEvent.getSource(), "logEvent.source");
    }

    @Test
    public void testGetRealm1() throws Throwable {
        LogSource source = new SystemMonitor(-100, new Logger(), "testLogEventRealm");
        LogEvent logEvent = new LogEvent(source, "testLogEventTag", "");
        String result = logEvent.getRealm();
        assertEquals("testLogEventRealm", result, "result");
        assertSame(source, logEvent.getSource(), "logEvent.source");
    }

    @Test
    public void testGetSource() throws Throwable {
        LogSource source = new ISO93APackager();
        LogSource result = new LogEvent(source, "x", Integer.valueOf(64)).getSource();
        assertSame(source, result, "result");
    }

    @Test
    public void testSetSource() throws Throwable {
        LogEvent logEvent = new LogEvent(new Log(), "testLogEventTag");
        LogSource source = new ISOBaseValidatingPackager();
        logEvent.setSource(source);
        assertSame(source, logEvent.getSource(), "logEvent.source");
    }

}
