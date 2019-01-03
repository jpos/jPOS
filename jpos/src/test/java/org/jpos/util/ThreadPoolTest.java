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

import org.jpos.iso.ISOUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.internal.matchers.Matches;

public class ThreadPoolTest {

    static class TestTask implements Runnable {
      public void run() {
        ISOUtil.sleep(500);
      }
    }

    @Test
    public void testConstructor() throws Throwable {
        ThreadPool threadPool = new ThreadPool(-1, 1);
        assertEquals("threadPool.getJobCount()", 0, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 1, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 1, threadPool.getMaxPoolSize());
    }

    @Test
    public void testConstructor1() throws Throwable {
        ThreadPool threadPool = new ThreadPool(-2, -1);
        assertEquals("threadPool.getJobCount()", 0, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 1, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 100, threadPool.getMaxPoolSize());
    }

    @Test
    public void testConstructor2() throws Throwable {
        ThreadPool threadPool = new ThreadPool(0, 100);
        assertEquals("threadPool.getJobCount()", 0, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 1, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 100, threadPool.getMaxPoolSize());
    }

    @Test
    public void testConstructor3() throws Throwable {
        ThreadPool threadPool = new ThreadPool(1, 0);
        assertEquals("threadPool.getJobCount()", 0, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 1, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 100, threadPool.getMaxPoolSize());
    }

    @Test
    public void testConstructor4() throws Throwable {
        ThreadPool threadPool = new ThreadPool(2, 100);
        assertEquals("threadPool.getJobCount()", 0, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 2, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 100, threadPool.getMaxPoolSize());
    }

    @Test
    public void testRun1() throws Throwable {
        ThreadPool threadPool = new ThreadPool(1, 100);
        ISOUtil.sleep(50);
        threadPool.execute(new TestTask());
        ISOUtil.sleep(50);
        assertEquals("threadPool.getJobCount()", 1, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 1, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 100, threadPool.getMaxPoolSize());
        ISOUtil.sleep(500);
        assertEquals("threadPool.getJobCount()", 1, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 1, threadPool.getPoolSize());
    }

    @Test
    public void testRun2() throws Throwable {
        ThreadPool threadPool = new ThreadPool(1, 1);
        threadPool.execute(new TestTask());
        threadPool.execute(new TestTask());
        ISOUtil.sleep(50);
        assertEquals("threadPool.getJobCount()", 2, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 1, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 1, threadPool.getMaxPoolSize());
        assertEquals("threadPool.getPendingCount()", 1, threadPool.getPendingCount());
        ISOUtil.sleep(500);
        assertEquals("threadPool.getJobCount()", 2, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 1, threadPool.getPoolSize());
        assertEquals("threadPool.getPendingCount()", 0, threadPool.getPendingCount());
        assertEquals("threadPool.getMaxPoolSize()", 1, threadPool.getMaxPoolSize());
        ISOUtil.sleep(550);
        assertEquals("threadPool.getJobCount()", 2, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 1, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 1, threadPool.getMaxPoolSize());
    }

    @Test
    public void testRun3() throws Throwable {
        ThreadPool threadPool = new ThreadPool(1, 2);
        threadPool.execute(new TestTask());
        ISOUtil.sleep(20);
        threadPool.execute(new TestTask());
        ISOUtil.sleep(20);
        assertEquals("threadPool.getJobCount()", 2, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 2, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 2, threadPool.getMaxPoolSize());
    }

    @Test
    public void testRunNames() throws Throwable {
        ThreadPool threadPool = new ThreadPool(1, 2);
        threadPool.execute(new TestTask());
        ISOUtil.sleep(20);
        threadPool.execute(new TestTask());
        ISOUtil.sleep(20);
        Thread[] tl = new Thread[threadPool.activeCount()];
        threadPool.enumerate(tl);
        for (Thread t :tl )
            assertThat(t.getName(), new Matches("ThreadPool.PooledThread-\\d+-(running|idle)"));
    }

    @Ignore
    @Test
    public void testConcurrentThreadAllocation() throws Throwable {
        ThreadPool pool = new ThreadPool(1, 200, "Test-ThreadPool");
        Server server = new Server(pool);
        Thread serverThread = new Thread(server);
        serverThread.start();

        serverThread.join(15000);
        assertEquals("pool.getActiveCount()", 100, pool.getActiveCount());
        
        synchronized (server) {
            server.notifyAll();
        }
        pool.close();
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testIssue196 () {
        ThreadPool pool = new ThreadPool(10, 20, "thepool");

        int available = pool.getAvailableCount();
        int active = pool.getActiveCount();

        long end = System.currentTimeMillis() + 10000L; // run for 10 seconds top
        int i = 0;
        while (active >= 0 && System.currentTimeMillis() < end) {
            while (available > 0) {
                pool.execute(() -> { });
                available = pool.getAvailableCount();
            }
            available = pool.getAvailableCount();
            active = pool.getActiveCount();
        }
        assertTrue ("Active should be >= 0 but it is " + active, active >= 0);
    }

    private static class Job implements Runnable {
        private final int jobId;
        private final Object monitor;

        private Job(int jobId, Object monitor) {
            this.jobId = jobId;
            this.monitor = monitor;
        }

        @Override
        public void run() {
            synchronized (monitor) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private static class Server implements Runnable {
        private final ThreadPool pool;

        public Server(ThreadPool pool) {
            this.pool = pool;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                pool.execute(new Job(i, this));
            }
        }
    }



}
