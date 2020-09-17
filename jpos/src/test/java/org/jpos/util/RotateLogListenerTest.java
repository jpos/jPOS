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
import static org.jpos.util.LogFileTestUtils.getStringFromFile;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RotateLogListenerTest {

    private LogRotationTestDirectory logRotationTestDirectory;

    @BeforeEach
    public void createLogRotateAbortsTestDir(@TempDir Path tempDir) {
        logRotationTestDirectory = new LogRotationTestDirectory(tempDir);
    }

    @Test
    public void testCheckSizeThrowsNullPointerException() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        try {
            rotateLogListener.openLogFile();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
            assertNull(rotateLogListener.f, "rotateLogListener.f");
            assertNotNull(rotateLogListener.p, "rotateLogListener.p");
        }
    }

    @Test
    @Disabled("test causes problems, closes stdout")
    public void testCloseLogFile() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        rotateLogListener.closeLogFile();
        assertNull(rotateLogListener.f, "rotateLogListener.f");
    }

    @Test
    public void testConstructor() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        assertNotNull(rotateLogListener.p, "rotateLogListener.p");
    }

    @Test
    public void testDestroy() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.destroy();
        assertNull(((DailyLogListener) dailyLogListener).f, "(DailyLogListener) dailyLogListener.f");
    }

    @Test
    @Disabled("test causes problems, closes stdout")
    public void testLog() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.close();
        LogEvent ev = new LogEvent();
        LogEvent result = dailyLogListener.log(ev);
        assertSame(ev, result, "result");
        assertNull(((DailyLogListener) dailyLogListener).p, "(DailyLogListener) dailyLogListener.p");
        assertEquals(1, ((DailyLogListener) dailyLogListener).msgCount, "(DailyLogListener) dailyLogListener.msgCount");
    }

    @Test
    @Disabled("test causes problems, closes stdout")
    public void testLog1() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.close();
        LogEvent result = dailyLogListener.log(null);
        assertNull(result, "result");
        assertNull(((DailyLogListener) dailyLogListener).p, "(DailyLogListener) dailyLogListener.p");
        assertEquals(1, ((DailyLogListener) dailyLogListener).msgCount, "(DailyLogListener) dailyLogListener.msgCount");
    }

    @Test
    public void testLogDebug() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.logDebug("testRotateLogListenerMsg");
        assertNotNull(((DailyLogListener) dailyLogListener).p, "(DailyLogListener) dailyLogListener.p");
    }

    @Test
    @Disabled("test causes problems, closes stdout")
    public void testLogDebug1() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.close();
        dailyLogListener.logDebug("testRotateLogListenerMsg");
        assertNull(((DailyLogListener) dailyLogListener).p, "(DailyLogListener) dailyLogListener.p");
    }

    @Test
    @Disabled("test causes problems, closes stdout")
    public void testLogRotateThrowsNullPointerException() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        try {
            rotateLogListener.logRotate();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(rotateLogListener.f, "rotateLogListener.f");
            assertNull(rotateLogListener.p, "rotateLogListener.p");
        }
    }

    @Test
    public void testOpenLogFileThrowsNullPointerException() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        try {
            dailyLogListener.openLogFile();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(((DailyLogListener) dailyLogListener).f, "(DailyLogListener) dailyLogListener.f");
            assertNotNull(((DailyLogListener) dailyLogListener).p, "(DailyLogListener) dailyLogListener.p");
        }
    }

    @Test
    public void testRotateConstructor() throws Throwable {
        new RotateLogListener().new Rotate();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        Configuration cfg = new SubConfiguration();
        try {
            rotateLogListener.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.getInt(String, int)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(rotateLogListener.logName, "rotateLogListener.logName");
            assertEquals(0L, rotateLogListener.sleepTime, "rotateLogListener.sleepTime");
            assertEquals(0L, rotateLogListener.maxSize, "rotateLogListener.maxSize");
            assertEquals(0, rotateLogListener.maxCopies, "rotateLogListener.maxCopies");
            assertNull(rotateLogListener.rotate, "rotateLogListener.rotate");
            assertNull(rotateLogListener.f, "rotateLogListener.f");
            assertNotNull(rotateLogListener.p, "rotateLogListener.p");
        }
    }

    @Test
    public void testLogRotationWorks() throws Exception {
        String logFileName = "RotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName);

        listener.log(new LogEvent("Message 1"));

        // when: a rotation is executed
        listener.logRotate();

        // then: new events should end up in the current file and old events in the archived file
        listener.log(new LogEvent("Message 2"));

        String currentLogFileContents = getStringFromFile(logRotationTestDirectory.getFile(logFileName));
        assertFalse(currentLogFileContents.contains("Message 1"), "Current log file should not contain the first message");
        assertTrue(currentLogFileContents.contains("Message 2"), "Current log file should contain the second message");
        assertTrue(currentLogFileContents.contains("<logger "), "Logger element should have been opened in the current file");
        assertFalse(currentLogFileContents.contains("</logger>"), "Logger element should not have been closed in the current file");

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        assertTrue(archivedLogFile1Contents.contains("Message 1"), "Archived log file should contain the first message");
        assertFalse(archivedLogFile1Contents.contains("Message 2"), "Archived log file should not contain the second message");
        assertTrue(archivedLogFile1Contents.contains("<logger "), "Logger element should have been opened in the archived file");
        assertTrue(archivedLogFile1Contents.contains("</logger>"), "Logger element should have been closed in the archived file");

        // when: another rotation is executed
        listener.logRotate();

        // then: new events should end up in the current file and old events in the archived files
        listener.log(new LogEvent("Message 3"));

        currentLogFileContents = getStringFromFile(logRotationTestDirectory.getFile(logFileName));
        assertFalse(currentLogFileContents.contains("Message 1"), "Current log file should not contain the first message");
        assertFalse(currentLogFileContents.contains("Message 2"), "Current log file should not contain the second message");
        assertTrue(currentLogFileContents.contains("Message 3"), "Current log file should contain the third message");
        assertTrue(currentLogFileContents.contains("<logger "), "Logger element should have been opened in the current file");
        assertFalse(currentLogFileContents.contains("</logger>"), "Logger element should not have been closed in the current file");

        archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        assertTrue(archivedLogFile1Contents.contains("Message 2"), "Archived log file should contain the second message");
        assertFalse(archivedLogFile1Contents.contains("Message 3"), "Archived log file should not contain the third message");
        assertTrue(archivedLogFile1Contents.contains("<logger "), "Logger element should have been opened in the archived file");
        assertTrue(archivedLogFile1Contents.contains("</logger>"), "Logger element should have been closed in the archived file");

        String archivedLogFile2Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".2"));
        assertTrue(archivedLogFile2Contents.contains("Message 1"), "Archived log file should contain the first message");
        assertTrue(archivedLogFile2Contents.contains("<logger "), "Logger element should have been opened in the archived file");
        assertTrue(archivedLogFile2Contents.contains("</logger>"), "Logger element should have been closed in the archived file");

    }

    @Test
    @Disabled("This feature doesn't work in Windows so we reverted the patch c94ff02f2")
    public void testLogRotateAbortsWhenCreatingNewFileFails() throws Exception {
        String logFileName = "RotateAbortsTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, null);

        listener.log(new LogEvent("Message 1"));

        // when: a rotation is required but a new file cannot be created
        logRotationTestDirectory.preventNewFileCreation();
        listener.logRotate();

        // then: no error should escape and the existing log file should continue being written to
        listener.log(new LogEvent("Message 2"));
        logRotationTestDirectory.allowNewFileCreation();

        String logFileContents = getStringFromFile(logRotationTestDirectory.getFile(logFileName));
        System.out.println("logFileContents = " + logFileContents);
        assertTrue(logFileContents.contains("Message 1"), "Log file should contain first message");
        assertTrue(logFileContents.contains("Message 2"), "Log file should contain second message");
        assertFalse(logFileContents.contains("</logger>"), "Logger element should not have been closed");

        Path archiveFile = logRotationTestDirectory.getFile(logFileName + ".1");
        assertFalse(Files.exists(archiveFile), "Archive file should not exist");
    }

    @Test
    public void testNoFileNamePatternThenNoReplacement() throws ConfigurationException, IOException {
        String logFileName = "%s-important-log";
        Properties config = new Properties();
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, config);
        assertEquals(
                logRotationTestDirectory.getDirectory().toAbsolutePath() + "/%s-important-log",
                listener.logName);
    }

    @Test
    public void testEmptyFileNamePatternThenNoReplacement() throws ConfigurationException, IOException {
        String logFileName = "%s-important-log";
        Properties config = new Properties();
        config.setProperty("file-name-pattern", "");
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, config);
        assertEquals(
                logRotationTestDirectory.getDirectory().toAbsolutePath() + "/%s-important-log",
                listener.logName);
    }

    @Test
    public void testFileNamePatternHostNameReplacement() throws ConfigurationException, IOException {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "#h";
        }
        String logFileName = "%s-important-log";
        Properties config = new Properties();
        config.setProperty("file-name-pattern", "h");

        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, config);
        assertEquals(
                logRotationTestDirectory.getDirectory().toAbsolutePath() + "/" + hostname + "-important-log",
                listener.logName);
    }

    @Test
    public void testEnvironmentCodeParsing() {
        Map<String, String> env = System.getenv();
        RotateLogListener listener = new RotateLogListener();
        Map.Entry<String, String> entry = env.entrySet().iterator().next();
        String replaced = listener.fileNameFromPattern("%s-log", "e{" + entry.getKey() + "}");
        assertEquals(entry.getValue() + "-log", replaced);
    }

    private RotateLogListener createRotateLogListenerWithIsoDateFormat(String logFileName) throws ConfigurationException, IOException {
        return createRotateLogListenerWithIsoDateFormat(logFileName, null);
    }

    private RotateLogListener createRotateLogListenerWithIsoDateFormat(
            String logFileName,
            Properties customConfig) throws ConfigurationException, IOException {
        RotateLogListener listener = new RotateLogListener();
        Properties configuration = new Properties();
        configuration.setProperty("file", logRotationTestDirectory.getDirectory().toAbsolutePath() + "/" + logFileName);
        configuration.setProperty("copies", "10");
        configuration.setProperty("maxsize", "1000000");
        if (customConfig != null) {
            configuration.putAll(customConfig);
        }
        logRotationTestDirectory.allowNewFileCreation();
        listener.setConfiguration(new SimpleConfiguration(configuration));
        return listener;
    }
}
