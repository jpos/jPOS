/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
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

import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BaseLogEventWriterTest {

    @Test
    void testSetPrintStreamShouldCallCloseWhenNullArgument() {
        TestLogEventWriter writer = spy(new TestLogEventWriter());
        writer.setPrintStream(null);
        verify(writer).close();
        assertNull(writer.p);
    }

    @Test
    void testSetPrintStreamShouldCallCloseOnDifferentArgument() {
        TestLogEventWriter writer = spy(new TestLogEventWriter());
        writer.p = new PrintStream(System.out);
        PrintStream newP = new PrintStream(System.out);
        writer.setPrintStream(newP);
        verify(writer).close();
        assertEquals(newP, writer.p);
    }

    @Test
    void testSetPrintStreamShouldNOTCallCloseWhenSetSameOrFirstTime() {
        TestLogEventWriter writer = spy(new TestLogEventWriter());
        PrintStream printStream = new PrintStream(System.out);
        writer.setPrintStream(printStream);
        writer.setPrintStream(printStream);
        verify(writer, never()).close();
    }

    @Test
    void testShouldClosePrintStreamOnClose() {
        LogEventWriter writer = new TestLogEventWriter();
        PrintStream p = mock(PrintStream.class);
        writer.setPrintStream(p);
        writer.close();
        verify(p).close();
    }

    @Test
    void testShouldNotAttemptCloseOnNullPrintStream() {
        LogEventWriter writer = new TestLogEventWriter();
        writer.setPrintStream(null);
        assertDoesNotThrow(writer::close);
    }

    @Test
    void testShouldSetPrintStreamNullOnClose() {
        BaseLogEventWriter writer = new BaseLogEventWriter() {
            @Override
            public synchronized void close() {
                super.close();
            }
        };
        writer.setPrintStream(new PrintStream(System.out));
        writer.close();
        assertNull(writer.p);
    }

    @Test
    void testShouldCallDumpOnLogEventAndFlushPrintStream() {
        LogEventWriter writer = new TestLogEventWriter();
        PrintStream p = mock(PrintStream.class);
        writer.setPrintStream(p);
        LogEvent ev = mock(LogEvent.class);
        writer.write(ev);
        verify(ev).dump(any(), anyString());
        verify(p).flush();
    }

    @Test
    void testShouldNotThrowExceptionOnWriteIfPrintStreamOrLogEventIsNull() {
        LogEventWriter writer = new TestLogEventWriter();
        writer.setPrintStream(null);
        assertDoesNotThrow(() -> writer.write(new LogEvent()));
        writer.setPrintStream(new PrintStream(System.out));
        assertDoesNotThrow(() -> writer.write(null));
    }

    static class TestLogEventWriter extends BaseLogEventWriter { }
}
