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

import junit.framework.*;


public class ThroughputControlTestCase extends TestCase {
    public void testSingleThread () throws Exception {
        ThroughputControl tc = new ThroughputControl (2, 1000);
        long start = System.currentTimeMillis();
        assertTrue ("Control should return 0L", tc.control() == 0L);
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

