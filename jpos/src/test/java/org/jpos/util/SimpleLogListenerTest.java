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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class SimpleLogListenerTest {

    @Test
    public void testClose() throws Throwable {
        SimpleLogListener simpleLogListener = new SimpleLogListener(new PrintStream(new ByteArrayOutputStream(), true));
        simpleLogListener.close();
        assertNull("simpleLogListener.p", simpleLogListener.p);
    }

    @Test
    public void testClose1() throws Throwable {
        SimpleLogListener simpleLogListener = new SimpleLogListener(null);
        simpleLogListener.close();
        assertNull("simpleLogListener.p", simpleLogListener.p);
    }

    @Test
    public void testConstructor() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true);
        SimpleLogListener simpleLogListener = new SimpleLogListener(p);
        assertSame("simpleLogListener.p", p, simpleLogListener.p);
    }

    @Test
    public void testConstructor1() throws Throwable {
        SimpleLogListener simpleLogListener = new SimpleLogListener();
        assertNotNull("simpleLogListener.p", simpleLogListener.p);
    }

    @Test
    public void testLog() throws Throwable {
        LogEvent result = new SimpleLogListener(null).log(null);
        assertNull("result", result);
    }

    @Test
    public void testLog1() throws Throwable {
        LogEvent ev = new LogEvent("testSimpleLogListenerTag", "1");
        LogEvent result = new SimpleLogListener(null).log(ev);
        assertSame("result", ev, result);
    }

    @Test
    public void testSetPrintStream() throws Throwable {
        SimpleLogListener simpleLogListener = new SimpleLogListener();
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true);
        simpleLogListener.setPrintStream(p);
        assertSame("simpleLogListener.p", p, simpleLogListener.p);
    }

}
