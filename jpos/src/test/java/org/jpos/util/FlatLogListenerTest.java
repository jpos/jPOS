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

import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.util.function.RemoveNewLinesMapper;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FlatLogListenerTest {

    @Test
    void testShouldDelegateSetConfigurationToMapper() throws ConfigurationException {
        FlatLogListener listener = new FlatLogListener();
        listener.mapper = mock(RemoveNewLinesMapper.class);
        SimpleConfiguration cfg = new SimpleConfiguration();
        listener.setConfiguration(cfg);
        verify(listener.mapper).setConfiguration(cfg);
    }

    @Test
    void testDestroyShouldCallCloseOnPrintStream() {
        FlatLogListener listener = new FlatLogListener();
        PrintStream printStream = mock(PrintStream.class);
        listener.p = printStream;
        listener.destroy();
        verify(printStream).close();
    }

    @Test
    void testInstantiationShouldSetupMapperAndStreams() {
        FlatLogListener listener = new FlatLogListener();
        assertNotNull(listener.mapper);
        assertNotNull(listener.captureStream);
        assertNotNull(listener.p);
    }

    @Test
    void testLog() throws IOException {
        FlatLogListener listener = new FlatLogListener();
        LogEvent ev = new LogEvent();
        ev.setNoArmor(true);
        ev.addMessage("Test");
        ev.addMessage("More");
        LogEvent r = listener.log(ev);
        ByteArrayOutputStream captureStream = new ByteArrayOutputStream();
        PrintStream capturePrintStream = new PrintStream(captureStream);
        r.dump(capturePrintStream, "");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(captureStream.toByteArray())));
        // No idea why there is 2 leading spaces, but it is not what is under test.
        assertEquals("  <info>    Test    More  </info>", reader.readLine());
        capturePrintStream.close();
        reader.close();
    }

    @Test
    void testLogCorrectlyMultipleEventsAKACaptureStreamResets() throws IOException {
        FlatLogListener listener = new FlatLogListener();
        LogEvent ev = new LogEvent();
        ev.setNoArmor(true);
        ev.addMessage("Test");
        ev.addMessage("More");
        LogEvent r = listener.log(ev);
        ByteArrayOutputStream captureStream = new ByteArrayOutputStream();
        PrintStream capturePrintStream = new PrintStream(captureStream);
        r.dump(capturePrintStream, "");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(captureStream.toByteArray())));
        captureStream.reset();
        // No idea why there is 2 leading spaces, but it is not what is under test.
        assertEquals("  <info>    Test    More  </info>", reader.readLine());
        assertFalse(reader.ready());
        reader.close();

        r = listener.log(ev);
        r.dump(capturePrintStream, "");
        reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(captureStream.toByteArray())));
        assertEquals("  <info>    Test    More  </info>", reader.readLine());
        // No more lines to read.
        assertFalse(reader.ready());
        capturePrintStream.close();
        reader.close();
    }
}
