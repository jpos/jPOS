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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jpos.util.NameRegistrar.NotFoundException;
import org.junit.Test;

public class NameRegistrarConcurrencyTest {
    static final int TOTAL_THREADS_TO_RUN = 1000;

    @Test
    public void testConcurrency() throws Exception {
        List<Runnable> parrallelTasksList = new ArrayList<Runnable>(TOTAL_THREADS_TO_RUN);
        for (int i = 0; i < TOTAL_THREADS_TO_RUN; i++) {
            final int counter = i;
            parrallelTasksList.add(new Runnable() {
                public void run() {
                    String key = "testKey" + counter;
                    String value = "testValue" + counter;
                    
                    assertThat(NameRegistrar.getIfExists(key), is(nullValue()));
                    NameRegistrar.register(key, value);
                    try {
                        String actualValue = (String) NameRegistrar.get(key);
                        assertThat(actualValue, is(value));
                    } catch (NotFoundException e) {
                        fail("key not found: " + key);
                    }
                    NameRegistrar.unregister(key);
                    // Uncomment the sysout below to show that test were not
                    // completed in order, the numbers should be interleaved
                    // (not an ordered list) to hopefully show
                    // the threads had the opportunity to step on each other;
                    // i.e. thread safety of operations (and not just of Sysout!).
                    // If it were to run too fast, can insert a Thread.sleep
                    // part way through - say, after the register step above for
                    // 200 milliseconds.
                    //
                    // System.out.println("done: "+ key);
                }
            });
        }
        int maxTimeoutSeconds = 5;
        assertConcurrent("Remove/Get/Add of NameRegistrar items must be ThreadSafe", parrallelTasksList, maxTimeoutSeconds);
    }

    public static void assertConcurrent(final String message, final List<? extends Runnable> runnables, final int maxTimeoutSeconds)
            throws InterruptedException {
        final int numThreads = runnables.size();
        final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        try {
            final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
            final CountDownLatch afterInitBlocker = new CountDownLatch(1);
            final CountDownLatch allDone = new CountDownLatch(numThreads);
            for (final Runnable submittedTestRunnable : runnables) {
                threadPool.submit(new Runnable() {
                    public void run() {
                        allExecutorThreadsReady.countDown();
                        try {
                            afterInitBlocker.await();
                            submittedTestRunnable.run();
                        } catch (final Throwable e) {
                            exceptions.add(e);
                        } finally {
                            allDone.countDown();
                        }
                    }
                });
            }
            // wait until all threads are ready
            assertTrue(
                    "Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent",
                    allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
            // start all test runners
            afterInitBlocker.countDown();
            assertTrue(message + " timeout! More than" + maxTimeoutSeconds + "seconds",
                    allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
        } finally {
            threadPool.shutdownNow();
        }
        assertTrue(message + "failed with exception(s)" + exceptions, exceptions.isEmpty());
    }
}
