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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jpos.core.Configuration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.Q2;
import org.jpos.util.DirPoll.DirPollException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DirPollTest {

    Throwable thrown;

    Configuration cfg;

    DirPoll dirPoll;

    @BeforeEach
    void setUp() {
        dirPoll = new DirPoll();
        cfg = new SubConfiguration();
    }

    @Test
    void testAccept() {
        dirPoll.addPriority("testDirPollFileExtension");
        boolean result = dirPoll.accept(new File("testDirPollParam1"), "testDirPollName");
        assertFalse(result, "result");
    }

    @Test
    void testAccept1() {
        dirPoll.setPriorities("");
        boolean result = dirPoll.accept(new File("testDirPollParam1"), "testDirPollName");
        assertFalse(result, "result");
    }

    @Test
    void testAcceptThrowsArrayIndexOutOfBoundsException() {
        thrown = assertThrows(ArrayIndexOutOfBoundsException.class,
            () -> dirPoll.accept(new File("testDirPollParam1"), "testDirPollName")
        );
        assertNotNull(thrown.getMessage());
    }

    @Test
    void testAcceptThrowsNullPointerException() {
        dirPoll.addPriority("testDirPollFileExtension");

        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.accept(new File("testDirPollParam1"), null)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testConstructor() {
        assertNull(dirPoll.logger, "dirPoll.logger");
        assertEquals(".", dirPoll.getPath(), "dirPoll.getPath()");
        assertNull(dirPoll.realm, "dirPoll.realm");
        assertEquals(1000L, dirPoll.getPollInterval(), "dirPoll.getPollInterval()");
        assertFalse(dirPoll.isPaused(), "dirPoll.isPaused()");
    }

    @Test
    void testDirPollExceptionConstructor() {
        Exception nested = new DirPollException("testDirPollExceptionDetail");
        DirPollException dirPollException = new DirPollException(nested);
        assertEquals("org.jpos.util.DirPoll$DirPollException: testDirPollExceptionDetail",
                dirPollException.getMessage(), "dirPollException.getMessage()");
        assertSame(nested, dirPollException.getNested(), "dirPollException.getNested()");
    }

    @Test
    void testDirPollExceptionConstructor1() {
        DirPollException dirPollException = new DirPollException();
        assertNull(dirPollException.getNested(), "dirPollException.getNested()");
    }

    @Test
    void testDirPollExceptionConstructor2() {
        Exception nested = new NumberFormatException();
        DirPollException dirPollException = new DirPollException("testDirPollExceptionDetail", nested);
        assertEquals("testDirPollExceptionDetail", dirPollException.getMessage(), "dirPollException.getMessage()");
        assertSame(nested, dirPollException.getNested(), "dirPollException.getNested()");
    }

    @Test
    void testDirPollExceptionConstructor3() {
        DirPollException dirPollException = new DirPollException("testDirPollExceptionDetail");
        assertEquals("testDirPollExceptionDetail", dirPollException.getMessage(), "dirPollException.getMessage()");
        assertNull(dirPollException.getNested(), "dirPollException.getNested()");
    }

    @Test
    void testDirPollExceptionConstructorThrowsNullPointerException() {
        thrown = assertThrows(NullPointerException.class,
            () -> new DirPollException((Exception) null)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testIsPaused() {
        boolean result = dirPoll.isPaused();
        assertFalse(result, "result");
    }

    @Test
    void testIsPaused1() {
        dirPoll.pause();
        boolean result = dirPoll.isPaused();
        assertTrue(result, "result");
    }

    @Test
    void testPause() {
        dirPoll.pause();
        assertTrue(dirPoll.isPaused(), "dirPoll.isPaused()");
    }

    @Test
    void testPause1() {
        dirPoll.pause();
        dirPoll.pause();
        assertTrue(dirPoll.isPaused(), "dirPoll.isPaused()");
    }

    @Test
    void testProcessorRunnerConstructorThrowsNullPointerException() {
        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.new ProcessorRunner(null)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testSetArchiveDirThrowsNullPointerException() {
        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.setArchiveDir(null)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testSetBadDirThrowsNullPointerException() {
        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.setBadDir(null)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    public void testSetConfigurationNull() throws Throwable {
        dirPoll.setProcessor("");
        dirPoll.setConfiguration(null);
    }

    @Test
    void testSetConfigurationThrowsNullPointerException1() {
        DirPoll processor = new DirPoll();
        processor.setProcessor("");
        dirPoll.setProcessor(processor);

        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.setConfiguration(cfg)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testSetConfigurationThrowsNullPointerException2() {
        dirPoll.setProcessor("");

        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.setConfiguration(cfg)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testSetConfigurationThrowsNullPointerException3() {
        dirPoll.setProcessor(new DirPoll());

        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.setConfiguration(cfg)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testSetConfigurationThrowsNullPointerException4() {
        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.setConfiguration(cfg)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testSetConfigurationThrowsStackOverflowError() {
        dirPoll.setProcessor(dirPoll);

        thrown = assertThrows(StackOverflowError.class,
            () -> dirPoll.setConfiguration(cfg)
        );
    }

    @Test
    void testSetPath() {
        dirPoll.setPath("testDirPollBase");
        assertEquals("testDirPollBase", dirPoll.getPath(), "dirPoll.getPath()");
    }

    @Test
    void testSetPollInterval() {
        dirPoll.setPollInterval(100L);
        assertEquals(100L, dirPoll.getPollInterval(), "dirPoll.getPollInterval()");
    }

    @Test
    void testSetPrioritiesThrowsNullPointerException() {
        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.setPriorities(null)
        );
    }

    @Test
    void testSetRequestDirThrowsNullPointerException() {
        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.setRequestDir(null)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testSetResponseDirThrowsNullPointerException() {
        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.setResponseDir(null)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testSetRunDirThrowsNullPointerException() {
        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.setRunDir(null)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testSetTmpDirThrowsNullPointerException() {
        thrown = assertThrows(NullPointerException.class,
            () -> dirPoll.setTmpDir(null)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testUnpause() {
        dirPoll.unpause();
        dirPoll.unpause();
        assertFalse(dirPoll.isPaused(), "dirPoll.isPaused()");
    }

    @Test
    void testUnpause1() {
        dirPoll.pause();
        dirPoll.unpause();
        assertFalse(dirPoll.isPaused(), "dirPoll.isPaused()");
    }

    @Test
    void testRetry() {
        Q2 q2 = new Q2("build/resources/test/org/jpos/util/dirpoll_retry/deploy");
        q2.start();
        ISOUtil.sleep(5000L);
        createTestFile("build/resources/test/org/jpos/util/dirpoll_retry/request/REQ1", "RETRYME");
        ISOUtil.sleep(5000L);
        q2.stop();
        ISOUtil.sleep(2000L);
        assertTrue(new File("build/resources/test/org/jpos/util/dirpoll_retry/request/REQ1").canRead(), "Can't read request");
    }

    static class RetryTest implements DirPoll.Processor {
        public byte[] process(String name, byte[] request) throws DirPollException {
            if ("RETRYME".equals(new String(request))) {
                DirPollException dpe = new DirPollException("Retrying");
                dpe.setRetry(true);
                throw dpe;
            }
            return new byte[0];
        }
    }

    private void createTestFile(String path, String content) {
        File tmp = new File(path);
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            out.write(content.getBytes());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
