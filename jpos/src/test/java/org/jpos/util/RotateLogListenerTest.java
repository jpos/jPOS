/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2019 jPOS Software SRL
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

import static org.jpos.util.LogFileTestUtils.getStringFromFile;
import static org.junit.Assert.*;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

public class RotateLogListenerTest {

    private final LogRotationTestDirectory logRotationTestDirectory = new LogRotationTestDirectory();

    @Test
    public void testCheckSizeThrowsNullPointerException() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        try {
            rotateLogListener.openLogFile();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("rotateLogListener.f", rotateLogListener.f);
            assertNotNull("rotateLogListener.p", rotateLogListener.p);
        }
    }

    @Test
    @Ignore ("test causes problems, closes stdout")
    public void testCloseLogFile() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        rotateLogListener.closeLogFile();
        assertNull("rotateLogListener.f", rotateLogListener.f);
    }

    @Test
    public void testConstructor() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        assertNotNull("rotateLogListener.p", rotateLogListener.p);
    }

    @Test
    public void testDestroy() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.destroy();
        assertNull("(DailyLogListener) dailyLogListener.f", ((DailyLogListener) dailyLogListener).f);
    }

    @Test
    @Ignore ("test causes problems, closes stdout")
    public void testLog() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.close();
        LogEvent ev = new LogEvent();
        LogEvent result = dailyLogListener.log(ev);
        assertSame("result", ev, result);
        assertNull("(DailyLogListener) dailyLogListener.p", ((DailyLogListener) dailyLogListener).p);
        assertEquals("(DailyLogListener) dailyLogListener.msgCount", 1, ((DailyLogListener) dailyLogListener).msgCount);
    }

    @Test
    @Ignore ("test causes problems, closes stdout")
    public void testLog1() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.close();
        LogEvent result = dailyLogListener.log(null);
        assertNull("result", result);
        assertNull("(DailyLogListener) dailyLogListener.p", ((DailyLogListener) dailyLogListener).p);
        assertEquals("(DailyLogListener) dailyLogListener.msgCount", 1, ((DailyLogListener) dailyLogListener).msgCount);
    }

    @Test
    public void testLogDebug() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.logDebug("testRotateLogListenerMsg");
        assertNotNull("(DailyLogListener) dailyLogListener.p", ((DailyLogListener) dailyLogListener).p);
    }

    @Test
    @Ignore ("test causes problems, closes stdout")
    public void testLogDebug1() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.close();
        dailyLogListener.logDebug("testRotateLogListenerMsg");
        assertNull("(DailyLogListener) dailyLogListener.p", ((DailyLogListener) dailyLogListener).p);
    }

    @Test
    @Ignore ("test causes problems, closes stdout")
    public void testLogRotateThrowsNullPointerException() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        try {
            rotateLogListener.logRotate();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("rotateLogListener.f", rotateLogListener.f);
            assertNull("rotateLogListener.p", rotateLogListener.p);
        }
    }

    @Test
    public void testOpenLogFileThrowsNullPointerException() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        try {
            dailyLogListener.openLogFile();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("(DailyLogListener) dailyLogListener.f", ((DailyLogListener) dailyLogListener).f);
            assertNotNull("(DailyLogListener) dailyLogListener.p", ((DailyLogListener) dailyLogListener).p);
        }
    }

    @Test
    public void testRotateConstructor() throws Throwable {
        new RotateLogListener().new Rotate();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        Configuration cfg = new SubConfiguration();
        try {
            rotateLogListener.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("rotateLogListener.logName", rotateLogListener.logName);
            assertEquals("rotateLogListener.sleepTime", 0L, rotateLogListener.sleepTime);
            assertEquals("rotateLogListener.maxSize", 0L, rotateLogListener.maxSize);
            assertEquals("rotateLogListener.maxCopies", 0, rotateLogListener.maxCopies);
            assertNull("rotateLogListener.rotate", rotateLogListener.rotate);
            assertNull("rotateLogListener.f", rotateLogListener.f);
            assertNotNull("rotateLogListener.p", rotateLogListener.p);
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
        assertFalse("Current log file should not contain the first message", currentLogFileContents.contains("Message 1"));
        assertTrue("Current log file should contain the second message", currentLogFileContents.contains("Message 2"));
        assertTrue("Logger element should have been opened in the current file", currentLogFileContents.contains("<logger "));
        assertFalse("Logger element should not have been closed in the current file", currentLogFileContents.contains("</logger>"));

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        assertTrue("Archived log file should contain the first message", archivedLogFile1Contents.contains("Message 1"));
        assertFalse("Archived log file should not contain the second message", archivedLogFile1Contents.contains("Message 2"));
        assertTrue("Logger element should have been opened in the archived file", archivedLogFile1Contents.contains("<logger "));
        assertTrue("Logger element should have been closed in the archived file", archivedLogFile1Contents.contains("</logger>"));

        // when: another rotation is executed
        listener.logRotate();

        // then: new events should end up in the current file and old events in the archived files
        listener.log(new LogEvent("Message 3"));

        currentLogFileContents = getStringFromFile(logRotationTestDirectory.getFile(logFileName));
        assertFalse("Current log file should not contain the first message", currentLogFileContents.contains("Message 1"));
        assertFalse("Current log file should not contain the second message", currentLogFileContents.contains("Message 2"));
        assertTrue("Current log file should contain the third message", currentLogFileContents.contains("Message 3"));
        assertTrue("Logger element should have been opened in the current file", currentLogFileContents.contains("<logger "));
        assertFalse("Logger element should not have been closed in the current file", currentLogFileContents.contains("</logger>"));

        archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        assertTrue("Archived log file should contain the second message", archivedLogFile1Contents.contains("Message 2"));
        assertFalse("Archived log file should not contain the third message", archivedLogFile1Contents.contains("Message 3"));
        assertTrue("Logger element should have been opened in the archived file", archivedLogFile1Contents.contains("<logger "));
        assertTrue("Logger element should have been closed in the archived file", archivedLogFile1Contents.contains("</logger>"));

        String archivedLogFile2Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".2"));
        assertTrue("Archived log file should contain the first message", archivedLogFile2Contents.contains("Message 1"));
        assertTrue("Logger element should have been opened in the archived file", archivedLogFile2Contents.contains("<logger "));
        assertTrue("Logger element should have been closed in the archived file", archivedLogFile2Contents.contains("</logger>"));

    }

    @Test
    @Ignore("This feature doesn't work in Windows so we reverted the patch c94ff02f2")
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
        assertTrue("Log file should contain first message", logFileContents.contains("Message 1"));
        assertTrue("Log file should contain second message", logFileContents.contains("Message 2"));
        assertFalse("Logger element should not have been closed", logFileContents.contains("</logger>"));

        File archiveFile = logRotationTestDirectory.getFile(logFileName + ".1");
        assertFalse("Archive file should not exist", archiveFile.exists());
    }

    @Test
    public void testNoFileNamePatternThenNoReplacement() throws ConfigurationException {
        String logFileName = "%s-important-log";
        Properties config = new Properties();
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, config);
        assertEquals(
                logRotationTestDirectory.getDirectory().getAbsolutePath() + "/%s-important-log",
                listener.logName);
    }

    @Test
    public void testEmptyFileNamePatternThenNoReplacement() throws ConfigurationException {
        String logFileName = "%s-important-log";
        Properties config = new Properties();
        config.setProperty("file-name-pattern", "");
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, config);
        assertEquals(
                logRotationTestDirectory.getDirectory().getAbsolutePath() + "/%s-important-log",
                listener.logName);
    }

    @Test
    public void testFileNamePatternHostNameReplacement() throws ConfigurationException {
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
                logRotationTestDirectory.getDirectory().getAbsolutePath() + "/" + hostname + "-important-log",
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

    private RotateLogListener createRotateLogListenerWithIsoDateFormat(String logFileName) throws ConfigurationException {
        return createRotateLogListenerWithIsoDateFormat(logFileName, null);
    }

    private RotateLogListener createRotateLogListenerWithIsoDateFormat(
            String logFileName,
            Properties customConfig) throws ConfigurationException {
        RotateLogListener listener = new RotateLogListener();
        Properties configuration = new Properties();
        configuration.setProperty("file", logRotationTestDirectory.getDirectory().getAbsolutePath() + "/" + logFileName);
        configuration.setProperty("copies", "10");
        configuration.setProperty("maxsize", "1000000");
        if (customConfig != null) {
            configuration.putAll(customConfig);
        }
        logRotationTestDirectory.allowNewFileCreation();
        listener.setConfiguration(new SimpleConfiguration(configuration));
        return listener;
    }

    @After
    public void cleanupLogRotateAbortsTestDir() {
        logRotationTestDirectory.delete();
    }
}
