package org.jpos.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ThreadPoolTest {

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
        assertEquals("threadPool.getPoolSize()", 100, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 100, threadPool.getMaxPoolSize());
    }

    @Test
    public void testConstructor2() throws Throwable {
        ThreadPool threadPool = new ThreadPool(0, 100);
        assertEquals("threadPool.getJobCount()", 0, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 100, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 100, threadPool.getMaxPoolSize());
    }

    @Test
    public void testConstructor3() throws Throwable {
        ThreadPool threadPool = new ThreadPool(1, 0);
        assertEquals("threadPool.getJobCount()", 0, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 100, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 100, threadPool.getMaxPoolSize());
    }

    @Test
    public void testConstructor4() throws Throwable {
        ThreadPool threadPool = new ThreadPool(1, 100);
        assertEquals("threadPool.getJobCount()", 0, threadPool.getJobCount());
        assertEquals("threadPool.getPoolSize()", 100, threadPool.getPoolSize());
        assertEquals("threadPool.getMaxPoolSize()", 100, threadPool.getMaxPoolSize());
    }
}
