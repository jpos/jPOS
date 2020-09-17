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

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.jpos.util.LogFileTestUtils.getStringFromCompressedFile;
import static org.jpos.util.LogFileTestUtils.getStringFromFile;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DailyLogListenerTest {

    private LogRotationTestDirectory logRotationTestDirectory;

    @BeforeEach
    public void createLogRotateAbortsTestDir(@TempDir Path tempDir) {
        logRotationTestDirectory = new LogRotationTestDirectory(tempDir);
    }

    @Test
    public void testCheckSize() {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.checkSize();
        assertNull(dailyLogListener.f, "dailyLogListener.f");
        assertNotNull(dailyLogListener.p, "dailyLogListener.p");
    }

    @Test
    public void testCloseCompressedOutputStream() throws Throwable {
        OutputStream os = new DeflaterOutputStream(new PrintStream(new ByteArrayOutputStream()), new Deflater());
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.closeCompressedOutputStream(os);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testCloseCompressedOutputStream1() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        OutputStream os = new DataOutputStream(new ByteArrayOutputStream());
        dailyLogListener.closeCompressedOutputStream(os);
        assertEquals(0, ((DataOutputStream) os).size(), "(DataOutputStream) os.size()");
    }

    @Test
    public void testCloseCompressedOutputStreamThrowsNullPointerException() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        try {
            dailyLogListener.closeCompressedOutputStream(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.OutputStream.close()\" because \"os\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testCompress() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.compress(new File("testDailyLogListenerParam1"));
        assertEquals(0, dailyLogListener.getCompressionFormat(), "dailyLogListener.getCompressionFormat()");
    }

    @Test
    public void testCompressorConstructor() throws Throwable {
        File f = new File("testCompressorParam1");
        DailyLogListener.Compressor compressor = new DailyLogListener().new Compressor(f);
        assertSame(f, compressor.f, "compressor.f");
    }

    @Disabled("test fails because file testCompressorParam1 does not exists")
    @Test
    public void testCompressorRun() throws Throwable {
        File f = new File(new File("testCompressorParam1"), "testCompressorParam2");
        DailyLogListener.Compressor compressor = new DailyLogListener().new Compressor(f);
        compressor.run();
        assertSame(f, compressor.f, "compressor.f");
    }

    @Test
    public void testConstructor() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        assertEquals(131072, dailyLogListener.getCompressionBufferSize(), "dailyLogListener.getCompressionBufferSize()");
        assertNotNull(dailyLogListener.p, "dailyLogListener.p");
        assertEquals("", dailyLogListener.getCompressedSuffix(), "dailyLogListener.getCompressedSuffix()");
        assertEquals(".log", dailyLogListener.getSuffix(), "dailyLogListener.getSuffix()");
        assertEquals(0, dailyLogListener.getCompressionFormat(), "dailyLogListener.getCompressionFormat()");
        assertTrue(dailyLogListener.getDateFmt().isLenient(), "dailyLogListener.getDateFmt().isLenient()");
    }

    @Test
    public void testDailyRotateConstructor() throws Throwable {
        new DailyLogListener().new DailyRotate();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    @Disabled
    public void testDailyRotateRunThrowsNullPointerException() throws Throwable {
        try {
            new DailyLogListener().new DailyRotate().run();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
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
        assertTrue(true, "Test completed without Exception");
        // dependencies on static and environment state led to removal of 1
        // assertion
    }

    @Test
    public void testLogDebugEx() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.logDebugEx("testDailyLogListenerMsg", new NullPointerException("testDailyLogListenerParam1"));
        assertNotNull(dailyLogListener.p, "dailyLogListener.p");
    }

    @Test
    public void testLogDebugExThrowsNullPointerException() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        try {
            dailyLogListener.logDebugEx("testDailyLogListenerMsg", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.lang.Throwable.printStackTrace(java.io.PrintStream)\" because \"e\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNotNull(dailyLogListener.p, "dailyLogListener.p");
        }
    }

    @Test
    @Disabled
    public void testLogRotateThrowsNullPointerException() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        try {
            dailyLogListener.logRotate();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
            assertNull(dailyLogListener.f, "dailyLogListener.f");
            assertNull(dailyLogListener.p, "dailyLogListener.p");
        }
    }

    @Test
    public void testSetCompressedSuffix() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setCompressedSuffix("testDailyLogListenerCompressedSuffix");
        assertEquals("testDailyLogListenerCompressedSuffix", dailyLogListener.getCompressedSuffix(),
                "dailyLogListener.getCompressedSuffix()");
    }

    @Test
    public void testSetCompressionBufferSize() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setCompressionBufferSize(-1);
        assertEquals(131072, dailyLogListener.getCompressionBufferSize(), "dailyLogListener.getCompressionBufferSize()");
    }

    @Test
    public void testSetCompressionBufferSize1() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setCompressionBufferSize(1);
        assertEquals(1, dailyLogListener.getCompressionBufferSize(), "dailyLogListener.getCompressionBufferSize()");
    }

    @Test
    public void testSetCompressionBufferSize2() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setCompressionBufferSize(0);
        assertEquals(0, dailyLogListener.getCompressionBufferSize(), "dailyLogListener.getCompressionBufferSize()");
    }

    @Test
    public void testSetCompressionFormat() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setCompressionFormat(100);
        assertEquals(100, dailyLogListener.getCompressionFormat(), "dailyLogListener.getCompressionFormat()");
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        Configuration cfg = new SubConfiguration();
        try {
            dailyLogListener.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.getLong(String, long)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(dailyLogListener.rotate, "dailyLogListener.rotate");
            assertEquals(131072, dailyLogListener.getCompressionBufferSize(), "dailyLogListener.getCompressionBufferSize()");
            assertNotNull(dailyLogListener.p, "dailyLogListener.p");
            assertNull(dailyLogListener.getPrefix(), "dailyLogListener.getPrefix()");
            assertEquals(".log", dailyLogListener.getSuffix(), "dailyLogListener.getSuffix()");
            assertNull(dailyLogListener.f, "dailyLogListener.f");
            assertEquals("", dailyLogListener.getCompressedSuffix(), "dailyLogListener.getCompressedSuffix()");
            assertNull(dailyLogListener.logName, "dailyLogListener.logName");
            assertEquals(0L, dailyLogListener.sleepTime, "dailyLogListener.sleepTime");
            assertEquals(0L, dailyLogListener.maxSize, "dailyLogListener.maxSize");
            assertEquals(0, dailyLogListener.getCompressionFormat(), "dailyLogListener.getCompressionFormat()");
            assertTrue(dailyLogListener.getDateFmt().isLenient(), "dailyLogListener.getDateFmt().isLenient()");
        }
    }

    @Test
    public void testSetDateFmt() throws Throwable {
        DateFormat dateFmt = DateFormat.getInstance();
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setDateFmt(dateFmt);
        assertSame(dateFmt, dailyLogListener.getDateFmt(), "dailyLogListener.getDateFmt()");
    }

    @Test
    public void testSetLastDate() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setLastDate("testDailyLogListenerLastDate");
        assertEquals("testDailyLogListenerLastDate", dailyLogListener.getLastDate(), "dailyLogListener.getLastDate()");
    }

    @Test
    public void testSetPrefix() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setPrefix("testDailyLogListenerPrefix");
        assertEquals("testDailyLogListenerPrefix", dailyLogListener.getPrefix(), "dailyLogListener.getPrefix()");
    }

    @Test
    public void testSetRotateCount() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setRotateCount(100);
        assertEquals(100, dailyLogListener.getRotateCount(), "dailyLogListener.getRotateCount()");
    }

    @Test
    public void testSetSuffix() throws Throwable {
        DailyLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setSuffix("testDailyLogListenerSuffix");
        assertEquals("testDailyLogListenerSuffix", dailyLogListener.getSuffix(), "dailyLogListener.getSuffix()");
    }

	@Test
	public void testSetMaxAge() throws Throwable {
		DailyLogListener dailyLogListener = new DailyLogListener();
		long maxAge = 7*24*3600*1000;
		dailyLogListener.setMaxAge(maxAge);
		assertEquals(maxAge, dailyLogListener.getMaxAge(), "dailyLogListener.getMaxAge()");
	}

    @Test
    public void testLogRotationAndCompressionWorks() throws Exception {
        String logFileName = "RotateWorksTestLog";
        DailyLogListener listener = createCompressingDailyLogListenerWithIsoDateFormat(logFileName);

        listener.log(new LogEvent("Message 1"));

        // when: a rotation is executed
        listener.logRotate();

        // then: new events should end up in the current file and old events in the archived file
        listener.log(new LogEvent("Message 2"));

        String currentLogFileContents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".log"));
        assertFalse(currentLogFileContents.contains("Message 1"), "Current log file should not contain the first message");
        assertTrue(currentLogFileContents.contains("Message 2"), "Current log file should contain the second message");
        assertTrue(currentLogFileContents.contains("<logger "), "Logger element should not have been opened in the current file");
        assertFalse(currentLogFileContents.contains("</logger>"), "Logger element should not have been closed in the current file");

        Thread.sleep(1000); // to allow compressor thread to run
		listener.destroy();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String archivedLogFileContents = getStringFromCompressedFile(logRotationTestDirectory.getFile(logFileName + "." + date + ".log.gz"));
        assertTrue(archivedLogFileContents.contains("Message 1"), "Archived log file should contain the first message");
        assertFalse(archivedLogFileContents.contains("Message 2"), "Archived log file should not contain the second message");
        assertTrue(archivedLogFileContents.contains("<logger "), "Logger element should have been opened in the archived file");
        assertTrue(archivedLogFileContents.contains("</logger>"), "Logger element should have been closed in the archived file");
    }


    @Test
    public void testMaxAgeFeatureDisabledWithNegativeValue() throws Exception {
		String logFileName = "MaxAgeWorksTestLog";
		DailyLogListener listener = createCompressingDailyLogListenerWithIsoDateFormat(logFileName);

		// when: maxage is a negative value
		listener.setMaxAge(-1L);

		// then: the maxage feature should be disable
		listener.deleteOldLogs();

		String currentLogFileContents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".log"));
		assertTrue(currentLogFileContents.contains("maxage feature is disabled."),
                               "Log file should contain a descriptive message about maxage feature disable");

		listener.destroy();
    }

	@Test
	public void testMaxAgeFeatureDisabledWithZeroValue() throws Exception {
		String logFileName = "MaxAgeWorksTestLog";
		DailyLogListener listener = createCompressingDailyLogListenerWithIsoDateFormat(logFileName);

		// when: maxage is zero value
		listener.setMaxAge(0L);

		// then: the maxage feature should be disable
		listener.deleteOldLogs();

		String currentLogFileContents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".log"));
		assertTrue(currentLogFileContents.contains("maxage feature is disabled."),
                               "Log file should contain a descriptive message about maxage feature disable");

		listener.destroy();
	}

	@Test
	public void testMaxAgeFeature() throws Exception {
		String logFileName = "MaxAgeWorksTestLog";
		DailyLogListener listener = createCompressingDailyLogListenerWithIsoDateFormat(logFileName);

		// a short max age value (100ms) is set for testing purpose
		listener.setMaxAge(100);
		listener.log(new LogEvent("Message 1"));

		// when: rotation is executed
		listener.logRotate();

		// and: the rotated log gets old
		Thread.sleep(1000);

		// then: the rotate file should be deleted
		listener.deleteOldLogs();

		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		Path rotateLogPath = Paths.get(logFileName + "." + date + ".log.gz");
		assertFalse(Files.exists(rotateLogPath, LinkOption.NOFOLLOW_LINKS), "Rotated log should be deleted");

		listener.destroy();
	}

	@Test
	public void testMaxAgeFeatureWhenThereIsNonLogFiles() throws Exception {
		String logFileName = "MaxAgeWorksTestLog";
		DailyLogListener listener = createCompressingDailyLogListenerWithIsoDateFormat(logFileName);
		File emptyFile = createEmptyFile("EmptyFile.txt");

		// a short max age value (100ms) is set for testing purpose
		listener.setMaxAge(100);
		listener.log(new LogEvent("Message 1"));

		// when: rotation is executed
		listener.logRotate();

		// and: the rotated log gets old
		Thread.sleep(1000);

		// then: the rotate file should be deleted but empty file still is in path
		listener.deleteOldLogs();

		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		Path rotateLogPath = Paths.get(logFileName + "." + date + ".log.gz");
		assertFalse(Files.exists(rotateLogPath, LinkOption.NOFOLLOW_LINKS), "Rotated log should be deleted");
		assertTrue(Files.exists(emptyFile.toPath(), LinkOption.NOFOLLOW_LINKS), "Empty file should exist");

		listener.destroy();
	}

	@Test
	@Disabled("This feature doesn't work in Windows so we reverted the patch c94ff02f2")
	public void testLogRotateAbortsWhenCreatingNewFileFails() throws Exception {
		String logFileName = "RotateAbortsTestLog";
		DailyLogListener listener = createCompressingDailyLogListenerWithIsoDateFormat(logFileName);

		listener.log(new LogEvent("Message 1"));

		// when: a rotation is required but a new file cannot be created
		logRotationTestDirectory.preventNewFileCreation();
		listener.logRotate();

		// then: no error should escape and the existing log file should continue being
		// written to
		listener.log(new LogEvent("Message 2"));
		logRotationTestDirectory.allowNewFileCreation();

		String logFileContents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".log"));
		assertTrue(logFileContents.contains("Message 1"), "Log file should contain first message");
		assertTrue(logFileContents.contains("Message 2"), "Log file should contain second message");
		assertFalse(logFileContents.contains("</logger>"), "Logger element should not have been closed");

		Thread.sleep(1000); // to allow compressor thread to run
		listener.destroy();
		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		Path archiveFile = logRotationTestDirectory.getFile(logFileName + ".log." + date + ".gz");
		assertFalse(Files.exists(archiveFile), "Archive file should not exist");
	}

    private DailyLogListener createCompressingDailyLogListenerWithIsoDateFormat(String logFileName) throws ConfigurationException, IOException {
        DailyLogListener listener = new DailyLogListener();
        Properties configuration = new Properties();
        configuration.setProperty("prefix", logRotationTestDirectory.getDirectory().toAbsolutePath() + "/" + logFileName);
        configuration.setProperty("date-format", ".yyyy-MM-dd");
        configuration.setProperty("compression-format", "gzip");
        configuration.setProperty("maxsize", "1000000");
        logRotationTestDirectory.allowNewFileCreation();
        listener.setConfiguration(new SimpleConfiguration(configuration));
        return listener;
    }

	private File createEmptyFile(String fileName) throws IOException {
		File emptyFile = new File(logRotationTestDirectory.getDirectory().toAbsolutePath() + "/" + fileName);
		emptyFile.getParentFile().mkdirs();
		emptyFile.createNewFile();
		return emptyFile;
	}
}
