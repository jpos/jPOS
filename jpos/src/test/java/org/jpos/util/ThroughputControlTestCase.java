/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;


public class ThroughputControlTestCase {
    @Test
    public void testSingleThread () throws Exception {
        ThroughputControl tc = new ThroughputControl (2, 1000);
        Instant start = Instant.now();
        assertTrue (tc.control() == 0L, "Control should return 0L");
        assertTrue (
                Duration.between(start, Instant.now()).toMillis() < 1000L,
                "Elapsed time should be less than one second"
        );
        tc.control();
        assertTrue (
                Duration.between(start, Instant.now()).toMillis() < 1000L,
                "Elapsed time should still be less than one second"
        );
        tc.control();
        assertTrue (
                Duration.between(start, Instant.now()).toMillis() > 1000L,
                "Elapsed time should be greater than one second"
        );
        tc.control();
        assertTrue (
                Duration.between(start, Instant.now()).toMillis() < 2000L,
                "second transaction should be less than two seconds"
        );
    }
    @Test
    public void testFifty () throws Exception {
        ThroughputControl tc = new ThroughputControl (10, 1000);
        Instant start = Instant.now();
        for (int i=0; i<50; i++)
            tc.control();

        long elapsed = Duration.between(start, Instant.now()).toMillis();
        assertTrue (
            elapsed >= 4000L,
            "50 transactions should take at least 4 seconds but took " + elapsed
        );
        assertTrue (
            elapsed < 4500L,
            "50 transactions shouldn't take more than aprox 4 seconds but took " + elapsed
        );
    }
    @Test
    public void testDualPeriod () throws Exception {
        ThroughputControl tc = new ThroughputControl (
            new int[] { 100, 150 }, 
            new int[] { 1000, 5000 }
        );
        Instant start = Instant.now();
        for (int i=0; i<100; i++)
            tc.control();

        long elapsed = Duration.between(start, Instant.now()).toMillis();
        assertTrue (
            elapsed <= 1000L,
            "100 initial transactions should take more than about one second but took " + elapsed
        );
        for (int i=0; i<100; i++)
            tc.control();

        elapsed = Duration.between(start, Instant.now()).toMillis();
        assertTrue (
            elapsed >= 5000L,
            "100 additional transactions should take more than five seconds but took " + elapsed
        );
    }
    @Test
    public void testMultiThread() throws Exception {
        final ThroughputControl tc = new ThroughputControl (2, 1000);
        Instant start = Instant.now();
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
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        assertTrue (
            elapsed > 4000L && elapsed < 5000L,
            "10 transactions should take about four seconds but took " + elapsed
        );
    }
}

