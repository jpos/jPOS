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

import org.jpos.iso.ISOUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.internal.matchers.Matches;

public class ThreadPoolTest {

    class TestTask implements Runnable {
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
          assertThat(t.getName(), new Matches("PooledThread-\\d+-(running|idle)"));
    }

}
