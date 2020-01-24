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
import org.jpos.util.function.ByteArrayMapper;
import org.jpos.util.function.LogEventMapper;
import org.jpos.util.function.RemoveNewLinesMapper;
import org.jpos.util.function.TestLogEventMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MappingLogEventWriterTest {

    @Test
    void testSetPrintStreamShouldConfigurePrintStreamsWhenCapturePrintStreamNull() {
        MappingLogEventWriter writer = spy(new MappingLogEventWriter());
        writer.setPrintStream(new PrintStream(System.out));
        verify(writer).configureCaptureStreams();
    }

    @Test
    void testSetPrintStreamShouldNOTConfigurePrintStreamsWhenCapturePrintStreamNOTNull() {
        MappingLogEventWriter writer = spy(new MappingLogEventWriter());
        writer.capturePrintStream = new PrintStream(System.out);
        writer.setPrintStream(new PrintStream(System.out));
        verify(writer, never()).configureCaptureStreams();
    }

    @Test
    void testMapEventsShouldCallEventMappersInOrderAndReturnFinalResult() {
        MappingLogEventWriter writer = new MappingLogEventWriter();
        List<LogEventMapper> mappers = new ArrayList<>();
        writer.eventMappers = mappers;
        mappers.add(logEvent -> {
            logEvent.addMessage("1");
            return logEvent;
        });
        mappers.add(logEvent -> {
            logEvent.addMessage("2");
            return logEvent;
        });
        LogEvent ev = new LogEvent();
        LogEvent logEvent = writer.mapEvents(ev);
        assertEquals(2, logEvent.getPayLoad().size());
        assertEquals("1", logEvent.getPayLoad().get(0));
        assertEquals("2", logEvent.getPayLoad().get(1));
    }

    @Test
    void testMapEventsShouldNotThrowExceptionWhenEventMappersIsNull() {
        MappingLogEventWriter writer = new MappingLogEventWriter();
        assertDoesNotThrow(() -> writer.mapEvents(new LogEvent()));
    }

    @Test
    void testMapOutputsShouldCallOutputMappersInOrderAndReturnFinalResult() {
        MappingLogEventWriter writer = new MappingLogEventWriter();
        List<ByteArrayMapper> mappers = new ArrayList<>();
        writer.outputMappers = mappers;
        mappers.add(b -> {
            byte[] temp = Arrays.copyOf(b, b.length + 1);
            temp[temp.length - 1] = '1';
            return temp;
        });
        mappers.add(b -> {
            byte[] temp = Arrays.copyOf(b, b.length + 1);
            temp[temp.length - 1] = '2';
            return temp;
        });
        byte[] b = {'0'};
        byte[] r = writer.mapOutput(b);
        assertArrayEquals(new byte[]{'0','1','2'}, r);
    }

    @Test
    void testMapOutputShouldNotThrowExceptionWhenOutputMappersIsNull() {
        MappingLogEventWriter writer = new MappingLogEventWriter();
        assertDoesNotThrow(() -> writer.mapOutput(new byte[]{}));
    }

    @Test
    void testShouldSetupAdditionalStreamsWhenOutputMappersNotEmpty() throws ConfigurationException {
        MappingLogEventWriter writer = new MappingLogEventWriter();
        List<ByteArrayMapper> mappers = new ArrayList<>();
        writer.outputMappers = mappers;
        mappers.add(b -> b);
        PrintStream printStream = new PrintStream(System.out);
        writer.setPrintStream(printStream);
        assertEquals(writer.p, printStream);
        assertNotNull(writer.captureOutputStream);
        assertNotNull(writer.capturePrintStream);
        assertNotEquals(writer.capturePrintStream, writer.p);
        printStream.close();
    }

    @Test
    void testShouldNOTSetupAdditionalStreamsWhenOutputMappersEmptyOrNull() throws ConfigurationException {
        Element root = new Element("root");
        MappingLogEventWriter writer = new MappingLogEventWriter();
        PrintStream printStream = new PrintStream(System.out);
        writer.setPrintStream(printStream);
        writer.setConfiguration(root);
        assertEquals(printStream, writer.p);
        assertNull(writer.captureOutputStream);
        assertNull(writer.capturePrintStream);

        writer = new MappingLogEventWriter();
        writer.outputMappers = new ArrayList<>();
        writer.setPrintStream(printStream);
        writer.setConfiguration(root);
        assertEquals(printStream, writer.p);
        assertNull(writer.captureOutputStream);
        assertNull(writer.capturePrintStream);
        printStream.close();
    }

    @Test
    void testConfigureEventMappersShouldThrowExceptionWhenFailOnNewInstance() {
        Element root = new Element("root");
        Element em = new Element("event-mapper");
        em.setAttribute("class", "i.dont.Exist");
        root.addContent(em);
        MappingLogEventWriter writer = new MappingLogEventWriter();
        assertThrows(ConfigurationException.class, () -> writer.setConfiguration(root));
    }

    @Test
    void testConfigureOutputMappersShouldThrowExceptionWhenFailOnNewInstance() {
        Element root = new Element("root");
        Element om = new Element("output-mapper");
        om.setAttribute("class", "i.dont.Exist");
        root.addContent(om);
        MappingLogEventWriter writer = new MappingLogEventWriter();
        assertThrows(ConfigurationException.class, () -> writer.setConfiguration(root));
    }

    @Test
    void testShouldClosePrintStreamsProperly() throws ConfigurationException, IOException {
        Element root = new Element("root");
        MappingLogEventWriter writer = new MappingLogEventWriter();
        PrintStream printStream = new PrintStream(System.out);
        writer.setPrintStream(printStream);
        List<ByteArrayMapper> mappers = new ArrayList<>();
        writer.outputMappers = mappers;
        mappers.add(b -> b);
        writer.setConfiguration(root);
        writer.close();
        assertNull(writer.p);
        assertNull(writer.capturePrintStream);
        assertNull(writer.captureOutputStream);

        PrintStream p1 = mock(PrintStream.class);
        PrintStream p2 = mock(PrintStream.class);
        writer.p = p1;
        writer.capturePrintStream = p2;
        writer.close();
        verify(p1).close();
        verify(p2).close();
    }

    @Test
    void testShouldNotTryToCloseAnyStreamsThatAreNull() {
        MappingLogEventWriter writer = new MappingLogEventWriter();
        writer.p = null;
        writer.captureOutputStream = null;
        writer.capturePrintStream = null;
        assertDoesNotThrow(writer::close);
    }

    @Test
    void testShouldAddEventMapperWhenConfigured() throws ConfigurationException {
        Element root = new Element("root");
        Element em = new Element("event-mapper");
        em.setAttribute("class", "org.jpos.util.function.TestLogEventMapper");
        root.addContent(em);
        MappingLogEventWriter writer = new MappingLogEventWriter();
        assertNull(writer.eventMappers);
        writer.setConfiguration(root);
        assertNotNull(writer.eventMappers);
        assertEquals(1, writer.eventMappers.size());
        assertTrue(writer.eventMappers.get(0) instanceof TestLogEventMapper);
    }

    @Test
    void testShouldAddOutputMapperWhenConfigured() throws ConfigurationException {
        Element root = new Element("root");
        Element om = new Element("output-mapper");
        om.setAttribute("class", "org.jpos.util.function.RemoveNewLinesMapper");
        root.addContent(om);
        MappingLogEventWriter writer = new MappingLogEventWriter();
        assertNull(writer.outputMappers);
        writer.setConfiguration(root);
        assertNotNull(writer.outputMappers);
        assertEquals(1, writer.outputMappers.size());
        assertTrue(writer.outputMappers.get(0) instanceof RemoveNewLinesMapper);
    }

    @Test
    void testWriteShouldMapEventsThenWriteOnSuperWhenNoOutputMappers() {
        MappingLogEventWriter spy = spy(new MappingLogEventWriter());
        spy.outputMappers = new ArrayList<>();
        spy.captureOutputStream = new ByteArrayOutputStream();
        spy.p = mock(PrintStream.class);
        LogEvent ev = new LogEvent();
        spy.write(ev);
        InOrder inOrder = inOrder(spy);
        inOrder.verify(spy).mapEvents(ev);
        inOrder.verify(spy).delegateWriteToSuper(ev);
        verify(spy, never()).writeToCaptureStream(ev);
        verify(spy, never()).mapOutput(any());
    }

    @Test
    void testWriteShouldMapEventsThenMapOutputThenWriteToMainPrintStream() throws IOException {
        MappingLogEventWriter spy = spy(new MappingLogEventWriter());
        spy.outputMappers = new ArrayList<>();
        spy.outputMappers.add(b -> b);
        PrintStream printStream = mock(PrintStream.class);
        spy.setPrintStream(printStream);
        LogEvent ev = new LogEvent();
        spy.write(ev);
        InOrder inOrder = inOrder(spy, printStream);
        inOrder.verify(spy).mapEvents(ev);
        inOrder.verify(spy).writeToCaptureStream(ev);
        inOrder.verify(spy).mapOutput(any());
        inOrder.verify(printStream).write(any());
        verify(spy, never()).delegateWriteToSuper(ev);
    }

    @Test
    void testWriteShouldNotCallMapOutputOrWriteToCaptureStreamsWhenNoOutputMappersExist() {
        MappingLogEventWriter spy = spy(new MappingLogEventWriter());
        LogEvent ev = new LogEvent();
        spy.write(ev);
        verify(spy, never()).mapOutput(any());
    }

    @Test
    void testMakeSureOutputIsOnlyThatOfCurrentEvent() throws IOException {
        MappingLogEventWriter writer = new MappingLogEventWriter();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        writer.setPrintStream(printStream);
        List<ByteArrayMapper> outputMappers = new ArrayList<>();
        outputMappers.add(b -> b);
        writer.outputMappers = outputMappers;
        writer.configureCaptureStreams();
        LogEvent e1 = new LogEvent("1");
        e1.setNoArmor(true);
        e1.addMessage("1");
        LogEvent e2 = new LogEvent("2");
        e2.addMessage("2");
        e2.setNoArmor(true);
        writer.write(e1);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));
        assertEquals(4, reader.lines().count());
        reader.close();
        byteArrayOutputStream.reset();
        writer.write(e2);
        reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));
        assertEquals(4, reader.lines().count());
        reader.close();
        printStream.close();
    }
}
