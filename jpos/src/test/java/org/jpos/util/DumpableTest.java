package org.jpos.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

public class DumpableTest {

    private Dumpable dumpable;

    @Before
    public void setUp() throws Exception {
        dumpable = new Dumpable("testingDumpable", "987654321".getBytes());
    }

    @Test
    public void testDumpable() {
        assertThat(dumpable.name, is("testingDumpable"));
        assertThat(dumpable.payload, is("987654321".getBytes()));
    }

    @Test
    public void testDump() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(baos);
        dumpable.dump(p, ":-o");
        String expected = ":-o<testingDumpable>\n0000  39 38 37 36 35 34 33 32  31                       987654321\n:-o</testingDumpable>\n";
        assertThat(baos.toString(), is(expected));
    }
}
