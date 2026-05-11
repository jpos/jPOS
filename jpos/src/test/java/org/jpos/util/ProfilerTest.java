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

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

public class ProfilerTest {

    @Test
    public void testCheckPoint() throws Throwable {
        Profiler profiler = new Profiler();
        profiler.checkPoint("testProfilerDetail1");

    }

    @Test
    public void testCheckPointNull() throws Throwable {
        Profiler profiler = new Profiler();
        profiler.checkPoint(null);
        assertEquals(1, profiler.events.size(), "profiler.events.size()");

    }

    @Test
    public void testConstructor() throws Throwable {
        Profiler profiler = new Profiler();
        assertEquals(0, profiler.events.size(), "profiler.events.size()");
    }

    @Test
    public void testDump() throws Throwable {
        Profiler profiler = new Profiler();
        profiler.dump(new PrintStream(new ByteArrayOutputStream()), "testProfilerIndent");
        assertEquals(1, profiler.events.size(), "profiler.events.size()");
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        Profiler profiler = new Profiler();
        try {
            profiler.dump(null, "testProfilerIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals(1, profiler.events.size(), "profiler.events.size()");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.PrintStream.println(String)\" because \"p\" is null",
                        ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testDumpWhileProfilingDoesNotThrowConcurrentModificationException() throws Throwable {
        Profiler profiler = new Profiler();
        Profiler.Entry mutatingEntry = new Profiler.Entry() {
            @Override
            public String toString() {
                profiler.checkPoint("mutated");
                return super.toString();
            }
        };
        mutatingEntry.setEventName("mutating-entry");
        profiler.events.put("mutating-entry", mutatingEntry);
        profiler.checkPoint("second-entry");

        profiler.dump(new PrintStream(new ByteArrayOutputStream()), "testProfilerIndent");
    }

    /**
     * Verifies that concurrent calls to {@link Profiler#dump} and
     * {@link Profiler#checkPoint}
     * do not cause a {@link java.util.ConcurrentModificationException}.
     *
     * <p>
     * This test simulates the real-world scenario where one thread is
     * logging/dumping
     * profiler results while other threads are simultaneously recording
     * checkpoints.
     * The race condition occurs when dump() iterates over the events map while
     * checkPoint() modifies it from another thread.
     * </p>
     *
     * <p>
     * Test strategy: spawn multiple writer threads calling checkPoint()
     * concurrently
     * with a dumper thread calling dump(). Both use CountDownLatch to start
     * simultaneously,
     * maximizing the chance of interleaving. Thread.yield() calls increase
     * contention
     * by giving other threads opportunity to acquire the profiler's lock.
     * </p>
     *
     * @throws Throwable if any thread encounters an exception during execution
     */
    @Test
    public void testConcurrentDumpAndCheckpoint() throws Throwable {
        Profiler profiler = new Profiler();

        // Test configuration: 4 writer threads each performing 2000 checkpoint
        // operations
        // with a single dumper thread performing 2000 dump operations.
        // This creates significant contention on the profiler's internal lock.
        final int threads = 4;
        final int iterations = 2000;

        // startLatch ensures all threads begin simultaneously, maximizing race
        // conditions
        final CountDownLatch startLatch = new CountDownLatch(1);

        // failed flag captured by all threads; any exception sets it to true
        final AtomicBoolean failed = new AtomicBoolean(false);

        // Spawn writer threads that continuously call checkPoint()
        final Thread[] writers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int threadNum = i;
            writers[i] = new Thread(() -> {
                try {
                    // Wait for start signal before beginning
                    startLatch.await();

                    for (int j = 0; j < iterations; j++) {
                        // Each thread records checkpoints with unique names to avoid lock
                        // contention on the same key, but still modifies the shared events map
                        profiler.checkPoint("t" + threadNum + "i" + j);

                        // Yield every 10 iterations to increase chance of lock contention
                        // between threads, improving the likelihood of catching the race
                        if (j % 10 == 0) {
                            Thread.yield();
                        }
                    }
                } catch (Exception e) {
                    // Capture any exception (including ConcurrentModificationException)
                    failed.set(true);
                }
            });
            writers[i].start();
        }

        // Spawn dumper thread that continuously calls dump()
        final Thread dumper = new Thread(() -> {
            try {
                // Wait for start signal before beginning
                startLatch.await();

                for (int j = 0; j < iterations; j++) {
                    // dump() iterates over the events map while writers modify it,
                    // triggering ConcurrentModificationException if not properly synchronized
                    profiler.dump(new PrintStream(new ByteArrayOutputStream()), "");

                    // Yield every 10 iterations to increase contention with writers
                    if (j % 10 == 0) {
                        Thread.yield();
                    }
                }
            } catch (Exception e) {
                // Capture any exception (including ConcurrentModificationException)
                failed.set(true);
            }
        });
        dumper.start();

        // Start all threads at the same moment
        startLatch.countDown();

        // Wait for dumper to finish first, then writers
        dumper.join();
        for (Thread t : writers) {
            t.join();
        }

        // Assert no thread encountered an exception
        assertTrue(!failed.get(),
                "Concurrent modification or other exception should not occur");
    }

    @Test
    public void testGetPartial() throws Throwable {
        new Profiler().getPartial();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testReset() throws Throwable {
        Profiler profiler = new Profiler();
        profiler.reset();
        assertEquals(0, profiler.events.size(), "profiler.events.size()");
    }
}
