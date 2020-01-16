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

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SimpleLogListenerTest {

    @Test
    public void testClose() throws Throwable {
        SimpleLogListener simpleLogListener = new SimpleLogListener(new PrintStream(new ByteArrayOutputStream(), true));
        simpleLogListener.close();
        assertNull(simpleLogListener.p, "simpleLogListener.p");
    }

    @Test
    public void testClose1() throws Throwable {
        SimpleLogListener simpleLogListener = new SimpleLogListener(null);
        simpleLogListener.close();
        assertNull(simpleLogListener.p, "simpleLogListener.p");
    }

    @Test
    public void testConstructor() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true);
        SimpleLogListener simpleLogListener = new SimpleLogListener(p);
        assertSame(p, simpleLogListener.p, "simpleLogListener.p");
    }

    @Test
    public void testConstructor1() throws Throwable {
        SimpleLogListener simpleLogListener = new SimpleLogListener();
        assertNotNull(simpleLogListener.p, "simpleLogListener.p");
    }

    @Test
    public void testLog() throws Throwable {
        LogEvent result = new SimpleLogListener(null).log(null);
        assertNull(result, "result");
    }

    @Test
    public void testLog1() throws Throwable {
        LogEvent ev = new LogEvent("testSimpleLogListenerTag", "1");
        LogEvent result = new SimpleLogListener(null).log(ev);
        assertSame(ev, result, "result");
    }

    @Test
    public void testSetPrintStream() throws Throwable {
        SimpleLogListener simpleLogListener = new SimpleLogListener();
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true);
        simpleLogListener.setPrintStream(p);
        assertSame(p, simpleLogListener.p, "simpleLogListener.p");
    }

    @Test
    void testSetConfigurationShouldNotCreateAndSetWriterIfNotPresent() throws ConfigurationException {
        SimpleLogListener simpleLogListener = new SimpleLogListener();
        Element root = new Element("root");
        simpleLogListener.setConfiguration(root);
        assertNull(simpleLogListener.writer);
    }

    @Test
    void testSetConfigurationShouldThrowConfigurationExceptionOnNewInstanceFailure() {
        SimpleLogListener simpleLogListener = new SimpleLogListener();
        Element root = new Element("root");
        Element we = new Element("writer");
        Element prop = new Element("property");
        we.setAttribute("class", "org.jpos.util.FakeLogEventWriter");
        we.addContent(prop);
        root.addContent(we);
        assertThrows(ConfigurationException.class, () -> simpleLogListener.setConfiguration(root));
    }

    @Test
    void testSetConfigurationShouldThrowConfigurationExceptionWhenWriterClassAttributeMissing() {
        SimpleLogListener simpleLogListener = new SimpleLogListener();
        Element root = new Element("root");
        Element we = new Element("writer");
        root.addContent(we);
        assertThrows(ConfigurationException.class, () -> simpleLogListener.setConfiguration(root));
    }

    @Test
    void testShouldClosePrintStreamOnNonNullWriter() {
        SimpleLogListener simpleLogListener = new SimpleLogListener();
        LogEventWriter logEventWriter = mock(LogEventWriter.class);
        simpleLogListener.accept(logEventWriter);
        simpleLogListener.setPrintStream(new PrintStream(System.out));
        assertNotNull(simpleLogListener.p);
        simpleLogListener.close();
        verify(logEventWriter).close();
        assertNull(simpleLogListener.p);
    }

    @Test
    void testSetPrintStreamShouldSetPrintStreamOnNonNullWriter() {
        SimpleLogListener simpleLogListener = new SimpleLogListener();
        LogEventWriter logEventWriter = mock(LogEventWriter.class);
        PrintStream printStream = new PrintStream(System.out);
        simpleLogListener.accept(logEventWriter);
        simpleLogListener.setPrintStream(printStream);
        verify(logEventWriter).setPrintStream(printStream);
    }

    @Test
    void testShouldLogUsingWriter() {
        SimpleLogListener simpleLogListener = new SimpleLogListener();
        LogEventWriter writer = mock(LogEventWriter.class);
        simpleLogListener.accept(writer);
        LogEvent ev = new LogEvent();
        simpleLogListener.log(ev);
        verify(writer).write(ev);
    }

    @Test
    void testShouldDumpDirectlyOnEventWhenWriterNull() {
        SimpleLogListener simpleLogListener = new SimpleLogListener();
        simpleLogListener.setPrintStream(System.out);
        LogEvent ev = mock(LogEvent.class);
        assertNull(simpleLogListener.writer);
        simpleLogListener.log(ev);
        verify(ev).dump(System.out, "");
    }
}
