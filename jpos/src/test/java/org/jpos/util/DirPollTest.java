/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jpos.core.Configuration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.Q2;
import org.junit.Test;

public class DirPollTest {

    @Test
    public void testAccept() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.addPriority("testDirPollFileExtension");
        boolean result = dirPoll.accept(new File("testDirPollParam1"), "testDirPollName");
        assertFalse("result", result);
    }

    @Test
    public void testAccept1() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.setPriorities("");
        boolean result = dirPoll.accept(new File("testDirPollParam1"), "testDirPollName");
        assertFalse("result", result);
    }

    @Test
    public void testAcceptThrowsArrayIndexOutOfBoundsException() throws Throwable {
        try {
            new DirPoll().accept(new File("testDirPollParam1"), "testDirPollName");
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0 >= 0", ex.getMessage());
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
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        assertNull("dirPoll.logger", dirPoll.logger);
        assertEquals("dirPoll.getPath()", ".", dirPoll.getPath());
        assertNull("dirPoll.realm", dirPoll.realm);
        assertEquals("dirPoll.getPollInterval()", 1000L, dirPoll.getPollInterval());
        assertFalse("dirPoll.isPaused()", dirPoll.isPaused());
    }

    @Test
    public void testDirPollExceptionConstructor() throws Throwable {
        Exception nested = new DirPoll.DirPollException("testDirPollExceptionDetail");
        DirPoll.DirPollException dirPollException = new DirPoll.DirPollException(nested);
        assertEquals("dirPollException.getMessage()", "org.jpos.util.DirPoll$DirPollException: testDirPollExceptionDetail",
                dirPollException.getMessage());
        assertSame("dirPollException.getNested()", nested, dirPollException.getNested());
    }

    @Test
    public void testDirPollExceptionConstructor1() throws Throwable {
        DirPoll.DirPollException dirPollException = new DirPoll.DirPollException();
        assertNull("dirPollException.getNested()", dirPollException.getNested());
    }

    @Test
    public void testDirPollExceptionConstructor2() throws Throwable {
        Exception nested = new NumberFormatException();
        DirPoll.DirPollException dirPollException = new DirPoll.DirPollException("testDirPollExceptionDetail", nested);
        assertEquals("dirPollException.getMessage()", "testDirPollExceptionDetail", dirPollException.getMessage());
        assertSame("dirPollException.getNested()", nested, dirPollException.getNested());
    }

    @Test
    public void testDirPollExceptionConstructor3() throws Throwable {
        DirPoll.DirPollException dirPollException = new DirPoll.DirPollException("testDirPollExceptionDetail");
        assertEquals("dirPollException.getMessage()", "testDirPollExceptionDetail", dirPollException.getMessage());
        assertNull("dirPollException.getNested()", dirPollException.getNested());
    }

    @Test
    public void testDirPollExceptionConstructorThrowsNullPointerException() throws Throwable {
        try {
            new DirPoll.DirPollException((Exception) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testIsPaused() throws Throwable {
        boolean result = new DirPoll().isPaused();
        assertFalse("result", result);
    }

    @Test
    public void testIsPaused1() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.pause();
        boolean result = dirPoll.isPaused();
        assertTrue("result", result);
    }

    @Test
    public void testPause() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.pause();
        assertTrue("dirPoll.isPaused()", dirPoll.isPaused());
    }

    @Test
    public void testPause1() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.pause();
        dirPoll.pause();
        assertTrue("dirPoll.isPaused()", dirPoll.isPaused());
    }

    @Test
    public void testProcessorRunnerConstructorThrowsNullPointerException() throws Throwable {
        try {
            new DirPoll().new ProcessorRunner(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetArchiveDirThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setArchiveDir(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetBadDirThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setBadDir(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
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
            assertNull("ex.getMessage()", ex.getMessage());
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
            assertNull("ex.getMessage()", ex.getMessage());
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
            assertNull("ex.getMessage()", ex.getMessage());
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
            assertNull("ex.getMessage()", ex.getMessage());
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
            assertEquals("ex.getClass()", StackOverflowError.class, ex.getClass());
        }
    }

    @Test
    public void testSetPath() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.setPath("testDirPollBase");
        assertEquals("dirPoll.getPath()", "testDirPollBase", dirPoll.getPath());
    }

    @Test
    public void testSetPollInterval() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.setPollInterval(100L);
        assertEquals("dirPoll.getPollInterval()", 100L, dirPoll.getPollInterval());
    }

    @Test
    public void testSetPrioritiesThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setPriorities(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetRequestDirThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setRequestDir(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetResponseDirThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setResponseDir(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetRunDirThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setRunDir(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetTmpDirThrowsNullPointerException() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        try {
            dirPoll.setTmpDir(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnpause() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.unpause();
        dirPoll.unpause();
        assertFalse("dirPoll.isPaused()", dirPoll.isPaused());
    }

    @Test
    public void testUnpause1() throws Throwable {
        DirPoll dirPoll = new DirPoll();
        dirPoll.pause();
        dirPoll.unpause();
        assertFalse("dirPoll.isPaused()", dirPoll.isPaused());
    }

    @Test
    public void testRetry() throws IOException {
        Q2 q2 = new Q2("build/resources/test/org/jpos/util/dirpoll_retry/deploy");
        q2.start();
        ISOUtil.sleep(5000L);
        createTestFile("build/resources/test/org/jpos/util/dirpoll_retry/request/REQ1", "RETRYME");
        ISOUtil.sleep(5000L);
        q2.stop();
        ISOUtil.sleep(2000L);
        assertTrue("Can't read request", new File("build/resources/test/org/jpos/util/dirpoll_retry/request/REQ1").canRead());
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
    private void createTestFile (String path, String content) throws IOException {
        File tmp = new File(path);
        FileOutputStream out = new FileOutputStream(tmp);
        out.write(content.getBytes());
        out.close();
    }
}
