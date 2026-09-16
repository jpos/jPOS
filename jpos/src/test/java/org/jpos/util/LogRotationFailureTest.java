/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

import org.jdom2.Element;
import org.jpos.core.SimpleConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class LogRotationFailureTest {
    @TempDir Path directory;

    private <T extends RotateLogListener> T configure(T listener) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("file", directory.resolve("active.log").toString());
        properties.setProperty("prefix", directory.resolve("active").toString());
        properties.setProperty("copies", "3");
        listener.setConfiguration(new SimpleConfiguration(properties));
        listener.timer = () -> { }; // no timer or asynchronous compression in these tests
        listener.setConfiguration((Element) null);
        if (listener instanceof DailyLogListener daily)
            daily.setLastDate("-archive");
        listener.log(new LogEvent("before-rotation"));
        return listener;
    }

    private void assertResumed(RotateLogListener listener) throws Exception {
        listener.log(new LogEvent("after-failure"));
        String content = Files.readString(Path.of(listener.logName));
        assertTrue(content.contains("before-rotation"));
        assertTrue(content.contains("after-failure"));
    }

    @Test void failedReservationDoesNotDeleteAnExistingArchive() throws Exception {
        Path archive = directory.resolve("active-archive.log");
        Files.writeString(archive, "existing archive");
        DailyLogListener listener = configure(new DailyLogListener() {
            @Override void reserveFile(Path destination) throws IOException {
                throw new FileSystemException(destination.toString(), null, "Too many open files");
            }
            @Override protected void compress(File file) { fail("must not compress after failure"); }
        });
        try {
            assertThrows(FileSystemException.class, listener::logRotate);
            assertEquals("existing archive", Files.readString(archive));
            assertResumed(listener);
        } finally { listener.destroy(); }
    }

    @Test void failedMoveCleansReservationAndDoesNotRetryAsCollision() throws Exception {
        List<Path> attempts = new ArrayList<>();
        DailyLogListener listener = configure(new DailyLogListener() {
            @Override void moveFile(Path source, Path destination) throws IOException {
                attempts.add(destination);
                throw new FileAlreadyExistsException(destination.toString());
            }
            @Override protected void compress(File file) { fail("must not compress after failure"); }
        });
        try {
            assertThrows(FileAlreadyExistsException.class, listener::logRotate);
            assertEquals(List.of(directory.resolve("active-archive.log")), attempts);
            assertFalse(Files.exists(attempts.getFirst()));
            assertResumed(listener);
        } finally { listener.destroy(); }
    }

    @Test void collisionSkipsExistingArchive() throws Exception {
        Path archive = directory.resolve("active-archive.log");
        Files.writeString(archive, "previous");
        DailyLogListener listener = configure(new DailyLogListener());
        try {
            listener.logRotate();
            assertEquals("previous", Files.readString(archive));
            assertTrue(Files.readString(directory.resolve("active-archive.1.log")).contains("before-rotation"));
        } finally { listener.destroy(); }
    }

    @Test void numberedRotationStopsAtFailedIntermediateMove() throws Exception {
        RotateLogListener listener = configure(new RotateLogListener());
        Path first = directory.resolve("active.log.1");
        Path second = directory.resolve("active.log.2");
        Path third = directory.resolve("active.log.3");
        Files.writeString(first, "first archive");
        Files.writeString(second, "second archive");
        Files.createDirectory(third);
        Files.writeString(third.resolve("obstruction"), "keep");
        try {
            assertThrows(IOException.class, listener::logRotate);
            assertEquals("first archive", Files.readString(first));
            assertEquals("second archive", Files.readString(second));
            assertEquals("keep", Files.readString(third.resolve("obstruction")));
            assertResumed(listener);
        } finally { listener.destroy(); }
    }

    @Test void numberedRotationRetainsCompletedMovesAndStopsBeforeClobberingFailedSource() throws Exception {
        RotateLogListener listener = configure(new RotateLogListener() {
            @Override void moveFile(Path source, Path destination) throws IOException {
                if (source.endsWith("active.log.1"))
                    throw new IOException("injected intermediate failure");
                super.moveFile(source, destination);
            }
        });
        Files.writeString(directory.resolve("active.log.1"), "first archive");
        Files.writeString(directory.resolve("active.log.2"), "second archive");
        try {
            assertThrows(IOException.class, listener::logRotate);
            assertEquals("first archive", Files.readString(directory.resolve("active.log.1")));
            assertEquals("second archive", Files.readString(directory.resolve("active.log.3")));
            assertFalse(Files.exists(directory.resolve("active.log.2")));
            assertResumed(listener);
        } finally { listener.destroy(); }
    }

    @Test void missingNumberedSlotsAreNotFailures() throws Exception {
        RotateLogListener listener = configure(new RotateLogListener());
        try {
            listener.logRotate();
            assertTrue(Files.readString(directory.resolve("active.log.1")).contains("before-rotation"));
        } finally { listener.destroy(); }
    }

    @Test void missingDestinationDirectoryIsNotAMissingSource() throws Exception {
        RotateLogListener listener = configure(new RotateLogListener() {
            @Override void moveFile(Path source, Path destination) throws IOException {
                if (Files.exists(source))
                    throw new NoSuchFileException(destination.toString());
                super.moveFile(source, destination);
            }
        });
        try {
            assertThrows(NoSuchFileException.class, listener::logRotate);
            assertResumed(listener);
        } finally { listener.destroy(); }
    }

    @Test void reopenFailureDoesNotHideRotationFailure() throws Exception {
        RotateLogListener listener = new RotateLogListener() {
            @Override protected synchronized void openLogFile() throws IOException {
                throw new IOException("reopen");
            }
        };
        IOException failure = new IOException("rotation");
        listener.rotationAlgo = () -> { throw failure; };
        IOException actual = assertThrows(IOException.class, () -> listener.logRotate(true));
        assertSame(failure, actual);
        assertEquals("reopen", actual.getSuppressed()[0].getMessage());
    }

    private static class CompressionListener extends DailyLogListener {
        final List<Throwable> errors = new ArrayList<>();
        @Override protected void logDebugEx(String message, Throwable error) { errors.add(error); }
    }

    private void assertCompressionFailurePreservesSource(CompressionListener listener) throws Exception {
        Path archive = directory.resolve("archive.log.gz");
        Files.writeString(archive, "complete raw archive");
        listener.new Compressor(archive.toFile()).run();
        assertEquals("complete raw archive", Files.readString(archive));
        assertFalse(listener.errors.isEmpty(), "failure must be reported");
        try (var files = Files.list(directory)) {
            assertEquals(List.of(archive), files.toList(), "temporary output should be cleaned up");
        }
    }

    @Test void compressionOpenFailurePreservesSource() throws Exception {
        assertCompressionFailurePreservesSource(new CompressionListener() {
            @Override protected OutputStream getCompressedOutputStream(File file) throws IOException {
                throw new IOException("open failure");
            }
        });
    }

    @Test void compressionWriteFailurePreservesSourceAndClosesStream() throws Exception {
        boolean[] closed = { false };
        assertCompressionFailurePreservesSource(new CompressionListener() {
            @Override protected OutputStream getCompressedOutputStream(File file) throws IOException {
                return new FilterOutputStream(new FileOutputStream(file)) {
                    @Override public void write(byte[] data, int offset, int length) throws IOException {
                        out.write(data, offset, 1);
                        throw new IOException("partial write");
                    }
                    @Override public void close() throws IOException { closed[0] = true; super.close(); }
                };
            }
        });
        assertTrue(closed[0]);
    }

    @Test void compressionFinishFailurePreservesSourceAndClosesStream() throws Exception {
        boolean[] closed = { false };
        assertCompressionFailurePreservesSource(new CompressionListener() {
            @Override protected OutputStream getCompressedOutputStream(File file) throws IOException {
                return new FileOutputStream(file) {
                    @Override public void close() throws IOException { closed[0] = true; super.close(); }
                };
            }
            @Override protected void closeCompressedOutputStream(OutputStream stream) throws IOException {
                throw new IOException("finish failure");
            }
        });
        assertTrue(closed[0]);
    }

    @Test void compressionCloseFailurePreservesSource() throws Exception {
        assertCompressionFailurePreservesSource(new CompressionListener() {
            @Override protected OutputStream getCompressedOutputStream(File file) throws IOException {
                return new FileOutputStream(file) {
                    @Override public void close() throws IOException {
                        super.close();
                        throw new IOException("close failure");
                    }
                };
            }
        });
    }

    @Test void compressionPublicationFailurePreservesSource() throws Exception {
        assertCompressionFailurePreservesSource(new CompressionListener() {
            @Override void moveFile(Path source, Path destination) throws IOException {
                throw new AtomicMoveNotSupportedException(source.toString(), destination.toString(), "unsupported");
            }
        });
    }

    @Test void gzipAndZipPublishCompleteReadableArchives() throws Exception {
        for (int format : new int[] {1, 2}) {
            CompressionListener listener = new CompressionListener();
            listener.logName = "active.log";
            listener.setCompressionFormat(format);
            listener.setCompressionBufferSize(0); // must not silently produce an empty archive
            Path archive = directory.resolve("archive-" + format);
            Files.writeString(archive, "complete raw archive");
            listener.new Compressor(archive.toFile()).run();
            assertTrue(listener.errors.isEmpty());
            try (InputStream input = format == 1
                    ? new GZIPInputStream(Files.newInputStream(archive))
                    : new ZipInputStream(Files.newInputStream(archive))) {
                if (input instanceof ZipInputStream zip)
                    assertEquals("active.log", zip.getNextEntry().getName());
                assertEquals("complete raw archive", new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            }
        }
    }
}
