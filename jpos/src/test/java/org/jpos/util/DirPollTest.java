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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jpos.core.Configuration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.Q2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DirPollTest {

    @Test
    public void testAccept() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.addPriority("testDirPollFileExtension");
        boolean result = dirPoll.accept(new File("testDirPollParam1"), "testDirPollName");
        assertFalse(result, "result");
    }

    @Test
    public void testAccept1() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.setPriorities("");
        boolean result = dirPoll.accept(new File("testDirPollParam1"), "testDirPollName");
        assertFalse(result, "result");
    }

    @Test
    public void testAcceptThrowsArrayIndexOutOfBoundsException() throws Throwable {
        try {
            new DirPoll().accept(new File("testDirPollParam1"), "testDirPollName");
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("0 >= 0", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testAcceptThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.addPriority("testDirPollFileExtension");
        try {
            dirPoll.accept(new File("testDirPollParam1"), null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.endsWith(String)\" because \"name\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        assertNull(dirPoll.logger, "dirPoll.logger");
        assertEquals(".", dirPoll.getPath(), "dirPoll.getPath()");
        assertNull(dirPoll.realm, "dirPoll.realm");
        assertEquals(1000L, dirPoll.getPollInterval(), "dirPoll.getPollInterval()");
        assertFalse(dirPoll.isPaused(), "dirPoll.isPaused()");
    }

    @Test
    public void testDirPollExceptionConstructor() throws Throwable {
        Exception nested = new DirPoll.DirPollException("testDirPollExceptionDetail");
        DirPoll.DirPollException dirPollException = new DirPoll.DirPollException(nested);
        assertEquals("org.jpos.util.DirPoll$DirPollException: testDirPollExceptionDetail",
                dirPollException.getMessage(), "dirPollException.getMessage()");
        assertSame(nested, dirPollException.getNested(), "dirPollException.getNested()");
    }

    @Test
    public void testDirPollExceptionConstructor1() throws Throwable {
        DirPoll.DirPollException dirPollException = new DirPoll.DirPollException();
        assertNull(dirPollException.getNested(), "dirPollException.getNested()");
    }

    @Test
    public void testDirPollExceptionConstructor2() throws Throwable {
        Exception nested = new NumberFormatException();
        DirPoll.DirPollException dirPollException = new DirPoll.DirPollException("testDirPollExceptionDetail", nested);
        assertEquals("testDirPollExceptionDetail", dirPollException.getMessage(), "dirPollException.getMessage()");
        assertSame(nested, dirPollException.getNested(), "dirPollException.getNested()");
    }

    @Test
    public void testDirPollExceptionConstructor3() throws Throwable {
        DirPoll.DirPollException dirPollException = new DirPoll.DirPollException("testDirPollExceptionDetail");
        assertEquals("testDirPollExceptionDetail", dirPollException.getMessage(), "dirPollException.getMessage()");
        assertNull(dirPollException.getNested(), "dirPollException.getNested()");
    }

    @Test
    public void testDirPollExceptionConstructorThrowsNullPointerException() throws Throwable {
        try {
            new DirPoll.DirPollException((Exception) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.lang.Throwable.toString()\" because \"nested\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testIsPaused() throws Throwable {
        boolean result = new DirPoll().isPaused();
        assertFalse(result, "result");
    }

    @Test
    public void testIsPaused1() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.pause();
        boolean result = dirPoll.isPaused();
        assertTrue(result, "result");
    }

    @Test
    public void testPause() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.pause();
        assertTrue(dirPoll.isPaused(), "dirPoll.isPaused()");
    }

    @Test
    public void testPause1() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.pause();
        dirPoll.pause();
        assertTrue(dirPoll.isPaused(), "dirPoll.isPaused()");
    }

    @Test
    public void testProcessorRunnerConstructorThrowsNullPointerException() throws Throwable {
        try {
            new DirPoll().new ProcessorRunner(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.File.getName()\" because \"f\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSetArchiveDirThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setArchiveDir(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testSetBadDirThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setBadDir(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testSetConfigurationNull() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.setProcessor("");
        dirPoll.setConfiguration(null);
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException1() throws Throwable {
        Configuration cfg = new SubConfiguration();
        DirPoll processor = new DirPoll();
        DirPoll dirPoll = new DirPoll();
        processor.setProcessor("");
        dirPoll.setProcessor(processor);
        try {
            dirPoll.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.get(String, String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException2() throws Throwable {
        Configuration cfg = new SubConfiguration();
        DirPoll dirPoll = new DirPoll();
        dirPoll.setProcessor("");
        try {
            dirPoll.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.get(String, String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException3() throws Throwable {
        Configuration cfg = new SubConfiguration();
        DirPoll dirPoll = new DirPoll();
        dirPoll.setProcessor(new DirPoll());
        try {
            dirPoll.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.get(String, String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException4() throws Throwable {
        Configuration cfg = new SubConfiguration();
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.get(String, String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSetConfigurationThrowsStackOverflowError() throws Throwable {
        Configuration cfg = new SubConfiguration();
        DirPoll processor = new DirPoll();
        processor.setProcessor(processor);
        try {
            processor.setConfiguration(cfg);
            fail("Expected StackOverflowError to be thrown");
        } catch (StackOverflowError ex) {
            assertEquals(StackOverflowError.class, ex.getClass(), "ex.getClass()");
        }
    }

    @Test
    public void testSetPath() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.setPath("testDirPollBase");
        assertEquals("testDirPollBase", dirPoll.getPath(), "dirPoll.getPath()");
    }

    @Test
    public void testSetPollInterval() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.setPollInterval(100L);
        assertEquals(100L, dirPoll.getPollInterval(), "dirPoll.getPollInterval()");
    }

    @Test
    public void testSetPrioritiesThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setPriorities(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"str\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSetRequestDirThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setRequestDir(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testSetResponseDirThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setResponseDir(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testSetRunDirThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setRunDir(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testSetTmpDirThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setTmpDir(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testUnpause() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.unpause();
        dirPoll.unpause();
        assertFalse(dirPoll.isPaused(), "dirPoll.isPaused()");
    }

    @Test
    public void testUnpause1() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.pause();
        dirPoll.unpause();
        assertFalse(dirPoll.isPaused(), "dirPoll.isPaused()");
    }

    @Test
    public void testRetry(@TempDir Path deployDir) throws IOException {
        Files.copy(Paths.get("build/resources/test/org/jpos/util/dirpoll_retry"), deployDir, REPLACE_EXISTING);
        Q2 q2 = new Q2(deployDir.resolve("deploy").toString());
        q2.start();
        ISOUtil.sleep(5000L);
        createTestFile(deployDir.resolve("request/REQ1"), "RETRYME");
        ISOUtil.sleep(5000L);
        q2.stop();
        ISOUtil.sleep(2000L);
        assertTrue(Files.isReadable(deployDir.resolve("request/REQ1")), "Can't read request");
    }
    public static class RetryTest implements DirPoll.Processor {
        public byte[] process(String name, byte[] request) throws DirPoll.DirPollException {
            if ("RETRYME".equals(new String(request))) {
                DirPoll.DirPollException dpe = new DirPoll.DirPollException("Retrying");
                dpe.setRetry(true);
                throw dpe;
            }
            return new byte[0];
        }
    }
    private void createTestFile (Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        AsynchronousFileChannel out = AsynchronousFileChannel.open(path, WRITE, CREATE);
        out.write(ByteBuffer.wrap(content.getBytes()), 0);
        out.close();
    }
}
