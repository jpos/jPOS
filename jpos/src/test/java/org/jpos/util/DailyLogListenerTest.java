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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.jpos.core.Configuration;
import org.jpos.core.SubConfiguration;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DailyLogListenerTest {

    @Test
    public void testCheckSize() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.checkSize();
        assertNull("dailyLogListener.f", dailyLogListener.f);
        assertNotNull("dailyLogListener.p", dailyLogListener.p);
    }

    @Test
    public void testCloseCompressedOutputStream() throws Throwable {
        OutputStream os = new DeflaterOutputStream(new PrintStream(new ByteArrayOutputStream()), new Deflater());
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.closeCompressedOutputStream(os);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testCloseCompressedOutputStream1() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        OutputStream os = new DataOutputStream(new ByteArrayOutputStream());
        dailyLogListener.closeCompressedOutputStream(os);
        assertEquals("(DataOutputStream) os.size()", 0, ((DataOutputStream) os).size());
    }

    @Test
    public void testCloseCompressedOutputStreamThrowsNullPointerException() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        try {
            dailyLogListener.closeCompressedOutputStream(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testCompress() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.compress(new File("testDailyLogListenerParam1"));
        assertEquals("dailyLogListener.getCompressionFormat()", 0, dailyLogListener.getCompressionFormat());
    }

    @Test
    public void testCompressorConstructor() throws Throwable {
        File f = new File("testCompressorParam1");
        DailyLogListener.Compressor compressor = new DailyLogListener().new Compressor(f);
        assertSame("compressor.f", f, compressor.f);
    }

    @Ignore("test fails because file testCompressorParam1 does not exists")
    @Test
    public void testCompressorRun() throws Throwable {
        File f = new File(new File("testCompressorParam1"), "testCompressorParam2");
        DailyLogListener.Compressor compressor = new DailyLogListener().new Compressor(f);
        compressor.run();
        assertSame("compressor.f", f, compressor.f);
    }

    @Test
    public void testConstructor() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        assertEquals("dailyLogListener.getCompressionBufferSize()", 131072, dailyLogListener.getCompressionBufferSize());
        assertNotNull("dailyLogListener.p", dailyLogListener.p);
        assertEquals("dailyLogListener.getCompressedSuffix()", "", dailyLogListener.getCompressedSuffix());
        assertEquals("dailyLogListener.getSuffix()", ".log", dailyLogListener.getSuffix());
        assertEquals("dailyLogListener.getCompressionFormat()", 0, dailyLogListener.getCompressionFormat());
        assertTrue("dailyLogListener.getDateFmt().isLenient()", dailyLogListener.getDateFmt().isLenient());
    }

    @Test
    public void testDailyRotateConstructor() throws Throwable {
        new DailyLogListener().new DailyRotate();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDailyRotateRunThrowsNullPointerException() throws Throwable {
        try {
            new DailyLogListener().new DailyRotate().run();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetCompressedOutputStreamThrowsNullPointerException() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        try {
            dailyLogListener.getCompressedOutputStream(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testGetCompressorThread() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.getCompressorThread(new File("testDailyLogListenerParam1"));
        assertTrue("Test completed without Exception", true);
        // dependencies on static and environment state led to removal of 1
        // assertion
    }

    @Test
    public void testLogDebugEx() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.logDebugEx("testDailyLogListenerMsg", new NullPointerException("testDailyLogListenerParam1"));
        assertNotNull("dailyLogListener.p", dailyLogListener.p);
    }

    @Test
    public void testLogDebugExThrowsNullPointerException() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        try {
            dailyLogListener.logDebugEx("testDailyLogListenerMsg", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNotNull("dailyLogListener.p", dailyLogListener.p);
        }
    }

    @Test
    public void testLogRotateThrowsNullPointerException() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        try {
            dailyLogListener.logRotate();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("dailyLogListener.f", dailyLogListener.f);
            assertNull("dailyLogListener.p", dailyLogListener.p);
        }
    }

    @Test
    public void testSetCompressedSuffix() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setCompressedSuffix("testDailyLogListenerCompressedSuffix");
        assertEquals("dailyLogListener.getCompressedSuffix()", "testDailyLogListenerCompressedSuffix",
                dailyLogListener.getCompressedSuffix());
    }

    @Test
    public void testSetCompressionBufferSize() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setCompressionBufferSize(-1);
        assertEquals("dailyLogListener.getCompressionBufferSize()", 131072, dailyLogListener.getCompressionBufferSize());
    }

    @Test
    public void testSetCompressionBufferSize1() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setCompressionBufferSize(1);
        assertEquals("dailyLogListener.getCompressionBufferSize()", 1, dailyLogListener.getCompressionBufferSize());
    }

    @Test
    public void testSetCompressionBufferSize2() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setCompressionBufferSize(0);
        assertEquals("dailyLogListener.getCompressionBufferSize()", 0, dailyLogListener.getCompressionBufferSize());
    }

    @Test
    public void testSetCompressionFormat() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setCompressionFormat(100);
        assertEquals("dailyLogListener.getCompressionFormat()", 100, dailyLogListener.getCompressionFormat());
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        Configuration cfg = new SubConfiguration();
        try {
            dailyLogListener.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("dailyLogListener.rotate", dailyLogListener.rotate);
            assertEquals("dailyLogListener.getCompressionBufferSize()", 131072, dailyLogListener.getCompressionBufferSize());
            assertNotNull("dailyLogListener.p", dailyLogListener.p);
            assertNull("dailyLogListener.getPrefix()", dailyLogListener.getPrefix());
            assertEquals("dailyLogListener.getSuffix()", ".log", dailyLogListener.getSuffix());
            assertNull("dailyLogListener.f", dailyLogListener.f);
            assertEquals("dailyLogListener.getCompressedSuffix()", "", dailyLogListener.getCompressedSuffix());
            assertNull("dailyLogListener.logName", dailyLogListener.logName);
            assertEquals("dailyLogListener.sleepTime", 0L, dailyLogListener.sleepTime);
            assertEquals("dailyLogListener.maxSize", 0L, dailyLogListener.maxSize);
            assertEquals("dailyLogListener.getCompressionFormat()", 0, dailyLogListener.getCompressionFormat());
            assertTrue("dailyLogListener.getDateFmt().isLenient()", dailyLogListener.getDateFmt().isLenient());
        }
    }

    @Test
    public void testSetDateFmt() throws Throwable {
        DateFormat dateFmt = DateFormat.getInstance();
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setDateFmt(dateFmt);
        assertSame("dailyLogListener.getDateFmt()", dateFmt, dailyLogListener.getDateFmt());
    }

    @Test
    public void testSetLastDate() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setLastDate("testDailyLogListenerLastDate");
        assertEquals("dailyLogListener.getLastDate()", "testDailyLogListenerLastDate", dailyLogListener.getLastDate());
    }

    @Test
    public void testSetPrefix() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setPrefix("testDailyLogListenerPrefix");
        assertEquals("dailyLogListener.getPrefix()", "testDailyLogListenerPrefix", dailyLogListener.getPrefix());
    }

    @Test
    public void testSetRotateCount() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setRotateCount(100);
        assertEquals("dailyLogListener.getRotateCount()", 100, dailyLogListener.getRotateCount());
    }

    @Test
    public void testSetSuffix() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setSuffix("testDailyLogListenerSuffix");
        assertEquals("dailyLogListener.getSuffix()", "testDailyLogListenerSuffix", dailyLogListener.getSuffix());
    }
}
