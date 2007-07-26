package org.jpos.util;

import junit.framework.*;


public class ThroughputControlTestCase extends TestCase {
    public void testSingleThread () throws Exception {
        ThroughputControl tc = new ThroughputControl (2, 1000);
        long start = System.currentTimeMillis();
        tc.control(); 
        assertTrue (
            "Elapsed time should be less than one second", 
            System.currentTimeMillis() - start < 1000L
        );
        tc.control();
        assertTrue (
            "Elapsed time should still be less than one second", 
            System.currentTimeMillis() - start < 1000L
        );
        tc.control();
        assertTrue (
            "Elapsed time should be greater than one second", 
            System.currentTimeMillis() - start > 1000L
        );
        tc.control();
        assertTrue (
            "second transaction should be less than two seconds", 
            System.currentTimeMillis() - start < 2000L
        );
    }
    public void testFifty () throws Exception {
        ThroughputControl tc = new ThroughputControl (10, 1000);
        long start = System.currentTimeMillis();
        for (int i=0; i<50; i++)
            tc.control();

        long elapsed =  System.currentTimeMillis() - start;
        assertTrue (
            "50 transactions should take at least 4 seconds but took " + elapsed, 
            elapsed >= 4000L
        );
        assertTrue (
            "50 transactions shouldn't take more than aprox 4 seconds but took " + elapsed, 
            elapsed < 4300L
        );
    }
    public void testDualPeriod () throws Exception {
        ThroughputControl tc = new ThroughputControl (
            new int[] { 100, 150 }, 
            new int[] { 1000, 5000 }
        );
        long start = System.currentTimeMillis();
        for (int i=0; i<100; i++)
            tc.control();

        long elapsed =  System.currentTimeMillis() - start;
        assertTrue (
            "100 initial transactions should take more than about one second but took " + elapsed, 
            elapsed <= 1000L
        );
        for (int i=0; i<100; i++)
            tc.control();

        elapsed =  System.currentTimeMillis() - start;
        assertTrue (
            "100 additional transactions should take more than five seconds but took " + elapsed, 
            elapsed > 5000L
        );
    }
    public void testMultiThread() throws Exception {
        final ThroughputControl tc = new ThroughputControl (2, 1000);
        long start = System.currentTimeMillis();
        Thread[] t = new Thread[10];
        for (int i=0; i<10; i++) {
            t[i] = new Thread() {
                public void run() {
                    tc.control();
                }
            };
            t[i].start();
        }
        for (int i=0; i<10; i++) {
            t[i].join();
        }
        long elapsed =  System.currentTimeMillis() - start;
        assertTrue (
            "10 transactions should take about four seconds but took " + elapsed, 
            elapsed > 4000L && elapsed < 5000L
        );
    }
}

