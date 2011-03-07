package org.jpos.iso.header;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class BaseHeaderTest {

    @Test
    public void testClone() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        byte[] header2 = new byte[1];
        baseHeader.unpack(header2);
        BaseHeader result = (BaseHeader) baseHeader.clone();
        assertEquals("result.getLength()", 1, result.getLength());
        assertSame("baseHeader.header", header2, baseHeader.header);
    }

    @Test
    public void testClone1() throws Throwable {
        BaseHeader result = (BaseHeader) new BaseHeader().clone();
        assertEquals("result.getLength()", 0, result.getLength());
    }

    @Test
    public void testConstructor() throws Throwable {
        BaseHeader baseHeader = new BaseHeader();
        assertNull("baseHeader.header", baseHeader.header);
    }

    @Test
    public void testConstructor1() throws Throwable {
        byte[] header = new byte[3];
        BaseHeader baseHeader = new BaseHeader(header);
        assertSame("baseHeader.header", header, baseHeader.header);
    }

    @Test
    public void testDump() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true);
        new BaseHeader(null).dump(p, "testBaseHeaderIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump1() throws Throwable {
        byte[] header = new byte[3];
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        new BaseHeader(header).dump(p, "testBaseHeaderIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            new BASE1Header("testBaseHeaderSource", "testBaseHeaderDestination").dump(null, "testBaseHeaderIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetDestination() throws Throwable {
        String result = new BaseHeader().getDestination();
        assertNull("result", result);
    }

    @Test
    public void testGetLength() throws Throwable {
        byte[] header = new byte[3];
        int result = new BaseHeader(header).getLength();
        assertEquals("result", 3, result);
    }

    @Test
    public void testGetLength1() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        int unpack = baseHeader.unpack((byte[]) null);
        int result = baseHeader.getLength();
        assertEquals("result", unpack, result);
    }

    @Test
    public void testGetSource() throws Throwable {
        String result = new BaseHeader().getSource();
        assertNull("result", result);
    }

    @Test
    public void testPack() throws Throwable {
        byte[] header = new byte[3];
        BaseHeader baseHeader = new BaseHeader(header);
        byte[] header2 = new byte[3];
        baseHeader.unpack(header2);
        byte[] result = baseHeader.pack();
        assertSame("result", header2, result);
        assertEquals("header2[0]", (byte) 0, header2[0]);
    }

    @Test
    public void testSetDestination() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        baseHeader.setDestination("testBaseHeaderDst");
        assertEquals("baseHeader.getLength()", 0, baseHeader.getLength());
    }

    @Test
    public void testSetSource() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        baseHeader.setSource("testBaseHeaderSrc");
        assertEquals("baseHeader.getLength()", 0, baseHeader.getLength());
    }

    @Test
    public void testSwapDirection() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        baseHeader.swapDirection();
        assertEquals("baseHeader.getLength()", 0, baseHeader.getLength());
    }

    @Test
    public void testUnpack() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        int result = baseHeader.unpack((byte[]) null);
        assertNull("baseHeader.header", baseHeader.header);
        assertEquals("result", 0, result);
    }

    @Test
    public void testUnpack1() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        byte[] header2 = new byte[1];
        int result = baseHeader.unpack(header2);
        assertSame("baseHeader.header", header2, baseHeader.header);
        assertEquals("baseHeader.header[0]", (byte) 0, baseHeader.header[0]);
        assertEquals("result", 1, result);
    }
}
