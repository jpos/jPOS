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
