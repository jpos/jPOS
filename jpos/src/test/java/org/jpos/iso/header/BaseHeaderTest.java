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

package org.jpos.iso.header;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class BaseHeaderTest {

    @Test
    public void testClone() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        byte[] header2 = new byte[1];
        baseHeader.unpack(header2);
        BaseHeader result = (BaseHeader) baseHeader.clone();
        assertEquals(1, result.getLength(), "result.getLength()");
        assertTrue(Arrays.equals(header2, baseHeader.header), "baseHeader.header");
    }

    @Test
    public void testClone1() throws Throwable {
        BaseHeader result = (BaseHeader) new BaseHeader().clone();
        assertEquals(0, result.getLength(), "result.getLength()");
    }

    @Test
    public void testConstructor() throws Throwable {
        BaseHeader baseHeader = new BaseHeader();
        assertNull(baseHeader.header, "baseHeader.header");
    }

    @Test
    public void testConstructor1() throws Throwable {
        byte[] header = new byte[3];
        BaseHeader baseHeader = new BaseHeader(header);
        assertTrue(Arrays.equals(header, baseHeader.header), "baseHeader.header");
    }

    @Test
    public void testDump() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true);
        new BaseHeader(null).dump(p, "testBaseHeaderIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDump1() throws Throwable {
        byte[] header = new byte[3];
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        new BaseHeader(header).dump(p, "testBaseHeaderIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            new BASE1Header("testBaseHeaderSource", "testBaseHeaderDestination").dump(null, "testBaseHeaderIndent");
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
    public void testGetDestination() throws Throwable {
        String result = new BaseHeader().getDestination();
        assertNull(result, "result");
    }

    @Test
    public void testGetLength() throws Throwable {
        byte[] header = new byte[3];
        int result = new BaseHeader(header).getLength();
        assertEquals(3, result, "result");
    }

    @Test
    public void testGetLength1() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        int unpack = baseHeader.unpack(null);
        int result = baseHeader.getLength();
        assertEquals(unpack, result, "result");
    }

    @Test
    public void testGetSource() throws Throwable {
        String result = new BaseHeader().getSource();
        assertNull(result, "result");
    }

    @Test
    public void testPack() throws Throwable {
        byte[] header = new byte[3];
        BaseHeader baseHeader = new BaseHeader(header);
        byte[] header2 = new byte[3];
        baseHeader.unpack(header2);
        byte[] result = baseHeader.pack();
        assertTrue(Arrays.equals(header2, result), "result");
        assertEquals((byte) 0, header2[0], "header2[0]");
    }

    @Test
    public void testSetDestination() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        baseHeader.setDestination("testBaseHeaderDst");
        assertEquals(0, baseHeader.getLength(), "baseHeader.getLength()");
    }

    @Test
    public void testSetSource() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        baseHeader.setSource("testBaseHeaderSrc");
        assertEquals(0, baseHeader.getLength(), "baseHeader.getLength()");
    }

    @Test
    public void testSwapDirection() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        baseHeader.swapDirection();
        assertEquals(0, baseHeader.getLength(), "baseHeader.getLength()");
    }

    @Test
    public void testUnpack() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        int result = baseHeader.unpack(null);
        assertNull(baseHeader.header, "baseHeader.header");
        assertEquals(0, result, "result");
    }

    @Test
    public void testUnpack1() throws Throwable {
        byte[] header = new byte[0];
        BaseHeader baseHeader = new BaseHeader(header);
        byte[] header2 = new byte[1];
        int result = baseHeader.unpack(header2);
        assertTrue(Arrays.equals(header2, baseHeader.header), "baseHeader.header");
        assertEquals((byte) 0, baseHeader.header[0], "baseHeader.header[0]");
        assertEquals(1, result, "result");
    }
}
